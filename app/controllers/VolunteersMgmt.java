package controllers;

import static play.data.Form.form;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import models.Donation;
import models.Event;
import models.Shift;
import models.SponsorItem;
import models.Volunteer;
import models.Volunteers;
import models.security.SecurityRole;
import models.security.User;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.data.Form;
import play.db.ebean.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.volunteers.*;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.feth.play.module.mail.Mailer;
import com.feth.play.module.mail.Mailer.Mail.Body;

// TODO: Auto-generated Javadoc
/**
 * Manage a database of events.
 */
public class VolunteersMgmt extends Controller {

	/**
	 * Handle the 'new donation form' submission.
	 * 
	 * @param id
	 *            the id
	 * @param shiftId
	 *            the shift id
	 * @param volunteerId
	 *            the volunteer id
	 * @return the result
	 */
	@Transactional
	public static Result activateShiftVolunteer(Event event, Long shiftId,
			Long volunteerId) {
		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		final Volunteer volunteer = Volunteer.findById(volunteerId);
		if (volunteer == null) {
			flash(ControllerUtil.FLASH_WARNING_KEY,
					"The volunteer validation has expired after "
							+ SponsorItem.EXPIRATION_TIME + " hours.");
		} else {
			volunteer.active = true;
			volunteer.update();
		}
		return VolunteersMgmt.get(event, Shift.findById(shiftId).volunteers.id);
	}

	/**
	 * Display the 'new pfp form'.
	 * 
	 * @param id
	 *            the id
	 * @return the result
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
			@Group(SecurityRole.EVENT_ASSIST) })
	@SubjectPresent(content = "/login")
	public static Result create(Event event) {
		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		if (!Event.canManage(ControllerUtil.getLocalUser(session()), event)) {
			flash(ControllerUtil.FLASH_DANGER_KEY,
					"The requested event action cannot be completed by the logged in user.");
			return redirect(routes.Application.index());
		}
		return ok(createForm.render(event, form(Volunteers.class)));
	}

	/**
	 * Display the 'new donation form'.
	 * 
	 * @param id
	 *            the id
	 * @param shiftId
	 *            the shift id
	 * @return the result
	 */
	public static Result createShiftVolunteer(Event event, Long shiftId) {
		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		return ok(modalCreateForm.render(event, shiftId, form(Volunteer.class)));
	}

	/**
	 * Display the 'edit form' of a existing Pfp.
	 * 
	 * @param id
	 *            Id of the pfp to edit
	 * @param pageId
	 *            the page id
	 * @return the result
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
			@Group(SecurityRole.EVENT_ASSIST) })
	@SubjectPresent(content = "/login")
	public static Result edit(Event event, Long pageId) {
		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		if (!Event.canManage(ControllerUtil.getLocalUser(session()), event)) {
			flash(ControllerUtil.FLASH_DANGER_KEY,
					"The requested event action cannot be completed by the logged in user.");
			return redirect(routes.Application.index());
		}
		final Form<Volunteers> volunteersForm = form(Volunteers.class).fill(
				Volunteers.findById(pageId));
		return ok(editForm.render(event, pageId, volunteersForm));
	}

	public static Result expireVolunteers(Event event, Long pageId ) {
		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		if (Event.isEventOpen(event)) {
			final Volunteers volunteers = Volunteers.findById(pageId);
			for (final Shift shift : volunteers.shifts) {
				if(CollectionUtils.isNotEmpty(shift.volunteerList)) {
					shift.expireVolunteers();
				}
			}
		}
		return redirect(routes.VolunteersMgmt.get(event, pageId));
	}
	
	/**
	 * Display the 'edit form' of a existing Pfp.
	 * 
	 * @param id
	 *            Id of the pfp to edit
	 * @param pageId
	 *            the page id
	 * @return the result
	 */
	public static Result get(Event event, Long pageId) {
		//System.out.println("within get ::"+event+" <pageId> "+pageId);
	if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		final User localUser = ControllerUtil.getLocalUser(session());

		if(!Event.isLive(event) && (localUser == null || !ControllerUtil.isEqual(event.userAdmin.id, localUser.id))) {
			flash(ControllerUtil.FLASH_WARNING_KEY,
					"This event is not published yet and therefore is not available at this time.");
			return ok(views.html.index.render());
		}
		//TODO - Until this is automated, only run the expire during certain times of the day.
		int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		if(hour > 1 && hour < 6) {
			VolunteersMgmt.expireVolunteers(event, pageId);
		}
		
		//Try to minimize the amount of duplicate queries

		final Volunteers volunteers = Volunteers.findById(pageId);


			System.out.println("");
			final Map<String, Map<Long, ?>> donations = Donation.getTotalAdminDonations(event);
			final boolean isOpen = Event.isEventOpen(event);
			//System.out.println("event "+event);
			//System.out.println("volunteers "+volunteers);
			//System.out.println("isOpen "+isOpen);
			//System.out.println("Event.canParticipate(localUser, isOpen) "+Event.canParticipate(localUser, isOpen));
			//System.out.println("donations.get.pfp "+(Map<Long, Donation.DonationsByPfp>)donations.get("pfp"));
			//System.out.println("donations.get.team"+(Map<Long, Donation.DonationsByTeam>)donations.get("team"));
			return ok(viewVolunteers.render(event, volunteers,
					isOpen, Event.canParticipate(localUser, isOpen),
					Event.canManage(localUser, event),
					(Map<Long, Donation.DonationsByPfp>)donations.get("pfp"),
					(Map<Long, Donation.DonationsByTeam>)donations.get("team")));


	}

