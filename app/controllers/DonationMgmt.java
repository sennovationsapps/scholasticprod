package controllers;


import base.utils.AuthorisedNetPaymentUtil;
import base.utils.PaymentGatewayUtil;
import base.utils.PaymentUtils;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import models.*;
import models.Donation.DonationType;
import models.Donation.PaymentStatus;
import models.Donation.PaymentType;
import models.Pfp.PfpType;
import models.aws.S3File;
import models.security.SecurityRole;
import models.security.User;
import net.authorize.api.contract.v1.CreateTransactionResponse;
import net.authorize.sim.Fingerprint;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.LoggerFactory;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.db.ebean.Transactional;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.donations.*;
import views.html.index;
import views.html.profile.profileDonationsCreate;
import views.html.profile.profileDonationsReconcile;
import views.html.profile.profileMain;

import java.text.SimpleDateFormat;
import java.util.*;

import static play.data.Form.form;
/**
 * Manage a database of donations.
 */

public class DonationMgmt extends Controller {
	
	private static final org.slf4j.Logger PAYMENT_LOGGER = LoggerFactory.getLogger("ScholasticDonationsLogger");


	/*******************   08.01.2016  start ***********************/
//	private static final String API_LOGIN_ID ="5Cdy5N57";//my cred test mode account credential
//	 private static final String TRANSACTION_KEY = "726SBegW3a42w4p8";//my test mode account credential
	private static final String API_LOGIN_ID ="347HgbCTh5";//scholastic cred test mode account credential
	private static final String TRANSACTION_KEY = "8q8a44YT4PUG4j7k";//scholastic mode account credential
	/*private String getPaymentFormFields(String paymentGatewayamountAmount,String digitalWalletAmount,String serviceTax)throws Exception {
		String url="";
		try {
			System.out.println("New Enter in  getPaymentFormFields");

			Random randomGenerator = new Random();
			int randomSequence = randomGenerator.nextInt(1000);
			Fingerprint fingerprint = Fingerprint.createFingerprint(API_LOGIN_ID, TRANSACTION_KEY, randomSequence, paymentGatewayamountAmount);
			 url="https://test.authorize.net/gateway/transact.dll?"+
					"x_fp_sequence="+randomSequence+
					"&x_fp_timestamp="+fingerprint.getTimeStamp()+
					"&x_fp_hash="+fingerprint.getFingerprintHash()+
					"&x_version=3.1"+
					"&x_method=CC"+
					"&x_type=AUTH_CAPTURE"+
					"&x_amount=0.1"+
					"&x_login="+API_LOGIN_ID+
					"&x_description=Scholastic"+
					"&x_show_form=PAYMENT_FORM"+
					"&x_relay_response=true";


		}catch (Exception e){
			System.out.println();
		}
		redirect(url);
	}*/

	/*******************   08.01.2016  end ***********************/
	
	/**
	 * This result directly redirect to application home.
	 */
	public static Result	GO_HOME	= redirect(routes.DonationMgmt.list(0, "firstName", "asc", ""));
	
	public static Result create(Event event) {
		if (event.isIdOnly()) {
			event = Event.findByIdWithMinAndGeneralFund(event.id);
		}
		final Form<Donation> donationForm = form(Donation.class);
		return ok(createForm.render(event, event.generalFund, donationForm));
	}
	
	/**
	 * Display the 'new donation form'.
	 * 
	 * @param id
	 *            the id
	 * @param pfpId
	 *            the pfp id
	 * @return the result
	 */
	public static Result createWithPfp(Event event, Pfp pfp) {
		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		if (pfp.isIdOnly()) {
			pfp = Pfp.findByIdWithMin(pfp.id);
		}
		final Form<Donation> donationForm = form(Donation.class);
		return ok(createForm.render(event, pfp, donationForm));
	}
	
	/**
	 * Display the 'new donation form'.
	 * 
	 * @param id
	 *            the id
	 * @param pfpId
	 *            the pfp id
	 * @return the result
	 */
	public static Result createModalWithSponsor(Event event, Pfp pfp, Long sponsorItem) {
		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		if (pfp.isIdOnly()) {
			pfp = Pfp.findByIdWithMin(pfp.id);
		}
		final Form<Donation> donationForm = form(Donation.class);
		return ok(views.html.sponsors.createDonationForm.render(event, pfp, SponsorItem.findById(sponsorItem),
						donationForm));
	}
	
	/**
	 * Display the 'edit form' of a existing Donation.
	 * 
	 * @param id
	 *            Id of the donation to edit
	 * @param pfpId
	 *            the pfp id
	 * @param donationId
	 *            the donation id
	 * @return the result
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN), @Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
					@Group(SecurityRole.EVENT_ASSIST) })
	@SubjectPresent
	public static Result edit(Event event, Pfp pfp, Long donationId) {
		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		if (pfp.isIdOnly()) {
			pfp = Pfp.findByIdWithMin(pfp.id);
		}
		if (!Event.canManage(ControllerUtil.getLocalUser(session()), event)) {
			flash(ControllerUtil.FLASH_DANGER_KEY,
							"The requested event action cannot be completed by the logged in user.");
			return redirect(routes.Application.index());
		}
		final Form<Donation> donationForm = form(Donation.class).fill(Donation.findById(donationId));
		return ok(editForm.render(event, pfp, donationId, donationForm));
	}
	
	/**
	 * Display the 'edit form' of a existing Donation.
	 * 
	 * @param donationId
	 *            the donation id
	 * @return the result
	 */
	@SubjectPresent
	public static Result get(Long donationId) {
		final Donation donation = Donation.findById(donationId);
		return ok(viewDonation.render(donation));
	}
	
	/**
	 * Display the 'edit form' of a existing Donation.
	 * 
	 * @param id
	 *            Id of the donation to edit
	 * @param donationId
	 *            the donation id
	 * @return the result
	 */
	@SubjectPresent
	public static Result get(Event event, Long donationId) {
		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		return DonationMgmt.get(donationId);
	}
	
	/**
	 * Handle default path requests, redirect to computers list.
	 * 
	 * @return the result
	 */
	@SubjectPresent
	public static Result index() {
		return GO_HOME;
	}
	
	/**
	 * Profile donations.
	 * 
	 * @return the result
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN), @Group(SecurityRole.SYS_ADMIN) })
	@SubjectPresent
	@Transactional
	public static Result profileDonationsReconcile() {
		final Form<Donation.DonationReconciliation> reconcileForm = form(Donation.DonationReconciliation.class)
						.bindFromRequest();
		if (reconcileForm.hasErrors()) {
			Logger.debug("Has errors {}", reconcileForm.errorsAsJson());
			return badRequest(profileDonationsReconcile.render(ControllerUtil.getLocalUser(session()), reconcileForm));
		}
		Map<String, Map<String, String>> ccProps = PaymentUtils.sendCCReconcile(reconcileForm.get().reconcileFrom,
						reconcileForm.get().reconcileTo);
		List<Donation> donations = Donation.findByFromAndToDate(reconcileForm.get().reconcileFrom,
						reconcileForm.get().reconcileTo);
		Logger.debug("The list of found donations - " + donations.size());
		for (Donation donation : donations) {
			if (ccProps.containsKey(donation.transactionNumber)) {
				Map<String, String> props = ccProps.get(donation.transactionNumber);
				if (StringUtils.equalsIgnoreCase(props.get("ssl_trans_status"), "STL")) {
				/*	if(donation.status == PaymentStatus.APPROVED && StringUtils.equalsIgnoreCase(props.get
							("ssl_transaction_type"), "Sale")) {
						Logger.debug("This transaction has been updated to settled or cleared - {}", donation.id);
						donation.status = PaymentStatus.CLEARED;
						donation.datePaid = new Date();
						donation.update();
					}*/
					if(donation.status == PaymentStatus.INITIATED && StringUtils.equalsIgnoreCase(props.get
							("ssl_transaction_type"), "Sale")) {
						Logger.debug("This transaction has been updated to settled or cleared - {}", donation.id);
						donation.status = PaymentStatus.CLEARED;
						donation.datePaid = new Date();
						donation.update();
					}
					else if(donation.status != PaymentStatus.REFUNDED && (StringUtils.equalsIgnoreCase(props.get("ssl_transaction_type"), "Void") || StringUtils.equalsIgnoreCase(props.get("ssl_transaction_type"), "Return"))) {
						Logger.debug("This transaction has been updated to returned - {}", donation.id);
						donation.status = PaymentStatus.REFUNDED;
						donation.datePaid = new Date();
						donation.update();
					}
					else {
						Logger.debug("This transaction {} has already been updated to refunded or cleared - {}", donation.transactionNumber, donation.id);
					}
				} else {
					Logger.debug("This transaction {} had a status of {}", donation.transactionNumber,
									props.get("ssl_trans_status"));
				}
			}
		}
		// if(StringUtils.isNotEmpty(ccProps.getProperty("errorCode"))) {
		// Logger.error("Failed to submit reconcile to Virtual Merchant with error code [{}] and error message [{}]",
		// ccProps.get("errorName"), ccProps.get("errorMessage"));
		// flash(ControllerUtil.FLASH_WARNING_KEY,
		// "Unable to process donation, please try your request again.  If the issue persists, please contact Scholastic Challenge.");
		// } else {
		// flash(ControllerUtil.FLASH_SUCCESS_KEY,
		// "Donation reconciliation has been processed.");
		// }
		
		return redirect(routes.Application.profileSearchDonations(0, "dateCreated",
						"asc", "", "dateCreated"));
	}
	
	/**
	 * Display the paginated list of events.
	 * 
	 * @param page
	 *            Current page number (starts from 0)
	 * @param sortBy
	 *            Column to be sorted
	 * @param order
	 *            Sort order (either asc or desc)
	 * @param filter
	 *            Filter applied on event names
	 * @return the result
	 */
	@SubjectPresent(content = "/login")
	public static Result list(int page, String sortBy, String order, String filter) {
		return ok(list.render(Donation.page(page, 10, sortBy, order, filter), sortBy, order, filter));
	}
	
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN), @Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
					@Group(SecurityRole.EVENT_ASSIST) })
	@SubjectPresent
	@Transactional
	public static Result refund(Event event, Pfp pfp, Long donationId) {
		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		if (pfp.isIdOnly()) {
			pfp = Pfp.findByIdWithMin(pfp.id);
		}
		final Map<String, String> donationMap = form(Donation.class).data();
		Donation donation = Donation.findById(donationId);
		Logger.debug("Donation to be refunded " + ToStringBuilder.reflectionToString(donation));
		Map<String, String> ccProps = PaymentUtils.sendCCRefund(donation);
		if (ccProps.containsKey("errorCode")) {
			Logger.error("Failed to submit transaction to Virtual Merchant with error code [{}] and error message [{}]",
							ccProps.get("errorName"), ccProps.get("errorMessage"));
			flash(ControllerUtil.FLASH_WARNING_KEY,
							"Unable to process donation, please try your request again.  If the issue persists, please contact Scholastic Challenge.");
		} else {
			donation.status = PaymentStatus.REFUNDED;
			donation.update();
			flash(ControllerUtil.FLASH_SUCCESS_KEY, "Donation has been refunded");
			PAYMENT_LOGGER.info("A CC Refund [transaction number {}] was made for PFP [{}] in the amount of [{}] by [{} {}] with CC [{}]", donation.transactionNumber, donation.pfp.name, donation.amount, donation.firstName, donation.lastName, donation.ccDigits);
		}
		return redirect(routes.Application.profileSearchDonations(0, "dateCreated",
				"asc", "", "dateCreated"));
	}
	
	/**
	 * Handle the 'new donation form' submission.
	 * 
	 * @param id
	 *            the id
	 * @return the result
	 */
	// @Restrict({ @Group(SecurityRole.ROOT_ADMIN),
	// @Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
	// @Group(SecurityRole.EVENT_ASSIST) })



	public static Result validateAndSendCreditInfo(Event event){
		System.out.println("within validateAndSendCreditInfo event ::"+event);

		if (event.isIdOnly()) {
			event = Event.findByIdWithMinAndGeneralFund(event.id);
		}

		final Form<Donation> donationForm = form(Donation.class).bindFromRequest();
		Map<String, String> ccFormErrors = PaymentUtils.validateCreditForm(donationForm.data());
		if(MapUtils.isNotEmpty(ccFormErrors)) {
			System.out.println("within validateAndSendCreditInfo event1 ::"+event);
			for(String key: ccFormErrors.keySet()) {
				if(StringUtils.isEmpty(key)) {
					donationForm.reject(ccFormErrors.get(key));
				} else {
					donationForm.reject(key, ccFormErrors.get(key));
				}
			}
			if (donationForm.hasErrors()) {
				System.out.println("within donationform.haserrors");
				Logger.debug("Has errors {}", donationForm.errorsAsJson());
				//return ok(creditForm.render(event,donationForm.get().pfp,donationForm));
				//return badRequest(createForm.render(event, event.generalFund, donationForm));
				return badRequest(creditForm.render(event, event.generalFund, donationForm));
			}


		}
//		String WorldPlayUrl=null;
//		Donation donation = donationForm.get();
//		try {
//			WorldPlayUrl=WorldPayUtils.checkout(String.valueOf(donation.amount),donation.transactionNumber,donation.email,donation.ccName,donation.ccNum);
//		} catch (BadPaddingException e) {
//			e.printStackTrace();
//		} catch (NoSuchAlgorithmException e) {
//			e.printStackTrace();
//		} catch (IllegalBlockSizeException e) {
//			e.printStackTrace();
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		} catch (NoSuchPaddingException e) {
//			e.printStackTrace();
//		} catch (InvalidKeyException e) {
//			e.printStackTrace();
//		} catch (InvalidKeySpecException e) {
//			e.printStackTrace();
//		}
//		System.out.println("within validateAndSendCreditInfo now URL sdfdsfsfsfsdfsdfsdfsdfdsfdsfsdfsdfdsf"+WorldPlayUrl);

		//return redirect(WorldPlayUrl);

		//return redirect("https://secure-test.worldpay.com/wcc/purchase?instId=&cartId=&currency=&desc=&testMode=100");
		return redirect(" ");
		//return ok(creditForm.render(event, donationForm.get().pfp, donationForm));


	}







