package providers.email;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.mindrot.jbcrypt.BCrypt;

import play.Logger;
import providers.email.EmailAuthProvider.EmailUserSignup;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.FirstLastNameIdentity;

public class EmailAuthUser extends UsernamePasswordAuthUser implements
		FirstLastNameIdentity, ContactIdentity, TermsOfServiceIdentity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Boolean agreeToPolicy;
	private final String firstName;
	private final String lastName;
	private final String phone;
	private final String zip;

	public EmailAuthUser(final EmailUserSignup signup) {
		super(signup.password, signup.email);
		this.firstName = signup.firstName;
		this.lastName = signup.lastName;
		this.zip = signup.zip;
		this.phone = signup.phone;
		this.agreeToPolicy = signup.agreeToPolicy;
	}

	/**
	 * Used for password reset only - do not use this to signup a user!
	 * 
	 * @param password
	 */
	public EmailAuthUser(final String password) {
		this(password, null, null, null, null, null, true);
	}

	public EmailAuthUser(String clearPassword, String email) {
		this(clearPassword, email, null, null, null, null, true);
	}

	public EmailAuthUser(String clearPassword, String email, String firstName,
			String lastName) {
		this(clearPassword, email, firstName, lastName, null, null, true);
	}

	public EmailAuthUser(String clearPassword, String email, String firstName,
			String lastName, String zip, String phone, Boolean agreeToPolicy) {
		super(clearPassword, email);
		this.firstName = firstName;
		this.lastName = lastName;
		this.zip = zip;
		this.phone = phone;
		this.agreeToPolicy = agreeToPolicy;
	}

	@Override
	public Boolean getAgreeToPolicy() {
		// TODO Auto-generated method stub
		return agreeToPolicy;
	}

	@Override
	public String getCity() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFirstName() {
		return firstName;
	}

	@Override
	public String getLastName() {
		return lastName;
	}

	@Override
	public String getName() {
		return firstName + " " + lastName;
	}

	@Override
	public String getPhone() {
		// TODO Auto-generated method stub
		return phone;
	}

	@Override
	public String getZip() {
		// TODO Auto-generated method stub
		return zip;
	}
	
	/**
	 * You *SHOULD* provide your own implementation of this which implements
	 * your own security.
	 */
	public boolean checkPassword(final String hashed, final String candidate) {
		Logger.debug("UniqueIdAuthUser checking the password");
		if ((hashed == null) || (candidate == null)) {
			return false;
		}
		return BCrypt.checkpw(candidate, hashed);
	}
	
	protected String createPassword(final String clearString) {
		Logger.debug("UniqueIdAuthUser creating the password");
		try {
			return BCrypt.hashpw(clearString, BCrypt.gensalt(12, SecureRandom.getInstance("SHA1PRNG")));
		} catch (NoSuchAlgorithmException e) {
			Logger.warn("Unable to find an instance of the algorithm for hashing passwords");
			return BCrypt.hashpw(clearString, BCrypt.gensalt(12));
		}
	}
}