	/**
	 * Removes the shift volunteer.
	 * 
	 * @param id
	 *            the id
	 * @param shiftId
	 *            the shift id
	 * @param volunteerId
	 *            the volunteer id
	 * @return the result
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
			@Group(SecurityRole.EVENT_ASSIST) })
	@Transactional
	public static Result removeShiftVolunteer(Event event, Long shiftId,
			Long volunteerId) {
		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		if (!Event.canManage(ControllerUtil.getLocalUser(session()), event)) {
			flash(ControllerUtil.FLASH_DANGER_KEY,
					"The requested event action cannot be completed by the logged in user.");
			return redirect(routes.Application.index());
		}
		final Shift shift = Shift.findById(shiftId);
		if (CollectionUtils.isNotEmpty(shift.volunteerList)) {
			for (final Volunteer volunteer : shift.volunteerList) {
				if (ObjectUtils.compare(volunteer.id, volunteerId) == 0) {
					shift.volunteerList.remove(volunteer);
					shift.update();
					volunteer.delete();
					break;
				}
			}
		}
		return VolunteersMgmt.get(event, shift.volunteers.id);
	}

	/**
	 * Handle the 'new pfp form' submission.
	 * 
	 * @param id
	 *            the id
	 * @return the result
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
			@Group(SecurityRole.EVENT_ASSIST) })
	@SubjectPresent(content = "/login")
	@Transactional
	public static Result save(Event event) {
		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		if (!Event.canManage(ControllerUtil.getLocalUser(session()), event)) {
			flash(ControllerUtil.FLASH_DANGER_KEY,
					"The requested event action cannot be completed by the logged in user.");
			return redirect(routes.Application.index());
		}
		final Form<Volunteers> volunteersForm = form(Volunteers.class)
				.bindFromRequest();
		if (volunteersForm.hasErrors()) {
			return badRequest(createForm.render(event, volunteersForm));
		}
		// User user = ControllerUtil.getLocalUser(session());
		final Volunteers volunteers = volunteersForm.get();
		if(StringUtils.isEmpty(volunteers.title)) {
			volunteers.title = volunteers.name;
		}
		volunteers.content = ControllerUtil.sanitizeText(volunteers.content);
		volunteers.eventid = event.id;
		volunteers.save();
		session(ControllerUtil.FLASH_SUCCESS_KEY, "Volunteers ["
				+ volunteersForm.get().name + "] has been created");
		return EventWorkflowMgmt.update(event);
	}

	/**
	 * Handle the 'new donation form' submission.
	 * 
	 * @param id
	 *            the id
	 * @param shiftId
	 *            the shift id
	 * @return the result
	 */
	@Transactional
	public static Result saveShiftVolunteer(Event event, Long shiftId) {
		System.out.println("within save volunteer");
		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		final Form<Volunteer> volunteerForm = form(Volunteer.class)
				.bindFromRequest();
		if (volunteerForm.hasErrors()) {
			Logger.debug("Has errors {}", volunteerForm.errorsAsJson());
			return badRequest(modalCreateForm
					.render(event, shiftId, volunteerForm));
		}
		final Shift shift = Shift.findById(shiftId);
		if (CollectionUtils.isEmpty(shift.volunteerList)) {
			shift.volunteerList = new ArrayList<Volunteer>();
		}
		final Volunteer volunteer = volunteerForm.get();
		volunteer.active = false;
		volunteer.dateCreated = new Date();
		volunteer.note = ControllerUtil.sanitizeText(volunteer.note);

		shift.volunteerList.add(volunteer);
		shift.update();
		// volunteerForm.get().save();

		final boolean isMailSecured = play.Play.application().configuration()
				.getBoolean("mail.secured");

//==============================start==============15.10.2015======================checking of email==================================//
		boolean isduplicateRegistration = Volunteer.findDuplicateRegistrationOfVolunteerForSameShift(volunteer.firstName, volunteer.lastName, volunteer.email, shiftId);
		System.out.println("isduplicateRegistration :: "+isduplicateRegistration);

		if(isduplicateRegistration == true){
			final Body body1 = 	 new Body(views.txt.volunteers.registration_email.render(
					routes.VolunteersMgmt.activateShiftVolunteer(event, shiftId,
							volunteer.id).absoluteURL(request(), isMailSecured),
					volunteer).toString(), views.html.volunteers.registration_email
					.render(routes.VolunteersMgmt.activateShiftVolunteer(event,
							shiftId, volunteer.id).absoluteURL(request(),
							isMailSecured), volunteer).toString());
			Mailer.getDefaultMailer().sendMail(
					"Volunteer Registration from Scholastic Challenge", body1,
					volunteer.email);

			flash(ControllerUtil.FLASH_SUCCESS_KEY,
					"An email has been sent to ["
							+ volunteerForm.get().email
							+ "] to register as a Volunteer.  This email is already activated as volunteer "
							);
		}else if(isduplicateRegistration == false){
			final Body body = new Body(views.txt.volunteers.verify_email.render(
					routes.VolunteersMgmt.activateShiftVolunteer(event, shiftId,
							volunteer.id).absoluteURL(request(), isMailSecured),
					volunteer).toString(), views.html.volunteers.verify_email
					.render(routes.VolunteersMgmt.activateShiftVolunteer(event,
							shiftId, volunteer.id).absoluteURL(request(),
							isMailSecured), volunteer).toString());

			Mailer.getDefaultMailer().sendMail(
					"Volunteer Activation from Scholastic Challenge", body,
					volunteer.email);

			flash(ControllerUtil.FLASH_SUCCESS_KEY,
					"An email has been sent to ["
							+ volunteerForm.get().email
							+ "] to activate as a Volunteer.  The activation will expire after "
							+ Shift.EXPIRATION_TIME + " hours.");
		}



//==============================start==============15.10.2015======================checking of email==================================//


		return VolunteersMgmt.get(event, shift.volunteers.id);
	}
	//================populateShiftVolunteer===========03.08.2015=================start:<T-257>=============//

