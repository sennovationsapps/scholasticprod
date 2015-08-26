package controllers;

import static play.data.Form.form;

import models.security.TokenAction;
import models.security.TokenAction.Type;
import models.security.User;

import org.apache.commons.lang.builder.ToStringBuilder;

import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Result;
import providers.email.EmailAuthProvider;
import providers.email.EmailAuthUser;
import providers.taxid.TaxidAuthProvider;
import providers.taxid.TaxidAuthProvider.TaxidUserIdentity;
import providers.taxid.TaxidAuthProvider.TaxidUserLogin;
import providers.taxid.TaxidAuthProvider.TaxidUserSignup;
import providers.taxid.TaxidAuthUser;
import providers.uniqueid.UniqueIdAuthProvider;
import views.html.login_taxid;
import views.html.signup_taxid;
import views.html.account.signup.no_token_or_invalid;
import views.html.account.signup.password_forgot_taxid;
import views.html.account.signup.password_reset_taxid;

import com.feth.play.module.pa.PlayAuthenticate;

import controllers.Signup.PasswordReset;

// TODO: Auto-generated Javadoc
/**
 * The Class TaxidSignup.
 */
public class TaxidSignup extends Signup {

	/** The Constant FORGOT_PASSWORD_FORM. */
	private static final Form<TaxidUserIdentity> FORGOT_PASSWORD_FORM = form(TaxidUserIdentity.class);

