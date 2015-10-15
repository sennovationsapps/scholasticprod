package controllers;

import static play.data.Form.form;

import com.feth.play.module.mail.Mailer;
import com.typesafe.config.ConfigFactory;
import models.*;
import org.apache.commons.collections.CollectionUtils;
import views.html.events.createForm;
import views.html.events.editForm;
import views.html.events.list;
import views.html.events.viewEvent;
import views.html.events.pariticipantsAddForm;
import views.html.events.pariticipantsEditForm;

import java.util.*;

import models.Pfp.PfpType;
import models.aws.S3File;
import models.security.SecurityRole;
import models.security.User;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.ObjectUtils;

import play.Logger;
import play.data.Form;
import play.db.ebean.Transactional;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;

// TODO: Auto-generated Javadoc
/**
 * Manage a database of events.
 */
public class EventMgmt extends Controller {

	/**
	 * This result directly redirect to application home.
	 */
	public static Result GO_HOME = redirect(routes.EventMgmt.list(0, "name",
			"asc", "", "name"));

	/**
	 * Display the 'new event form'.
	 * 
	 * @return the result
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
			@Group(SecurityRole.EVENT_ASSIST) })
	@SubjectPresent(content = "/loginTaxid")
	public static Result create() {
		final User user = ControllerUtil.getLocalUser(session());
		Logger.debug(ToStringBuilder.reflectionToString(user));
		final Form<Event> eventForm = form(Event.class);
		return ok(createForm.render(eventForm));
	}

	/**
	 * Handle default path requests, redirect to computers list.
	 * 
	 * @return the result
	 */
	public static Result donateList() {
		flash(ControllerUtil.FLASH_INFO_KEY,
				"Search for an event to submit a donation on behalf of the event.");
		return GO_HOME;
	}

	/**
	 * Display the 'edit form' of a existing Event.
	 * 
	 * @param id
	 *            Id of the event to edit
	 * @return the result
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
			@Group(SecurityRole.EVENT_ASSIST) })
	// @Pattern("event.content.edit")
	@SubjectPresent(content = "/loginTaxid")
	public static Result edit(Event event) {
		System.out.println("within edit ");
		if (event.isIdOnly()) {
			event = Event.findById(event.id);
		}
		if (!Event.canManage(ControllerUtil.getLocalUser(session()), event)) {
			flash(ControllerUtil.FLASH_DANGER_KEY,
					"The requested event action cannot be completed by the logged in user.");
			return redirect(routes.Application.index());
		}
		final Form<Event> eventForm = form(Event.class)
				.fill(event);
		return ok(editForm.render(event, eventForm));
	}

	/**
	 * Display the 'edit form' of a existing Event.
	 * 
	 * @param id
	 *            Id of the event to edit
	 * @return the result
	 */
	public static Result get(Event event,int page, String sortBy, String order,
							 String filter, String fieldName) {
		System.out.println("within get view event666..");
		if (event.isIdOnly()) {
			event = Event.findById(event.id);
		}









		//===========================uploadimage=======================20.08.2015======================start==============================//
		/*//  List donationList = new ArrayList();
		List<Donation> donationList = (List<Donation>)Donation.findAllByEventId(event.id);
		int imgUrl=0;
		System.out.println("donationList .size :: "+donationList.size());
		System.out.println("donationList :: "+donationList);
		for(Donation donation:donationList){
			if(donation.imgUrl!=null){
				imgUrl++;
			}

		}


*/


		//  List donationList = new ArrayList();
		System.out.println("within view event ::event id ----> "+event.id);
		final Sponsors sponsors = Sponsors.findByEventId(event.id);
		//final Sponsors sponsors1 = SponsorsfindByEventId(event.id);
		//System.out.println("sponsors.id=> "+sponsors.id);
		//System.out.println("sponsors.sponsoritems.size()=> "+sponsors.sponsoritems.size());


		List<Donation> donationList = (List<Donation>)Donation.findAllByEventId(event.id);
		Iterator<Donation> iterator=donationList.iterator();
		List<Donation> donationList1 = new ArrayList();
		while(iterator.hasNext()){
			Donation donation=iterator.next();
			//System.out.println("donation while");
			//System.out.println("donation.sponsorItem=> "+donation.sponsorItem);
			//System.out.println("donation.sponsorItem.title=> "+donation.sponsorItem.title);
			SponsorItem sponsorItemFromSponsors;
			if((sponsors.sponsoritems!=null&& sponsors.sponsoritems.size()>0) && donation.sponsorItem!=null){
				for(int i = 0 ; i<sponsors.sponsoritems.size();i++){
					sponsorItemFromSponsors= sponsors.sponsoritems.get(i);
					//System.out.println("sponsorItemFromSponsors :: "+sponsorItemFromSponsors);
					//System.out.println("donation.sponsorItem == sponsorItemFromSponsors  -->>");
					//System.out.println(donation.sponsorItem == sponsorItemFromSponsors);
					//System.out.println("donation.sponsorItem.id "+donation.sponsorItem.id);
					//System.out.println("sponsorItemFromSponsors.id "+sponsorItemFromSponsors.id);
					//System.out.println("donation.sponsorItem.id.equals(sponsorItemFromSponsors.id) "+donation.sponsorItem.id.equals(sponsorItemFromSponsors.id));
					if( donation.sponsorItem.id.equals(sponsorItemFromSponsors.id) ){
						System.out.println("checkbox for :: "+donation.sponsorItem.title+" sponsor item logo "+sponsorItemFromSponsors.logo);

						System.out.println("donation.sponsorItem.logo :: "+donation.sponsorItem.logo);

						//donation.sponsorItem.logo!= null

						if(donation.sponsorItem.logo == true){
							//System.out.println("donationList "+donationList);
							//============web url checking======start============07.09.2015========================//
							System.out.println("iffff  " + donation.imgUrl + "<for>" + donation.sponsorItem.title);
							if(donation.sponsorItem.webLogo == true){
								donationList1.add(donation);
							}else{
								donation.webUrl = null;
								donationList1.add(donation);
							}
							//============web url checking=======end=============07.09.2015========================//
							//donationList.remove(donation);
							//System.out.println("after donationList "+donationList);
							//donationList1.add(donation);
						}else {
							System.out.println(" elseee " + donation.imgUrl + "<for>"+donation.sponsorItem.title);
							/*donationList1.add(donation);*/
						}

					}
				}
			}





         /*if(donation.sponsorItem!=null) {
            System.out.println("donation.sponsorItem.title=> "+donation.sponsorItem.title);
            System.out.println("---------------------------" + donation.sponsorItem);
            System.out.println("donation.sponsorItem.logo :: "+donation.sponsorItem.logo);
            if(donation.sponsorItem.logo.equals("false")){
               donationList.remove(donation);
            }
         }*/
         /*if(donation.sponsorItem.logo.equals("false")){
            donationList.remove(donation);
         }*/
		}
		int imgUrl=0;
   /* System.out.println("donationList .size :: "+donationList.size());
      System.out.println("donationList :: "+donationList);*/
		for(Donation donation:donationList1){
			if(donation.imgUrl!=null){
				imgUrl++;
			}

		}


//===========================uploadimage=======================20.08.2015======================end==============================//








		final User localUser = ControllerUtil.getLocalUser(session());
		if(!Event.isLive(event) && (localUser == null || !ControllerUtil.isEqual(event.userAdmin.id, localUser.id))) {
			flash(ControllerUtil.FLASH_WARNING_KEY,
					"This event is not published yet and therefore is not available at this time.");
			return ok(views.html.index.render());
		}
		final Map<String, Map<Long, ?>> donations = Donation.getTotalAdminDonations(event);
		final boolean isOpen = Event.isEventOpen(event);



/*


		return ok(viewEvent.render(event,
				isOpen,
				Event.canParticipate(localUser, isOpen),
				Event.canManage(localUser, event),
				(Map<Long, Donation.DonationsByPfp>)donations.get("pfp"),
				(Map<Long, Donation.DonationsByTeam>)donations.get("team"),null,null,null,null));
		//Donation.getTotalPfpAdminDonations(event),
		//Donation.getTotalTeamDonations(event.id)));*/

		System.out.println("page "+page);
		System.out.println("sortBy "+sortBy);
		System.out.println("order "+order);
		System.out.println("StringUtils.trimToEmpty(filter) "+StringUtils.trimToEmpty(filter));
		System.out.println("fieldName "+fieldName);
		System.out.println("localUser "+localUser);
		System.out.println("page "+page);
//==================new addition ===================18.08.2015===========start=======================//

   if(page>0 && localUser != null){
	   System.out.println("localuser!= null");
	   return ok(viewEvent.render(event,
			   isOpen,
			   Event.canParticipate(localUser, isOpen),
			   Event.canManage(localUser, event),
			   (Map<Long, Donation.DonationsByPfp>)donations.get("pfp"),
			   (Map<Long, Donation.DonationsByTeam>)donations.get("team"),
				Pfp.page(page, 0, sortBy, order, StringUtils.trimToEmpty(filter), fieldName, localUser),
			   /*Pfp.page(0, 0, sortBy, order, StringUtils.trimToEmpty(filter), fieldName, localUser),*/
			   sortBy,
			   order, filter,donationList1,imgUrl, null));
   }else{
	   return ok(viewEvent.render(event,
			   isOpen,
			   Event.canParticipate(localUser, isOpen),
			   Event.canManage(localUser, event),
			   (Map<Long, Donation.DonationsByPfp>)donations.get("pfp"),
			   (Map<Long, Donation.DonationsByTeam>)donations.get("team"),
				/*Pfp.page(page, 0, sortBy, order, StringUtils.trimToEmpty(filter), fieldName, localUser),*/
			   null,
			   sortBy,
			   order, filter,donationList1,imgUrl, null));
   }


//==================new addition ===================18.08.2015===========end=======================//












	}