	public static Result populateShiftVolunteer(Event event, Long shiftId, Long volunteerId)
	{
		System.out.println("within editShiftVolunteer...volunteerId :: "+volunteerId+"<shiftId>"+shiftId);
		//get the details of volunteer through volunteer id

		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		Volunteer volunteer = Volunteer.findById(volunteerId);
		final Form<Volunteer> volunteerForm = form(Volunteer.class)
				.bindFromRequest().fill(volunteer);
		if (volunteerForm.hasErrors()) {

			System.out.println("within volunteerForm.errors");
			Logger.debug("Has errors {}", volunteerForm.errorsAsJson());
			return badRequest(modalCreateForm
					.render(event, shiftId, volunteerForm));
		}else {

			System.out.println("within else33");
			return ok(modalEditForm.render(event, volunteer,shiftId, volunteerForm));

		}

	}
	//================populateShiftVolunteer===========03.08.2015=================end:<T-257>===============//

//================updateShiftVolunteer===========03.08.2015=================start:<T-257>===============//

	public static Result updateShiftVolunteer(Event event, Long shiftId, Long volunteerId){

		System.out.println("within updateShiftVolunteer..");
		final Form<Volunteer> volunteerForm = form(Volunteer.class)
				.bindFromRequest();
		Volunteer volunteer = Volunteer.findById(volunteerId);
		if (volunteerForm.hasErrors()) {
			return badRequest(modalEditForm.render(event,volunteer,shiftId, volunteerForm));
		}

		Volunteer updatedVolunteer = volunteerForm.get();




		if(!StringUtils.equals(volunteer.firstName,updatedVolunteer.firstName))
		{
			volunteer.firstName = updatedVolunteer.firstName;

		}
		if(!StringUtils.equals(volunteer.lastName,updatedVolunteer.lastName))
		{
			volunteer.lastName = updatedVolunteer.lastName;

		}
		if(!StringUtils.equals(volunteer.email,updatedVolunteer.email))
		{
			volunteer.email = updatedVolunteer.email;

		}
		if(!StringUtils.equals(volunteer.phone,updatedVolunteer.phone))
		{
			volunteer.phone = updatedVolunteer.phone;

		}
		if(!StringUtils.equals(volunteer.mobile,updatedVolunteer.mobile))
		{
			volunteer.mobile = updatedVolunteer.mobile;

		}
		if(!StringUtils.equals(volunteer.note,updatedVolunteer.note))
		{
			volunteer.note = updatedVolunteer.note;

		}


		volunteer.update();

		//saveShiftVolunteer(event, shiftId);
		System.out.println("before calling get  event "+event+" volunteerId "+volunteerId);
		Long pageId = Long.parseLong("1");
		if(volunteerId == pageId) {
			System.out.println("volunteerId == pageId :: "+volunteerId);
			return VolunteersMgmt.get(event, volunteerId);
		}
		else{
			System.out.println("else volunteerId == pageId :: "+pageId);
			return VolunteersMgmt.get(event, pageId);
		}
		//return ok(modalEditForm.render(event, volunteer, shiftId, volunteerForm));
	}

