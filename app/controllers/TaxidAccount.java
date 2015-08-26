package controllers;

import models.security.SecurityRole;
import models.security.User;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Result;
import providers.taxid.TaxidAuthProvider;
import providers.taxid.TaxidAuthUser;
import views.html.account.password_change_taxid;
import views.html.account.unverified;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;

// TODO: Auto-generated Javadoc
/**
 * The Class TaxidAccount.
 */
public class TaxidAccount extends Account {

	/**
	 * Change password.
	 * 
	 * @return the result
	 */
	@SubjectPresent
	public static Result changePassword() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final User u = ControllerUtil.getLocalUser(session());

		if (!u.emailValidated) {
			return ok(unverified.render());
		} else {
			return ok(password_change_taxid.render(Account.PASSWORD_CHANGE_FORM));
		}
	}

	
	/**
	 * Do change password.
	 * 
	 * @return the result
	 */
	@Restrict(@Group(SecurityRole.EVENT_ADMIN))
	public static Result doChangePassword() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<TaxidAccount.PasswordChange> filledForm = Account.PASSWORD_CHANGE_FORM
				.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not select whether to link or not link
			return badRequest(password_change_taxid.render(filledForm));
		} else {
			final User user = ControllerUtil.getLocalUser(session());
			final String newPassword = filledForm.get().password;
			user.changePassword(new TaxidAuthUser(newPassword), true);
			flash(ControllerUtil.FLASH_INFO_KEY,
					Messages.get("playauthenticate.change_password.success"));
			return redirect(routes.Application.profile());
		}
	}

	/**
	 * Verify email.
	 * 
	 * @return the result
	 */
	@Restrict(@Group(SecurityRole.EVENT_ADMIN))
	public static Result verifyEmail() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final User user = ControllerUtil.getLocalUser(session());
		if (user.emailValidated) {
			// E-Mail has been validated already
			flash(ControllerUtil.FLASH_INFO_KEY,
					Messages.get("playauthenticate.verify_email.error.already_validated"));
		} else if ((user.email != null) && !user.email.trim().isEmpty()) {
			flash(ControllerUtil.FLASH_INFO_KEY, Messages.get(
					"playauthenticate.verify_email.message.instructions_sent",
					user.email));
			TaxidAuthProvider.getProvider().sendVerifyEmailMailingAfterSignup(
					user, ctx());
		} else {
			flash(ControllerUtil.FLASH_INFO_KEY, Messages.get(
					"playauthenticate.verify_email.error.set_email_first",
					user.email));
		}
		return redirect(routes.Application.profile());
	}
}
