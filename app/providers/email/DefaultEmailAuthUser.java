package providers.email;

public class DefaultEmailAuthUser extends EmailAuthUser {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The session timeout in seconds Defaults to two weeks
	 */
	final static long SESSION_TIMEOUT = 24 * 14 * 3600;
	private final long expiration;

	public DefaultEmailAuthUser(final EmailAuthUser emailAuthUser) {
		this(emailAuthUser.getPassword(), emailAuthUser.getEmail(),
				emailAuthUser.getFirstName(), emailAuthUser.getLastName(),
				emailAuthUser.getZip(), emailAuthUser.getPhone(), emailAuthUser.getAgreeToPolicy());
	}

	/**
	 * For logging the user in automatically
	 *
	 * @param email
	 */
	public DefaultEmailAuthUser(final String email) {
		this(null, email);
	}

	public DefaultEmailAuthUser(final String clearPassword, final String email) {
		this(clearPassword, email, null, null, null, null, true);
	}

	public DefaultEmailAuthUser(final String clearPassword, final String email,
			final String firstName, final String lastName, final String zip,
			final String phone, final Boolean agreeToPolicy) {
		super(clearPassword, email, firstName, lastName, zip, phone, agreeToPolicy);

		expiration = System.currentTimeMillis() + (1000 * SESSION_TIMEOUT);
	}

	@Override
	public long expires() {
		return expiration;
	}

	@Override
	public String getId() {
		return super.getEmail();
	}

}