	//================updateShiftVolunteer===========03.08.2015=================end:<T-257>===============//
//==================addShiftVolunteer================04.08.2015========start:<T-258>===================//

	public static Result addShiftVolunteer(Event event, Long shiftId) {
		System.out.println("within addShiftVolunteer");
		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
	//	final Form<Volunteer> volunteerForm = form(Volunteer.class)
		//		.bindFromRequest();
		if (!Event.canManage(ControllerUtil.getLocalUser(session()), event)) {
			flash(ControllerUtil.FLASH_DANGER_KEY,
					"The requested event action cannot be completed by the logged in user.");
			return redirect(routes.Application.index());
		}
		return ok(modalAddForm.render(event, shiftId,form(Volunteer.class)));



	}

//==================addShiftVolunteer================04.08.2015========end:<T-258>===================//
	/**
	 * Handle the 'edit form' submission .
	 * 
	 * @param id
	 *            Id of the pfp to edit
	 * @param pageId
	 *            the page id
	 * @return the result
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
			@Group(SecurityRole.EVENT_ASSIST) })
	@SubjectPresent(content = "/login")
	@Transactional
	public static Result update(Event event, Long pageId) {
		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		if (!Event.canManage(ControllerUtil.getLocalUser(session()), event)) {
			flash(ControllerUtil.FLASH_DANGER_KEY,
					"The requested event action cannot be completed by the logged in user.");
			return redirect(routes.Application.index());
		}
		final Form<Volunteers> volunteersForm = form(Volunteers.class)
				.bindFromRequest();
		if (volunteersForm.hasErrors()) {
			Logger.info("We had errors: {}", volunteersForm.errorsAsJson());
			return badRequest(editForm.render(event, pageId, volunteersForm));
		}
		final Volunteers origVolunteers = Volunteers.findById(pageId);
		Map<Long, Shift> itemMap = Volunteers.getShiftsWithVolunteers(origVolunteers.shifts);
		final Volunteers updatedVolunteers = volunteersForm.get();
		List<Shift> mergedList = new ArrayList<Shift>();
		if(MapUtils.isNotEmpty(itemMap)) {
			for(Shift item: updatedVolunteers.shifts) {
				if(itemMap.containsKey(item.id)) {
					mergedList.add(itemMap.get(item.id));
				} else {
					mergedList.add(item);
				}
			}
			updatedVolunteers.shifts = mergedList;
		}
		updatedVolunteers.content = ControllerUtil
				.sanitizeText(updatedVolunteers.content);
		updatedVolunteers.update(pageId);
		flash(ControllerUtil.FLASH_SUCCESS_KEY,
				"Event Page [" + volunteersForm.get().name
						+ "] has been updated");
		return EventWorkflowMgmt.update(event);
	}
}