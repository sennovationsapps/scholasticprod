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
import play.mvc.Controller;
import play.mvc.Result;
import providers.email.DefaultEmailAuthUser;
import providers.email.EmailAuthProvider;
import providers.email.EmailAuthProvider.EmailUserIdentity;
import providers.email.EmailAuthProvider.EmailUserLogin;
import providers.email.EmailAuthProvider.EmailUserSignup;
import providers.email.EmailAuthUser;
import views.html.login;
import views.html.signup;
import views.html.account.signup.exists;
import views.html.account.signup.no_token_or_invalid;
import views.html.account.signup.oAuthDenied;
import views.html.account.signup.password_forgot;
import views.html.account.signup.password_reset;
import views.html.account.signup.unverified;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;

// TODO: Auto-generated Javadoc
/**
 * The Class Signup.
 */
public class Signup extends Controller {

	/** The Constant FORGOT_PASSWORD_FORM. */
	private static final Form<EmailUserIdentity> FORGOT_PASSWORD_FORM = form(EmailUserIdentity.class);

	/** The Constant PASSWORD_RESET_FORM. */
	protected static final Form<PasswordReset> PASSWORD_RESET_FORM = form(PasswordReset.class);

	/**
	 * Do forgot password.
	 *
	 * @return the result
	 */
	public static Result doForgotPassword() {
		Logger.debug("Account doForgotPassword");
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<EmailUserIdentity> filledForm = FORGOT_PASSWORD_FORM
				.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not fill in his/her email
			return badRequest(password_forgot.render(filledForm));
		} else {
			// The email address given *BY AN UNKNWON PERSON* to the form - we
			// should find out if we actually have a user with this email
			// address and whether password login is enabled for him/her. Also
			// only send if the email address of the user has been verified.
			final String email = filledForm.get().email;

			final User user = User.findByEmail(email);
			if (user == null) {
				// We don't want to expose whether a given email address is signed
				// up, so just say an email has been sent, even though it might not
				// be true - that's protecting our user privacy.
				flash(ControllerUtil.FLASH_WARNING_KEY,
						"Your email address doesn't match our records. Please try again.");
			} else {
				// We don't want to expose whether a given email address is signed
				// up, so just say an email has been sent, even though it might not
				// be true - that's protecting our user privacy.
				flash(ControllerUtil.FLASH_INFO_KEY,
						Messages.get(
								"playauthenticate.reset_password.message.instructions_sent",
								email));

				// yep, we have a user with this email that is active - we do
				// not know if the user owning that account has requested this
				// reset, though.
				final EmailAuthProvider provider = EmailAuthProvider
						.getProvider();
				// User exists
				if (user.emailValidated) {
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
			}

			return redirect(routes.Signup.login());
		}
	}