	/******Start Code T-260***************


	 public static Result profileSearchPfpsEvent(Event event,int page, String sortBy, String order,
	 String filter, String fieldName) {

	 if (event.isIdOnly()) {
	 event = Event.findById(event.id);
	 }
	 final User localUser = ControllerUtil.getLocalUser(session());
	 if(!Event.isLive(event) && (localUser == null || !ControllerUtil.isEqual(event.userAdmin.id, localUser.id))) {
	 flash(ControllerUtil.FLASH_WARNING_KEY,
	 "This event is not published yet and therefore is not available at this time.");
	 return ok(views.html.index.render());
	 }
	 final Map<String, Map<Long, ?>> donations = Donation.getTotalAdminDonations(event);
	 final boolean isOpen = Event.isEventOpen(event);





	 return ok(viewEvent.render(event,
	 isOpen,
	 Event.canParticipate(localUser, isOpen),
	 Event.canManage(localUser, event),
	 (Map<Long, Donation.DonationsByPfp>)donations.get("pfp"),
	 (Map<Long, Donation.DonationsByTeam>)donations.get("team"),Event.page(page, 10, sortBy, order, StringUtils.trimToEmpty(filter), fieldName, localUser), sortBy,
	 order, filter)));




	 //User localUser = ControllerUtil.getLocalUser(session());
	 if(localUser.isEventAdmin()) {
	 return ok(viewEvent.render(
	 Event.page(page, 10, sortBy, order, StringUtils.trimToEmpty(filter), fieldName, localUser), sortBy,
	 order, filter));
	 } else if(localUser.isRootAdmin() || localUser.isSysAdmin()) {
	 return ok(profileSearchEvents.render(
	 Event.page(page, 10, sortBy, order, StringUtils.trimToEmpty(filter), fieldName, null), sortBy,
	 order, filter));
	 } else {
	 return redirect(routes.Application.profile());
	 }
	 }





	 /******End Code T-260*******************/







//====commented out====use this search option for user not logged in===================26.08.2015==========start==================//

