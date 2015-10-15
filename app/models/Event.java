package models;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Page;
import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.EnumValue;
import controllers.ControllerUtil;
import models.security.SecurityRole;
import models.security.User;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.math.NumberUtils;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import play.db.ebean.Model;
import play.i18n.Messages;
import play.mvc.PathBindable;
import security.BaseX;

import javax.persistence.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Event entity managed by JPA
 */
@Entity
public class Event extends Model implements PathBindable<Event> {

	public static final Finder<Long, Event> find = new Finder<Long, Event>(
			Long.class, Event.class);
	private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");

	private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

	@Constraints.MaxLength(value = 50)
	public String city;

	@Constraints.Required
	@Column(columnDefinition = "TEXT")
	public String content;

	public Date dateCreated;
	
	@Constraints.Required
	@Formats.DateTime(pattern = "MM/dd/yyyy")
	public Date eventStart;

	@Formats.DateTime(pattern = "MM/dd/yyyy")
	public Date eventEnd;
	
	@Constraints.Required
	@Formats.DateTime(pattern = "MM/dd/yyyy")
	public Date fundraisingEnd;

	@Constraints.Required
	@Formats.DateTime(pattern = "MM/dd/yyyy")
	public Date fundraisingStart;

	@Constraints.Required
	public int goal;

	public URL heroImgUrl;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	public URL imgUrl;
	public URL imgUrl1;
	public URL imgUrl2;
	public URL imgUrl3;
	public URL imgUrl4;

	@Constraints.Required
	public String name;

	@Constraints.Required
	public String schoolId;

	public String slug;

	@Constraints.Pattern(value = "^[a-zA-Z\\s]*$", message = "state.pattern")
	public String state;

	public PublishStatus status;

	public String title;

	public int total;
	
	@OneToOne
	@JoinColumn(name = "generalPfpId")
	public Pfp generalFund;
	
	@OneToOne
	@JoinColumn(name = "sponsorPfpId")
	public Pfp sponsorFund;

	@ManyToOne(optional = false)
	public User userAdmin;

	// @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	// @JoinColumn(name="USER_ID")
	// public List<User> eventAssistants;

	@Constraints.Pattern(value = "(\\d{5}([\\-]\\d{4})?)", message = "zip.pattern")
	public String zipCode;

	public Event() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public static boolean canManage(User localUser, Event event) {
		if (ControllerUtil.isUserInRole(SecurityRole.SYS_ADMIN, localUser)) {
			return true; 
		}
		if (ControllerUtil.isUserInRole(SecurityRole.ROOT_ADMIN, localUser)) {
			return true; 
		}
		if (ControllerUtil.isUserInRole(SecurityRole.EVENT_ADMIN, localUser) && ControllerUtil.isEqual(event.userAdmin.id, localUser.id)) {
			return true; 
		}
		return false;
	}

	public static boolean canManage(User localUser, Long id) {
		return canManage(localUser, Event.findByIdWithMin(id));
	}

	public static boolean canParticipate(User localUser, Event event) {
		if (isEventOpen(event)
				&& (!ControllerUtil.isUserInRole(SecurityRole.EVENT_ADMIN,
						localUser))) {
			return true;
		}
		return false;
	}

	public static boolean canParticipate(User localUser, boolean isOpen) {
		if (isOpen
				&& (!ControllerUtil.isUserInRole(SecurityRole.EVENT_ADMIN,
						localUser))) {
			return true;
		}
		return false;
	}
	public static boolean canParticipate(User localUser, Long id) {
		return canParticipate(localUser, Event.findByIdWithMin(id));
	}

	public static Calendar DateToCalendar(Date date) {
		final Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}

	public static int daysUntilEvent(Date eventDate) {
		final Calendar startDate = Calendar.getInstance();
		final Calendar endDate = Calendar.getInstance();
		endDate.setTime(eventDate);
		if (endDate.before(startDate)) {
			return -1;
		}
		// assert: startDate must be before endDate
		final int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
		final long endInstant = endDate.getTimeInMillis();
		int presumedDays = (int) ((endInstant - startDate.getTimeInMillis()) / MILLIS_IN_DAY);
		final Calendar cursor = (Calendar) startDate.clone();
		cursor.add(Calendar.DAY_OF_YEAR, presumedDays);
		final long instant = cursor.getTimeInMillis();
		if (instant == endInstant) {
			return presumedDays;
		}

		final int step = instant < endInstant ? 1 : -1;
		do {
			cursor.add(Calendar.DAY_OF_MONTH, step);
			presumedDays += step;
		} while (cursor.getTimeInMillis() <= endInstant);
		return presumedDays;
	}

	public static boolean existsByUserId(Long userId) {
		return find.where().eq("userAdmin.id", userId).select("id").setMaxRows(1).findRowCount() > 0;
	}

	public static List<Event> findAllEvents() {
		return find.all();
	}
	
