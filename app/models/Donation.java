package models;

import base.utils.DateUtils;
import base.utils.SortUtils;
import com.avaje.ebean.*;
import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import models.Pfp.PfpType;
import models.security.User;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.NumberUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Pattern;
import play.db.ebean.Model;
import play.mvc.PathBindable;

import javax.persistence.*;
import java.net.URL;
import java.util.*;

// import scala.collection.generic.BitOperations.Int;

/**
 * Event entity managed by JPA
 */
@Entity
public class Donation extends Model implements PathBindable<Donation> {

	private static final org.slf4j.Logger MAIL_LOGGER = LoggerFactory.getLogger("ScholasticReceiptsLogger");
	
	public static final Finder<Long, Donation>	find	= new Finder<Long, Donation>(Long.class, Donation.class);
	
	public static class DonationReconciliation {
		@Constraints.Required
		@Formats.DateTime(pattern = "MM/dd/yyyy")
		public Date	reconcileFrom;
		
		@Constraints.Required
		@Formats.DateTime(pattern = "MM/dd/yyyy")
		public Date	reconcileTo;
	}
	
	@Constraints.Required

	public int				amount;
	
	@Pattern(value = "^[0-9]{3,4}$", message = "cvv.pattern")
	@Transient
	public String			ccCvvCode;
	
	@Formats.DateTime(pattern = "MM/yyyy")
	@Transient
	public Date				ccExpDate;
	
	@Pattern(value = "^[0-9]{13,19}$", message = "credit.pattern")
	@Transient
	public String			ccNum;
	
	@Transient
	public String			ccName;
	
	@Transient
	@Pattern(value = "(\\d{5}([\\-]\\d{4})?)", message = "zip.pattern")
	public String			ccZip;
	
	public String			checkNum;
	
	@MaxLength(value = 4)
	public String			ccDigits;
	
	@Formats.DateTime(pattern = "MM/dd/yyyy")
	public Date				dateCreated;
	
	@Formats.DateTime(pattern = "MM/dd/yyyy")
	public Date				datePaid;
	
	public String			donorMessage;
	
	public String			donorName;
	
	public DonationType		donationType;
	
	@Transient
	public int				donationTotal;
	
	@Transient
	public int				donationCount;
	
	@Constraints.Required
	@Constraints.Email
	public String			email;
	