	/**
	 * Do login.
	 *
	 * @return the result
	 */
	public static Result doLogin() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<EmailUserLogin> filledForm = EmailAuthProvider.LOGIN_FORM
				.bindFromRequest();
		Logger.debug("Signup doLogin - {}",
				ToStringBuilder.reflectionToString(filledForm));
		if (filledForm.hasErrors()) {
			flash(ControllerUtil.FLASH_DANGER_KEY,
					"Unable to login.  Please check the Login Form and try again.");
			// User did not fill everything properly
			return badRequest(login.render(filledForm));
		} else {
			// Everything was filled
			return EmailAuthProvider.handleLogin(ctx());
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
			return badRequest(password_reset.render(filledForm));
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
				u.resetPassword(new EmailAuthUser(newPassword), false);
			} catch (final RuntimeException re) {
				flash(ControllerUtil.FLASH_INFO_KEY,
						Messages.get("playauthenticate.reset_password.message.no_password_account"));
			}
			final boolean login = EmailAuthProvider.getProvider()
					.isLoginAfterPasswordReset();
			if (login) {
				// automatically log in
				flash(ControllerUtil.FLASH_INFO_KEY,
						Messages.get("playauthenticate.reset_password.message.success.auto_login"));

//				return PlayAuthenticate.loginAndRedirect(ctx(),
//						new EmailAuthUser(u.email));
				return redirect(routes.Signup.login());
			} else {
				// send the user to the login page
				flash(ControllerUtil.FLASH_INFO_KEY,
						Messages.get("playauthenticate.reset_password.message.success.manual_login"));


			}
			return redirect(routes.Signup.login());
		}
	}

	/**
	 * Do signup.
	 *
	 * @return the result
	 */
	public static Result doSignup() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<EmailUserSignup> filledForm = EmailAuthProvider.SIGNUP_FORM
				.bindFromRequest();
		if (StringUtils.isNotEmpty(filledForm.data().get("mail")) || StringUtils.isNotEmpty(filledForm.data().get("mobile"))) {
			filledForm.reject("Form submission has errors, please contact us if you are receiving this error.");
		}
		if (filledForm.hasErrors()) {
			// User did not fill everything properly
			return badRequest(signup.render(filledForm));
		} else {
			// Everything was filled
			// do something with your part of the form before handling the user
			// signup
			Logger.debug("About to handle signup in Application {}",
					ToStringBuilder.reflectionToString(filledForm));
			return EmailAuthProvider.handleSignup(ctx());
		}
	}

	/**
	 * Exists.
	 *
	 * @return the result
	 */
	public static Result exists() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		return ok(exists.render());
	}

	/**
	 * Forgot password.
	 *
	 * @param email
	 *            the email
	 * @return the result
	 */
	public static Result forgotPassword(final String email) {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		Form<EmailUserIdentity> form = FORGOT_PASSWORD_FORM;
		if ((email != null) && !email.trim().isEmpty()) {
			form = FORGOT_PASSWORD_FORM.fill(new EmailUserIdentity(email));
		}
		return ok(password_forgot.render(form));
	}

	/**
	 * Login.
	 *
	 * @return the result
	 */
	public static Result login() {
		return ok(login.render(EmailAuthProvider.LOGIN_FORM));
	}

	/**
	 * Login from corp.
	 *
	 * @return the result
	 */
	public static Result loginFromCorp() {
		return ok(login.render(EmailAuthProvider.LOGIN_FORM));
	}

	/**
	 * O auth denied.
	 *
	 * @param getProviderKey
	 *            the get provider key
	 * @return the result
	 */
	public static Result oAuthDenied(final String getProviderKey) {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		return ok(oAuthDenied.render(getProviderKey));
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

		return ok(password_reset.render(PASSWORD_RESET_FORM
				.fill(new PasswordReset(token))));
	}

	/**
	 * Signup.
	 *
	 * @return the result
	 */
	public static Result signup() {
		// session().put("acctType", "pfp");
		return ok(signup.render(EmailAuthProvider.SIGNUP_FORM));
	}

	/**
	 * Signup from corp.
	 *
	 * @return the result
	 */
	public static Result signupFromCorp() {
		// session().put("acctType", "pfp");
		return ok(signup.render(EmailAuthProvider.SIGNUP_FORM));
	}

	/**
	 * Unverified.
	 *
	 * @return the result
	 */
	public static Result unverified() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		return ok(unverified.render());
	}

	/**
	 * Verify.
	 *
	 * @param token
	 *            the token
	 * @return the result
	 */
	public static Result verify(final String token) {
		Logger.debug("Account verify");
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final TokenAction ta = tokenIsValid(token, Type.EMAIL_VERIFICATION);
		if (ta == null) {
			return badRequest(no_token_or_invalid.render());
		}
		final String email = ta.targetUser.email;
//		final User verifiedUser = ta.targetUser;
		// if(session().containsKey("acctType") && StringUtils.equals("event",
		// session().get("acctType"))) {
		// verifiedUser.addRoles(SecurityRole.EVENT_ADMIN);
		// } else {
		// verifiedUser.addRoles(SecurityRole.PFP_ADMIN);
		// }
		User.verify(ta.targetUser);
		flash(ControllerUtil.FLASH_INFO_KEY,
				Messages.get("playauthenticate.verify_email.success", email));
		if (ControllerUtil.getLocalUser(session()) != null) {
			return redirect(routes.Application.index());
		} else {
			return redirect(routes.Signup.login());
		}
	}

	/**
	 * Returns a token object if valid, null if not.
	 *
	 * @param token
	 *            the token
	 * @param type
	 *            the type
	 * @return the token action
	 */
	protected static TokenAction tokenIsValid(final String token,
			final Type type) {
		TokenAction ret = null;
		if ((token != null) && !token.trim().isEmpty()) {
			final TokenAction ta = TokenAction.findByToken(token, type);
			if ((ta != null) && ta.isValid()) {
				ret = ta;
			}
		}

		return ret;
	}

	/**
	 * The Class PasswordReset.
	 */
	public static class PasswordReset extends Account.PasswordChange {

		/** The token. */
		public String token;

		/**
		 * Instantiates a new password reset.
		 */
		public PasswordReset() {
		}

		/**
		 * Instantiates a new password reset.
		 *
		 * @param token
		 *            the token
		 */
		public PasswordReset(final String token) {
			this.token = token;
		}

		/**
		 * Gets the token.
		 *
		 * @return the token
		 */
		public String getToken() {
			return token;
		}

		/**
		 * Sets the token.
		 *
		 * @param token
		 *            the new token
		 */
		public void setToken(String token) {
			this.token = token;
		}
	}

}
