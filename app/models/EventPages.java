package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

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
public class EventPages extends Model {

	public static final Finder<Long, EventPages> find = new Finder<Long, EventPages>(
			Long.class, EventPages.class);

	@Constraints.Required
	@Column(columnDefinition = "TEXT")
	public String content;

	public Long eventid;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

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

	public static List<EventPages> findByEventId(Long eventid) {
		final ExpressionList<EventPages> eventPages = find.where().eq(
				"eventId", eventid);
		if (eventPages != null) {
			return eventPages.findList();
		}
		return new ArrayList<EventPages>();
	}
	
	public static List<EventPages> findByEventIdSorted(Long eventid) {
		final Query<EventPages> eventPages = find.where().eq(
				"eventId", eventid).order("name");
		if (eventPages != null) {
			return eventPages.findList();
		}
		return new ArrayList<EventPages>();
	}
	
	public static List<EventPages> findByEventIdMinSorted(Long eventid) {
		final Query<EventPages> eventPages = find.where().eq(
				"eventId", eventid).select("id, name").order("name");
		if (eventPages != null) {
			return eventPages.findList();
		}
		return new ArrayList<EventPages>();
	}

	public static EventPages findById(Long id) {
		return find.byId(id);
	}
	
	public List<ValidationError> validate() {
	    List<ValidationError> errors = new ArrayList<ValidationError>();
	    if(StringUtils.equals(content, "<p><br></p>")) {
	    	errors.add(new ValidationError("content", Messages.get("error.required")));
		}
	    return errors.isEmpty() ? null : errors;
	}
}