package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.apache.commons.lang.builder.ToStringBuilder;

import play.data.validation.Constraints;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import play.data.validation.Constraints.Pattern;

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

}