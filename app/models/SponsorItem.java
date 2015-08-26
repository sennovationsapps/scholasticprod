package models;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.time.DateUtils;

import com.avaje.ebean.Ebean;

import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class SponsorItem extends Model {

	public static int EXPIRATION_TIME = 24;

	public static final Finder<Long, SponsorItem> find = new Finder<Long, SponsorItem>(
			Long.class, SponsorItem.class);

//	@Required
	public int amount;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

//	@Required
	public String description;

	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.REFRESH }, optional = true)
	//@OneToOne(cascade = CascadeType.ALL, optional = true)
	public Donation donation;

	@ManyToOne
	@JsonBackReference
	public Sponsors sponsors;

//	@Required
	public String title;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public static SponsorItem findById(Long id) {
		return find.byId(id);
	}
	
	public static List<SponsorItem> getSortedSponsorItems(List<SponsorItem> item) {
		Ebean.sort(item, "amount desc, title");
		return item;
	}

	// public static Map<Date, Set<SponsorItem>>
	// getOrderedShifts(List<SponsorItem> shifts) {
	// Map<Date, Set<SponsorItem>> orderedShifts = new TreeMap<Date,
	// Set<SponsorItem>>(new Comparator<Date>() {
	// public int compare(Date date1, Date date2) {
	// return date1.compareTo(date2);
	// }
	// });
	// for(SponsorItem shift: shifts) {
	// // ArrayList<Shift> newShifts = null;
	// if(orderedShifts.containsKey(shift.date)) {
	// Logger.debug("Contains the date - " + shift.name + " - " +
	// shift.date);
	// Set<SponsorItem> newShifts = orderedShifts.get(shift.date);
	// newShifts.add(shift);
	// } else {
	// Logger.debug("Does not contain the date - " + shift.name + " - " +
	// shift.date);
	// Set<SponsorItem> newShifts = new TreeSet<SponsorItem>(new
	// Comparator<SponsorItem>() {
	// public int compare(SponsorItem shift1, SponsorItem shift2) {
	// int compare = shift1.startTime.compareTo(shift2.startTime);
	// if(compare != 0) {
	// return compare;
	// }
	// compare = shift1.endTime.compareTo(shift2.endTime);
	// if(compare == 0) {
	// return -1;
	// }
	// return compare;
	// }
	// });
	// newShifts.add(shift);
	// orderedShifts.put(shift.date, newShifts);
	// }
	// }
	// return orderedShifts;
	// }
	//

}