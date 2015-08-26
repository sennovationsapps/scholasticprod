package providers.uniqueid;

public class DefaultUniqueIdAuthUser extends UniqueIdAuthUser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DefaultUniqueIdAuthUser(final String clearPassword,
			final String email, final Long uniqueId) {
		super(clearPassword, email, uniqueId);
	}

	@Override
	public String getId() {
		return super.getUniqueId().toString();
	}

}
