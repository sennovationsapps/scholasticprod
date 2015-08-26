package controllers;

import static play.data.Form.form;
import models.security.User;
import play.Logger;
import play.data.Form;
import play.data.format.Formats.NonEmpty;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import providers.email.EmailAuthProvider;
import providers.email.EmailAuthUser;
import views.html.account.ask_link;
import views.html.account.ask_merge;
import views.html.account.link;
import views.html.account.password_change;
import views.html.account.unverified;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;

// TODO: Auto-generated Javadoc
/**
 * The Class Account.
 */
public class Account extends Controller {

	/** The Constant ACCEPT_FORM. */
	protected static final Form<Accept> ACCEPT_FORM = form(Accept.class);

	/** The Constant PASSWORD_CHANGE_FORM. */
	protected static final Form<Account.PasswordChange> PASSWORD_CHANGE_FORM = form(Account.PasswordChange.class);

	/**
	 * Ask link.
	 * 
	 * @return the result
	 */
	@SubjectPresent
	public static Result askLink() {
		Logger.debug("Account askLink");
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final AuthUser u = PlayAuthenticate.getLinkUser(session());
		if (u == null) {
			// account to link could not be found, silently redirect to login
			return redirect(routes.Application.index());
		}
		return ok(ask_link.render(ACCEPT_FORM, u));
	}

