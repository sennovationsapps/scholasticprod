package providers.email;

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
import play.Application;
import play.Logger;
import play.data.Form;
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

import com.feth.play.module.mail.Mailer.Mail.Body;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;

import controllers.routes;

public class EmailAuthProvider
		extends
		UsernamePasswordAuthProvider<String, DefaultEmailAuthUser, EmailAuthUser, EmailAuthProvider.EmailUserLogin, EmailAuthProvider.EmailUserSignup> {

	public static final Form<EmailUserLogin> LOGIN_FORM = form(EmailUserLogin.class);
	public static final Form<EmailUserSignup> SIGNUP_FORM = form(EmailUserSignup.class);
	private static final String EMAIL_TEMPLATE_FALLBACK_LANGUAGE = "en";

	private static final String SETTING_KEY_LINK_LOGIN_AFTER_PASSWORD_RESET = "loginAfterPasswordReset";

	private static final String SETTING_KEY_PASSWORD_RESET_LINK_SECURE = SETTING_KEY_MAIL
			+ "." + "passwordResetLink.secure";

	private static final String SETTING_KEY_VERIFICATION_LINK_SECURE = SETTING_KEY_MAIL
			+ "." + "verificationLink.secure";

	public EmailAuthProvider(Application app) {
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
	protected DefaultEmailAuthUser buildLoginAuthUser(
			final EmailUserLogin login, final Context ctx) {
		return new DefaultEmailAuthUser(login.getPassword(), login.getEmail());
	}

	@Override
	protected EmailAuthUser buildSignupAuthUser(final EmailUserSignup signup,
			final Context ctx) {
		return new EmailAuthUser(signup);
	}

	protected String generatePasswordResetRecord(final User u) {
		final String token = generateToken();
		TokenAction.create(Type.PASSWORD_RESET, token, u);
		return token;
	}

	@Override
	protected String generateVerificationRecord(final EmailAuthUser user) {
		return generateVerificationRecord(User.findByAuthUserIdentity(user));
	}

	protected String generateVerificationRecord(final User user) {
		final String token = generateToken();
		// Do database actions, etc.
		TokenAction.create(Type.EMAIL_VERIFICATION, token, user);
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
	protected Form<EmailUserLogin> getLoginForm() {
		return LOGIN_FORM;
	}

	protected Body getPasswordResetMailingBody(final String token,
			final User user, final Context ctx) {

		final boolean isSecure = getConfiguration().getBoolean(
				SETTING_KEY_PASSWORD_RESET_LINK_SECURE);
		final String url = routes.Signup.resetPassword(token).absoluteURL(
				ctx.request(), isSecure);

		final Lang lang = Lang.preferred(ctx.request().acceptLanguages());
		final String langCode = lang.code();

		final String html = getEmailTemplate(
				"views.html.account.email.password_reset", langCode, url,
				token, user.firstName + " " + user.lastName, user.email);
		final String text = getEmailTemplate(
				"views.txt.account.email.password_reset", langCode, url, token,
				user.firstName + " " + user.lastName, user.email);

		return new Body(text, html);
	}

	protected String getPasswordResetMailingSubject(final User user,
			final Context ctx) {
		return Messages.get("playauthenticate.password.reset_email.subject");
	}

	@Override
	protected Form<EmailUserSignup> getSignupForm() {
		return SIGNUP_FORM;
	}

	@Override
	protected Body getVerifyEmailMailingBody(final String token,
			final EmailAuthUser user, final Context ctx) {

		final boolean isSecure = getConfiguration().getBoolean(
				SETTING_KEY_VERIFICATION_LINK_SECURE);
		final String url = routes.Signup.verify(token).absoluteURL(
				ctx.request(), isSecure);

		final Lang lang = Lang.preferred(ctx.request().acceptLanguages());
		final String langCode = lang.code();

		final String html = getEmailTemplate(
				"views.html.account.signup.email.verify_email", langCode, url,
				token, user.getName(), user.getEmail());
		final String text = getEmailTemplate(
				"views.txt.account.signup.email.verify_email", langCode, url,
				token, user.getName(), user.getEmail());

		return new Body(text, html);
	}

	protected Body getVerifyEmailMailingBodyAfterSignup(final String token,
			final User user, final Context ctx) {

		final boolean isSecure = getConfiguration().getBoolean(
				SETTING_KEY_VERIFICATION_LINK_SECURE);
		final String url = routes.Signup.verify(token).absoluteURL(
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
	protected String getVerifyEmailMailingSubject(final EmailAuthUser user,
			final Context ctx) {
		return Messages.get("playauthenticate.password.verify_signup.subject");
	}

	protected String getVerifyEmailMailingSubjectAfterSignup(final User user,
			final Context ctx) {
		return Messages.get("playauthenticate.password.verify_email.subject");
	}

	@Override
	protected com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider.LoginResult loginUser(
			final DefaultEmailAuthUser authUser) {
		Logger.debug("What is this class - {}", authUser.getClass());
		final User u = User.findByUsernamePasswordIdentity(authUser);
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
		return super.onLoginUserNotFound(context);
	}

	@Override
	protected com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider.SignupResult signupUser(
			final EmailAuthUser user) {
		final User u = User.findByUsernamePasswordIdentity(user);
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
		Logger.debug("EmailAuthProvider save user - ");
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
	protected DefaultEmailAuthUser transformAuthUser(
			final EmailAuthUser authUser, final Context context) {
		return new DefaultEmailAuthUser(authUser.getEmail());
	}

	@Override
	protected Call userExists(final UsernamePasswordAuthUser authUser) {
		return routes.Signup.exists();
	}

	@Override
	protected Call userUnverified(final UsernamePasswordAuthUser authUser) {
		return routes.Signup.unverified();
	}

	public static EmailAuthProvider getProvider() {
		return (EmailAuthProvider) PlayAuthenticate
				.getProvider(UsernamePasswordAuthProvider.PROVIDER_KEY);
	}

	private static String generateToken() {
		return UUID.randomUUID().toString();
	}

	public static class EmailUserIdentity {

		@Required
		@Email
		public String email;

		public EmailUserIdentity() {
		}

		public EmailUserIdentity(final String email) {
			this.email = email;
		}

	}

	public static class EmailUserLogin extends EmailUserIdentity
			implements
			com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider.UsernamePassword {

		@Required
		@MinLength(5)
		public String password;

		@Override
		public String getEmail() {
			return email;
		}

		@Override
		public String getPassword() {
			return password;
		}
	}


	public static class EmailUserSignup extends EmailUserLogin {

		@Required
		public boolean agreeToPolicy;

		@Required
		public String firstName;

		@Required
		public String lastName;

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
		@Required
		@MinLength(5)
		public String repeatPassword;

		@Pattern(value = "(\\d{5}([\\-]\\d{4})?)", message = "zip.pattern")
		public String zip;

		public List<ValidationError> validate() {
			List<ValidationError> errors = new ArrayList<ValidationError>();
			if ((password == null) || !password.equals(repeatPassword)) {
				errors.add(new ValidationError("password", "playauthenticate.password.signup.error.passwords_not_same"));
			}
			if (agreeToPolicy == false) {
				errors.add(new ValidationError("agreeToPolicy", "You must agree to the Terms of Service to create an account."));
			}
			return errors.isEmpty() ? null : errors;
		}
	}



	/*public static class EmailUserSignup extends EmailUserLogin {

		@Required
		public boolean agreeToPolicy;

		@Required
		public String firstName;

		@Required
		public String lastName;

		@Required
		@MaxLength(value = 12, message = "The phone number cannot be longer than 12 digits including - or .")
		public String phone;

		@Required
		@MinLength(5)
		public String repeatPassword;

		@Pattern(value = "(\\d{5}([\\-]\\d{4})?)", message = "zip.pattern")
		public String zip;

		public List<ValidationError> validate() {
		    List<ValidationError> errors = new ArrayList<ValidationError>();
		    if ((password == null) || !password.equals(repeatPassword)) {
		    	errors.add(new ValidationError("password", "playauthenticate.password.signup.error.passwords_not_same"));
			}
		    if (agreeToPolicy == false) {
		    	errors.add(new ValidationError("agreeToPolicy", "You must agree to the Terms of Service to create an account."));
			}
		    return errors.isEmpty() ? null : errors;
		}
	}*/
}
