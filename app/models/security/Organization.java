package models.security;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import com.avaje.ebean.ExpressionList;
import play.data.validation.Constraints;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Pattern;
import play.db.ebean.Model;

@Entity
public class Organization extends Model {

	public static final Finder<Long, Organization> find = new Finder<Long, Organization>(
			Long.class, Organization.class);

	public String address;
	
	@MaxLength(value = 50)
	public String city;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	public String name;
	@Constraints.Required

	@Constraints.Pattern(value = "^[0-9]{3}-[0-9]{3}-[0-9]{4}$",message = "The phone number cannot be longer than 12 digits including -")
	@MaxLength(value = 12)
	public String phone;

	@MaxLength(value = 20)
	@Pattern(value = "^[a-zA-Z\\s]*$", message = "state.pattern")
	public String state;

	@Pattern(value = "^[0-9]{9}$", message = "taxid.pattern")
	public Long taxId;

	@OneToOne(mappedBy = "organization")
	public User user;

	@Pattern(value = "(\\d{5}([\\-]\\d{4})?)", message = "zip.pattern")
	public String zip;

	public static Organization findById(Long id) {
		return find.byId(id);
	}

	public static ExpressionList<Organization> findByName(String name)
	{
		return find.where().like("LCASE(NAME)", name.toLowerCase()+"%");
	}
}