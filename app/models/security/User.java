package models.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import models.Awards;
import models.security.TokenAction.Type;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import play.Logger;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Pattern;
import play.db.ebean.Model;
import providers.email.ContactIdentity;
import providers.email.EmailAuthUser;
import providers.email.TermsOfServiceIdentity;
import providers.taxid.OrganizationIndentity;
import providers.taxid.TaxidAuthUser;
import providers.uniqueid.RepresentOrgIdentity;
import providers.uniqueid.UniqueIdAuthUser;
import providers.uniqueid.UniqueIdIdentity;
import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Role;
import be.objectify.deadbolt.core.models.Subject;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.user.EmailIdentity;
import com.feth.play.module.pa.user.FirstLastNameIdentity;

import controllers.ControllerUtil;

/**
 * Initial version based on work by Steve Chaloner (steve@objectify.be) for
 * Deadbolt2
 */
@Entity
@Table(name = "users")
public class User extends Model implements Subject {
	public static final Finder<Long, User> find = new Finder<Long, User>(
			Long.class, User.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public boolean active;

	public Boolean agreeToPolicy;

	public Boolean agreeToRepresent;

	public Date dateCreated;

	@Constraints.Email
	// if you make this unique, keep in mind that users *must* merge/link their
	// accounts then on signup with additional providers
	// @Column(unique = true)
	public String email;

	public boolean emailValidated;

	public String firstName;

	@Id
	public Long id;

	@Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date lastLogin;

	public String lastName;

	@OneToMany(cascade = CascadeType.ALL)
	public List<LinkedAccount> linkedAccounts;
	@OneToMany(cascade = CascadeType.ALL)
	public List<MergedAccount> mergedAccounts;

	@OneToOne(cascade = CascadeType.ALL, optional = true)
	public Organization organization;

	@ManyToMany
	public List<UserPermission> permissions;


	@Constraints.Required

	@Constraints.Pattern(value = "^[0-9]{3}-[0-9]{3}-[0-9]{4}$",message = "The phone number cannot be longer than 12 digits including -")

	@MaxLength(value = 12)
	public String phone;

	@ManyToMany
	public List<SecurityRole> roles;

	@Column(unique = true, nullable = true)
	public Long taxid;

	@Pattern(value = "(\\d{5}([\\-]\\d{4})?)", message = "zip.pattern")
	public String zip;

	public void addRoles(String roleName) {
		if (roles == null) {
			roles = new ArrayList<SecurityRole>();
		}
		roles.add(SecurityRole.findByRoleName(roleName));
		final List<UserPermission> rolePermissions = UserPermission
				.getUserPermissions(roleName);
		if (CollectionUtils.isNotEmpty(rolePermissions)) {
			Logger.debug("Role Permissions Exist = {}",
					Arrays.toString(rolePermissions.toArray()));
			if (permissions == null) {
				permissions = new ArrayList<UserPermission>();
			}
			permissions.addAll(rolePermissions);
		} else {
			Logger.debug("Role Permissions Don't Exist");
		}
	}

	public void changePassword(final AuthUser authUser, final boolean create) {
		LinkedAccount a = this.getAccountByProvider(authUser.getProvider());
		Logger.debug("Is there a linked account - " + a);
		if (a == null) {
			if (create) {
				a = LinkedAccount.create(authUser);
				a.user = this;
			} else {
				throw new RuntimeException(
						"Account not enabled for password usage");
			}
		}
		if (authUser instanceof UniqueIdAuthUser) {
			a.providerUserId = ((UniqueIdAuthUser) authUser)
					.getHashedPassword();
		} else if (authUser instanceof UsernamePasswordAuthUser) {
			a.providerUserId = ((UsernamePasswordAuthUser) authUser)
					.getHashedPassword();
		}
		a.save();
	}

	public LinkedAccount getAccountByProvider(final String providerKey) {
		return LinkedAccount.findByProviderKey(this, providerKey);
	}

	@Override
	public String getIdentifier() {
		return Long.toString(id);
	}

	@Override
	public List<? extends Permission> getPermissions() {
		return permissions;
	}

	public Set<String> getProviders() {
		final Set<String> providerKeys = new HashSet<String>(
				linkedAccounts.size());
		for (final LinkedAccount acc : linkedAccounts) {
			providerKeys.add(acc.providerKey);
		}
		return providerKeys;
	}

	@Override
	public List<? extends Role> getRoles() {
		return roles;
	}

	public boolean isEventAdmin() {
		return isUserInRole(SecurityRole.EVENT_ADMIN);
	}

	public boolean isEventAssist() {
		return isUserInRole(SecurityRole.EVENT_ASSIST);
	}

	public boolean isPfpAdmin() {
		return isUserInRole(SecurityRole.PFP_ADMIN);
	}

	public boolean isRootAdmin() {
		return isUserInRole(SecurityRole.ROOT_ADMIN);
	}

	public boolean isSysAdmin() {
		return isUserInRole(SecurityRole.SYS_ADMIN);
	}

	public boolean isUserInPermission(String userPermission) {
		if (permissions != null) {
			for (final UserPermission permission : permissions) {
				if (StringUtils.equals(permission.value, userPermission)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isUserInRole(String userRole) {
		if (roles != null) {
			for (final SecurityRole role : roles) {
				if (StringUtils.equals(role.roleName, userRole)) {
					return true;
				}
			}
		}
		return false;
	}

	public void merge(final User otherUser) {
		Logger.debug(
				"We are now in the User.merge thisUser={} and otherUser{}",
				this.email, otherUser.email);
		// deactivate the merged user that got added to this one
		otherUser.active = false;
		// store the user in the merged accounts
		// it appears we have to do this because linked accounts can't have the
		// same provider and so trying to link accounts with the same provider
		// invokes the merge functionality.
		// we don't want to merge, we really just want to deactivate one user
		// and store that deactivated user so we can access their donations

		this.mergedAccounts.add(MergedAccount.create(otherUser));

		Ebean.save(Arrays.asList(new User[] { otherUser, this }));
	}

	public void resetPassword(final AuthUser authUser, final boolean create) {
		// You might want to wrap this into a transaction
		this.changePassword(authUser, create);
		TokenAction.deleteByUser(this, Type.PASSWORD_RESET);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public static void addLinkedAccount(final AuthUser oldUser,
			final AuthUser newUser) {
		Logger.debug(
				"We are now in the User.addLinkedAccount oldUser={} and newUser{}",
				oldUser.getProvider(), newUser.getProvider());
		final User u = User.findByAuthUserIdentity(oldUser);
		u.linkedAccounts.add(LinkedAccount.create(newUser));
		u.save();
	}

	// TODO Clean up and remove
	public static void addMergedAccount(final AuthUser oldUser,
			final AuthUser newUser) {
		Logger.debug(
				"We are now in the User.addLinkedAccount oldUser={} and newUser{}",
				oldUser.getProvider(), newUser.getProvider());
		final User u = User.findByAuthUserIdentity(oldUser);
		u.mergedAccounts.add(MergedAccount.create(User
				.findByAuthUserIdentity(newUser)));
		u.save();
	}

	public static User create(final AuthUser authUser) {
		// TODO - see what happens when a user is created
		Logger.debug("create - Taxid {}",
				ToStringBuilder.reflectionToString(authUser));
		final User user = new User();
		if (authUser instanceof TaxidAuthUser) {
			user.addRoles(SecurityRole.EVENT_ADMIN);
		} else if (authUser instanceof RootEmailAuthUser) {
			user.addRoles(SecurityRole.ROOT_ADMIN);
		} else if (authUser instanceof EmailAuthUser) {
			user.addRoles(SecurityRole.PFP_ADMIN);
		} else {
			user.addRoles(SecurityRole.USER);
		}

		user.active = true;
		user.lastLogin = new Date();
		user.linkedAccounts = Collections.singletonList(LinkedAccount
				.create(authUser));

		if (authUser instanceof EmailIdentity) {
			final EmailIdentity identity = (EmailIdentity) authUser;
			// Remember, even when getting them from FB & Co., emails should be
			// verified within the application as a security breach there might
			// break your security as well!
			user.email = identity.getEmail();
			user.emailValidated = true;
		}

		if (authUser instanceof FirstLastNameIdentity) {
			final FirstLastNameIdentity identity = (FirstLastNameIdentity) authUser;
			final String firstName = identity.getFirstName();
			final String lastName = identity.getLastName();
			if (firstName != null) {
				user.firstName = firstName;
			}
			if (lastName != null) {
				user.lastName = lastName;
			}
		}

		if (authUser instanceof UniqueIdIdentity) {
			Logger.debug("This user has a uniqueid identity");
			final UniqueIdIdentity identity = (UniqueIdIdentity) authUser;
			final Long uniqueId = identity.getUniqueId();
			if (uniqueId != null) {
				user.taxid = uniqueId;
			}
		}

		if (authUser instanceof ContactIdentity) {
			Logger.debug("This user has a contact identity");
			final ContactIdentity identity = (ContactIdentity) authUser;
			final String zip = identity.getZip();
			if (zip != null) {
				user.zip = zip;
			}
			final String phone = identity.getPhone();
			if (phone != null) {
				user.phone = ControllerUtil.stripPhone(phone);
			}
		}

		if (authUser instanceof OrganizationIndentity) {
			Logger.debug("This user has a organization identity");
			final Organization org = new Organization();
			final OrganizationIndentity identity = (OrganizationIndentity) authUser;
			final String name = identity.getOrgName();
			if (name != null) {
				org.name = name;
			}
			final String address = identity.getOrgAddress();
			if (address != null) {
				org.address = address;
			}
			final String city = identity.getOrgCity();
			if (city != null) {
				org.city = city;
			}
			final String state = identity.getOrgState();
			if (state != null) {
				org.state = state;
			}
			final String zip = identity.getOrgZip();
			if (zip != null) {
				org.zip = zip;
			}
			final String phone = identity.getOrgPhone();
			if (phone != null) {
				org.phone = ControllerUtil.stripPhone(phone);
			}
			final Long taxid = identity.getOrgTaxid();
			if (taxid != null) {
				org.taxId = taxid;
			}
			org.user = user;
			user.organization = org;
		}

		if (authUser instanceof TermsOfServiceIdentity) {
			final TermsOfServiceIdentity identity = (TermsOfServiceIdentity) authUser;
			final Boolean agree = identity.getAgreeToPolicy();
			if (agree != null) {
				user.agreeToPolicy = agree;
			}
		}

		if (authUser instanceof RepresentOrgIdentity) {
			final RepresentOrgIdentity identity = (RepresentOrgIdentity) authUser;
			final Boolean agree = identity.getAgreeToRepresent();
			if (agree != null) {
				user.agreeToRepresent = agree;
			}
		}

		// if (authUser instanceof FirstLastNameIdentity) {
		// final FirstLastNameIdentity identity = (FirstLastNameIdentity)
		// authUser;
		// final String firstName = identity.getFirstName();
		// final String lastName = identity.getLastName();
		// if (firstName != null) {
		// user.firstName = firstName;
		// }
		// if (lastName != null) {
		// user.lastName = lastName;
		// }
		// }
		user.dateCreated = new Date();
		user.save();
		user.saveManyToManyAssociations("roles");
		user.saveManyToManyAssociations("permissions");
		return user;
	}

	public static void deactivate(final User otherUser) {
		Logger.debug("User to deactivate {}", otherUser.email);
		otherUser.active = false;
		otherUser.save();
	}

	public static boolean existsByAuthUserIdentity(
			final AuthUserIdentity identity) {
		final ExpressionList<User> exp;
		if (identity instanceof UsernamePasswordAuthUser) {
			exp = getUsernamePasswordAuthUserFind((UsernamePasswordAuthUser) identity);
		} else {
			exp = getAuthUserFind(identity);
		}
		return exp.findRowCount() > 0;
	}

	public static User findByAuthUserIdentity(final AuthUserIdentity identity) {
		if (identity == null) {
			return null;
		}
		// TODO - for some reason the user is not being found
		Logger.debug("findByAuthUserIdentity - Taxid {}", ToStringBuilder.reflectionToString(identity));
		if (identity instanceof UsernamePasswordAuthUser) {
			Logger.debug("Finding the email user - but cant print it");// +
			return findByUsernamePasswordIdentity((UsernamePasswordAuthUser) identity);
		} else if (identity instanceof UniqueIdAuthUser) {
			// TODO - for some reason the user is not being found
			Logger.debug("Finding the unique user - but cant print it");// +
																		// ToStringBuilder.reflectionToString(identity));
			return findByTaxidIdentity((UniqueIdAuthUser) identity);
		} else {
			return getAuthUserFind(identity).findUnique();
		}

	}

	public static User findByEmail(final String email) {
		return getEmailUserFind(email).findUnique();
	}
	
	public static User findById(Long id) {
		return find.byId(id);
	}

	public static User findByTaxid(final Long taxid) {
		return getTaxidUserFind(taxid).findUnique();
	}

	public static User findByTaxidIdentity(final UniqueIdAuthUser identity) {
		return getTaxidAuthUserFind(identity).findUnique();
	}

	public static User findByUsernamePasswordIdentity(
			final UsernamePasswordAuthUser identity) {
		return getUsernamePasswordAuthUserFind(identity).findUnique();
	}

	public static void merge(final AuthUser oldUser, final AuthUser newUser) {
		User.findByAuthUserIdentity(oldUser).merge(
				User.findByAuthUserIdentity(newUser));
	}

	public static void setLastLoginDate(final AuthUser knownUser) {
		final User u = User.findByAuthUserIdentity(knownUser);
		u.lastLogin = new Date();
		u.save();
	}

	public static void verify(final User unverified) {
		// You might want to wrap this into a transaction
		Logger.debug("Verifying user - {}",
				ToStringBuilder.reflectionToString(unverified));
		unverified.emailValidated = true;
		unverified.save();
		TokenAction.deleteByUser(unverified, Type.EMAIL_VERIFICATION);
	}

	private static ExpressionList<User> getAuthUserFind(
			final AuthUserIdentity identity) {
		return find.where().eq("active", true)
				.eq("linkedAccounts.providerUserId", identity.getId())
				.eq("linkedAccounts.providerKey", identity.getProvider());
	}

	private static ExpressionList<User> getEmailUserFind(final String email) {
		return find.where().eq("active", true).eq("email", email);
	}


	/*****************new addition******for partial search of email*********19.08.2015**********start*******/
	private static ExpressionList<User> getEmailUserFindByPartialValue(final String emailValue) {
		System.out.println("within getEmailUserFindByPartialValue :: " + emailValue);
		ExpressionList<User> user=find.where().eq("active", true).like("email",emailValue+"%");
		//System.out.println("-------------Size of mail search-----------"+user.);

		return find.where().eq("active", true).like("email",emailValue + "%");
	}
	/*****************new addition******for partial search of email*********19.08.2015***********end********/

	private static ExpressionList<User> getTaxidAuthUserFind(
			final UniqueIdAuthUser identity) {
		return getTaxidUserFind(identity.getUniqueId()).eq(
				"linkedAccounts.providerKey", identity.getProvider());
	}

	private static ExpressionList<User> getTaxidUserFind(final Long taxid) {
		return find.where().eq("active", true).like("taxid", "%"+taxid+"%");
	}

	private static ExpressionList<User> getUsernamePasswordAuthUserFind(
			final UsernamePasswordAuthUser identity) {
		return getEmailUserFind(identity.getEmail()).eq(
				"linkedAccounts.providerKey", identity.getProvider());
	}


	/*****Start Code T-264*************/


	public static  ExpressionList<User> findByEmailList(final String email) {
		/*****************new addition******for partial search of email*********19.08.2015**********start*******/
		System.out.println("within findByEmailList : "+email);
		return getEmailUserFindByPartialValue(email);
		/*****************new addition******for partial search of email*********19.08.2015***********end********/
	}

	public static ExpressionList<User> findByFirst(final String first) {
		return getFirstUserFind(first);
	}

	public static ExpressionList<User> findByOrganizationId(final long orgaId){
		return find.where().eq("active", true).eq("organization_id",orgaId);
	}


	public static ExpressionList<User> findByLast(final String last) {
		return getLastUserFind(last);
	}


	public static ExpressionList<User>  findByTaxidList(final Long taxid) {
		return getTaxidUserFind(taxid);
	}

	//public ExpressionList<User> findBy


	private static ExpressionList<User>  getFirstUserFind(String first){
		return find.where().eq("active", true).like("LCASE(firstName)", first.toLowerCase()+"%");

	}


	private static ExpressionList<User>  getLastUserFind(String last){
		return find.where().eq("active", true).like("LCASE(lastName)", last.toLowerCase()+"%");

	}




	/*****End Code T-264*************/



}