	@ManyToOne(optional = false, cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	// @JoinColumn(name="eventId")
	@JsonBackReference
	public Event			event;
	
	public String			firstName;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long				id;
	
	public String			lastName;
	
	public boolean			payByCheck;
	
	@ManyToOne(optional = true, cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	// @JoinColumn(name="pfpId")
	@JsonBackReference
	public Pfp				pfp;

	/*public String           ccname;*/


//=============================start=========================01.10.2015=====================================//
	/*@Transient

	public String  collectionTypes;
	@Transient

	public String email1;
*/
	//=============================end===========================01.10.2015=====================================//

	//=====================02.10.2015================================start======================================================//
	@Transient
	public String statusOfBulkCashDonation;

	//=====================02.10.2015================================end========================================================//



//=======================change for the phno format=============start=============01.09.2015========================//



//=======================change for the phno format=============start=============01.09.2015========================//




	@Constraints.Required
		/*@Pattern(value = "^[0-9]$", message = "phone.pattern")*/
	@MaxLength(value = 3)
	@Pattern(value = "[0-9.+]{3}+", message = "A valid phone number is required")
	public String phPart1;




	@Constraints.Required
		/*@Pattern(value = "^[0-9]$", message = "phone.pattern")*/
	@MaxLength(value = 3)
	@Pattern(value = "[0-9.+]{3}+", message = "A valid phone number is required")
	public String phPart2;




	@Constraints.Required
		/*@Pattern(value = "^[0-9]$", message = "phone.pattern")*/
	@Pattern(value = "[0-9.+]{4}+", message = "A valid phone number is required")
	@MaxLength(value = 4)
	public String phPart3;


	@Constraints.Required
		/*@Pattern(value = "^[0-9]$", message = "phone.pattern")*/
	@MaxLength(value = 10)
	public String phone;


	//=======================change for the phno format==============end==============01.09.2015========================//




	/*@Constraints.Required

	@Pattern(value = "^[0-9]{3}-[0-9]{3}-[0-9]{4}$", message = "phone.pattern")
	@MaxLength(value = 12)
	public String			phone;*/
	//===============for image and web=================start==================================//

	/*public URL             imgUrl;

	public String           webUrl;*/
	//===============for image and web=================end==================================//
	
	public PaymentStatus	status;
	
	public PaymentType		paymentType;
	
	public String			transactionNumber;
	
	public String			invoiceNumber;
	
	@Pattern(value = "(\\d{5}([\\-]\\d{4})?)", message = "zip.pattern")
	public String			zipCode;

	/*****************For Img URL*****************/

	public URL imgUrl;

	public String           webUrl;


	
	@OneToOne(mappedBy = "donation", optional = true)
	@JsonManagedReference
	public SponsorItem		sponsorItem;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}






	//========start======rimi donation of event logic=========30.07.2015================================//
	public static boolean findDuplicateDonationToSameDonor(Long pfpId, String donorName, Long eventId ){
		System.out.println("within findDuplicateDonationToSameDonor....eventId :: "+eventId);
		return find.where().eq("pfp_id",pfpId).eq("donor_name",donorName).eq("event_id",eventId).select("id").findRowCount() > 0;
		//return find.where().eq("id", id).select("id, heroImgUrl, eventEnd, eventStart, slug, status, schoolId, fundraisingEnd, fundraisingStart, goal, name, userAdmin, generalFund").fetch("userAdmin").fetch("generalFund").findUnique();
	}
	//========end======rimi donation of event logic=========30.07.2015================================//











	public static boolean existsByEventId(Long eventId) {
		return find.where().eq("eventId", eventId).select("id").findRowCount() > 0;
	}
	
	public static boolean existsByPfpId(Long pfpId) {
		return find.where().eq("pfp.id", pfpId).select("id").findRowCount() > 0;
	}
	
	public static boolean existsByTransactionNumber(String transactionNumber) {
		return find.where().eq("transactionNumber", transactionNumber).select("id").findRowCount() > 0;
	}

	public static Donation findByTransactionNumber(String transactionNumber) {
		System.out.println("within findByTransactionNumber ");
		Donation donation = null;
		try{
			List<Donation> donations = find.where().eq("transactionNumber", transactionNumber).findList();
			System.out.println("donations : "+donations);
			if(donations!=null && donations.size()>0){
				donation = find.where().eq("transactionNumber", transactionNumber).findList().get(0);
				System.out.println("donation :: "+donation);
			}

		}catch(Exception ex){
			ex.printStackTrace();
			MAIL_LOGGER.error("*** error within findByTransactionNumber ***"+ex.getMessage());

		}

		return donation;
	}
/********************start******************difftimeBetweenTransaction**************18.01.2016******************************/

	public static int getDiffTimeByTransactionNumber(String transactionNumber){
		System.out.println("within getDiffTimeByTransactionNumber ");
		Donation donation = null;
		int diffInMillies1 = 0;
		String timeDiff = null;
		try{
			List<Donation> donations = find.where().eq("transactionNumber", transactionNumber).findList();
			System.out.println("donations : "+donations);
			if(donations!=null && donations.size()>0){
				donation = find.where().eq("transactionNumber", transactionNumber).findList().get(0);
				System.out.println("donation :: "+donation);
				/****new add*****/
				Date currDate = new Date();
				System.out.println("currDate.getTime() :: "+currDate.getTime());
				System.out.println("donation.dateCreated.getTime()"+donation.dateCreated.getTime());
				long diffInMillies = currDate.getTime() - donation.dateCreated.getTime();
				diffInMillies1 = (int)(diffInMillies / (1000 * 60 * 60)) ;
				System.out.println("diffInMillies1 :: "+diffInMillies1);
				/****new add*****/
				timeDiff = String.valueOf(diffInMillies1);
			}

		}catch(Exception ex){
			ex.printStackTrace();
			//MAIL_LOGGER.error("*** error within findByTransactionNumber ***"+ex.getMessage());

		}

		return diffInMillies1;
	}
	/*********************end*******************difftimeBetweenTransaction**************18.01.2016******************************/
	public static List<Donation> findAllByEventId(Long id) {
		final ExpressionList<Donation> donations = find.where().eq("event.id", id);
		if (donations != null) {
			return donations.findList();
		}
		return new ArrayList<Donation>();
	}
	
	public static List<Donation> findAllByEventIdMin(Long id) {
		/*final Query<Donation> donations = find.where().eq("event.id", id)
						.select("id, firstName, lastName, zipCode, email, phone, donationType, datePaid, paymentType, status, amount, event.name, pfp.name, donation.pfp.team.name, transactionNumber")
						.fetch("pfp").fetch("event");*/
		final Query<Donation> donations = find.where().eq("event.id", id)
				.select("id, firstName, lastName, zipCode, email, phone, donationType, datePaid, paymentType, status, amount, donation.pfp.team.name, transactionNumber")
				.fetch("pfp", "name").fetch("event", "name");
		if (donations != null) {
			return donations.findList();
		}
		return new ArrayList<Donation>();
	}
	
	public static List<Donation> findAllByEventIdAndCleared(Long id) {
		/*final Query<Donation> donations = find.where().eq("event.id", id).eq("status", "2")
						.select("id, firstName, lastName, zipCode, email, phone, donationType, datePaid, paymentType, status, amount, event.name, pfp.name, donation.pfp.team.name, transactionNumber")
						.fetch("pfp").fetch("event");*/
		final Query<Donation> donations = find.where().eq("event.id", id).eq("status", "2")
				.select("id, firstName, lastName, zipCode, email, phone, donationType, datePaid, paymentType, status, amount, donation.pfp.team.name, transactionNumber")
				.fetch("pfp","name").fetch("event","name");
		if (donations != null) {
			return donations.findList();
		}
		return new ArrayList<Donation>();
	}

	/******start*******getting cleared and cash donations*************09.10.2015**************************/
	public static List<Donation> findAllCashDonationsByEventIdAndCleared(Long id) {
		String sortBy = "dateCreated";
		String order = "desc";
		/*final Query<Donation> donations = find.where().eq("event.id", id).eq("status", "2").eq("payment_type","3")
				.select("id, firstName, lastName, zipCode, email, phone, donationType, datePaid, paymentType, status, amount, event.name, pfp.name, donation.pfp.team.name, transactionNumber")
				.fetch("pfp").fetch("event").orderBy(sortBy + " " + order);*/

		final Query<Donation> donations = find.where().eq("event.id", id).eq("status", "2").eq("payment_type","3")
				.select("id, firstName, lastName, zipCode, email, phone, donationType, datePaid, paymentType, status, amount, donation.pfp.team.name, transactionNumber")
				.fetch("pfp", "name").fetch("event", "name").orderBy(sortBy + " " + order);

		if (donations != null) {
			return donations.findList();
		}
		return new ArrayList<Donation>();
	}


	/*******end********getting cleared and cash donations*************09.10.2015**************************/

	//// findDonationPaymentCleared Added by  Suvadeep Datta

	public static List<Donation> findDonationPaymentCleared(Long id) {

		final Query<Donation> donationCleared = find.where().eq("status", "2").eq("event.id", id)
				                          .select("id, firstName, lastName, zipCode, email, phone, donationType, datePaid, paymentType, status, amount, event.name, pfp.name, donation.pfp.team.name, transactionNumber");
		if (donationCleared != null) {
			return donationCleared.findList();
		}
		return new ArrayList<Donation>();
	}
	
	public static List<Donation> findAllByEventIdAndDateRange(Long id, Date from, Date to) {
		/*final Query<Donation> donations = find.where().eq("event.id", id).gt("dateCreated", from)
						.le("dateCreated", to)
						.select("id, firstName, lastName, zipCode, email, phone, donationType, datePaid, paymentType, status, amount, event.name, pfp.name, donation.pfp.team.name, transactionNumber")
						.fetch("pfp").fetch("event");*/
		final Query<Donation> donations = find.where().eq("event.id", id).gt("dateCreated", from)
				.le("dateCreated", to)
				.select("id, firstName, lastName, zipCode, email, phone, donationType, datePaid, paymentType, status, amount, donation.pfp.team.name, transactionNumber")
				.fetch("pfp", "name").fetch("event", "name");
		if (donations != null) {
			return donations.findList();
		}
		return new ArrayList<Donation>();
	}


	public static List<Donation> findAllByEventIdAndDateRange1(Long id, Date from, Date to) {
		/*final Query<Donation> donations = find.where().eq("event.id", id).gt("dateCreated", from)
						.le("dateCreated", to)
						.select("id, firstName, lastName, zipCode, email, phone, donationType, datePaid, paymentType, status, amount, event.name, pfp.name, donation.pfp.team.name, transactionNumber")
						.fetch("pfp").fetch("event");*/
		final Query<Donation> donations = find.where().eq("event.id", id).gt("dateCreated", from)
				.le("dateCreated", to)
				.select("id, firstName, lastName, zipCode, email, phone, donationType, datePaid, paymentType, status, amount, donation.pfp.team.name, transactionNumber")
				.fetch("pfp", "name").fetch("event", "name").fetch("transaction","transId");
		if (donations != null) {
			return donations.findList();
		}
		return new ArrayList<Donation>();
	}



	public static List<Donation> findAllByEventIdAndDateRangeAndPaymentType(Long id, Date from, Date to,
					String paymentType) {
		if (StringUtils.isEmpty(paymentType)) {
			return Donation.findAllByEventIdAndDateRange(id, from, to);
		}
		/*final Query<Donation> donations = find.where().eq("event.id", id).eq("paymentType", paymentType)
						.gt("dateCreated", from).le("dateCreated", to)
						.select("id, firstName, lastName, zipCode, email, phone, donationType, datePaid, paymentType, status, amount, event.name, pfp.name, donation.pfp.team.name, transactionNumber")
						.fetch("pfp").fetch("event");*/

		final Query<Donation> donations = find.where().eq("event.id", id).eq("paymentType", paymentType)
				.gt("dateCreated", from).le("dateCreated", to)
				.select("id, firstName, lastName, zipCode, email, phone, donationType, datePaid, paymentType, status, amount, donation.pfp.team.name, transactionNumber")
				.fetch("pfp", "name").fetch("event","name");

		if (donations != null) {
			return donations.findList();
		}
		return new ArrayList<Donation>();
	}
	
	public static List<Donation> findAllByEventIdAndClearedAndPaymentType(Long id, String paymentType) {
		if (StringUtils.isEmpty(paymentType)) {
			return Donation.findAllByEventIdAndCleared(id);
		}
		/*final Query<Donation> donations = find.where().eq("event.id", id).eq("paymentType", paymentType)
						.eq("status", "2")
						.select("id, firstName, lastName, zipCode, email, phone, donationType, datePaid, paymentType, status, amount, event.name, pfp.name, donation.pfp.team.name, transactionNumber")
						.fetch("pfp").fetch("event");*/

		final Query<Donation> donations = find.where().eq("event.id", id).eq("paymentType", paymentType)
				.eq("status", "2")
				.select("id, firstName, lastName, zipCode, email, phone, donationType, datePaid, paymentType, status, amount, donation.pfp.team.name, transactionNumber")
				.fetch("pfp","name").fetch("event","name");

		if (donations != null) {
			return donations.findList();
		}
		return new ArrayList<Donation>();
	}
	
	public static List<Donation> findAllByEventIdAndPaymentType(Long id, String paymentType) {
		if (StringUtils.isEmpty(paymentType)) {
			return Donation.findAllByEventIdMin(id);
		}
		/*final Query<Donation> donations = find.where()
						.eq("event.id", id).eq("paymentType", paymentType)
						.select("id, firstName, lastName, zipCode, email, phone, donationType, datePaid, paymentType, status, amount, event.name, pfp.name, donation.pfp.team.name, transactionNumber")
						.fetch("pfp").fetch("event");*/
		final Query<Donation> donations = find.where()
				.eq("event.id", id).eq("paymentType", paymentType)
				.select("id, firstName, lastName, zipCode, email, phone, donationType, datePaid, paymentType, status, amount, donation.pfp.team.name, transactionNumber")
				.fetch("pfp","name").fetch("event","name");

		if (donations != null) {
			return donations.findList();
		}
		return new ArrayList<Donation>();
	}
	
	public static List<Donation> findAllByEventIdAndOptions(Long id, Map<String, String> options) {
		System.out.println("within findAllByEventIdAndOptions...");
		final ExpressionList<Donation> donations = find.where()
						.eq("event.id", id);
						if(options.containsKey("paymentType") && StringUtils.isNotEmpty(options.get("paymentType"))) {
							donations.eq("paymentType", options.get("paymentType"));	
						}
						if(options.containsKey("status") && StringUtils.isNotEmpty(options.get("status"))) {
							donations.eq("status", options.get("status"));	
						}
						if(options.containsKey("fromDate")) {
							donations.gt("dateCreated", DateUtils.parseDate(options.get("fromDate")).get());	
						}
						if(options.containsKey("toDate")) {
							donations.lt("dateCreated", DateUtils.parseDate(options.get("toDate")).get());	
						}
		/*final Query<Donation> queryDonations = donations.select("id, firstName, lastName, zipCode, email, phone, donationType, dateCreated, datePaid, paymentType, status, amount, event.name, pfp.id, pfp.name, pfp.team.name, transactionNumber")
						.fetch("pfp").fetch("event");*/
		/*final Query<Donation> queryDonations = donations.select("id, firstName, lastName, zipCode, email, phone, donationType, dateCreated, datePaid, paymentType, status, amount, event.name, pfp.id, pfp.name, pfp.team.name, transactionNumber")
				.fetch("pfp").fetch("event");*/
		final Query<Donation> queryDonations = donations.select("id, firstName, lastName, zipCode, email, phone, donationType, dateCreated, datePaid, paymentType, status, amount, transactionNumber")
				.fetch("pfp", "name").fetch("pfp", "team").fetch("pfp.team", "name").fetch("event","name");

		if(StringUtils.isEmpty(options.get("totalByPfp"))) {
			queryDonations.orderBy("pfp.id");
		}
		if (queryDonations != null) {
			return queryDonations.findList();
		}
		return new ArrayList<Donation>();
	}





	/********************start*****************************21.01.2016*********************************/
	public static List<Donation> findAllByEventIdAndOptionsForReconcile(Long id, Map<String, String> options) {
		System.out.println("within findAllByEventIdAndOptions...");
		final ExpressionList<Donation> donations = find.where()
				.eq("event.id", id);
		if(options.containsKey("paymentType") && StringUtils.isNotEmpty(options.get("paymentType"))) {
			donations.eq("paymentType", options.get("paymentType"));
		}
		if(options.containsKey("status") && StringUtils.isNotEmpty(options.get("status"))) {
			donations.eq("status", options.get("status"));
		}
		if(options.containsKey("fromDate")) {
			donations.gt("dateCreated", DateUtils.parseDate(options.get("fromDate")).get());
		}
		if(options.containsKey("toDate")) {
			donations.lt("dateCreated", DateUtils.parseDate(options.get("toDate")).get());
		}
		/*final Query<Donation> queryDonations = donations.select("id, firstName, lastName, zipCode, email, phone, donationType, dateCreated, datePaid, paymentType, status, amount, event.name, pfp.id, pfp.name, pfp.team.name, transactionNumber")
						.fetch("pfp").fetch("event");*/
		/*final Query<Donation> queryDonations = donations.select("id, firstName, lastName, zipCode, email, phone, donationType, dateCreated, datePaid, paymentType, status, amount, event.name, pfp.id, pfp.name, pfp.team.name, transactionNumber")
				.fetch("pfp").fetch("event");*/
		/*final Query<Donation> queryDonations = donations.select("id, firstName, lastName, zipCode, email, phone, donationType, dateCreated, datePaid, paymentType, status, amount, transactionNumber")
				.fetch("pfp", "name").fetch("pfp", "team").fetch("pfp.team", "name").fetch("event","name");

		if(StringUtils.isEmpty(options.get("totalByPfp"))) {
			queryDonations.orderBy("pfp.id");
		}
		if (queryDonations != null) {
			return queryDonations.findList();
		}*/
		if (donations != null) {
			return donations.findList();
		}
		return new ArrayList<Donation>();
		//return new ArrayList<Donation>();
	}
	/*********************end******************************21.01.2016*********************************/
	/*****start******19.01.2016**************************************************/


	public static List<Donation> findAllAmountsByEventIdAndOptions(Long id, Map<String, String> options) {
		System.out.println("within findAllByEventIdAndOptions...");
		final ExpressionList<Donation> donations = find.where()
				.eq("event.id", id);
		if(options.containsKey("paymentType") && StringUtils.isNotEmpty(options.get("paymentType"))) {
			donations.eq("paymentType", options.get("paymentType"));
		}
		if(options.containsKey("status") && StringUtils.isNotEmpty(options.get("status"))) {
			donations.eq("status", options.get("status"));
		}
		if(options.containsKey("fromDate")) {
			donations.gt("dateCreated", DateUtils.parseDate(options.get("fromDate")).get());
		}
		if(options.containsKey("toDate")) {
			donations.lt("dateCreated", DateUtils.parseDate(options.get("toDate")).get());
		}
		/*final Query<Donation> queryDonations = donations.select("id, firstName, lastName, zipCode, email, phone, donationType, dateCreated, datePaid, paymentType, status, amount, event.name, pfp.id, pfp.name, pfp.team.name, transactionNumber")
						.fetch("pfp").fetch("event");*/
		/*final Query<Donation> queryDonations = donations.select("id, firstName, lastName, zipCode, email, phone, donationType, dateCreated, datePaid, paymentType, status, amount, event.name, pfp.id, pfp.name, pfp.team.name, transactionNumber")
				.fetch("pfp").fetch("event");*/
		final Query<Donation> queryDonations = donations.select("id, firstName, lastName, zipCode, email, phone, donationType, dateCreated, datePaid, paymentType, status, amount, transactionNumber")
				.fetch("pfp","name").fetch("pfp","team").fetch("pfp.team","name").fetch("event","name");


		if (queryDonations != null) {
			return queryDonations.findList();
		}
		return new ArrayList<Donation>();
	}


	/*****end********19.01.2016**************************************************/
	
	public static List<Donation> findByEventAndTeamId(Long id, Long teamId) {
		final ExpressionList<Donation> donations = find.where().eq("event.id", id).eq("pfp.team.id", teamId)
						.or(Expr.eq("status", "2"), Expr.eq("status", "0"));
		if (donations != null) {
			return donations.findList();
		}
		return new ArrayList<Donation>();
	}
	
	public static List<Donation> findByEventId(Long id) {
		final ExpressionList<Donation> donations = find.where().eq("event.id", id)
						.or(Expr.eq("status", "2"), Expr.eq("status", "0"));
		if (donations != null) {
			return donations.findList();
		}
		return new ArrayList<Donation>();
	}
	
	public static List<Donation> findByEventExceptSponsor(Event event) {
		final ExpressionList<Donation> donations = find.where().eq("event.id", event.id)
						.or(Expr.eq("status", "2"), Expr.eq("status", "0")).ne("donationType", "3");
		if (donations != null) {
			return donations.findList();
		}
		return new ArrayList<Donation>();
	}
	
	public static List<Donation.DonationsByPfp> findByEventExceptSponsorMinPfp(Long eventId) {
		List<Donation.DonationsByPfp> donations = new ArrayList<Donation.DonationsByPfp>();
		String sql = "SELECT donation.pfp_id, pfp.name, donation.amount, donation.private_account from donation, pfp where donation.pfp_id = pfp.id and donation.donation_type != 3 and (donation.status = 0 or donation.status = 2) and donation.event_id = :eventId order by donation.pfp_id";
		
		SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
		sqlQuery.setParameter("eventId", eventId);
		
		// execute the query returning a List of MapBean objects
		List<SqlRow> list = sqlQuery.findList();
		if (CollectionUtils.isNotEmpty(list)) {
			for (SqlRow row : list) {
				Donation.DonationsByPfp donationByPfp = new Donation.DonationsByPfp();
				donationByPfp.amount = row.getInteger("amount");
				donationByPfp.id = row.getLong("pfp_id");
				donationByPfp.name = row.getString("pfp.name");
				donationByPfp.isPrivate = row.getBoolean("pfp.name");
				donations.add(donationByPfp);
			}
		}
		if (donations == null) {
			return new ArrayList<Donation.DonationsByPfp>();
		}
		return donations;
	}
	
	public static List<Donation.DonationTotals> findByEventExceptSponsorTotals(Long eventId) {
		List<Donation.DonationTotals> donations = new ArrayList<Donation.DonationTotals>();
		System.out.println("eventId findByEventExceptSponsorTotals :: "+eventId);
		//String sql = "SELECT donation.pfp_id, pfp.name as pfp_name, pfp.private_acct, donation.id as donation_id, team.id as team_id, team.name as team_name, donation.amount from donation, pfp, team where donation.pfp_id = pfp.id and donation.donation_type != 3 and (donation.status = 0 or donation.status = 2) and donation.event_id = :eventId and pfp.team_id = team.id order by team.id;";
		//===================14.09.2015============================start===========================//
		/*String sql = "SELECT donation.status, donation.donation_type, donation.pfp_id, pfp.name as pfp_name, pfp.private_acct, donation.id as donation_id, team.id as team_id, team.name as team_name, donation.amount from donation, pfp, team where donation.pfp_id = pfp.id and donation.donation_type != 3 and (donation.status = 0 or donation.status = 2) and donation.event_id = :eventId and pfp.team_id = team.id order by team.id;";*/
		//===================14.09.2015============================start===========================//

		//===================16.09.2015============================start===========================//
		String sql = "SELECT donation.status, donation.donation_type, donation.pfp_id, pfp.name as pfp_name, pfp.private_acct, donation.id as donation_id, team.id as team_id, team.name as team_name, donation.amount from donation, pfp, team where donation.pfp_id = pfp.id and donation.donation_type != 3 and  donation.status = 2 and donation.event_id = :eventId and pfp.team_id = team.id order by team.id;";
		//===================16.09.2015=============================end============================//



		SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
		sqlQuery.setParameter("eventId", eventId);
		
		// execute the query returning a List of MapBean objects
		List<SqlRow> list = sqlQuery.findList();
		if (CollectionUtils.isNotEmpty(list)) {
			for (SqlRow row : list) {
				Donation.DonationTotals donationByPfp = new Donation.DonationTotals();
				donationByPfp.amount = row.getInteger("amount");
				donationByPfp.pfpId = row.getLong("pfp_id");
				donationByPfp.pfpName = row.getString("pfp_name");
				donationByPfp.teamId = row.getLong("team_id");
				donationByPfp.teamName = row.getString("team_name");
				donationByPfp.donationId = row.getLong("donation_id");
				donationByPfp.isPrivate = row.getBoolean("private_acct");
				//===================14.09.2015============================start===========================//

			//	System.out.println("row.getInteger:status "+row.getInteger("status"));
				//PaymentStatus paymentStatus=PaymentStatus.get(row.getInteger("status") + "");
				int id=row.getInteger("status");

				if(id == 0){
					donationByPfp.status = PaymentStatus.APPROVED;
				}else if(id == 1){
					donationByPfp.status = PaymentStatus.PENDING;

					//actualPaymentStatusValue = "Pending";

				}else if(id == 2){
					donationByPfp.status = PaymentStatus.CLEARED;

					//actualPaymentStatusValue = "Cleared";

				}else if(id == 3){
					donationByPfp.status = PaymentStatus.REFUNDED;

				//	actualPaymentStatusValue = "Refunded";

				}else {
					donationByPfp.status = PaymentStatus.FAILED;
					//actualPaymentStatusValue = "Failed";

				}












				//donationByPfp.status	=  PaymentStatus.get(row.getInteger("status") + "");


				if(donationByPfp.pfpName.equals("FRIENDS OF DR JOEY JONES")) {
					System.out.println("**************************************");

					System.out.println("id :: " + id);
					System.out.println("donationByPfp.pfpName :: " + donationByPfp.pfpName);
					System.out.println("donationByPfp.status :: " + donationByPfp.status);
					System.out.println("row.getInteger:donation_type " + row.getInteger("donation_type"));
					System.out.println("**************************************");
				} else if(donationByPfp.pfpName.equals("Morgan Tinsley")) {
					System.out.println("**************************************");

					System.out.println("id :: " + id);
					System.out.println("donationByPfp.pfpName :: " + donationByPfp.pfpName);
					System.out.println("donationByPfp.status :: " + donationByPfp.status);
					System.out.println("row.getInteger:donation_type " + row.getInteger("donation_type"));
					System.out.println("**************************************");
				}




						int paymentId = row.getInteger("donation_type");



				 if(paymentId == 1){
					// System.out.println("PaymentType.CREDIT :: "+PaymentType.CREDIT);
					donationByPfp.paymentType = PaymentType.CREDIT;

					//actualPaymentStatusValue = "Pending";

				}else if(paymentId == 2){
					// System.out.println("donationByPfp.paymentType = PaymentType.CHECK :: "+donationByPfp.paymentType);
					donationByPfp.paymentType = PaymentType.CHECK;

					//actualPaymentStatusValue = "Cleared";

				}else {
					// System.out.println("donationByPfp.paymentType = PaymentType.CASH :: "+donationByPfp.paymentType);
					donationByPfp.paymentType = PaymentType.CASH;
					//actualPaymentStatusValue = "Failed";

				}





			//	donationByPfp.paymentType	=	PaymentType.get(row.getInteger("donation_type")+"");
				//System.out.println("donationByPfp.paymentType :: "+donationByPfp.paymentType);




				//===================14.09.2015============================end=============================//
				donations.add(donationByPfp);
			}
		}
		if (donations == null) {
			return new ArrayList<Donation.DonationTotals>();
		}
		return donations;
	}
	
	public static List<Donation.DonationsByTeam> findByEventExceptSponsorMinTeam(Long eventId) {
		List<Donation.DonationsByTeam> donations = new ArrayList<Donation.DonationsByTeam>();
		/*String sql = "SELECT team.id, team.name, donation.amount from donation, pfp, team where donation.pfp_id = pfp.id and donation.donation_type != 3 and (donation.status = 0 or donation.status = 2) and donation.event_id = :eventId and pfp.team_id = team.id order by donation.pfp_id;";*/
        /************start***************25.09.2015**************************in clear state********************************************/

		String sql = "SELECT team.id, team.name, donation.amount, donation.status, donation.donation_type  from donation, pfp, team where donation.pfp_id = pfp.id and donation.donation_type != 3 and  donation.status = 2 and donation.event_id = :eventId and pfp.team_id = team.id order by donation.pfp_id;";

		/*************end****************25.09.2015**************************in clear state********************************************/
		SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
		sqlQuery.setParameter("eventId", eventId);
		
		// execute the query returning a List of MapBean objects
		List<SqlRow> list = sqlQuery.findList();
		if (CollectionUtils.isNotEmpty(list)) {
			for (SqlRow row : list) {
				Donation.DonationsByTeam donationByTeam = new Donation.DonationsByTeam();
				donationByTeam.amount = row.getInteger("amount");
				donationByTeam.id = row.getLong("id");
				donationByTeam.name = row.getString("name");

				//===================25.09.2015============================start===========================//

				//	System.out.println("row.getInteger:status "+row.getInteger("status"));
				//PaymentStatus paymentStatus=PaymentStatus.get(row.getInteger("status") + "");
				int id=row.getInteger("status");

				if(id == 0){
					donationByTeam.status = PaymentStatus.APPROVED;
				}else if(id == 1){
					donationByTeam.status = PaymentStatus.PENDING;

					//actualPaymentStatusValue = "Pending";

				}else if(id == 2){
					donationByTeam.status = PaymentStatus.CLEARED;

					//actualPaymentStatusValue = "Cleared";

				}else if(id == 3){
					donationByTeam.status = PaymentStatus.REFUNDED;

					//	actualPaymentStatusValue = "Refunded";

				}else {
					donationByTeam.status = PaymentStatus.FAILED;
					//actualPaymentStatusValue = "Failed";

				}












				//donationByPfp.status	=  PaymentStatus.get(row.getInteger("status") + "");


				/*if(donationByTeam.pfpName.equals("FRIENDS OF DR JOEY JONES")) {
					System.out.println("**************************************");

					System.out.println("id :: " + id);
					System.out.println("donationByPfp.pfpName :: " + donationByPfp.pfpName);
					System.out.println("donationByPfp.status :: " + donationByPfp.status);
					System.out.println("row.getInteger:donation_type " + row.getInteger("donation_type"));
					System.out.println("**************************************");
				} else if(donationByPfp.pfpName.equals("Morgan Tinsley")) {
					System.out.println("**************************************");

					System.out.println("id :: " + id);
					System.out.println("donationByPfp.pfpName :: " + donationByPfp.pfpName);
					System.out.println("donationByPfp.status :: " + donationByPfp.status);
					System.out.println("row.getInteger:donation_type " + row.getInteger("donation_type"));
					System.out.println("**************************************");
				}*/




				int paymentId = row.getInteger("donation_type");



				if(paymentId == 1){
					// System.out.println("PaymentType.CREDIT :: "+PaymentType.CREDIT);
					donationByTeam.paymentType = PaymentType.CREDIT;

					//actualPaymentStatusValue = "Pending";

				}else if(paymentId == 2){
					// System.out.println("donationByPfp.paymentType = PaymentType.CHECK :: "+donationByPfp.paymentType);
					donationByTeam.paymentType = PaymentType.CHECK;

					//actualPaymentStatusValue = "Cleared";

				}else {
					// System.out.println("donationByPfp.paymentType = PaymentType.CASH :: "+donationByPfp.paymentType);
					donationByTeam.paymentType = PaymentType.CASH;
					//actualPaymentStatusValue = "Failed";

				}





				//	donationByPfp.paymentType	=	PaymentType.get(row.getInteger("donation_type")+"");
				System.out.println("donationByTeam.paymentType :: "+donationByTeam.paymentType);




				//===================25.09.2015============================end=============================//
				donations.add(donationByTeam);
			}
		}
		if (donations == null) {
			return new ArrayList<Donation.DonationsByTeam>();
		}
		return donations;
	}
	
	public static class DonationTotals implements Comparable {



		public DonationTotals() {
			super();

			// TODO Auto-generated constructor stub
		}
		
		public DonationTotals(int amount, Long pfpId, String pfpName, Long teamId, String teamName, Long donationId,
						boolean isPrivate) {
			super();
			this.amount = amount;
			this.pfpId = pfpId;
			this.pfpName = pfpName;
			this.teamId = teamId;
			this.teamName = teamName;
			this.donationId = donationId;
			this.isPrivate = isPrivate;
		}
		
		public int		amount;
		public Long		pfpId;
		public String	pfpName;
		public Long		teamId;
		public String	teamName;
		public Long		donationId;
		public boolean	isPrivate;


		//============start====================14.09.2015=====================================//

		public PaymentStatus	status;

		public PaymentType		paymentType;
		//=============end=====================14.09.2015=====================================//
		@Override
		public int compareTo(Object arg0) {
			return this.pfpName.compareTo(((DonationTotals) arg0).pfpName);
		}
	}
	
	public static class DonationsByPfp implements Comparable {
		
		public DonationsByPfp(int amount, Long id, String name, boolean isPrivate) {
			super();
			this.amount = amount;
			this.id = id;
			this.name = name;
			this.isPrivate = isPrivate;
		}
		
		public DonationsByPfp(DonationTotals totals) {
			super();
			this.amount = totals.amount;
			this.id = totals.pfpId;
			this.name = totals.pfpName;
			this.isPrivate = totals.isPrivate;
			this.status = totals.status;
			this.paymentType = totals.paymentType;
		}
		
		public DonationsByPfp() {
			super();
			// TODO Auto-generated constructor stub
		}
		
		public int		amount;
		public Long		id;
		public String	name;
		public boolean	isPrivate;

		//==========================start=========================14.09.2015=============================//
		public PaymentStatus	status;

		public PaymentType		paymentType;
		//==========================end===========================14.09.2015=============================//
		
		@Override
		public int compareTo(Object arg0) {
			return this.name.compareTo(((DonationsByPfp) arg0).name);
		}

		//=========delete===========start=========//

		//=========delete============end==========//
	}
	
	public static class DonationsByTeam implements Comparable {
		
		public DonationsByTeam(int amount, Long teamId, String name) {
			super();
			this.amount = amount;
			this.id = teamId;
			this.name = name;
		}
		
		public DonationsByTeam(DonationTotals totals) {
			super();
			this.amount = totals.amount;
			this.id = totals.teamId;
			this.name = totals.teamName;
		}
		
		public DonationsByTeam() {
			super();
			// TODO Auto-generated constructor stub
		}
		
		public int		amount;
		public Long		id;
		public String	name;

		//==========================start=========================25.09.2015=============================//
		public PaymentStatus	status;

		public PaymentType		paymentType;
		//==========================end===========================25.09.2015=============================//
		
		@Override
		public int compareTo(Object arg0) {
			return this.name.compareTo(((DonationsByTeam) arg0).name);
		}
	}
	
	public static List<Donation> findByEventIdAnonTotal(Long id) {
		final ExpressionList<Donation> donations = find.where().eq("event.id", id).eq("pfp", null)
						.or(Expr.eq("status", "2"), Expr.eq("status", "0"));
		if (donations != null) {
			return donations.findList();
		}
		return new ArrayList<Donation>();
	}
	
	public static List<Donation> findByFromAndToDate(Date from, Date to) {
		final ExpressionList<Donation> donations = find.where().gt("dateCreated", from).le("dateCreated", to)
						.eq("paymentType", "1").eq("status", "0");
		if (donations != null) {
			return donations.findList();
		}
		return new ArrayList<Donation>();
	}
	
	public static Donation findById(Long id) {
		return find.byId(id);
	}

	//==========================findByTransactionNo================start=====================15.09.2015========================//

	/*public static Donation findByTransactionNo(Long transactionNumber) {
		return find.byId(transactionNumber);
	}*/

	//==========================findByTransactionNo=================end======================15.09.2015========================//
	
	public static Donation findByIdWithMin(Long id) {
		/*return find.where().eq("id", id).select("id, amount, donorName, pfp.id, pfp.name, transactionNumber, ccDigits, paymentType, status, event.id, event.name, invoiceNumber").fetch("pfp").fetch("event").findUnique();*/

		return find.where().eq("id", id).select("id, amount, donorName, transactionNumber, ccDigits, paymentType, status, invoiceNumber").fetch("pfp","name").fetch("event","name").findUnique();

	}
	
	public static List<Donation> findByPfpId(Long id) {
		final ExpressionList<Donation> donations = find.where().eq("pfp.id", id)
						.or(Expr.eq("status", "2"), Expr.eq("status", "0"));
		if (donations != null) {
			return donations.findList();
		}
		return new ArrayList<Donation>();
	}
	
	public static int getTotalAnonDonations(Long id) {
		return getTotalDonations(findByEventIdAnonTotal(id));
	}
	
	public static int getTotalDonations(List<Donation> donations) {
		int total = 0;
		for (final Donation donation : donations) {
			//======================start============================16.09.2015============================//
			if(donation.status.getValue().equalsIgnoreCase("Cleared")){
				total += donation.amount;
			}
			//=======================end=============================16.09.2015============================//
		}
		return total;
	}
	
	public static int getTotalEventDonationsByUserId(Long userId) {
		List<Object> eventIds = (List<Object>) Event.findAllIdsByUserId(userId);
		int total = 0;
		for (Object eventId : eventIds) {
			total = total + getTotalDonations(findByEventId((Long) eventId));
		}
		return total;
	}
	
	public static int getTotalEventDonations(Long id) {
		return getTotalDonations(findByEventId(id));
	}
	
	public static Map<String, Map<Long, ?>> getTotalAdminDonations(Event event) {
		return Donation.getTotalAdminDonations(event, null);
	}
	
	public static Map<String, Map<Long, ?>> getTotalAdminDonations(Event event, Team pfpTeam) {
		final Map<Long, Donation.DonationsByPfp> pfpTotals = new HashMap<Long, Donation.DonationsByPfp>();
		final Map<Long, Donation.DonationsByTeam> teamTotals = new HashMap<Long, Donation.DonationsByTeam>();
		final List<Donation.DonationTotals> donations = findByEventExceptSponsorTotals(event.id);
		for (final Donation.DonationTotals donation : donations) {
			if(pfpTeam == null || (pfpTeam != null && ObjectUtils.compare(pfpTeam.id, donation.teamId) == 0)) {
				if (pfpTotals.containsKey(donation.pfpId)) {
					if(donation.status.getValue().equalsIgnoreCase("Cleared")){
						pfpTotals.get(donation.pfpId).amount = pfpTotals.get(donation.pfpId).amount + donation.amount;
					}

					// pfpTotals.put(donation.pfpId,
					// (pfpTotals.get(donation.pfpId).amount + donation.amount));
				} else {
					pfpTotals.put(donation.pfpId, new Donation.DonationsByPfp(donation));
				}
			}
			if (teamTotals.containsKey(donation.teamId)) {
				if(donation.status.getValue().equalsIgnoreCase("Cleared")){
					teamTotals.get(donation.teamId).amount = teamTotals.get(donation.teamId).amount + donation.amount;
				}
				/*teamTotals.get(donation.teamId).amount = teamTotals.get(donation.teamId).amount + donation.amount;*/
				// teamTotals.put(donation.teamId,
				// (teamTotals.get(donation.teamId).amount + donation.amount));
			} else {
				teamTotals.put(donation.teamId, new Donation.DonationsByTeam(donation));
			}
			
		}
		
		List<Pfp> pfps = null;
		if(pfpTeam == null) {
			pfps =	Pfp.findByEventExceptSponsor(event);
		} else {
			pfps = Pfp.findByEventAndTeamExceptSponsor(event, pfpTeam);
		}
		for (Pfp pfp : pfps) {
			if (!pfpTotals.containsKey(pfp.id)) {
				int amount = 0;
				if(pfp.pfpType == PfpType.GENERAL) {
					amount = Donation.getTotalPfpDonations(pfp.id);
				}
				pfpTotals.put(pfp.id, new Donation.DonationsByPfp(amount, pfp.id, pfp.name, pfp.privateAcct));
			}
		}
		List<Team> teams = Team.findByEventId(event.id);
		for (Team team : teams) {
			if (!teamTotals.containsKey(team.id)) {
				teamTotals.put(team.id, new Donation.DonationsByTeam(0, team.id, team.name));
			}
		}
		Map<String, Map<Long, ?>> allDonations = new HashMap<String, Map<Long, ?>>();
		allDonations.put("pfp", SortUtils.sortDonationsByPfp(pfpTotals));
		allDonations.put("team", SortUtils.sortDonationsByTeam(teamTotals));
		return allDonations;
	}
	
	public static int getTotalPfpDonations(Long id) {
		return getTotalDonations(findByPfpId(id));
	}
	
	public static int getTotalPfpDonationsByUserId(Long userId) {
		List<Object> eventIds = (List<Object>) Pfp.findAllIdsByUserId(userId);
		int total = 0;
		for (Object eventId : eventIds) {
			total = total + getTotalDonations(findByPfpId((Long) eventId));
		}
		return total;
	}
	
	public static Map<Pfp, Integer> getTotalPfpTeamDonations(Long id, Long teamId) {
		final Map<Pfp, Integer> donationsTotal = new HashMap<Pfp, Integer>();
		final List<Donation> donations = findByEventAndTeamId(id, teamId);
		for (final Donation donation : donations) {
			if (donationsTotal.containsKey(donation.pfp)) {
				donationsTotal.put(donation.pfp, (donationsTotal.get(donation.pfp) + donation.amount));
			} else {
				donationsTotal.put(donation.pfp, donation.amount);
			}
			
		}
		List<Pfp> teamPfps = Pfp.findByTeamId(teamId);
		for (Pfp teamPfp : teamPfps) {
			if (!donationsTotal.containsKey(teamPfp)) {
				donationsTotal.put(teamPfp, 0);
			}
		}
		return donationsTotal;
	}
	
	public static Map<Long, Donation.DonationsByTeam> getTotalTeamDonations(Long id) {
		final List<Donation.DonationsByTeam> donations = findByEventExceptSponsorMinTeam(id);
		final Map<Long, Donation.DonationsByTeam> teamTotals = new HashMap<Long, Donation.DonationsByTeam>();
		for (final Donation.DonationsByTeam donation : donations) {

//=================start=================25.09.2015========================for cleared status=================================//

			if(donation.status.getValue().equalsIgnoreCase("Cleared")){
				if (teamTotals.containsKey(donation.id)) {

						teamTotals.get(donation.id).amount = teamTotals.get(donation.id).amount + donation.amount;
				} else {
					teamTotals.put(donation.id, donation);
				}
			}



//==================end==================25.09.2015========================for cleared status=================================//

		}
		List<Team> teams = Team.findByEventId(id);
		for (Team team : teams) {
			if (!teamTotals.containsKey(team.id)) {
				teamTotals.put(team.id, new Donation.DonationsByTeam(0, team.id, team.name));
			}
		}
		return SortUtils.sortDonationsByTeam(teamTotals);
	}
	
	public static int getTotalTeamDonations(Long id, Long teamId) {
		return getTotalDonations(findByEventAndTeamId(id, teamId));
	}
	
	/**
	 * Return a page of computer
	 * 
	 * @param page
	 *            Page to display
	 * @param pageSize
	 *            Number of pfps per page
	 * @param sortBy
	 *            Pfp property used for sorting
	 * @param order
	 *            Sort order (either or asc or desc)
	 * @param filter
	 *            Filter applied on the name column
	 */
	public static Page<Donation> page(int page, int pageSize, String sortBy, String order, String filter) {
		if (pageSize > 20) {
			pageSize = 20;
		}
		return find.where().ilike("firstName", "%" + filter + "%").orderBy(sortBy + " " + order)
						.findPagingList(pageSize).setFetchAhead(false).getPage(page);
	}
	
	public static Page<Donation> page(int page, int pageSize, String sortBy, String order, String filter, String fieldName, User localUser) {
		System.out.println("filter : "+filter);
		String queryField = "donorName";
		if (StringUtils.equals("donorName", fieldName)
				|| StringUtils.equals("transactionNumber", fieldName)
				|| StringUtils.equals("ccDigits", fieldName)
				|| StringUtils.equals("schoolId", fieldName)
				|| StringUtils.equals("email", fieldName)
				|| StringUtils.equals("invoice_number",fieldName)) {
			if (fieldName.equals("donorName")){
				queryField="ccname";
			}
			else {
				queryField = fieldName;
			}
		}
		if (StringUtils.equals("pfpName", fieldName) ) {
			queryField = "pfp.name";
		}
		 if (StringUtils.equals("paymentType", fieldName) ) {
				if(StringUtils.equalsIgnoreCase("CREDIT", filter)) {
					filter = "1";
				} else if(StringUtils.equalsIgnoreCase("CHECK", filter)) {
					filter = "2";
				} else {
					filter = "3";
				}
				queryField = fieldName;
			} 
		if (pageSize > 20) {
			pageSize = 20;
		}
		ExpressionList<Donation> query = find.where().ilike(queryField, "%" + filter + "%");
		if(localUser != null) {
			query.eq("event.userAdmin.id", localUser.id);									
		}
		System.out.println("query :: " + query + " : page : " + page + " : filter : " + filter + " : queryField : " + queryField);
		//int pageValue = 0;
		System.out.println("pageValue : " + page);
		System.out.println("final output :: "+query.select("id, amount, donorName, transactionNumber, ccDigits, paymentType, status, invoiceNumber")
				.fetch("pfp", "name").fetch("event", "name")
				.orderBy(sortBy + " " + order)
				.findPagingList(pageSize).setFetchAhead(false).getPage(page).getTotalRowCount()+ " : page :"+page  + " : sortBy : " + sortBy
				+ " : order : " + order);
		//System.out.println("query :: "+query);
		/*System.out.println("final output :: "+query.select("id, amount, donorName, transactionNumber, ccDigits, paymentType, status, invoiceNumber")
				.fetch("pfp","name").fetch("event", "name")
				.orderBy(sortBy + " " + order)
				.findPagingList(pageSize).setFetchAhead(false).getPage(page).getList());*/
		/*return query.select("id, amount, donorName, pfp.id, pfp.name, transactionNumber, ccDigits, paymentType, status, event.id, event.name, invoiceNumber")
							.fetch("pfp").fetch("event")
							.orderBy(sortBy + " " + order)
							.findPagingList(pageSize).setFetchAhead(false).getPage(page);*/
		return query.select("id, amount, donorName, transactionNumber, ccDigits, paymentType, status, invoiceNumber")
				.fetch("pfp","name").fetch("event", "name")
				.orderBy(sortBy + " " + order)
				.findPagingList(pageSize).setFetchAhead(false).getPage(page);
	}
	/***********************start***************18.01.2016***************donation search with year***********************/
	public static Page<Donation> page1(int page, int pageSize, String sortBy, String order, String filter, String fieldName,String filter1, String fieldName1, User localUser) {
		System.out.println("filter : "+filter);
		System.out.println("filter1 : "+filter1);
		System.out.println("fieldName : "+fieldName);
		System.out.println("fieldName1 : "+fieldName1);
		//filter1 = "2016";
		//fieldName1 = "year";
		String queryField = "donorName";
		String queryField1 = null;
		if (StringUtils.equals("donorName", fieldName)
				|| StringUtils.equals("transactionNumber", fieldName)
				|| StringUtils.equals("ccDigits", fieldName)
				|| StringUtils.equals("schoolId", fieldName)
				|| StringUtils.equals("email", fieldName)
				|| StringUtils.equals("invoice_number",fieldName)) {
			if (fieldName.equals("donorName")){
				/*queryField="ccname";******rimi*****21.01.2016*/
				queryField="donorName";
			}
			else {
				queryField = fieldName;
			}
		}
		if (StringUtils.equals("pfpName", fieldName) ) {
			queryField = "pfp.name";
		}
		if (StringUtils.equals("paymentType", fieldName) ) {
			if(StringUtils.equalsIgnoreCase("CREDIT", filter)) {
				filter = "1";
			} else if(StringUtils.equalsIgnoreCase("CHECK", filter)) {
				filter = "2";
			} else {
				filter = "3";
			}
			queryField = fieldName;
		}
         if(fieldName1!=null){
			 if(StringUtils.equals("year", fieldName1) ){
				 queryField1 = "dateCreated";
			 }
		 }

		if (pageSize > 20) {
			pageSize = 20;
		}
		ExpressionList<Donation> query = null;
		if(queryField1!=null && (filter1!=null && !(filter1.trim()).equals("selectYear"))){
			query = find.where().ilike(queryField, "%" + filter + "%").ilike(queryField1,"%" + filter1 + "%");
			if(localUser != null) {
				query.eq("event.userAdmin.id", localUser.id);
			}
		}else{
		 query = find.where().ilike(queryField, "%" + filter + "%");
			if(localUser != null) {
				query.eq("event.userAdmin.id", localUser.id);
			}
		}

		System.out.println("query :: " + query + " : page : " + page + " : filter : " + filter + " : queryField : " + queryField);
		//int pageValue = 0;
		System.out.println("pageValue : " + page);
		System.out.println("final output :: "+query.select("id, amount, donorName, transactionNumber, ccDigits, paymentType, status, invoiceNumber")
				.fetch("pfp", "name").fetch("event", "name")
				.orderBy(sortBy + " " + order)
				.findPagingList(pageSize).setFetchAhead(false).getPage(page).getTotalRowCount()+ " : page :"+page  + " : sortBy : " + sortBy
				+ " : order : " + order);
		//System.out.println("query :: "+query);
		/*System.out.println("final output :: "+query.select("id, amount, donorName, transactionNumber, ccDigits, paymentType, status, invoiceNumber")
				.fetch("pfp","name").fetch("event", "name")
				.orderBy(sortBy + " " + order)
				.findPagingList(pageSize).setFetchAhead(false).getPage(page).getList());*/
		/*return query.select("id, amount, donorName, pfp.id, pfp.name, transactionNumber, ccDigits, paymentType, status, event.id, event.name, invoiceNumber")
							.fetch("pfp").fetch("event")
							.orderBy(sortBy + " " + order)
							.findPagingList(pageSize).setFetchAhead(false).getPage(page);*/
		return query.select("id, amount, donorName, transactionNumber, ccDigits, paymentType, status, invoiceNumber")
				.fetch("pfp","name").fetch("event", "name")
				.orderBy(sortBy + " " + order)
				.findPagingList(pageSize).setFetchAhead(false).getPage(page);
	}
	/************************end****************18.01.2016***************donation search with year***********************/
	public enum PaymentStatus {
		@EnumValue("0")
		APPROVED(0, "Approved"),
		@EnumValue("1")
		PENDING(1, "Pending"),
		@EnumValue("2")
		CLEARED(2, "Cleared"),
		@EnumValue("3")
		REFUNDED(3, "Refunded"),
		@EnumValue("4")
		FAILED(4, "Failed"); // credit transaction failed
		
		private static final Map<String, PaymentStatus>	MAP		= new HashMap<String, PaymentStatus>();
		public static final Map<String, String>			VALUES	= new HashMap<String, String>();
		
		static {
			for (PaymentStatus pt : PaymentStatus.values()) {
				VALUES.put(pt.name(), pt.value);
				MAP.put(pt.name(), pt);
			}
		}
		
		public static PaymentStatus get(String name) {
			return MAP.get(name);
		}
		
		public final int	id;
		
		public final String	value;
		
		public int getId() {
			return id;
		}
		
		public String getValue() {
			return value;
		}
		
		PaymentStatus(int id, String value) {
			this.id = id;
			this.value = value;
		}






		//====================get value against id======start================14.09.2015==============================//




		public static String getPaymentStatusValueFromId(int id){

		//	PaymentStatus pt.value = actualPaymentStatusValue;
			String actualPaymentStatusValue = null;

			if(id == 0){
				actualPaymentStatusValue = "Approved";
			}else if(id == 1){
				actualPaymentStatusValue = "Pending";

			}else if(id == 2){
				actualPaymentStatusValue = "Cleared";

			}else if(id == 3){
				actualPaymentStatusValue = "Refunded";

			}else if(id == 4){
				actualPaymentStatusValue = "Failed";

			}

			return actualPaymentStatusValue;
		}




		//====================get value against id=======end=================14.09.2015==============================//
		// @Override
		// public String toString() {
		// return id + "";
		// }
	}
	
	public enum PaymentType {
		@EnumValue("1")
		CREDIT(1, "Credit"),
		@EnumValue("2")
		CHECK(2, "Check"),
		@EnumValue("3")
		CASH(3, "Cash");
		
		private static final Map<String, PaymentType>	MAP		= new HashMap<String, PaymentType>();
		public static final Map<String, String>			VALUES	= new HashMap<String, String>();
		
		static {
			for (PaymentType pt : PaymentType.values()) {
				VALUES.put(pt.name(), pt.value);
				MAP.put(pt.name(), pt);
			}
		}
		
		public static PaymentType get(String name) {
			return MAP.get(name);
		}
		
		public final int	id;
		
		public final String	value;
		
		public int getId() {
			return id;
		}
		
		public String getValue() {
			return value;
		}
		
		PaymentType(int id, String value) {
			this.id = id;
			this.value = value;
		}
		
		// @Override
		// public String toString() {
		// return id + "";
		// }
	}
	
	public enum DonationType {
		@EnumValue("1")
		PFP(1, "Pfp"), @EnumValue("2")
		GENERAL(2, "General Fund"), @EnumValue("3")
		SPONSOR(3, "Sponsor Fund");
		
		private static final Map<String, DonationType>	MAP		= new HashMap<String, DonationType>();
		public static final Map<String, String>			VALUES	= new HashMap<String, String>();
		
		static {
			for (DonationType pt : DonationType.values()) {
				VALUES.put(pt.name(), pt.value);
				MAP.put(pt.name(), pt);
			}
		}
		
		public static DonationType get(String name) {
			return MAP.get(name);
		}
		
		public final int	id;
		
		public final String	value;
		
		public int getId() {
			return id;
		}
		
		public String getValue() {
			return value;
		}
		
		DonationType(int id, String value) {
			this.id = id;
			this.value = value;
		}
		
		// @Override
		// public String toString() {
		// return id + "";
		// }
		
	}
	
	@Override
	public Donation bind(String key, String value) {
		Donation donation = Donation.findById(NumberUtils.createLong(value));
		return donation;
	}
	
	@Override
	public String javascriptUnbind() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String unbind(String arg0) {
		return id + "";
	}
	
}