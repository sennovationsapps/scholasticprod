package models;

import java.util.LinkedHashMap;
import java.util.Map;

import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;

public class ContactUs {

	@Email
	@Required
	public String email;

	@Required
	public String firstName;

	@Required
	public String group;

	@Required
	public String lastName;

	@Required
	@MaxLength(value = 2000, message = "Your question must be less than 2,000 characters.")
	public String message;

	/*@MaxLength(value = 12, message = "The phone number cannot be longer than 12 digits including - or .")
	public String phone;*/


	//======start====the pattern of phone no added like us phone no=======24.08.2015==================//
	@Required
		/*@Pattern(value = "^[0-9]$", message = "phone.pattern")*/
	@MaxLength(value = 3)
	@Pattern(value = "[0-9.+]+", message = "A valid phone number is required")
	public String phPart1;




	@Required
		/*@Pattern(value = "^[0-9]$", message = "phone.pattern")*/
	@MaxLength(value = 3)
	@Pattern(value = "[0-9.+]+", message = "A valid phone number is required")
	public String phPart2;




	@Required
		/*@Pattern(value = "^[0-9]$", message = "phone.pattern")*/
	@Pattern(value = "[0-9.+]+", message = "A valid phone number is required")
	@MaxLength(value = 4)
	public String phPart3;


	@Required
		/*@Pattern(value = "^[0-9]$", message = "phone.pattern")*/
	@MaxLength(value = 10)
	public String phone;
	//======end====the pattern of phone no added like us phone no=======24.08.2015==================//



	//======start====the pattern of phone no added like us phone no=======12.08.2015==================//
	/*@Required

	@Pattern(value = "^[0-9]{3}-[0-9]{3}-[0-9]{4}$", message = "phone.pattern")
	@MaxLength(value = 12)
	public String phone;*/
//======end====the pattern of phone no added like us phone no=======12.08.2015==================//

	@Required
	public String subject;

	public static Map<String, String> options() {
		final Map<String, String> options = new LinkedHashMap<String, String>();
		options.put("Event Support", "Event Support");
		options.put("Starting an Event", "Starting an Event");
		options.put("Technical/Software", "Technical/Software");
		options.put("General", "General");
		return options;
	}
}