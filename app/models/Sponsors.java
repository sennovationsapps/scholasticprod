package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import play.i18n.Messages;

import com.avaje.ebean.Ebean;

@Entity

public class Sponsors extends Model {

	public static final Finder<Long, Sponsors> find = new Finder<Long, Sponsors>(
			Long.class, Sponsors.class);

	@Constraints.Required
	@Column(columnDefinition = "TEXT")
	public String content;

	public Long eventid;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	@Required
	public String name;

	@OneToMany(cascade = CascadeType.ALL)
	@JsonManagedReference
	public List<SponsorItem> sponsoritems;

	public String title;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public static boolean existsByEventId(Long eventId) {
		return find.where().eq("eventId", eventId).select("id").setMaxRows(1).findRowCount() > 0;
	}

	public static Sponsors findByEvent(Event event) {
		if(event == null) {
			return null;
		}
		return find.where().eq("eventid", event.id).findUnique();
	}

	
	public static Sponsors findByEventId(Long id) {
		return find.where().eq("eventid", id).findUnique();
	}
	
	public static Sponsors findByEventIdMin(Long id) {
		return find.where().eq("eventid", id).select("id, name").findUnique();
	}

	public static Sponsors findById(Long id) {
		return find.byId(id);
	}
	
	public static Map<Long, SponsorItem> getSponsorItemWithDonations(List<SponsorItem> sponsoritems) {
		Map<Long, SponsorItem> itemMap = new HashMap<Long, SponsorItem>();
		for(SponsorItem item: sponsoritems) {
			if(item.donation != null) {
				itemMap.put(item.id, item);
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


	//// FindAll Added by  Suvadeep Datta

	public static List<Sponsors> findAll(String event) {
		final List<Sponsors> sponsors = find.all();
		System.out.println(event);
		if (sponsors != null) {
			return sponsors;

		}
		return new ArrayList<Sponsors>();
	}

}