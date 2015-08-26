package controllers;

import static play.data.Form.form;

import java.util.List;
import java.util.Map;

import models.Donation;
import models.Event;
import models.Team;
import models.aws.S3File;
import models.security.SecurityRole;
import models.security.User;

import play.Logger;
import play.data.Form;
import play.db.ebean.Transactional;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.teams.createForm;
import views.html.teams.editForm;
import views.html.teams.list;
import views.html.teams.viewTeam;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;

// TODO: Auto-generated Javadoc
/**
 * Manage a database of events.
 */
public class TeamMgmt extends Controller {
	/*
	 * This result directly redirect to application home.
	 */
	/** The go home. */
	public static Result GO_HOME = redirect(routes.TeamMgmt.list(0, "name",
			"asc", "", "name"));

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
		Form<Team> teamsForm = null;
		final Team team = Team.findFirstByEventId(event.id);
		if (team != null) {
			team.name = null;
			team.id = null;
			teamsForm = form(Team.class).fill(team);
			flash(ControllerUtil.FLASH_INFO_KEY,
					"The values for this team have been prefilled with the first team created for this event.");
		} else {
			teamsForm = form(Team.class);
		}
		return ok(createForm.render(event, teamsForm));
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
		final Form<Team> teamsForm = form(Team.class).fill(
				Team.findById(pageId));
		return ok(editForm.render(event, pageId, teamsForm));
	}

	/**
	 * Display the 'edit form' of a existing Pfp.
	 * 
	 * @param id
	 *            Id of the pfp to edit
	 * @param teamId
	 *            the team id
	 * @return the result
	 */
	public static Result get(Event event, Long teamId) {
		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		final User localUser = ControllerUtil.getLocalUser(session());

		if(!Event.isLive(event) && (localUser == null || !ControllerUtil.isEqual(event.userAdmin.id, localUser.id))) {
			flash(ControllerUtil.FLASH_WARNING_KEY,
					"This event is not published yet and therefore is not available at this time.");
			return ok(views.html.index.render());
		}
		final Team team = Team.findById(teamId);
		final Map<String, Map<Long, ?>> donations = Donation.getTotalAdminDonations(event, team);
		final boolean isOpen = Event.isEventOpen(event);
		
		return ok(viewTeam.render(event, team,
				isOpen,
				Event.canParticipate(localUser, isOpen),
				Event.canManage(localUser, event),
				(Map<Long, Donation.DonationsByPfp>)donations.get("pfp"),
				(Map<Long, Donation.DonationsByTeam>)donations.get("team")));
	}

	public static String getUrl(Team team) {
		return routes.TeamMgmt.get(Event.findById(team.eventid), team.id).url();
	}
	
	/**
	 * Handle default path requests, redirect to computers list.
	 * 
	 * @return the result
	 */
	public static Result index() {
		return GO_HOME;
	}

	/**
	 * Display the paginated list of events.
	 * 
	 * @param page
	 *            Current page number (starts from 0)
	 * @param sortBy
	 *            Column to be sorted
	 * @param order
	 *            Sort order (either asc or desc)
	 * @param filter
	 *            Filter applied on event names
	 * @return the result
	 */
	@SubjectPresent(content = "/login")
	public static Result list(int page, String sortBy, String order,
			String filter, String fieldName) {
		if(true) {
			return redirect(routes.Application.profile());
		}
		return ok(list.render(Team.page(page, 10, sortBy, order, filter, fieldName),
				sortBy, order, filter));
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
		final Form<Team> teamsForm = form(Team.class).bindFromRequest();
		if (teamsForm.hasErrors()) {
			return badRequest(createForm.render(event, teamsForm));
		}
		final boolean saveAndAdd = Boolean.valueOf(teamsForm
				.field("saveAndAdd").value());
		final Http.MultipartFormData body = request().body()
				.asMultipartFormData();
		final Http.MultipartFormData.FilePart imgUrlFilePart = body
				.getFile("imgUrl");
		S3File imgUrlFile = null;
		if (imgUrlFilePart != null) {
			if (!ControllerUtil.isImage(imgUrlFilePart.getFilename())) {
				teamsForm.reject("imgUrl", ControllerUtil.IMAGE_ERROR_MSG);
			} else if (ControllerUtil.isFileTooLarge(imgUrlFilePart.getFile())) {
				teamsForm.reject("imgUrl", ControllerUtil.IMAGE_SIZE_ERROR_MSG);
			} else {
				imgUrlFile = new S3File();
				imgUrlFile.name = ControllerUtil.decodeFileName(imgUrlFilePart
						.getFilename());
				imgUrlFile.file = imgUrlFilePart.getFile();
				teamsForm.get().imgUrl = imgUrlFile.getUrl();
			}
		}
		if (teamsForm.hasErrors()) {
			return badRequest(createForm.render(event, teamsForm));
		}
		final Team team = teamsForm.get();
		team.content = ControllerUtil.sanitizeText(team.content);
		if (imgUrlFile != null) {
			imgUrlFile.save();
		}
		team.eventid = event.id;
		team.save();
		Logger.debug("About to save a team status");
		List<Team> eventTeams = Team.findByEventId(team.eventid);
		if (eventTeams != null && eventTeams.size() > 0) {
			Logger.debug("Found event");
			if (event.status == Event.PublishStatus.NEW) {
				Logger.debug("Change event to Publish because it was Default and now has a team");
				event.status = Event.PublishStatus.LIVE;
				event.update(event.id);
			}
		}
		session(ControllerUtil.FLASH_SUCCESS_KEY, "Team ["
				+ teamsForm.get().name + "] has been created");
		if (saveAndAdd) {
			return redirect(routes.TeamMgmt.create(event));
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
		final Form<Team> teamsForm = form(Team.class).bindFromRequest();
		if (teamsForm.hasErrors()) {
			Logger.debug("We had errors: {}", teamsForm.errorsAsJson());
			return badRequest(editForm.render(event, pageId, teamsForm));
		}
		final Http.MultipartFormData body = request().body()
				.asMultipartFormData();
		final Http.MultipartFormData.FilePart imgUrlFilePart = body
				.getFile("imgUrl");
		S3File imgUrlFile = null;
		if (imgUrlFilePart != null) {
			if (!ControllerUtil.isImage(imgUrlFilePart.getFilename())) {
				teamsForm.reject("imgUrl", ControllerUtil.IMAGE_ERROR_MSG);
			} else if (ControllerUtil.isFileTooLarge(imgUrlFilePart.getFile())) {
				teamsForm.reject("imgUrl", ControllerUtil.IMAGE_SIZE_ERROR_MSG);
			} else {
				imgUrlFile = new S3File();
				imgUrlFile.name = ControllerUtil.decodeFileName(imgUrlFilePart
						.getFilename());
				imgUrlFile.file = imgUrlFilePart.getFile();
				teamsForm.get().imgUrl = imgUrlFile.getUrl();
			}
		}
		if (teamsForm.hasErrors()) {
			Logger.debug("We had errors: {}", teamsForm.errorsAsJson());
			return badRequest(editForm.render(event, pageId, teamsForm));
		}
		final Team updatedTeam = teamsForm.get();
		updatedTeam.content = ControllerUtil.sanitizeText(updatedTeam.content);
		//TODO this was also incorrectly pointing to the event id and not the team id
		final Team team = Team.findById(pageId);
		if (imgUrlFile != null) {
			if(team.imgUrl != null) {
				S3File.delete(team.imgUrl);
			}
			imgUrlFile.save();
		}
		updatedTeam.update(pageId);
		flash(ControllerUtil.FLASH_SUCCESS_KEY,
				"Event Page [" + teamsForm.get().name + "] has been updated");
		return EventWorkflowMgmt.update(event);
	}

}
