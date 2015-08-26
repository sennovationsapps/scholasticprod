package models;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import play.i18n.Messages;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Page;
import com.avaje.ebean.Query;

@Entity
public class Team extends Model implements Comparable {

	public static final Finder<Long, Team> find = new Finder<Long, Team>(
			Long.class, Team.class);

	public String captain;

	@Constraints.Email
	// if you make this unique, keep in mind that users *must* merge/link their
	// accounts then on signup with additional providers
	// @Column(unique = true)
	public String captainEmail;

	@Constraints.Required
	@Column(columnDefinition = "TEXT")
	public String content;

	public Long eventid;

	@Required
	public int goal;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	public URL imgUrl;

	@Required
	public String name;

	public String title;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public static boolean existsByEventId(Long eventId) {
		return find.where().eq("eventId", eventId).select("id").setMaxRows(1).findRowCount() > 0;
	}

	public static List<Team> findByEventId(Long eventid) {
		final ExpressionList<Team> teams = find.where().eq("eventId", eventid);
		if (teams != null) {
			return teams.findList();
		}
		return new ArrayList<Team>();
	}
	
	public static List<Team> findByEventIdSorted(Long eventid) {
		final Query<Team> teams = find.where().eq("eventId", eventid).order("name");
		if (teams != null) {
			return teams.findList();
		}
		return new ArrayList<Team>();
	}

	public static List<Team> findByEventIdMinSorted(Long eventid) {
		final Query<Team> teams = find.where().eq("eventId", eventid).select("id, name").order("name");
		if (teams != null) {
			return teams.findList();
		}
		return new ArrayList<Team>();
	}
	
	public static Team findById(Long id) {
		return find.byId(id);
	}

	public static Team findBySpecificTeam(List<Team> eventTeams, Long teamId) {
		for (final Team team : eventTeams) {
			if (ObjectUtils.compare(team.id, teamId) == 0) {
				return team;
			}
		}
		return null;
	}

	public static Team findFirstByEventId(Long id) {
		return find.where().eq("eventId", id).setMaxRows(1).findUnique();
	}

	public static long[] getGoalPercentage(Long eventId, Long teamId, int teamGoal) {
		long total[] = new long[3];
		total[0] = models.Donation.getTotalTeamDonations(eventId,
						teamId);
		if (total[0] == 0 || teamGoal == 0) {
			total[1] = 0;
			total[2] = 0;
			return total;
		}
		final int percentage = Long.valueOf((total[0] * 100) / teamGoal).intValue();
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

	public static Map<String, String> options(Long eventid) {
		@SuppressWarnings("unchecked")
		final List<Team> teams = Team.findByEventId(eventid);
		final LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
		for (final Team team : teams) {
			options.put(team.id.toString(), team.name);
		}
		return options;
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
	public static Page<Team> page(int page, int pageSize, String sortBy,
			String order, String filter, String fieldName) {
		String queryField = "name";
		if (StringUtils.equals("name", fieldName)
				|| StringUtils.equals("captain", fieldName)
				|| StringUtils.equals("eventid", fieldName)
				|| StringUtils.equals("goal", fieldName)) {
			queryField = fieldName;
		}
		if (pageSize > 20) {
			pageSize = 20;
		}
		return find.where()
				.ilike(queryField, "%" + filter + "%")
				.orderBy(sortBy + " " + order).findPagingList(pageSize)
				.setFetchAhead(false).getPage(page);
	}

	public List<ValidationError> validate() {
	    List<ValidationError> errors = new ArrayList<ValidationError>();
	    if(StringUtils.equals(content, "<p><br></p>")) {
	    	errors.add(new ValidationError("content", Messages.get("error.required")));
		}
	    return errors.isEmpty() ? null : errors;
	}

	@Override
	public int compareTo(Object arg0) {
		return this.name.compareTo(((Team)arg0).name);
	}
}