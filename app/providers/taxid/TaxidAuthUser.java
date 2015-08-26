package providers.taxid;

import providers.email.ContactIdentity;
import providers.taxid.TaxidAuthProvider.TaxidUserSignup;
import providers.uniqueid.RepresentOrgIdentity;
import providers.uniqueid.UniqueIdAuthUser;

import com.feth.play.module.pa.user.FirstLastNameIdentity;

public class TaxidAuthUser extends UniqueIdAuthUser implements
		OrganizationIndentity, FirstLastNameIdentity, ContactIdentity,
		RepresentOrgIdentity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Boolean agreeToPolicy;

	private Boolean agreeToRepresent;

	private String zip;

	private String firstName;

	private String lastName;

	private String orgAddress;
	private String orgCity;
	private String orgName;
	private String orgPhone;
	private String orgState;
	private Long orgTaxId;
	private String orgZip;
	private String phone;

	/**
	 * Used for password reset only - do not use this to signup a user!
	 * 
	 * @param password
	 */
	public TaxidAuthUser(final String password) {
		this(password, null, null, null, null, null, null, null, null, null, null,
				null, null, true, true);
	}

	public TaxidAuthUser(String clearPassword, String email) {
		this(clearPassword, email, null);
	}

	public TaxidAuthUser(String clearPassword, String email, Long taxid) {
		this(clearPassword, email, taxid, null, null, null, null, null, null,
				null, null, null, null, true, true);
	}

	public TaxidAuthUser(String clearPassword, String email, Long taxid, String orgAddress, 
			String orgName, String orgPhone, String orgCity, String orgState,
			String orgZip, String firstName, String lastName, String zip,
			String phone, Boolean agreeToPolicy, Boolean agreeToRepresent) {
		super(clearPassword, email, taxid);
		this.orgAddress = orgAddress;
		this.orgName = orgName;
		this.orgTaxId = taxid;
		this.orgPhone = orgPhone;
		this.orgCity = orgCity;
		this.orgState = orgState;
		this.orgZip = orgZip;
		this.firstName = firstName;
		this.lastName = lastName;
		this.zip = zip;
		this.phone = phone;
		this.agreeToPolicy = agreeToPolicy;
		this.agreeToRepresent = agreeToRepresent;
	}

	public TaxidAuthUser(final TaxidUserSignup signup) {
		this(signup.password, signup.email, signup.uniqueId);
		this.orgAddress = signup.orgAddress;
		this.orgName = signup.orgName;
		this.orgTaxId = signup.uniqueId;
		this.orgPhone = signup.orgPhone;
		this.orgCity = signup.orgCity;
		this.orgState = signup.orgState;
		this.orgZip = signup.orgZip;
		this.firstName = signup.firstName;
		this.lastName = signup.lastName;
		this.zip = signup.zip;
		this.phone = signup.phone;
		this.agreeToPolicy = signup.agreeToPolicy;
		//this.agreeToRepresent = signup.agreeToRepresent;
	}

	@Override
	public Boolean getAgreeToPolicy() {
		// TODO Auto-generated method stub
		return agreeToPolicy;
	}

	@Override
	public Boolean getAgreeToRepresent() {
		// TODO Auto-generated method stub
		return agreeToRepresent;
	}

	@Override
	public String getCity() {
		// TODO Auto-generated method stub
		return zip;
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
	public String getOrgAddress() {
		// TODO Auto-generated method stub
		return orgAddress;
	}
	
	@Override
	public String getOrgCity() {
		// TODO Auto-generated method stub
		return orgCity;
	}

	@Override
	public String getOrgName() {
		// TODO Auto-generated method stub
		return orgName;
	}

	@Override
	public String getOrgPhone() {
		// TODO Auto-generated method stub
		return orgPhone;
	}

	@Override
	public String getOrgState() {
		// TODO Auto-generated method stub
		return orgState;
	}

	@Override
	public Long getOrgTaxid() {
		// TODO Auto-generated method stub
		return orgTaxId;
	}

	@Override
	public String getOrgZip() {
		// TODO Auto-generated method stub
		return orgZip;
	}

	@Override
	public String getPhone() {
		// TODO Auto-generated method stub
		return phone;
	}

	@Override
	public String getZip() {
		return null;
	}
}
