package providers.uniqueid;

public class SessionUniqueIdAuthUser extends DefaultUniqueIdAuthUser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final long expires;

	public SessionUniqueIdAuthUser(final String clearPassword,
			final Long uniqueId, final long expires) {
		this(clearPassword, null, uniqueId, expires);
	}

	public SessionUniqueIdAuthUser(final String clearPassword,
			final String email, final Long uniqueId, final long expires) {
		super(clearPassword, email, uniqueId);
		this.expires = expires;
	}

	@Override
	public long expires() {
		return expires;
	}
}