	/*@Restrict({ @Group(SecurityRole.ROOT_ADMIN), @Group(SecurityRole.PFP_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN) })
	@SubjectPresent*/

//====commented out====use this search option for user not logged in===================26.08.2015==========end==================//
	public static Result profileSearchPfpsEvents(Event event,int page, String sortBy, String order,
												 String filter, String fieldName) {
		System.out.println("within profileSearchPfpsEvents");
		System.out.println("fieldName :: "+fieldName);
		if (event.isIdOnly()) {
			event = Event.findById(event.id);
		}


		//===========================uploadimage=======================20.08.2015======================start==============================//
		/*//  List donationList = new ArrayList();
		List<Donation> donationList = (List<Donation>)Donation.findAllByEventId(event.id);
		int imgUrl=0;
		System.out.println("donationList .size :: "+donationList.size());
		System.out.println("donationList :: "+donationList);
		for(Donation donation:donationList){
			if(donation.imgUrl!=null){
				imgUrl++;
			}

		}


*/


		//  List donationList = new ArrayList();
		System.out.println("within view event ::event id ----> "+event.id);
		final Sponsors sponsors = Sponsors.findByEventId(event.id);
		//final Sponsors sponsors1 = SponsorsfindByEventId(event.id);
		//System.out.println("sponsors.id=> "+sponsors.id);
		//System.out.println("sponsors.sponsoritems.size()=> "+sponsors.sponsoritems.size());


		List<Donation> donationList = (List<Donation>)Donation.findAllByEventId(event.id);
		Iterator<Donation> iterator=donationList.iterator();
		List<Donation> donationList1 = new ArrayList();
		while(iterator.hasNext()){
			Donation donation=iterator.next();
			//System.out.println("donation while");
			//System.out.println("donation.sponsorItem=> "+donation.sponsorItem);
			//System.out.println("donation.sponsorItem.title=> "+donation.sponsorItem.title);
			SponsorItem sponsorItemFromSponsors;
			if((sponsors.sponsoritems!=null&& sponsors.sponsoritems.size()>0) && donation.sponsorItem!=null){
				for(int i = 0 ; i<sponsors.sponsoritems.size();i++){
					sponsorItemFromSponsors= sponsors.sponsoritems.get(i);
					//System.out.println("sponsorItemFromSponsors :: "+sponsorItemFromSponsors);
					//System.out.println("donation.sponsorItem == sponsorItemFromSponsors  -->>");
					//System.out.println(donation.sponsorItem == sponsorItemFromSponsors);
					//System.out.println("donation.sponsorItem.id "+donation.sponsorItem.id);
					//System.out.println("sponsorItemFromSponsors.id "+sponsorItemFromSponsors.id);
					//System.out.println("donation.sponsorItem.id.equals(sponsorItemFromSponsors.id) "+donation.sponsorItem.id.equals(sponsorItemFromSponsors.id));
					if( donation.sponsorItem.id.equals(sponsorItemFromSponsors.id) ){
						System.out.println("checkbox for :: "+donation.sponsorItem.title+" sponsor item logo "+sponsorItemFromSponsors.logo);

						//System.out.println("donation.sponsorItem.logo :: "+donation.sponsorItem.logo);

						//donation.sponsorItem.logo!= null

						if(donation.sponsorItem.logo == true){
							//System.out.println("donationList "+donationList);
							System.out.println("iffff  " + donation.imgUrl + "<for>" + donation.sponsorItem.title);
							//donationList.remove(donation);
							//System.out.println("after donationList "+donationList);
						}else{
							System.out.println(" elseee "+donation.imgUrl+"<for>"+donation.sponsorItem.title);
							donationList1.add(donation);
						}

					}
				}
			}





         /*if(donation.sponsorItem!=null) {
            System.out.println("donation.sponsorItem.title=> "+donation.sponsorItem.title);
            System.out.println("---------------------------" + donation.sponsorItem);
            System.out.println("donation.sponsorItem.logo :: "+donation.sponsorItem.logo);
            if(donation.sponsorItem.logo.equals("false")){
               donationList.remove(donation);
            }
         }*/
         /*if(donation.sponsorItem.logo.equals("false")){
            donationList.remove(donation);
         }*/
		}
		int imgUrl=0;
   /* System.out.println("donationList .size :: "+donationList.size());
      System.out.println("donationList :: "+donationList);*/
		for(Donation donation:donationList1){
			if(donation.imgUrl!=null){
				imgUrl++;
			}

		}


//===========================uploadimage=======================20.08.2015======================end==============================//


		User localUser = ControllerUtil.getLocalUser(session());
		if (!Event.isLive(event) && (localUser == null || !ControllerUtil.isEqual(event.userAdmin.id, localUser.id))) {
			flash(ControllerUtil.FLASH_WARNING_KEY,
					"This event is not published yet and therefore is not available at this time.");
			return ok(views.html.index.render());
		}
		final Map<String, Map<Long, ?>> donations = Donation.getTotalAdminDonations(event);
		final boolean isOpen = Event.isEventOpen(event);



/*		return ok(viewEvent.render(event,
				isOpen,
				Event.canParticipate(localUser, isOpen),
				Event.canManage(localUser, event),
				(Map<Long, Donation.DonationsByPfp>)donations.get("pfp"),
				(Map<Long, Donation.DonationsByTeam>)donations.get("team"),
				Pfp.page(page, 10, sortBy, order, StringUtils.trimToEmpty(filter), fieldName, localUser),
				sortBy,
				order,
				filter));*/


		//User localUser = ControllerUtil.getLocalUser(session());
		System.out.println("page1 " + page);
		System.out.println("sortBy1 " + sortBy);
		System.out.println("order1 " + order);
		System.out.println("StringUtils.trimToEmpty(filter)1 " + StringUtils.trimToEmpty(filter));
		System.out.println("fieldName1 " + fieldName);
		System.out.println("localUser1 " + localUser);
		System.out.println("page1 " + page);
		//========use this search option for user not logged in===================26.08.2015==========start==================//
	if(localUser!=null){
		//========use this search option for user not logged in===================26.08.2015==========end==================//
		if (localUser.isEventAdmin() || localUser.isPfpAdmin()) {
			System.out.println("-------------------__Root Admi------------------");


			return ok(viewEvent.render(event,
					isOpen,
					Event.canParticipate(localUser, isOpen),
					Event.canManage(localUser, event),
					(Map<Long, Donation.DonationsByPfp>) donations.get("pfp"),
					(Map<Long, Donation.DonationsByTeam>) donations.get("team"),
					Pfp.pageForParticularEvent(page, 10, sortBy, order, StringUtils.trimToEmpty(filter), fieldName, null, event.id),
					sortBy,
					order,
					filter, donationList, imgUrl, fieldName));
			/*return ok(viewEvent.render(
					Pfp.page(page, 10, sortBy, order, StringUtils.trimToEmpty(filter), fieldName, localUser), sortBy,
					order, filter));  */
		} else if (localUser.isRootAdmin() || localUser.isSysAdmin()) {
			System.out.println("-------------------__Root Adminnnnnnnnnnnnnnnnnnnnn------------------");


			return ok(viewEvent.render(event,
					isOpen,
					Event.canParticipate(localUser, isOpen),
					Event.canManage(localUser, event),
					(Map<Long, Donation.DonationsByPfp>) donations.get("pfp"),
					(Map<Long, Donation.DonationsByTeam>) donations.get("team"),
					Pfp.pageForParticularEvent(page, 10, sortBy, order, StringUtils.trimToEmpty(filter), fieldName, null, event.id),
					sortBy,
					order, filter, donationList, imgUrl, fieldName));






			/*return ok(profileSearchPfps.render(
					Pfp.page(page, 10, sortBy, order, StringUtils.trimToEmpty(filter), fieldName, null), sortBy,
					order, filter));*/
		} else {
			System.out.println("-----------within else----------------------");
			return redirect(routes.Application.profile());

			/*return ok(viewEvent.render(event,
					isOpen,
					Event.canParticipate(localUser, isOpen),
					Event.canManage(localUser, event),
					(Map<Long, Donation.DonationsByPfp>)donations.get("pfp"),
					(Map<Long, Donation.DonationsByTeam>)donations.get("team"),
					Pfp.page(page, 10, sortBy, order, StringUtils.trimToEmpty(filter), fieldName, null),
					sortBy,
					order, filter));*/
		}

		//========use this search option for user not logged in===================26.08.2015==========start==================//
	}else{
		return ok(viewEvent.render(event,
				isOpen,
				Event.canParticipate(localUser, isOpen),
				Event.canManage(localUser, event),
				(Map<Long, Donation.DonationsByPfp>) donations.get("pfp"),
				(Map<Long, Donation.DonationsByTeam>) donations.get("team"),
				Pfp.pageForParticularEvent(page, 10, sortBy, order, StringUtils.trimToEmpty(filter), fieldName, null, event.id),
				sortBy,
				order, filter, donationList, imgUrl, fieldName));
	}
		//========use this search option for user not logged in===================26.08.2015==========end==================//
	}

	/******End Code T-260*******************/












	//============start:<>================delete the participants from view event page==========05.08.2015===============//
	public static Result deleteParticipants(Event event, Long participantsId)
		 {
			 System.out.println("within removeShiftVolunteer..participantsId :: ."+participantsId);
		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}