	public static List<Event> findAllByUserId(Long id) {
		final Query<Event> events = find.where().eq("userAdmin.id", id).select("id, heroImgUrl, eventEnd, eventStart, slug, status, schoolId, fundraisingEnd, fundraisingStart, goal, name, userAdmin").fetch("userAdmin");
		if (events != null) {
			return events.findList();
		}
		return new ArrayList<Event>();
	}
	
	public static List<Object> findAllIdsByUserId(Long id) {
		final Query<Event> events = find.where()
				.eq("userAdmin.id", id).select("id");
		if (events != null) {
			return events.findIds();
		}
		return new ArrayList<Object>();
	}

	public static Event findById(Long id) {
		return find.byId(id);
	}
	public static Event findBySlug(String slug) {
		return find.where().eq("slug", slug).select("id").findUnique();
	}

	public static Event findByIdWithMin(Long id) {
		return find.where().eq("id", id).select("id, heroImgUrl, eventEnd, eventStart, slug, status, schoolId, fundraisingEnd, fundraisingStart, goal, name, userAdmin").fetch("userAdmin").findUnique();
	}
	
	public static Event findByIdWithMinAndGeneralFund(Long id) {
		return find.where().eq("id", id).select("id, heroImgUrl, eventEnd, eventStart, slug, status, schoolId, fundraisingEnd, fundraisingStart, goal, name, userAdmin, generalFund").fetch("userAdmin").fetch("generalFund").findUnique();
	}

	//rimi ====

	public static List<Event> findRandomCurrentEvents() {
		return find
		// .where()
		// .gt("eventStart",new Date())
				.setMaxRows(4).findList();
	}

	public static Date getDateToMidnight(Date date) {
		final Calendar cal = Calendar.getInstance(); // get calendar instance
		cal.setTime(date); // set cal to date
		cal.set(Calendar.HOUR_OF_DAY, 24); // set hour to midnight
		cal.set(Calendar.MINUTE, 0); // set minute in hour
		cal.set(Calendar.SECOND, 0); // set second in minute
		cal.set(Calendar.MILLISECOND, 0); // set millis in second
		return cal.getTime();
	}
	
	public static long[] getGoalPercentage(Long eventId, int eventGoal) {
		long total[] = new long[3];
		total[0] = models.Donation.getTotalEventDonations(eventId);
		if (total[0] == 0 || eventGoal == 0) {
			total[1] = 0;
			total[2] = 0;
			return total;
		}
		final int percentage = Long.valueOf((total[0] * 100) / eventGoal).intValue();
		if (percentage > 150) {
			total[2] = 150;
		} else if (percentage < 10) {
			total[2] = 1;
		} else {
			total[2] = round(percentage);
		}
		total[1] = percentage;
		return total;
	}		

	public static int round(int num) {  
		return ((((num / (double)10 )- num / 10 ) * 10 ) >= 5 ?(( num / 10 ) * 10 + 10) : ( num / 10) * 10);  
		}
	
	public static boolean hasAwards(Long id) {
		return Awards.existsByEventId(id);
	}

	public static boolean hasEventPages(Long id) {
		return EventPages.existsByEventId(id);
	}

	public static boolean hasSponsors(Long id) {
		return Sponsors.existsByEventId(id);
	}

	public static boolean hasTeams(Long id) {
		return Team.existsByEventId(id);
	}

	public static boolean hasVolunteers(Long id) {
		return Volunteers.existsByEventId(id);
	}

	public static Long getIdFromSlug(String slug) {
		return NumberUtils.createLong(StringUtils.substringAfterLast(slug, "@"));
	}
	
	public static boolean isEventOpen(Event event) {
		final Calendar cal = Calendar.getInstance();
		final Date today = cal.getTime();
		final Date fundraisingEnd = getDateToMidnight(event.fundraisingEnd);
		return event.status == PublishStatus.LIVE
				&& fundraisingEnd.after(today)
				&& event.fundraisingStart.before(today);
	}

	public static boolean isEventOpen(Long id) {
		return isEventOpen(Event.findByIdWithMin(id));
	}


	public static boolean isLive(Event event) {
		if(event.status == PublishStatus.LIVE) {
			return true;
		}
		return false;
	}
	
	/**
	 * Return a page of computer
	 * 
	 * @param page
	 *            Page to display
	 * @param pageSize
	 *            Number of pfps per page
	 * @param sortBy
	 *            Pfp property used for sorting
	 * @param order
	 *            Sort order (either or asc or desc)
	 * @param filter
	 *            Filter applied on the name column
	 */
	public static Page<Event> page(int page, int pageSize, String sortBy,
			String order, String filter, String fieldName) {
		String queryField = "name";
		if (StringUtils.equals("name", fieldName)
				|| StringUtils.equals("city", fieldName)
				|| StringUtils.equals("state", fieldName)
				|| StringUtils.equals("zip_code", fieldName)
				|| StringUtils.equals("schoolId", fieldName)) {
			queryField = fieldName;
		}
		if (pageSize > 20) {
			pageSize = 20;
		}
			return find.where().eq("status", 3)
							.ilike(queryField, "%" + filter + "%")
							.orderBy(sortBy + " " + order).findPagingList(pageSize)
							.setFetchAhead(false).getPage(page);
	}

