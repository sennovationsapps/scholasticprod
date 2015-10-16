package models;

import base.utils.DateUtils;
import base.utils.SortUtils;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Page;
import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonBackReference;
import controllers.ControllerUtil;
import models.security.SecurityRole;
import models.security.User;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.math.NumberUtils;
import play.data.validation.Constraints;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;
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
public class Pfp extends Model implements PathBindable<Pfp>, Comparable {

	public Pfp() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static final Finder<Long, Pfp> find = new Finder<Long, Pfp>(
			Long.class, Pfp.class);
	private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");

	private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

	@Required
	@Column(columnDefinition = "TEXT")
	public String content;

	public Date dateCreated;

	public String emergencyContact;



	@Constraints.Pattern(value = "[0-9.+]+",message = "The phone number cannot be longer than 12 digits including -")

	@MaxLength(value = 10)
	public String emergencyContactPhone;

	@ManyToOne(optional = false)
	@JsonBackReference
	public Event event;

	@Required
	public int goal;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	public URL imgUrl;

	@Required
	public String name;

	public PfpType pfpType;
	
	public boolean privateAcct;

	public String slug;

	@ManyToOne(optional = true)
	@JsonBackReference
	public Team team;

	public String title;

	public int total;

	@ManyToOne(optional = false)
	@JsonBackReference
	public User userAdmin;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public static boolean canManage(User localUser, Event event, Pfp pfp) {
		if (ControllerUtil.isUserInRole(SecurityRole.SYS_ADMIN, localUser)) {
			return true; 
		}
		if (ControllerUtil.isUserInRole(SecurityRole.ROOT_ADMIN, localUser)) {
			return true; 
		}
		if (ControllerUtil.isUserInRole(SecurityRole.EVENT_ADMIN, localUser) && ControllerUtil.isEqual(event.userAdmin.id, localUser.id)) {
			return true; 
		}
		if (ControllerUtil.isUserInRole(SecurityRole.PFP_ADMIN, localUser) && ControllerUtil.isEqual(pfp.userAdmin.id, localUser.id)) {
			return true; 
		}
		return false;
	}

	public static boolean canManage(User localUser, Long eventId, Long pfpId) {
		return canManage(localUser, Event.findById(eventId),
				Pfp.findById(pfpId));
	}
	
	public static boolean existsByUserId(Long id) {
		final List<Pfp> pfps = find.where().eq("userAdmin.id", id)
				.setMaxRows(1).findList();
		if (CollectionUtils.isNotEmpty(pfps)) {
			return true;
		}
		return false;
	}
	
	public static boolean existsAlready(Pfp pfp, Event event) {
		final List<Pfp> pfps = find.where().eq("userAdmin.id", pfp.userAdmin.id).eq("event.id", event.id).eq("team.id", pfp.team.id).like("name", pfp.name)
				.setMaxRows(1).findList();
		if (CollectionUtils.isNotEmpty(pfps)) {
			return true;
		}
		return false;
	}

	public static List<Pfp> findByEventId(Long id) {
		final ExpressionList<Pfp> pfps = find.where().eq("event.id", id);
		if (pfps != null) {
			return pfps.findList();
		}
		return new ArrayList<Pfp>();
	}
	
	public static Pfp findByIdWithMin(Long id) {
		/*return find.where().eq("id", id).select("id, name, goal, event.id, event.name, pfpType, team.id, team.name, userAdmin").fetch("userAdmin").fetch("event").fetch("team").findUnique();*/
		return find.where().eq("id", id).select("id, name, goal, pfpType, userAdmin").fetch("userAdmin").fetch("event","name").fetch("team","name").findUnique();

	}

	public static Long findEventIdByPfpId(Long pfpId) {
		final Pfp pfp = find.select("event.id").where().eq("id", pfpId).setMaxRows(1).findUnique();
		if (pfp != null) {
			return pfp.event.id;
		}
		return null;
	}
	
