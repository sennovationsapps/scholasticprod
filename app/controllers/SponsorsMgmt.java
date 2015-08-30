package controllers;

import static play.data.Form.form;
import views.html.sponsors.createForm;
import views.html.sponsors.editForm;
import views.html.sponsors.viewSponsors;

import java.util.*;

import models.Donation;
import models.Event;
import models.SponsorItem;
import models.Sponsors;
import models.security.SecurityRole;
import models.security.User;

import org.apache.commons.collections.MapUtils;
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
public class SponsorsMgmt extends Controller {

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
		return ok(createForm.render(event, form(Sponsors.class)));
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
		final Form<Sponsors> sponsorsForm = form(Sponsors.class).fill(
				Sponsors.findById(pageId));
		return ok(editForm.render(event, pageId, sponsorsForm));
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
		final Sponsors sponsors = Sponsors.findById(pageId);




		List<SponsorItem> previousItemList= sponsors.findByEventId(event.id).sponsoritems;

		System.out.println("SIZE OF SPONSOR LIST-------------------------"+previousItemList.size());
		if(previousItemList!= null &&previousItemList.size()>0){
			System.out.println("sponsors.previousItemList.size() :: "+previousItemList.size());
			sponsors.sponsoritems = previousItemList;

		}


		final Map<String, Map<Long, ?>> donations = Donation.getTotalAdminDonations(event);
		final boolean isOpen = Event.isEventOpen(event);
		ControllerUtil.convertSessionMessages();
		return ok(viewSponsors.render(event, sponsors,
				isOpen, Event.canParticipate(localUser, isOpen),
				Event.canManage(localUser, event),
				(Map<Long, Donation.DonationsByPfp>)donations.get("pfp"),
				(Map<Long, Donation.DonationsByTeam>)donations.get("team")));
	}

//	/**
//	 * Removes the item sponsor.
//	 * 
//	 * @param id
//	 *            the id
//	 * @param itemId
//	 *            the item id
//	 * @return the result
//	 */
//	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
//			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
//			@Group(SecurityRole.EVENT_ASSIST) })
//	public static Result removeItemSponsor(Event event, Long itemId) {
//		final SponsorItem item = SponsorItem.findById(itemId);
//		final Sponsor sponsor = item.sponsor;
//		item.sponsor = null;
//		item.update();
//		sponsor.delete();
//		return SponsorsMgmt.get(event, item.sponsors.id);
//	}

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
		final Form<Sponsors> sponsorsForm = form(Sponsors.class)
				.bindFromRequest();
		if (sponsorsForm.hasErrors()) {
			return badRequest(createForm.render(event, sponsorsForm));
		}
		// User user = ControllerUtil.getLocalUser(session());
		final Sponsors sponsors = sponsorsForm.get();
		if(StringUtils.isEmpty(sponsors.title)) {
			sponsors.title = sponsors.name;
		}
		sponsors.content = ControllerUtil.sanitizeText(sponsors.content);
		sponsors.eventid = event.id;
		sponsors.save();
		session(ControllerUtil.FLASH_SUCCESS_KEY,
				"Sponsors [" + sponsorsForm.get().name + "] has been created");
		return EventWorkflowMgmt.update(event);
	}

