package providers.taxid;

import static play.data.Form.form;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import models.security.LinkedAccount;
import models.security.TokenAction;
import models.security.TokenAction.Type;
import models.security.User;

import org.apache.commons.lang.builder.ToStringBuilder;

import play.Application;
import play.Logger;
import play.data.Form;
import play.data.validation.Constraints;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import play.data.validation.ValidationError;
import play.i18n.Lang;
import play.i18n.Messages;
import play.mvc.Call;
import play.mvc.Http.Context;
import providers.uniqueid.UniqueIdAuthProvider;
import providers.uniqueid.UniqueIdAuthUser;

import com.feth.play.module.mail.Mailer.Mail.Body;
import com.feth.play.module.pa.PlayAuthenticate;

import controllers.routes;

public class TaxidAuthProvider
		extends
		UniqueIdAuthProvider<String, DefaultTaxidAuthUser, TaxidAuthUser, TaxidAuthProvider.TaxidUserLogin, TaxidAuthProvider.TaxidUserSignup> {

	public static final Form<TaxidUserLogin> LOGIN_FORM = form(TaxidUserLogin.class);
	public static final Form<TaxidUserSignup> SIGNUP_FORM = form(TaxidUserSignup.class);
	private static final String EMAIL_TEMPLATE_FALLBACK_LANGUAGE = "en";

	private static final String SETTING_KEY_LINK_LOGIN_AFTER_PASSWORD_RESET = "loginAfterPasswordReset";

	private static final String SETTING_KEY_PASSWORD_RESET_LINK_SECURE = SETTING_KEY_MAIL
			+ "." + "passwordResetLink.secure";

	private static final String SETTING_KEY_VERIFICATION_LINK_SECURE = SETTING_KEY_MAIL
			+ "." + "verificationLink.secure";

	public TaxidAuthProvider(Application app) {
		super(app);
	}

	public boolean isLoginAfterPasswordReset() {
		return getConfiguration().getBoolean(
				SETTING_KEY_LINK_LOGIN_AFTER_PASSWORD_RESET);
	}

	public void sendPasswordResetMailing(final User user, final Context ctx) {
		final String token = generatePasswordResetRecord(user);
		final String subject = getPasswordResetMailingSubject(user, ctx);
		final Body body = getPasswordResetMailingBody(token, user, ctx);
		sendMail(subject, body, getEmailName(user));
	}

	public void sendVerifyEmailMailingAfterSignup(final User user,
			final Context ctx) {

		final String subject = getVerifyEmailMailingSubjectAfterSignup(user,
				ctx);
		final String token = generateVerificationRecord(user);
		final Body body = getVerifyEmailMailingBodyAfterSignup(token, user, ctx);
		sendMail(subject, body, getEmailName(user));
	}

	private String getEmailName(final User user) {
		return getEmailName(user.email, user.firstName + " " + user.lastName);
	}

	@Override
	protected DefaultTaxidAuthUser buildLoginAuthUser(
			final TaxidUserLogin login, final Context ctx) {
		return new DefaultTaxidAuthUser(login.getPassword(), login.getEmail(),
				login.getUniqueId());
	}

	@Override
	protected TaxidAuthUser buildSignupAuthUser(final TaxidUserSignup signup,
			final Context ctx) {
		return new TaxidAuthUser(signup);
	}

	protected String generatePasswordResetRecord(final User u) {
		final String token = generateToken();
		TokenAction.create(Type.PASSWORD_RESET, token, u);
		return token;
	}

	@Override
	protected String generateVerificationRecord(final TaxidAuthUser user) {
		Logger.debug("generateVerificationRecord - Taxid {}",
				ToStringBuilder.reflectionToString(user));
		return generateVerificationRecord(User.findByAuthUserIdentity(user));
	}

	protected String generateVerificationRecord(final User user) {
		final String token = generateToken();
		// Do database actions, etc.
		// TODO Uncomment
		final TokenAction action = TokenAction.create(Type.EMAIL_VERIFICATION,
				token, user);
		Logger.debug("generateVerificationRecord - {}",
				ToStringBuilder.reflectionToString(action));
		return token;
	}

	protected String getEmailTemplate(final String template,
			final String langCode, final String url, final String token,
			final String name, final String email) {
		Class<?> cls = null;
		String ret = null;
		try {
			cls = Class.forName(template + "_" + langCode);
		} catch (final ClassNotFoundException e) {
			Logger.warn("Template: '"
					+ template
					+ "_"
					+ langCode
					+ "' was not found! Trying to use English fallback template instead.");
		}
		if (cls == null) {
			try {
				cls = Class.forName(template + "_"
						+ EMAIL_TEMPLATE_FALLBACK_LANGUAGE);
			} catch (final ClassNotFoundException e) {
				Logger.error("Fallback template: '" + template + "_"
						+ EMAIL_TEMPLATE_FALLBACK_LANGUAGE
						+ "' was not found either!");
			}
		}
		if (cls != null) {
			Method htmlRender = null;
			try {
				htmlRender = cls.getMethod("render", String.class,
						String.class, String.class, String.class);
				ret = htmlRender.invoke(null, url, token, name, email)
						.toString();

			} catch (final NoSuchMethodException e) {
				e.printStackTrace();
			} catch (final IllegalAccessException e) {
				e.printStackTrace();
			} catch (final InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	@Override
	protected Form<TaxidUserLogin> getLoginForm() {
		return LOGIN_FORM;
	}

	protected Body getPasswordResetMailingBody(final String token,
			final User user, final Context ctx) {

		final boolean isSecure = getConfiguration().getBoolean(
				SETTING_KEY_PASSWORD_RESET_LINK_SECURE);
		final String url = routes.TaxidSignup.resetPassword(token).absoluteURL(
				ctx.request(), isSecure);

		final Lang lang = Lang.preferred(ctx.request().acceptLanguages());
		final String langCode = lang.code();

		final String html = getEmailTemplate(
				"views.html.account.email.password_reset", langCode, url,
				token, user.taxid.toString(), user.email);
		final String text = getEmailTemplate(
				"views.txt.account.email.password_reset", langCode, url, token,
				user.taxid.toString(), user.email);

		return new Body(text, html);
	}

	protected String getPasswordResetMailingSubject(final User user,
			final Context ctx) {
		return Messages.get("playauthenticate.password.reset_email.subject");
	}

	@Override
	protected Form<TaxidUserSignup> getSignupForm() {
		return SIGNUP_FORM;
	}

	@Override
	protected Body getVerifyEmailMailingBody(final String token,
			final TaxidAuthUser user, final Context ctx) {

		final boolean isSecure = getConfiguration().getBoolean(
				SETTING_KEY_VERIFICATION_LINK_SECURE);
		final String url = routes.TaxidSignup.verify(token).absoluteURL(
				ctx.request(), isSecure);

		final Lang lang = Lang.preferred(ctx.request().acceptLanguages());
		final String langCode = lang.code();

		final String html = getEmailTemplate(
				"views.html.account.signup.email.verify_email", langCode, url,
				token, user.getUniqueId().toString(), user.getEmail());
		final String text = getEmailTemplate(
				"views.txt.account.signup.email.verify_email", langCode, url,
				token, user.getUniqueId().toString(), user.getEmail());

		return new Body(text, html);
	}

	protected Body getVerifyEmailMailingBodyAfterSignup(final String token,
			final User user, final Context ctx) {

		final boolean isSecure = getConfiguration().getBoolean(
				SETTING_KEY_VERIFICATION_LINK_SECURE);
		final String url = routes.TaxidSignup.verify(token).absoluteURL(
				ctx.request(), isSecure);

		final Lang lang = Lang.preferred(ctx.request().acceptLanguages());
		final String langCode = lang.code();

		final String html = getEmailTemplate(
				"views.html.account.email.verify_email", langCode, url, token,
				user.firstName + " " + user.lastName, user.email);
		final String text = getEmailTemplate(
				"views.txt.account.email.verify_email", langCode, url, token,
				user.firstName + " " + user.lastName, user.email);

		return new Body(text, html);
	}

	@Override
	protected String getVerifyEmailMailingSubject(final TaxidAuthUser user,
			final Context ctx) {
		return Messages.get("playauthenticate.password.verify_signup.subject");
	}

	protected String getVerifyEmailMailingSubjectAfterSignup(final User user,
			final Context ctx) {
		return Messages.get("playauthenticate.password.verify_email.subject");
	}

	@Override
	protected providers.uniqueid.UniqueIdAuthProvider.LoginResult loginUser(
			final DefaultTaxidAuthUser authUser) {
		final User u = User.findByTaxidIdentity(authUser);
		if (u == null) {
			return LoginResult.NOT_FOUND;
		} else {
			if (!u.emailValidated) {
				return LoginResult.USER_UNVERIFIED;
			} else {
				for (final LinkedAccount acc : u.linkedAccounts) {
					if (getKey().equals(acc.providerKey)) {
						if (authUser.checkPassword(acc.providerUserId,
								authUser.getPassword())) {
							// Password was correct
							return LoginResult.USER_LOGGED_IN;
						} else {
							// if you don't return here,
							// you would allow the user to have
							// multiple passwords defined
							// usually we don't want this
							return LoginResult.WRONG_PASSWORD;
						}
					}
				}
				return LoginResult.WRONG_PASSWORD;
			}
		}
	}

	@Override
	protected List<String> neededSettingKeys() {
		final List<String> needed = new ArrayList<String>(
				super.neededSettingKeys());
		needed.add(SETTING_KEY_VERIFICATION_LINK_SECURE);
		needed.add(SETTING_KEY_PASSWORD_RESET_LINK_SECURE);
		needed.add(SETTING_KEY_LINK_LOGIN_AFTER_PASSWORD_RESET);
		return needed;
	}

	@Override
	protected String onLoginUserNotFound(final Context context) {
		context.flash()
				.put(controllers.ControllerUtil.FLASH_DANGER_KEY,
						Messages.get("playauthenticate.password.login.unknown_user_or_pw"));
		return routes.TaxidSignup.login().url();
		// return super.onLoginUserNotFound(context);
	}

	@Override
	protected providers.uniqueid.UniqueIdAuthProvider.SignupResult signupUser(
			final TaxidAuthUser user) {
		final User u = User.findByTaxidIdentity(user);
		if (u != null) {
			if (u.emailValidated) {
				// This user exists, has its email validated and is active
				return SignupResult.USER_EXISTS;
			} else {
				// this user exists, is active but has not yet validated its
				// email
				return SignupResult.USER_EXISTS_UNVERIFIED;
			}
		}
		Logger.debug("UniqueIdAuthProvider save user - ");
		// The user either does not exist or is inactive - create a new one
		@SuppressWarnings("unused")
		final User newUser = User.create(user);
		// Usually the email should be verified before allowing login, however
		// if you return
		// return SignupResult.USER_CREATED;
		// then the user gets logged in directly
		if (newUser.emailValidated) {
			// This user exists, has its email validated and is active
			return SignupResult.USER_CREATED;
		} else {
			// this user exists, is active but has not yet validated its
			// email
			return SignupResult.USER_CREATED_UNVERIFIED;
		}
	}

	@Override
	protected DefaultTaxidAuthUser transformAuthUser(
			final TaxidAuthUser authUser, final Context context) {
		return new DefaultTaxidAuthUser(authUser.getEmail(), authUser.getOrgTaxid());
	}

	@Override
	protected Call userExists(final UniqueIdAuthUser authUser) {
		return routes.Signup.exists();
	}

	@Override
	protected Call userUnverified(final UniqueIdAuthUser authUser) {
		return routes.Signup.unverified();
	}

	public static TaxidAuthProvider getProvider() {
		return (TaxidAuthProvider) PlayAuthenticate
				.getProvider(UniqueIdAuthProvider.PROVIDER_KEY);
	}

	private static String generateToken() {
		return UUID.randomUUID().toString();
	}

	public static class TaxidUserIdentity {

		@Required
		public Long uniqueId;

		public TaxidUserIdentity() {
		}

		public TaxidUserIdentity(final Long uniqueId) {
			this.uniqueId = uniqueId;
		}

	}

	public static class TaxidUserLogin extends TaxidUserIdentity implements
			providers.uniqueid.UniqueIdAuthProvider.UniqueId {

		@Email
		public String email;

		@Required
		@MinLength(5)
		public String password;

		public String getEmail() {
			return email;
		}

		@Override
		public String getPassword() {
			return password;
		}

		@Override
		public Long getUniqueId() {
			return uniqueId;
		}
	}

	public static class TaxidUserSignup extends TaxidUserLogin {



/*
      @Required
	  @MaxLength(value = 9)
	  @Pattern(value = "[0-9.+]+", message = "Enter only the 9 digit Tax ID number, no symbols or characters")
      public String orgTaxIdNo;
*/

		@Required
		public Boolean agreeToPolicy;
/*

		@Required
		public Boolean agreeToRepresent;
*/

		// @Required
		@Constraints.Pattern(value = "(\\d{5}([\\-]\\d{4})?)", message = "zip.pattern")
		public String zip;

		@Required
		public String firstName;

		@Required
		public String lastName;

		@Required
		public String orgAddress;
		
		@Required
		@Constraints.MaxLength(value = 50)
		public String orgCity;

		@Required
		public String orgName;

		/*@Required
		@MaxLength(value = 12, message = "The phone number cannot be longer than 12 digits including - or .")*/
	/*	@Required

		@Pattern(value = "^[0-9]{3}-[0-9]{3}-[0-9]{4}$", message = "phone.pattern")
		@MaxLength(value = 12)
		public String orgPhone;*/


		//======start====the pattern of phone no added like us phone no=======12.08.2015==================//
		@Required
		/*@Pattern(value = "^[0-9]$", message = "phone.pattern")*/
		@MaxLength(value = 3)
		@Pattern(value = "[0-9.+]+", message = "A valid phone number is required")
		public String orgPhPart1;




		@Required
		/*@Pattern(value = "^[0-9]$", message = "phone.pattern")*/
		@MaxLength(value = 3)
		@Pattern(value = "[0-9.+]+", message = "A valid phone number is required")
		public String orgPhPart2;




		@Required
		/*@Pattern(value = "^[0-9]$", message = "phone.pattern")*/
		@Pattern(value = "[0-9.+]+", message = "A valid phone number is required")
		@MaxLength(value = 4)
		public String orgPhPart3;


		@Required
		/*@Pattern(value = "^[0-9]$", message = "phone.pattern")*/
		@MaxLength(value = 10)
		public String orgPhone;



		//======end====the pattern of phone no added like us phone no=======12.08.2015==================//


		@Required
		@Constraints.Pattern(value = "^[a-zA-Z\\s]*$", message = "state.pattern")
		public String orgState;

		@Required
		@Constraints.Pattern(value = "(\\d{5}([\\-]\\d{4})?)", message = "zip.pattern")
		public String orgZip;

		/*@Required
		@MaxLength(value = 12, message = "The phone number cannot be longer than 12 digits including - or .")
		public String phone;
*/


		//======start====the pattern of phone no added like us phone no=======24.08.2015==================//
		@Required
		/*@Pattern(value = "^[0-9]$", message = "phone.pattern")*/
		@MaxLength(value = 3)
		@Pattern(value = "[0-9.+]+", message = "A valid phone number is required")
		public String phPart1;




		@Required
		/*@Pattern(value = "^[0-9]$", message = "phone.pattern")*/
		@MaxLength(value = 3)
		@Pattern(value = "[0-9.+]+", message = "A valid phone number is required")
		public String phPart2;




		@Required
		/*@Pattern(value = "^[0-9]$", message = "phone.pattern")*/
		@Pattern(value = "[0-9.+]+", message = "A valid phone number is required")
		@MaxLength(value = 4)
		public String phPart3;


		@Required
		/*@Pattern(value = "^[0-9]$", message = "phone.pattern")*/
		@MaxLength(value = 10)
		public String phone;
		//======end====the pattern of phone no added like us phone no=======24.08.2015==================//



		//======start====the pattern of phone no added like us phone no=======12.08.2015==================//
		/*@Required

		@Pattern(value = "^[0-9]{3}-[0-9]{3}-[0-9]{4}$", message = "phone.pattern")
		@MaxLength(value = 12)
		public String phone;*/
		//======end====the pattern of phone no added like us phone no=======12.08.2015==================//
		@Required
		@MinLength(5)
		public String repeatPassword;

		
		public List<ValidationError> validate() {
		    List<ValidationError> errors = new ArrayList<ValidationError>();
		    if ((password == null) || !password.equals(repeatPassword)) {
		    	errors.add(new ValidationError("password", "playauthenticate.password.signup.error.passwords_not_same"));
			}
		    if (agreeToPolicy == false) {
		    	errors.add(new ValidationError("agreeToPolicy", "You must agree to the Terms of Service to create an account."));
			}
			/*if (agreeToRepresent == false) {
				errors.add(new ValidationError("agreeToRepresent", "You must agree to have the right to enter into a contract with Scholastic Challenge on behalf of the above listed organization."));
			}*/
		    return errors.isEmpty() ? null : errors;
		}

	}

}