	public static List<Pfp> findByEventExceptSponsor(Event event) {
		final ExpressionList<Pfp> pfps = find.select("id, name").where().eq("event.id", event.id).ne("pfpType", "3");
		if (pfps != null) {
			return pfps.findList();
		}
		return new ArrayList<Pfp>();
	}

//// FindAll Added by  Suvadeep Datta

	public static List<Pfp> findAll(String eventId) {
		Event event=Event.findBySlug(eventId);
		Query<Pfp> query = Ebean.createQuery(Pfp.class);
		query.select("id, name")
				.where().eq("event.id", event.id).eq("pfpType", "1");

		if (query != null) {
		return query.findList();
		}
		return new ArrayList<Pfp>();
	}
	
	public static List<Pfp> findByEventAndTeamExceptSponsor(Event event, Team team) {
		final ExpressionList<Pfp> pfps = find.select("id, name").where().eq("event.id", event.id).eq("team.id", team.id).ne("pfpType", "3");
		if (pfps != null) {
			return pfps.findList();
		}
		return new ArrayList<Pfp>();
	}
	
	public static List<Pfp> findByEventExceptSponsorAndPrivate(Event event) {
		final ExpressionList<Pfp> pfps = find.select("id, name").where().eq("event.id", event.id).ne("pfpType", "3").eq("privateAcct", "0");
		if (pfps != null) {
			return pfps.findList();
		}
		return new ArrayList<Pfp>();
	}

	public static Map<String, String> findByEventIdMap(Event event) {
		List<Pfp> pfps = Pfp.findByEventExceptSponsorAndPrivate(event);
		Map<String, String> pfpMap = new HashMap<String, String>();
		for(Pfp pfp: pfps) {
			pfpMap.put(pfp.id + "", pfp.name);	
		}
		return SortUtils.sortDonationsByName(pfpMap);
	}

	public static Map<String, String> findByEventIdMapWithPrivate(Event event) {
		List<Pfp> pfps = Pfp.findByEventExceptSponsor(event);
		Map<String, String> pfpMap = new HashMap<String, String>();
		for(Pfp pfp: pfps) {
			pfpMap.put(pfp.id + "", pfp.name);	
		}
		return SortUtils.sortDonationsByName(pfpMap);
	}

	public static List<Pfp> findByTeamId(Long id) {
		final ExpressionList<Pfp> pfps = find.where().eq("team.id", id);
		if (pfps != null) {
			return pfps.findList();
		}
		return new ArrayList<Pfp>();
	}
	
	public static Pfp findById(Long id) {
		return find.byId(id);
	}

	public static List<Pfp> findByUserId(Long id) {
		final ExpressionList<Pfp> pfps = find.where().eq("userAdmin.id", id);
		if (pfps != null) {
			return pfps.findList();
		}
		return new ArrayList<Pfp>();
	}
	
	public static List<Object> findAllIdsByUserId(Long id) {
		final Query<Pfp> events = find.where()
				.eq("userAdmin.id", id).select("id");
		if (events != null) {
			return events.findIds();
		}
		return new ArrayList<Object>();
	}
	