//=================returning page to the credit form================start============03.09.2015=======================//



	public static Result returnCreditCardInfo() throws Exception{
		System.out.println("within returnCreditCardInfo..");
		/*Map<String,String[]> val=request().queryString();
		for (Map.Entry<String, String[]> entry : val.entrySet()) {
			String[] val1=entry.getValue();
			System.out.println("Key : " + entry.getKey() + " Value : " + val1[0]);
		}*/
		TripleDES tripleDES = new TripleDES();
		Map<String,String[]> valForDecrypt=request().queryString();
		for (Map.Entry<String, String[]> entry : valForDecrypt.entrySet()) {
			String[] val1=entry.getValue();
			System.out.println("Key : " + entry.getKey() + " Value : " + val1);
			System.out.println("val1.length :: "+val1.length);
			for(int i = 0; i<val1.length;i++){
				System.out.println("val1[i] :: "+val1[i]+" for i  "+i);
				String actualText =  tripleDES.decrypt(val1[i]);
				System.out.println("actualText :: "+actualText);

			}

		}



		String texdt=Http.Context.current().request().body().asText();
		System.out.println("texdt==> "+texdt);
		return ok(returnCreditForm.render());
	}



	public static Result returnCreditInfo() throws Exception{
		System.out.println("within return creditInfo..");
		Map<String,String[]> val=request().queryString();
		for (Map.Entry<String, String[]> entry : val.entrySet()) {
			System.out.println("Key post : " + entry.getKey() + " Value post: " + entry.getValue());
		}
		//System.out.println("Value---"+val);

		TripleDES tripleDES = new TripleDES();
		Map<String,String[]> valForDecrypt=request().queryString();
		for (Map.Entry<String, String[]> entry : valForDecrypt.entrySet()) {
			String[] val1=entry.getValue();
			System.out.println("Key post: " + entry.getKey() + " Value post: " + val1[0]);
			for(int i = 0; i<val1.length;i++){

				String actualText =  tripleDES.decrypt(val1[i]);
				System.out.println("actualText post:: "+actualText);

			}

		}


		String texdt=Http.Context.current().request().body().asText();
		System.out.println("texdt==> "+texdt);
		return ok(returnCreditForm.render());
	}






	public static Result returnCreditCardSuccessInfo() throws Exception{
		System.out.println("within returnCreditCardSuccessInfo..");
		/*Map<String,String[]> val=request().queryString();
		for (Map.Entry<String, String[]> entry : val.entrySet()) {
			String[] val1=entry.getValue();
			System.out.println("Key : " + entry.getKey() + " Value : " + val1[0]);
		}*/
		//TripleDES tripleDES = new TripleDES();
		Map<String,String[]> valForDecrypt=request().queryString();
		for (Map.Entry<String, String[]> entry : valForDecrypt.entrySet()) {
			String[] val1=entry.getValue();
			System.out.println("Key returnCreditCardSuccessInfo : " + entry.getKey() + " Value : " + val1);
			System.out.println("val1.length :: "+val1.length);
			for(int i = 0; i<val1.length;i++){
				System.out.println("val1[i] returnCreditCardSuccessInfo :: "+val1[i]+" for i  "+i);
				//String actualText =  tripleDES.decrypt(val1[i]);
				//System.out.println("actualText :: "+actualText);

			}

		}



		String texdt=Http.Context.current().request().body().asText();
		System.out.println("texdt==> "+texdt);
		return ok(returnCreditForm.render());
	}



	public static Result returnCreditCardFailInfo() throws Exception{
		System.out.println("within return returnCreditCardFailInfo..");
		Map<String,String[]> val=request().queryString();
		for (Map.Entry<String, String[]> entry : val.entrySet()) {
			System.out.println("Key post returnCreditCardFailInfo: " + entry.getKey() + " Value post: " + entry.getValue());
		}
		//System.out.println("Value---"+val);

		//TripleDES tripleDES = new TripleDES();
		Map<String,String[]> valForDecrypt=request().queryString();
		for (Map.Entry<String, String[]> entry : valForDecrypt.entrySet()) {
			String[] val1=entry.getValue();
			System.out.println("Key post: " + entry.getKey() + " Value post: " + val1[0]);
			for(int i = 0; i<val1.length;i++){

				//	String actualText =  tripleDES.decrypt(val1[i]);
				//	System.out.println("actualText post:: "+actualText);

			}

		}


		String texdt=Http.Context.current().request().body().asText();
		System.out.println("texdt==> "+texdt);
		return ok(returnCreditForm.render());
	}


	//=================returning page to the credit form================end============03.09.2015=======================//





	//=====================validate donation by cash==================start=============02.10.2015=======================//
	public static List<Donation> donationList = new ArrayList<Donation>();
	public static Result validateandStoreDonationByCash(Event event) {
		System.out.println("within validateandStoreDonationByCash :");
		final Form<Donation> donationForm = form(Donation.class).bindFromRequest();
		System.out.println("eventId within validateandStoreDonationByCash :: " + event.id);
		Event eventFromId = Event.findById(event.id);
		final User localUser = ControllerUtil.getLocalUser(session());
		//Sponsors sponsors = Sponsors.findbyspon
		List<Event> events = new ArrayList<Event>();
		if (ControllerUtil.isUserInRole(SecurityRole.ROOT_ADMIN)) {
			events = Event.findAllEvents();
		} else if (ControllerUtil.isUserInRole(SecurityRole.EVENT_ADMIN)) {
			events = Event.findAllByUserId(localUser.id);
		}


		List<Donation> donations = Donation.findAllByEventId(event.id);
		List<Pfp> pfps = Pfp.findByEventId(event.id);
		System.out.println("before test");
		//return ok(test.render(donations));
		donations = new ArrayList<Donation>();
		/*Long eventId = Long.parseLong(eventIdFromRequest);
		Long pageId = Long.parseLong(pageIdFromRequest);*/
		// Event event = Event.findById(event.id);


		// System.out.println("donationForm.get() :: "+donationForm.get());

		/*if(StringUtils.isEmpty(donationForm.data().get("email"))){
			donationForm.data().put("email","rimi@gmail.com");

		}*/

		System.out.println("donationForm.data().get(email) :: "+donationForm.data().get("email"));
		System.out.println(("donationForm.data().get(pfp.id) :: " + donationForm.data().get("pfp.id")));
		if (donationForm.hasErrors()) {







			System.out.println("donationform has errors..");
			System.out.println("statusss :: "+donationForm.data().get("statusOfBulkCashDonation"));

			if(donationForm.data().get("statusOfBulkCashDonation").equals("1")){
				if(donationList!=null && donationList.size()>0){
					System.out.println("pfpId :: "+donationForm.data().get("pfp.id")+" :: collectionTypes :: "+donationForm.data().get("collectionTypes")+":: amount ::"+StringUtils.isEmpty(donationForm.data().get("amount"))+":: email :: "+donationForm.data().get("email1"));
					if(StringUtils.isEmpty(donationForm.data().get("pfp.id"))  && (StringUtils.isEmpty(donationForm.data().get("amount")) || donationForm.data().get("amount").equalsIgnoreCase("true"))){
						System.out.println("email1 :: "+ donationForm.data().get("email1") +" :: email :: "+donationForm.data().get("email"));
						if( StringUtils.isEmpty(donationForm.data().get("email"))){
							Iterator donationListItr = donationList.iterator();
							/*while(donationListItr.hasNext()){
								Donation donation1 = (Donation)donationListItr.next();
								donation1.save();


								Donation updatedDonation = Donation.findById(donation1.id);
								System.out.println("after save :: " + donation1.status);
								// final Pfp pfp = Pfp.findById(donationForm.get().pfp.id);

								ReceiptMgmt.sendSponsoredMsg(updatedDonation);
								System.out.println("event :: " + event);
								System.out.println("updatedDonation.event :: "+updatedDonation.event);


								//return ok(ProfileCashDonations.render(localUser, events, eventForPfp, pfps, donationForm1, donations, donationList));

								//return redirect(routes.ReceiptMgmt.getAndSendCashReceipt(updatedDonation.event, updatedDonation));
							}*/
							donationList =  new ArrayList<Donation>();
							final Form<Donation> donationForm2 = form(Donation.class);
							flash(ControllerUtil.FLASH_SUCCESS_KEY, "Bulk Cash donations has been submitted successfully. ");
							return ok(ProfileCashDonations.render(localUser, events, eventFromId, pfps, donationForm2, donations, donationList));

						}else{
							System.out.println("form has values ...");
							Logger.debug("Has errors {}", donationForm.errorsAsJson());
							if (StringUtils.isEmpty(donationForm.data().get("pfp.id"))) {



								return badRequest(ProfileCashDonations.render(localUser, events, eventFromId, pfps, donationForm, donations, donationList));
								// return badRequest(createForm.render(event, event.generalFund, donationForm));
							}
							if(Integer.parseInt(donationForm.data().get("amount"))<5) {
								donationForm.reject("amount", "The minimum donation is $5.00, please make the correction to proceed.");
								return badRequest(ProfileCashDonations.render(localUser, events, eventFromId, pfps, donationForm, donations, donationList));
							}
							/*else if(StringUtils.isEmpty(donationForm.data().get("collectionTypes")) || donationForm.data().get("collectionTypes").equalsIgnoreCase("select")){
								System.out.println("CollectionTypes :: "+donationForm.data().get("collectionTypes"));
								donationForm.reject("collectionTypes", "Please select the Cash Collection Types.");
								if (donationForm.hasErrors()) {
									Logger.debug("Has errors {}", donationForm.errorsAsJson());
									return badRequest(views.html.donations.ProfileCashDonations.render(localUser, events, eventFromId, pfps, donationForm, donations, donationList));
								}

							}else if(StringUtils.isEmpty(donationForm.data().get("email1"))){
								donationForm.reject("email1", "Please enter the valid Email Id.");
								if (donationForm.hasErrors()) {
									Logger.debug("Has errors {}", donationForm.errorsAsJson());
									return badRequest(views.html.donations.ProfileCashDonations.render(localUser, events, eventFromId, pfps, donationForm, donations, donationList));
								}
							}*/
							else {
								System.out.println("within baad req.");
								//return badRequest(createForm.render(event, donationForm.get().pfp, donationForm)); //30.07.2015
								return badRequest(ProfileCashDonations.render(localUser, events, eventFromId, pfps, donationForm, donations, donationList));
							}
						}
					}else{
						System.out.println("form has values111 ...");
						Logger.debug("Has errors {}", donationForm.errorsAsJson());
						if (StringUtils.isEmpty(donationForm.data().get("pfp.id"))) {



							return badRequest(ProfileCashDonations.render(localUser, events, eventFromId, pfps, donationForm, donations, donationList));
							// return badRequest(createForm.render(event, event.generalFund, donationForm));
						}
						if(Integer.parseInt(donationForm.data().get("amount"))<5) {
							donationForm.reject("amount", "The minimum donation is $5.00, please make the correction to proceed.");
							return badRequest(ProfileCashDonations.render(localUser, events, eventFromId, pfps, donationForm, donations, donationList));
						}
						/*else if(StringUtils.isEmpty(donationForm.data().get("collectionTypes")) || donationForm.data().get("collectionTypes").equalsIgnoreCase("select")){
							System.out.println("CollectionTypes :: "+donationForm.data().get("collectionTypes"));
							donationForm.reject("collectionTypes", "Please select the Cash Collection Types.");
							if (donationForm.hasErrors()) {
								Logger.debug("Has errors {}", donationForm.errorsAsJson());
								return badRequest(views.html.donations.ProfileCashDonations.render(localUser, events, eventFromId, pfps, donationForm, donations, donationList));
							}

						}else if(StringUtils.isEmpty(donationForm.data().get("email1"))){
							donationForm.reject("email1", "Please enter the valid Email Id.");
							if (donationForm.hasErrors()) {
								Logger.debug("Has errors {}", donationForm.errorsAsJson());
								return badRequest(views.html.donations.ProfileCashDonations.render(localUser, events, eventFromId, pfps, donationForm, donations, donationList));
							}
						}*/
						else {
							System.out.println("within baad req.");
							//return badRequest(createForm.render(event, donationForm.get().pfp, donationForm)); //30.07.2015
							return badRequest(ProfileCashDonations.render(localUser, events, eventFromId, pfps, donationForm, donations, donationList));
						}
					}
				}else if(donationList!=null && donationList.size() == 0){
					System.out.println("size of donationList ::"+donationList.size());
					Logger.debug("Has errors {}", donationForm.errorsAsJson());
					if (StringUtils.isEmpty(donationForm.data().get("pfp.id"))) {



						return badRequest(ProfileCashDonations.render(localUser, events, eventFromId, pfps, donationForm, donations, donationList));
						// return badRequest(createForm.render(event, event.generalFund, donationForm));
					}
					if(Integer.parseInt(donationForm.data().get("amount"))<5) {
						donationForm.reject("amount", "The minimum donation is $5.00, please make the correction to proceed.");
						return badRequest(ProfileCashDonations.render(localUser, events, eventFromId, pfps, donationForm, donations, donationList));
					}
					/*else if(StringUtils.isEmpty(donationForm.data().get("collectionTypes")) || donationForm.data().get("collectionTypes").equalsIgnoreCase("select")){
						System.out.println("CollectionTypes :: "+donationForm.data().get("collectionTypes"));
						donationForm.reject("collectionTypes", "Please select the Cash Collection Types.");
						if (donationForm.hasErrors()) {
							Logger.debug("Has errors {}", donationForm.errorsAsJson());
							return badRequest(views.html.donations.ProfileCashDonations.render(localUser, events, eventFromId, pfps, donationForm, donations, donationList));
						}

					}else if(StringUtils.isEmpty(donationForm.data().get("email1"))){
						donationForm.reject("email1", "Please enter the valid Email Id.");
						if (donationForm.hasErrors()) {
							Logger.debug("Has errors {}", donationForm.errorsAsJson());
							return badRequest(views.html.donations.ProfileCashDonations.render(localUser, events, eventFromId, pfps, donationForm, donations, donationList));
						}
					}*/
					else {
						System.out.println("within baad req11.");
						//return badRequest(createForm.render(event, donationForm.get().pfp, donationForm)); //30.07.2015
						return badRequest(ProfileCashDonations.render(localUser, events, eventFromId, pfps, donationForm, donations, donationList));
					}
				}
			}else if(donationForm.data().get("statusOfBulkCashDonation").equals("0")){
				System.out.println("statusOfBulkCashDonation == 0");
				System.out.println("donationForm.data().get(email) 1111:: "+donationForm.data().get("email"));
				Logger.debug("Has errors {}", donationForm.errorsAsJson());
				if (StringUtils.isEmpty(donationForm.data().get("pfp.id"))) {



					return badRequest(ProfileCashDonations.render(localUser, events, eventFromId, pfps, donationForm, donations, donationList));
					// return badRequest(createForm.render(event, event.generalFund, donationForm));
				}
				else if(Integer.parseInt(donationForm.data().get("amount"))<5) {
					donationForm.reject("amount", "The minimum donation is $5.00, please make the correction to proceed.");
					return badRequest(ProfileCashDonations.render(localUser, events, eventFromId, pfps, donationForm, donations, donationList));
				}
				/*else if(StringUtils.isEmpty(donationForm.data().get("collectionTypes")) || donationForm.data().get("collectionTypes").equalsIgnoreCase("select")){
					System.out.println("CollectionTypes :: "+donationForm.data().get("collectionTypes"));
					donationForm.reject("collectionTypes", "Please select the Cash Collection Types.");
					if (donationForm.hasErrors()) {
						Logger.debug("Has errors {}", donationForm.errorsAsJson());
						return badRequest(views.html.donations.ProfileCashDonations.render(localUser, events, eventFromId, pfps, donationForm, donations, donationList));
					}*/

				else if(StringUtils.isEmpty(donationForm.data().get("email"))){
				System.out.println("email error");
					donationForm.reject("email", "Please enter the valid Email Id.");

						Logger.debug("Has errors {}", donationForm.errorsAsJson());
						return badRequest(ProfileCashDonations.render(localUser, events, eventFromId, pfps, donationForm, donations, donationList));

				}
				else if(StringUtils.isEmpty(donationForm.data().get("phone"))){
					System.out.println("phone error");
					donationForm.reject("phone", "Please enter the valid Email Id.");

					Logger.debug("Has errors {}", donationForm.errorsAsJson());
					return badRequest(ProfileCashDonations.render(localUser, events, eventFromId, pfps, donationForm, donations, donationList));

				}
				else if(StringUtils.isEmpty(donationForm.data().get("phPart1"))){
					System.out.println("phPart1 error");
					donationForm.reject("phone", "Please enter the valid Email Id.");

					Logger.debug("Has errors {}", donationForm.errorsAsJson());
					return badRequest(ProfileCashDonations.render(localUser, events, eventFromId, pfps, donationForm, donations, donationList));

				}
				else if(StringUtils.isEmpty(donationForm.data().get("phPart2"))){
					System.out.println("phPart2 error");
					donationForm.reject("phone", "Please enter the valid Email Id.");

					Logger.debug("Has errors {}", donationForm.errorsAsJson());
					return badRequest(ProfileCashDonations.render(localUser, events, eventFromId, pfps, donationForm, donations, donationList));

				}
				else if(StringUtils.isEmpty(donationForm.data().get("phPart3"))){
					System.out.println("phPart3 error");
					donationForm.reject("phone", "Please enter the valid Email Id.");

					Logger.debug("Has errors {}", donationForm.errorsAsJson());
					return badRequest(ProfileCashDonations.render(localUser, events, eventFromId, pfps, donationForm, donations, donationList));

				}

				else {
					System.out.println("within baad req.");
					//return badRequest(createForm.render(event, donationForm.get().pfp, donationForm)); //30.07.2015
					return badRequest(ProfileCashDonations.render(localUser, events, eventFromId, pfps, donationForm, donations, donationList));
				}
			}





		/*	Logger.debug("Has errors {}", donationForm.errorsAsJson());
			if (StringUtils.isEmpty(donationForm.data().get("pfp.id"))) {


				return badRequest(views.html.donations.ProfileCashDonations.render(localUser, events, eventFromId, pfps, donationForm, donations, donationList));
				// return badRequest(createForm.render(event, event.generalFund, donationForm));
			}else if(StringUtils.isEmpty(donationForm.data().get("collectionTypes")) || donationForm.data().get("collectionTypes").equalsIgnoreCase("select")){
				System.out.println("CollectionTypes :: "+donationForm.data().get("collectionTypes"));
				donationForm.reject("collectionTypes", "Please select the Cash Collection Types.");
				if (donationForm.hasErrors()) {
					Logger.debug("Has errors {}", donationForm.errorsAsJson());
					return badRequest(views.html.donations.ProfileCashDonations.render(localUser, events, eventFromId, pfps, donationForm, donations, donationList));
				}

			}else if(StringUtils.isEmpty(donationForm.data().get("email1"))){
				donationForm.reject("email1", "Please enter the valid Email Id.");
				if (donationForm.hasErrors()) {
					Logger.debug("Has errors {}", donationForm.errorsAsJson());
					return badRequest(views.html.donations.ProfileCashDonations.render(localUser, events, eventFromId, pfps, donationForm, donations, donationList));
				}
			}else {
				System.out.println("within baad req.");
				//return badRequest(createForm.render(event, donationForm.get().pfp, donationForm)); //30.07.2015
				return badRequest(views.html.donations.ProfileCashDonations.render(localUser, events, eventFromId, pfps, donationForm, donations, donationList));
			}*/
		}


		if (StringUtils.isEmpty(donationForm.data().get("pfp.id"))) {
			donationForm.reject("pfp.id", "Please select the Participant that you would like to donate to.  The General Fund donations are made to the Event only and not an individual Participant.");
			if (donationForm.hasErrors()) {
				Logger.debug("Has errors {}", donationForm.errorsAsJson());
				return badRequest(ProfileCashDonations.render(localUser, events, eventFromId, pfps, donationForm, donations, donationList));
			}
		}
		if(Integer.parseInt(donationForm.data().get("amount"))<5) {
			donationForm.reject("amount", "The minimum donation is $5.00, please make the correction to proceed.");
			if (donationForm.hasErrors()) {
				Logger.debug("Has errors {}", donationForm.errorsAsJson());
				return badRequest(ProfileCashDonations.render(localUser, events, eventFromId, pfps, donationForm, donations, donationList));
			}
		}

		//========new add=====================start==================//
		/*if (StringUtils.isEmpty(donationForm.data().get("collectionTypes")) || donationForm.data().get("collectionTypes").equalsIgnoreCase("select")) {
			donationForm.reject("collectionTypes", "Please select the Collection Types.");
			if (donationForm.hasErrors()) {
				Logger.debug("Has errors {}", donationForm.errorsAsJson());
				return badRequest(views.html.donations.ProfileCashDonations.render(localUser, events, eventFromId, pfps, donationForm, donations, donationList));
			}
		}*/
		//=======new add======================end==================//

		Event eventForPfp = Event.findById(event.id);


		Donation donation = donationForm.get(); // donation form in donation variable fdatsuv
		final Form<Donation> donationForm1 = form(Donation.class);
		System.out.println("donation.statusOfBulkCashDonation :: "+donation.statusOfBulkCashDonation);
		if(donation.statusOfBulkCashDonation.equals("1")){
			donation.dateCreated = new Date();
			if(event.id != Pfp.findEventIdByPfpId(donation.pfp.id)) {
				PAYMENT_LOGGER.warn("The donation [{}] being made for pfp [{}] does not match the event [{}].", donation.id, donation.pfp.id, donation.event.id);
				donation.event = donation.pfp.event;
			} else {
				donation.event = event;
			}
			donation.invoiceNumber = donation.event.id + "_" + donation.dateCreated.getTime();
			System.out.println("invoiceNumber :: "+donation.invoiceNumber);
			/*if(donation.collectionTypes.equalsIgnoreCase("COMMUNITY_COLLECTION")){
				donation.phone = " ";
			}else{
				donation.phone = ControllerUtil.stripPhone(donation.phone);
			}


			if(donation.collectionTypes.equalsIgnoreCase("INDIVIDUAL")){
				donation.email = " ";
			}

			System.out.println("phone :: "+donation.phone);
			if(StringUtils.isNotEmpty(donation.donorMessage)) {
				donation.donorMessage = donation.donorMessage.replaceAll("\\p{P}", "");
			}

			if(donation.collectionTypes.equalsIgnoreCase("COMMUNITY_COLLECTION")){
				donation.donorName = "Community Collection";
			}*/
			if (StringUtils.isEmpty(donation.donorName) && StringUtils.isNotEmpty(donation.donorMessage)) {
				donation.donorName = "Anonymous";
			}

			donation.donationType = DonationType.GENERAL;
			donation.paymentType = PaymentType.CASH;
			System.out.println("elseee in donation type");
			donation.status = PaymentStatus.CLEARED;
			donation.datePaid = new Date();

			//==============new add==========start================14.10.2015========================================//

			donation.email = " ";
			donation.phone = " ";
			donation.phPart1 = " ";
			donation.phPart2 = " ";
			donation.phPart3 = " ";

			//==============new add===========end=================14.10.2015========================================//

			donationList.add(donation);

			//===========new add=================start===================08.10.2015==========================//
			donation.save();
			donationList =  new ArrayList<Donation>();
			Donation updatedDonation = Donation.findById(donation.id);
			System.out.println("after save :: " + donation.status);
			final Pfp pfp = Pfp.findById(donationForm.get().pfp.id);
			ReceiptMgmt.sendSponsoredMsg(updatedDonation);

			ReceiptMgmt.sendCashDonationMsgToEventManager(updatedDonation);
			//System.out.println("event :: " + event);
			//System.out.println("updatedDonation.event :: " + updatedDonation.event);
			//===========new add==================end====================08.10.2015==========================//

		/*	if(donationList!= null && donationList.size()>0){
				Iterator donationListItr = donationList.iterator();
				while(donationListItr.hasNext()){
					donation = (Donation)donationListItr.next();
					donation.save();

					donationList =  new ArrayList<Donation>();
					Donation updatedDonation = Donation.findById(donation.id);
					System.out.println("after save :: " + donation.status);
					final Pfp pfp = Pfp.findById(donationForm.get().pfp.id);

					ReceiptMgmt.sendSponsoredMsg(updatedDonation);
					System.out.println("event :: " + event);
					System.out.println("updatedDonation.event :: "+updatedDonation.event);

					//return ok(ProfileCashDonations.render(localUser, events, eventForPfp, pfps, donationForm1, donations, donationList));

					//return redirect(routes.ReceiptMgmt.getAndSendCashReceipt(updatedDonation.event, updatedDonation));
				}

			}else{
				//return ok(ProfileCashDonations.render(localUser, events, eventForPfp, pfps, donationForm1, donations, donationList));
			}*/
			flash(ControllerUtil.FLASH_SUCCESS_KEY, "Bulk Cash donations has been submitted successfully. ");

		}
		else if(donation.statusOfBulkCashDonation.equals("0")){
			System.out.println("donation.statusOfBulkCashDonation == 0");
			donation.dateCreated = new Date();
			if (event.id != Pfp.findEventIdByPfpId(donation.pfp.id)) {
				PAYMENT_LOGGER.warn("The donation [{}] being made for pfp [{}] does not match the event [{}].", donation.id, donation.pfp.id, donation.event.id);
				donation.event = donation.pfp.event;
			} else {
				donation.event = event;
			}
			donation.invoiceNumber = donation.event.id + "_" + donation.dateCreated.getTime();
			System.out.println("invoiceNumber :: " + donation.invoiceNumber);
			/*if (donation.collectionTypes.equalsIgnoreCase("COMMUNITY_COLLECTION")) {
				donation.phone = " ";
			} else {
				donation.phone = ControllerUtil.stripPhone(donation.phone);
			}


			if (donation.collectionTypes.equalsIgnoreCase("INDIVIDUAL")) {
				donation.email = " ";
			}

			System.out.println("phone :: " + donation.phone);
			if (StringUtils.isNotEmpty(donation.donorMessage)) {
				donation.donorMessage = donation.donorMessage.replaceAll("\\p{P}", "");
			}

			if (donation.collectionTypes.equalsIgnoreCase("COMMUNITY_COLLECTION")) {
				donation.donorName = "Community Collection";
			}*/
			System.out.println("dobnor name :: "+donation.donorName);
			if(StringUtils.isEmpty(donation.donorName)){
				donation.donorName = "Anonymous";
			}
			/*if (StringUtils.isEmpty(donation.donorName) && StringUtils.isNotEmpty(donation.donorMessage)) {
				donation.donorName = "Anonymous";
			}*/

			donation.donationType = DonationType.GENERAL;
			donation.paymentType = PaymentType.CASH;
			System.out.println("elseee in donation type");
			donation.status = PaymentStatus.CLEARED;
			donation.datePaid = new Date();


			//==============new add==========start================14.10.2015========================================//

			donation.email = " ";
			donation.phone = " ";
			donation.phPart1 = " ";
			donation.phPart2 = " ";
			donation.phPart3 = " ";

			//==============new add===========end=================14.10.2015========================================//


			//Event eventForPfp = Event.findById(event.id);
			donationList.add(donation);

			//===========new add=================start===================08.10.2015==========================//
			donation.save();

			Donation updatedDonation = Donation.findById(donation.id);
			System.out.println("after save :: " + donation.status);
			System.out.println("after save the donation Id is :: "+donation.id);
			final Pfp pfp = Pfp.findById(donationForm.get().pfp.id);
			ReceiptMgmt.sendSponsoredMsg(updatedDonation);

			ReceiptMgmt.sendCashDonationMsgToEventManager(updatedDonation);
			//System.out.println("event :: " + event);
			//System.out.println("updatedDonation.event :: " + updatedDonation.event);
			//===========new add==================end====================08.10.2015==========================//

			//final Form<Donation> donationForm1 = form(Donation.class);

			//return ok(ProfileCashDonations.render(localUser, events, eventForPfp, pfps, donationForm1, donations, donationList));
			flash(ControllerUtil.FLASH_SUCCESS_KEY, "Bulk Cash donations has been saved and added successfully. ");
		}
		//donationList = Donation.findAllCashDonationsByEventIdAndCleared(eventForPfp.id);
		return ok(ProfileCashDonations.render(localUser, events, eventForPfp, pfps, donationForm1, donations, donationList));
	}

	//=====================validate donation by cash===================end==============02.10.2015=======================//







	@Transactional
	public static Result save(Event event) {
		String	url="";
		PAYMENT_LOGGER.warn("within save");
		System.out.println("within save....");
		if (event.isIdOnly()) {
			event = Event.findByIdWithMinAndGeneralFund(event.id);
		}
		final Form<Donation> donationForm = form(Donation.class).bindFromRequest(); //bind request
		if (donationForm.hasErrors()) {
			Logger.debug("Has errors {}", donationForm.errorsAsJson());
			if(StringUtils.isEmpty(donationForm.data().get("pfp.id"))) {
				return badRequest(createForm.render(event, event.generalFund, donationForm));
			}else if(donationForm.data().get("amount") == null || StringUtils.isEmpty(donationForm.data().get("amount"))){
				donationForm.reject("amount", "Please provide the donation amount.The minimum donation is $5.00, please make the correction to proceed.");

				return badRequest(createForm.render(event, event.generalFund, donationForm));
			}
			else if(Integer.parseInt(donationForm.data().get("amount"))<5) {
				donationForm.reject("amount", "The minimum donation is $5.00, please make the correction to proceed.");
				return badRequest(createForm.render(event, event.generalFund, donationForm));
			}else
			{
				//return badRequest(createForm.render(event, donationForm.get().pfp, donationForm)); //30.07.2015
				return badRequest(createForm.render(event, event.generalFund, donationForm));
			}
		}
		if(StringUtils.isEmpty(donationForm.data().get("pfp.id"))) {
			donationForm.reject("pfp.id", "Please select the Participant that you would like to donate to.  The General Fund donations are made to the Event only and not an individual Participant.");
			if (donationForm.hasErrors()) {
				Logger.debug("Has errors {}", donationForm.errorsAsJson());
				return badRequest(createForm.render(event, event.generalFund, donationForm));
			}
		}
		 if(donationForm.data().get("amount") == null || StringUtils.isEmpty(donationForm.data().get("amount"))){
			donationForm.reject("amount", "Please provide the donation amount.The minimum donation is $5.00, please make the correction to proceed.");

			return badRequest(createForm.render(event, event.generalFund, donationForm));
		}
		if(Integer.parseInt(donationForm.data().get("amount"))<5) {
			donationForm.reject("amount", "The minimum donation is $5.00, please make the correction to proceed.");
			return badRequest(createForm.render(event, event.generalFund, donationForm));
		}




		Donation donation = donationForm.get(); // donation form in donation variable fdatsuv
		donation.dateCreated = new Date();
		if(event.id != Pfp.findEventIdByPfpId(donation.pfp.id)) {
			PAYMENT_LOGGER.warn("The donation [{}] being made for pfp [{}] does not match the event [{}].", donation.id, donation.pfp.id, donation.event.id);
			donation.event = donation.pfp.event;
		} else {
			donation.event = event;
		}





		/****************25.01.2016*************************start**************************************************************/
		/*donation.invoiceNumber = donation.event.id + "_" + donation.dateCreated.getTime();*/
		Random randomGenerator1 = new Random();
		int randomSequence1 = randomGenerator1.nextInt(100000);

		Date date = new Date();
		String modifiedDate= new SimpleDateFormat("yyyy-MM-dd").format(date);
		donation.invoiceNumber = modifiedDate+String.valueOf(randomSequence1);
		System.out.println("invoiceNumber :: "+donation.invoiceNumber);
		/****************25.01.2016**************************end***************************************************************/
		donation.phone = ControllerUtil.stripPhone(donation.phone);
		if(StringUtils.isNotEmpty(donation.donorMessage)) {
			donation.donorMessage = donation.donorMessage.replaceAll("\\p{P}", "");
		}
		if (StringUtils.isEmpty(donation.donorName) && StringUtils.isNotEmpty(donation.donorMessage)) {
			donation.donorName = "Anonymous";
		}
		if (donation.pfp.pfpType == PfpType.GENERAL) {
			donation.donationType = DonationType.GENERAL;
		} else if (donation.pfp.pfpType == PfpType.SPONSOR) {
			donation.donationType = DonationType.SPONSOR;
		} else {
			donation.donationType = DonationType.PFP;
		}
		if (donation.paymentType == null) {
			if (StringUtils.isNotEmpty(donation.ccNum)) {
				donation.paymentType = PaymentType.CREDIT;
			} else if (StringUtils.isNotEmpty(donation.ccCvvCode)) {
				donation.paymentType = PaymentType.CREDIT;
			} else if (StringUtils.isNotEmpty(donation.checkNum)) {
				System.out.println("PaymentType.CHECK : "+PaymentType.CHECK);
				donation.paymentType = PaymentType.CHECK;
			} else {
				donation.paymentType = PaymentType.CASH;
			}
		}


		if (donation.paymentType == PaymentType.CREDIT) {
			PAYMENT_LOGGER.warn("payment type is credit");
			System.out.println("payment type is credit");
			PAYMENT_LOGGER.info("donation.paymentType is credit " + donation.paymentType);
			if (event.isIdOnly()) {
				event = Event.findByIdWithMinAndGeneralFund(event.id);
			}
			System.out.println("within save......donation.pfp.id " + donation.pfp.id);
			System.out.println("before calling findDuplicateDonationToSameDonor.. ");
			/*boolean isDuplicationOfDonation = donation.findDuplicateDonationToSameDonor( donation.pfp.id, donation.donorName, event.id);
			System.out.println("isDuplicationOfDonation :: "+isDuplicationOfDonation);
			if(isDuplicationOfDonation == true){
				System.out.println("isDuplicationOfDonation == true");
				donationForm.reject("Please Donate to Different Participant");
				return badRequest(createForm.render(event, donationForm.get().pfp, donationForm));
			}
			else{*/
			/*donation.status = PaymentStatus.APPROVED;*/




			/*******rimi**********25.02.2016****************************start************************************/

				if (StringUtils.isEmpty(donationForm.data().get("ccNum"))) {
					System.out.println("*************the ccnum is blank**************");
					donationForm.reject("Please click on  'Pay By Credit Card' button to proceed.");
					donationForm.reject("ccNum", "Please provide the credit card number,which is only digits, please make" +
							" the correction to proceed.");

					return badRequest(createForm.render(event, event.generalFund, donationForm));
				}
			/*if(StringUtils.isEmpty(donationForm.data().get("expDate"))){
				System.out.println("*************the expDate is blank11**************");
				donationForm.reject("expDate", "Please provide the Expiration Date(Format is MMYY), please click on  'Pay " +
						"By Credit Card' button to proceed.");
				return badRequest(createForm.render(event, event.generalFund, donationForm));
			}*/


				if (StringUtils.isEmpty(donationForm.data().get("month")) || donationForm.data().get
						("month").equals("-Month-")) {
					System.out.println("*************month**************" + StringUtils.isEmpty(donationForm.data().get
							("month")));
					donationForm.reject("Please click on  'Pay By Credit Card' button to proceed.");
					donationForm.reject("month", "Please select Month, please make the correction to proceed. ");

					return badRequest(createForm.render(event, event.generalFund, donationForm));
				}
				if (StringUtils.isEmpty(donationForm.data().get("year")) || donationForm.data().get
						("year").equals("-Year-")) {
					System.out.println("*************year**************" + StringUtils.isEmpty(donationForm.data().get
							("year")));
					donationForm.reject("Please click on  'Pay By Credit Card' button to proceed.");
					donationForm.reject("month", "Please select Year,please make the correction to proceed. ");

					return badRequest(createForm.render(event, event.generalFund, donationForm));
				}

				if (StringUtils.isEmpty(donationForm.data().get("ccCvvCode"))) {
					System.out.println("*************the ccCvvCode is blank11**************");
					donationForm.reject("Please click on  'Pay By Credit Card' button to proceed.");
					donationForm.reject("ccCvvCode", "Please provide the cvv code,which is only digits, Please make the " +
							"correction to proceed.");

					return badRequest(createForm.render(event, event.generalFund, donationForm));
				}

			/*******rimi**********25.02.2016*****************************end*************************************/


			donation.status = PaymentStatus.INITIATED;
			donation.transactionNumber = UUID.randomUUID().toString();
			donation.ccDigits = null;
			/*********************start*********************comment out******************************25.01.2016***********************************************/
			/*Random randomGenerator1 = new Random();
			int randomSequence1 = randomGenerator1.nextInt(1000);
			donation.transactionNumber = String.valueOf(randomSequence1);*/
			/**********************end**********************comment out******************************25.01.2016***********************************************/
			PAYMENT_LOGGER.info("Successfully submitted transaction to Virtual Merchant for CCNum [{}] and Transaction ID [{}] in the amount of [{}]",
					donation.ccDigits, donation.transactionNumber, donation.amount);
			try {


				donation.save();  // main donation table saved

				Donation donation1 = Donation.findByTransactionNumber(donation.transactionNumber);
				System.out.println("***********************donation id=======>" + donation1.id);
				System.out.println("*************Status=========>" + donation.status);

				PAYMENT_LOGGER.warn("payment type is credit11");
				System.out.println("payment type is credit11");
			} catch (Exception e) {
				//PAYMENT_LOGGER.error("An error occurred saving the Donation so it will be refunded " + ToStringBuilder.reflectionToString(donation));
				PAYMENT_LOGGER.error("The error that caused the db failure [{}]", e.getMessage());
				//Map<String, String> response = new HashMap<String, String>();
//				try {
//					//response = PaymentUtils.sendCCRefund(donation);
//				} catch (Exception r) {
//					PAYMENT_LOGGER.error("Failed to refund donation  to Virtual Merchant for CCNum [{}] and Transaction ID [{}] in the amount of [{}]",
//							donation.ccDigits, donation.transactionNumber, donation.amount);
//					PAYMENT_LOGGER.error("The error that caused the refund failure [{}]", r.getMessage());
//				}
				//Map<String, String> errors = PaymentUtils.validateCreditPayment(response);

				//donationForm.reject("An error has occurred in our submission of your donation, please try your donation again.");
				if (donationForm.hasErrors()) {
					Logger.debug("Has errors {}", donationForm.errorsAsJson());
					PAYMENT_LOGGER.warn("payment type is credit22");
					System.out.println("payment type is credit22");

					/*******rimi**********25.02.2016****************************start************************************/
					if (StringUtils.isEmpty(donationForm.data().get("ccNum"))) {
						System.out.println("*************the ccnum is blank11**************");
						donationForm.reject("Please click on  'Pay By Credit Card' button to proceed.");

						donationForm.reject("ccNum", "Please provide the credit card number,which is only digits, " +
								"please make the correction and click on 'Pay' button to proceed.");
						return badRequest(createForm.render(event, event.generalFund, donationForm));
					}
					/*else if(StringUtils.isEmpty(donationForm.data().get("expDate"))){
						System.out.println("*************the expDate is blank11**************");
						donationForm.reject("expDate", "Please provide the Expiration Date(Format is MMYY), please click on  " +
								"'Pay " +
								"By Credit Card' button to proceed.");
						return badRequest(createForm.render(event, event.generalFund, donationForm));
					}*/
					else if (StringUtils.isEmpty(donationForm.data().get("month")) || donationForm.data().get
							("month").equals("-Month-")) {
						donationForm.reject("Please click on  'Pay By Credit Card' button to proceed.");
						donationForm.reject("month", "Please select the Month.please make the correction and click on 'Pay' button to proceed.");
						if (donationForm.hasErrors()) {
							Logger.debug("Has errors {}", donationForm.errorsAsJson());
							return badRequest(createForm.render(event, event.generalFund, donationForm));
						}
					} else if (StringUtils.isEmpty(donationForm.data().get("year")) || donationForm.data().get
							("year").equals("-Year-")) {
						donationForm.reject("Please click on  'Pay By Credit Card' button to proceed.");
						donationForm.reject("month", "Please select the Year.please make the correction and click on 'Pay' button to proceed.");
						if (donationForm.hasErrors()) {
							Logger.debug("Has errors {}", donationForm.errorsAsJson());
							return badRequest(createForm.render(event, event.generalFund, donationForm));
						}
					} else if (StringUtils.isEmpty(donationForm.data().get("ccCvvCode"))) {
						System.out.println("*************the ccCvvCode is blank11**************");
						donationForm.reject("Please click on  'Pay By Credit Card' button to proceed.");
						donationForm.reject("ccCvvCode", "Please provide the cvv code,which is only digits, please " +
								"make the correction to proceed. ");

						return badRequest(createForm.render(event, event.generalFund, donationForm));
					} else {
						/*******rimi**********25.02.2016*****************************end*************************************/
						return badRequest(createForm.render(event, donationForm.get().pfp, donationForm));
					}

				}
				return badRequest(createForm.render(event, donationForm.get().pfp, donationForm));
			}
			PAYMENT_LOGGER.warn("payment type is credit33");
			System.out.println("payment type is credit33");


			/*******rimi**********25.02.2016****************************start************************************/
		/*if(donation.paymentType == PaymentType.CREDIT)	{
			if (StringUtils.isEmpty(donationForm.data().get("ccNum"))) {
				System.out.println("*************the ccnum is blank**************");
				donationForm.reject("Please click on  'Pay By Credit Card' button to proceed.");
				donationForm.reject("ccNum", "Please provide the credit card number,which is only digits, please make" +
						" the correction to proceed.");

				return badRequest(createForm.render(event, event.generalFund, donationForm));
			}
			*//*if(StringUtils.isEmpty(donationForm.data().get("expDate"))){
				System.out.println("*************the expDate is blank11**************");
				donationForm.reject("expDate", "Please provide the Expiration Date(Format is MMYY), please click on  'Pay " +
						"By Credit Card' button to proceed.");
				return badRequest(createForm.render(event, event.generalFund, donationForm));
			}*//*


			if (StringUtils.isEmpty(donationForm.data().get("month")) || donationForm.data().get
					("month").equals("-Month-")) {
				System.out.println("*************month**************" + StringUtils.isEmpty(donationForm.data().get
						("month")));
				donationForm.reject("Please click on  'Pay By Credit Card' button to proceed.");
				donationForm.reject("year", "Please select Month, please make the correction to proceed. ");

				return badRequest(createForm.render(event, event.generalFund, donationForm));
			}
			if (StringUtils.isEmpty(donationForm.data().get("year")) || donationForm.data().get
					("year").equals("-Year-")) {
				System.out.println("*************year**************" + StringUtils.isEmpty(donationForm.data().get
						("year")));
				donationForm.reject("Please click on  'Pay By Credit Card' button to proceed.");
				donationForm.reject("year", "Please select Year,please make the correction to proceed. ");

				return badRequest(createForm.render(event, event.generalFund, donationForm));
			}

			if (StringUtils.isEmpty(donationForm.data().get("ccCvvCode"))) {
				System.out.println("*************the ccCvvCode is blank11**************");
				donationForm.reject("Please click on  'Pay By Credit Card' button to proceed.");
				donationForm.reject("ccCvvCode", "Please provide the cvv code,which is only digits, Please make the " +
						"correction to proceed.");

				return badRequest(createForm.render(event, event.generalFund, donationForm));
			}
		}*/
			/*******rimi**********25.02.2016*****************************end*************************************/


			//return ok(creditForm.render(event, donationForm.get().pfp, donationForm));
	   /* String WorldPlayUrl=null;
		try {
			WorldPlayUrl=WorldPayUtils.checkout(String.valueOf(donation.amount+".00"),donation.transactionNumber,donation.email,"events/"+event.slug);
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		System.out.println("within validateAndSendCreditInfo now URL sdfdsfsfsfsdfsdfsdfsdfdsfdsfsdfsdfdsf"+WorldPlayUrl);

		return redirect(WorldPlayUrl);*/
			/**********start********authorize.net**********************************************08.01.2016******************/
			try {
				//	WorldPlayUrl=WorldPayUtils.checkout(String.valueOf(donation.amount+".00"),donation.transactionNumber,donation.email,"events/"+event.slug);
				/*Random randomGenerator1 = new Random();
				int randomSequence1 = randomGenerator1.nextInt(1000);*/
				final User user = ControllerUtil.getLocalUser(session());


				/********* 23.02.2016 start **********/


				/*Donation donationDetails = donationForm.get();*/
				String userId="none";
				if(user != null){
					userId=user.id+"";
				}
				/********* 23.02.2016 end **********/

				String msg=AuthorisedNetPaymentUtil.makeCreditCardPayment(donation,userId);


                System.out.println("Response From Authorised net gateway =====>"+msg);
             /*******rimi***********start***********25.02.2016******************************start*********/
               if(msg.equals("The credit card number is invalid."))
			   {
				   donationForm.reject("Please click on  'Pay By Credit Card' button to proceed.");
				   donationForm.reject("ccNum", "Credit card number is Invalid, please make the correction to proceed" +
						   ". ");

				   return badRequest(createForm.render(event, event.generalFund, donationForm));
			   }
				if(msg.equals("The credit card has expired.")){
					donationForm.reject("Please click on  'Pay By Credit Card' button to proceed.");
						System.out.println("*************the expDate is blank11222**************");
						donationForm.reject("month", "Credit card already expired, please make the correction to proceed. " );
						return badRequest(createForm.render(event, event.generalFund, donationForm));
					}
				if(msg.equals("cvv missmatches.") ){
					donationForm.reject("Please click on  'Pay By Credit Card' button to proceed.");
					System.out.println("*************the expDate is blank11222**************");
					donationForm.reject("ccCvvCode", "cvv code not matches, please make the correction to proceed." );
					return badRequest(createForm.render(event, event.generalFund, donationForm));
				}
				if(msg.endsWith(" and cvv not matches.")){
					donationForm.reject("Please click on  'Pay By Credit Card' button to proceed.");
					System.out.println("*************the expDate is blank11222**************");
					donationForm.reject("ccCvvCode", "cvv code not matches, please make the correction to proceed.");
					return badRequest(createForm.render(event, event.generalFund, donationForm));
				}
				if(msg.equals("Internal Server Error.") ){
					//donationForm.reject("Please click on  'Pay By Credit Card' button to proceed.");
					System.out.println("*************the expDate is blank11222**************");
					donationForm.reject("payByCreditCard", "Internal server error please make another payment");
					return badRequest(createForm.render(event, event.generalFund, donationForm));

				}


           /*******rimi***********start***********25.02.2016******************************end*********/



				/*if(user == null){
					System.out.println("*********************event id tooo  11*********************" + event.id);
					//Long eventId = Long.parseLong(result.getResponseMap().get( "&x_event_id"));
					//Long userId = Long.parseLong(result.getResponseMap().get( "&x_user_id"));
					//System.out.println("*********************user id tooo  11*********************" + user.id);
					Random randomGenerator = new Random();
					int randomSequence = randomGenerator.nextInt(1000);
					Fingerprint fingerprint = Fingerprint.createFingerprint(API_LOGIN_ID, TRANSACTION_KEY, randomSequence, String.valueOf(donation.amount));
					//url="https://test.authorize.net/gateway/transact.dll?"+
					url = "https://secure.authorize.net/gateway/transact.dll?" +
							"x_fp_sequence=" + fingerprint.getSequence() +
							"&x_fp_timestamp=" + fingerprint.getTimeStamp() +
							"&x_fp_hash=" + fingerprint.getFingerprintHash() +
							"&x_version=3.1" +
							"&x_method=CC" +
							"&x_type=AUTH_CAPTURE" +
							"&x_login=" + API_LOGIN_ID +
							"&x_description=Scholastic" +
							"&x_show_form=PAYMENT_FORM" +
							"&x_relay_response=true" +
							"&x_logo_url=http://www.scholasticchallenge.com/assets/images/LogoSmaller.png" +

							"&x_donation_transaction_number=" + donation.transactionNumber +
							"&x_amount=" + String.valueOf(donation.amount) +
							"&x_event_id=" + event.id +
							"&x_pfp_id=" + donation.pfp.id +
							"&x_donation_payment_status=" + donation.status +
							"&x_email_id=" + donation.email +
							"&x_invoice_num=" + donation.invoiceNumber +
							"&x_donation_type=" + donation.donationType +
							"&x_user_id=" +"none"+
							"&x_event_id=" + event.id;
				}else {
					System.out.println("*********************event id tooo*********************" + event.id);
					//Long eventId = Long.parseLong(result.getResponseMap().get( "&x_event_id"));
					//Long userId = Long.parseLong(result.getResponseMap().get( "&x_user_id"));
					System.out.println("*********************user id tooo*********************" + user.id);
					Random randomGenerator = new Random();
					int randomSequence = randomGenerator.nextInt(1000);
					Fingerprint fingerprint = Fingerprint.createFingerprint(API_LOGIN_ID, TRANSACTION_KEY, randomSequence, String.valueOf(donation.amount));
					//url="https://test.authorize.net/gateway/transact.dll?"+
					url = "https://secure.authorize.net/gateway/transact.dll?" +
							"x_fp_sequence=" + fingerprint.getSequence() +
							"&x_fp_timestamp=" + fingerprint.getTimeStamp() +
							"&x_fp_hash=" + fingerprint.getFingerprintHash() +
							"&x_version=3.1" +
							"&x_method=CC" +
							"&x_type=AUTH_CAPTURE" +
							"&x_login=" + API_LOGIN_ID +
							"&x_description=Scholastic" +
							"&x_show_form=PAYMENT_FORM" +
							"&x_relay_response=true" +
							"&x_logo_url=http://www.scholasticchallenge.com/assets/images/LogoSmaller.png" +

							"&x_donation_transaction_number=" + donation.transactionNumber +
							"&x_amount=" + String.valueOf(donation.amount) +
							"&x_event_id=" + event.id +
							"&x_pfp_id=" + donation.pfp.id +
							"&x_donation_payment_status=" + donation.status +
							"&x_email_id=" + donation.email +
							"&x_invoice_num=" + donation.invoiceNumber +
							"&x_donation_type=" + donation.donationType +
							"&x_user_id=" + user.id +
							"&x_event_id=" + event.id;
				}
				System.out.println("url"+url);*/
				//return redirect(url);
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("within validateAndSendCreditInfo now Authorize URLEEEEEEEEEEEEEEEEEEEEEEEEEE =>>>>>>>>>>>>>>>"+url);


			//return redirect(url);
			/**********end********authorize.net**********************************************08.01.2016******************/
			//return redirect("https://trans.worldpay.us/cgi-bin/WebPay.cgi?formid=574301A941C9A095E474EF84D558739DC1AD0EE09278E5E321CB1E4970121245&sessionid=62A6DC8C9A988EA9");
		//}

		} else if (donation.paymentType == PaymentType.CHECK) {
			System.out.println("PaymentType.CHECK 111:"+donation.paymentType);
            if(StringUtils.isEmpty(donationForm.data().get("checkNum"))){
                System.out.println("*************the ccnum is blank**************");
                donationForm.reject("checkNum", "Please provide the check number,click on  'Pay By Check' button to proceed.");
                return badRequest(createForm.render(event, event.generalFund, donationForm));
            }
			donation.transactionNumber = UUID.randomUUID().toString();
			donation.status = PaymentStatus.PENDING;
			donation.save();
			System.out.println("after save PaymentType.CHECK 111 : "+donation.paymentType);

			PAYMENT_LOGGER.info("A Check Donation [{}] was made for PFP ID [{}] in the amount of [{}] by [{} {}]", donation.id, donation.pfp.id, donation.amount, donation.firstName, donation.lastName);
		} else {
			System.out.println("elseee in donation type");
			donation.status = PaymentStatus.CLEARED;
			donation.datePaid = new Date();
			donation.save();
			PAYMENT_LOGGER.info("A Cash Donation [{}] was made for PFP ID [{}] in the amount of [{}] by [{} {}]", donation.id, donation.pfp.id, donation.amount, donation.firstName, donation.lastName);
		}
		System.out.println("========donation.id==="+donation.id);
		Donation updatedDonation = Donation.findById(donation.id);
		System.out.println("after save :: "+updatedDonation.status);
		final Pfp pfp = Pfp.findById(donationForm.get().pfp.id);
		if (pfp.pfpType == PfpType.PFP && updatedDonation.paymentType != PaymentType.CHECK) {
			ReceiptMgmt.sendSponsoredMsg(updatedDonation);
		}
		flash(ControllerUtil.FLASH_SUCCESS_KEY, "Donation has been submitted");
		if (updatedDonation.paymentType == PaymentType.CREDIT) {
			return redirect(routes.ReceiptMgmt.getAndSendCCReceipt(event, updatedDonation));
		} else if (updatedDonation.paymentType == PaymentType.CHECK) {
			return redirect(routes.ReceiptMgmt.getAndSendCheckReceipt(event, updatedDonation));
		} else {
			return redirect(routes.ReceiptMgmt.getAndSendCashReceipt(event, updatedDonation));
		}
	}
	
	/**
	 * Handle the 'new donation form' submission.
	 * 
	 * @param id
	 *            the id
	 * @param pfpId
	 *            the pfp id
	 * @return the result
	 */
	@Transactional
	public static Result saveModalWithSponsor(Event event, Pfp pfp, Long sponsorItemId) {
		String	url="";
		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		if (pfp.isIdOnly()) {
			pfp = Pfp.findByIdWithMin(pfp.id);
		}
		SponsorItem sponsorItem = SponsorItem.findById(sponsorItemId);
		final Form<Donation> donationForm = form(Donation.class).bindFromRequest();
		System.out.println(("********donationForm.hasErrors()*****************"+donationForm.hasErrors()));
		if (donationForm.hasErrors()) {
			Logger.debug("Has errors {}", donationForm.errorsAsJson());
			return badRequest(views.html.sponsors.createDonationForm.render(event, pfp, sponsorItem,
							donationForm));
		}


		Donation donation = donationForm.get();
		//============new add for checking web url and img url======================07.09.2015======================start==================//

        if(donation.webUrl!=null){
			System.out.println("donation.imgUrl :: "+donation.imgUrl);
			final Http.MultipartFormData body1 = request().body()
					.asMultipartFormData();
			System.out.println("body1 :: "+body1);
			final Http.MultipartFormData.FilePart imgUrlFilePart1 = body1
					.getFile("imgUrl");
			System.out.println("imgUrlFilePart1 :: "+imgUrlFilePart1);
			if(imgUrlFilePart1 == null){
				donationForm.reject("Please Upload Logo Image");
				//return badRequest(createForm.render(event, donationForm.get().pfp, donationForm));
				return badRequest(views.html.sponsors.createDonationForm.render(event, pfp, sponsorItem,
						donationForm));
			}
		}

		//============new add for checking web url and img url======================07.09.2015=======================end===================//
		donation.dateCreated = new Date();
		donation.event = event;
		/*donation.invoiceNumber = donation.event.id + "_" + donation.dateCreated.getTime();*/
		/****************25.01.2016*************************start**************************************************************/
		/*donation.invoiceNumber = donation.event.id + "_" + donation.dateCreated.getTime();*/
		Random randomGenerator1 = new Random();
		int randomSequence1 = randomGenerator1.nextInt(100000);

		Date date = new Date();
		String modifiedDate= new SimpleDateFormat("yyyy-MM-dd").format(date);
		donation.invoiceNumber = modifiedDate+String.valueOf(randomSequence1);
		System.out.println("invoiceNumber111 :: "+donation.invoiceNumber);
		/****************25.01.2016**************************end***************************************************************/
		donation.donationType = DonationType.SPONSOR;
		if (donation.paymentType == null) {
//			if (StringUtils.isNotEmpty(donation.ccNum)) {
//				donation.paymentType = PaymentType.CREDIT;
//			} else if (StringUtils.isNotEmpty(donation.ccCvvCode)) {
//				donation.paymentType = PaymentType.CREDIT;
//			} else {
//				donation.paymentType = PaymentType.CHECK;
//			}
			if (StringUtils.isNotEmpty(donation.checkNum))
			{
				donation.paymentType = PaymentType.CHECK;
			}
			else if (StringUtils.isNotEmpty(donation.checkNum)&&StringUtils.isNotEmpty(donation.ccNum)){
				return badRequest(views.html.sponsors.createDonationForm.render(event, pfp, sponsorItem,
						donationForm));
			}
			else{
				donation.paymentType = PaymentType.CREDIT;
			}
		}

		System.out.println("Now the status of payment type is"+donation.paymentType);

		if (donation.paymentType == PaymentType.CREDIT) {
			Map<String, String> ccFormErrors = PaymentUtils.validateCreditForm(donationForm.data());
//			if(MapUtils.isNotEmpty(ccFormErrors)) {
//				for(String key: ccFormErrors.keySet()) {
//					if(StringUtils.isEmpty(key)) {
//						donationForm.reject(ccFormErrors.get(key));
//					} else {
//						donationForm.reject(key, ccFormErrors.get(key));
//					}
//				}
//				if (donationForm.hasErrors()) {
//					Logger.debug("Has errors {}", donationForm.errorsAsJson());
//					return badRequest(views.html.sponsors.createDonationForm.render(event, pfp, sponsorItem,
//									donationForm));
//				}
//			}
//			donation.ccNum = donation.ccNum.replaceAll("\\s","");
//			Map<String, String> ccProps = PaymentUtils.sendCCPayment(donation);
//			Map<String, String> ccErrors = PaymentUtils.validateCreditPayment(ccProps);
//			if(MapUtils.isNotEmpty(ccErrors)) {
//				for(String key: ccErrors.keySet()) {
//					if(StringUtils.isEmpty(key)) {
//						donationForm.reject(ccErrors.get(key));
//					} else if(StringUtils.startsWith(key, "_")) {
//						donationForm.reject(ccErrors.get(key));
//					} else {
//						donationForm.reject(key, ccErrors.get(key));
//					}
//				}
//				if (donationForm.hasErrors()) {
//					Logger.debug("Has errors {}", donationForm.errorsAsJson());
//					return badRequest(views.html.sponsors.createDonationForm.render(event, pfp, sponsorItem,
//									donationForm));
//				}
//			}
			/*donation.status = PaymentStatus.APPROVED;*/
			System.out.println("********************donation amount for sponsor credit*******************"+donation
					.amount);
			donation.status = PaymentStatus.INITIATED;
			donation.paymentType = PaymentType.CREDIT;
			System.out.println("************before transactionNumber****************");
			donation.transactionNumber =UUID.randomUUID().toString();
			//ccProps.get("ssl_txn_id");

			/*if (StringUtils.isEmpty(donation.transactionNumber)) {
//				Logger.error("There is no transaction number for the ccNum {} and props {}.", donationForm.get().ccNum,
//								ToStringBuilder.reflectionToString(ccProps));
			}*/
			System.out.println("************after transactionNumber****************");
			System.out.println(("*************donation.paymentType*************"+donation.paymentType));
			System.out.println(("********donationForm.hasErrors()1*****************"+donationForm.hasErrors()));
			/*if (donationForm.hasErrors()) {*/
				System.out.println("************donationForm.hasErrors()****************");
				Logger.debug("Has errors {}", donationForm.errorsAsJson());
				PAYMENT_LOGGER.warn("payment type is credit22");
				System.out.println("payment type is credit22");

				/*******rimi**********25.02.2016****************************start************************************/
				if(StringUtils.isEmpty(donationForm.data().get("ccNum"))){
					System.out.println("*************the ccnum is blank11**************");
					donationForm.reject("Please click on  'Pay By Credit Card' button to proceed.");

					donationForm.reject("ccNum", "Please provide the credit card number,which is only digits, please make the correction to proceed.");
					return badRequest(views.html.sponsors.createDonationForm.render(event, pfp, sponsorItem,
							donationForm));
				}
					/*else if(StringUtils.isEmpty(donationForm.data().get("expDate"))){
						System.out.println("*************the expDate is blank11**************");
						donationForm.reject("expDate", "Please provide the Expiration Date(Format is MMYY), please click on  " +
								"'Pay " +
								"By Credit Card' button to proceed.");
						return badRequest(createForm.render(event, event.generalFund, donationForm));
					}*/
				else if(StringUtils.isEmpty(donationForm.data().get("month")) || donationForm.data().get
						("month").equals("-Month-")) {
					donationForm.reject("Please click on  'Pay By Credit Card' button to proceed.");
					donationForm.reject("month", "Please select the Month.please make the correction to proceed.");
					/*if (donationForm.hasErrors()) {*/
						Logger.debug("Has errors {}", donationForm.errorsAsJson());
						return badRequest(views.html.sponsors.createDonationForm.render(event, pfp, sponsorItem,
								donationForm));
					/*}*/
				}else if(StringUtils.isEmpty(donationForm.data().get("year"))  || donationForm.data().get
						("year").equals("-Year-")) {
					donationForm.reject("Please click on  'Pay By Credit Card' button to proceed.");
					donationForm.reject("month", "Please select the Year.please make the correction to proceed.");
					/*if (donationForm.hasErrors()) {*/
						Logger.debug("Has errors {}", donationForm.errorsAsJson());
						return badRequest(views.html.sponsors.createDonationForm.render(event, pfp, sponsorItem,
								donationForm));
					/*}*/
				}else if(StringUtils.isEmpty(donationForm.data().get("ccCvvCode"))){
					System.out.println("*************the ccCvvCode is blank11**************");
					donationForm.reject("Please click on  'Pay By Credit Card' button to proceed.");
					donationForm.reject("ccCvvCode", "Please provide the cvv code,which is only digits, please " +
							"make the correction to proceed. ");

					return badRequest(views.html.sponsors.createDonationForm.render(event, pfp, sponsorItem,
							donationForm));
				}
					/*******rimi**********25.02.2016*****************************end*************************************/



			/*}*/

			//donation.ccDigits = donation.ccNum.substring(Math.max(0, donation.ccNum.length() - 4));
		} else {

			System.out.println("within else***********");
			donation.status = PaymentStatus.PENDING;
			donation.paymentType = PaymentType.CHECK;
			donation.transactionNumber = UUID.randomUUID().toString();
		}
		donation.sponsorItem = sponsorItem;
		donation.phone = ControllerUtil.stripPhone(donation.phone);
		donation.event = event;
		System.out.println("********************donation amount for sponsor*******************"+donation.amount);





		/**************upload image*******************20.08.2015****************start*****************/

		final Http.MultipartFormData body = request().body()
				.asMultipartFormData();
		System.out.println("body :: "+body);
		final Http.MultipartFormData.FilePart imgUrlFilePart = body
				.getFile("imgUrl");
		System.out.println("imgUrlFilePart :: "+imgUrlFilePart);

		System.out.println("in saveModalWithSponsor");


		S3File imgUrlFile = null;
		if (imgUrlFilePart != null) {
			System.out.println("imgUrlFile is not null");
			if (!ControllerUtil.isImage(imgUrlFilePart.getFilename())) {
				donationForm.reject("imgUrl", ControllerUtil.IMAGE_ERROR_MSG);
			} else if (ControllerUtil.isFileTooLarge(imgUrlFilePart.getFile())) {
				donationForm.reject("imgUrl", ControllerUtil.IMAGE_SIZE_ERROR_MSG);
			} else {
				imgUrlFile = new S3File();
				imgUrlFile.name = ControllerUtil.decodeFileName(imgUrlFilePart
						.getFilename());
				imgUrlFile.file = imgUrlFilePart.getFile();
				System.out.println("imgUrlFile.file =>"+imgUrlFile.file );
				donationForm.get().imgUrl = imgUrlFile.getUrl();
				System.out.println("donationForm.get().imgUrl =>"+donationForm.get().imgUrl );
			}
		}
		if (imgUrlFile != null) {
			System.out.println("imgUrlFile save");

   /*if(event.imgUrl != null) {
      S3File.delete(event.imgUrl);
   }*/
			imgUrlFile.save();
			System.out.println("imgUrlFile save end");
		}

/**************upload image*******************20.08.2015****************end****************/

		donation.save();
		/*Logger.info("Successfully submitted transaction to Virtual Merchant for CCNum [{}] and Transaction ID [{}]
		in the amount of [{}]",
						donation.ccDigits, donation.transactionNumber, donation.amount);
		sponsorItem.donation = donation;
		sponsorItem.update();
		Donation updatedDonation = Donation.findById(donationForm.get().id);
		flash(ControllerUtil.FLASH_SUCCESS_KEY, "Sponsorship has been submitted");*/
		/*ReceiptMgmt.sendSponsoredMsg(updatedDonation);*/
		/*if (updatedDonation.paymentType == PaymentType.CREDIT) {
			String WorldPlayUrl=null;
			try {
				WorldPlayUrl=WorldPayUtils.checkout(String.valueOf(donation.amount+".00"),donation.transactionNumber,donation.email,"events/"+event.slug);
			} catch (BadPaddingException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (IllegalBlockSizeException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (NoSuchPaddingException e) {
				e.printStackTrace();
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				e.printStackTrace();
			}
			System.out.println("worldpay Now URL"+WorldPlayUrl);
			//return redirect(routes.ReceiptMgmt.getAndSendCCReceipt(event, updatedDonation));
			return redirect(WorldPlayUrl);
		}*/
		Donation updatedDonation = Donation.findById(donationForm.get().id);
		System.out.println("***********updated donation id************"+updatedDonation.id);
		System.out.println("***********updatedDonation.paymentType************"+updatedDonation.paymentType);
		if (updatedDonation.paymentType == PaymentType.CREDIT) {
			/**********start********authorize.net**********************************************08.01.2016******************/
			System.out.println("********************donation amount for sponsor credit2211*******************"+donation
					.amount);
			try {




				final User user = ControllerUtil.getLocalUser(session());


				/********* 23.02.2016 start **********/


				/*Donation donationDetails = donationForm.get();*/
				String userId="none";
				if(user != null){
					userId=user.id+"";
				}
				/********* 23.02.2016 end **********/

				String msg=AuthorisedNetPaymentUtil.makeCreditCardPayment(donation,userId);


				System.out.println("Response From Authorised net gateway =====>"+msg);
				/*******rimi***********start***********25.02.2016******************************start*********/
				if(msg.equals("The credit card number is invalid."))
				{
					donationForm.reject("Please click on  'Pay By Credit Card' button to proceed.");
					donationForm.reject("ccNum", "Credit card number is Invalid, please make the correction to proceed" +
							". ");

					return badRequest(views.html.sponsors.createDonationForm.render(event, pfp, sponsorItem,
							donationForm));
				}
				else if(msg.equals("The credit card has expired.")){
					donationForm.reject("Please click on  'Pay By Credit Card' button to proceed.");
					System.out.println("*************the expDate is blank11222**************");
					donationForm.reject("month", "Credit card already expired, please make the correction to proceed. " );
					return badRequest(views.html.sponsors.createDonationForm.render(event, pfp, sponsorItem,
							donationForm));
				}
				else if(msg.equals("cvv missmatches.") ){
					donationForm.reject("Please click on  'Pay By Credit Card' button to proceed.");
					System.out.println("*************the expDate is blank11222**************");
					donationForm.reject("ccCvvCode", "cvv code not matches, please make the correction to proceed." );
					return badRequest(views.html.sponsors.createDonationForm.render(event, pfp, sponsorItem,
							donationForm));
				}
				else if(msg.endsWith(" and cvv not matches.")){
					donationForm.reject("Please click on  'Pay By Credit Card' button to proceed.");
					System.out.println("*************the expDate is blank11222**************");
					donationForm.reject("ccCvvCode", "cvv code not matches, please make the correction to proceed.");
					return badRequest(views.html.sponsors.createDonationForm.render(event, pfp, sponsorItem,
							donationForm));
				}
				else if(msg.equals("Internal Server Error.") ){
					//donationForm.reject("Please click on  'Pay By Credit Card' button to proceed.");
					System.out.println("*************the expDate is blank11222**************");
					donationForm.reject("payByCreditCard", "Internal server error please make another payment");
					return badRequest(views.html.sponsors.createDonationForm.render(event, pfp, sponsorItem,
							donationForm));

				}else if(msg.indexOf("Successful Credit Card Transaction your Transaction Id is")>-1){
					System.out.println("**********after donation save donation id**********"+donation.id);
					System.out.println("**********after donation save donation type**********"+donation.paymentType);
					System.out.println("**********after donation save donation status**********"+donation.status);
					System.out.println("**********after donation save donation type**********"+donation.donationType);
					flash(ControllerUtil.FLASH_SUCCESS_KEY, "Sponsorship has been submitted");
				}


				/*******rimi***********start***********25.02.2016******************************end*********/

				//	WorldPlayUrl=WorldPayUtils.checkout(String.valueOf(donation.amount+".00"),donation.transactionNumber,donation.email,"events/"+event.slug);
				/*Random randomGenerator1 = new Random();
				int randomSequence1 = randomGenerator1.nextInt(1000);*/
				/*final User user = ControllerUtil.getLocalUser(session());


				*//********* 23.02.2016 start **********//*


				*//*Donation donationDetails = donationForm.get();*//*
				String userId="none";
				if(user != null){
					userId=user.id+"";
				}
				*//********* 23.02.2016 end **********//*

				String msg=AuthorisedNetPaymentUtil.makeCreditCardPayment(donation,userId);


				System.out.println("Response From Authorised net gateway =====>"+msg);
				*//*******rimi***********start***********25.02.2016******************************start*********//*
				if(msg.equals("The credit card number is invalid."))
				{
					donationForm.reject("Please click on  'Pay By Credit Card' button to proceed.");
					donationForm.reject("ccNum", "Credit card number is Invalid, please make the correction to proceed" +
							". ");

					return badRequest(views.html.sponsors.createDonationForm.render(event, pfp, sponsorItem,
							donationForm));
				}
				if(msg.equals("The credit card has expired.")){
					donationForm.reject("Please click on  'Pay By Credit Card' button to proceed.");
					System.out.println("*************the expDate is blank11222**************");
					donationForm.reject("month", "Credit card already expired, please make the correction to proceed. " );
					return badRequest(views.html.sponsors.createDonationForm.render(event, pfp, sponsorItem,
							donationForm));
				}
				if(msg.equals("cvv missmatches.") ){
					donationForm.reject("Please click on  'Pay By Credit Card' button to proceed.");
					System.out.println("*************the expDate is blank11222**************");
					donationForm.reject("ccCvvCode", "cvv code not matches, please make the correction to proceed." );
					return badRequest(views.html.sponsors.createDonationForm.render(event, pfp, sponsorItem,
							donationForm));
				}
				if(msg.endsWith(" and cvv not matches.")){
					donationForm.reject("Please click on  'Pay By Credit Card' button to proceed.");
					System.out.println("*************the expDate is blank11222**************");
					donationForm.reject("ccCvvCode", "cvv code not matches, please make the correction to proceed.");
					return badRequest(views.html.sponsors.createDonationForm.render(event, pfp, sponsorItem,
							donationForm));
				}
				if(msg.equals("Internal Server Error.") ){
					//donationForm.reject("Please click on  'Pay By Credit Card' button to proceed.");
					System.out.println("*************the expDate is blank11222**************");
					donationForm.reject("payByCreditCard", "Internal server error please make another payment");
					return badRequest(views.html.sponsors.createDonationForm.render(event, pfp, sponsorItem,
							donationForm));

				}


				/*******rimi***********start***********25.02.2016******************************end*********/
				/*if(user == null){
					Random randomGenerator = new Random();
					int randomSequence = randomGenerator.nextInt(1000);
					Fingerprint fingerprint = Fingerprint.createFingerprint(API_LOGIN_ID, TRANSACTION_KEY, randomSequence, String.valueOf(donation.amount));
					//url="https://test.authorize.net/gateway/transact.dll?"+
					url = "https://secure.authorize.net/gateway/transact.dll?" +
							"x_fp_sequence=" + fingerprint.getSequence() +
							"&x_fp_timestamp=" + fingerprint.getTimeStamp() +
							"&x_fp_hash=" + fingerprint.getFingerprintHash() +
							"&x_version=3.1" +
							"&x_method=CC" +
							"&x_type=AUTH_CAPTURE" +
							"&x_login=" + API_LOGIN_ID +
							"&x_description=Scholastic" +
							"&x_show_form=PAYMENT_FORM" +
							"&x_relay_response=true" +
							"&x_logo_url=http://www.scholasticchallenge.com/assets/images/LogoSmaller.png" +

							"&x_donation_transaction_number=" + donation.transactionNumber +
							"&x_amount=" + String.valueOf(donation.amount) +
							"&x_event_id=" + event.id +
							"&x_pfp_id=" + donation.pfp.id +
							"&x_donation_payment_status=" + donation.status +
							"&x_email_id=" + donation.email +
							"&x_invoice_num=" + donation.invoiceNumber +
							"&x_donation_type=" + donation.donationType +
							"&x_sponsorItem_Id=" + sponsorItemId +
							"&x_user_id=" + "none" +
							"&x_event_id=" + event.id;

				}else {
					Random randomGenerator = new Random();
					int randomSequence = randomGenerator.nextInt(1000);
					Fingerprint fingerprint = Fingerprint.createFingerprint(API_LOGIN_ID, TRANSACTION_KEY, randomSequence, String.valueOf(donation.amount));
					//url="https://test.authorize.net/gateway/transact.dll?"+
					url = "https://secure.authorize.net/gateway/transact.dll?" +
							"x_fp_sequence=" + fingerprint.getSequence() +
							"&x_fp_timestamp=" + fingerprint.getTimeStamp() +
							"&x_fp_hash=" + fingerprint.getFingerprintHash() +
							"&x_version=3.1" +
							"&x_method=CC" +
							"&x_type=AUTH_CAPTURE" +
							"&x_login=" + API_LOGIN_ID +
							"&x_description=Scholastic" +
							"&x_show_form=PAYMENT_FORM" +
							"&x_relay_response=true" +
							"&x_logo_url=http://www.scholasticchallenge.com/assets/images/LogoSmaller.png" +

							"&x_donation_transaction_number=" + donation.transactionNumber +
							"&x_amount=" + String.valueOf(donation.amount) +
							"&x_event_id=" + event.id +
							"&x_pfp_id=" + donation.pfp.id +
							"&x_donation_payment_status=" + donation.status +
							"&x_email_id=" + donation.email +
							"&x_invoice_num=" + donation.invoiceNumber +
							"&x_donation_type=" + donation.donationType +
							"&x_sponsorItem_Id=" + sponsorItemId +
							"&x_user_id=" + user.id +
							"&x_event_id=" + event.id;
				}
				System.out.println("url"+url);
				//return redirect(url);*/
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("within validateAndSendCreditInfo now Authorize URLEEEEEEEEEEEEEEEEEEEEEEEEEE1111111111 =>>>>>>>>>>>>>>>"+url);
			//return redirect(url);
			updatedDonation = Donation.findById(donationForm.get().id);
			flash(ControllerUtil.FLASH_SUCCESS_KEY, "Sponsorship has been submitted");
			ReceiptMgmt.sendSponsoredMsg(updatedDonation);
			System.out.println("before calling getAndSendCheckReceipt..");
			return redirect(routes.ReceiptMgmt.getAndSendCCReceipt(event, updatedDonation));
			//return ok(index.render());
			/**********end********authorize.net**********************************************08.01.2016******************/
		}else {

			donation.save();
			Logger.info("Successfully submitted transaction to Virtual Merchant for CCNum [{}] and Transaction ID [{}] in the amount of [{}]",
					donation.ccDigits, donation.transactionNumber, donation.amount);
			sponsorItem.donation = donation;
			sponsorItem.update();
			 updatedDonation = Donation.findById(donationForm.get().id);
			flash(ControllerUtil.FLASH_SUCCESS_KEY, "Sponsorship has been submitted");
			ReceiptMgmt.sendSponsoredMsg(updatedDonation);
			System.out.println("before calling getAndSendCheckReceipt..");
			return redirect(routes.ReceiptMgmt.getAndSendCheckReceipt(event, updatedDonation));
		}
	}



	@Transactional
	public static Result worldPayPostBack(String customdata) {

//		Form<WorldPay> worldPayForm = Form.form(WorldPay.class).bindFromRequest();
//		WorldPay wp=worldPayForm.get();
//		System.out.println("World Pay Credit Card Name"+wp.ccname);
		DynamicForm requestData = Form.form().bindFromRequest();
		System.out.println("Request from worldpay"+customdata);
		System.out.println("Request form worldpay" + requestData.get("customdata"));

		return ok("Hello in the worldPayPostBack" + requestData.get("customdata"));

	}


	
	/**
	 * Handle the 'edit form' submission .
	 * 
	 * @param id
	 *            Id of the donation to edit
	 * @param pfpId
	 *            the pfp id
	 * @param donationId
	 *            the donation id
	 * @return the result
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN), @Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
					@Group(SecurityRole.EVENT_ASSIST) })
	@SubjectPresent
	public static Result update(Event event, Pfp pfp, Long donationId) {
		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		if (pfp.isIdOnly()) {
			pfp = Pfp.findByIdWithMin(pfp.id);
		}
		if (!Event.canManage(ControllerUtil.getLocalUser(session()), event)) {
			flash(ControllerUtil.FLASH_DANGER_KEY,
							"The requested event action cannot be completed by the logged in user.");
			return redirect(routes.Application.index());
		}
		final Form<Donation> donationForm = form(Donation.class).bindFromRequest();
		if (donationForm.hasErrors()) {
			return badRequest(editForm.render(event, pfp, donationId, donationForm));
		}
		final Donation donation = Donation.findById(donationId);
		PaymentStatus originalStatus = donation.status;
		if (PaymentType.CHECK == donationForm.get().paymentType && PaymentStatus.CLEARED == donationForm.get().status) {
			if (PaymentStatus.CLEARED != originalStatus) {
				donationForm.get().datePaid = new Date();
			}
		}
		donationForm.get().phone = ControllerUtil.stripPhone(donationForm.get().phone);
		if(donationForm.get().event == null) {
			donationForm.get().event = event;
		}
		donationForm.get().update(donationId);
		Donation updatedDonation = Donation.findById(donationId);
		if (PaymentType.CHECK == donationForm.get().paymentType && PaymentStatus.CLEARED == donationForm.get().status) {
			if (PaymentStatus.CLEARED != originalStatus) {
				ReceiptMgmt.sendCheckReceivedReceipt(updatedDonation);
				if (pfp.pfpType == PfpType.PFP) {
					ReceiptMgmt.sendSponsoredMsg(updatedDonation);
				}
			}
		}
		flash(ControllerUtil.FLASH_SUCCESS_KEY, "Donation has been updated");
		return redirect(routes.Application.profileSearchDonations(0, "dateCreated",
				"asc", "", "dateCreated"));
	}
	
	/**
	 * Handle the 'edit form' submission .
	 * 
	 * @param id
	 *            Id of the donation to edit
	 * @param pfpId
	 *            the pfp id
	 * @param donationId
	 *            the donation id
	 * @return the result
	 */



	/****************start*******************Bulk Cash Donation*******************24.09.2015**********************************/


	/*public static Result profileCashDonations(){
		final User localUser = ControllerUtil.getLocalUser(session());
		List<Event> events = new ArrayList<Event>();
		if(ControllerUtil.isUserInRole(models.security.SecurityRole.ROOT_ADMIN)){
			events = Event.findAllEvents();
		}else if(ControllerUtil.isUserInRole(SecurityRole.EVENT_ADMIN)){
			events = Event.findAllByUserId(localUser.id);
		}
		final Form<Donation> donationForm = form(Donation.class);
		//if(!Event.isLive(event) && (localUser == null || !ControllerUtil.isEqual(event.userAdmin.id, localUser.id)))

		if(events!= null && events.size()>0){
			return ok(ProfileCashDonations.render(localUser, events, null , null,donationForm));
		}else{
			return ok(ProfileCashDonations.render(localUser, null, null, null, donationForm));
		}

	}*/

	public static Result participantDetailsForParticularEvent(){
		System.out.println("eventId within participantDetailsForParticularEvent :: ");
		final User localUser = ControllerUtil.getLocalUser(session());
		String type = request().getQueryString("type");
		System.out.println("type : "+type);
		String id = request().getQueryString("id");
		System.out.println("eventId within participantDetailsForParticularEvent :: "+id);

		List<Event> events = new ArrayList<Event>();
		if(ControllerUtil.isUserInRole(SecurityRole.ROOT_ADMIN)){
			events = Event.findAllEvents();
		}else if(ControllerUtil.isUserInRole(SecurityRole.EVENT_ADMIN)){
			events = Event.findAllByUserId(localUser.id);
		}
		System.out.println("after events");

		Long eventId = Long.parseLong(id);
		Event event = Event.findById(eventId);
		System.out.println("after event");
		final Form<Donation> donationForm = form(Donation.class);
		List<Donation> donations = Donation.findAllByEventId(eventId);
		List<Pfp> pfps= Pfp.findByEventId(eventId);
		System.out.println("before test");
		//return ok(test.render(donations));
		donations = new ArrayList<Donation>();
		donationList = new ArrayList<Donation>();
		//donationList = Donation.findAllCashDonationsByEventIdAndCleared(eventId);
		return ok(ProfileCashDonations.render(localUser, events, event, pfps, donationForm, donations, donationList));
		//return ok(test.render(localUser,events, event, pfps, donationForm, donations));

	}





	/*****************end********************Bulk Cash Donation*******************24.09.2015**********************************/

	//=====================new update=================22.09.2015=========================start==============================//

	@Restrict({ @Group(SecurityRole.ROOT_ADMIN), @Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
			@Group(SecurityRole.EVENT_ASSIST) })
	@SubjectPresent
	public static Result updateDonations(Event event, Pfp pfp, Long donationId) {
		System.out.println("donationid within updateDonations :: "+donationId);

		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		if (pfp.isIdOnly()) {
			pfp = Pfp.findByIdWithMin(pfp.id);
		}
		final Form<Donation> donationForm = form(Donation.class).bindFromRequest();
		if (!Event.canManage(ControllerUtil.getLocalUser(session()), event)) {
			flash(ControllerUtil.FLASH_DANGER_KEY,
					"The requested event action cannot be completed by the logged in user.");
			return redirect(routes.Application.index());
		}

		//System.out.println("donationForm :: "+donationForm.get());
		if (donationForm.hasErrors()) {
			return badRequest(editForm.render(event, pfp, donationId, donationForm));
		}




		Donation updatedDonation = (Donation)donationForm.get();
		System.out.println("updateddonation :: "+updatedDonation);

		Donation donation = Donation.findById(donationId);

		if(donation.amount != updatedDonation.amount){

			donation.amount = updatedDonation.amount;
		}
		if(!StringUtils.equals(donation.firstName, updatedDonation.firstName)){

			donation.firstName = updatedDonation.firstName;
		}
		if(!StringUtils.equals(donation.lastName, updatedDonation.lastName)) {

			donation.lastName = updatedDonation.lastName;
		}

		if(!StringUtils.equals(donation.zipCode, updatedDonation.zipCode)){
			donation.zipCode = updatedDonation.zipCode;
		}

		if(!StringUtils.equals(donation.email, updatedDonation.email)){
			donation.email = updatedDonation.email;
		}
		updatedDonation.phPart1 = ControllerUtil.stripPhone(donationForm.get().phPart1);
		System.out.println("updatedDonation.phPart1 :: "+updatedDonation.phPart1);
		if(!StringUtils.equals(donation.phPart1, updatedDonation.phPart1)){
			donation.phPart1 = updatedDonation.phPart1;
		}
		updatedDonation.phPart2 = ControllerUtil.stripPhone(donationForm.get().phPart2);
		System.out.println("updatedDonation.phPart2 :: "+updatedDonation.phPart2);
		if(!StringUtils.equals(donation.phPart2, updatedDonation.phPart2)){
			donation.phPart2 = updatedDonation.phPart2;
		}
		updatedDonation.phPart3 = ControllerUtil.stripPhone(donationForm.get().phPart3);
		System.out.println("updatedDonation.phPart3 :: "+updatedDonation.phPart3);
		if(!StringUtils.equals(donation.phPart3, updatedDonation.phPart3)){
			donation.phPart3 = updatedDonation.phPart3;
		}
		updatedDonation.phone = ControllerUtil.stripPhone(donationForm.get().phone);
		if(!StringUtils.equals(donation.phone, updatedDonation.phone)){
			donation.phone = updatedDonation.phone;
		}
		if(!donation.paymentType.equals(updatedDonation.paymentType)){
			donation.paymentType = updatedDonation.paymentType;
		}
		if(!StringUtils.equals(donation.checkNum, updatedDonation.checkNum)){
			donation.checkNum = updatedDonation.checkNum;
		}
		if(!StringUtils.equals(donation.donorName, updatedDonation.donorName)){
			donation.donorName = updatedDonation.donorName;
		}
		if(!StringUtils.equals(donation.donorMessage, updatedDonation.donorMessage)){
			donation.donorMessage = updatedDonation.donorMessage;
		}
		if(!donation.dateCreated.equals( updatedDonation.dateCreated)){
			donation.dateCreated = updatedDonation.dateCreated;
		}
		/*if(!donation.datePaid.equals( updatedDonation.datePaid)){
			donation.datePaid = updatedDonation.datePaid;
		}*/
		if(!StringUtils.equals(donation.transactionNumber, updatedDonation.transactionNumber)){
			donation.transactionNumber = updatedDonation.transactionNumber;
		}



		PaymentStatus originalStatus = donation.status;
		if (PaymentType.CHECK == donationForm.get().paymentType && PaymentStatus.CLEARED == donationForm.get().status) {
			if (PaymentStatus.CLEARED != originalStatus) {
				updatedDonation.datePaid = new Date();
				donation.datePaid = updatedDonation.datePaid;

			}
		}

		if(!donation.status.equals( updatedDonation.status)){


			donation.status = updatedDonation.status;
		}


		if(updatedDonation.event == null) {
			updatedDonation.event = event;
		}

		if(!donation.event.equals(updatedDonation.event)){
			donation.event = updatedDonation.event;
		}


		donation.update();


		System.out.println("donation.phPart1 :: " + donation.phPart1);
		System.out.println("donation.phPart2 :: " + donation.phPart2);
		System.out.println("donation.phPart3 :: " + donation.phPart3);





		//donationForm.get().phone = ControllerUtil.stripPhone(donationForm.get().phone);


		//Donation updatedDonation = Donation.findById(donationId);
		if (PaymentType.CHECK == donation.paymentType && PaymentStatus.CLEARED == donation.status) {
			if (PaymentStatus.CLEARED != originalStatus) {
				ReceiptMgmt.sendCheckReceivedReceipt(donation);
				if (pfp.pfpType == PfpType.PFP) {
					ReceiptMgmt.sendSponsoredMsg(donation);
				}
			}
		}
		flash(ControllerUtil.FLASH_SUCCESS_KEY, "Donation has been updated");
		return redirect(routes.Application.profileSearchDonations(0, "dateCreated",
				"asc", "", "dateCreated"));
	}

	//====================new update==================22.09.2015=========================end================================//



	@Restrict({ @Group(SecurityRole.ROOT_ADMIN), @Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
					@Group(SecurityRole.EVENT_ASSIST) })
	@SubjectPresent(content = "/login")
	public static Result updateInline(Event event, Pfp pfp, Long donationId) {
		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		if (pfp.isIdOnly()) {
			pfp = Pfp.findByIdWithMin(pfp.id);
		}
		if (!Event.canManage(ControllerUtil.getLocalUser(session()), event)) {
			flash(ControllerUtil.FLASH_DANGER_KEY,
							"The requested event action cannot be completed by the logged in user.");
			return redirect(routes.Application.index());
		}
		final Map<String, String> formMap = form(Donation.class).bindFromRequest().data();
		final Donation donation = Donation.findById(donationId);
		PaymentStatus originalStatus = donation.status;
		if (PaymentType.CHECK == PaymentType.get(formMap.get("paymentType"))
						&& PaymentStatus.CLEARED == PaymentStatus.get(formMap.get("status"))
						&& PaymentStatus.CLEARED != originalStatus) {
			donation.datePaid = new Date();
		}
		if (formMap.containsKey("paymentType")) {
			donation.paymentType = PaymentType.get(formMap.get("paymentType"));
		}
		if (formMap.containsKey("status")) {
			donation.status = PaymentStatus.get(formMap.get("status"));
		}
		if (formMap.containsKey("amount")) {
			donation.amount = Integer.parseInt(formMap.get("amount"));
		}
		donation.update(donationId);
		if (PaymentType.CHECK == PaymentType.get(formMap.get("paymentType"))
						&& PaymentStatus.CLEARED == PaymentStatus.get(formMap.get("status"))
						&& PaymentStatus.CLEARED != originalStatus) {
			ReceiptMgmt.sendCheckReceivedReceipt(donation);
			if (pfp.pfpType == PfpType.PFP) {
				ReceiptMgmt.sendSponsoredMsg(donation);
			}
		}
		flash(ControllerUtil.FLASH_SUCCESS_KEY, "Donation has been updated");
		return redirect(routes.Application.profileSearchDonations(0, "dateCreated",
						"asc", "", "dateCreated"));
	}




	/**** --------------------Start Code Authorize.net-------11.01.2016- start---------------------
	 * @param response*****/
	/*public static Result getResponseFromAuthorize(){
		System.out.println("Enter getResponseFromAuthorizeeee");

		String paymentStatus="Failed";
		//Map<String,String[]> valForDecrypt=request().queryString();
		//System.out.println("valForDecrypt"+valForDecrypt.toString());
		net.authorize.sim.Result result = net.authorize.sim.Result.createResult(API_LOGIN_ID, API_LOGIN_ID, request().queryString());

	*//*x_response_code
	1—Approved
	2—Declined
	3—Error
	4—Held for Review*//*
		System.out.println("result.getResponseMap().get(x_response_code)"+result.getResponseMap().get("x_response_code"));
		System.out.println("result.getResponseMap().get(x_trans_id)"+result.getResponseMap().get("x_trans_id"));
		System.out.println("result.getResponseMap().get(x_account_number)" + result.getResponseMap().get("x_account_number"));
		System.out.println("result.getResponseMap().get(x_donation_transaction_number)" + result.getResponseMap().get("x_donation_transaction_number"));
		System.out.println("result.getResponseMap().get(x_email_id)" + result.getResponseMap().get("x_email_id"));

		final Form<Donation> donationForm = form(Donation.class);
		Donation donation=Donation.findByTransactionNumber(result.getResponseMap().get("x_donation_transaction_number"));
		if(result.getResponseMap().get("x_response_code").equals("1")){
			donation.status=PaymentStatus.CLEARED;
			paymentStatus="Payment Done successfully";
		}
		if(result.getResponseMap().get("x_response_code").equals("2")){
			donation.status=PaymentStatus.FAILED;
			paymentStatus="Payment Failed";
		}
		if(result.getResponseMap().get("x_response_code").equals("3")){
			donation.status=PaymentStatus.FAILED;
			paymentStatus="Payment Failed";
		}
		if(result.getResponseMap().get("x_response_code").equals("4")){
			donation.status=PaymentStatus.PENDING;
			paymentStatus="Payment Pending";
		}
		donation.ccNum=result.getResponseMap().get("x_account_number");
		donation.update();
		System.out.println("Donation table saved");
	*//*"&x_donation_transaction_number="+donation.transactionNumber+
			"&x_amount="+String.valueOf(donation.amount)+
			"&x_event_id="+event.id+
			"&x_pfp_id="+donation.pfp.id+
			"&x_donation_payment_status="+donation.status+
			"&x_email_id="+donation.email;*//*
		try{
			Transaction transaction = Transaction.class.newInstance() ;
	*//*Transaction transaction = Transaction.findByDonationTranId();*//*
			transaction.accountNumber = result.getResponseMap().get("x_account_number");
			transaction.donationTranId=result.getResponseMap().get("x_donation_transaction_number");
			transaction.email=result.getResponseMap().get("x_email_id");
			transaction.mailSent=false;
			transaction.reason=result.getResponseMap().get("x_response_code");
			transaction.transid=result.getResponseMap().get("x_trans_id");
			transaction.ccname="lll";
			transaction.rcode="";
			transaction.authcode="";
			transaction.activityCheck=false;







	*//*transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber*//*


			transaction.save();
		}catch (Exception ex){
			ex.printStackTrace();
		}


		System.out.println("Transaction Table saved");
		flash(ControllerUtil.FLASH_SUCCESS_KEY, paymentStatus);
		return ok(profileDonationsCreate.render(ControllerUtil
				.getLocalUser(session()), null));
	}
*/




	public static Result getResponseFromAuthorize(CreateTransactionResponse response, Donation donationDetails,
												  String userid) {
		System.out.println("Enter getResponseFromAuthorizeeee");
		PAYMENT_LOGGER.info("***************Enter getResponseFromAuthorize*************************");
		String paymentStatus = "Failed";
		//Map<String,String[]> valForDecrypt=request().queryString();
		//System.out.println("valForDecrypt"+valForDecrypt.toString());
		net.authorize.sim.Result result = net.authorize.sim.Result.createResult(API_LOGIN_ID, API_LOGIN_ID, request().queryString());

	/*x_response_code
	1—Approved
	2—Declined
	3—Error
	4—Held for Review*/
		if (response != null) {
			System.out.println("result.getResponseMap().get(x_response_code)" + response.getTransactionResponse().getResponseCode());//result.getResponseMap().get("x_response_code"));
			System.out.println("result.getResponseMap().get(x_trans_id)" + response.getTransactionResponse().getTransId());//result.getResponseMap().get("x_trans_id"));
			System.out.println("result.getResponseMap().get(x_account_number)" + response.getTransactionResponse().getAccountNumber());//result.getResponseMap().get("x_account_number"));

		}


		System.out.println("result.getResponseMap().get(x_donation_transaction_number)" + donationDetails.transactionNumber);//result.getResponseMap().get("x_donation_transaction_number"));
		System.out.println("result.getResponseMap().get(x_invoice_num)" + donationDetails.invoiceNumber);// result.getResponseMap().get("x_invoice_num"));
		System.out.println("result.getResponseMap().get(x_email_id)" + donationDetails.email);// result.getResponseMap().get("x_email_id"));
		System.out.println("result.getResponseMap().get(x_donation_type)" + donationDetails.donationType);// result.getResponseMap().get("x_donation_type"));
		/****new add***start**/
       /* String transactionNo = result.getResponseMap().get("x_donation_transaction_number");
		String responseTransId = result.getResponseMap().get("x_trans_id");

        PaymentGatewayUtil paymentGatewayUtil = new PaymentGatewayUtil();
		System.out.println("before calling GetSettledBatchList");
		List<String> settledBatchListId =(List<String>)paymentGatewayUtil.GetSettledBatchList(API_LOGIN_ID,TRANSACTION_KEY);
         if(settledBatchListId!=null && settledBatchListId.size()>0){
			 Iterator settledBatchListIdItr = settledBatchListId.iterator();
			 while(settledBatchListIdItr.hasNext()){
				 String settledBatchId = (String)settledBatchListIdItr.next();
				 System.out.println("before calling get transaction list");
				 List<String> settledTransIdList = (List)paymentGatewayUtil.getTransactionList(API_LOGIN_ID, TRANSACTION_KEY, settledBatchId);
			     Iterator settledTransIdListItr = settledTransIdList.iterator();
				 while(settledTransIdListItr.hasNext()){
					 String setteledTransId = (String)settledTransIdListItr.next();

					 if(responseTransId.equals(setteledTransId)){
						 System.out.println("responseTransId.equals(setteledTransId)");
						 updateDonationAndTransactionOnSetteledTransaction(result, responseTransId);
						 break;
					 }else{
						 setUnsetteledTransId(transactionNo, responseTransId);
					 }


				 }



			 }

		 }*/
		/****new add***end**/

		String reasonForFailure = " ";
		final Form<Donation> donationForm = form(Donation.class);
		Donation donation = Donation.findByTransactionNumber(donationDetails.transactionNumber);//result.getResponseMap().get("x_donation_transaction_number")
		System.out.println("donationId ::: " + donation.id);

		if (response != null) {
			if (response.getTransactionResponse().getResponseCode().equals("1")) {
				System.out.println("within reason code 1");
				donation.status = PaymentStatus.CLEARED;
				paymentStatus = "Payment Done successfully";
			} else if (response.getTransactionResponse().getResponseCode().equals("4")) {
				System.out.println("**********within pending**************");
				donation.status = PaymentStatus.PENDING;
				paymentStatus = "Payment Pending";
			} else {
				donation.status = PaymentStatus.FAILED;
				if (response.getTransactionResponse().getErrors() != null) {
					System.out.println("within reason code else");
					Integer extremeErrorIndx = response.getTransactionResponse().getErrors().getError().size() - 1;
					if (!response.getTransactionResponse().getCvvResultCode().equals("M") && !response.getTransactionResponse().getCvvResultCode().isEmpty())
						reasonForFailure = response.getTransactionResponse().getErrors().getError().get(extremeErrorIndx).getErrorText() + " and cvv not matches.";
					else
						reasonForFailure = response.getTransactionResponse().getErrors().getError().get(extremeErrorIndx).getErrorText();
				} else {
					System.out.println("within reason code else1111");
					reasonForFailure = "cvv missmatches.";
				}
				//reasonForFailure = result.getResponseMap().get("x_response_ reason_text");
				paymentStatus = "Payment Failed";
			}
		} else {
			donation.status = PaymentStatus.INITIATED;
			paymentStatus = "No respose from Authorize Net";
			reasonForFailure = "No respose from Authorize Net";
		}

		/*if(response.getTransactionResponse().getResponseCode().equals("2")){


		}
		if(result.getResponseMap().get("x_response_code").equals("3")){
			donation.status=PaymentStatus.FAILED;
			reasonForFailure = result.getResponseMap().get("x_response_ reason_text");
			paymentStatus="Payment Failed";
		}*/
		if (response != null) {
			donation.ccNum = response.getTransactionResponse().getAccountNumber();//result.getResponseMap().get("x_account_number");
		} else {
			donation.ccNum = "";
		}

		donation.invoiceNumber = donationDetails.invoiceNumber;//result.getResponseMap().get("x_invoice_num");
		//donation.ccDigits=null;
		System.out.println("-----donationType1111----- :: " + donation.donationType);
		System.out.println("----donation type2222-------- :: " + donationDetails.donationType);//result.getResponseMap().get("x_donation_type"));
		System.out.println("-----amount-----" + donationDetails.amount);//result.getResponseMap().get("x_amount"));
		//System.out.println("---------sponsor item id -----------" + donationDetails.sponsorItem.id);//result
		// .getResponseMap().get
		// ("x_sponsorItem_Id"));
		//donation.amount = Integer.parseInt(result.getResponseMap().get("x_amount"));
		donation.update();
		System.out.println("*********before sponsor response*************"+response);
		/*if (response != null && donation.status == PaymentStatus.CLEARED){*/
			if (donation.donationType == DonationType.SPONSOR) {
				Long sponsorItemId = donationDetails.sponsorItem.id;// Long.parseLong(result.getResponseMap().get("x_sponsorItem_Id"));

				SponsorItem sponsorItem = SponsorItem.findById(sponsorItemId);
			/*donation =*/
				donation.amount = sponsorItem.amount;
				donation.update();
				System.out.println("-------------sponsor amount------------" + donation.amount);
				sponsorItem.donation = donation;
				sponsorItem.update();
				//Donation updatedDonation = Donation.findById(donationForm.get().id);
				//flash(ControllerUtil.FLASH_SUCCESS_KEY, "Sponsorship has been submitted");
				//ReceiptMgmt.sendSponsoredMsg(updatedDonation);
			}
	/*}*/
		System.out.println("Donation table saved");

		try{
			System.out.println("********within try**********");
			Transaction transaction = Transaction.class.newInstance() ;
			/*Transaction transaction = Transaction.findByDonationTranId();*/
			if(response!=null){
				transaction.accountNumber = response.getTransactionResponse().getAccountNumber();//result.getResponseMap().get("x_account_number");
				transaction.reason=response.getTransactionResponse().getResponseCode();//result.getResponseMap().get("x_response_code");
				transaction.transid=response.getTransactionResponse().getTransId();//result.getResponseMap().get("x_trans_id");
			}else{
				transaction.accountNumber = "";//result.getResponseMap().get("x_account_number");
				transaction.reason="";//result.getResponseMap().get("x_response_code");
				transaction.transid="";//result.getResponseMap().get("x_trans_id");
			}



			transaction.donationTranId=donationDetails.transactionNumber;//result.getResponseMap().get("x_donation_transaction_number");
			transaction.email=donationDetails.email;//result.getResponseMap().get("x_email_id");
			transaction.mailSent=false;

			transaction.ccname="lll";
			//transaction.rcode="";
			transaction.rcode = reasonForFailure;
			transaction.authcode="";
			transaction.activityCheck=false;













			transaction.save();


			PAYMENT_LOGGER.info("****saved successfully getResponseAuthorize transaction id ****"+transaction
					.id+"********transaction response text*****"+transaction.reason);


			/*********new add***********************21.01.2016**************************/
			System.out.println("***************payment status before donation.status == PaymentStatus" +
					".CLEARED************"+donation.status);


				//MAIL_LOGGER.info("*** Donation.PaymentStatus.CLEARED ***");

				String creditCardNumber = transaction.accountNumber;
				System.out.println("-----------------donation.ccDigits1------------------------"+creditCardNumber);
				if (creditCardNumber != null && creditCardNumber.trim().length()>0) {
					creditCardNumber = creditCardNumber.substring(creditCardNumber.length() - 4, creditCardNumber.length());
					System.out.println("creditCardNumber :: " + creditCardNumber);
					donation.ccNum = creditCardNumber;
					donation.ccDigits = creditCardNumber;


				}

				donation.ccName = transaction.ccname;
				System.out.println("-----------------donation.ccDigits------------------------"+donation.ccDigits);
				donation.update();


			if (donation.status == PaymentStatus.CLEARED) {

				ReceiptMgmt receiptMgmt = new ReceiptMgmt();

				System.out.println(("before calling getAndSendCCReceipt"));
				System.out.println("donation.event " + donation.event);
				System.out.println("donation.event.userAdmin " + donation.event.userAdmin);
				if(donation.donationType == DonationType.SPONSOR){



					Long sponsorItemId = donationDetails.sponsorItem.id;//Long.parseLong(result.getResponseMap().get
					// ("x_sponsorItem_Id"));

					SponsorItem sponsorItem = SponsorItem.findById(sponsorItemId);
			/*donation =*/
					/*donation.amount = sponsorItem.amount;
					donation.update();*/
					System.out.println("-------------sponsor amount------------"+donation.amount);
					sponsorItem.donation = donation;
					sponsorItem.update();

					//ReceiptMgmt.sendSponsoredMsg(donation);
				}else {
					System.out.println("");
					receiptMgmt.sendCCReceiptForCron(donation);
					//System.out.println("result :: " + result);
					System.out.println(("after calling getAndSendCCReceipt"));

					System.out.println("before calling sendCCReceiptForPfp...");
					receiptMgmt.sendCCReceiptForPfp(donation);
					System.out.println("after calling sendCCReceiptForPfp");
				}
				//update
				transaction.mailSent = true;
				transaction.update();


			}
				/******/

		}catch (Exception ex){
			PAYMENT_LOGGER.error("*************error in getResponseFromAuthorize******************"+ex.getMessage());
			ex.printStackTrace();
		}

		System.out.println("*********************event id*********************" + donationDetails.event.id);//result.getResponseMap().get("x_event_id"));
		Long eventId = donationDetails.event.id;//Long.parseLong(result.getResponseMap().get( "x_event_id"));
		//Long userId = Long.parseLong(result.getResponseMap().get( "&x_user_id"));
		System.out.println("*********************user id*********************" + userid);//result.getResponseMap().get("x_user_id"));
		String userId = userid;//result.getResponseMap().get("x_user_id");
     	Event event =Event.findById(eventId);
		//final User user = User.findByUserId(userId)
		System.out.println("**************Final donationId********************"+donation.id);
		System.out.println("**************Final donation.ccDigits***********************"+donation.ccDigits);
		System.out.println("Transaction Table saved");
		flash(ControllerUtil.FLASH_SUCCESS_KEY, paymentStatus);
		//return ok(profileDonationsCreate.render(User.findByUserId(userId), event));
		//return ok(profileMain.render(User.findByUserId(userId)));
		PAYMENT_LOGGER.info("***************Exit getResponseFromAuthorize*************************");
		return ok(index.render());
	}
public static HashMap transIdForTransactionNo = new HashMap();
  public static void setUnsetteledTransId(String transactionNo,String responseTransId){
	  transIdForTransactionNo.put(transactionNo,responseTransId);
  }
	public static void getUnsetteledTransId(String transactionNo){
		String responseTransId = (String)transIdForTransactionNo.get(transactionNo);
		/****new add***start**/
		//String transactionNo = result.getResponseMap().get("x_donation_transaction_number");
		//StresponseTransId = result.getResponseMap().get("x_trans_id");
		net.authorize.sim.Result result = net.authorize.sim.Result.createResult(API_LOGIN_ID, API_LOGIN_ID, request().queryString());
		PaymentGatewayUtil paymentGatewayUtil = new PaymentGatewayUtil();
		System.out.println("before calling GetSettledBatchList");
		List<String> settledBatchListId =(List<String>)paymentGatewayUtil.GetSettledBatchList(API_LOGIN_ID,TRANSACTION_KEY);
		if(settledBatchListId!=null && settledBatchListId.size()>0){
			Iterator settledBatchListIdItr = settledBatchListId.iterator();
			while(settledBatchListIdItr.hasNext()){
				String settledBatchId = (String)settledBatchListIdItr.next();
				System.out.println("before calling get transaction list");
				List<String> settledTransIdList = (List)paymentGatewayUtil.getTransactionList(API_LOGIN_ID, TRANSACTION_KEY, settledBatchId);
				Iterator settledTransIdListItr = settledTransIdList.iterator();
				while(settledTransIdListItr.hasNext()){
					String setteledTransId = (String)settledTransIdListItr.next();

					if(responseTransId.equals(setteledTransId)){
						System.out.println("responseTransId.equals(setteledTransId)");
						updateDonationAndTransactionOnSetteledTransaction1(result, responseTransId);
						break;
					}else{
						setUnsetteledTransId(transactionNo, responseTransId);
					}


				}



			}

		}
		/****new add***end**/
	}


	public static  void updateDonationAndTransactionOnSetteledTransaction1(net.authorize.sim.Result result ,String responseTransId){
		final Form<Donation> donationForm = form(Donation.class);
		String paymentStatus = null;
		Donation donation=Donation.findByTransactionNumber(result.getResponseMap().get("x_donation_transaction_number"));
		if(result.getResponseMap().get("x_response_code").equals("1")){
			donation.status=PaymentStatus.CLEARED;
			paymentStatus="Payment Done successfully";
		}
		if(result.getResponseMap().get("x_response_code").equals("2")){
			donation.status=PaymentStatus.FAILED;
			paymentStatus="Payment Failed";
		}
		if(result.getResponseMap().get("x_response_code").equals("3")){
			donation.status=PaymentStatus.FAILED;
			paymentStatus="Payment Failed";
		}
		if(result.getResponseMap().get("x_response_code").equals("4")){
			donation.status=PaymentStatus.PENDING;
			paymentStatus="Payment Pending";
		}
		donation.ccNum=result.getResponseMap().get("x_account_number");
		donation.update();
		System.out.println("Donation table saved");
	/*"&x_donation_transaction_number="+donation.transactionNumber+
			"&x_amount="+String.valueOf(donation.amount)+
			"&x_event_id="+event.id+
			"&x_pfp_id="+donation.pfp.id+
			"&x_donation_payment_status="+donation.status+
			"&x_email_id="+donation.email;*/
		try{
			Transaction transaction = Transaction.class.newInstance() ;
	/*Transaction transaction = Transaction.findByDonationTranId();*/
			transaction.accountNumber = result.getResponseMap().get("x_account_number");
			transaction.donationTranId=result.getResponseMap().get("x_donation_transaction_number");
			transaction.email=result.getResponseMap().get("x_email_id");
			transaction.mailSent=false;
			transaction.reason=result.getResponseMap().get("x_response_code");
			/*transaction.transid=result.getResponseMap().get("x_trans_id");*/
			transaction.transid=responseTransId;
			transaction.ccname="lll";
			transaction.rcode="";
			transaction.authcode="";
			transaction.activityCheck=false;







	/*transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber*/


			transaction.save();
		}catch (Exception ex){
			ex.printStackTrace();
		}


		System.out.println("Transaction Table saved");

	}



	public static  Result updateDonationAndTransactionOnSetteledTransaction(net.authorize.sim.Result result ,String responseTransId){
		final Form<Donation> donationForm = form(Donation.class);
		String paymentStatus = null;
		Donation donation=Donation.findByTransactionNumber(result.getResponseMap().get("x_donation_transaction_number"));
		if(result.getResponseMap().get("x_response_code").equals("1")){
			donation.status=PaymentStatus.CLEARED;
			paymentStatus="Payment Done successfully";
		}
		if(result.getResponseMap().get("x_response_code").equals("2")){
			donation.status=PaymentStatus.FAILED;
			paymentStatus="Payment Failed";
		}
		if(result.getResponseMap().get("x_response_code").equals("3")){
			donation.status=PaymentStatus.FAILED;
			paymentStatus="Payment Failed";
		}
		if(result.getResponseMap().get("x_response_code").equals("4")){
			donation.status=PaymentStatus.PENDING;
			paymentStatus="Payment Pending";
		}
		donation.ccNum=result.getResponseMap().get("x_account_number");
		donation.update();
		System.out.println("Donation table saved");
	/*"&x_donation_transaction_number="+donation.transactionNumber+
			"&x_amount="+String.valueOf(donation.amount)+
			"&x_event_id="+event.id+
			"&x_pfp_id="+donation.pfp.id+
			"&x_donation_payment_status="+donation.status+
			"&x_email_id="+donation.email;*/
		try{
			Transaction transaction = Transaction.class.newInstance() ;
	/*Transaction transaction = Transaction.findByDonationTranId();*/
			transaction.accountNumber = result.getResponseMap().get("x_account_number");
			transaction.donationTranId=result.getResponseMap().get("x_donation_transaction_number");
			transaction.email=result.getResponseMap().get("x_email_id");
			transaction.mailSent=false;
			transaction.reason=result.getResponseMap().get("x_response_code");
			/*transaction.transid=result.getResponseMap().get("x_trans_id");*/
			transaction.transid=responseTransId;
			transaction.ccname="lll";
			transaction.rcode="";
			transaction.authcode="";
			transaction.activityCheck=false;







	/*transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber
	transaction.accountNumber*/


			transaction.save();


		/*	if (donation.status == Donation.PaymentStatus.CLEARED) {

				//MAIL_LOGGER.info("*** Donation.PaymentStatus.CLEARED ***");

				String creditCardNumber = transaction.accountNumber;

				if (creditCardNumber != null) {
					creditCardNumber = creditCardNumber.substring(creditCardNumber.length() - 4, creditCardNumber.length());
					System.out.println("creditCardNumber :: " + creditCardNumber);
					donation.ccNum = creditCardNumber;
				}

				donation.ccName = transaction.ccname;
				ReceiptMgmt receiptMgmt = new ReceiptMgmt();

				System.out.println(("before calling getAndSendCCReceipt"));
				System.out.println("donation.event "+donation.event);
				System.out.println("donation.event.userAdmin "+donation.event.userAdmin);
				System.out.println("");
				receiptMgmt.sendCCReceiptForCron(donation);
				//System.out.println("result :: " + result);
				System.out.println(("after calling getAndSendCCReceipt"));

				System.out.println("before calling sendCCReceiptForPfp...");
				receiptMgmt.sendCCReceiptForPfp(donation);
				System.out.println("after calling sendCCReceiptForPfp");

				//update
				transaction.mailSent = true;
				transaction.update();


			}*/





		}catch (Exception ex){
			ex.printStackTrace();
		}


		System.out.println("Transaction Table saved");
		flash(ControllerUtil.FLASH_SUCCESS_KEY, paymentStatus);
		return ok(profileDonationsCreate.render(ControllerUtil
				.getLocalUser(session()), null));
	}

	/**** --------------------Start Code Authorize.net-------11.01.2016- -end----------------------*****/


	/***********************start code refund transaction*************14.01.2016***********************/

	public static Result refundDonations(Long donId,int transactionId, String tranNumber, int amount){
		System.out.println("###########tranNumber within refundDonations####################### "+tranNumber);
		String expDate = "1216";
		System.out.println("id within refundDonations "+donId);
		Donation donation = Donation.findById(donId);
		Transaction transaction = Transaction.findById(transactionId);
		System.out.println("donation "+donation.id);
		String donationAmount = String.valueOf(amount);
		System.out.println("tranNumber within refundDonations "+tranNumber);
		System.out.println("ccNum within refundDonations "+transaction.accountNumber);
		String ccNum =transaction.accountNumber;
		ccNum = ccNum.substring(ccNum.length()-4,ccNum.length());
		System.out.println("ccNum1 within refundDonations "+ccNum);
		System.out.println("donationAmount "+donationAmount);
		donationAmount = donationAmount+".00";
		System.out.println("donationAmount1 "+donationAmount);

		PaymentGatewayUtil paymentGatewayUtil = new PaymentGatewayUtil();
		//String refundedVal=paymentGatewayUtil.anotherRefundmethod(ccNum, expDate, tranNumber, donationAmount);



		HashMap refundHashMap =paymentGatewayUtil.anotherRefundmethod(ccNum, expDate, tranNumber, donationAmount);
		String status = (String)refundHashMap.get("status");
		if(status.equals("successful")){
			donation.status = PaymentStatus.REFUNDED;
			donation.update();
			String refundTransactionId = (String)refundHashMap.get("transactionId");
			System.out.println("refundTransactionId :: "+refundTransactionId);
			flash(ControllerUtil.FLASH_SUCCESS_KEY, "Transaction has been refunded");
		}else{
			flash(ControllerUtil.FLASH_SUCCESS_KEY, "Refund is not successful");
		}





	/*	if(refundedVal.equals("1")) {
			donation.status = PaymentStatus.REFUNDED;
			donation.update();
		}*/


		final User localUser = ControllerUtil.getLocalUser(session());
		return ok(profileMain.render(localUser));
	}

	/************************end code refund transaction**************14.01.2016***********************/
	
}