		try {
			System.out.println("within removeShiftVolunteer..participantsId :: ." + participantsId);
			if (event.isIdOnly()) {
				event = Event.findByIdWithMin(event.id);
			}


			//if (donationForm.hasErrors()) {
			//return badRequest(createForm.render(event, donationForm));
			//}
			// User user = ControllerUtil.getLocalUser(session());


			final List<Donation> donationList = Donation.findByPfpId(participantsId);
			// donation.;
			// final Map<String, Map<Long, ?>> donations = Donation.getTotalAdminDonations(event);
			System.out.println("donationList :" + donationList);
			//   Long key ;

     		/*
			if (donationList.size() > 0) {

				flash(ControllerUtil.FLASH_DANGER_KEY,
						"The Participant can't deleted");
				return redirect(routes.EventMgmt.get(event, 0, "name", "asc", "", "name"));

			}
			*/
			 /*for (final Donation donation : donationList) {
				 System.out.println("donation: donationlist");

				// Iterator itr = (Iterator) donations.get("pfp").keySet().iterator();

				 donation.delete();
				 donation.update();
				 System.out.println("after delete111222");
			 }*/


			Pfp pfp = Pfp.findById(participantsId);
			System.out.println("pfp.name :: " + pfp.name);
			System.out.println("pfp.total :: " + pfp.total);
			System.out.println(" pfp.id :: " + pfp.id);
			pfp.delete();
			System.out.println("after delete11");

		}catch(Exception e){
			System.out.println("------------------Error In Deletion------------"+e);
			e.printStackTrace();
			flash(ControllerUtil.FLASH_DANGER_KEY,
					"Cannot delete. There is a Donation linked to this PFP");


		}
        return EventMgmt.get(event,0,"name","asc","","name");




	}

	//============end:<>================delete the participants from view event page==========05.08.2015===============//
//============start:<>================add new participants in view event page==========05.08.2015===============//

	public static Result addParticipantsForEvent(Event event) {
		System.out.println("within addParticipantsForEvent");
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
		System.out.println("before calling pfpForm");
		Form<Pfp> pfpForm = form(Pfp.class);
		System.out.println("before calling pfp");
		//Pfp pfp = pfpForm.get();
		System.out.println("before calling donationform");
		Form<Donation> donationForm = form(Donation.class);
		System.out.println("before calling donationform");
		//Donation donation = donationForm.get();
		System.out.println("before returning ");
		return ok(pariticipantsAddForm.render(event, pfpForm, donationForm));

		//return ok(participantsAddForm.);



	}

//============end:<>================add new participants in view event page==========05.08.2015===============//

	//============start:<>================add new participants in view event page==========05.08.2015===============//

	public static Result saveParticipantsForEvent(Event event) {
		System.out.println("within saveParticipantsForEvent ");

		System.out.println("before calling donationform");
		Form<Donation> donationForm = form(Donation.class).bindFromRequest();
		System.out.println("after calling donationform");


	/*	if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		final Form<Pfp> pfpForm = form(Pfp.class).bindFromRequest();

		if (StringUtils.isEmpty(pfpForm.data().get("team.id"))) {
			pfpForm.reject("team.id", "You must select a team for your participant.");
		}

		if (pfpForm.hasErrors()) {
			return badRequest(pariticipantsAddForm.render(event, pfpForm, donationForm));
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
			return badRequest(pariticipantsAddForm.render(event, pfpForm, donationForm));
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
				return badRequest(pariticipantsAddForm.render(event, pfpForm, donationForm));
			}
			pfp.userAdmin = user;
		}
		if (Pfp.existsAlready(pfpForm.get(), event)) {
			pfpForm.reject("name", "It appears you are attempting to create a duplicate participant.  If you need help with your original participant page, please let contact Scholastic Challenge.");
			return badRequest(pariticipantsAddForm.render(event, pfpForm, donationForm));
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
		updatedPfp.update();*/
		//EventMgmt.generatePfpCreate(updatedPfp, ConfigFactory.load().getString("base.url") + routes.PfpMgmt.get(updatedPfp.event, updatedPfp).url());
		//flash(ControllerUtil.FLASH_SUCCESS_KEY, "Pfp [" + pfpForm.get().name
		//		+ "] has been created");



		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		//	final Form<Volunteer> volunteerForm = form(Volunteer.class)
		//		.bindFromRequest();
   //event.update();

		final Form<Pfp> pfpForm = form(Pfp.class)
				.bindFromRequest();
		System.out.println("after pfp form");
		Pfp pfp = pfpForm.get();
		System.out.println("after pfp");

		/*System.out.println("before calling event update");
		event.update();
		System.out.println("after calling event.update");*/
//==============new add



		final Http.MultipartFormData body = request().body()
				.asMultipartFormData();
		System.out.println("after MultipartFormData");
		final Http.MultipartFormData.FilePart imgUrlFilePart = body
				.getFile("imgUrl");
		System.out.println("after imaurlfilepath");
		S3File imgUrlFile = null;
		System.out.println("imgUrlFile :: "+imgUrlFile);
		if (imgUrlFilePart != null) {
			System.out.println("imgUrlFilePart != null");
			if (!ControllerUtil.isImage(imgUrlFilePart.getFilename())) {
				pfpForm.reject("imgUrl", ControllerUtil.IMAGE_ERROR_MSG);
			} else if (ControllerUtil.isFileTooLarge(imgUrlFilePart.getFile())) {
				pfpForm.reject("imgUrl", ControllerUtil.IMAGE_SIZE_ERROR_MSG);
			} else {
				System.out.println("imgUrlFilePart == null");
				imgUrlFile = new S3File();
				imgUrlFile.name = ControllerUtil.decodeFileName(imgUrlFilePart
						.getFilename());
				imgUrlFile.file = imgUrlFilePart.getFile();
				pfpForm.get().imgUrl = imgUrlFile.getUrl();
			}
		}


		System.out.println("pfp.userAdmin :: "+pfp.userAdmin);

		if (pfp.userAdmin == null) {
			pfp.userAdmin = ControllerUtil.getLocalUser(session());
		} else if ((pfp.userAdmin.id == null) && (pfp.userAdmin.email != null)) {
			Logger.debug("Created by Sys Admin, so must find Pfp Admin by email");
			final User user = User.findByEmail(pfp.userAdmin.email);
			if (user == null) {
				pfpForm.reject("userAdmin.email",
						"A user account does not exist for the supplied email.");
				return badRequest(pariticipantsAddForm.render(event, pfpForm, donationForm));
			}
			pfp.userAdmin = user;
		}
		System.out.println("before pfp title");
		pfp.title = "Event - " + pfp.name;
		System.out.println("after pfp.title :: "+pfp.title);
		//pfp.emergencyContactPhone = ControllerUtil.stripPhone(pfp.emergencyContactPhone);
		//pfp.content = ControllerUtil.sanitizeText(pfp.content);
		pfp.total = 0;

		pfp.pfpType = PfpType.PFP;
		System.out.println("pfp.pfpType :: "+pfp.pfpType);
		pfp.dateCreated = new Date();
		pfp.event = event;
		if (imgUrlFile != null) {
			imgUrlFile.save();
		}


		System.out.println("before pfp.save");
		pfp.save();



		Pfp updatedPfp = Pfp.findById(pfp.id);
		System.out.println("updatedpfp :: "+updatedPfp);
		updatedPfp.slug = Pfp.toSlug(updatedPfp);
		updatedPfp.update();

        Donation donation = donationForm.get();
	donation.pfp.id = pfp.id;
		System.out.println("donation.pfp.id :: "+donation.pfp.id);
  donation.event.id = event.id;
		System.out.println("donation.event.id :: "+donation.event.id);

		donation.save();

		System.out.println("before calling team form");
		final Form<Team> teamForm = form(Team.class);
		System.out.println("after calling team form");
		Team team = teamForm.get();

		team.name = pfp.team.name;
		System.out.println("team.name :: "+team.name);
		team.id = pfp.team.id;
		System.out.println("team.id  :: "+team.id );
//====================new add

//===add for referencial integrity

	/*	if(!StringUtils.equals(event.name,pfp.name)){
			event.id = pfp.name;
		}*/




		System.out.println("pfp within saveParticipantsForEvent:: " + pfp);





		System.out.println("before returning");
		return EventMgmt.get(event,0,"name","asc","","name");


	}

	private static void generatePfpCreate(Pfp pfp, String url) {
		String content = views.txt.pfps.pfp_created_msg
				.render(pfp, url).toString();
		final Mailer.Mail.Body body = new Mailer.Mail.Body(content);

		Mailer.getDefaultMailer().sendMail("Scholastic Challenge - PFP Page Created", body,
				pfp.userAdmin.email);
	}

