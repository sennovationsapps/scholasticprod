package controllers;

import base.utils.CronJobUtilsThread;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import com.avaje.ebean.ExpressionList;
import models.*;
import models.security.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.LoggerFactory;
import play.Logger;
import play.Routes;
import play.data.Form;
import play.db.ebean.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.donations.ProfileCashDonations;
import views.html.donations.ProfileRefundDonations;
import views.html.general.helpcenter;
import views.html.index;
import views.html.profile.*;
import views.html.restricted;

import java.util.*;

import static play.data.Form.form;


/**
 * The Class Application.
 */
public class Application extends Controller {

	/**
	 * Faqs.
	 * 
	 * @return the result
	 */

	private static final org.slf4j.Logger MAIL_LOGGER = LoggerFactory.getLogger("ScholasticReceiptsLogger");

	public static Result faqs() {
		return ok(helpcenter.render(form(ContactUs.class)));
	}

	/**
	 * Faqs events.
	 * 
	 * @return the result
	 */
	public static Result faqsEvents() {
		return ok(helpcenter.render(form(ContactUs.class)));
	}

	/**
	 * Index.
	 * 
	 * @return the result
	 */

	public static boolean schedulerTheadStatus = false;


	public static Result index() {




		//==========================new================06.10.2015======================start===================================//
		//System.out.println("before calling cronJobUtilsThread..");
		/*CronJobUtilsThread cronJobUtilsThread = new CronJobUtilsThread();

		System.out.println("schedulerTheadStatus :: "+schedulerTheadStatus);

		if(schedulerTheadStatus == false)
		{
			//System.out.println("schedulerTheadStatus == false");
			MAIL_LOGGER.info("before calling cronJobUtilsThread..");
			Thread t = new Thread(cronJobUtilsThread);
			t.setPriority(1);

			t.start();
			MAIL_LOGGER.info("after calling cronJobUtilsThread..");
			//System.out.println("after ");
			schedulerTheadStatus = true;

		}*/
		//System.out.println("after calling cronJobUtilsThread..");
		//==========================new================06.10.2015=======================end====================================//



		return ok(index.render());
	}

	/**
	 * Js routes.
	 * 
	 * @return the result
	 */
	public static Result jsRoutes() {
		return ok(
				Routes.javascriptRouter("jsRoutes",
						controllers.routes.javascript.Signup.forgotPassword()))
				.as("text/javascript");
	}

	/**
	 * Profile.
	 * 
	 * @return the result
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.PFP_ADMIN),
			@Group(SecurityRole.EVENT_ADMIN) })
	@SubjectPresent
	public static Result profile() {

/*

		//=======================new add================start=========================15.09.2015==============================//

		System.out.println("before calling cronJobUtilsThread..");
		CronJobUtilsThread cronJobUtilsThread = new CronJobUtilsThread();
		Thread t = new Thread(cronJobUtilsThread);
		t.setPriority(1);
		t.start();

		System.out.println("after calling cronJobUtilsThread..");



		*/
/*ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
		ses.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				// do some work
				System.out.println("within thread ScheduledExecutorService");
			}
		}, 0, 5, TimeUnit.MINUTES);  // every 5 minutes

		// when anything is entered, the task is stopped
		Scanner sc = new Scanner(System.in);
		String whatever = sc.next();
		// shutdown the executor
		ses.shutdown();
		try{
			ses.awaitTermination(15, TimeUnit.SECONDS);
		}catch(Exception ex){
			ex.printStackTrace();
		}
*//*




		//=======================new add================end=========================15.09.2015==============================//
*/


		final User localUser = ControllerUtil.getLocalUser(session());
		if (localUser == null) {
			return ok(index.render());
		}
		if(Logger.isDebugEnabled()) {
			final List<LinkedAccount> linkedAccts = localUser.linkedAccounts;
			if (CollectionUtils.isNotEmpty(linkedAccts)) {
				for (final LinkedAccount linkedAcct : linkedAccts) {
					Logger.debug("Linked Account - {}",
							ToStringBuilder.reflectionToString(linkedAcct));
				}
			}
			final List<MergedAccount> mergedAccts = localUser.mergedAccounts;
			if (CollectionUtils.isNotEmpty(mergedAccts)) {
				for (final MergedAccount mergedAcct : mergedAccts) {
					Logger.debug("Merged Account - {}",
							ToStringBuilder.reflectionToString(mergedAcct));
				}
			}
			Logger.debug("Permissions = {}",
					Arrays.toString(localUser.permissions.toArray()));
		}
		if (localUser.isUserInRole(SecurityRole.ROOT_ADMIN)
				|| localUser.isUserInRole(SecurityRole.SYS_ADMIN)) {
			return ok(profileMain.render(localUser));
		}
		if (localUser.isUserInRole(SecurityRole.EVENT_ADMIN)) {
			if (Event.existsByUserId(localUser.id)) {
			} else {
				session(ControllerUtil.FLASH_INFO_KEY,
						"Your account is now setup. These next steps will create your event.");
				return EventWorkflowMgmt.create();
			}
			return ok(profileMain.render(localUser));
		}
		if (!Pfp.existsByUserId(localUser.id)) {
			flash(ControllerUtil.FLASH_INFO_KEY,
					"Your account is now setup.  Your next step is to select the Event that you would like to participate it.");
			return EventMgmt.index();
		}



