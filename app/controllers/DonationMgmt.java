package controllers;

import base.utils.PaymentUtils;
import base.utils.WorldPayUtils;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import models.Donation;
import models.Donation.DonationType;
import models.Donation.PaymentStatus;
import models.Donation.PaymentType;
import models.Event;
import models.Pfp;
import models.Pfp.PfpType;
import models.SponsorItem;
import models.aws.S3File;
import models.security.SecurityRole;
import models.security.User;
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
import views.html.profile.profileDonationsReconcile;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

import static play.data.Form.form;
/**
 * Manage a database of donations.
 */

public class DonationMgmt extends Controller {
	
	private static final org.slf4j.Logger PAYMENT_LOGGER = LoggerFactory.getLogger("ScholasticDonationsLogger");
	
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
					if(donation.status == PaymentStatus.APPROVED && StringUtils.equalsIgnoreCase(props.get("ssl_transaction_type"), "Sale")) {
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









	@Transactional
	public static Result save(Event event) {

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
			} else {
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
		Donation donation = donationForm.get(); // donation form in donation variable fdatsuv
		donation.dateCreated = new Date();
		if(event.id != Pfp.findEventIdByPfpId(donation.pfp.id)) {
			PAYMENT_LOGGER.warn("The donation [{}] being made for pfp [{}] does not match the event [{}].", donation.id, donation.pfp.id, donation.event.id);
			donation.event = donation.pfp.event;
		} else {
			donation.event = event;
		}
		donation.invoiceNumber = donation.event.id + "_" + donation.dateCreated.getTime();
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
			System.out.println("within save......donation.pfp.id "+donation.pfp.id);
			System.out.println("before calling findDuplicateDonationToSameDonor.. ");
			/*boolean isDuplicationOfDonation = donation.findDuplicateDonationToSameDonor( donation.pfp.id, donation.donorName, event.id);
			System.out.println("isDuplicationOfDonation :: "+isDuplicationOfDonation);
			if(isDuplicationOfDonation == true){
				System.out.println("isDuplicationOfDonation == true");
				donationForm.reject("Please Donate to Different Participant");
				return badRequest(createForm.render(event, donationForm.get().pfp, donationForm));
			}
			else{*/
			donation.status = PaymentStatus.APPROVED;
			donation.transactionNumber = UUID.randomUUID().toString();
			PAYMENT_LOGGER.info("Successfully submitted transaction to Virtual Merchant for CCNum [{}] and Transaction ID [{}] in the amount of [{}]",
					donation.ccDigits, donation.transactionNumber, donation.amount);
			try {
				donation.save();  // main donation table saved
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
					return badRequest(createForm.render(event, donationForm.get().pfp, donationForm));
				}
				return badRequest(createForm.render(event, donationForm.get().pfp, donationForm));
			}
			PAYMENT_LOGGER.warn("payment type is credit33");
			System.out.println("payment type is credit33");
			//return ok(creditForm.render(event, donationForm.get().pfp, donationForm));
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
		System.out.println("within validateAndSendCreditInfo now URL sdfdsfsfsfsdfsdfsdfsdfdsfdsfsdfsdfdsf"+WorldPlayUrl);

		return redirect(WorldPlayUrl);
			//return redirect("https://trans.worldpay.us/cgi-bin/WebPay.cgi?formid=574301A941C9A095E474EF84D558739DC1AD0EE09278E5E321CB1E4970121245&sessionid=62A6DC8C9A988EA9");
		//}

		} else if (donation.paymentType == PaymentType.CHECK) {
			System.out.println("PaymentType.CHECK 111:"+donation.paymentType);
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
		
		Donation updatedDonation = Donation.findById(donation.id);
		System.out.println("after save :: "+donation.status);
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
		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		if (pfp.isIdOnly()) {
			pfp = Pfp.findByIdWithMin(pfp.id);
		}
		SponsorItem sponsorItem = SponsorItem.findById(sponsorItemId);
		final Form<Donation> donationForm = form(Donation.class).bindFromRequest();

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
		donation.invoiceNumber = donation.event.id + "_" + donation.dateCreated.getTime();
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
			donation.status = PaymentStatus.APPROVED;
			donation.paymentType = PaymentType.CREDIT;
			donation.transactionNumber =UUID.randomUUID().toString();
			//ccProps.get("ssl_txn_id");
			if (StringUtils.isEmpty(donation.transactionNumber)) {
//				Logger.error("There is no transaction number for the ccNum {} and props {}.", donationForm.get().ccNum,
//								ToStringBuilder.reflectionToString(ccProps));
			}
			//donation.ccDigits = donation.ccNum.substring(Math.max(0, donation.ccNum.length() - 4));
		} else {
			donation.status = PaymentStatus.PENDING;
			donation.paymentType = PaymentType.CHECK;
			donation.transactionNumber = UUID.randomUUID().toString();
		}
		donation.sponsorItem = sponsorItem;
		donation.phone = ControllerUtil.stripPhone(donation.phone);
		donation.event = event;





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
		Logger.info("Successfully submitted transaction to Virtual Merchant for CCNum [{}] and Transaction ID [{}] in the amount of [{}]",
						donation.ccDigits, donation.transactionNumber, donation.amount);
		sponsorItem.donation = donation;
		sponsorItem.update();
		Donation updatedDonation = Donation.findById(donationForm.get().id);
		flash(ControllerUtil.FLASH_SUCCESS_KEY, "Sponsorship has been submitted");
		ReceiptMgmt.sendSponsoredMsg(updatedDonation);
		if (updatedDonation.paymentType == PaymentType.CREDIT) {
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
		} else {
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
		System.out.println("Request form worldpay"+requestData.get("customdata"));

		return ok("Hello in the worldPayPostBack"+requestData.get("customdata"));

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
		if(ControllerUtil.isUserInRole(models.security.SecurityRole.ROOT_ADMIN)){
			events = Event.findAllEvents();
		}else if(ControllerUtil.isUserInRole(SecurityRole.EVENT_ADMIN)){
			events = Event.findAllByUserId(localUser.id);
		}


		Long eventId = Long.parseLong(id);
		Event event = Event.findById(eventId);
		final Form<Donation> donationForm = form(Donation.class);
		List<Donation> donations = Donation.findAllByEventId(eventId);
		List<Pfp> pfps= Pfp.findByEventId(eventId);
		return ok(test.render(donations));
		//return ok(ProfileCashDonations.render(localUser,events, event, pfps, donationForm, donations));


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
			return badRequest(views.html.donations.editForm.render(event, pfp, donationId, donationForm));
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
	
}