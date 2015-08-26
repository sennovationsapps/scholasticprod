package controllers;

import static play.data.Form.form;
import views.html.pages.createForm;
import views.html.pages.editForm;
import views.html.pages.viewEventPage;

import java.util.Map;

import models.Donation;
import models.Event;
import models.EventPages;
import models.security.SecurityRole;
import models.security.User;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.data.Form;
import play.db.ebean.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;

// TODO: Auto-generated Javadoc
/**
 * Manage a database of events.
 */
public class EventPagesMgmt extends Controller {

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
		final Form<EventPages> eventPagesForm = form(EventPages.class);
		return ok(createForm.render(event, eventPagesForm));
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
		final Form<EventPages> eventPagesForm = form(EventPages.class).fill(
				EventPages.findById(pageId));
		return ok(editForm.render(event, pageId, eventPagesForm));
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
		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		final User localUser = ControllerUtil.getLocalUser(session());
		if(!Event.isLive(event) && (localUser == null || !ControllerUtil.isEqual(event.userAdmin.id, localUser.id))) {
			flash(ControllerUtil.FLASH_WARNING_KEY,
					"This event is not published yet and therefore is not available at this time.");
			return ok(views.html.index.render());
		}
		final EventPages eventPage = EventPages.findById(pageId);
		final Map<String, Map<Long, ?>> donations = Donation.getTotalAdminDonations(event);
		final boolean isOpen = Event.isEventOpen(event);
		return ok(viewEventPage.render(event, eventPage,
				isOpen,
				Event.canParticipate(localUser, isOpen),
				Event.canManage(localUser, event),
				(Map<Long, Donation.DonationsByPfp>)donations.get("pfp"),
				(Map<Long, Donation.DonationsByTeam>)donations.get("team")));
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
		final Form<EventPages> eventPagesForm = form(EventPages.class)
				.bindFromRequest();
		if (eventPagesForm.hasErrors()) {
			return badRequest(createForm.render(event, eventPagesForm));
		}
		final boolean saveAndAdd = Boolean.valueOf(eventPagesForm.field(
				"saveAndAdd").value());
		// User user = ControllerUtil.getLocalUser(session());
		final EventPages eventPages = eventPagesForm.get();
		eventPages.content = ControllerUtil.sanitizeText(eventPages.content);
		if(StringUtils.isEmpty(eventPages.title)) {
			eventPages.title = eventPages.name;
		}
		eventPages.eventid = event.id;
		eventPages.save();
		session(ControllerUtil.FLASH_SUCCESS_KEY, "EventPages ["
				+ eventPagesForm.get().name + "] has been created");
		if (saveAndAdd) {
			return redirect(routes.EventPagesMgmt.create(event));
		}
		return EventWorkflowMgmt.update(event);
	}

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
		final Form<EventPages> eventPagesForm = form(EventPages.class)
				.bindFromRequest();
		if (eventPagesForm.hasErrors()) {
			Logger.info("We had errors: {}", eventPagesForm.errorsAsJson());
			return badRequest(editForm.render(event, pageId, eventPagesForm));
		}
		final EventPages updatedEventPages = eventPagesForm.get();
		updatedEventPages.content = ControllerUtil
				.sanitizeText(updatedEventPages.content);

		updatedEventPages.update(pageId);
		flash(ControllerUtil.FLASH_SUCCESS_KEY,
				"Event Page [" + eventPagesForm.get().name
						+ "] has been updated");
		return EventWorkflowMgmt.update(event);
	}

}