//	/**
//	 * Handle the 'new donation form' submission.
//	 * 
//	 * @param id
//	 *            the id
//	 * @param itemId
//	 *            the item id
//	 * @return the result
//	 */
//	public static Result saveItemSponsor(Event event, Long itemId) {
//		final Form<Sponsor> sponsorForm = form(Sponsor.class).bindFromRequest();
//		final SponsorItem sponsorItem = SponsorItem.findById(itemId);
//
//		if (sponsorForm.hasErrors()) {
//			Logger.debug("Has errors {}", sponsorForm.errorsAsJson());
//			return badRequest(modalCreateForm.render(event, itemId, sponsorForm));
//		}
//		final Sponsor sponsor = sponsorForm.get();
//		sponsor.active = false;
//		sponsor.dateCreated = new Date();
//		sponsor.note = ControllerUtil.sanitizeText(sponsor.note);
//
//		sponsorItem.sponsor = sponsor;
//		sponsorItem.update();
//		// sponsorForm.get().save();
//
//		final boolean isMailSecured = play.Play.application().configuration()
//				.getBoolean("mail.secured");
//
//		final Body body = new Body(views.txt.sponsors.verify_email.render(
//				routes.SponsorsMgmt.activateItemSponsor(event, itemId, sponsor.id)
//						.absoluteURL(request(), isMailSecured), sponsor)
//				.toString(), views.html.sponsors.verify_email.render(
//				routes.SponsorsMgmt.activateItemSponsor(event, itemId, sponsor.id)
//						.absoluteURL(request(), isMailSecured), sponsor)
//				.toString());
//
//		Mailer.getDefaultMailer().sendMail(
//				"Sponsor Activation from Scholastic Challenge", body,
//				sponsor.email);
//
//		flash(ControllerUtil.FLASH_SUCCESS_KEY,
//				"An email has been sent to ["
//						+ sponsorForm.get().email
//						+ "] to activate as a Sponsor.  The activation will expire after "
//						+ SponsorItem.EXPIRATION_TIME + " hours.");
//
//		return SponsorsMgmt.get(event, sponsorItem.sponsors.id);
//	}

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

//==============new update==========================28.08.2015===================start===========================//
	public static Result update(Event event, Long pageId) {


		System.out.println("=======================Hello============");
		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		if (!Event.canManage(ControllerUtil.getLocalUser(session()), event)) {
			flash(ControllerUtil.FLASH_DANGER_KEY,
					"The requested event action cannot be completed by the logged in user.");
			return redirect(routes.Application.index());
		}
		final Form<Sponsors> sponsorsForm = form(Sponsors.class)
				.bindFromRequest();
		if (sponsorsForm.hasErrors()) {
			//Logger.info("We had errors: {}", sponsorsForm.errorsAsJson());
			return badRequest(editForm.render(event, pageId, sponsorsForm));
		}
		final Sponsors sponsors = Sponsors.findById(pageId);
		//System.out.println("sponsors.id=> "+sponsors.id);
		//System.out.println("sponsors.sponsoritems.size()=> "+sponsors.sponsoritems.size());
		//System.out.println("previous value of s");
		Map<Long, SponsorItem> itemMap = new HashMap<Long, SponsorItem>();
		/*if(sponsors.getSponsorsItemWithDonatedSponsorUnmodified()!=null){
			itemMap = Sponsors.getSponsorItemWithDonations(sponsors.getSponsorsItemWithDonatedSponsorUnmodified());
		}else{
			itemMap = Sponsors.getSponsorItemWithDonations(sponsors.sponsoritems);
		}*/

		itemMap = Sponsors.getSponsorItemWithDonations(sponsors.sponsoritems);
		//System.out.println("itemMap.size()=> "+itemMap.size());
		final Sponsors updatedSponsors = sponsorsForm.get();
		System.out.println("updatedSponsors.name=> "+updatedSponsors.name);
		updatedSponsors.content = ControllerUtil
				.sanitizeText(updatedSponsors.content);
		List<SponsorItem> mergedList = new ArrayList<SponsorItem>();
		System.out.println("before mergedList :: "+mergedList);
		//System.out.println("updatedSponsors=> "+updatedSponsors);
		//====commentedout =====start====//
		List<SponsorItem> mergedList1 = new ArrayList<SponsorItem>();
		final Sponsors updatedSponsors1 = sponsorsForm.get();
		if(MapUtils.isNotEmpty(itemMap)) {
			for(SponsorItem item: updatedSponsors1.sponsoritems) {
				//=================new=====start====================//
				// System.out.println("checkbox for "+item.title+" "+item.logo);
				//=================new=====end====================//


				if(itemMap.containsKey(item.id)) {
					System.out.println("if");
					mergedList1.add(itemMap.get(item.id));
					//mergedList.add(item);


				} else {
					System.out.println("else");
					mergedList1.add(item);
				}







			}

			System.out.println("mergedList.size1=> "+mergedList1.size());
			//updatedSponsors1.sponsoritems = mergedList1;
			updatedSponsors1.setSponsorsItemWithDonatedSponsorUnmodified(mergedList1);


		}
		//====commentedout =====end====//
		for(SponsorItem item: updatedSponsors.sponsoritems){
			System.out.println("else");
			mergedList.add(item);

		}
		System.out.println("after mergedList :: "+mergedList);
		System.out.println("mergedList.size1=> "+mergedList.size());

		updatedSponsors.sponsoritems = mergedList;



		//final Sponsors sponsorsForm1 = Sponsors.findById(pageId);
		//System.out.println("sponsorsForm1=> "+sponsorsForm1.data().get(0).);

		System.out.println("mergedList.size2=> "+mergedList.size());
		Iterator<SponsorItem> sponitr=mergedList.iterator();

		while(sponitr.hasNext()){
			//System.out.println("==================Hello 22222222222#####################");
			SponsorItem spon=sponitr.next();

			System.out.println("==========CheckBox======="+spon.logo+"::"+spon.title);
		}



		//updatedSponsors.update();
		updatedSponsors.update(event.id);
		System.out.println("sponsoritem update ------->page id :: "+pageId+"event.id :: "+event.id);
		//updatedSponsors.update(event.id);
		//System.out.println("updatedSponsors.sponsoritems :: " + updatedSponsors.sponsoritems);
		Iterator<SponsorItem> sponitr1=updatedSponsors.findByEventId(event.id).sponsoritems.iterator();

		while(sponitr1.hasNext()){
			//System.out.println("==================Hello 22222222222#####################");
			SponsorItem spon=sponitr1.next();

			System.out.println("==========CheckBox======="+spon.logo+"****for***"+spon.title);
		}

		flash(ControllerUtil.FLASH_SUCCESS_KEY,
				"Sponsor Page [" + sponsorsForm.get().name
						+ "] has been updated");
		return EventWorkflowMgmt.update(event);
	}
	//==============new update==========================28.08.2015===================end===========================//
