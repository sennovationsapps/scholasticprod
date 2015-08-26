package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import play.i18n.Messages;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Query;

@Entity
public class Volunteers extends Model {

	public static final Finder<Long, Volunteers> find = new Finder<Long, Volunteers>(
			Long.class, Volunteers.class);

	@Constraints.Required
	@Column(columnDefinition = "TEXT")
	public String content;

	public Long eventid;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	@Required
	public String name;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JsonManagedReference
	public List<Shift> shifts;

	public String title;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public static boolean existsByEventId(Long eventId) {
		return find.where().eq("eventId", eventId).select("id").setMaxRows(1).findRowCount() > 0;
	}

	public static Volunteers findByEvent(Event event) {
		if(event == null) {
			return null;
		}
		return find.where().eq("eventid", event.id).findUnique();
	}
	
	public static Volunteers findByEventId(Long id) {
		return find.where().eq("eventid", id).findUnique();
	}
	
	public static Volunteers findByEventIdMin(Long id) {
		return find.where().eq("eventid", id).select("id, name").findUnique();
	}

	public static Volunteers findById(Long id) {
		return find.byId(id);
	}

	public static Map<Long, Shift> getShiftsWithVolunteers(List<Shift> shifts) {
		Map<Long, Shift> itemMap = new HashMap<Long, Shift>();
		if(shifts != null) {
			for(Shift item: shifts) {
				if(CollectionUtils.isNotEmpty(item.volunteerList)) {
					itemMap.put(item.id, item);
				}
			}
		}
		return itemMap;
	}
	
	public List<ValidationError> validate() {
	    List<ValidationError> errors = new ArrayList<ValidationError>();
	    if(StringUtils.equals(content, "<p><br></p>")) {
	    	errors.add(new ValidationError("content", Messages.get("error.required")));
		}
	    return errors.isEmpty() ? null : errors;
	}
	

	public static Volunteers findAllByEventIdAndOptions(Long id, Map<String, String> options) {
		final ExpressionList<Volunteers> volunteers = find.where()
						.eq("eventid", id);
//						if(options.containsKey("fromDate")) {
//							shifts.gt("date", base.utils.DateUtils.parseDate(options.get("fromDate")));	
//						}
//						if(options.containsKey("toDate")) {
//							shifts.lt("date", base.utils.DateUtils.parseDate(options.get("toDate")));	
//						}
		final Query<Volunteers> queryVolunteers = volunteers.select("id, eventid, volunteer.id, volunteer.firstName, volunteer.lastName, volunteer.mobile, volunteer.phone, volunteer.email, volunteer.note, shift.id, shift.date, shift.name, shift.startTime, shift.endTime, shift.volunteerCount")
						.fetch("shifts");
		return queryVolunteers.findUnique();
	}



	//// FindAll Added by  Suvadeep Datta

	public static List<Volunteers>  findAllVolunteer(String id) {
		Event eventy=Event.findBySlug(id);
		final ExpressionList<Volunteers> volunteers = find.where()
				.eq("eventid", eventy.id);
		final Query<Volunteers> queryVolunteers = volunteers.select("id, eventid, volunteer.id, volunteer.firstName, volunteer.lastName, volunteer.mobile, volunteer.phone, volunteer.email, volunteer.note, shift.id, shift.date, shift.name, shift.startTime, shift.endTime, shift.volunteerCount")
				.fetch("shifts");
		return queryVolunteers.findList();
	}
}