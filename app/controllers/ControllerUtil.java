package controllers;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.security.User;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.math.NumberUtils;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

import play.Logger;
import play.Play;
import play.mvc.Controller;
import play.mvc.Http.Context;
import play.mvc.Http.Session;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;

/**
 * The Class ControllerUtil.
 */
public class ControllerUtil extends Controller {

	/** The Constant FLASH_DANGER_KEY. */
	public static final String FLASH_DANGER_KEY = "alert-danger";

	/** The Constant FLASH_INFO_KEY. */
	public static final String FLASH_INFO_KEY = "alert-info";

	/** The Constant FLASH_SUCCESS_KEY. */
	public static final String FLASH_SUCCESS_KEY = "alert-success";

	/** The Constant FLASH_WARNING_KEY. */
	public static final String FLASH_WARNING_KEY = "alert-warning";

	/** The Constant IMAGE_ERROR_MSG. */
	public static final String IMAGE_ERROR_MSG = "The image must be of type \""
			+ ControllerUtil.IMAGE_TYPES + "\".";

	/** The Constant IMAGE_PATTERN. */
	public static final Pattern IMAGE_PATTERN = Pattern
			.compile(".*?\\.(jpeg|jpg|tif|tiff|png|gif|bmp).*", Pattern.CASE_INSENSITIVE);

	/** The Constant IMAGE_SIZE_ERROR_MSG. */
	public static final String IMAGE_SIZE_ERROR_MSG = "The uploaded image must be less than 5 megabytes.";

	/** The Constant IMAGE_TYPES. */
	public static final String IMAGE_TYPES = "jpeg|jpg|tif|tiff|png|gif|bmp";

	/**
	 * Convert session messages.
	 */
	public static void convertSessionMessages() {
		if (StringUtils.isNotEmpty(session(ControllerUtil.FLASH_SUCCESS_KEY))) {
			flash(ControllerUtil.FLASH_SUCCESS_KEY,
					session(ControllerUtil.FLASH_SUCCESS_KEY));
			session().remove(ControllerUtil.FLASH_SUCCESS_KEY);
		}
		if (StringUtils.isNotEmpty(session(ControllerUtil.FLASH_INFO_KEY))) {
			flash(ControllerUtil.FLASH_INFO_KEY,
					session(ControllerUtil.FLASH_INFO_KEY));
			session().remove(ControllerUtil.FLASH_INFO_KEY);
		}
		if (StringUtils.isNotEmpty(session(ControllerUtil.FLASH_WARNING_KEY))) {
			flash(ControllerUtil.FLASH_WARNING_KEY,
					session(ControllerUtil.FLASH_WARNING_KEY));
			session().remove(ControllerUtil.FLASH_WARNING_KEY);
		}
		if (StringUtils.isNotEmpty(session(ControllerUtil.FLASH_DANGER_KEY))) {
			flash(ControllerUtil.FLASH_DANGER_KEY,
					session(ControllerUtil.FLASH_DANGER_KEY));
			session().remove(ControllerUtil.FLASH_DANGER_KEY);
		}
	}

	/**
	 * Decode file name.
	 * 
	 * @param fileName
	 *            the file name
	 * @return the string
	 */
	public static String decodeFileName(String fileName) {
		return fileName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
	}

	/**
	 * Format timestamp.
	 * 
	 * @param t
	 *            the t
	 * @return the string
	 */
	public static String formatTimestamp(final long t) {
		return new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").format(new Date(t));
	}

	/**
	 * Gets the event id.
	 * 
	 * @return the event id
	 */
	public static Long getEventId() {
		final String path = Context.current().request().path();
		if (StringUtils.contains(path, "events/")) {
			String subPath = StringUtils.substringAfter(path, "@");
			if (StringUtils.contains("?", subPath)) {
				subPath = StringUtils.substringBeforeLast(subPath, "?");
			}
			if (StringUtils.contains(subPath, "/")) {
				return NumberUtils.toLong(
						StringUtils.substringBefore(subPath, "/"), 0);
			} else {
				return NumberUtils.toLong(subPath, 0);
			}
		}
		return 0L;
	}

