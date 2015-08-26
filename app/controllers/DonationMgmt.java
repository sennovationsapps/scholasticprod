package controllers;

import static play.data.Form.form;

import models.aws.S3File;
import play.mvc.Http;
import views.html.donations.*;
import views.html.profile.profileDonationsReconcile;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import models.Donation;
import models.Donation.DonationType;
import models.Donation.PaymentStatus;
import models.Donation.PaymentType;
import models.Event;
import models.Pfp;
import models.Pfp.PfpType;
import models.SponsorItem;
import models.security.SecurityRole;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.LoggerFactory;

import play.Logger;
import play.data.Form;
import play.db.ebean.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import base.utils.PaymentUtils;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;

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
             /*********************Payment gateway start*****************/

			//return redirect("https://secure-test.worldpay.com/wcc/purchase?instId=&cartId=&currency=&desc=&testMode=100");

			/*********************Payment gateway end*****************/

		}
		System.out.println("within validateAndSendCreditInfo event2 ::"+event);
		return redirect("https://secure-test.worldpay.com/wcc/purchase?instId=&cartId=&currency=&desc=&testMode=100");

		//return ok(creditForm.render(event, donationForm.get().pfp, donationForm));


	}




























	@Transactional
	public static Result save(Event event) {

		PAYMENT_LOGGER.warn("within save");
		System.out.println("within save....");
		if (event.isIdOnly()) {
			event = Event.findByIdWithMinAndGeneralFund(event.id);
		}
		// if (!Event.canManage(ControllerUtil.getLocalUser(session()), event))
		// {
		// flash(ControllerUtil.FLASH_DANGER_KEY,
		// "The requested event action cannot be completed by the logged in user.");
		// return redirect(routes.Application.index());
		// }
		final Form<Donation> donationForm = form(Donation.class).bindFromRequest();
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
		Donation donation = donationForm.get();
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
				donation.paymentType = PaymentType.CHECK;
			} else {
				donation.paymentType = PaymentType.CASH;
			}
		}







		if (donation.paymentType == PaymentType.CREDIT) {
			PAYMENT_LOGGER.warn("payment type is credit");
			System.out.println("payment type is credit");
			PAYMENT_LOGGER.info("donation.paymentType is credit " + donation.paymentType);
			/*Map<String, String> ccFormErrors = PaymentUtils.validateCreditForm(donationForm.data());
			if(MapUtils.isNotEmpty(ccFormErrors)) {

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
					return badRequest(creditForm.render(event, donation.pfp, donationForm));
				}
			}*/


			//donation.ccNum = donation.ccNum.replaceAll("\\s","");
			/****************save start**************************/
			//donation.ccDigits = donation.ccNum.substring(Math.max(0, donation.ccNum.length() - 4));
			//==================new add for duplicate checking =====================07.08.2015==================(else braces will be commented to go back to previous form)=========================//
			if (event.isIdOnly()) {
				event = Event.findByIdWithMinAndGeneralFund(event.id);
			}


			System.out.println("within save......donation.pfp.id "+donation.pfp.id);
			System.out.println("before calling findDuplicateDonationToSameDonor.. ");
			boolean isDuplicationOfDonation = donation.findDuplicateDonationToSameDonor( donation.pfp.id, donation.donorName, event.id);
			System.out.println("isDuplicationOfDonation :: "+isDuplicationOfDonation);
			if(isDuplicationOfDonation == true){
				System.out.println("isDuplicationOfDonation == true");
				donationForm.reject("Please Donate to Different Participant");
				return badRequest(createForm.render(event, donationForm.get().pfp, donationForm));
			}
			//==================new add for duplicate checking =====================07.08.2015===========(else braces will be commented to go back to previous form)==============================//

			else{
			donation.status = PaymentStatus.APPROVED;
			PAYMENT_LOGGER.info("Successfully submitted transaction to Virtual Merchant for CCNum [{}] and Transaction ID [{}] in the amount of [{}]",
					donation.ccDigits, donation.transactionNumber, donation.amount);
			try {
				donation.save();
				PAYMENT_LOGGER.warn("payment type is credit11");
				System.out.println("payment type is credit11");
				return ok(creditForm.render(event, donationForm.get().pfp, donationForm));
			} catch (Exception e) {
				PAYMENT_LOGGER.error("An error occurred saving the Donation so it will be refunded " + ToStringBuilder.reflectionToString(donation));
				PAYMENT_LOGGER.error("The error that caused the db failure [{}]", e.getMessage());
				Map<String, String> response = new HashMap<String, String>();
				try {
					response = PaymentUtils.sendCCRefund(donation);
				} catch (Exception r) {
					PAYMENT_LOGGER.error("Failed to refund donation  to Virtual Merchant for CCNum [{}] and Transaction ID [{}] in the amount of [{}]",
							donation.ccDigits, donation.transactionNumber, donation.amount);
					PAYMENT_LOGGER.error("The error that caused the refund failure [{}]", r.getMessage());
				}
				Map<String, String> errors = PaymentUtils.validateCreditPayment(response);
				donationForm.reject("An error has occurred in our submission of your donation, please try your donation again.");
				if (donationForm.hasErrors()) {
					Logger.debug("Has errors {}", donationForm.errorsAsJson());
					PAYMENT_LOGGER.warn("payment type is credit22");
					System.out.println("payment type is credit22");
					return ok(creditForm.render(event, donationForm.get().pfp, donationForm));
					//return badRequest(createForm.render(event, donationForm.get().pfp, donationForm));
				}
			}
			PAYMENT_LOGGER.warn("payment type is credit33");
			System.out.println("payment type is credit33");
			return ok(creditForm.render(event, donationForm.get().pfp, donationForm));
		}

		/******************save  end*******************/
		/**************payment gateway*************************/
			/*Map<String, String> ccProps = PaymentUtils.sendCCPayment(donation);
			Map<String, String> ccErrors = PaymentUtils.validateCreditPayment(ccProps);
			if(MapUtils.isNotEmpty(ccErrors)) {
				for(String key: ccErrors.keySet()) {
					if(StringUtils.isEmpty(key)) {
						donationForm.reject(ccErrors.get(key));
					} else if(StringUtils.startsWith(key, "_")) {
						donationForm.reject(ccErrors.get(key));
					} else {
						donationForm.reject(key, ccErrors.get(key));
					}
				}
				if (donationForm.hasErrors()) {
					Logger.debug("Has errors {}", donationForm.errorsAsJson());
					return badRequest(createForm.render(event, donationForm.get().pfp, donationForm));
				}
			}
			donation.transactionNumber = ccProps.get("ssl_txn_id");
			if (StringUtils.isEmpty(donation.transactionNumber)) {
				Logger.error("There is no transaction number for the ccNum {} and props {}.", donationForm.get().ccNum,
								ToStringBuilder.reflectionToString(ccProps));
			}*/

			//PAYMENT_LOGGER.info("A CC Donation [{}] with transaction number [{}] was made for PFP [{}] in the amount of [{}] by [{} {}] with CC [{}]", donation.id, donation.transactionNumber, donation.pfp.id, donation.amount, donation.firstName, donation.lastName, donation.ccDigits);
		} else if (donation.paymentType == PaymentType.CHECK) {
			donation.transactionNumber = UUID.randomUUID().toString();
			donation.status = PaymentStatus.PENDING;
			donation.save();
			PAYMENT_LOGGER.info("A Check Donation [{}] was made for PFP ID [{}] in the amount of [{}] by [{} {}]", donation.id, donation.pfp.id, donation.amount, donation.firstName, donation.lastName);
		} else {
			donation.status = PaymentStatus.CLEARED;
			donation.datePaid = new Date();
			donation.save();
			PAYMENT_LOGGER.info("A Cash Donation [{}] was made for PFP ID [{}] in the amount of [{}] by [{} {}]", donation.id, donation.pfp.id, donation.amount, donation.firstName, donation.lastName);
		}
		
		Donation updatedDonation = Donation.findById(donation.id);
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



