package controllers;

import static play.data.Form.form;

import java.util.*;

import models.*;
import models.Pfp.PfpType;
import models.aws.S3File;
import models.security.SecurityRole;
import models.security.User;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import com.feth.play.module.mail.Mailer;
import com.feth.play.module.mail.Mailer.Mail.Body;
import com.typesafe.config.ConfigFactory;

import play.Logger;
import play.data.Form;
import play.db.ebean.Transactional;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.pfps.createForm;
import views.html.pfps.editForm;
import views.html.pfps.list;
import views.html.pfps.viewPfp;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;

// TODO: Auto-generated Javadoc
/**
 * Manage a database of pfps.
 */
public class PfpMgmt extends Controller {

	/**
	 * This result directly redirect to application home.
	 */
	public static Result GO_HOME = redirect(routes.PfpMgmt.list(0, "name",
			"asc", ""));

	/**
	 * Display the 'new pfp form'.
	 * 
	 * @param id
	 *            the id
	 * @return the result
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.PFP_ADMIN),
			@Group(SecurityRole.EVENT_ADMIN) })
	@SubjectPresent(content = "/login")
	public static Result create(Event event) {
		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		final Form<Pfp> pfpForm = form(Pfp.class);
		return ok(createForm.render(event, pfpForm));
	}

	/**
	 * Handle default path requests, redirect to computers list.
	 * 
	 * @return the result
	 */
	public static Result donateList() {
		flash(ControllerUtil.FLASH_INFO_KEY,
				"Search for a participant page to submit a donation on behalf of the individual.");
		return GO_HOME;
	}

	/**
	 * Display the 'edit form' of a existing Pfp.
	 * 
	 * @param id
	 *            Id of the pfp to edit
	 * @param pfpId
	 *            the pfp id
	 * @return the result
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.PFP_ADMIN),
			@Group(SecurityRole.EVENT_ADMIN) })
	@SubjectPresent(content = "/login")
	public static Result edit(Event event, Pfp pfp) {
		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		if (pfp.isIdOnly()) {
			pfp = Pfp.findById(pfp.id);
		}
		if (!Pfp.canManage(ControllerUtil.getLocalUser(session()), event, pfp)) {
			flash(ControllerUtil.FLASH_DANGER_KEY,
					"The requested event action cannot be completed by the logged in user.");
			return redirect(routes.Application.index());
		}
		final Form<Pfp> pfpForm = form(Pfp.class).fill(pfp);
		return ok(editForm.render(event, pfp, pfpForm));
	}

	public static Result getById(Event event, Long pfpId) {
		event = Event.findByIdWithMin(event.id);
		Pfp pfp = Pfp.findById(pfpId);
		return redirect(routes.PfpMgmt.get(event, pfp));
	}
	
	/**
	 * Display the 'edit form' of a existing Pfp.
	 * 
	 * @param id
	 *            Id of the pfp to edit
	 * @param pfpId
	 *            the pfp id
	 * @return the result
	 */
	public static Result get(Event event, Pfp pfp) {
		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		if (pfp.isIdOnly()) {
			pfp = Pfp.findById(pfp.id);
		}
		final List<Donation> donations = Donation.findByPfpId(pfp.id);
		final Map<Long, Donation.DonationsByTeam> teamDonations = Donation
				.getTotalTeamDonations(event.id);









		//===========================uploadimage and web url=======================10.09.2015======================start==============================//


		System.out.println("within view event ::event id ----> "+event.id);
		final Sponsors sponsors = Sponsors.findByEventId(event.id);
		List<Donation> donationList = (List<Donation>)Donation.findAllByEventId(event.id);
		Iterator<Donation> iterator=donationList.iterator();
		List<Donation> donationList1 = new ArrayList();
		while(iterator.hasNext()){
			Donation donation=iterator.next();

			SponsorItem sponsorItemFromSponsors;
			if((sponsors.sponsoritems!=null&& sponsors.sponsoritems.size()>0) && donation.sponsorItem!=null){
				for(int i = 0 ; i<sponsors.sponsoritems.size();i++){
					sponsorItemFromSponsors= sponsors.sponsoritems.get(i);

					if( donation.sponsorItem.id.equals(sponsorItemFromSponsors.id) ){
						System.out.println("checkbox for :: "+donation.sponsorItem.title+" sponsor item logo "+sponsorItemFromSponsors.logo);

						System.out.println("donation.sponsorItem.logo :: "+donation.sponsorItem.logo);
						if(donation.sponsorItem.logo == true){

							//============web url checking======start============07.09.2015========================//
							System.out.println("iffff  " + donation.imgUrl + "<for>" + donation.sponsorItem.title);
							if(donation.sponsorItem.webLogo == true){
								donationList1.add(donation);
							}else{
								donation.webUrl = null;
								donationList1.add(donation);
							}
							//============web url checking=======end=============07.09.2015========================//

						}else {
							System.out.println(" elseee " + donation.imgUrl + "<for>"+donation.sponsorItem.title);
							/*donationList1.add(donation);*/
						}

					}
				}
			}

		}


		int imgUrl=0;

		for(Donation donation:donationList1){
			if(donation.imgUrl!=null){
				imgUrl++;
			}

		}



	/*	return ok(viewTeam.render(event, team,
				isOpen,
				Event.canParticipate(localUser, isOpen),
				Event.canManage(localUser, event),
				(Map<Long, Donation.DonationsByPfp>)donations.get("pfp"),
				(Map<Long, Donation.DonationsByTeam>)donations.get("team"),
				donationList1,
				imgUrl));
*/


//===========================uploadimage and web url=======================10.09.2015======================end==============================//






		User localUser = ControllerUtil.getLocalUser(session());
		if(Logger.isDebugEnabled()) {
			if(pfp == null) {
				Logger.warn("The pfp you are trying to get is null");
			} else {
				Logger.debug("Can user manage the PFP named {}", pfp.name);
				Logger.debug("The pfp user id is {}", pfp.id);
				if(localUser != null) {
					Logger.debug("The pfp users admin is {} with id {}", pfp.userAdmin.firstName + " " + pfp.userAdmin.lastName, pfp.userAdmin.id);
					Logger.debug("The session user is {} with id {}", localUser.firstName + " " + localUser.lastName, localUser.id);
					Logger.debug("What does Pfp.canManage = {}", Pfp.canManage(localUser, event, pfp));
					Logger.debug("Is user a pfp admin {}", ControllerUtil.isUserInRole(SecurityRole.PFP_ADMIN, localUser));
				}
			}
		}
		/*return ok(viewPfp.render(event, pfp, donations, teamDonations, Event
				.isEventOpen(event), Pfp.canManage(
						localUser, event, pfp)));*/

		return ok(viewPfp.render(event, pfp, donations, teamDonations, Event
				.isEventOpen(event), Pfp.canManage(
				localUser, event, pfp),donationList1,imgUrl));


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
			String filter) {
		if(true) {
			return redirect(routes.Application.profile());
		}
		return ok(list.render(Pfp.page(page, 10, sortBy, order, filter),
				sortBy, order, filter));
	}
	