		return ok(profileMain.render(localUser));
	}

	/**
	 * Profile.
	 * 
	 * @return the result
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.PFP_ADMIN),
			@Group(SecurityRole.EVENT_ADMIN) })
	@SubjectPresent
	public static Result editProfile(Long id) {
		User localUser = ControllerUtil.getLocalUser(session());
		if (!ControllerUtil.isEqual(localUser.id, id)) {
			flash(ControllerUtil.FLASH_DANGER_KEY,
					"The account that has been accessed is not your own and therefore cannot be edited.");
			return redirect(routes.Application.profile());
		}
		User user = User.findById(id);
		final Form<User> userForm = form(User.class)
				.fill(user);
		return ok(profileEdit.render(user, userForm));
	}








	/************Start Code T-264***************/


	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN) })
	@SubjectPresent
	public static Result organizationalSearch() {
		System.out.println("within organizational search...");
		String type = request().getQueryString("type");
		String id = request().getQueryString("id");
		System.out.println("id within organizationalSearch :: "+id);
		ExpressionList<User> user = null;
		ExpressionList<Organization> orga = null;
		User user1=null;
		List<User> list = null;
		List<Organization> listorga = null;
		System.out.println("type :: "+type);
		if (StringUtils.equals(type, "email")) {
			System.out.println("StringUtils.equals(type, email)");

			user = User.findByEmailList(id);
			System.out.println("user :: "+user);
			list = user.findList();
			System.out.println(":::::::::::::::::::  list :::::::::::::::: "+list.size());
		}
		if (StringUtils.equals(type, "first")) {
			System.out.println("within type first..");
			user = User.findByFirst(id);
			list = user.findList();
			//System.out.println("list after type first.."+list);
		}
		if (StringUtils.equals(type, "last")) {
			user = User.findByLast(id);
			list = user.findList();
		}
		if (StringUtils.equals(type, "taxid")) {
			user = User.findByTaxidList(NumberUtils.createLong(id));
			list = user.findList();
		}






		if (StringUtils.equals(type, "organization")) {
			System.out.println("Name------------"+id);
			orga= Organization.findByName(id);
			listorga = orga.findList();
			System.out.println("Size-------------"+listorga.size());
			//list = user.findList();
			if(listorga.size()>0) {
				Organization org = listorga.get(0);
				user = User.findByOrganizationId(Long.parseLong(org.id+""));
				list = user.findList();
			}
		}








		Logger.debug("-----This are the params of type {} and id {}", type, id);
		Logger.debug("-----This is the user {}", user);
		models.security.User uservalue = new models.security.User();


		if (user == null) {
			return ok(organizationalSearch.render(ControllerUtil
					.getLocalUser(session()), list, uservalue, user1, form(User.class)));
		} else {
			return ok(organizationalSearch.render(ControllerUtil
					.getLocalUser(session()), list, uservalue, user1, form(User.class)));
		}


	}







	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN) })
	@SubjectPresent
	public static Result organizationEditProfiles() {
		String type = request().getQueryString("type");
		String id = request().getQueryString("id");
		User user = null;
		List<User> list = null;
		if(StringUtils.equals(type, "email")) {
			System.out.println("the id is :: "+id);
			user = User.findByEmail(id);
		}
		//===============new addtion get user by id=============start===================10.09.2015===========================================//


		if(StringUtils.equals(type, "userId")) {
			System.out.println("the id in userID is :: "+id);
			user = User.findByUserId(id);
		}

		//===============new addtion get user by id==============end====================10.09.2015===========================================//
		if(StringUtils.equals(type, "taxid")) {
			user = User.findByTaxid(NumberUtils.createLong(id));
		}
		Logger.debug("-----This are the params of type {} and id {}", type, id);
		Logger.debug("-----This is the user {}", user);
		models.security.User uservalue = new models.security.User();
		if(user == null) {
			return ok(organizationalSearch.render(ControllerUtil
					.getLocalUser(session()), list,uservalue,user, form(User.class)));
		} else {
			return ok(organizationalSearch.render(ControllerUtil
					.getLocalUser(session()), list,uservalue,user, form(User.class)
					.fill(user)));
		}
	}










	/************End Code T-264*****************/






















	/**
	 * Profile.
	 * 
	 * @return the result
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.PFP_ADMIN),
			@Group(SecurityRole.EVENT_ADMIN) })
	@SubjectPresent
	@Transactional
	public static Result saveProfile(Long id) {
		User localUser = ControllerUtil.getLocalUser(session());
		if (!localUser.isRootAdmin() && !localUser.isSysAdmin() && !ControllerUtil.isEqual(localUser.id, id)) {
			flash(ControllerUtil.FLASH_DANGER_KEY,
					"The account that has been accessed is not your own and therefore cannot be edited.");
			return redirect(routes.Application.profile());
		}
		final Form<User> userForm = form(User.class).bindFromRequest();
		if (userForm.hasErrors()) {
			return badRequest(profileEdit.render(userForm.get(), userForm));
		}
		User updatedUser = userForm.get();
		User user = User.findById(id);
		if(!StringUtils.equals(user.phone, updatedUser.phone)) {
			user.phone = ControllerUtil.stripPhone(updatedUser.phone);			
		}
		if(!StringUtils.equals(user.zip, updatedUser.zip)) {
			user.zip = updatedUser.zip;			
		}
		if(!StringUtils.equals(user.firstName, updatedUser.firstName)) {
			user.firstName = updatedUser.firstName;			
		}
		if(!StringUtils.equals(user.lastName, updatedUser.lastName)) {
			user.lastName = updatedUser.lastName;			
		}
		if(user.organization != null) {
			if(updatedUser.organization != null && !StringUtils.equals(user.organization.phone, updatedUser.organization.phone)) {
				user.organization.phone = ControllerUtil.stripPhone(updatedUser.organization.phone);
			}
			if((localUser.isRootAdmin() || localUser.isSysAdmin())) {
				if(!StringUtils.equals(user.email, updatedUser.email)) {
					user.email = updatedUser.email;
					
				}
			}
		}
		user.update();
		
		flash(ControllerUtil.FLASH_SUCCESS_KEY,
				"Profile has been updated for " + user.email);
		return redirect(routes.Application.profile());
	}
	
	/**
	 * Profile donations.
	 * 
	 * @return the result
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN) })
	@SubjectPresent
	public static Result profileDonationsCreate() {
		return ok(profileDonationsCreate.render(ControllerUtil
				.getLocalUser(session()), null));
	}


	/****************start*******************Bulk Cash Donation*******************24.09.2015**********************************/

	public static Result profileCashDonations(){
		final User localUser = ControllerUtil.getLocalUser(session());
		List<Event> events = new ArrayList<Event>();
		if(ControllerUtil.isUserInRole(models.security.SecurityRole.ROOT_ADMIN)){
			events = Event.findAllEvents();
		}else if(ControllerUtil.isUserInRole(SecurityRole.EVENT_ADMIN)){
			events = Event.findAllByUserId(localUser.id);
		}
		final Form<Donation> donationForm = form(Donation.class);
		//if(!Event.isLive(event) && (localUser == null || !ControllerUtil.isEqual(event.userAdmin.id, localUser.id)))

		if(events!= null && events.size()>0){
			return ok(ProfileCashDonations.render(localUser, events, null , null,donationForm, null, null));
		}else{
			return ok(ProfileCashDonations.render(localUser, null, null, null, donationForm, null, null));
		}

	}

	/****************start*******************Bulk Cash Donation*******************24.09.2015**********************************/

	/****************start*******************profileRefundDonations*******************14.01.2016**********************************/

	public static Result profileRefundDonations(){
		final User localUser = ControllerUtil.getLocalUser(session());
		List<Event> events = new ArrayList<Event>();
		if(ControllerUtil.isUserInRole(models.security.SecurityRole.ROOT_ADMIN)){
			events = Event.findAllEvents();
		}else if(ControllerUtil.isUserInRole(SecurityRole.EVENT_ADMIN)){
			events = Event.findAllByUserId(localUser.id);
		}
		final Form<Donation> donationForm = form(Donation.class);
		List<Transaction> transactions = new ArrayList<Transaction>();
		transactions = Transaction.findAllTransactions();
		Iterator transactionItr = transactions.iterator();

		List<Donation> donations = new ArrayList<Donation>();



		while(transactionItr.hasNext()) {
			try{
				Transaction transaction = (Transaction) transactionItr.next();
				System.out.println("transaction.mailSent :: " + transaction.mailSent);
				System.out.println("transaction.transid2:: " + transaction.donationTranId);
				Donation donation = Donation.findByTransactionNumber(transaction.donationTranId);
				/****new add*****/
				Date currDate = new Date();
				System.out.println("currDate.getTime() :: "+currDate.getTime());
				System.out.println("donation.dateCreated.getTime()"+donation.dateCreated.getTime());
				long diffInMillies = currDate.getTime() - donation.dateCreated.getTime();
				int diffInMillies1 = (int)(diffInMillies / (1000 * 60 * 60)) ;
				System.out.println("diffInMillies1 :: "+diffInMillies1);
				/****new add*****/
				donations.add(donation);
			}catch(Exception e)
			{

				e.printStackTrace();
			}

		}


		//if(!Event.isLive(event) && (localUser == null || !ControllerUtil.isEqual(event.userAdmin.id, localUser.id)))

		if(events!= null && events.size()>0){
			return ok(ProfileRefundDonations.render(localUser,transactions,donations));
		}else{
			return ok(ProfileRefundDonations.render(localUser,transactions,donations));
		}

	}

	/*****************end********************profileRefundDonations*******************14.01.2016**********************************/


	/**
	 * Profile donations.
	 * 
	 * @return the result
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN) })
	@SubjectPresent
	public static Result profileDonationsReconcile() {
		return ok(profileDonationsReconcile.render(ControllerUtil
				.getLocalUser(session()), form(Donation.DonationReconciliation.class)));
	}
	
	/**
	 * Profile donations.
	 * 
	 * @return the result
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN) })
	@SubjectPresent
	public static Result profileEditProfiles() {
		String type = request().getQueryString("type");
		String id = request().getQueryString("id");
		User user = null;
		if(StringUtils.equals(type, "email")) {
			user = User.findByEmail(id);
		}
		if(StringUtils.equals(type, "taxid")) {
			user = User.findByTaxid(NumberUtils.createLong(id));
		}
		Logger.debug("-----This are the params of type {} and id {}", type, id);
		Logger.debug("-----This is the user {}", user);
		if(user == null) {
			return ok(profileEditProfiles.render(ControllerUtil
					.getLocalUser(session()), user, form(User.class)));
		} else {
			return ok(profileEditProfiles.render(ControllerUtil
					.getLocalUser(session()), user, form(User.class)
					.fill(user)));
		}
	}
	
	/**
	 * Profile events.
	 * 
	 * @return the result
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN) })
	@SubjectPresent
	public static Result profileEvents() {
		return ok(profileEvents.render(ControllerUtil.getLocalUser(session())));
	}
	
	/**
	 * Profile events.
	 * 
	 * @return the result
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN) })
	@SubjectPresent
	public static Result profileSearchEvents(int page, String sortBy, String order,
					String filter, String fieldName) {
		System.out.println("fieldName :: "+fieldName);
		User localUser = ControllerUtil.getLocalUser(session());
		if(localUser.isEventAdmin()) {
			return ok(profileSearchEvents.render(
							Event.page(page, 10, sortBy, order, StringUtils.trimToEmpty(filter), fieldName, localUser), sortBy,
							order, filter, fieldName));
		} else if(localUser.isRootAdmin() || localUser.isSysAdmin()) {
			return ok(profileSearchEvents.render(
							Event.page(page, 10, sortBy, order, StringUtils.trimToEmpty(filter), fieldName, null), sortBy,
							order, filter, fieldName));
		} else {
			return redirect(routes.Application.profile());
		}
	}
	
	/**
	 * Profile events.
	 * 
	 * @return the result
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN) })
	@SubjectPresent
	public static Result profileSearchDonations(int page, String sortBy, String order,
					String filter, String fieldName) {
		System.out.println("fieldName :: "+fieldName);
		User localUser = ControllerUtil.getLocalUser(session());
		if(localUser.isEventAdmin()) {
			return ok(profileSearchDonations.render(
							Donation.page(page, 10, sortBy, order, StringUtils.trimToEmpty(filter), fieldName, localUser), sortBy,
							order, filter, fieldName,null,null));
		} else if(localUser.isRootAdmin() || localUser.isSysAdmin()) {
			return ok(profileSearchDonations.render(
							Donation.page(page, 10, sortBy, order, StringUtils.trimToEmpty(filter), fieldName, null), sortBy,
							order, filter, fieldName,null,null));
		} else {
			return redirect(routes.Application.profile());
		}
	}


	/*************start****with year filter type**************************************************************18.01.2016*********/

	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN) })
	@SubjectPresent
	public static Result profileSearchDonations1(int page, String sortBy, String order,
												String filter, String fieldName,String filter1, String fieldName1) {
		System.out.println("fieldName :: "+fieldName);
		System.out.println("fieldName1 :: "+fieldName1);
		User localUser = ControllerUtil.getLocalUser(session());
		if(localUser.isEventAdmin()) {
			return ok(profileSearchDonations.render(
					Donation.page1(page, 10, sortBy, order, StringUtils.trimToEmpty(filter), fieldName1, StringUtils.trimToEmpty(filter1), fieldName, localUser), sortBy,
					order, filter, fieldName, filter1, fieldName1));
		} else if(localUser.isRootAdmin() || localUser.isSysAdmin()) {
			return ok(profileSearchDonations.render(
					Donation.page1(page, 10, sortBy, order, StringUtils.trimToEmpty(filter), fieldName,StringUtils.trimToEmpty(filter1), fieldName1, null), sortBy,
					order, filter, fieldName, filter1, fieldName1));
		} else {
			return redirect(routes.Application.profile());
		}
	}
	/*************end****with year filter type**************************************************************18.01.2016*********/
	
	/**
	 * Profile events.
	 * 
	 * @return the result
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN), @Group(SecurityRole.PFP_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN) })
	@SubjectPresent
	public static Result profileSearchPfps(int page, String sortBy, String order,
					String filter, String fieldName) {
		System.out.println("fieldName :: "+fieldName);
		User localUser = ControllerUtil.getLocalUser(session());
		if(localUser.isEventAdmin() || localUser.isPfpAdmin()) {
			return ok(profileSearchPfps.render(
							Pfp.page(page, 10, sortBy, order, StringUtils.trimToEmpty(filter), fieldName, localUser), sortBy,
							order, filter, fieldName));
		} else if(localUser.isRootAdmin() || localUser.isSysAdmin()) {
			return ok(profileSearchPfps.render(
							Pfp.page(page, 10, sortBy, order, StringUtils.trimToEmpty(filter), fieldName, null), sortBy,
							order, filter, fieldName));
		} else {
			return redirect(routes.Application.profile());
		}
	}
	
	/**
	 * Profile pfps.
	 * 
	 * @return the result
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN) })
	@SubjectPresent
	public static Result profilePassword() {
		return ok(profilePassword.render(ControllerUtil.getLocalUser(session()), Signup.PASSWORD_RESET_FORM, TaxidSignup.PASSWORD_RESET_FORM));
	}
	
	/**
	 * Profile pfps.
	 * 
	 * @return the result
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.PFP_ADMIN),
			@Group(SecurityRole.EVENT_ADMIN) })
	@SubjectPresent
	public static Result profilePfps() {
		User user = ControllerUtil.getLocalUser(session());
		List<Pfp> pfps = null;
		if(ControllerUtil.isUserInRole(models.security.SecurityRole.PFP_ADMIN)) {
			pfps = Pfp.findByUserId(user.id);
		} else if(StringUtils.isNotEmpty(request().getQueryString("id"))) {
			Long id = Event.getIdFromSlug(request().getQueryString("id"));
			pfps = Pfp.findByEventId(id);
		}
		return ok(profilePfps.render(user, pfps));
	}

	/**
	 * Profile reports.
	 * 
	 * @return the result
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN) })
	@SubjectPresent
	public static Result profileReports() {
		Event event = null;
		if(StringUtils.isNotEmpty(request().getQueryString("id"))) {
			Long id = Event.getIdFromSlug(request().getQueryString("id"));
			event = Event.findById(id);
			event.serviceFee = 10.0;
		}
		return ok(profileReports.render(ControllerUtil.getLocalUser(session()), event));
	}

	/**
	 * Restricted.
	 * 
	 * @return the result
	 */
	@SubjectPresent
	public static Result restricted() {
		return ok(restricted.render(ControllerUtil.getLocalUser(session())));
	}

}