	/**
	 * Gets the local user.
	 * 
	 * @param session
	 *            the session
	 * @return the local user
	 */
	public static User getLocalUser(final Session session) {
		final AuthUser currentAuthUser = PlayAuthenticate.getUser(session);
		if (currentAuthUser != null) {
			final User localUser = User.findByAuthUserIdentity(currentAuthUser);
			Logger.trace("Controller Util - getLocalUser - {}",
					ToStringBuilder.reflectionToString(localUser));
			return localUser;
		}
		Logger.trace("Controller Util - getLocalUser - is null");
		return null;
	}

	/**
	 * Gets the resource route.
	 * 
	 * @param filename
	 *            the filename
	 * @return the resource route
	 */
	public static String getResourceRoute(String filename) {
		String folder = null;
		if (StringUtils.contains(filename, "font-awesome")
				|| StringUtils.contains(filename, "glyphicons-halflings")) {
			folder = "fonts";
		} else if (StringUtils.endsWith(filename, "js")) {
			folder = "javascripts";
		} else if (StringUtils.endsWith(filename, "css")) {
			folder = "stylesheets";
		} else {
			folder = "images";
		}
		final String rootPath = Play.application().configuration()
				.getString("img.root.url");
		if (StringUtils.isEmpty(rootPath)) {
			// Logger.debug("This is a local file - " +
			// routes.Assets.at(StringUtils.join(folder, "/", filename)).url());
			return routes.Assets.at(StringUtils.join(new String[] {folder, "/", filename}))
					.url();
		}
		// Logger.debug("This is a remote file - " +
		// routes.Assets.at(StringUtils.join(rootPath, "/", folder, "/",
		// filename)).url());
		return StringUtils.join(new String[] {rootPath, "/", folder, "/", filename});
	}