	public static long[] getGoalPercentage(Long pfpId, int pfpGoal) {
		long total[] = new long[3];
		total[0] = models.Donation.getTotalPfpDonations(pfpId);
		if (total[0] == 0 || pfpGoal == 0) {
			total[1] = 0;
			total[2] = 0;
			return total;
		}
		final int percentage = Long.valueOf((total[0] * 100) / pfpGoal).intValue();
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

	public static Long getIdFromSlug(String slug) {
		return NumberUtils.createLong(StringUtils.substringAfterLast(slug, "@"));
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
	public static Page<Pfp> page(int page, int pageSize, String sortBy,
			String order, String filter) {
		if (pageSize > 20) {
			pageSize = 20;
		}
		return find.where().ne("privateAcct", true)
				.ilike("name", "%" + filter + "%")
				.orderBy(sortBy + " " + order).findPagingList(pageSize)
				.setFetchAhead(false).getPage(page);
	}

	public static Page<Pfp> page(int page, int pageSize, String sortBy,
					String order, String filter, String fieldName, User localUser) {
		System.out.println("filter : "+filter);
		String queryField = "name";
		if (StringUtils.equals("name", fieldName)
						|| StringUtils.equals("name", fieldName)) {
					queryField = fieldName;
				} else if (StringUtils.equals("teamName", fieldName) ) {
					queryField = "team.name";
				} else if (StringUtils.equals("eventName", fieldName) ) {
					queryField = "event.name";
				} else if (StringUtils.equals("userAdmin", fieldName) ) {
					queryField = "userAdmin.email";
				}
				if (pageSize > 20) {
					pageSize = 20;
				}
				ExpressionList<Pfp> query = find.where().ilike(queryField, "%" + filter + "%");
				if(localUser != null) {
					if(localUser.isEventAdmin()) {
						query.eq("event.userAdmin.id", localUser.id);
					} else {
						query.eq("userAdmin.id", localUser.id);
					}														
				}
				/*return query.select("id, name, goal, event.id, event.name, pfpType, team.id, team.name, userAdmin")
				.fetch("userAdmin").fetch("event").fetch("team")
				.orderBy(sortBy + " " + order).findPagingList(pageSize)
				.setFetchAhead(false).getPage(page);*/

		System.out.println("query :: " + query + " : page : " + page + " : filter : " + filter + " : queryField : " + queryField);
		//int pageValue = 0;
		System.out.println("pageValue : " + page);
		System.out.println("final output :: "+query.select("id, name, goal, pfpType, userAdmin")
				.fetch("userAdmin").fetch("event", "name").fetch("team", "name")
				.orderBy(sortBy + " " + order).findPagingList(pageSize)
				.setFetchAhead(false).getPage(page).getTotalRowCount()+ " : page :"+page  + " : sortBy : " + sortBy
				+ " : order : " + order);

		return query.select("id, name, goal, pfpType, userAdmin")
				.fetch("userAdmin").fetch("event","name").fetch("team","name")
				.orderBy(sortBy + " " + order).findPagingList(pageSize)
				.setFetchAhead(false).getPage(page);
			}





	//====================pfp search for particular event============28.08.2015=======================start=======================//

	public static Page<Pfp> pageForParticularEvent(int page, int pageSize, String sortBy,
												   String order, String filter, String fieldName, User localUser, Long eventId) {
		System.out.println("filter : "+filter);
		String queryField = "name";
		if (StringUtils.equals("name", fieldName)
				|| StringUtils.equals("name", fieldName)) {
			queryField = fieldName;
		} else if (StringUtils.equals("teamName", fieldName) ) {
			queryField = "team.name";
		} else if (StringUtils.equals("eventName", fieldName) ) {
			queryField = "event.name";
		} else if (StringUtils.equals("userAdmin", fieldName) ) {
			queryField = "userAdmin.email";
		}
		if (pageSize > 20) {
			pageSize = 20;
		}
		System.out.println("filter :: "+"%" + filter + "%");
		ExpressionList<Pfp> query = find.where().ilike(queryField, "%" + filter + "%").eq("event.id", eventId);
		if(localUser != null) {
			if(localUser.isEventAdmin()) {
				query.eq("event.userAdmin.id", localUser.id);
			} else {
				query.eq("userAdmin.id", localUser.id);
			}
		}
		System.out.println("query :: " + query + " : page : " + page + " : filter : " + filter + " : queryField : " + queryField);
		//int pageValue = 0;
		System.out.println("pageValue : " + page);
		System.out.println("final output :: "+query.select("id, name, goal, pfpType, userAdmin")
				.fetch("userAdmin").fetch("event","name").fetch("team","name")
				.orderBy(sortBy + " " + order).findPagingList(pageSize)
				.setFetchAhead(false).getPage(page).getTotalRowCount()+ " : page :"+page  + " : sortBy : " + sortBy
				+ " : order : " + order);
		/*return query.select("id, name, goal, event.id, event.name, pfpType, team.id, team.name, userAdmin")
				.fetch("userAdmin").fetch("event").fetch("team")
				.orderBy(sortBy + " " + order).findPagingList(pageSize)
				.setFetchAhead(false).getPage(page);*/
		return query.select("id, name, goal, pfpType, userAdmin")
				.fetch("userAdmin").fetch("event","name").fetch("team","name")
				.orderBy(sortBy + " " + order).findPagingList(pageSize)
				.setFetchAhead(false).getPage(page);
	}

	//====================pfp search for particular event============28.08.2015=======================end=======================//


	
	public static List<Pfp> findAllByEventIdAndOptions(Long id, Map<String, String> options) {
		final ExpressionList<Pfp> pfps = find.where()
						.eq("event.id", id);
						if(options.containsKey("fromDate")) {
							pfps.gt("dateCreated", DateUtils.parseDate(options.get("fromDate")).get());	
						}
						if(options.containsKey("toDate")) {
							pfps.lt("dateCreated", DateUtils.parseDate(options.get("toDate")).get());	
						}
						if(options.containsKey("pfpType") && StringUtils.isNotEmpty(options.get("pfpType"))) {
							pfps.eq("pfpType", options.get("pfpType"));	
						}
		/*final Query<Pfp> queryPfps = pfps.select("id, name, dateCreated, goal, emergencyContact, emergencyContactPhone, pfpType, team.id, team.name, event.id, event.name, userAdmin.id, userAdmin.firstName, userAdmin.lastName, userAdmin.email, userAdmin.phone")
						.fetch("team").fetch("event").fetch("userAdmin");*/

		final Query<Pfp> queryPfps = pfps.select("id, name, dateCreated, goal, emergencyContact, emergencyContactPhone, pfpType")
				.fetch("team","name").fetch("event","name").fetch("userAdmin","firstName").fetch("userAdmin","lastName").fetch("userAdmin","email").fetch("userAdmin","phone");

		if (queryPfps != null) {
			return queryPfps.findList();
		}
		return new ArrayList<Pfp>();
	}
	
	public static Map<String, String> privateOptions() {
		final LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
		options.put("false", "Default");
		options.put("true", "Private");
		return options;
	}

	public static String toSlug(Pfp pfp) {
		return BaseX.encode(pfp.id, pfp.dateCreated);
	}

	@Override
	public Pfp bind(String key, String value) {
//		Pfp pfp = Pfp.findById(Pfp.getIdFromSlug(value));
		Pfp pfp = new Pfp();
		pfp.id = Pfp.getIdFromSlug(value);
		return pfp;
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

	public enum PfpType {
		@EnumValue("1")
	    PFP(1, "Pfp"),
	    @EnumValue("2")
	    GENERAL(2, "General Fund"),
	    @EnumValue("3")
	    SPONSOR(3, "Sponsor Fund");

		private static final Map<String, PfpType> MAP = new HashMap<String, PfpType>();
        public static final Map<String, String> VALUES = new HashMap<String, String>();
        
        static {
	    	for(PfpType pt: PfpType.values()) {
	    		VALUES.put(pt.name(), pt.value);
	    		MAP.put(pt.name(), pt);
	    	}
        }
	    
	    public static PfpType get(String name) {
            return MAP.get(name);
        }
		
	    public final int id;

	    public int getId() {
			return id;
		}

		public String getValue() {
			return value;
		}

		public final String value;
	    
	    PfpType(int id, String value) {
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
	
	public static String getDefaultPfpMessage() {
		 return "I am participating in this years event_name event. I am asking you to join me in the effort to support education and our school by making a contribution to support my efforts. The money I raise will be supporting all the students in our school. Because education is so important to me I am challenging myself to raise pfp_goal. As my friends and family make donations to my personal fundraising page my apple tree will grow and flourish. I will be watching and working very hard to not only grow one tree but an entire orchard by exceeding my goal. With your help I will be successful. So please help me add an apple to my tree. To make a donation click the red donate button at the top of the page. Whatever you can give will help! Thank You! pfp_name.";	
	}

	@Override
	public int compareTo(Object arg0) {
		return this.name.compareTo(((Pfp)arg0).name);
	}
	
	public boolean isIdOnly() {
		if(this.dateCreated == null && this.pfpType == null && this.slug == null) {
			return true;
		}
		return false;
	}
}