	/**
	 * Do forgot password.
	 * 
	 * @return the result
	 */
	public static Result doForgotPassword() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<TaxidUserIdentity> filledForm = FORGOT_PASSWORD_FORM
				.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not fill in his/her email
			return badRequest(password_forgot_taxid.render(filledForm));
		} else {
			// The email address given *BY AN UNKNWON PERSON* to the form - we
			// should find out if we actually have a user with this email
			// address and whether password login is enabled for him/her. Also
			// only send if the email address of the user has been verified.
			final Long uniqueId = filledForm.get().uniqueId;

			// We don't want to expose whether a given email address is signed
			// up, so just say an email has been sent, even though it might not
			// be true - that's protecting our user privacy.

			final User user = User.findByTaxid(uniqueId);
			if (user != null) {
				// yep, we have a user with this email that is active - we do
				// not know if the user owning that account has requested this
				// reset, though.
				final TaxidAuthProvider provider = TaxidAuthProvider
						.getProvider();
				// User exists
				if (user.emailValidated) {
					Logger.debug("Taxid doForgotPassword email is validated");
					flash(ControllerUtil.FLASH_INFO_KEY,
							Messages.get(
									"playauthenticate.reset_password.message.instructions_sent",
									user.email));
					
					provider.sendPasswordResetMailing(user, ctx());
					// In case you actually want to let (the unknown person)
					// know whether a user was found/an email was sent, use,
					// change the flash message
				} else {
					// We need to change the message here, otherwise the user
					// does not understand whats going on - we should not verify
					// with the password reset, as a "bad" user could then sign
					// up with a fake email via OAuth and get it verified by an
					// a unsuspecting user that clicks the link.
					flash(ControllerUtil.FLASH_INFO_KEY,
							Messages.get("playauthenticate.reset_password.message.email_not_verified"));

					// You might want to re-send the verification email here...
					provider.sendVerifyEmailMailingAfterSignup(user, ctx());
				}
			} else {
				flash(ControllerUtil.FLASH_INFO_KEY,
						"Account was not found for taxid=" + uniqueId + ", so reset password email was not sent.");
			}

			return redirect(routes.Application.profilePassword());
		}
	}

	/**
	 * Do login.
	 * 
	 * @return the result
	 */
	public static Result doLogin() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<TaxidUserLogin> filledForm = TaxidAuthProvider.LOGIN_FORM
				.bindFromRequest();
		if (filledForm.hasErrors()) {
			flash(ControllerUtil.FLASH_DANGER_KEY,
					"Unable to login.  Please check the Login Form and try again.");
			// User did not fill everything properly
			return badRequest(login_taxid.render(filledForm));
		} else {
			// Everything was filled
			return TaxidAuthProvider.handleLogin(ctx());
		}
	}

	/**
	 * Do reset password.
	 * 
	 * @return the result
	 */
	public static Result doResetPassword() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<PasswordReset> filledForm = PASSWORD_RESET_FORM
				.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(password_reset_taxid.render(filledForm));
		} else {
			final String token = filledForm.get().token;
			final String newPassword = filledForm.get().password;

			final TokenAction ta = tokenIsValid(token, Type.PASSWORD_RESET);
			if (ta == null) {
				return badRequest(no_token_or_invalid.render());
			}
			final User u = ta.targetUser;
			try {
				// Pass true for the second parameter if you want to
				// automatically create a password and the exception never to
				// happen
				u.resetPassword(new TaxidAuthUser(newPassword), false);
			} catch (final RuntimeException re) {
				flash(ControllerUtil.FLASH_INFO_KEY,
						Messages.get("playauthenticate.reset_password.message.no_password_account"));
			}
			final boolean login = TaxidAuthProvider.getProvider()
					.isLoginAfterPasswordReset();
			if (login) {
				// automatically log in
				flash(ControllerUtil.FLASH_INFO_KEY,
						Messages.get("playauthenticate.reset_password.message.success.auto_login"));

				return PlayAuthenticate.loginAndRedirect(ctx(),
						new TaxidAuthUser(u.email));
			} else {
				// send the user to the login page
				flash(ControllerUtil.FLASH_INFO_KEY,
						Messages.get("playauthenticate.reset_password.message.success.manual_login"));
			}
			return redirect(routes.TaxidSignup.login());
		}
	}

	/**
	 * Do signup.
	 * 
	 * @return the result
	 */
	public static Result doSignup() {
		System.out.println("----------------------------Hello---------------------------");
		Logger.debug("IN the doSignup");
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<TaxidUserSignup> filledForm = TaxidAuthProvider.SIGNUP_FORM
				.bindFromRequest();
		if (StringUtils.isNotEmpty(filledForm.data().get("mail")) || StringUtils.isNotEmpty(filledForm.data().get("mobile"))) {

			/*TaxidUserSignup taxidUserSignup	 = (TaxidUserSignup)filledForm.get();
			taxidUserSignup.agreeToPolicy = false;*/

			filledForm.reject("Form submission has errors, please contact us if you are receiving this error.");
		}
		if (filledForm.hasErrors()) {
			Logger.debug("Errors as json - {}", filledForm.errorsAsJson()
					.toString());
			Logger.debug("IN the doSignup - has errors");
			// User did not fill everything properly

			/*TaxidUserSignup taxidUserSignup	 = (TaxidUserSignup)filledForm.get();
			taxidUserSignup.agreeToPolicy = false;*/
			return badRequest(signup_taxid.render(filledForm));
		} else {
			Logger.debug("IN the doSignup - no has errors");
			// Everything was filled
			// do something with your part of the form before handling the user
			// signup
			return TaxidAuthProvider.handleSignup(ctx());
		}
	}

	/**
	 * Forgot password.
	 * 
	 * @param taxid
	 *            the taxid
	 * @return the result
	 */
	public static Result forgotPassword(final Long taxid) {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		Form<TaxidUserIdentity> form = FORGOT_PASSWORD_FORM;
		if (taxid != null) {
			form = FORGOT_PASSWORD_FORM.fill(new TaxidUserIdentity(taxid));
		}
		return ok(password_forgot_taxid.render(form));
	}

	/**
	 * Login.
	 * 
	 * @return the result
	 */
	public static Result login() {
		return ok(login_taxid.render(TaxidAuthProvider.LOGIN_FORM));
	}

	/**
	 * Login from corp.
	 * 
	 * @return the result
	 */
	public static Result loginFromCorp() {
		return ok(login_taxid.render(TaxidAuthProvider.LOGIN_FORM));
	}

	/**
	 * Reset password.
	 * 
	 * @param token
	 *            the token
	 * @return the result
	 */
	public static Result resetPassword(final String token) {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final TokenAction ta = tokenIsValid(token, Type.PASSWORD_RESET);
		if (ta == null) {
			return badRequest(no_token_or_invalid.render());
		}

		return ok(password_reset_taxid.render(PASSWORD_RESET_FORM
				.fill(new PasswordReset(token))));
	}
	
	/**
	 * Signup.
	 * 
	 * @return the result
	 */
	public static Result signup() {
		// session().put("acctType", "event");
		return ok(signup_taxid.render(TaxidAuthProvider.SIGNUP_FORM));
	}

	/**
	 * Signup from corp.
	 * 
	 * @return the result
	 */
	public static Result signupFromCorp() {
		// session().put("acctType", "event");
		return ok(signup_taxid.render(TaxidAuthProvider.SIGNUP_FORM));
	}

	/**
	 * Verify.
	 * 
	 * @param token
	 *            the token
	 * @return the result
	 */
	public static Result verify(final String token) {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final TokenAction ta = tokenIsValid(token, Type.EMAIL_VERIFICATION);
		if (ta == null) {
			return badRequest(no_token_or_invalid.render());
		}
		// TODO - Remove this
		Logger.debug(ToStringBuilder.reflectionToString(ta));
		User.verify(ta.targetUser);
		// TODO - the following was email
		final Long taxid = ta.targetUser.taxid;
		flash(ControllerUtil.FLASH_INFO_KEY,
				Messages.get("playauthenticate.verify_email.success", taxid));
		if (ControllerUtil.getLocalUser(session()) != null) {
			return redirect(routes.Application.index());
		} else {
			return redirect(routes.TaxidSignup.login());
		}
	}

}
