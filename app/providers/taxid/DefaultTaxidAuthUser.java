package providers.taxid;

public class DefaultTaxidAuthUser extends TaxidAuthUser {
	/**
* 
*/
	private static final long serialVersionUID = 1L;

	/**
	 * The session timeout in seconds Defaults to two weeks
	 */
	final static long SESSION_TIMEOUT = 24 * 14 * 3600;
	private final long expiration;

	/**
	 * For logging the user in automatically
	 * 
	 * @param email
	 */
	public DefaultTaxidAuthUser(final String email) {
		this(null, email);
	}

	public DefaultTaxidAuthUser(final String email, final Long taxid) {
		this(null, email, taxid);
	}

	public DefaultTaxidAuthUser(final String clearPassword, final String email) {
		this(clearPassword, email, null);
	}

	public DefaultTaxidAuthUser(final String clearPassword, final String email,
			final Long taxid) {
		this(clearPassword, email, taxid, null, null, null, null, null, null, null,
				null, null, null, true, true);
	}

	public DefaultTaxidAuthUser(String clearPassword, String email, Long taxid, String orgAddress,
			String orgName, String orgPhone, String orgCity, String orgState,
			String orgZip, String firstName, String lastName, String zip,
			String phone, Boolean agreeToPolicy, Boolean agreeToRepresent) {
		super(clearPassword, email, taxid, orgAddress, orgName, orgPhone, orgCity,
				orgState, orgZip, firstName, lastName, zip, phone,
				agreeToPolicy, agreeToRepresent);

		expiration = System.currentTimeMillis() + (1000 * SESSION_TIMEOUT);
	}

	public DefaultTaxidAuthUser(final TaxidAuthUser authUser) {
		this(authUser.getPassword(), authUser.getEmail(), authUser
				.getUniqueId(), authUser.getOrgAddress(), authUser.getOrgName(), authUser.getOrgPhone(),
				authUser.getOrgCity(), authUser.getOrgState(), authUser
						.getOrgZip(), authUser.getFirstName(), authUser
						.getLastName(), authUser.getCity(),
				authUser.getPhone(), authUser.getAgreeToPolicy(), authUser
						.getAgreeToRepresent());
	}

	@Override
	public long expires() {
		return expiration;
	}

	@Override
	public String getId() {
		return super.getUniqueId().toString();
	}

}
