package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.apache.commons.lang.builder.ToStringBuilder;
import play.data.validation.Constraints;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Volunteer extends Model {

	public static final Finder<Long, Volunteer> find = new Finder<Long, Volunteer>(
			Long.class, Volunteer.class);

	public boolean active;

	public Date dateCreated;

	@Constraints.Email
	// if you make this unique, keep in mind that users *must* merge/link their
	// accounts then on signup with additional providers
	// @Column(unique = true)
	@Required
	public String email;

	@Required
	public String firstName;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	@Required
	public String lastName;


	/*@Required*/

	/*@Pattern(value = "^[0-9]{3}-[0-9]{3}-[0-9]{4}$",message = "The phone number cannot be longer than 12 digits including - or .")*/

	@Pattern(value = "[0-9.+]+", message = "phone.pattern")
	@MaxLength(value = 10)
	public String mobile;



	public String note;

	/*
	@Required
	@MaxLength(value = 12, message = "The phone number cannot be longer than 12 digits including - or .")
	public String phone;*/

	//======start====the pattern of phone no added like us phone no=======12.08.2015==================//
/*	@Required*/




	@Pattern(value = "[0-9.+]+", message = "phone.pattern")
	@MaxLength(value = 10)
	public String phone;
//======end====the pattern of phone no added like us phone no=======12.08.2015==================//


	@ManyToOne
	@JsonBackReference
	public Shift shift;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public static Volunteer findById(Long id) {
		return find.byId(id);
	}

	//========start======rimi volunteer of event logic=========15.10.2015================================//
	public static boolean findDuplicateRegistrationOfVolunteerForSameShift(String firstName, String lastName,String email, Long shiftId ){
		System.out.println("within findDuplicateDonationToSameDonor....email :: "+email + " :: firstName :: "+firstName+" :: lastName :: "+lastName+" :: shiftId :: "+shiftId);
		System.out.println("the status :: "+find.where().eq("first_name",firstName).eq("last_name",lastName).eq("email",email).eq("shift_id",shiftId).select("id").findList());
		return find.where().eq("first_name",firstName).eq("last_name",lastName).eq("email",email).eq("shift_id",shiftId).select("id").findRowCount() > 1;
		//return find.where().eq("id", id).select("id, heroImgUrl, eventEnd, eventStart, slug, status, schoolId, fundraisingEnd, fundraisingStart, goal, name, userAdmin, generalFund").fetch("userAdmin").fetch("generalFund").findUnique();
	}
	//========end======rimi volunteer of event logic=========15.10.2015================================//


}