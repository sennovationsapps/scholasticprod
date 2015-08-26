package models;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateUtils;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Query;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;

import play.Logger;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class Shift extends Model {

	public static int EXPIRATION_TIME = 24;

	public static final Finder<Long, Shift> find = new Finder<Long, Shift>(
			Long.class, Shift.class);

	@Constraints.Required
	@Formats.DateTime(pattern = "MM/dd/yyyy")
	public Date date;

	public String description;

	@Constraints.Required
	@Formats.DateTime(pattern = "h:mm a")
	public Date endTime;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	@Required
	public String name;

	@Constraints.Required
	@Formats.DateTime(pattern = "h:mm a")
	public Date startTime;

	public int volunteerCount;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JsonManagedReference
	public List<Volunteer> volunteerList;


	public List<Volunteer> getVolunteerList() {
		return volunteerList;
	}

	public void setVolunteerList(List<Volunteer> volunteerList) {
		this.volunteerList = volunteerList;
	}

	@ManyToOne
	@JsonBackReference
	public Volunteers volunteers;
	
	public static boolean hasVolunteers(scala.Option<String> shiftId) {
		if(StringUtils.equals(shiftId.toString(), "None")) {
			return false;
		}
		Volunteer vol = Volunteer.find.where().eq("shift_id", shiftId.get()).setMaxRows(1).setFirstRow(1).findUnique();
    	if(vol == null || vol.id == null) {
    		return false;
    	}
    	return true;	
    }

	public void expireVolunteers() {
		if (CollectionUtils.isNotEmpty(this.volunteerList)) {
			List<Volunteer> expiredVolunteers = null;
			final Volunteer[] volunteers = volunteerList
					.toArray(new Volunteer[volunteerList.size()]);
			final Date todaysDate = new Date();
			for (final Volunteer volunteer : volunteers) {
				if(!volunteer.active) {
					final Date createdDate = DateUtils.addHours(
							volunteer.dateCreated, EXPIRATION_TIME);
					if (createdDate.before(todaysDate)) {
						if (expiredVolunteers == null) {
							expiredVolunteers = new ArrayList<Volunteer>();
						}
						volunteerList.remove(volunteer);
						expiredVolunteers.add(volunteer);
					}
				}
			}
			this.update();
			if (CollectionUtils.isNotEmpty(expiredVolunteers)) {
				for (final Volunteer volunteer : expiredVolunteers) {
					volunteer.delete();
				}
			}
		}
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public static Shift findById(Long id) {
		return find.byId(id);
	}
	
	public static List<Shift> getSortedShifts(List<Shift> shifts) {
		Ebean.sort(shifts, "date,startTime,name");
		return shifts;
	}

	public static List<Object> getShiftNames(Shift shift) {
		final List<Object> shiftNames = new ArrayList<Object>();
		if (!CollectionUtils.isEmpty(shift.volunteerList)) {
			shiftNames.addAll(shift.volunteerList);
		}
		for(int count = shiftNames.size() + 1; count <= shift.volunteerCount; count++) {
			shiftNames.add("Available");
		}
		
		return shiftNames;
	}

	public static boolean isShiftFull(Shift shift) {
		int count = 0;
		for (final Volunteer volunteer : shift.volunteerList) {
			if (volunteer.active) {
				count = count + 1;
			}
		}
		if (count == shift.volunteerCount) {
			return true;
		}
		return false;
	}
	
}