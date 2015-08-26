package models.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import play.Logger;
import play.db.ebean.Model;
import be.objectify.deadbolt.core.models.Permission;

/**
 * Initial version based on work by Steve Chaloner (steve@objectify.be) for
 * Deadbolt2
 */
@Entity
public class UserPermission extends Model implements Permission {

	public static final Model.Finder<Long, UserPermission> find = new Model.Finder<Long, UserPermission>(
			Long.class, UserPermission.class);
	
	public static final String DEFAULT_USER = "default.user";
	
	public static final String EVENT_CONTENT_EDIT = "event.content.edit";
	public static final String EVENT_DONATION_EDIT = "event.donation.edit";
	public static final String EVENT_PAGES_EDIT = "event.pages.edit";
	public static final String EVENT_ROOT_EDIT = "event.root.edit";
	public static final String EVENT_SPONSOR_EDIT = "event.sponsor.edit";
	public static final String EVENT_VOLUNTEER_EDIT = "event.volunteer.edit";
	
	public static final String PFP_DONATION_EDIT = "pfp.donation.edit";
	public static final String PFP_ROOT_EDIT = "pfp.root.edit";
	
	public static final String SYS_REPORTS_EDIT = "sys.reports.edit";
	public static final String SYS_DONATION_EDIT = "sys.donation.edit";
	public static final String SYS_EVENT_EDIT = "sys.event.edit";
	public static final String SYS_PFP_EDIT = "sys.pfp.edit";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	public Long id;

	public String value;

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public static UserPermission findByValue(String value) {
		return find.where().eq("value", value).findUnique();
	}

	public static List<UserPermission> getUserPermissions(String roleName) {
		final Map<String, UserPermission> allRecords = UserPermission.lookup();
		final List<UserPermission> rolePermissions = new ArrayList<UserPermission>();
		if (StringUtils.equals(SecurityRole.PFP_ADMIN, roleName)) {
			rolePermissions.add(allRecords.get(UserPermission.PFP_ROOT_EDIT));
		} else if (StringUtils.equals(SecurityRole.EVENT_ADMIN, roleName)) {
			rolePermissions.add(allRecords.get(UserPermission.EVENT_ROOT_EDIT));
			rolePermissions.add(allRecords
					.get(UserPermission.EVENT_DONATION_EDIT));
			rolePermissions.add(allRecords
					.get(UserPermission.EVENT_SPONSOR_EDIT));
			rolePermissions.add(allRecords
					.get(UserPermission.EVENT_VOLUNTEER_EDIT));
			rolePermissions.add(allRecords
					.get(UserPermission.EVENT_CONTENT_EDIT));
			rolePermissions
					.add(allRecords.get(UserPermission.EVENT_PAGES_EDIT));
		} else if (StringUtils.equals(SecurityRole.ROOT_ADMIN, roleName)) {
			rolePermissions.addAll(allRecords.values());
		} else if (StringUtils.equals(SecurityRole.SYS_ADMIN, roleName)) {
			rolePermissions.add(allRecords.get(UserPermission.EVENT_ROOT_EDIT));
			rolePermissions.add(allRecords
					.get(UserPermission.EVENT_DONATION_EDIT));
			rolePermissions.add(allRecords
					.get(UserPermission.EVENT_SPONSOR_EDIT));
			rolePermissions.add(allRecords
					.get(UserPermission.EVENT_VOLUNTEER_EDIT));
			rolePermissions.add(allRecords
					.get(UserPermission.EVENT_CONTENT_EDIT));
			rolePermissions
					.add(allRecords.get(UserPermission.EVENT_PAGES_EDIT));
			rolePermissions.add(allRecords.get(UserPermission.PFP_ROOT_EDIT));
			rolePermissions.add(allRecords.get(UserPermission.SYS_EVENT_EDIT));
			rolePermissions.add(allRecords.get(UserPermission.SYS_PFP_EDIT));
		} else {
			rolePermissions.add(allRecords.get(UserPermission.DEFAULT_USER));
		}
		Logger.debug("The permissions being added to a user {} - {}", roleName,
				Arrays.toString(rolePermissions.toArray()));
		return rolePermissions;
	}

	public static List<String> getUserPermissionValues() {
		return Arrays.asList(UserPermission.EVENT_ROOT_EDIT,
				UserPermission.EVENT_DONATION_EDIT,
				UserPermission.EVENT_SPONSOR_EDIT,
				UserPermission.EVENT_VOLUNTEER_EDIT,
				UserPermission.EVENT_CONTENT_EDIT,
				UserPermission.EVENT_PAGES_EDIT,
				UserPermission.PFP_DONATION_EDIT, 
				UserPermission.PFP_ROOT_EDIT,
				UserPermission.SYS_DONATION_EDIT,
				UserPermission.SYS_EVENT_EDIT, 
				UserPermission.SYS_PFP_EDIT,
				UserPermission.SYS_REPORTS_EDIT,
				UserPermission.DEFAULT_USER);

	}

	public static Map<String, UserPermission> lookup() {
		@SuppressWarnings("unchecked")
		final List<UserPermission> permissions = find.findList();
		final Map<String, UserPermission> options = new HashMap<String, UserPermission>();
		for (final UserPermission permission : permissions) {
			options.put(permission.value, permission);
		}
		return options;
	}

	public static Map<String, String> options() {
		@SuppressWarnings("unchecked")
		final List<UserPermission> permissions = find.findList();
		final LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
		for (final UserPermission permission : permissions) {
			options.put(permission.id.toString(), permission.value);
		}
		return options;
	}
}