/*	public static Result update(Event event, Long pageId) {
		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		if (!Event.canManage(ControllerUtil.getLocalUser(session()), event)) {
			flash(ControllerUtil.FLASH_DANGER_KEY,
					"The requested event action cannot be completed by the logged in user.");
			return redirect(routes.Application.index());
		}
		final Form<Sponsors> sponsorsForm = form(Sponsors.class)
				.bindFromRequest();
		if (sponsorsForm.hasErrors()) {
			Logger.info("We had errors: {}", sponsorsForm.errorsAsJson());
			return badRequest(editForm.render(event, pageId, sponsorsForm));
		}
		final Sponsors sponsors = Sponsors.findById(pageId);
		Map<Long, SponsorItem> itemMap = Sponsors.getSponsorItemWithDonations(sponsors.sponsoritems);
		final Sponsors updatedSponsors = sponsorsForm.get();
		updatedSponsors.content = ControllerUtil
				.sanitizeText(updatedSponsors.content);
		List<SponsorItem> mergedList = new ArrayList<SponsorItem>();
		if(MapUtils.isNotEmpty(itemMap)) {
			for(SponsorItem item: updatedSponsors.sponsoritems) {
				if(itemMap.containsKey(item.id)) {
					mergedList.add(itemMap.get(item.id));
				} else {
					mergedList.add(item);
				}
			}
			updatedSponsors.sponsoritems = mergedList;
		}
		updatedSponsors.update(pageId);
		flash(ControllerUtil.FLASH_SUCCESS_KEY,
				"Sponsor Page [" + sponsorsForm.get().name
						+ "] has been updated");
		return EventWorkflowMgmt.update(event);
	}*/
}
