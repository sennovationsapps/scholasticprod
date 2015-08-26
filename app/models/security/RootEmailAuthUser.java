package models.security;

import providers.email.EmailAuthProvider.EmailUserSignup;
import providers.email.EmailAuthUser;

public class RootEmailAuthUser extends EmailAuthUser {

	public RootEmailAuthUser(EmailUserSignup signup) {
		super(signup);
		// TODO Auto-generated constructor stub
	}

	public RootEmailAuthUser(String password) {
		super(password);
		// TODO Auto-generated constructor stub
	}

	public RootEmailAuthUser(String clearPassword, String email) {
		super(clearPassword, email);
		// TODO Auto-generated constructor stub
	}

	public RootEmailAuthUser(String clearPassword, String email,
			String firstName, String lastName) {
		super(clearPassword, email, firstName, lastName);
		// TODO Auto-generated constructor stub
	}

	public RootEmailAuthUser(String clearPassword, String email,
			String firstName, String lastName, String city, String phone,
			Boolean agreeToPolicy) {
		super(clearPassword, email, firstName, lastName, city, phone,
				agreeToPolicy);
		// TODO Auto-generated constructor stub
	}

}