//============end:<>================add new participants in view event page==========05.08.2015===============//
//============start:<>================populate  participants in view event page with previous values==========06.08.2015===============//
public static Result populateParticipantsForEvent(Event event, Long participantsId)
{
	System.out.println("within populateParticipantsForEvent...participantsId :: "+participantsId);
	//get the details of volunteer through volunteer id

	if (event.isIdOnly()) {
		event = Event.findByIdWithMin(event.id);
	}


	Pfp pfp = Pfp.findById(participantsId);
	//System.out.println("pfp "+pfp);
	final Form<Pfp> pfpForm = form(Pfp.class)
			.bindFromRequest().fill(pfp);
	final Form<Donation> donationForm = form(Donation.class);
	if (pfpForm.hasErrors()) {

		System.out.println("within volunteerForm.errors");
	//	Logger.debug("Has errors {}", volunteerForm.errorsAsJson());
		return badRequest(pariticipantsEditForm.render(event, pfpForm, donationForm));
	}else {

		System.out.println("within else33");
		return ok(pariticipantsEditForm.render(event, pfpForm, donationForm));

	}

}
//============end:<>==================populate  participants in view event page with previous values==========06.08.2015===============//

//============start:<>================update participants in view event page with previous values==========06.08.2015===============//
public static Result updateParticipantsForEvent(Event event, Long participantsId){

	System.out.println("within updateParticipantsForEvent..");
	final Form<Pfp> pfpForm = form(Pfp.class)
			.bindFromRequest();
	if (pfpForm.hasErrors()) {
		//return badRequest(modalEditForm.render(volunteerForm.get(), volunteerForm));
	}

	Pfp updatedPfp = pfpForm.get();
	Pfp pfp = Pfp.findById(participantsId);


	if(!StringUtils.equals(pfp.name,updatedPfp.name))
	{
		pfp.name = updatedPfp.name;

	}
	if(!StringUtils.equals(pfp.content,updatedPfp.content))
	{
		pfp.content = updatedPfp.content;

	}
	if(!StringUtils.equals(pfp.emergencyContact,updatedPfp.emergencyContact))
	{
		pfp.emergencyContact = updatedPfp.emergencyContact;

	}
	if(!StringUtils.equals(pfp.emergencyContactPhone,updatedPfp.emergencyContactPhone))
	{
		pfp.emergencyContactPhone = updatedPfp.emergencyContactPhone;

	}
	if(!StringUtils.equals(pfp.title,updatedPfp.title))
	{
		pfp.title = updatedPfp.title;

	}

	System.out.println("pfp updateParticipantsForEvent :: "+pfp);
	pfp.update();


	//saveShiftVolunteer(event, shiftId);
	System.out.println("before calling get  event " + event + " participantsId " + participantsId);
	return EventMgmt.get(event,0,"name","asc","","name");


	//return ok(modalEditForm.render(event, volunteer, shiftId, volunteerForm));
}

