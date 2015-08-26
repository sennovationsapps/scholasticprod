package controllers;

import static play.data.Form.form;

import models.ContactUs;
import org.apache.commons.lang3.StringUtils;
import play.Configuration;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.contactus;

import com.feth.play.module.mail.Mailer;
import com.feth.play.module.mail.Mailer.Mail.Body;

// TODO: Auto-generated Javadoc
/**
 * The Class ContactMgmt.
 */
public class ContactMgmt extends Controller {

	/**
	 * Contactus.
	 * 
	 * @return the result
	 */
	public static Result contactus() {
		return ok(contactus.render(form(ContactUs.class)));
	}
	
	public static Result contactusToStartEvent() {
		ContactUs contactUs = new ContactUs();
		contactUs.subject = "Starting an Event";
		return ok(contactus.render(form(ContactUs.class).fill(contactUs)));
	}

	/**
	 * Contactus events.
	 * 
	 * @return the result
	 */
	public static Result contactusEvents() {
		return ok(contactus.render(form(ContactUs.class)));
	}

	/**
	 * Send mail.
	 * 
	 * @return the result
	 */
	public static Result sendMail() {
		Logger.debug("Account sendMail");
		final Form<ContactUs> filledForm = form(ContactUs.class)
				.bindFromRequest();
		if (StringUtils.isNotEmpty(filledForm.data().get("mail")) || StringUtils.isNotEmpty(filledForm.data().get("mobile"))) {
			filledForm.reject("Form submission has errors, please contact us if you are receiving this error.");
		}
		if (filledForm.hasErrors()) {
			if (ControllerUtil.isEventPage()) {
				return badRequest(contactus.render(filledForm));
			} else {
				return badRequest(contactus.render(filledForm));
			}
		} else {
			final ContactUs contactUs = filledForm.get();
			contactUs.phone = ControllerUtil.stripPhone(contactUs.phone);
			final Body body = new Body(views.txt.contacts.email_contactus
					.render(contactUs).toString(),
					views.html.contacts.email_contactus.render(contactUs)
							.toString());
			Mailer.getDefaultMailer().sendMail(contactUs.subject, body,
					Configuration.root().getString("mail.contactus.default"));

			flash(ControllerUtil.FLASH_INFO_KEY,
					getEmailMessage(contactUs.firstName));
			return redirect(routes.Application.index());
		}
	}

	/**
	 * Gets the email message.
	 * 
	 * @param firstName
	 *            the first name
	 * @return the email message
	 */
	private static String getEmailMessage(String firstName) {
		return "Dear "
				+ firstName
				+ ", thank you for contacting Scholastic Challenge.  We have received your request and generally respond within 24 hours. Please add Info@scholasticchallenge to your spam filter whitelist to ensure you receive our response.  In the meantime, we have a comprehensive FAQ. Here you will may find the answer to your question as well as others.  Thank you for contact us and we looking forward to helping you.  Scholastic Challenge Support";
	}

}