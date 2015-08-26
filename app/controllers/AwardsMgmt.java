package controllers;

import static play.data.Form.form;

import java.util.Map;

import models.Awards;
import models.Donation;
import models.Event;
import models.Prize;
import models.aws.S3File;
import models.security.SecurityRole;
import models.security.User;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import play.Logger;
import play.data.Form;
import play.db.ebean.Transactional;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.awards.createForm;
import views.html.awards.editForm;
import views.html.awards.viewAwards;
import base.utils.DebugUtils;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;

/**
 * Manage a database of events.
 */
public class AwardsMgmt extends Controller {
	
	/**
	 * Display the 'new pfp form'.
	 *
	 * @param id the id
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
		return ok(createForm.render(event, form(Awards.class)));
	}

	/**
	 * Display the 'edit form' of a existing Pfp.
	 *
	 * @param id            Id of the pfp to edit
	 * @param pageId the page id
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
		final Form<Awards> awardsForm = form(Awards.class).fill(
				Awards.findById(pageId));
		return ok(editForm.render(event, pageId, awardsForm));
	}

	/**
	 * Display the 'edit form' of a existing Pfp.
	 *
	 * @param id            Id of the pfp to edit
	 * @param pageId the page id
	 * @return the result
	 */
	public static Result get(Event event, Long pageId) {
		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		final User localUser = ControllerUtil.getLocalUser(session());
		final Awards awards = Awards.findById(pageId);

		final Map<String, Map<Long, ?>> donations = Donation.getTotalAdminDonations(event);
		final boolean isOpen = Event.isEventOpen(event);
		return ok(viewAwards.render(event, awards,
				isOpen,
				Event.canParticipate(localUser, isOpen),
				Event.canManage(localUser, event),
				(Map<Long, Donation.DonationsByPfp>)donations.get("pfp"),
				(Map<Long, Donation.DonationsByTeam>)donations.get("team")));
	}

	/**
	 * Handle the 'new pfp form' submission.
	 *
	 * @param id the id
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
		Form<Awards> awardsForm = form(Awards.class).bindFromRequest();
		if (awardsForm.hasErrors()) {
			return badRequest(createForm.render(event, awardsForm));
		}
		final Http.MultipartFormData body = request().body()
				.asMultipartFormData();

		final Http.MultipartFormData.FilePart imgUrlFilePart = body
				.getFile("imgUrl");
		S3File imgUrlFile = null;
		if (imgUrlFilePart != null) {
			Logger.debug("Found imgUrl in request");
			if (!ControllerUtil.isImage(imgUrlFilePart.getFilename())) {
				awardsForm.reject("imgUrl", ControllerUtil.IMAGE_ERROR_MSG);
			} else if (ControllerUtil.isFileTooLarge(imgUrlFilePart.getFile())) {
				awardsForm
						.reject("imgUrl", ControllerUtil.IMAGE_SIZE_ERROR_MSG);
			} else {
				imgUrlFile = new S3File();
				imgUrlFile.name = ControllerUtil.decodeFileName(imgUrlFilePart
						.getFilename());
				imgUrlFile.file = imgUrlFilePart.getFile();
				awardsForm.data().put(imgUrlFilePart.getKey(),
						imgUrlFile.getUrl().toString());
			}
		}

		if (awardsForm.hasErrors()) {
			return badRequest(createForm.render(event, awardsForm));
		}
		awardsForm = form(Awards.class).bind(awardsForm.data());
		final Awards awards = awardsForm.get();
		if (StringUtils.isEmpty(awards.title)) {
			awards.title = awards.name;
		}
		awards.content = ControllerUtil.sanitizeText(awards.content);
		if(CollectionUtils.isNotEmpty(awards.prizes)) {
			for(Prize prize: awards.prizes) {
				if(StringUtils.isNotEmpty(prize.info)) {
					prize.info = ControllerUtil.sanitizeText(prize.info);
				}
			}
		}
		if (imgUrlFile != null) {
			imgUrlFile.save();
		}
		awards.eventid = event.id;
		awards.save();
		session(ControllerUtil.FLASH_SUCCESS_KEY, "Awards ["
				+ awardsForm.get().name + "] has been created");
		return EventWorkflowMgmt.update(event);
	}

	/**
	 * Handle the 'edit form' submission.
	 *
	 * @param id            Id of the pfp to edit
	 * @param pageId the page id
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
		Form<Awards> awardsForm = form(Awards.class).bindFromRequest();
		if (awardsForm.hasErrors()) {
			return badRequest(editForm.render(event, pageId, awardsForm));
		}
		final Http.MultipartFormData body = request().body()
				.asMultipartFormData();
		final Http.MultipartFormData.FilePart imgUrlFilePart = body
				.getFile("imgUrl");
		S3File imgUrlFile = null;
		if (imgUrlFilePart != null) {
			if (!ControllerUtil.isImage(imgUrlFilePart.getFilename())) {
				awardsForm.reject("imgUrl", ControllerUtil.IMAGE_ERROR_MSG);
			} else if (ControllerUtil.isFileTooLarge(imgUrlFilePart.getFile())) {
				awardsForm
						.reject("imgUrl", ControllerUtil.IMAGE_SIZE_ERROR_MSG);
			} else {
				Logger.debug("Found imgUrl in request");
				imgUrlFile = new S3File();
				imgUrlFile.name = ControllerUtil.decodeFileName(imgUrlFilePart
						.getFilename());
				imgUrlFile.file = imgUrlFilePart.getFile();
				awardsForm.data().put(imgUrlFilePart.getKey(),
						imgUrlFile.getUrl().toString());
				// awardsForm.get().imgUrl = imgUrlFile.getUrl();
			}
		}
		if (awardsForm.hasErrors()) {
			Logger.info("We had errors: {}", awardsForm.errorsAsJson());
			return badRequest(editForm.render(event, pageId, awardsForm));
		}
		awardsForm = form(Awards.class).bind(awardsForm.data());
		final Awards updatedAwards = awardsForm.get();
		updatedAwards.content = ControllerUtil
				.sanitizeText(updatedAwards.content);
		if(CollectionUtils.isNotEmpty(updatedAwards.prizes)) {
			for(Prize prize: updatedAwards.prizes) {
				if(StringUtils.isNotEmpty(prize.info)) {
					prize.info = ControllerUtil.sanitizeText(prize.info);
				}
			}
		}
		//TODO this was messed up with event id and not award id
		final Awards awards = Awards.findById(pageId);
		if (imgUrlFile != null) {
			if (awards.imgUrl != null) {
				S3File.delete(awards.imgUrl);
			}
			imgUrlFile.save();
		}
		updatedAwards.update(pageId);
		flash(ControllerUtil.FLASH_SUCCESS_KEY,
				"Event Page [" + awardsForm.get().name + "] has been updated");
		return EventWorkflowMgmt.update(event);
	}

}