//============end:<>================update participants in view event page with previous values==========06.08.2015===============//

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
	 * @param fieldName
	 *            the field name
	 * @return the result
	 */
	public static Result list(int page, String sortBy, String order,
			String filter, String fieldName) {
		return ok(list.render(
				Event.page(page, 10, sortBy, order, filter, fieldName), sortBy,
				order, filter));
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
	public static Result remove(Event event) {
		if (event.isIdOnly()) {
			event = Event.findById(event.id);
		}
		if (!Event.canManage(ControllerUtil.getLocalUser(session()), event)) {
			flash(ControllerUtil.FLASH_DANGER_KEY,
					"The requested event action cannot be completed by the logged in user.");
			return redirect(routes.Application.profileEvents());
		}
		if (Donation.existsByEventId(event.id)) {
			flash(ControllerUtil.FLASH_INFO_KEY,
					"The requested event cannot be removed because donations exist for this account.");
			routes.Application.profileEvents();
		}
		flash(ControllerUtil.FLASH_INFO_KEY,
				"The requested event [" + event.name + "] has been removed from the system.");
		event.generalFund.delete();
		event.sponsorFund.delete();
		Awards awards = Awards.findByEventId(event.id);
		if(awards != null) {
			awards.delete();
		}
		List<EventPages> eventPages = EventPages.findByEventId(event.id);
		for(EventPages eventPage: eventPages) {
			if(eventPage != null) {
				eventPage.delete();
			}
		}
		List<Team> teams = Team.findByEventId(event.id);
		for(Team team: teams) {
			if(team != null) {
				team.delete();
			}
		}
		Sponsors sponsors = Sponsors.findByEventId(event.id);
		if(sponsors != null) {
			sponsors.delete();
		}
		Volunteers volunteers = Volunteers.findByEventId(event.id);
		if(volunteers != null) {
			volunteers.delete();
		}
		event.delete();
		return redirect(routes.Application.profileEvents());
	}

	/**
	 * Handle the 'new event form' submission.
	 *
	 * @return the result
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
			@Group(SecurityRole.EVENT_ASSIST) })
	@SubjectPresent(content = "/loginTaxid")
	@Transactional
	public static Result save() {
		final Form<Event> eventForm = form(Event.class).bindFromRequest();
		// if(request().body().isMaxSizeExceeded()) {
		// return badRequest("The uploaded image files were too large.");
		// }
		if (eventForm.hasErrors()) {
			return badRequest(createForm.render(eventForm));
		}
//		System.out.println("********************");
//		System.out.println("********************");
//		System.out.println(ToStringBuilder.reflectionToString(eventForm));
//		System.out.println("********************");
//		System.out.println("********************");
//		if(true) {
//			return badRequest(createForm.render(eventForm));
//		}
		final Http.MultipartFormData body = request().body()
				.asMultipartFormData();
		final Http.MultipartFormData.FilePart heroImgUrlFilePart = body
				.getFile("heroImgUrl");
		S3File heroImgUrlFile = null;
		if (heroImgUrlFilePart != null) {
			if (!ControllerUtil.isImage(heroImgUrlFilePart.getFilename())) {
				eventForm.reject("heroImgUrl", ControllerUtil.IMAGE_ERROR_MSG);
			} else if (ControllerUtil.isFileTooLarge(heroImgUrlFilePart
					.getFile())) {
				eventForm.reject("heroImgUrl",
						ControllerUtil.IMAGE_SIZE_ERROR_MSG);
			} else {
				heroImgUrlFile = new S3File();
				heroImgUrlFile.name = ControllerUtil
						.decodeFileName(heroImgUrlFilePart.getFilename());
				heroImgUrlFile.file = heroImgUrlFilePart.getFile();
				eventForm.get().heroImgUrl = heroImgUrlFile.getUrl();
			}
		} else {
			Logger.debug(
					"No heroImgUrl was submitted with the request, retrieve default {}.",
					S3File.getImage("img.default.hero"));
			eventForm.get().heroImgUrl = S3File.getImage("img.default.hero");
		}
		final Http.MultipartFormData.FilePart imgUrlFilePart = body
				.getFile("imgUrl");
		S3File imgUrlFile = null;
		if (imgUrlFilePart != null) {
			if (!ControllerUtil.isImage(imgUrlFilePart.getFilename())) {
				eventForm.reject("imgUrl", ControllerUtil.IMAGE_ERROR_MSG);
			} else if (ControllerUtil.isFileTooLarge(imgUrlFilePart.getFile())) {
				eventForm.reject("imgUrl", ControllerUtil.IMAGE_SIZE_ERROR_MSG);
			} else {
				imgUrlFile = new S3File();
				imgUrlFile.name = ControllerUtil.decodeFileName(imgUrlFilePart
						.getFilename());
				imgUrlFile.file = imgUrlFilePart.getFile();
				eventForm.get().imgUrl = imgUrlFile.getUrl();
			}
		}
		final Http.MultipartFormData.FilePart imgUrlFilePart1 = body
				.getFile("imgUrl1");
		S3File imgUrlFile1 = null;
		if (imgUrlFilePart1 != null) {
			if (!ControllerUtil.isImage(imgUrlFilePart1.getFilename())) {
				eventForm.reject("imgUrl1", ControllerUtil.IMAGE_ERROR_MSG);
			} else if (ControllerUtil.isFileTooLarge(imgUrlFilePart1.getFile())) {
				eventForm.reject("imgUrl1", ControllerUtil.IMAGE_SIZE_ERROR_MSG);
			} else {
				imgUrlFile1 = new S3File();
				imgUrlFile1.name = ControllerUtil.decodeFileName(imgUrlFilePart1
						.getFilename());
				imgUrlFile1.file = imgUrlFilePart1.getFile();
				eventForm.get().imgUrl1 = imgUrlFile1.getUrl();
			}
		}

		final Http.MultipartFormData.FilePart imgUrlFilePart2 = body
				.getFile("imgUrl2");
		S3File imgUrlFile2 = null;
		if (imgUrlFilePart2 != null) {
			if (!ControllerUtil.isImage(imgUrlFilePart2.getFilename())) {
				eventForm.reject("imgUrl2", ControllerUtil.IMAGE_ERROR_MSG);
			} else if (ControllerUtil.isFileTooLarge(imgUrlFilePart2.getFile())) {
				eventForm.reject("imgUrl2", ControllerUtil.IMAGE_SIZE_ERROR_MSG);
			} else {
				imgUrlFile2 = new S3File();
				imgUrlFile2.name = ControllerUtil.decodeFileName(imgUrlFilePart2
						.getFilename());
				imgUrlFile2.file = imgUrlFilePart2.getFile();
				eventForm.get().imgUrl2 = imgUrlFile2.getUrl();
			}
		}

		final Http.MultipartFormData.FilePart imgUrlFilePart3 = body
				.getFile("imgUrl3");
		S3File imgUrlFile3 = null;
		if (imgUrlFilePart3 != null) {
			if (!ControllerUtil.isImage(imgUrlFilePart3.getFilename())) {
				eventForm.reject("imgUrl3", ControllerUtil.IMAGE_ERROR_MSG);
			} else if (ControllerUtil.isFileTooLarge(imgUrlFilePart3.getFile())) {
				eventForm.reject("imgUrl3", ControllerUtil.IMAGE_SIZE_ERROR_MSG);
			} else {
				imgUrlFile3 = new S3File();
				imgUrlFile3.name = ControllerUtil.decodeFileName(imgUrlFilePart3
						.getFilename());
				imgUrlFile3.file = imgUrlFilePart3.getFile();
				eventForm.get().imgUrl3 = imgUrlFile3.getUrl();
			}
		}

		final Http.MultipartFormData.FilePart imgUrlFilePart4 = body
				.getFile("imgUrl4");
		S3File imgUrlFile4 = null;
		if (imgUrlFilePart4 != null) {
			if (!ControllerUtil.isImage(imgUrlFilePart4.getFilename())) {
				eventForm.reject("imgUrl4", ControllerUtil.IMAGE_ERROR_MSG);
			} else if (ControllerUtil.isFileTooLarge(imgUrlFilePart4.getFile())) {
				eventForm.reject("imgUrl4", ControllerUtil.IMAGE_SIZE_ERROR_MSG);
			} else {
				imgUrlFile4 = new S3File();
				imgUrlFile4.name = ControllerUtil.decodeFileName(imgUrlFilePart4
						.getFilename());
				imgUrlFile4.file = imgUrlFilePart4.getFile();
				eventForm.get().imgUrl4 = imgUrlFile4.getUrl();
			}
		}

		// if(eventForm.get().fundraisingEnd != null) {
		// eventForm.get().fundraisingEnd =
		// EventMgmt.setDateToMidnight(eventForm.get().fundraisingEnd);
		// }
		if (eventForm.hasErrors()) {
			return badRequest(createForm.render(eventForm));
		}
		final Event event = eventForm.get();
		if (event.userAdmin == null) {
			event.userAdmin = ControllerUtil.getLocalUser(session());
		} else if ((event.userAdmin.id == null)
				&& (event.userAdmin.email != null)) {
			Logger.debug("Created by Sys Admin, so must find Event Admin by email");
			final User user = User.findByEmail(event.userAdmin.email);
			if (user == null) {
				eventForm
						.reject("userAdmin.email",
								"A user account does not exist for the supplied email.");
				return badRequest(createForm.render(eventForm));
			}
			event.userAdmin = user;
		}
		Logger.debug(ToStringBuilder.reflectionToString(event));
		event.status = Event.PublishStatus.NEW;
		if(StringUtils.isEmpty(event.title)) {
			event.title = "Event - " + event.name;
		}
		event.content = ControllerUtil.sanitizeText(event.content);

		// event.fundraisingEnd =
		// EventMgmt.setDateToMidnight(event.fundraisingEnd);
		if (heroImgUrlFile != null) {
			heroImgUrlFile.save();
		}
		if (imgUrlFile != null) {
			imgUrlFile.save();
		}
		if (imgUrlFile1 != null) {
			imgUrlFile1.save();
		}
		if (imgUrlFile2 != null) {
			imgUrlFile2.save();
		}
		event.dateCreated = new Date();
		event.save();
		session(ControllerUtil.FLASH_SUCCESS_KEY, "Event ["
				+ eventForm.get().name + "] has been created");

		// Create a default General Fund Account for each Event
		// Anonymous donors or donors to the vent are placed against this
		// account
		Event updatedEvent = Event.findById(event.id);
		Pfp generalFund = createDefaultPfp(updatedEvent);
		Pfp generalSponsor = createDefaultSponsor(updatedEvent);
		createVolunteerPage(updatedEvent);
		createSponsorPage(updatedEvent);
		updatedEvent.generalFund = generalFund;
		updatedEvent.sponsorFund = generalSponsor;
		updatedEvent.slug = Event.toSlug(event);
		updatedEvent.update();

		return EventWorkflowMgmt.update(updatedEvent);
	}

	/**
	 * Handle the 'edit form' submission .
	 *
	 * @param id
	 *            Id of the event to edit
	 * @return the result
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
			@Group(SecurityRole.EVENT_ASSIST) })
	@SubjectPresent(content = "/loginTaxid")
	@Transactional
	public static Result update(Event event) {
		if (event.isIdOnly()) {
			event = Event.findById(event.id);
		}
		if (!Event.canManage(ControllerUtil.getLocalUser(session()), event)) {
			flash(ControllerUtil.FLASH_DANGER_KEY,
					"The requested event action cannot be completed by the logged in user.");
			return redirect(routes.Application.index());
		}
		final Form<Event> eventForm = form(Event.class).bindFromRequest();
		if (eventForm.hasErrors()) {
			return badRequest(editForm.render(event, eventForm));
		}
		final Http.MultipartFormData body = request().body()
				.asMultipartFormData();
		final Http.MultipartFormData.FilePart heroImgUrlFilePart = body
				.getFile("heroImgUrl");
		S3File heroImgUrlFile = null;
		if (heroImgUrlFilePart != null) {
			if (!ControllerUtil.isImage(heroImgUrlFilePart.getFilename())) {
				eventForm.reject("heroImgUrl", ControllerUtil.IMAGE_ERROR_MSG);
			} else if (ControllerUtil.isFileTooLarge(heroImgUrlFilePart
					.getFile())) {
				eventForm.reject("heroImgUrl",
						ControllerUtil.IMAGE_SIZE_ERROR_MSG);
			} else {
				heroImgUrlFile = new S3File();
				heroImgUrlFile.name = ControllerUtil
						.decodeFileName(heroImgUrlFilePart.getFilename());
				heroImgUrlFile.file = heroImgUrlFilePart.getFile();
				eventForm.get().heroImgUrl = heroImgUrlFile.getUrl();
			}
		}
		final Http.MultipartFormData.FilePart imgUrlFilePart = body
				.getFile("imgUrl");
		S3File imgUrlFile = null;
		if (imgUrlFilePart != null) {
			if (ControllerUtil.isImage(imgUrlFilePart.getFilename())) {
				imgUrlFile = new S3File();
				imgUrlFile.name = ControllerUtil.decodeFileName(imgUrlFilePart
						.getFilename());
				imgUrlFile.file = imgUrlFilePart.getFile();
				Logger.debug("IMAGE FILE SIZE - "
						+ imgUrlFilePart.getFile().length());
				eventForm.get().imgUrl = imgUrlFile.getUrl();
			} else {
				eventForm.reject("imgUrl", ControllerUtil.IMAGE_ERROR_MSG);
			}
		}

		final Http.MultipartFormData.FilePart imgUrlFilePart1 = body
				.getFile("imgUrl1");
		S3File imgUrlFile1 = null;
		if (imgUrlFilePart1 != null) {
			if (ControllerUtil.isImage(imgUrlFilePart1.getFilename())) {
				imgUrlFile1 = new S3File();
				imgUrlFile1.name = ControllerUtil.decodeFileName(imgUrlFilePart1
						.getFilename());
				imgUrlFile1.file = imgUrlFilePart1.getFile();
				Logger.debug("IMAGE FILE SIZE - "
						+ imgUrlFilePart1.getFile().length());
				eventForm.get().imgUrl1 = imgUrlFile1.getUrl();
			} else {
				eventForm.reject("imgUrl1", ControllerUtil.IMAGE_ERROR_MSG);
			}
		}

		final Http.MultipartFormData.FilePart imgUrlFilePart2 = body
				.getFile("imgUrl2");
		S3File imgUrlFile2 = null;
		if (imgUrlFilePart2 != null) {
			if (ControllerUtil.isImage(imgUrlFilePart2.getFilename())) {
				imgUrlFile2 = new S3File();
				imgUrlFile2.name = ControllerUtil.decodeFileName(imgUrlFilePart2
						.getFilename());
				imgUrlFile2.file = imgUrlFilePart2.getFile();
				Logger.debug("IMAGE FILE SIZE - "
						+ imgUrlFilePart2.getFile().length());
				eventForm.get().imgUrl2 = imgUrlFile2.getUrl();
			} else {
				eventForm.reject("imgUrl2", ControllerUtil.IMAGE_ERROR_MSG);
			}
		}

		final Http.MultipartFormData.FilePart imgUrlFilePart3 = body
				.getFile("imgUrl3");
		S3File imgUrlFile3 = null;
		if (imgUrlFilePart3 != null) {
			if (ControllerUtil.isImage(imgUrlFilePart3.getFilename())) {
				imgUrlFile3 = new S3File();
				imgUrlFile3.name = ControllerUtil.decodeFileName(imgUrlFilePart3
						.getFilename());
				imgUrlFile3.file = imgUrlFilePart3.getFile();
				Logger.debug("IMAGE FILE SIZE - "
						+ imgUrlFilePart3.getFile().length());
				eventForm.get().imgUrl3 = imgUrlFile3.getUrl();
			} else {
				eventForm.reject("imgUrl3", ControllerUtil.IMAGE_ERROR_MSG);
			}
		}

		final Http.MultipartFormData.FilePart imgUrlFilePart4 = body
				.getFile("imgUrl4");
		S3File imgUrlFile4 = null;
		if (imgUrlFilePart4 != null) {
			if (ControllerUtil.isImage(imgUrlFilePart4.getFilename())) {
				imgUrlFile4 = new S3File();
				imgUrlFile4.name = ControllerUtil.decodeFileName(imgUrlFilePart4
						.getFilename());
				imgUrlFile4.file = imgUrlFilePart4.getFile();
				Logger.debug("IMAGE FILE SIZE - "
						+ imgUrlFilePart4.getFile().length());
				eventForm.get().imgUrl4 = imgUrlFile4.getUrl();
			} else {
				eventForm.reject("imgUrl4", ControllerUtil.IMAGE_ERROR_MSG);
			}
		}
		// if(eventForm.get().fundraisingEnd != null) {
		// eventForm.get().fundraisingEnd =
		// EventMgmt.setDateToMidnight(eventForm.get().fundraisingEnd);
		// }
		if (eventForm.hasErrors()) {
			return badRequest(editForm.render(event, eventForm));
		}
		final Event updatedEvent = eventForm.get();
		updatedEvent.content = ControllerUtil
				.sanitizeText(updatedEvent.content);
		if (heroImgUrlFile != null) {
			if(event.heroImgUrl != null) {
				S3File.delete(event.heroImgUrl);
			}
			heroImgUrlFile.save();
		}
		if (imgUrlFile != null) {
			if(event.imgUrl != null) {
				S3File.delete(event.imgUrl);
			}
			imgUrlFile.save();
		}
		if (imgUrlFile1 != null) {
			if(event.imgUrl1 != null) {
				S3File.delete(event.imgUrl1);
			}
			imgUrlFile1.save();
		}
		if (imgUrlFile2 != null) {
			if(event.imgUrl2 != null) {
				S3File.delete(event.imgUrl2);
			}
			imgUrlFile2.save();
		}
		if (imgUrlFile3 != null) {
			if(event.imgUrl3 != null) {
				S3File.delete(event.imgUrl3);
			}
			imgUrlFile3.save();
		}
		if (imgUrlFile4 != null) {
			if(event.imgUrl4 != null) {
				S3File.delete(event.imgUrl4);
			}
			imgUrlFile4.save();
		}

		updatedEvent.update(event.id);
		flash(ControllerUtil.FLASH_SUCCESS_KEY, "Event ["
				+ eventForm.get().name + "] has been updated");
		return EventWorkflowMgmt.update(event);
	}

	/**
	 * Handle the 'edit form' submission .
	 * 
	 * @param id
	 *            Id of the event to edit
	 * @return the result
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
			@Group(SecurityRole.EVENT_ASSIST) })
	@SubjectPresent(content = "/loginTaxid")
	@Transactional
	public static Result updateInline(Event event) {
		if (event.isIdOnly()) {
			event = Event.findById(event.id);
		}
		if (!Event.canManage(ControllerUtil.getLocalUser(session()), event)) {
			flash(ControllerUtil.FLASH_DANGER_KEY,
					"The requested event action cannot be completed by the logged in user.");
			return redirect(routes.Application.index());
		}
//		Logger.debug(ToStringBuilder.reflectionToString(eventForm));
		final Form<Event> eventForm = form(Event.class).bindFromRequest();
		final Map<String, String> formMap = eventForm.data();
		if(StringUtils.isEmpty(formMap.get("goal"))) {
			flash(ControllerUtil.FLASH_DANGER_KEY,
							"The updated goal amount was empty, it is a required field.");
			return Application.profileEvents();
		}
		boolean updated = false;
		if (formMap.containsKey("eventStart") && !DateUtils.isSameDay(event.eventStart, new Date(formMap.get("eventStart")))) {
			event.eventStart = new Date(formMap.get("eventStart"));
			updated = true;
		}
		if (formMap.containsKey("status") && event.status != Event.PublishStatus.get(formMap.get("status"))) {
			event.status = Event.PublishStatus.get(formMap.get("status"));
			updated = true;
		}
		if (formMap.containsKey("goal") && ObjectUtils.compare(event.goal, Integer.parseInt(formMap.get("goal"))) != 0) {
			event.goal = Integer.parseInt(formMap.get("goal"));
			updated = true;
		}
		if(updated) {
			event.update(event.id);
			flash(ControllerUtil.FLASH_SUCCESS_KEY, "Event [" + event.name
					+ "] has been updated");
		} else {
			flash(ControllerUtil.FLASH_INFO_KEY, "No changes were made to Event [" + event.name
							+ "]");
		}
		return redirect(routes.Application.profileSearchEvents(0, "name",
						"asc", "", "name"));
	}

	/**
	 * Creates the default pfp.
	 * 
	 * @param event
	 *            the event
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
			@Group(SecurityRole.EVENT_ASSIST) })
	@SubjectPresent
	private static Pfp createDefaultPfp(Event event) {
		final Pfp pfp = new Pfp();
		pfp.content = event.content;
		pfp.event = event;
		pfp.goal = event.goal;
		pfp.imgUrl = event.imgUrl;
		pfp.name = "General Fund";
		pfp.title = "General Fund";
		pfp.userAdmin = event.userAdmin;
		pfp.pfpType = PfpType.GENERAL;
		pfp.dateCreated = new Date();
		pfp.save();
		Logger.trace("General Fund pfp right after save {}", ToStringBuilder.reflectionToString(pfp));
		Pfp updatedPfp = Pfp.findById(pfp.id);
		updatedPfp.slug = Pfp.toSlug(updatedPfp);
		updatedPfp.update();
		Logger.trace("General Fund pfp right after update {}", ToStringBuilder.reflectionToString(pfp));
		return updatedPfp;
	}

	/**
	 * Creates the default pfp.
	 * 
	 * @param event
	 *            the event
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
			@Group(SecurityRole.EVENT_ASSIST) })
	@SubjectPresent
	private static Pfp createDefaultSponsor(Event event) {
		final Pfp pfp = new Pfp();
		pfp.content = event.content;
		pfp.event = event;
		pfp.goal = event.goal;
		pfp.imgUrl = event.imgUrl;
		pfp.name = "Sponsor Fund";
		pfp.title = "Sponsor Fund";
		pfp.userAdmin = event.userAdmin;
		pfp.pfpType = PfpType.SPONSOR;
		pfp.dateCreated = new Date();
		pfp.save();
		Logger.trace("Sponsor Fund pfp right after save {}", ToStringBuilder.reflectionToString(pfp));
		Pfp updatedPfp = Pfp.findById(pfp.id);
		updatedPfp.slug = Pfp.toSlug(updatedPfp);
		updatedPfp.update();
		Logger.trace("Sponsor Fund pfp right after update {}", ToStringBuilder.reflectionToString(pfp));
		return updatedPfp;
	}

	
	/**
	 * Creates the sponsor page.
	 * 
	 * @param event
	 *            the event
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
			@Group(SecurityRole.EVENT_ASSIST) })
	@SubjectPresent
	private static void createSponsorPage(Event event) {
		final Sponsors sponsors = new Sponsors();
		sponsors.eventid = event.id;
		sponsors.content = "At this time there are no sponsorship opportunities available. If you are interested in becoming or having questions about sponsoring the " + event.name + " Please contact the event coordinator.";
		sponsors.name = "Sponsor";
		sponsors.title = "Sponsor";
		sponsors.save();
	}

	/**
	 * Creates the volunteer page.
	 * 
	 * @param event
	 *            the event
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
			@Group(SecurityRole.EVENT_ASSIST) })
	@SubjectPresent
	private static void createVolunteerPage(Event event) {
		final Volunteers volunteers = new Volunteers();
		volunteers.eventid = event.id;
		volunteers.name = "Volunteer";
		volunteers.title = "Volunteer";
		volunteers.content = "At this time there are no volunteering opportunities available. If you are interested in becoming or having questions about volunteering at the " + event.name + " Please contact the event coordinator.";
		volunteers.save();
	}

	/**
	 * Sets the date to midnight.
	 * 
	 * @param date
	 *            the date
	 * @return the date
	 */
	private static Date setDateToMidnight(Date date) {
		final Calendar cal = Calendar.getInstance(); // get calendar instance
		cal.setTime(date); // set cal to date
		cal.set(Calendar.HOUR_OF_DAY, 23); // set hour to midnight
		cal.set(Calendar.MINUTE, 0); // set minute in hour
		cal.set(Calendar.SECOND, 0); // set second in minute
		cal.set(Calendar.MILLISECOND, 0); // set millis in second
		return cal.getTime();
	}
}
