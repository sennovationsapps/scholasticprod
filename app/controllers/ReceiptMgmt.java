package controllers;

import com.feth.play.module.mail.Mailer;
import com.feth.play.module.mail.Mailer.Mail.Body;
import models.Donation;
import models.Donation.DonationType;
import models.Event;
import models.Pfp.PfpType;
import org.slf4j.LoggerFactory;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.donations.viewReceipt;

/**
 * Manage a database of donations.
 */

public class ReceiptMgmt extends Controller {

	private static final org.slf4j.Logger RECEIPT_LOGGER = LoggerFactory.getLogger("ScholasticReceiptsLogger");
	
	public static void sendCheckReceipt(Donation donation) {
		String message = views.txt.donations.email_check_receipt
				.render(donation).toString();
		ReceiptMgmt.generateEmailReceipt(ReceiptMgmt.getGeneralSubject(donation), message, donation.email);
		RECEIPT_LOGGER.info("Successfully sent a check receipt for PFP ID [{}] and Event ID [{}] with a Donation ID [{}] in the amount of [{}]",
				donation.pfp.id, donation.event.id, donation.id, donation.amount);
	}

	public static Result getCheckReceipt(Event event, Donation donation) {
		String message = views.html.donations.email_check_receipt
				.render(donation).toString();
		response().setHeader("Cache-Control", "no-cache");
		response().setHeader("Cache-Control", "no-store");
		response().setHeader("Pragma", "no-cache");
		response().setHeader("Expires", "0");

		//===================add================18.08.2015===============start================================//
		/*sendMail("Donations@Scholasticchallenge.com", "4fQb0Cc&sn8g");*/
		//===================add================18.08.2015===============end================================//
		if(donation.pfp.pfpType == PfpType.PFP) {
			return ok(viewReceipt.render(message, donation.event, donation.pfp));	
		}
		return ok(viewReceipt.render(message, donation.event, null));
	}

	public static Result getAndSendCheckReceipt(Event event, Donation donation) {
		System.out.println("within getAndSendCheckReceipt");
		ReceiptMgmt.sendCheckReceipt(donation);
		//===================add================18.08.2015===============start=========send mail=======================//
		/*MailUtils mailUtils = new MailUtils();
		System.out.println("before sending mail");
		System.out.println("donation.email :: "+donation.email);
		mailUtils.sendMail("Donations@Scholasticchallenge.com", "4fQb0Cc&sn8g",donation.email);*/
		//===================add================18.08.2015===============end============send mail====================//
		return ReceiptMgmt.getCheckReceipt(event, donation);
	}
	
	public static void sendCheckReceivedReceipt(Donation donation) {
		String message = views.txt.donations.email_check_received_receipt
				.render(donation).toString();
		ReceiptMgmt.generateEmailReceipt(ReceiptMgmt.getGeneralSubject(donation), message, donation.email);
		RECEIPT_LOGGER.info("Successfully sent a check received receipt for PFP ID [{}] and Event ID [{}] with a Donation ID [{}] in the amount of [{}]",
						donation.pfp.id, donation.event.id, donation.id, donation.amount);
	}

	public static void sendCCReceipt(Donation donation) {
		String message = views.txt.donations.email_cc_receipt
				.render(donation).toString();
		ReceiptMgmt.generateEmailReceipt(ReceiptMgmt.getGeneralSubject(donation), message, donation.email);
		RECEIPT_LOGGER.info("Successfully sent a cc receipt for PFP ID [{}] and Event ID [{}] with a Donation ID [{}] and Transaction Number [{}] in the amount of [{}]",
						donation.pfp.id, donation.event.id, donation.id, donation.transactionNumber, donation.amount);
	}



	public static void sendCCReceiptForCron(Donation donation) {
		String message = views.txt.donations.email_cc_receipt
				.render(donation).toString();
		ReceiptMgmt.generateEmailReceipt("Scholastic Challenge : Tax Letter", message, donation.email);
		RECEIPT_LOGGER.info("Successfully sent a cc receipt for PFP ID [{}] and Event ID [{}] with a Donation ID [{}] and Transaction Number [{}] in the amount of [{}]",
				donation.pfp.id, donation.event.id, donation.id, donation.transactionNumber, donation.amount);
	}

	public static Result getCCReceipt(Event event, Donation donation) {
		String message = views.html.donations.email_cc_receipt
				.render(donation).toString();
		response().setHeader("Cache-Control", "no-cache");
		response().setHeader("Cache-Control", "no-store");
		response().setHeader("Pragma", "no-cache");
		response().setHeader("Expires", "0");
		if(donation.pfp.pfpType == PfpType.PFP) {
			return ok(viewReceipt.render(message, donation.event, donation.pfp));	
		}
		return ok(viewReceipt.render(message, donation.event, null));
	}

	public static Result getAndSendCCReceipt(Event event, Donation donation) {
		ReceiptMgmt.sendCCReceipt(donation);
		return ReceiptMgmt.getCCReceipt(event, donation);
	}
	
	public static void sendCashReceipt(Donation donation) {
		String message = views.txt.donations.email_cash_receipt
				.render(donation).toString();
		ReceiptMgmt.generateEmailReceipt(ReceiptMgmt.getGeneralSubject(donation), message, donation.email);
		RECEIPT_LOGGER.info("Successfully sent a cash receipt for PFP ID [{}] and Event ID [{}] with a Donation ID [{}] in the amount of [{}]",
				donation.pfp.id, donation.event.id, donation.id, donation.amount);
	}

	public static Result getCashReceipt(Event event, Donation donation) {
		String message = views.html.donations.email_cash_receipt
				.render(donation).toString();
		response().setHeader("Cache-Control", "no-cache");
		response().setHeader("Cache-Control", "no-store");
		response().setHeader("Pragma", "no-cache");
		response().setHeader("Expires", "0");
		if(donation.pfp.pfpType == PfpType.PFP) {
			return ok(viewReceipt.render(message, donation.event, donation.pfp));	
		}
		return ok(viewReceipt.render(message, donation.event, null));
	}

	public static Result getAndSendCashReceipt(Event event, Donation donation) {
		ReceiptMgmt.sendCashReceipt(donation);
		return ReceiptMgmt.getCashReceipt(event, donation);
	}
	
	public static void sendSponsoredMsg(Donation donation) {
		String subject = "Scholastic Challenge - You have been sponsored";
		String message = views.txt.donations.email_sponsored_msg
				.render(donation).toString();
		if(donation.donationType == DonationType.SPONSOR) {
			ReceiptMgmt.generateEmailReceipt(subject, message, donation.event.userAdmin.email);
		} else {
			ReceiptMgmt.generateEmailReceipt(subject, message, donation.pfp.userAdmin.email);
		}
	}
	
	private static void generateEmailReceipt(String subject, String content, String email) {
		System.out.println("subject in generateEmailReceipt :: "+subject);
		final Body body = new Body(content);

		Mailer.getDefaultMailer().sendMail(subject, body,
				email);
	}
	
/*
	private static String getGeneralSubject(Donation donation) {
		if(donation.donationType == DonationType.SPONSOR) {


				return "Scholastic Challenge - Sponsorship";



		}

			return "Scholastic Challenge - Donation";


	}
*/



	private static String getGeneralSubject(Donation donation) {
		if(donation.donationType == DonationType.SPONSOR) {
			return "Scholastic Challenge - Sponsorship";
		}
		return "Scholastic Challenge - Donation";
	}



	public static void PaymentPostBackUrl(final String token) {

		System.out.println(token);

	}

}