	/**
	 * Display the 'edit form' of a existing Pfp.
	 * 
	 * @param id
	 *            Id of the pfp to edit
	 * @param pfpId
	 *            the pfp id
	 * @return the result
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.PFP_ADMIN),
			@Group(SecurityRole.EVENT_ADMIN) })
	@SubjectPresent(content = "/login")
	public static Result remove(Event event, Pfp pfp) {
		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		if (pfp.isIdOnly()) {
			pfp = Pfp.findById(pfp.id);
		}
		if (!Pfp.canManage(ControllerUtil.getLocalUser(session()), event, pfp)) {
			flash(ControllerUtil.FLASH_DANGER_KEY,
					"The requested event action cannot be completed by the logged in user.");
			return redirect(routes.Application.profilePfps());
		}
		if (Donation.existsByPfpId(pfp.id)) {
			flash(ControllerUtil.FLASH_INFO_KEY,
					"The requested pfp cannot be removed because donations exist for this account.");
			routes.Application.profilePfps();
		}
		flash(ControllerUtil.FLASH_INFO_KEY,
				"The requested pfp [" + pfp.name + "] has been removed from the system.");
		pfp.delete();
		return redirect(routes.Application.profilePfps());
	}

	/**
	 * Handle the 'new pfp form' submission.
	 * 
	 * @param id
	 *            the id
	 * @return the result
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.PFP_ADMIN),
			@Group(SecurityRole.EVENT_ADMIN) })
	@SubjectPresent(content = "/login")
	@Transactional
	public static Result save(Event event) {
		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		final Form<Pfp> pfpForm = form(Pfp.class).bindFromRequest();
		if (StringUtils.isEmpty(pfpForm.data().get("team.id"))) {
			pfpForm.reject("team.id", "You must select a team for your participant.");
		}
		if (pfpForm.hasErrors()) {
			return badRequest(createForm.render(event, pfpForm));
		}
		final boolean saveAndAdd = Boolean.valueOf(pfpForm.field("saveAndAdd")
				.value());
		final Http.MultipartFormData body = request().body()
				.asMultipartFormData();
		final Http.MultipartFormData.FilePart imgUrlFilePart = body
				.getFile("imgUrl");
		S3File imgUrlFile = null;
		if (imgUrlFilePart != null) {
			if (!ControllerUtil.isImage(imgUrlFilePart.getFilename())) {
				pfpForm.reject("imgUrl", ControllerUtil.IMAGE_ERROR_MSG);
			} else if (ControllerUtil.isFileTooLarge(imgUrlFilePart.getFile())) {
				pfpForm.reject("imgUrl", ControllerUtil.IMAGE_SIZE_ERROR_MSG);
			} else {
				imgUrlFile = new S3File();
				imgUrlFile.name = ControllerUtil.decodeFileName(imgUrlFilePart
						.getFilename());
				imgUrlFile.file = imgUrlFilePart.getFile();
				pfpForm.get().imgUrl = imgUrlFile.getUrl();
			}
		}
		if (pfpForm.hasErrors()) {
			return badRequest(createForm.render(event, pfpForm));
		}
		final Pfp pfp = pfpForm.get();
		if (pfp.userAdmin == null) {
			pfp.userAdmin = ControllerUtil.getLocalUser(session());
		} else if ((pfp.userAdmin.id == null) && (pfp.userAdmin.email != null)) {
			Logger.debug("Created by Sys Admin, so must find Pfp Admin by email");
			final User user = User.findByEmail(pfp.userAdmin.email);
			if (user == null) {
				pfpForm.reject("userAdmin.email",
						"A user account does not exist for the supplied email.");
				return badRequest(createForm.render(event, pfpForm));
			}
			pfp.userAdmin = user;
		}
		if (Pfp.existsAlready(pfpForm.get(), event)) {
			pfpForm.reject("name", "It appears you are attempting to create a duplicate participant.  If you need help with your original participant page, please let contact Scholastic Challenge.");
			return badRequest(createForm.render(event, pfpForm));
		}
		pfp.title = "Event - " + pfp.name;
		pfp.emergencyContactPhone = ControllerUtil.stripPhone(pfp.emergencyContactPhone);
		pfp.content = ControllerUtil.sanitizeText(pfp.content);
		pfp.pfpType = PfpType.PFP;
		pfp.dateCreated = new Date();
		pfp.event = event;
		if (imgUrlFile != null) {
			imgUrlFile.save();
		}
		pfp.save();
		Pfp updatedPfp = Pfp.findById(pfp.id);
		updatedPfp.slug = Pfp.toSlug(updatedPfp);
		updatedPfp.update();
		PfpMgmt.generatePfpCreate(updatedPfp, ConfigFactory.load().getString("base.url") + routes.PfpMgmt.get(updatedPfp.event, updatedPfp).url());
		flash(ControllerUtil.FLASH_SUCCESS_KEY, "Pfp [" + pfpForm.get().name
				+ "] has been created");
		if (saveAndAdd) {
			return redirect(routes.PfpMgmt.create(event));
		}
		return redirect(routes.PfpMgmt.get(event, updatedPfp));
	}

	/**
	 * Handle the 'edit form' submission .
	 * 
	 * @param id
	 *            Id of the pfp to edit
	 * @param pfpId
	 *            the pfp id
	 * @return the result
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.PFP_ADMIN),
			@Group(SecurityRole.EVENT_ADMIN) })
	@SubjectPresent(content = "/login")
	@Transactional
	public static Result update(Event event, Pfp pfp) {
		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		if (pfp.isIdOnly()) {
			pfp = Pfp.findById(pfp.id);
		}
		if (!Pfp.canManage(ControllerUtil.getLocalUser(session()), event, pfp)) {
			flash(ControllerUtil.FLASH_DANGER_KEY,
					"The requested event action cannot be completed by the logged in user.");
			return redirect(routes.Application.index());
		}
		final Form<Pfp> pfpForm = form(Pfp.class).bindFromRequest();
		if (pfpForm.hasErrors()) {
			Logger.info("We had errors: {}", pfpForm.errorsAsJson());
			return badRequest(editForm.render(event, pfp, pfpForm));
		}
		final Http.MultipartFormData body = request().body()
				.asMultipartFormData();
		final Http.MultipartFormData.FilePart imgUrlFilePart = body
				.getFile("imgUrl");
		S3File imgUrlFile = null;
		if (imgUrlFilePart != null) {
			if (!ControllerUtil.isImage(imgUrlFilePart.getFilename())) {
				pfpForm.reject("imgUrl", ControllerUtil.IMAGE_ERROR_MSG);
			} else if (ControllerUtil.isFileTooLarge(imgUrlFilePart.getFile())) {
				pfpForm.reject("imgUrl", ControllerUtil.IMAGE_SIZE_ERROR_MSG);
			} else {
				imgUrlFile = new S3File();
				imgUrlFile.name = ControllerUtil.decodeFileName(imgUrlFilePart
						.getFilename());
				imgUrlFile.file = imgUrlFilePart.getFile();
				pfpForm.get().imgUrl = imgUrlFile.getUrl();
			}
		}
		if (pfpForm.hasErrors()) {
			Logger.info("We had errors: {}", pfpForm.errorsAsJson());
			return badRequest(editForm.render(event, pfp, pfpForm));
		}
		final Pfp updatedPfp = pfpForm.get();
		updatedPfp.content = ControllerUtil.sanitizeText(updatedPfp.content);
		if (imgUrlFile != null) {
			if(pfp.imgUrl != null) {
				S3File.delete(pfp.imgUrl);
			}
			imgUrlFile.save();
		}
		updatedPfp.emergencyContactPhone = ControllerUtil.stripPhone(updatedPfp.emergencyContactPhone);
		updatedPfp.update(pfp.id);
		flash(ControllerUtil.FLASH_SUCCESS_KEY, "Pfp [" + pfpForm.get().name
				+ "] has been updated");
		return PfpMgmt.get(event, Pfp.findById(pfp.id));
	}

	/**
	 * Handle the 'edit form' submission .
	 * 
	 * @param id
	 *            Id of the pfp to edit
	 * @param pfpId
	 *            the pfp id
	 * @return the result
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.PFP_ADMIN),
			@Group(SecurityRole.EVENT_ADMIN) })
	@SubjectPresent(content = "/login")
	@Transactional
	public static Result updateInline(Event event, Pfp pfp) {
		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		if (pfp.isIdOnly()) {
			pfp = Pfp.findById(pfp.id);
		}
		if (!Pfp.canManage(ControllerUtil.getLocalUser(session()), event, pfp)) {
			flash(ControllerUtil.FLASH_DANGER_KEY,
					"The requested event action cannot be completed by the logged in user.");
			return redirect(routes.Application.index());
		}
//		final User user = ControllerUtil.getLocalUser(session());
//		Logger.debug(ToStringBuilder.reflectionToString(user));
		final Form<Pfp> pfpForm = form(Pfp.class).bindFromRequest();
		final Map<String, String> formMap = pfpForm.data();
		if(StringUtils.isEmpty(formMap.get("goal"))) {
			flash(ControllerUtil.FLASH_DANGER_KEY,
							"The updated goal amount was empty, it is a required field.");
			return Application.profilePfps();
		}
		boolean updated = false;
		if (formMap.containsKey("private") && (Boolean.compare(pfp.privateAcct, Boolean.parseBoolean(formMap.get("private"))) != 0)) {
			pfp.privateAcct = new Boolean(formMap.get("private"));
			updated = true;
		}
		if (formMap.containsKey("goal") && pfp.goal != Integer.parseInt(formMap.get("goal"))) {
			pfp.goal = Integer.parseInt(formMap.get("goal"));
			updated = true;
		}
		if (formMap.containsKey("team.id") && ObjectUtils.compare(pfp.team.id, Long.parseLong(formMap.get("team.id"))) != 0) {
			Logger.debug("{} {}", pfp.team.id, formMap.get("team.id"));
			pfp.team = Team.findById(Long.parseLong(formMap.get("team.id")));
			updated = true;
		}
		if(updated) {
			pfp.update(pfp.id);
			flash(ControllerUtil.FLASH_SUCCESS_KEY, "Pfp [" + pfp.name
					+ "] has been updated");
		} else {
			flash(ControllerUtil.FLASH_INFO_KEY, "No changes were made to Pfp [" + pfp.name
							+ "]");
		}
		return redirect(routes.Application.profileSearchPfps(0, "name",
						"asc", "", "name"));
	}

	private static void generatePfpCreate(Pfp pfp, String url) {
		String content = views.txt.pfps.pfp_created_msg
				.render(pfp, url).toString();
		final Body body = new Body(content);

		Mailer.getDefaultMailer().sendMail("Scholastic Challenge - PFP Page Created", body,
				pfp.userAdmin.email);
	}
}