	/**
	 * Ask merge.
	 * 
	 * @return the result
	 */
	@SubjectPresent
	public static Result askMerge() {
		Logger.debug("Account askMerge");
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		// this is the currently logged in user
		final AuthUser aUser = PlayAuthenticate.getUser(session());

		// this is the user that was selected for a login
		final AuthUser bUser = PlayAuthenticate.getMergeUser(session());
		if (bUser == null) {
			// user to merge with could not be found, silently redirect to login
			return redirect(routes.Application.index());
		}

		// You could also get the local user object here via
		// User.findByAuthUserIdentity(newUser)
		return ok(ask_merge.render(ACCEPT_FORM, aUser, bUser));
	}

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
			return ok(password_change.render(PASSWORD_CHANGE_FORM));
		}
	}

	/**
	 * Do change password.
	 * 
	 * @return the result
	 */
	@SubjectPresent
	public static Result doChangePassword() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<Account.PasswordChange> filledForm = PASSWORD_CHANGE_FORM
				.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not select whether to link or not link
			return badRequest(password_change.render(filledForm));
		} else {
			final User user = ControllerUtil.getLocalUser(session());
			final String newPassword = filledForm.get().password;
			user.changePassword(new EmailAuthUser(newPassword), true);
			flash(ControllerUtil.FLASH_INFO_KEY,
					Messages.get("playauthenticate.change_password.success"));
			return redirect(routes.Application.profile());
		}
	}

	/**
	 * Do link.
	 * 
	 * @return the result
	 */
	@SubjectPresent
	public static Result doLink() {
		Logger.debug("Account doLink");
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final AuthUser u = PlayAuthenticate.getLinkUser(session());
		if (u == null) {
			// account to link could not be found, silently redirect to login
			return redirect(routes.Application.index());
		}

		final Form<Accept> filledForm = ACCEPT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not select whether to link or not link
			return badRequest(ask_link.render(filledForm, u));
		} else {
			// User made a choice :)
			final boolean link = filledForm.get().accept;
			if (link) {
				flash(ControllerUtil.FLASH_INFO_KEY,
						Messages.get("playauthenticate.accounts.link.success"));
			}
			return PlayAuthenticate.link(ctx(), link);
		}
	}

	/**
	 * Do merge.
	 * 
	 * @return the result
	 */
	@SubjectPresent
	public static Result doMerge() {
		Logger.debug("Account doMerge");
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		// this is the currently logged in user
		final AuthUser aUser = PlayAuthenticate.getUser(session());

		// this is the user that was selected for a login
		final AuthUser bUser = PlayAuthenticate.getMergeUser(session());
		if (bUser == null) {
			// user to merge with could not be found, silently redirect to login
			return redirect(routes.Application.index());
		}

		final Form<Accept> filledForm = ACCEPT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not select whether to merge or not merge
			return badRequest(ask_merge.render(filledForm, aUser, bUser));
		} else {
			// User made a choice :)
			final boolean merge = filledForm.get().accept;
			if (merge) {
				flash(ControllerUtil.FLASH_INFO_KEY,
						Messages.get("playauthenticate.accounts.merge.success"));
			}
			return PlayAuthenticate.merge(ctx(), merge);
		}
	}

	/**
	 * Link.
	 * 
	 * @return the result
	 */
	@SubjectPresent
	public static Result link() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		return ok(link.render());
	}

	/**
	 * Verify email.
	 * 
	 * @return the result
	 */
	@SubjectPresent
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
			EmailAuthProvider.getProvider().sendVerifyEmailMailingAfterSignup(
					user, ctx());
		} else {
			flash(ControllerUtil.FLASH_INFO_KEY, Messages.get(
					"playauthenticate.verify_email.error.set_email_first",
					user.email));
		}
		return redirect(routes.Application.profile());
	}

	/**
	 * Verify email.
	 * 
	 * @return the result
	 */
	@SubjectPresent
	public static Result verifyEmailById(Long userId) {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final User user = User.findById(userId);
		if (user.emailValidated) {
			// E-Mail has been validated already
			flash(ControllerUtil.FLASH_INFO_KEY,
					Messages.get("playauthenticate.verify_email.error.already_validated"));
		} else if ((user.email != null) && !user.email.trim().isEmpty()) {
			flash(ControllerUtil.FLASH_INFO_KEY, Messages.get(
					"playauthenticate.verify_email.message.instructions_sent",
					user.email));
			EmailAuthProvider.getProvider().sendVerifyEmailMailingAfterSignup(
					user, ctx());
		} else {
			flash(ControllerUtil.FLASH_INFO_KEY, Messages.get(
					"playauthenticate.verify_email.error.set_email_first",
					user.email));
		}
		return redirect(routes.Application.profile());
	}
	
	/**
	 * The Class Accept.
	 */
	public static class Accept {

		/** The accept. */
		@Required
		@NonEmpty
		public Boolean accept;

		/**
		 * Gets the accept.
		 * 
		 * @return the accept
		 */
		public Boolean getAccept() {
			return accept;
		}

		/**
		 * Sets the accept.
		 * 
		 * @param accept
		 *            the new accept
		 */
		public void setAccept(Boolean accept) {
			this.accept = accept;
		}

	}

	/**
	 * The Class PasswordChange.
	 */
	public static class PasswordChange {

		/** The password. */
		@MinLength(5)
		@Required
		public String password;

		/** The repeat password. */
		@MinLength(5)
		@Required
		public String repeatPassword;

		/**
		 * Gets the password.
		 * 
		 * @return the password
		 */
		public String getPassword() {
			return password;
		}

		/**
		 * Gets the repeat password.
		 * 
		 * @return the repeat password
		 */
		public String getRepeatPassword() {
			return repeatPassword;
		}

		/**
		 * Sets the password.
		 * 
		 * @param password
		 *            the new password
		 */
		public void setPassword(String password) {
			this.password = password;
		}

		/**
		 * Sets the repeat password.
		 * 
		 * @param repeatPassword
		 *            the new repeat password
		 */
		public void setRepeatPassword(String repeatPassword) {
			this.repeatPassword = repeatPassword;
		}

		/**
		 * Validate.
		 * 
		 * @return the string
		 */
		public String validate() {
			if ((password == null) || !password.equals(repeatPassword)) {
				return Messages
						.get("playauthenticate.change_password.error.passwords_not_same");
			}
			return null;
		}
	}

}