	/**
	 * Checks if is event page.
	 * 
	 * @return true, if is event page
	 */
	public static boolean isEventPage() {
		final String path = Context.current().request().path();
		if ((ControllerUtil.getLocalUser(session()) != null)
				|| StringUtils.contains(path, "events")
				|| StringUtils.contains(path, "search")
				|| StringUtils.contains(path, "profile")
				|| StringUtils.contains(path, "accounts")) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if is file too large.
	 * 
	 * @param image
	 *            the image
	 * @return true, if is file too large
	 */
	public static boolean isFileTooLarge(final File image) {
		if (image.length() > 5242880) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if is image.
	 * 
	 * @param image
	 *            the image
	 * @return true, if is image
	 */
	public static boolean isImage(final String image) {
		final Matcher matcher = IMAGE_PATTERN.matcher(image);
		return matcher.matches();
	}

	/**
	 * Checks if is user in permission.
	 * 
	 * @param permission
	 *            the permission
	 * @return true, if is user in permission
	 */
	public static boolean isUserInPermission(String permission) {
		return isUserInPermission(permission, getLocalUser(session()));
	}

	/**
	 * Checks if is user in permission.
	 * 
	 * @param permission
	 *            the permission
	 * @param user
	 *            the user
	 * @return true, if is user in permission
	 */
	public static boolean isUserInPermission(String permission, User user) {
		if (user == null) {
			return false;
		}
		return user.isUserInPermission(permission);
	}

	/**
	 * Checks if is user in role.
	 * 
	 * @param securityRole
	 *            the security role
	 * @return true, if is user in role
	 */
	public static boolean isUserInRole(String securityRole) {
		return isUserInRole(securityRole, getLocalUser(session()));
	}

	/**
	 * Checks if is user in role.
	 * 
	 * @param securityRole
	 *            the security role
	 * @param user
	 *            the user
	 * @return true, if is user in role
	 */
	public static boolean isUserInRole(String securityRole, User user) {
		if (user == null) {
			return false;
		}
		return user.isUserInRole(securityRole);
	}

	/**
	 * Sanitize text.
	 * 
	 * @param text
	 *            the text
	 * @return the string
	 */
	public static String sanitizeText(String text) {
		final PolicyFactory policy = Sanitizers.FORMATTING.and(
				Sanitizers.BLOCKS).and(Sanitizers.STYLES);
		return policy.sanitize(text);
	}

	public static final Map<String, String> STATE_MAP;
	static {
	    STATE_MAP = new TreeMap<String, String>();
	    STATE_MAP.put("AL", "Alabama");
	    STATE_MAP.put("AK", "Alaska");
	    STATE_MAP.put("AS", "American Samoa");
	    STATE_MAP.put("AZ", "Arizona");
	    STATE_MAP.put("AR", "Arkansas");
	    STATE_MAP.put("CA", "California");
	    STATE_MAP.put("CO", "Colorado");
	    STATE_MAP.put("CT", "Connecticut");
	    STATE_MAP.put("DE", "Delaware");
	    STATE_MAP.put("DC", "District Of Columbia");
	    STATE_MAP.put("FM", "Federated States of Micronesia");
	    STATE_MAP.put("FL", "Florida");
	    STATE_MAP.put("GA", "Georgia");
	    STATE_MAP.put("GU", "Guam");
	    STATE_MAP.put("HI", "Hawaii");
	    STATE_MAP.put("ID", "Idaho");
	    STATE_MAP.put("IL", "Illinois");
	    STATE_MAP.put("IN", "Indiana");
	    STATE_MAP.put("IA", "Iowa");
	    STATE_MAP.put("KS", "Kansas");
	    STATE_MAP.put("KY", "Kentucky");
	    STATE_MAP.put("LA", "Louisiana");
	    STATE_MAP.put("ME", "Maine");
	    STATE_MAP.put("MD", "Maryland");
	    STATE_MAP.put("MH", "Marshall Islands");
	    STATE_MAP.put("MA", "Massachusetts");
	    STATE_MAP.put("MI", "Michigan");
	    STATE_MAP.put("MN", "Minnesota");
	    STATE_MAP.put("MS", "Mississippi");
	    STATE_MAP.put("MO", "Missouri");
	    STATE_MAP.put("MT", "Montana");
	    STATE_MAP.put("NE", "Nebraska");
	    STATE_MAP.put("NV", "Nevada");
	    STATE_MAP.put("NH", "New Hampshire");
	    STATE_MAP.put("NJ", "New Jersey");
	    STATE_MAP.put("NM", "New Mexico");
	    STATE_MAP.put("NY", "New York");
	    STATE_MAP.put("NC", "North Carolina");
	    STATE_MAP.put("ND", "North Dakota");
	    STATE_MAP.put("MP", "Northern Mariana Islands");
	    STATE_MAP.put("OH", "Ohio");
	    STATE_MAP.put("OK", "Oklahoma");
	    STATE_MAP.put("OR", "Oregon");
	    STATE_MAP.put("PW", "Palau");
	    STATE_MAP.put("PA", "Pennsylvania");
	    STATE_MAP.put("PR", "Puerto Rico");
	    STATE_MAP.put("RI", "Rhode Island");
	    STATE_MAP.put("SK", "Saskatchewan");
	    STATE_MAP.put("SC", "South Carolina");
	    STATE_MAP.put("SD", "South Dakota");
	    STATE_MAP.put("TN", "Tennessee");
	    STATE_MAP.put("TX", "Texas");
	    STATE_MAP.put("UT", "Utah");
	    STATE_MAP.put("VT", "Vermont");
	    STATE_MAP.put("VI", "Virgin Islands");
	    STATE_MAP.put("VA", "Virginia");
	    STATE_MAP.put("WA", "Washington");
	    STATE_MAP.put("WV", "West Virginia");
	    STATE_MAP.put("WI", "Wisconsin");
	    STATE_MAP.put("WY", "Wyoming");
	}
	
	public static String stripPhone(String phone) {
		if(StringUtils.isNotEmpty(phone)) {
			return phone.replaceAll("[.-/]", "");
		}
		return phone;
	}
	
	public static boolean isEqual(Long long1, Long long2) {
		int result = ObjectUtils.compare(long1, long2);
		if(result == 0) {
			return true;
		}
		return false;
	}
}