	/**
	 * Return a page of computer
	 * 
	 * @param page
	 *            Page to display
	 * @param pageSize
	 *            Number of pfps per page
	 * @param sortBy
	 *            Pfp property used for sorting
	 * @param order
	 *            Sort order (either or asc or desc)
	 * @param filter
	 *            Filter applied on the name column
	 */
	public static Page<Event> page(int page, int pageSize, String sortBy,
			String order, String filter, String fieldName, User localUser) {
		System.out.println("filter : "+filter);
		String queryField = "name";
		if (StringUtils.equals("name", fieldName)
				|| StringUtils.equals("schoolId", fieldName)) {
			queryField = fieldName;
		} else if (StringUtils.equals("status", fieldName) ) {
			if(StringUtils.equalsIgnoreCase("NEW", filter)) {
				filter = "1";
			} else if(StringUtils.equalsIgnoreCase("PRIVATE", filter)) {
				filter = "2";
			} else if(StringUtils.equalsIgnoreCase("LOCKED", filter)) {
				filter = "4";
			} else {
				filter = "3";
			}
			queryField = fieldName;
		} else if (StringUtils.equals("userAdmin", fieldName) ) {
			queryField = "userAdmin.organization.taxId";
		}
		if (StringUtils.equals("eventName", sortBy)) {
			sortBy = "name";
		}
		if (pageSize > 20) {
			pageSize = 20;
		}
		ExpressionList<Event> query = find.where().ilike(queryField, "%" + filter + "%");
		if(localUser != null) {
			query.eq("userAdmin.id", localUser.id);									
		}
		System.out.println("query :: " + query + " : page : " + page + " : filter : " + filter + " : queryField : " + queryField);
		//int pageValue = 0;
		System.out.println("pageValue : " + page);
		System.out.println("final output :: "+query
				.orderBy(sortBy + " " + order).select("id, eventEnd, eventStart, status, schoolId, fundraisingEnd, fundraisingStart, goal, name, userAdmin").fetch("userAdmin").findPagingList(pageSize)
				.setFetchAhead(false).getPage(page).getTotalRowCount()+ " : page :"+page  + " : sortBy : " + sortBy
				+ " : order : " + order);
		return query
					.orderBy(sortBy + " " + order).select("id, eventEnd, eventStart, status, schoolId, fundraisingEnd, fundraisingStart, goal, name, userAdmin").fetch("userAdmin").findPagingList(pageSize)
					.setFetchAhead(false).getPage(page);
	}

	public static String toSlug(Event event) {
		return BaseX.encode(event.id, event.dateCreated);
	}
	
	@Override
	public Event bind(String key, String value) {
//		Event event = Event.findById(Event.getIdFromSlug(value));
		Event event = new Event();
		event.id = Event.getIdFromSlug(value);
		return event;
	}

	@Override
	public String javascriptUnbind() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String unbind(String arg0) {
		return slug;
	}
	
	public static Event getDefaultEvent() {
		Event event = new Event();
		event.id = 555L;
		event.slug = BaseX.encode(event.id, new Date());
		return event;
	}
	
	public enum PublishStatus {
		@EnumValue("1")
	    NEW(1, "New"),
	    @EnumValue("2")
	    PRIVATE(2, "Private"),
	    @EnumValue("3")
	    LIVE(3, "Live"),
	    @EnumValue("4")
	    LOCKED(4, "Locked");

		private static final Map<String, PublishStatus> MAP = new HashMap<String, PublishStatus>();
        public static final Map<String, String> VALUES = new HashMap<String, String>();
        
        static {
	    	for(PublishStatus pt: PublishStatus.values()) {
	    		VALUES.put(pt.name(), pt.value);
	    		MAP.put(pt.name(), pt);
	    	}
        }
	    
	    public static PublishStatus get(String name) {
            return MAP.get(name);
        }
		
	    public final int id;

	    public final String value;


	    public int getId() {
			return id;
		}

		public String getValue() {
			return value;
		}

	    
	    PublishStatus(int id, String value) {
	      this.id = id;
	      this.value = value;
	    }
	    
//	    @Override
//		public String toString() {
//			return id + "";
//		}
	}
	
	public List<ValidationError> validate() {
	    List<ValidationError> errors = new ArrayList<ValidationError>();
	    if(StringUtils.equals(content, "<p><br></p>")) {
	    	errors.add(new ValidationError("content", Messages.get("error.required")));
		}
	    return errors.isEmpty() ? null : errors;
	}
	
	public boolean isIdOnly() {
		if(this.eventEnd == null && this.eventStart == null && this.fundraisingEnd == null && this.fundraisingStart == null) {
			return true;
		}
		return false;
	}
}