/*
		final Http.MultipartFormData body = request().body()
				.asMultipartFormData();
		System.out.println("body :: "+body);
		final Http.MultipartFormData.FilePart imgUrlFilePart = body
				.getFile("imgUrl");
		System.out.println("imgUrlFilePart :: "+imgUrlFilePart);
		S3File imgUrlFile = null;
		if (imgUrlFilePart != null) {
			if (!ControllerUtil.isImage(imgUrlFilePart.getFilename())) {
				donationForm.reject("imgUrl", ControllerUtil.IMAGE_ERROR_MSG);
			} else if (ControllerUtil.isFileTooLarge(imgUrlFilePart.getFile())) {
				donationForm.reject("imgUrl", ControllerUtil.IMAGE_SIZE_ERROR_MSG);
			} else {
				imgUrlFile = new S3File();
				imgUrlFile.name = ControllerUtil.decodeFileName(imgUrlFilePart
						.getFilename());
				imgUrlFile.file = imgUrlFilePart.getFile();
				donationForm.get().imgUrl = imgUrlFile.getUrl();
			}
		}*/




		if (donationForm.hasErrors()) {
			Logger.debug("Has errors {}", donationForm.errorsAsJson());
			return badRequest(views.html.sponsors.createDonationForm.render(event, pfp, sponsorItem,
							donationForm));
		}
		Donation donation = donationForm.get();
		donation.dateCreated = new Date();
		donation.invoiceNumber = donation.event.id + "_" + donation.dateCreated.getTime();
		donation.donationType = DonationType.SPONSOR;
		if (donation.paymentType == null) {
			if (StringUtils.isNotEmpty(donation.ccNum)) {
				donation.paymentType = PaymentType.CREDIT;
			} else if (StringUtils.isNotEmpty(donation.ccCvvCode)) {
				donation.paymentType = PaymentType.CREDIT;
			} else {
				donation.paymentType = PaymentType.CHECK;
			}
		}
		if (donation.paymentType == PaymentType.CREDIT) {
			Map<String, String> ccFormErrors = PaymentUtils.validateCreditForm(donationForm.data());
			if(MapUtils.isNotEmpty(ccFormErrors)) {
				for(String key: ccFormErrors.keySet()) {
					if(StringUtils.isEmpty(key)) {
						donationForm.reject(ccFormErrors.get(key));
					} else {
						donationForm.reject(key, ccFormErrors.get(key));
					}
				}
				if (donationForm.hasErrors()) {
					Logger.debug("Has errors {}", donationForm.errorsAsJson());
					return badRequest(views.html.sponsors.createDonationForm.render(event, pfp, sponsorItem,
									donationForm));
				}
			}
			donation.ccNum = donation.ccNum.replaceAll("\\s","");
			Map<String, String> ccProps = PaymentUtils.sendCCPayment(donation);
			Map<String, String> ccErrors = PaymentUtils.validateCreditPayment(ccProps);
			if(MapUtils.isNotEmpty(ccErrors)) {
				for(String key: ccErrors.keySet()) {
					if(StringUtils.isEmpty(key)) {
						donationForm.reject(ccErrors.get(key));
					} else if(StringUtils.startsWith(key, "_")) {
						donationForm.reject(ccErrors.get(key));
					} else {
						donationForm.reject(key, ccErrors.get(key));
					}
				}
				if (donationForm.hasErrors()) {
					Logger.debug("Has errors {}", donationForm.errorsAsJson());
					return badRequest(views.html.sponsors.createDonationForm.render(event, pfp, sponsorItem,
									donationForm));
				}
			}
			donation.status = PaymentStatus.APPROVED;
			donation.paymentType = PaymentType.CREDIT;
			donation.transactionNumber = ccProps.get("ssl_txn_id");
			if (StringUtils.isEmpty(donation.transactionNumber)) {
				Logger.error("There is no transaction number for the ccNum {} and props {}.", donationForm.get().ccNum,
								ToStringBuilder.reflectionToString(ccProps));
			}
			donation.ccDigits = donation.ccNum.substring(Math.max(0, donation.ccNum.length() - 4));
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

		System.out.println("in saveModalWithSponsorrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr");


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
			return redirect(routes.ReceiptMgmt.getAndSendCCReceipt(event, updatedDonation));
		} else {
			System.out.println("before calling getAndSendCheckReceipt..");
			return redirect(routes.ReceiptMgmt.getAndSendCheckReceipt(event, updatedDonation));
		}
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