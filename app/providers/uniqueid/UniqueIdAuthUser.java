package providers.uniqueid;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.mindrot.jbcrypt.BCrypt;

import play.Logger;

import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.EmailIdentity;

public abstract class UniqueIdAuthUser extends AuthUser implements
		EmailIdentity, UniqueIdIdentity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final String email;
	private final transient String password;
	private final Long uniqueId;

	public UniqueIdAuthUser(final String clearPassword, final String email,
			final Long uniqueId) {
		this.password = clearPassword;
		this.email = email;
		this.uniqueId = uniqueId;
	}

	/**
	 * Should return null if the clearString given is null.
	 * 
	 * @return
	 */
	// protected abstract String createPassword(final String clearString);

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

	@Override
	public String getEmail() {
		return email;
	}

	public String getHashedPassword() {
		return createPassword(this.password);
	}

	/**
	 * Should return false if either the candidate or stored password is null.
	 * 
	 * @param candidate
	 * @return
	 */
	// public abstract boolean checkPassword(final String candidate);

	@Override
	public String getId() {
		return getHashedPassword();
	}

	public String getPassword() {
		return this.password;
	}

	@Override
	public String getProvider() {
		return UniqueIdAuthProvider.PROVIDER_KEY;
	}

	@Override
	public Long getUniqueId() {
		return uniqueId;
	}

	/**
	 * You *SHOULD* provide your own implementation of this which implements
	 * your own security.
	 */
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
