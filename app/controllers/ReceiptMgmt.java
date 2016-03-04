package controllers;

import akka.actor.Cancellable;
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
		donation.ccName = donation.firstName+" "+donation.lastName;
		System.out.println("*********donation.ccName within sendCCReceipt********"+donation.ccName);
		String message = views.txt.donations.email_cc_receipt
				.render(donation).toString();
		ReceiptMgmt.generateEmailReceipt(ReceiptMgmt.getGeneralSubject(donation), message, donation.email);
		RECEIPT_LOGGER.info("Successfully sent a cc receipt for PFP ID [{}] and Event ID [{}] with a Donation ID [{}] and Transaction Number [{}] in the amount of [{}]",
						donation.pfp.id, donation.event.id, donation.id, donation.transactionNumber, donation.amount);
	}



	public static void sendCCReceiptForCron(Donation donation) {
		if(donation!=null){
			String message = views.txt.donations.email_cc_receipt
					.render(donation).toString();
			RECEIPT_LOGGER.info("*** Before calling emailReceipt for Tax Letter ***");
			ReceiptMgmt.generateEmailReceipt("Scholastic Challenge : Tax Letter", message, donation.email);
			RECEIPT_LOGGER.info("*** Successfully calling emailReceipt for Tax Letter for PFP ID [{}] and Event ID [{}] with a Donation ID [{}] and Transaction Number [{}] in the amount of [{}] :: ",
					donation.pfp.id, donation.event.id, donation.id, donation.transactionNumber, donation.amount +" ***");
		}

	}


	//=====================mail sent to pfp================start===============21.09.2015==================================//


	public static void sendCCReceiptForPfp(Donation donation){
		String message = views.txt.donations.email_cc_pfp_receipt
				.render(donation).toString();
		System.out.println("donation.pfp.emergencyContact :: " + donation.pfp.userAdmin.email);
		System.out.println("message in sendCCReceiptForPfp.." + message);
		RECEIPT_LOGGER.info("*** Before calling emailReceipt for Donation Receipt Letter ***");
		ReceiptMgmt.generateEmailReceipt("Scholastic Challenge : Donation Receipt Letter", message, donation.pfp.userAdmin.email);
		RECEIPT_LOGGER.info("*** Successfully calling emailReceipt for Donation Receipt Letter for PFP ID [{}] and Event ID [{}] with a Donation ID [{}] and Transaction Number [{}] in the amount of [{}] :: ",
				donation.pfp.id, donation.event.id, donation.id, donation.transactionNumber, donation.amount+" ***");
	}

	//====================mailsent to pfp=================end================21.09.2015===================================//



	public static Result getCCReceipt(Event event, Donation donation) {
		donation.ccName = donation.firstName+" "+donation.lastName;
		System.out.println("************donation.ccName within getCCReceipt********"+donation.ccName);
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
		System.out.println("********donation.ccName within getAndSendCCReceipt*******"+donation.ccName);
		donation.ccName = donation.firstName+" "+donation.lastName;
		System.out.println("********donation.ccName before sendCCReceipt*******"+donation.ccName);
		ReceiptMgmt.sendCCReceipt(donation);
		System.out.println("********donation.ccName after sendCCReceipt*******" + donation.ccName);
		donation.ccName = donation.firstName+" "+donation.lastName;
		System.out.println("********donation.ccName before getCCReceipt*******"+donation.ccName);
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




	/*********start********send mail to event manager for cash donation************09.10.2015**********/
	public static void sendCashDonationMsgToEventManager(Donation donation) {
		String subject = "Scholastic Challenge - You have been donated in cash";
		String message = views.txt.donations.email_cash_eventManager_receipt
				.render(donation).toString();
		System.out.println("event manager :: "+donation.event.userAdmin.email);

		ReceiptMgmt.generateEmailReceipt(subject, message, donation.event.userAdmin.email);

	}
	/**********end*********send mail to event manager for cash donation************09.10.2015**********/

	private static void generateEmailReceipt(String subject, String content, String email) {
		System.out.println("subject in generateEmailReceipt :: " + subject);
		final Body body = new Body(content);
		RECEIPT_LOGGER.info("*** Before sending mail  ***");
		/*Mailer.getDefaultMailer().sendMail(subject, body,
				email);*/
		Cancellable result = Mailer.getDefaultMailer().sendMail(subject, body,
				email);
		RECEIPT_LOGGER.info("*** After sending mail ::"+result.toString()+"::  ***");
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