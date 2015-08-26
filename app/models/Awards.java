package models;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import play.data.validation.Constraints;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import play.data.validation.ValidationError;
import play.i18n.Messages;

@Entity
public class Awards extends Model {

	public static final Finder<Long, Awards> find = new Finder<Long, Awards>(
			Long.class, Awards.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 5533495101228269641L;

	@Constraints.Required
	@Column(columnDefinition = "TEXT")
	public String content;

	public Long eventid;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;
	
	public URL imgUrl;

	@Required
	public String name;

	@OneToMany(cascade = CascadeType.ALL)
	public List<Prize> prizes;

	public String title;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public static boolean existsByEventId(Long eventId) {
		return find.where().eq("eventId", eventId).select("id").setMaxRows(1).findRowCount() > 0;
	}

	public static Awards findByEventId(Long id) {
		return find.where().eq("eventid", id).findUnique();
	}
	
	public static Awards findByEventIdMin(Long id) {
		return find.where().eq("eventid", id).select("id, name").findUnique();
	}

	public static Awards findById(Long id) {
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