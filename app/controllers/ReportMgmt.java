package controllers;

import au.com.bytecode.opencsv.CSVWriter;
import base.utils.DateUtils;
import base.utils.DebugUtils;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import com.fasterxml.jackson.databind.JsonNode;
import models.*;
import models.Pfp.PfpType;
import models.security.SecurityRole;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.slf4j.LoggerFactory;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * The Class ReportMgmt.
 */
public class ReportMgmt extends Controller {

	/**
	 * Donations report.
	 *
	 * @return the result
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */

	private static final org.slf4j.Logger REPORT_LOGGER = LoggerFactory.getLogger("ScholasticWebApplication");

	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
			@Group(SecurityRole.EVENT_ASSIST) })
	@SubjectPresent(content = "/login")
	public static Result adminDonationsReport(Event event) throws IOException {
		Map<String, String> requestData = Form.form().bindFromRequest().data();
		if(StringUtils.isEmpty(requestData.get("paymentType"))) {
			requestData.remove("paymentType");
		}
		if(StringUtils.isEmpty(requestData.get("status"))) {
			requestData.remove("status");
		}
		if(StringUtils.isEmpty(requestData.get("fromDate")) || !DateUtils.parseDate(requestData.get("fromDate")).isDefined()) {
			requestData.remove("fromDate");
		}
		if(StringUtils.isEmpty(requestData.get("toDate")) || !DateUtils.parseDate(requestData.get("toDate")).isDefined()) {
			requestData.remove("toDate");
		}
		List<Donation> donations = Donation.findAllByEventIdAndOptions(event.id, requestData);
		Logger.debug("Donations size {}", donations.size());
		final File file = new File("yourfile.csv");
		final BufferedWriter out = new BufferedWriter(new FileWriter(file));
		final CSVWriter writer = new CSVWriter(out, ',');
		List<String[]> data = null;
		DebugUtils.callout();
		System.out.println("Total by PFP - " + requestData.get("totalByPfp"));
		System.out.println((StringUtils.isNotEmpty(requestData.get("totalByPfp")) && Boolean.getBoolean(requestData.get("totalByPfp"))));
		System.out.println(Boolean.parseBoolean(requestData.get("totalByPfp")));
		DebugUtils.callout();
		if(StringUtils.isNotEmpty(requestData.get("totalByPfp")) && Boolean.parseBoolean(requestData.get("totalByPfp"))) {
			data = donatioTotalToStringArray(donations);
		} else {
			data = donationToStringArray(donations);
		}
		writer.writeAll(data);
		writer.close();
		response().setHeader("Content-Disposition",
				"attachment; filename=\"DonationsReport.csv\"");
		response().setContentType("text/csv");
		return ok(file).as("text/csv");
	}

	/**
	 * Donations report.
	 *
	 * @return the result
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
			@Group(SecurityRole.EVENT_ASSIST) })
	@SubjectPresent(content = "/login")
	public static Result adminVolunteersReport(Event event) throws IOException {
		Map<String, String> requestData = Form.form().bindFromRequest().data();
		if(StringUtils.isEmpty(requestData.get("fromDate")) || !DateUtils.parseDate(requestData.get("fromDate")).isDefined()) {
			requestData.remove("fromDate");
		}
		if(StringUtils.isEmpty(requestData.get("toDate")) || !DateUtils.parseDate(requestData.get("toDate")).isDefined()) {
			requestData.remove("toDate");
		}
		Volunteers volunteers = Volunteers.findAllByEventIdAndOptions(event.id, requestData);
		List<String[]> data = null;
		if(volunteers != null) {
			List<Shift> shifts = volunteers.shifts;
			Logger.debug("Shift size {}", shifts.size());
			data = shiftToStringArray(shifts);
		} else {
			data = new ArrayList<String[]>();
		}
		final File file = new File("yourfile.csv");
		final BufferedWriter out = new BufferedWriter(new FileWriter(file));
		final CSVWriter writer = new CSVWriter(out, ',');
		writer.writeAll(data);
		writer.close();
		response().setHeader("Content-Disposition",
				"attachment; filename=\"VolunteersReport.csv\"");
		response().setContentType("text/csv");
		return ok(file).as("text/csv");
	}

	/**
	 * Donations report.
	 *
	 * @return the result
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
			@Group(SecurityRole.EVENT_ASSIST) })
	@SubjectPresent(content = "/login")
	public static Result adminPfpsReport(Event event) throws IOException {
		System.out.println("within adminPfpsReport...");
		Map<String, String> requestData = Form.form().bindFromRequest().data();
		if(StringUtils.isEmpty(requestData.get("pfpType"))) {
			requestData.remove("pfpType");
		}
		if(StringUtils.isEmpty(requestData.get("fromDate")) || !DateUtils.parseDate(requestData.get("fromDate")).isDefined()) {
			requestData.remove("fromDate");
		}
		if(StringUtils.isEmpty(requestData.get("toDate")) || !DateUtils.parseDate(requestData.get("toDate")).isDefined()) {
			requestData.remove("toDate");
		}
		List<Pfp> pfps = Pfp.findAllByEventIdAndOptions(event.id, requestData);
		Logger.debug("Pfp size {}", pfps.size());
		final File file = new File("yourfile.csv");
		final BufferedWriter out = new BufferedWriter(new FileWriter(file));
		final CSVWriter writer = new CSVWriter(out, ',');
		final List<String[]> data = pfpToStringArray(pfps);
		writer.writeAll(data);
		writer.close();
		response().setHeader("Content-Disposition",
				"attachment; filename=\"PfpsReport.csv\"");
		response().setContentType("text/csv");
		return ok(file).as("text/csv");
	}

//	/**
//	 * Donations report.
//	 *
//	 * @return the result
//	 * @throws IOException
//	 *             Signals that an I/O exception has occurred.
//	 */
//	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
//		@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
//		@Group(SecurityRole.EVENT_ASSIST) })
//	@SubjectPresent(content = "/login")
//	public static Result allDonationsReport(Event event) throws IOException {
//		String paymentType = request().getQueryString("paymentType");
//		// DynamicForm reportForm = form().bindFromRequest();
//		// String reportType = reportForm.get("reportType");
//		// String id = reportForm.get("id");
//		List<Donation> donations = Donation.findAllByEventIdAndPaymentType(event.id, paymentType);
//		Logger.debug("Donations size {}", donations.size());
//		final File file = new File("yourfile.csv");
//		final BufferedWriter out = new BufferedWriter(new FileWriter(file));
//		final CSVWriter writer = new CSVWriter(out, ',');
//		final List<String[]> data = toStringArray(donations);
//		writer.writeAll(data);
//		writer.close();
//		response().setHeader("Content-Disposition",
//				"attachment; filename=\"allDonationsReport.csv\"");
//		response().setContentType("text/csv");
//		return ok(file).as("text/csv");
//	}
//
//	/**
//	 * Donations report.
//	 *
//	 * @return the result
//	 * @throws IOException
//	 *             Signals that an I/O exception has occurred.
//	 */
//	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
//		@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
//		@Group(SecurityRole.EVENT_ASSIST) })
//	@SubjectPresent(content = "/login")
//	public static Result clearedDonationsReport(Event event) throws IOException {
//		String paymentType = request().getQueryString("paymentType");
//		// DynamicForm reportForm = form().bindFromRequest();
//		// String reportType = reportForm.get("reportType");
//		// String id = reportForm.get("id");
//		List<Donation> donations = Donation.findAllByEventIdAndClearedAndPaymentType(event.id, paymentType);
//		Logger.debug("Donations size {}", donations.size());
//		final File file = new File("yourfile.csv");
//		final BufferedWriter out = new BufferedWriter(new FileWriter(file));
//		final CSVWriter writer = new CSVWriter(out, ',');
//		final List<String[]> data = toStringArray(donations);
//		writer.writeAll(data);
//		writer.close();
//		response().setHeader("Content-Disposition",
//				"attachment; filename=\"clearedDonationsReport.csv\"");
//		response().setContentType("text/csv");
//		return ok(file).as("text/csv");
//	}
//
//	/**
//	 * Donations report.
//	 *
//	 * @return the result
//	 * @throws IOException
//	 *             Signals that an I/O exception has occurred.
//	 */
//	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
//		@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
//		@Group(SecurityRole.EVENT_ASSIST) })
//	@SubjectPresent(content = "/login")
//	public static Result lastWeekReport(Event event) throws IOException {
//		String paymentType = request().getQueryString("paymentType");
//		// DynamicForm reportForm = form().bindFromRequest();
//		// String reportType = reportForm.get("reportType");
//		// String id = reportForm.get("id");
//		Date to = new Date();
//		Calendar cal = Calendar.getInstance();
//		cal.add(Calendar.DATE, -7);
//		Date from = cal.getTime();
//		List<Donation> donations = Donation.findAllByEventIdAndDateRangeAndPaymentType(event.id, from, to, paymentType);
//		Logger.debug("Donations size {}", donations.size());
//		final File file = new File("yourfile.csv");
//		final BufferedWriter out = new BufferedWriter(new FileWriter(file));
//		final CSVWriter writer = new CSVWriter(out, ',');
//		final List<String[]> data = toStringArray(donations);
//		writer.writeAll(data);
//		writer.close();
//		response().setHeader("Content-Disposition",
//				"attachment; filename=\"lastWeeksDonationsReport.csv\"");
//		response().setContentType("text/csv");
//		return ok(file).as("text/csv");
//	}

	/**
	 * To string array.
	 *
	 * @param donations
	 *            the donations
	 * @return the list
	 */
	private static List<String[]> donationToStringArray(List<Donation> donations) {
		final List<String[]> records = new ArrayList<String[]>();
		// add header record
		records.add(new String[] { "Event Name", " PFP Name", "Team Name", "Donor First Name", "Donor Last Name", "Donation Zip", "Donor Email", "Donor Phone", "Donation Type", "Date Created", "Date Paid", "Payment Type", "Status",
				"Amount", "Transaction Number" });
		for(Donation donation: donations) {
			String datePaid = "";
			String dateCreated = "";
			String teamName = "";

			if(donation.datePaid != null) {
				datePaid = new SimpleDateFormat("MM/dd/yyyy").format(donation.datePaid);
			}
			if(donation.dateCreated != null) {
				dateCreated = new SimpleDateFormat("MM/dd/yyyy").format(donation.dateCreated);
			}
			if(donation.pfp.pfpType == PfpType.GENERAL) {
				teamName = PfpType.GENERAL.value;
			} else if(donation.pfp.pfpType == PfpType.SPONSOR) {
				teamName = PfpType.SPONSOR.value;
			} else {
				if(donation.pfp.team == null) {
					teamName = "No Team Selected";
				} else {
					teamName = donation.pfp.team.name;
				}
			}
			if (donation.pfp == null) {
				records.add(new String[] { donation.event.name, "none",
						teamName, donation.firstName, donation.lastName, donation.zipCode + "", donation.email, donation.phone, donation.donationType.value, dateCreated, datePaid, donation.paymentType.value, donation.status.value, donation.amount + "", donation.transactionNumber });
			} else {
				records.add(new String[] { donation.event.name, donation.pfp.name,
						teamName, donation.firstName, donation.lastName, donation.zipCode + "", donation.email, donation.phone, donation.donationType.value, dateCreated, datePaid, donation.paymentType.value, donation.status.value, donation.amount + "", donation.transactionNumber });
			}
		}
		return records;
	}

	/**
	 * To string array.
	 *
	 * @param donations
	 *            the donations
	 * @return the list
	 */
	private static List<String[]> donatioTotalToStringArray(List<Donation> donations) {
		final List<String[]> records = new ArrayList<String[]>();
		// add header record
		records.add(new String[] { "Event Name", " PFP Name", "Team Name", "Donor First Name", "Donor Last Name", "Donation Zip", "Donor Email", "Donor Phone", "Donation Type", "Date Created", "Date Paid", "Payment Type", "Status",
				"Transaction Number", "Donations Total", "Donations Amount" });
		Map<Long, Donation> pfpDonations = new HashMap<Long, Donation>();
		for(Donation donation: donations) {
			if(pfpDonations.containsKey(donation.pfp.id)) {
				Donation pfpDonation = pfpDonations.get(donation.pfp.id);
				pfpDonation.donationTotal = pfpDonation.donationTotal + donation.amount;
				pfpDonation.donationCount = pfpDonation.donationCount + 1;
				pfpDonations.put(donation.pfp.id, pfpDonation);
			} else {
				Donation pfpDonation = donation;
				pfpDonation.donationTotal = pfpDonation.amount;
				pfpDonation.donationCount = 1;
				pfpDonations.put(donation.pfp.id, pfpDonation);
			}
		}

		for(Long key: pfpDonations.keySet()) {
			String datePaid = "";
			String dateCreated = "";
			String teamName = "";

			Donation donation = pfpDonations.get(key);
			if(donation.datePaid != null) {
				datePaid = new SimpleDateFormat("MM/dd/yyyy").format(donation.datePaid);
			}
			if(donation.dateCreated != null) {
				dateCreated = new SimpleDateFormat("MM/dd/yyyy").format(donation.dateCreated);
			}
			if(donation.pfp.pfpType == PfpType.GENERAL) {
				teamName = PfpType.GENERAL.value;
			} else if(donation.pfp.pfpType == PfpType.SPONSOR) {
				teamName = PfpType.SPONSOR.value;
			} else {
				if(donation.pfp.team == null) {
					teamName = "No Team Selected";
				} else {
					teamName = donation.pfp.team.name;
				}
			}
			if (donation.pfp == null) {
				records.add(new String[] { donation.event.name, "none",
						teamName, donation.firstName, donation.lastName, donation.zipCode + "", donation.email, donation.phone, donation.donationType.value, dateCreated, datePaid, donation.paymentType.value, donation.status.value, donation.transactionNumber, donation.donationTotal + "", donation.donationCount + "" });
			} else {
				records.add(new String[] { donation.event.name, donation.pfp.name,
						teamName, donation.firstName, donation.lastName, donation.zipCode + "", donation.email, donation.phone, donation.donationType.value, dateCreated, datePaid, donation.paymentType.value, donation.status.value, donation.transactionNumber, donation.donationTotal + "", donation.donationCount + "" });
			}
		}
		return records;
	}

	/**
	 * To string array.
	 *
	 * @param donations
	 *            the donations
	 * @return the list
	 */
	private static List<String[]> shiftToStringArray(List<Shift> shifts) {
		final List<String[]> records = new ArrayList<String[]>();
		// add header record
		List<Shift> sortedShifts = Shift.getSortedShifts(shifts);
		records.add(new String[] { "Shift Name", "Shift Date", "Shift Start Time", "Shift End Time", "Volunteer Count", "Name", " Mobile", "Phone", "Email", "Note" });
		for(Shift shift: sortedShifts) {
			String shiftDate = "";
			String shiftStartTime = "";
			String shiftEndTime = "";

			if(shift.date != null) {
				shiftDate = new SimpleDateFormat("MM/dd/yyyy").format(shift.date);
			}
			if(shift.startTime != null) {
				shiftStartTime = new SimpleDateFormat("h:mm a").format(shift.date);
			}
			if(shift.endTime != null) {
				shiftEndTime = new SimpleDateFormat("h:mm a").format(shift.date);
			}
			for(Volunteer volunteer: shift.volunteerList) {
				records.add(new String[] { shiftDate, shiftStartTime, shiftEndTime, shift.name, shift.volunteerCount + "", volunteer.firstName + " " + volunteer.lastName, volunteer.mobile,
						volunteer.phone, volunteer.email, volunteer.note });
			}
			for(int count = shift.volunteerList.size() + 1; count <= shift.volunteerCount; count++) {
				records.add(new String[] { shiftDate, shiftStartTime, shiftEndTime, shift.name, shift.volunteerCount + "", "Available", "",
						"", "", "" });
			}
		}
		return records;
	}

	/**
	 * To string array.
	 *
	 * @param donations
	 *            the donations
	 * @return the list
	 */
	private static List<String[]> pfpToStringArray(List<Pfp> pfps) {
		final List<String[]> records = new ArrayList<String[]>();
		// add header record
		records.add(new String[] { "Name", "Date Created", "Goal", "Emergency Contact", "Emergency Contact Phone", "Pfp Type", "Team Name", "Event Name", "Admin Name", "Admin Email", "Admin Phone",
				"Amount", "Transaction Number" });
		for(Pfp pfp: pfps) {
			String dateCreated = "";
			String teamName = "";

			if(pfp.dateCreated != null) {
				dateCreated = new SimpleDateFormat("MM/dd/yyyy").format(pfp.dateCreated);
			}
			if(pfp.pfpType == PfpType.GENERAL) {
				teamName = PfpType.GENERAL.value;
			} else if(pfp.pfpType == PfpType.SPONSOR) {
				teamName = PfpType.SPONSOR.value;
			} else {
				if(pfp.team == null) {
					teamName = "No Team Selected";
				} else {
					teamName = pfp.team.name;
				}
			}
			records.add(new String[] { pfp.name, dateCreated + "", pfp.goal + "", pfp.emergencyContact, pfp.emergencyContactPhone, pfp.pfpType.value,
					teamName, pfp.event.name, pfp.userAdmin.firstName + " " + pfp.userAdmin.lastName, pfp.userAdmin.email, pfp.userAdmin.phone });
		}
		return records;
	}


	/******start*********19.01.2016************************************/

/*
	private static List<String[]> payoutToStringArray1(List<String> lists) {
		String rowHeadervalue = null;
		System.out.println("payoutToStringArray");
		final List<String[]> records = new ArrayList<String[]>();
		// add header record
		records.add(new String[] { "Donations", "#" ,"$","#%","$%"});
		for(String totalCreditAmount: lists) {
			String dateCreated = "";
			String teamName = "";
			System.out.println("totalCreditAmount :: "+totalCreditAmount);
		*//*	if(pfp.dateCreated != null) {
				dateCreated = new SimpleDateFormat("MM/dd/yyyy").format(pfp.dateCreated);
			}
			if(pfp.pfpType == PfpType.GENERAL) {
				teamName = PfpType.GENERAL.value;
			} else if(pfp.pfpType == PfpType.SPONSOR) {
				teamName = PfpType.SPONSOR.value;
			} else {
				if(pfp.team == null) {
					teamName = "No Team Selected";
				} else {
					teamName = pfp.team.name;
				}
			}*//*
			records.add(new String[] { rowHeadervalue,totalCreditAmount,"","","" });
		}
		return records;
	}*/

	private static List<String[]> reconcileToStringArrayForMonth(List<Donation> donations, Event eventy){
		final List<String[]> records = new ArrayList<String[]>();


		records.add(new String[] {" "," "," ", " ", "Reconciliation  Report " ," "," "," "," "," "," "});
		records.add(new String[] {" "," "," ", " ", " " ," "," "," "," "," "," "});
		records.add(new String[] {" "," "," ", " ", " " ," "," "," "," "," "," "});
		records.add(new String[] {" ", " ","Event Name ",eventy.name, " " ," "," "," "," "," "," "});
		records.add(new String[] {" "," "," ", " ", " " ," "," "," "," "," "," "});
		records.add(new String[] {"Donor Name","Pfp Name","Donor Email","Amount","Donation Type", " Transaction Reference from SC", " Invoice No For Both SC and authorize.net " ,"Transaction id from authorize.net ","Status"," Date Created"," "});
		records.add(new String[] {" ", " ", " " ," "," "," "," "});
		if(donations!=null && donations.size()>0){
			for(Donation donation:donations){
				if(donation!=null){
					Transaction transaction = Transaction.findByDonationTranId(donation.transactionNumber);
					if(transaction!=null && (donation.transactionNumber!=null && !donation.transactionNumber.trim().equals(""))){
						records.add(new String[] {donation.donorName,donation.pfp.name,donation.email,String.valueOf(donation.amount), donation.paymentType.getValue(), donation.transactionNumber, donation.invoiceNumber ,transaction.transid,donation.status.getValue(),String.valueOf(donation.dateCreated)," "});
					}else{
						records.add(new String[] {donation.donorName,donation.pfp.name,donation.email,String.valueOf(donation.amount), donation.paymentType.getValue(), donation.transactionNumber, donation.invoiceNumber ," ",donation.status.getValue(),String.valueOf(donation.dateCreated)," "});
					}



				}
			}
		}
		return records;
	}


	/*************start*********************28.01.2016******************************************************/

	private static List<String[]> reconcileToStringArrayForMonth1(List<Donation> donations){
		final List<String[]> records = new ArrayList<String[]>();


		records.add(new String[] {""," "," "," ", " ", "Reconciliation  Report " ," "," "," "," "," "," "});
		records.add(new String[] {""," "," "," ", " ", " " ," "," "," "," "," "," "});
		records.add(new String[] {""," "," "," ", " ", " " ," "," "," "," "," "," "});
		/*records.add(new String[] {" ", " ","Event Name ",eventy.name, " " ," "," "," "," "," "," "});
		records.add(new String[] {" "," "," ", " ", " " ," "," "," "," "," "," "});*/
		records.add(new String[] {"Event Name","Donor Name","Pfp Name","Donor Email","Amount","Donation Type", " Transaction Reference from SC", " Invoice No For Both SC and authorize.net " ,"Transaction id from authorize.net ","Status"," Date Created"," "});
		records.add(new String[] {""," ", " ", " " ," "," "," "," "});
		if(donations!=null && donations.size()>0){
			for(Donation donation:donations){
				if(donation!=null){
					Transaction transaction = Transaction.findByDonationTranId(donation.transactionNumber);
					if(transaction!=null && (donation.transactionNumber!=null && !donation.transactionNumber.trim().equals(""))) {
						if (donation.pfp != null) {
							records.add(new String[]{donation.event.name, donation.donorName, donation.pfp.name, donation.email, String.valueOf(donation.amount), donation.paymentType.getValue(), donation.transactionNumber, donation.invoiceNumber, transaction.transid, donation.status.getValue(), String.valueOf(donation.dateCreated), " "});

						}else{
							records.add(new String[]{donation.event.name, donation.donorName, "", donation.email, String.valueOf(donation.amount), donation.paymentType.getValue(), donation.transactionNumber, donation.invoiceNumber, transaction.transid, donation.status.getValue(), String.valueOf(donation.dateCreated), " "});
						}
						/*records.add(new String[]{donation.event.name, donation.donorName, donation.pfp.name, donation.email, String.valueOf(donation.amount), donation.paymentType.getValue(), donation.transactionNumber, donation.invoiceNumber, transaction.transid, donation.status.getValue(), String.valueOf(donation.dateCreated), " "});*/
					}else{
						System.out.println("donation.id :: "+donation.id);
						System.out.println("donation.event.name ::: "+donation.event.name);
						System.out.println("donation.donorName :: "+donation.donorName);
						System.out.println("donation.pfp :: "+donation.pfp);
						//System.out.println("donation.pfp.id :: "+donation.pfp.id);
						System.out.println("donation.email :: "+donation.email);
						System.out.println("donation.paymentType :: "+donation.paymentType);
						System.out.println("donation.transactionNumber :: "+donation.transactionNumber);
						System.out.println("donation.invoiceNumber v:: "+donation.invoiceNumber);
						System.out.printf("donation.status :: " + donation.status);
						/*if()*/
						if(donation.pfp!=null){
							records.add(new String[]{donation.event.name, donation.donorName, donation.pfp.name, donation.email, String.valueOf(donation.amount), donation.paymentType.getValue(), donation.transactionNumber, donation.invoiceNumber, " ", donation.status.getValue(), String.valueOf(donation.dateCreated), " "});
						}else{
							records.add(new String[]{donation.event.name, donation.donorName,"" , donation.email, String.valueOf(donation.amount), donation.paymentType.getValue(), donation.transactionNumber, donation.invoiceNumber, " ", donation.status.getValue(), String.valueOf(donation.dateCreated), " "});
						}
						/*records.add(new String[]{donation.event.name, donation.donorName, donation.pfp.name, donation.email, String.valueOf(donation.amount), donation.paymentType.getValue(), donation.transactionNumber, donation.invoiceNumber, " ", donation.status.getValue(), String.valueOf(donation.dateCreated), " "});*/
					}



				}
			}
		}
		return records;
	}

	/*************end***********************28.01.2016******************************************************/
	private static List<String[]> payoutToStringArray1(List<Donation> donations, Event eventy) {
		String rowHeadervalue = null;
		double serviceFee = eventy.serviceFee;
		System.out.println("serviceFee payoutToStringArray : "+serviceFee);

		if(serviceFee == 0){
			serviceFee = 10.0;
		}
		System.out.println("serviceFee payoutToStringArray111 : "+serviceFee);
		final List<String[]> records = new ArrayList<String[]>();

		records.add(new String[] { " ", " " ," Income Report",""," "," "});
		records.add(new String[] { " ", " " ," ",""," "," "});
		records.add(new String[] { " ", " " ," ",""," "," "});
		records.add(new String[] { " ", " " ," ",""," "," "});
		// add header record
		records.add(new String[] { "Donations", "#" ,"$","#%","$%","$%Overall"});
		/*for(String totalCreditAmount: lists) {
			String dateCreated = "";
			String teamName = "";
			System.out.println("totalCreditAmount :: "+totalCreditAmount);
		*//*	if(pfp.dateCreated != null) {
				dateCreated = new SimpleDateFormat("MM/dd/yyyy").format(pfp.dateCreated);
			}
			if(pfp.pfpType == PfpType.GENERAL) {
				teamName = PfpType.GENERAL.value;
			} else if(pfp.pfpType == PfpType.SPONSOR) {
				teamName = PfpType.SPONSOR.value;
			} else {
				if(pfp.team == null) {
					teamName = "No Team Selected";
				} else {
					teamName = pfp.team.name;
				}
			}*//*
			records.add(new String[] { rowHeadervalue,totalCreditAmount,"","","" });
		}*/

        double totalCreditAmountNo = 0;
		String totalCreditAmount = null;
		String totalCreditAcc = null;
		int totalCreditAccNo = 0;
		double totalCheckAmountNo = 0;
		String totalCheckAmount = null;
		String totalCheckAcc = null;
		int totalCheckAccNo = 0;
		for(Donation donation:donations){
			if(donation!=null && donation.donationType!= Donation.DonationType.SPONSOR){
				if(donation.paymentType== Donation.PaymentType.CREDIT){
					//rowHeadervalue = "Paid By Credit Card";
					totalCreditAmountNo = totalCreditAmountNo+donation.amount;
					totalCreditAmount = String.valueOf(totalCreditAmountNo);
					totalCreditAccNo = totalCreditAccNo+1;
					totalCreditAcc = String.valueOf(totalCreditAccNo);

				}else if(donation.paymentType== Donation.PaymentType.CHECK){
					//rowHeadervalue = "Paid By Credit Card";
					totalCheckAmountNo = totalCheckAmountNo+donation.amount;
					totalCheckAmount = String.valueOf(totalCheckAmountNo);
					totalCheckAccNo = totalCheckAccNo+1;
					totalCheckAcc = String.valueOf(totalCheckAccNo);

				}
			}
		}
       int totalAccNo = totalCreditAccNo+totalCheckAccNo;
		String totalAcc = String.valueOf(totalAccNo);
		double totalAmountNo = totalCreditAmountNo+totalCheckAmountNo;
		String totalAmount = String.valueOf(totalAmountNo);
		double creditAccNoPercent = 0.0;
		double checkAccNoPercent  = 0.0;
		double creditAmountNoPercent = 0.0;
		double checkAmountNoPercent  = 0.0;
		if(totalAccNo>0){
			creditAccNoPercent = totalCreditAccNo/totalAccNo*100;
			checkAccNoPercent = totalCheckAccNo/totalAccNo*100;
		}
       if(totalAmountNo>0){
		   creditAmountNoPercent =  totalCreditAmountNo/totalAmountNo*100;
		   checkAmountNoPercent =  totalCheckAmountNo/totalAmountNo*100;
	   }


		records.add(new String[]{"Paid By Credit Card", String.valueOf(totalCreditAccNo), "$"+String.valueOf(totalCreditAmountNo),String.valueOf(creditAccNoPercent)+"%",String.valueOf(creditAmountNoPercent)+"%"," " });
		records.add(new String[] { "Paid By Check",String.valueOf(totalCheckAccNo),"$"+String.valueOf(totalCheckAmountNo),String.valueOf(checkAccNoPercent)+"%",String.valueOf(checkAmountNoPercent)+"%" ," "});
		records.add(new String[] { "Total donations",totalAcc,"$"+String.valueOf(totalAmountNo),"100%","100%" ," "});

		records.add(new String[] { " ", " " ," ",""," "," "});
		records.add(new String[] { " ", " " ," ",""," "," "});
		records.add(new String[] { "Sponsorships", " " ," "," "," "," "});




		double totalCreditAmountNo1 = 0;
		/*String totalCreditAmount = null;
		String totalCreditAcc = null;*/
		int totalCreditAccNo1 = 0;
		double totalCheckAmountNo1 = 0;
		/*String totalCheckAmount = null;
		String totalCheckAcc = null;*/
		int totalCheckAccNo1 = 0;


		for(Donation donation:donations){
			if(donation!=null && donation.donationType == Donation.DonationType.SPONSOR){
				if(donation.paymentType== Donation.PaymentType.CREDIT){
					//rowHeadervalue = "Paid By Credit Card";
					totalCreditAmountNo1 = totalCreditAmountNo+donation.amount;
					totalCreditAmount = String.valueOf(totalCreditAmountNo);
					totalCreditAccNo1 = totalCreditAccNo+1;
					totalCreditAcc = String.valueOf(totalCreditAccNo);

				}else if(donation.paymentType== Donation.PaymentType.CHECK){
					//rowHeadervalue = "Paid By Credit Card";
					totalCheckAmountNo1 = totalCheckAmountNo+donation.amount;
					totalCheckAmount = String.valueOf(totalCheckAmountNo);
					totalCheckAccNo1 = totalCheckAccNo+1;
					totalCheckAcc = String.valueOf(totalCheckAccNo);

				}
			}
		}


		/*records.add(new String[]{"Paid By Credit Card", "0", "$"+"0","0"+"%","0"+"%"," " });
		records.add(new String[] { "Paid By Check","0", "$"+"0","0"+"%","0"+"%"," " });
		records.add(new String[]{"Total sponsorships", "0", "$" + "0", "0" + "%", "0" + "%", " "});*/

		int totalAccNo1 = totalCreditAccNo1+totalCheckAccNo1;
		String totalAcc1 = String.valueOf(totalAccNo);
		double totalAmountNo1 = totalCreditAmountNo1+totalCheckAmountNo1;
		totalAmount = String.valueOf(totalAmountNo1);
		double creditAccNoPercent1 = 0.0;
		double checkAccNoPercent1 = 0.0;
		double creditAmountNoPercent1 = 0.0;
		double checkAmountNoPercent1 = 0.0;
		if(totalAccNo1>0){
			creditAccNoPercent1 = totalCreditAccNo1/totalAccNo1*100;
			checkAccNoPercent1 = totalCheckAccNo1/totalAccNo1*100;

		}

  if(totalAmountNo1>0){
	  creditAmountNoPercent1 =  totalCreditAmountNo1/totalAmountNo1*100;
	  checkAmountNoPercent1 =  totalCheckAmountNo1/totalAmountNo1*100;
  }



		records.add(new String[]{"Paid By Credit Card", String.valueOf(totalCreditAccNo1), "$"+String.valueOf(totalCreditAmountNo1),String.valueOf(creditAccNoPercent1)+"%",String.valueOf(creditAmountNoPercent1)+"%"," " });
		records.add(new String[] { "Paid By Check",String.valueOf(totalCheckAccNo1),"$"+String.valueOf(totalCheckAmountNo1),String.valueOf(checkAccNoPercent1)+"%",String.valueOf(checkAmountNoPercent1)+"%" ," "});
		records.add(new String[] { "Total sponsorships",totalAcc1,"$"+String.valueOf(totalAmountNo1),"100%","100%" ," "});


		records.add(new String[] { " ", " " ," ",""," "," "});
		records.add(new String[] { " ", " " ," ",""," "," "});
		double grossAmountIncomeNo = totalAmountNo+totalAmountNo1;
		records.add(new String[] { "Gross Income","","$"+String.valueOf(grossAmountIncomeNo)," "," " ,"100.0%"});


		records.add(new String[] { " ", " " ," ",""," "," "});
		//double serviceFee = 5;
		double netServiceFeeNo = grossAmountIncomeNo*serviceFee/100;
		records.add(new String[] { "Scholastic Challenge Fee","","($"+String.valueOf(netServiceFeeNo)+")"," "," " ,String.valueOf(serviceFee)+"%"});

		records.add(new String[]{" ", " ", " ", "", " ", " "});
		double netAmountIncomeNo = grossAmountIncomeNo - netServiceFeeNo;
		double netAmountIncomePercent = 100-serviceFee;
		records.add(new String[]{"Net Income", "", "$" + String.valueOf(netAmountIncomeNo), " ", " ", String.valueOf(netAmountIncomePercent) + "%"});

		records.add(new String[]{" ", " ", " ", "", " ", " "});
		/*records.add(new String[]{" ", " ", " ", "", " ", " "});*/

			/*	long daysBetweenStartAndEndFundraising = eventy.fundraisingEnd.getTime()-eventy.fundraisingStart.getTime();
		System.out.println("start day:: "+eventy.fundraisingStart);
		System.out.println("end day:: "+eventy.fundraisingEnd);
		System.out.println ("Days: " + TimeUnit.DAYS.convert(daysBetweenStartAndEndFundraising, TimeUnit.MILLISECONDS));
		int diffDays = (int)TimeUnit.DAYS.convert(daysBetweenStartAndEndFundraising, TimeUnit.MILLISECONDS);
		Calendar localCalendar = Calendar.getInstance(TimeZone.getDefault());
		int currentDayOfMonth = localCalendar.get(Calendar.DAY_OF_MONTH);*/

				Date currDate = new Date();
		long daysBetweenStartAndCurrDate = currDate.getTime()-eventy.fundraisingStart.getTime();
		int diffDays1 = (int)TimeUnit.DAYS.convert(daysBetweenStartAndCurrDate, TimeUnit.MILLISECONDS);

		/*if(diffDays>90 && diffDays<120){*/


		if(diffDays1>30 && diffDays1<=60){
			Date monthAfter1 = new Date(eventy.fundraisingStart.getTime() + TimeUnit.DAYS.toMillis( 30 ));
			Date monthAfter2 = new Date(monthAfter1.getTime() + TimeUnit.DAYS.toMillis( 30 ));
			Date monthAfter3 = new Date(monthAfter2.getTime() + TimeUnit.DAYS.toMillis( 30 ));
			System.out.println("dayAfter :: " + monthAfter1);
			double firstPayout = netAmountIncomeNo*75/100;
			double amountRemainingOnEventFor1st=netAmountIncomeNo*25/100;
			double amountSpentOnEvent = netAmountIncomePercent*75.0/100;

			records.add(new String[]{"First Payout", " ", "($" + firstPayout + ")", "100%", "100%", String.valueOf(amountSpentOnEvent)+"%"});
			/*records.add(new String[]{"Second Payout", totalAcc, "$" + totalAmount, "100%", "100%", " 41.5%"});
			records.add(new String[]{"Final Payout", totalAcc, "$" + totalAmount, "100%", "100%", " "});*/
		/*}*/
			double remainingAmount = netAmountIncomeNo-firstPayout;
			double remainingAmountPercent = netAmountIncomePercent - amountSpentOnEvent;
			records.add(new String[]{"Balance Due", " ", "($" + remainingAmount + ")", "100%", "100%", remainingAmountPercent+"%"});
		}
		if(diffDays1>60 && diffDays1<=90){


			Date monthAfter1 = new Date(eventy.fundraisingStart.getTime() + TimeUnit.DAYS.toMillis( 30 ));
			Date monthAfter2 = new Date(monthAfter1.getTime() + TimeUnit.DAYS.toMillis( 30 ));
			Date monthAfter3 = new Date(monthAfter2.getTime() + TimeUnit.DAYS.toMillis( 30 ));
			System.out.println("dayAfter :: " + monthAfter1);
			double SecondPayout = netAmountIncomeNo*75/100;
			double amountRemainingOnEventFor2nd=netAmountIncomeNo*25/100;
			double amountSpentOnEvent = netAmountIncomePercent*75.0/100;

		/*	records.add(new String[]{"First Payout", " ", "($" + firstPayout + ")", "100%", "100%", String.valueOf(amountSpentOnEvent)+"%"});*/
			records.add(new String[]{"First Payout", " ", "($" + SecondPayout + ")", "100%", "100%", String.valueOf(amountSpentOnEvent)+"%"});
			/*records.add(new String[]{"Second Payout", totalAcc, "$" + totalAmount, "100%", "100%", " 41.5%"});
			records.add(new String[]{"Final Payout", totalAcc, "$" + totalAmount, "100%", "100%", " "});*/
		/*}*/
			double remainingAmount = netAmountIncomeNo-SecondPayout;
			double remainingAmountPercent = netAmountIncomePercent - amountSpentOnEvent;
			records.add(new String[]{"Balance Due", " ", "($" + remainingAmount + ")", "100%", "100%", remainingAmountPercent+"%"});



		}else{

		}


		return records;
	}


	private static List<String[]> payoutToStringArray(List<Donation> donations, Event eventy) {
		 List<String[]> records = new ArrayList<String[]>();
	/*	String rowHeadervalue = null;
		double serviceFee = eventy.serviceFee;
		System.out.println("serviceFee payoutToStringArray : "+serviceFee);

		if(serviceFee == 0){
			serviceFee = 10.0;
		}
		System.out.println("serviceFee payoutToStringArray111 : "+serviceFee);
		final List<String[]> records = new ArrayList<String[]>();

		records.add(new String[] { " ", " " ," Income Report",""," "," "});
		records.add(new String[] { " ", " " ," ",""," "," "});
		records.add(new String[] { " ", " " ," ",""," "," "});
		records.add(new String[] { " ", " " ," ",""," "," "});
		// add header record
		records.add(new String[] { "Donations", "#" ,"$","#%","$%","$%Overall"});
		*//*for(String totalCreditAmount: lists) {
			String dateCreated = "";
			String teamName = "";
			System.out.println("totalCreditAmount :: "+totalCreditAmount);
		*//**//*	if(pfp.dateCreated != null) {
				dateCreated = new SimpleDateFormat("MM/dd/yyyy").format(pfp.dateCreated);
			}
			if(pfp.pfpType == PfpType.GENERAL) {
				teamName = PfpType.GENERAL.value;
			} else if(pfp.pfpType == PfpType.SPONSOR) {
				teamName = PfpType.SPONSOR.value;
			} else {
				if(pfp.team == null) {
					teamName = "No Team Selected";
				} else {
					teamName = pfp.team.name;
				}
			}*//**//*
			records.add(new String[] { rowHeadervalue,totalCreditAmount,"","","" });
		}*//*

		double totalCreditAmountNo = 0;
		String totalCreditAmount = null;
		String totalCreditAcc = null;
		int totalCreditAccNo = 0;
		double totalCheckAmountNo = 0;
		String totalCheckAmount = null;
		String totalCheckAcc = null;
		int totalCheckAccNo = 0;
		for(Donation donation:donations){
			if(donation!=null){
				if(donation.paymentType== Donation.PaymentType.CREDIT){
					//rowHeadervalue = "Paid By Credit Card";
					totalCreditAmountNo = totalCreditAmountNo+donation.amount;
					totalCreditAmount = String.valueOf(totalCreditAmountNo);
					totalCreditAccNo = totalCreditAccNo+1;
					totalCreditAcc = String.valueOf(totalCreditAccNo);

				}else if(donation.paymentType== Donation.PaymentType.CHECK){
					//rowHeadervalue = "Paid By Credit Card";
					totalCheckAmountNo = totalCheckAmountNo+donation.amount;
					totalCheckAmount = String.valueOf(totalCheckAmountNo);
					totalCheckAccNo = totalCheckAccNo+1;
					totalCheckAcc = String.valueOf(totalCheckAccNo);

				}
			}
		}
		int totalAccNo = totalCreditAccNo+totalCheckAccNo;
		String totalAcc = String.valueOf(totalAccNo);
		double totalAmountNo = totalCreditAmountNo+totalCheckAmountNo;
		String totalAmount = String.valueOf(totalAmountNo);
		double creditAccNoPercent = totalCreditAccNo/totalAccNo*100;
		double creditAmountNoPercent =  totalCreditAmountNo/totalAmountNo*100;

		double checkAccNoPercent = totalCheckAccNo/totalAccNo*100;
		double checkAmountNoPercent =  totalCheckAmountNo/totalAmountNo*100;

		records.add(new String[]{"Paid By Credit Card", String.valueOf(totalCreditAccNo), "$"+String.valueOf(totalCreditAmountNo),String.valueOf(creditAccNoPercent)+"%",String.valueOf(creditAmountNoPercent)+"%"," " });
		records.add(new String[] { "Paid By Check",String.valueOf(totalCheckAccNo),"$"+String.valueOf(totalCheckAmountNo),String.valueOf(checkAccNoPercent)+"%",String.valueOf(checkAmountNoPercent)+"%" ," "});
		records.add(new String[] { "Total donations",totalAcc,"$"+totalAmount,"100%","100%" ," "});

		records.add(new String[] { " ", " " ," ",""," "," "});
		records.add(new String[] { " ", " " ," ",""," "," "});
		records.add(new String[] { "Sponsorships", " " ," "," "," "," "});
		records.add(new String[]{"Paid By Credit Card", "0", "$"+"0","0"+"%","0"+"%"," " });
		records.add(new String[] { "Paid By Check","0", "$"+"0","0"+"%","0"+"%"," " });
		records.add(new String[]{"Total sponsorships", "0", "$" + "0", "0" + "%", "0" + "%", " "});

		records.add(new String[] { " ", " " ," ",""," "," "});
		records.add(new String[] { " ", " " ," ",""," "," "});
		double grossAmountIncomeNo = totalAmountNo;
		records.add(new String[] { "Gross Income","","$"+String.valueOf(grossAmountIncomeNo)," "," " ,"100.0%"});


		records.add(new String[] { " ", " " ," ",""," "," "});
		//double serviceFee = 5;
		double netServiceFeeNo = grossAmountIncomeNo*serviceFee/100;
		records.add(new String[] { "Scholastic Challenge Fee","","($"+String.valueOf(netServiceFeeNo)+")"," "," " ,String.valueOf(serviceFee)+"%"});

		records.add(new String[]{" ", " ", " ", "", " ", " "});
		double netAmountIncomeNo = grossAmountIncomeNo - netServiceFeeNo;
		double netAmountIncomePercent = 100-serviceFee;
		records.add(new String[]{"Net Income", "", "$" + String.valueOf(netAmountIncomeNo), " ", " ", String.valueOf(netAmountIncomePercent) + "%"});

		records.add(new String[]{" ", " ", " ", "", " ", " "});
		records.add(new String[]{" ", " ", " ", "", " ", " "});*/

		long daysBetweenStartAndEndFundraising = eventy.fundraisingEnd.getTime()-eventy.fundraisingStart.getTime();
		System.out.println("start day:: "+eventy.fundraisingStart);
		System.out.println("end day:: "+eventy.fundraisingEnd);
		System.out.println ("Days: " + TimeUnit.DAYS.convert(daysBetweenStartAndEndFundraising, TimeUnit.MILLISECONDS));
		int diffDays = (int)TimeUnit.DAYS.convert(daysBetweenStartAndEndFundraising, TimeUnit.MILLISECONDS);
		Calendar localCalendar = Calendar.getInstance(TimeZone.getDefault());
		int currentDayOfMonth = localCalendar.get(Calendar.DAY_OF_MONTH);
		System.out.println("currentDayOfMonth :: "+currentDayOfMonth);

		//if the diff  between start and end date is less than 90


	/*	if(diffDays<90 ){

		}*/
		if(currentDayOfMonth == 5 || currentDayOfMonth>5){
			System.out.println("currentDayOfMonth >= 5");
			Date currDate = new Date();
			long daysBetweenStartAndCurrDate = currDate.getTime()-eventy.fundraisingStart.getTime();
			int diffDays1 = (int)TimeUnit.DAYS.convert(daysBetweenStartAndCurrDate, TimeUnit.MILLISECONDS);
			if(diffDays1>=0 ){

				Calendar aCalendar = Calendar.getInstance();
// add -1 month to current month
				aCalendar.add(Calendar.MONTH, -1);
// set DATE to 1, so first date of previous month
				aCalendar.set(Calendar.DATE, 1);

				Date firstDateOfPreviousMonth = aCalendar.getTime();

// set actual maximum date of previous month
				aCalendar.set(Calendar.DATE,     aCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
//read it

				List<Donation> donationList = new ArrayList<Donation>();
				Date lastDateOfPreviousMonth = aCalendar.getTime();
				System.out.println("lastDateOfPreviousMonth :: "+lastDateOfPreviousMonth);
				for(Donation donation:donations){
					long daysBetweenPaidAndLastDateOfPrevMonth = donation.dateCreated.getTime()-lastDateOfPreviousMonth.getTime();
					int diffDays2 = (int)TimeUnit.DAYS.convert(daysBetweenPaidAndLastDateOfPrevMonth, TimeUnit.MILLISECONDS);
					if(diffDays2>=0){
						donationList.add(donation);
						records = payoutToStringArray1(donationList,eventy);

					}
				}
			}
		}else{

		}
		/*double firstPayout = netAmountIncomeNo*32.8/100;

		if(diffDays>90 && diffDays<120){
			Date monthAfter1 = new Date(eventy.fundraisingStart.getTime() + TimeUnit.DAYS.toMillis( 30 ));
			Date monthAfter2 = new Date(monthAfter1.getTime() + TimeUnit.DAYS.toMillis( 30 ));
			Date monthAfter3 = new Date(monthAfter2.getTime() + TimeUnit.DAYS.toMillis( 30 ));
			System.out.println("dayAfter :: "+monthAfter1);
			records.add(new String[]{"First Payout", totalAcc, "$" + firstPayout, "100%", "100%", " 32.8%"});
			records.add(new String[]{"Second Payout", totalAcc, "$" + totalAmount, "100%", "100%", " 41.5%"});
			records.add(new String[]{"Final Payout", totalAcc, "$" + totalAmount, "100%", "100%", " "});
		}*/

		return records;
	}




	private static List<String[]> payoutToStringArrayForMonth(List<Donation> donations, Event eventy){
		List<String[]> records = new ArrayList<String[]>();
		Date currDate = new Date();
		long daysBetweenStartAndCurrDate = currDate.getTime()-eventy.fundraisingStart.getTime();
		Date date1=null;
		Date date2=null;
		//Calendar calendar=Calendar.getInstance(eventy.fundraisingStart.getTime());
		date2=new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth(), eventy.fundraisingStart.getDate());
		if(eventy.fundraisingStart.getMonth() == 12){
			date1 = new Date(eventy.fundraisingStart.getYear()+1,  1, 5);
		}else {
			 date1 = new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth() + 1, 5);
		}

		System.out.println("date2----"+date2+"@@@@date1---"+date1+"@@@@currDate@@@"+currDate);




		long difffirstpayout=date1.getTime()-eventy.fundraisingStart.getTime();
		Map<Integer,Double> payout=new HashMap<Integer,Double>();
		Map<Integer,Double> consilation=new HashMap<Integer,Double>();
		int totalCreditAccNo=0;
		String totalCreditAcc=null;
		int totalCheckAccNo=0;
		String totalCheckAcc=null;

		double totalPaymentByCredit=0.0;
		double totalPaymentByCheck=0.0;
		double totalserviceTax=0.0;

		/*if(difffirstpayout<daysBetweenStartAndCurrDate){
			int i=1;
			do{
				double amount=0;
				double totalCreditAmountNo=0;
				double totalCreditAmount=0;
				double totalCheckAmountNo=0;
				for(Donation donation:donations) {
					if (donation != null) {
						*//*System.out.println("donation.dateCreated :: "+donation.dateCreated.after(date2)+"@@@@@@"+donation.dateCreated+"####"+date2);
						System.out.println("donation.dateCreated111 ::: "+donation.dateCreated.before(date1)+"$$$$$"+date1);
						System.out.println("------------Hello22222222222222----------------------------");
*//*
					if (donation.dateCreated.after(date2) && donation.dateCreated.before(date1)) {
						System.out.println("------------Hello----------------------------");

						if (donation != null && donation.donationType != Donation.DonationType.SPONSOR) {
							if (donation.paymentType == Donation.PaymentType.CREDIT) {
								//rowHeadervalue = "Paid By Credit Card";

								totalCreditAmountNo = totalCreditAmountNo + donation.amount;
								System.out.println("Amount in Check--------i--"+donation.amount+"##"+i);
								//totalCreditAmount = String.valueOf(totalCreditAmountNo);
								totalCreditAccNo = totalCreditAccNo + 1;
								totalCreditAcc = String.valueOf(totalCreditAccNo);

							} else if (donation.paymentType == Donation.PaymentType.CHECK) {
								//rowHeadervalue = "Paid By Credit Card";
								totalCheckAmountNo = totalCheckAmountNo + donation.amount;
								//totalCheckAmount = String.valueOf(totalCheckAmountNo);
								totalCheckAccNo = totalCheckAccNo + 1;
								totalCheckAcc = String.valueOf(totalCheckAccNo);

							}
						}



						//double totalAmountNo = totalCreditAmountNo+totalCheckAmountNo;
						totalPaymentByCredit = totalPaymentByCredit + totalCreditAmountNo;
						totalPaymentByCheck = totalPaymentByCheck + totalCheckAmountNo;
						amount = totalCheckAmountNo + totalCreditAmountNo;
						totalserviceTax=totalserviceTax+((amount*5)/100);


					} else {
						//break;
					}
				}
				}

                amount=amount-((amount*5)/100);
				System.out.println("!!!!!!!!!!!!!!!!!!amount!!!!!!!!!"+amount+"---"+i);
				double  payoutamount=(amount*75)/100;
				System.out.println("payoutamount---i****"+payoutamount+i);
				payout.put(i, payoutamount);
				consilation.put(i,(amount-payoutamount));
				System.out.println("consilation---i****" + (amount-payoutamount) + i);
				i++;

				date2=date1;
				if(date1.getMonth() == 12){
					date1 = new Date(date1.getYear()+1,  1, 5);
				}else {
					date1 = new Date(date1.getYear(), date1.getMonth() + 1, 5);
				}

				difffirstpayout=date1.getTime()-eventy.fundraisingStart.getTime();


			}while(difffirstpayout<daysBetweenStartAndCurrDate);

			double amount=0;
			double totalCreditAmountNo=0;
			double totalCreditAmount=0;
			double totalCheckAmountNo=0;
			for(Donation donation:donations) {
				if (donation != null) {


					if (donation.dateCreated.after(date2) && donation.dateCreated.before(date1)) {


					if (donation != null && donation.donationType != Donation.DonationType.SPONSOR) {
						if (donation.paymentType == Donation.PaymentType.CREDIT) {
							//rowHeadervalue = "Paid By Credit Card";
							totalCreditAmountNo = totalCreditAmountNo + donation.amount;
							//totalCreditAmount = String.valueOf(totalCreditAmountNo);
							totalCreditAccNo = totalCreditAccNo + 1;
							totalCreditAcc = String.valueOf(totalCreditAccNo);

						} else if (donation.paymentType == Donation.PaymentType.CHECK) {
							//rowHeadervalue = "Paid By Credit Card";
							totalCheckAmountNo = totalCheckAmountNo + donation.amount;
							//totalCheckAmount = String.valueOf(totalCheckAmountNo);
							totalCheckAccNo = totalCheckAccNo + 1;
							totalCheckAcc = String.valueOf(totalCheckAccNo);

						}
					}
					//totalPaymentByCredit = totalPaymentByCredit + totalCreditAmountNo;
					totalPaymentByCheck = totalPaymentByCheck + totalCheckAmountNo;
					amount = totalCheckAmountNo + totalCreditAmountNo;
						totalserviceTax=totalserviceTax+((amount*5)/100);


				} else {
					//break;
				}
			}
			}

			//amount=amount-((amount*5)/100);
			//double  payoutamount=(amount*75)/100;
			payout.put(i,0.0);

			consilation.put(i,(amount));
			System.out.println("!!!!!!!!!!!!!!!!!!amount!!!!!!!!!" + amount + "---" + i);



		}
		else{


			int i=1;

			double amount=0;
			double totalCreditAmountNo=0;
			double totalCreditAmount=0;
			double totalCheckAmountNo=0;
			double totalCreditAmountNoForSponsor=0.0;
			int totalCreditAccNoForSponsor=0;
			double totalCheckAmountNoForSponsor=0.0;
			double totalAmountForSponsor=0.0;
			int totalCheckAccNoForSponsor=0;
			int totalAccNoForsponsor=0;
			for(Donation donation:donations) {

				if (donation != null) {


					if (donation.dateCreated.after(date2) && donation.dateCreated.before(date1)) {

						System.out.println("");

					if (donation != null && donation.donationType != Donation.DonationType.SPONSOR) {
						if (donation.paymentType == Donation.PaymentType.CREDIT) {
							//rowHeadervalue = "Paid By Credit Card";
							totalCreditAmountNo = totalCreditAmountNo + donation.amount;
							//totalCreditAmount = String.valueOf(totalCreditAmountNo);
							totalCreditAccNo = totalCreditAccNo + 1;
							totalCreditAcc = String.valueOf(totalCreditAccNo);

						} else if (donation.paymentType == Donation.PaymentType.CHECK) {
							//rowHeadervalue = "Paid By Credit Card";
							totalCheckAmountNo = totalCheckAmountNo + donation.amount;
							//totalCheckAmount = String.valueOf(totalCheckAmountNo);
							totalCheckAccNo = totalCheckAccNo + 1;
							totalCheckAcc = String.valueOf(totalCheckAccNo);

						}
					}
					//totalPaymentByCredit = totalPaymentByCredit + totalCreditAmountNo;
					//totalPaymentByCheck = totalPaymentByCheck + totalCheckAmountNo;
					amount = totalCheckAmountNo + totalCreditAmountNo;
						//totalserviceTax=totalserviceTax+((amount*5)/100);


						if (donation != null && donation.donationType == Donation.DonationType.SPONSOR) {
							if (donation.paymentType == Donation.PaymentType.CREDIT) {
								//rowHeadervalue = "Paid By Credit Card";
								totalCreditAmountNoForSponsor = totalCreditAmountNoForSponsor + donation.amount;
								//totalCreditAmount = String.valueOf(totalCreditAmountNo);
								totalCreditAccNoForSponsor = totalCreditAccNoForSponsor + 1;
								//totalCreditAcc = String.valueOf(totalCreditAccNo);

							} else if (donation.paymentType == Donation.PaymentType.CHECK) {
								//rowHeadervalue = "Paid By Credit Card";
								totalCheckAmountNoForSponsor = totalCheckAmountNoForSponsor + donation.amount;
								//totalCheckAmount = String.valueOf(totalCheckAmountNo);
								totalCheckAccNoForSponsor = totalCheckAccNoForSponsor + 1;
								//totalCheckAcc = String.valueOf(totalCheckAccNo);

							}
						}
						totalAmountForSponsor = totalCreditAmountNoForSponsor + totalCheckAmountNoForSponsor;
						totalAccNoForsponsor = totalCreditAccNoForSponsor + totalCheckAccNoForSponsor;
						double creditAmountForSponsorPerct=totalCreditAmountNoForSponsor/totalAmountForSponsor*100;
						double checkAmountForSponsorPerct=totalCheckAmountNoForSponsor/totalAmountForSponsor*100;
						double creditAccNoForSponsorPerct=totalCreditAccNoForSponsor/totalAccNoForsponsor*100;
						double checkAccNoForSponsorPerct=totalCheckAccNoForSponsor/totalAccNoForsponsor*100;


						//gross income
						//double grossIncome =amount + totalAmountForSponsor;



				} else {
					//break;
				}

			}
			}


			//gross income
			double grossIncome =amount + totalAmountForSponsor;

			//service fee
			double serviceFeePecnt= eventy.serviceFee;
			if(serviceFeePecnt == 0.0){
				serviceFeePecnt = 10.0;
			}

			double serviceFeeAmount = grossIncome*serviceFeePecnt/100;

			//net income
            double netIncome= grossIncome - serviceFeeAmount;



			//amount=amount-((amount*5)/100);
			//double  payoutamount=(amount*75)/100;
			payout.put(i,0.0);
			System.out.println("!!!!!!!!!!!!!!!!!!amount!!!!!!!!!" + amount + "---" + i);
			consilation.put(i,(amount));








		}

		int diffDays1 = (int)TimeUnit.DAYS.convert(daysBetweenStartAndCurrDate, TimeUnit.MILLISECONDS);
        int diffMonth = diffDays1/30;



		for (Map.Entry<Integer, Double> entry : payout.entrySet()) {
			String key = entry.getKey().toString();;
			Double value = entry.getValue();
			System.out.println("%%%key, " + key + "%%%value " + value );
		}


		for (Map.Entry<Integer, Double> entry : consilation.entrySet()) {
			String key = entry.getKey().toString();;
			Double value = entry.getValue();
			System.out.println("---key, " + key + "--- value " + value );
		}



		//System.out.println("-----------totalPaymentByCredit-------"+totalPaymentByCredit);
		//System.out.println("------------totalPaymentByCheck---------------"+totalPaymentByCheck);
		//System.out.println("###########totalCreditAccNo###############"+totalCreditAccNo);
		//System.out.println("#########totalCheckAccNo#############"+totalCheckAccNo);
		//System.out.println("@@@@@@@@@@@@@@@@@@totalserviceTax@@@@@@@@@@@@@@@"+totalserviceTax);
		*/


      //----rimi-----start----------------21.01.2016-------------------------------------------------//

		double scholasticChallengeFeeAmount = 0.0;
		double scholasticChallengeFeeAmountPerct = eventy.serviceFee;
		if(scholasticChallengeFeeAmountPerct == 0){
			scholasticChallengeFeeAmountPerct = 10.0;
		}
		long daysBetweenStartAndEndFundraising = eventy.fundraisingEnd.getTime()-eventy.fundraisingStart.getTime();
		System.out.println("start day:: "+eventy.fundraisingStart);
		System.out.println("end day:: "+eventy.fundraisingEnd);
		System.out.println ("Days: " + TimeUnit.DAYS.convert(daysBetweenStartAndEndFundraising, TimeUnit.MILLISECONDS));
		int diffDays = (int)TimeUnit.DAYS.convert(daysBetweenStartAndEndFundraising, TimeUnit.MILLISECONDS);
		Calendar localCalendar = Calendar.getInstance(TimeZone.getDefault());
		int currentDayOfMonth = localCalendar.get(Calendar.DAY_OF_MONTH);
		System.out.println("currentDayOfMonth :: "+currentDayOfMonth);
		int diffMonthBetweenStartAndEndFundraising = (int) diffDays/30;
		Date currDate1 = new Date();
		long daysBetweenStartAndCurrDate1 = currDate1.getTime()-eventy.fundraisingStart.getTime();
		int diffDaysBetweenStartAndCurrDate1 = (int)TimeUnit.DAYS.convert(daysBetweenStartAndCurrDate1, TimeUnit.MILLISECONDS);
		int monthBetweenStartAndCurrDate = diffDaysBetweenStartAndCurrDate1/30;
		date1 = new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth() + 1, 5);
		//int diffMonthBetweenStartAndEndFundraising



		//Calculate Difference between month---


		//Date firstDate = new Date();
		//Date enddate= even;
	/*	try{
		firstDate =  new SimpleDateFormat("yyyy-MM-dd").parse("2016-03-04");
			enddate= new SimpleDateFormat("yyyy-MM-dd").parse("2016-05-03");

		}catch(Exception e){

		}*/
		//System.out.println("----------------firstDate-------------------"+firstDate);
		//System.out.println("----------------enddate-------------------"+enddate);



		/*int months  = (firstDate.getYear() - eventy.fundraisingStart.getYear()* 12 +
				(firstDate.getMonth()- eventy.fundraisingStart.getMonth()) +
				(firstDate.getDate() >= eventy.fundraisingStart.getDate()? 0: -1));*/


		LocalDate start = new LocalDate(eventy.fundraisingStart.getTime());
		//LocalDate start = new LocalDate(firstDate.getTime());
		LocalDate end = new LocalDate(currDate1.getTime());

		monthBetweenStartAndCurrDate =Months.monthsBetween(start, end).getMonths();






		//Date startDate = new Date("2015-02-14");


	/*	int months  = (firstDate.getTime().get(Calendar.YEAR) - secondDate.get(Calendar.YEAR)) * 12 +
				(firstDate.get(Calendar.MONTH)- secondDate.get(Calendar.MONTH)) +
				(firstDate.get(Calendar.DAY_OF_MONTH) >= secondDate.get(Calendar.DAY_OF_MONTH)? 0: -1);*/


		System.out.println("-----------------------Diff months is-------------------" + monthBetweenStartAndCurrDate);

		System.out.println();


//Calculate Difference between month---







		//Test


		System.out.println();

		int i = 0;
		int firstPayoutCount = 0;
		double reconciliationAmount = 0.0;
		HashMap payOutTimeAndPaidAmountMap = new HashMap();
		HashMap payOutTimeAndAllAmountsMap = new HashMap();
		HashMap payOutTimeAndPayableAmountMap = new HashMap();
		if(monthBetweenStartAndCurrDate<diffMonthBetweenStartAndEndFundraising){
			for( i = 0 ;i<monthBetweenStartAndCurrDate;i++) {

				System.out.println("--------------------------------------i---------------------" + i);

				Date dateStartFundraising = eventy.fundraisingStart;


				//fundraising start date+ i no of month
				Calendar calendar = Calendar.getInstance();


				//java.util.Date date= new Date();
				Calendar cal = Calendar.getInstance();
				System.out.println("------------@@@@@@" + eventy.fundraisingStart);

				//Date date
				cal.setTime(date2);
				//int month = eventy.fundraisingStart.getMonth();

				//System.out.println("---hjhhh-----"+month);
				//cal.add(month, i);
				//System.out.println("----------Jan--------"+cal.MONTH);
				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
				Date eachMonthFirstDay = cal.getTime();
				System.out.println("---eachMonthFirstDay3----" + eachMonthFirstDay);
				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
				Date eachMonthLastDay = cal.getTime();
				System.out.println("---eachMonthLastDay3---" + eachMonthLastDay);


				//current day
				Date currdate = new Date();


				//total logic
				// for donations variable
				double totalCreditAmountNo = 0.0;
				double totalCheckAmountNo = 0.0;
				totalCreditAccNo = 0;
				totalCheckAccNo = 0;
				double totalDonationsAmount = 0.0;
				double totalDonationsAccNo = 0.0;

				//for sponsors variable
				double totalCreditAmountNoForSponsor = 0.0;
				double totalCheckAmountNoForSponsor = 0.0;
				double totalCreditAccNoForSponsor = 0.0;
				double totalCheckAccNoForSponsor = 0.0;
				double totalSponsorAmount = 0.0;
				double totalSponsorsAccNo = 0.0;


				if (donations != null && donations.size() > 0) {


				for (Donation donation : donations) {
					if(donation !=null){
					if ((donation.dateCreated.equals(eachMonthFirstDay) || donation.dateCreated.after(eachMonthFirstDay)) && (donation.dateCreated.before(eachMonthLastDay) || donation.dateCreated.equals(eachMonthLastDay))) {
						//total donations calculation for i where i
						if (donation.donationType != Donation.DonationType.SPONSOR) {
							if (donation.paymentType == Donation.PaymentType.CREDIT) {
								//rowHeadervalue = "Paid By Credit Card";
								totalCreditAmountNo = totalCreditAmountNo + donation.amount;
								//totalCreditAmount = String.valueOf(totalCreditAmountNo);
								totalCreditAccNo = totalCreditAccNo + 1;
								//totalCreditAcc = String.valueOf(totalCreditAccNo);

							} else if (donation.paymentType == Donation.PaymentType.CHECK) {
								//rowHeadervalue = "Paid By Credit Card";
								totalCheckAmountNo = totalCheckAmountNo + donation.amount;
								//totalCheckAmount = String.valueOf(totalCheckAmountNo);
								totalCheckAccNo = totalCheckAccNo + 1;
								//totalCheckAcc = String.valueOf(totalCheckAccNo);

							}
						}
						if (donation.donationType == Donation.DonationType.SPONSOR) {
							if (donation.paymentType == Donation.PaymentType.CREDIT) {
								//rowHeadervalue = "Paid By Credit Card";
								totalCreditAmountNoForSponsor = totalCreditAmountNoForSponsor + donation.amount;
								//totalCreditAmount = String.valueOf(totalCreditAmountNo);
								totalCreditAccNoForSponsor = totalCreditAccNoForSponsor + 1;
								//totalCreditAcc = String.valueOf(totalCreditAccNo);

							} else if (donation.paymentType == Donation.PaymentType.CHECK) {
								//rowHeadervalue = "Paid By Credit Card";
								totalCheckAmountNoForSponsor = totalCheckAmountNoForSponsor + donation.amount;
								//totalCheckAmount = String.valueOf(totalCheckAmountNo);
								totalCheckAccNoForSponsor = totalCheckAccNoForSponsor + 1;
								//totalCheckAcc = String.valueOf(totalCheckAccNo);

							}
						}


					}
				}
				}

			}

				//calculations of donations
				totalDonationsAmount = totalCreditAmountNo+totalCheckAmountNo;
				System.out.println("----totalDonationsAmount---"+totalDonationsAmount);
				totalDonationsAccNo = totalCreditAccNo+totalCheckAccNo;
				System.out.println("----totalDonationsAccNo---"+totalDonationsAccNo);
               double totalCreditDonationsPerct = (totalCreditAmountNo/totalDonationsAmount)*100;
			   double totalCheckDonationsPerct = (totalCheckAmountNo/totalDonationsAmount)*100;
				double totalCreditDonationsAccNoPerct = (totalCreditAccNo/totalDonationsAccNo)*100;
				double totalCheckDonationsAccNoPerct = (totalCheckAccNo/totalDonationsAccNo)*100;


				//total sponsorship calculations
				totalSponsorAmount = totalCreditAmountNoForSponsor+totalCheckAmountNoForSponsor;
				totalSponsorsAccNo = totalCreditAccNoForSponsor+totalCheckAccNoForSponsor;
				double totalCreditSponsorsPerct = (totalCreditAmountNoForSponsor/totalSponsorAmount)*100;
				double totalCheckSponsorsPerct = (totalCheckAmountNoForSponsor/totalSponsorAmount)*100;
				double totalCreditSponsorsAccNoPerct = (totalCreditAccNoForSponsor/totalSponsorsAccNo)*100;
				double totalCheckSponsorsAccNoPerct = (totalCheckAccNoForSponsor/totalSponsorsAccNo)*100;

				//gross income
				double grossIncomeAmount = totalDonationsAmount + totalSponsorAmount;

				//scholastic challenge fee
				scholasticChallengeFeeAmount = 0.0;

				/*double scholasticChallengeFeeAmountPerct = eventy.serviceFee;
				if(scholasticChallengeFeeAmountPerct == 0){
					scholasticChallengeFeeAmountPerct = 10.0;
				}*/

				scholasticChallengeFeeAmount = grossIncomeAmount*scholasticChallengeFeeAmountPerct/100;



				//net income

				double netIncomeAmount = grossIncomeAmount-scholasticChallengeFeeAmount;

				//first payout

				double remainingPayableAmount = 0.0;
				double paidoutAmount = 0.0;
				firstPayoutCount = firstPayoutCount + 1;
                 //paid amout percentage
				double paidAmountPercent = 75;

				paidoutAmount = (netIncomeAmount*75)/100;
				remainingPayableAmount = netIncomeAmount - paidoutAmount;





				//Commend on 27.01.16--------
			/*	Formatter formatter=new Formatter();
				formatter.format("%tb",cal);

				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-month",formatter);*/






				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditAmountNo",totalCreditAmountNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckAmountNo",totalCheckAmountNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalDonationsAmount",totalDonationsAmount);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditAccNo",totalCreditAccNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckAccNo",totalCheckAccNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalDonationsAccNo",totalDonationsAccNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditDonationsPerct",totalCreditDonationsPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckDonationsPerct",totalCheckDonationsPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditDonationsAccNoPerct",totalCreditDonationsAccNoPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckDonationsAccNoPerct",totalCheckDonationsAccNoPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditAmountNoForSponsor",totalCreditAmountNoForSponsor);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckAmountNoForSponsor",totalCheckAmountNoForSponsor);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalSponsorAmount",totalSponsorAmount);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditAccNoForSponsor",totalCreditAccNoForSponsor);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckAccNoForSponsor",totalCheckAccNoForSponsor);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalSponsorsAccNo",totalSponsorsAccNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditSponsorsPerct",totalCreditSponsorsPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckSponsorsPerct",totalCheckSponsorsPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditSponsorsAccNoPerct",totalCreditSponsorsAccNoPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckSponsorsAccNoPerct",totalCheckSponsorsAccNoPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-grossIncomeAmount",grossIncomeAmount);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-netIncomeAmount",netIncomeAmount);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-scholasticChallengeFeeAmount",scholasticChallengeFeeAmount);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-netAmount",netIncomeAmount);
				payOutTimeAndPaidAmountMap.put(firstPayoutCount,paidoutAmount);
				payOutTimeAndPayableAmountMap.put(firstPayoutCount,remainingPayableAmount);


				/*localCalendar = Calendar.getInstance(TimeZone.getDefault());
				currentDayOfMonth = localCalendar.get(Calendar.DAY_OF_MONTH);
				if(currentDayOfMonth==5 || currentDayOfMonth>5){
					for(int i1=1;i1<=firstPayoutCount;i1++){
						//i1 payout will be shown

						double remainingPayableAmount1 = (double)payOutTimeAndPaidAmountMap.get(i1);
						//reconciliationAmount
						reconciliationAmount = reconciliationAmount+remainingPayableAmount1;


					}
				}else{
					for(int i1=1;i1<=firstPayoutCount-1;i1++){
						//i1 payout will be shown

						double remainingPayableAmount1 = (double)payOutTimeAndPaidAmountMap.get(i1);
						//reconciliationAmount
						reconciliationAmount = reconciliationAmount+remainingPayableAmount1;


					}
				}
*/



				if(date2.getMonth() == 11){
					date2 = new Date(date2.getYear()+1,  0, date2.getDate());
				}else {
					date2 = new Date(date2.getYear(), date2.getMonth() + 1, date2.getDate());
				}


				System.out.println("------------------date2----------------"+date2);


				//


			}






			records.add(new String[]{"Event Start Date", String.valueOf(eventy.fundraisingStart), "", "", "",  ""});
			records.add(new String[]{"Event End Date", String.valueOf(eventy.fundraisingEnd), "", "", "",  ""});
			records.add(new String[]{"SC Fee", String.valueOf(scholasticChallengeFeeAmountPerct)+"%", "", "", "",  ""});
			records.add(new String[]{"Monthly payout ", "75%", "", "", "",  ""});
			records.add(new String[]{"", "", "", "", "",  ""});

			String[] string =  new String[100];
			string[0]="Collections Summary";








			/********Show Month Between Start and end*********/
			Date date3=null;
			Calendar cal2 = Calendar.getInstance();
			//cal2.setTime();
			for( i = 0 ;i<monthBetweenStartAndCurrDate;i++){
				date3=new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth(), eventy.fundraisingStart.getDate());
				cal2.setTime(date3);

				Formatter formatter=new Formatter();
				formatter.format("%tb", cal2);

				payOutTimeAndAllAmountsMap.put(firstPayoutCount + "-month", formatter);


				if(date3.getMonth() == 12){
					date3 = new Date(eventy.fundraisingStart.getYear()+1,  1, 5);
				}else {
					date3 = new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth() + 1, 5);
				}

			}




				for(int i1=1;i1<=monthBetweenStartAndCurrDate;i1++){
				String element=payOutTimeAndAllAmountsMap.get(i1 + "-month").toString();
				string[i1]=element;
			     }



			records.add(string);
			records.add(new String[]{"", "", "", "", "",  ""});


			String[] totalCheckDonations =  new String[100];
			totalCheckDonations[0] = "Cheque";

			String[]  totalCreditDonations = new String[100];
			totalCreditDonations[0] = "Credit Card";

			String[]  grossIncomeDonations = new String[100];
			grossIncomeDonations[0] = "Gross Income";


			String[] scFee = new String[100];
			scFee[0] = "SC Fee";


			String[] disbursibleAmount = new String[10];
			disbursibleAmount[0] = "Disbursible Amount";



			for(int i1=1;i1<=firstPayoutCount;i1++){
				//i1 payout will be shown
				System.out.println("-----------------------------------i1------------------------------------"+i1);
				System.out.println("totalCreditAmountNo::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAmountNo"));
				System.out.println("totalCheckAmountNo::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAmountNo"));
				System.out.println("totalDonationsAmount::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalDonationsAmount"));
				System.out.println("totalCreditAccNo::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAccNo"));
				System.out.println("totalCheckAccNo::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAccNo"));
				System.out.println("totalDonationsAccNo::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalDonationsAccNo"));
				System.out.println("totalCreditDonationsPerct::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditDonationsPerct"));
				System.out.println("totalCheckDonationsPerct::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckDonationsPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditDonationsAccNoPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckDonationsAccNoPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAmountNoForSponsor"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAmountNoForSponsor"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalSponsorAmount"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAccNoForSponsor"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAccNoForSponsor"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalSponsorsAccNo"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditSponsorsPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckSponsorsPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditSponsorsAccNoPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckSponsorsAccNoPerct"));
				System.out.println("grossIncomeAmount::"+payOutTimeAndAllAmountsMap.get(i1 + "-grossIncomeAmount"));
				System.out.println("netIncomeAmount::"+payOutTimeAndAllAmountsMap.get(i1 + "-netIncomeAmount"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-scholasticChallengeFeeAmount"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-netAmount"));
                double totalCreditAmounts = (double)payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAmountNoForSponsor")+(double)payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAmountNo");
				/*totalCheckDonations[i1] =String.valueOf(Double.parseDouble(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAmountNoForSponsor")+"") + Double.parseDouble(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAccNo")+"")) ;*/


				totalCheckDonations[i1] =String.valueOf(totalCreditAmounts) ;
				double totalChequeAmounts = (double)payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAmountNo")+(double)payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAmountNoForSponsor");
				totalCreditDonations[i1] = String.valueOf(totalChequeAmounts);
				grossIncomeDonations[i1] = String.valueOf((double)payOutTimeAndAllAmountsMap.get(i1 + "-grossIncomeAmount"));
				scFee[i1] = String.valueOf((double)payOutTimeAndAllAmountsMap.get(i1 + "-scholasticChallengeFeeAmount"));
				disbursibleAmount[i1] = String.valueOf((double)payOutTimeAndAllAmountsMap.get(i1 + "-netIncomeAmount"));

				//records.add(new String[]{"Balance Due", " ", "($" + remainingAmount + ")", "100%", "100%", remainingAmountPercent + "%"});


			}

			records.add(totalCheckDonations);
			records.add(totalCreditDonations);
			records.add(grossIncomeDonations);
			records.add(scFee);
			records.add(disbursibleAmount);
			localCalendar = Calendar.getInstance(TimeZone.getDefault());
			currentDayOfMonth = localCalendar.get(Calendar.DAY_OF_MONTH);
			records.add(new String[]{"column1", "payout", "Withheld amount", "", "",  ""});
			if(currentDayOfMonth==5 || currentDayOfMonth>5){
				for(int i1=1;i1<=firstPayoutCount;i1++){
					//i1 payout will be shown

					double paidAmount1 = (double)payOutTimeAndPaidAmountMap.get(i1);
					System.out.println("paidAmount1----"+paidAmount1);
					double remainingPayableAmount1 = (double)payOutTimeAndPayableAmountMap.get(i1);
					//reconciliationAmount
					System.out.println("remainingPayableAmount1890 "+remainingPayableAmount1);

					reconciliationAmount = reconciliationAmount+remainingPayableAmount1;
					System.out.println("reconciliationAmount890 "+reconciliationAmount);



					records.add(new String[]{String.valueOf(i1), String.valueOf((double)payOutTimeAndPaidAmountMap.get(i1)),String.valueOf((double)reconciliationAmount) , "", "",  ""});


				}
			}else{


				if(currentDayOfMonth==eventy.fundraisingStart.getDate()){
					for(int i1=1;i1<=firstPayoutCount-1;i1++){


						double paidAmount1 = (double)payOutTimeAndPaidAmountMap.get(i1);
						System.out.println("paidAmount1----"+paidAmount1);
						double remainingPayableAmount1 = (double)payOutTimeAndPayableAmountMap.get(i1);
						//reconciliationAmount
						System.out.println("remainingPayableAmount2890 "+remainingPayableAmount1);

						reconciliationAmount = reconciliationAmount+remainingPayableAmount1;
						System.out.println("reconciliationAmount3890 "+reconciliationAmount);



						records.add(new String[]{String.valueOf(i1), String.valueOf((double)payOutTimeAndPaidAmountMap.get(i1)),String.valueOf((double)reconciliationAmount) , "", "",  ""});


					}
				}
				else{
					for(int i1=1;i1<=firstPayoutCount;i1++){


						double paidAmount1 = (double)payOutTimeAndPaidAmountMap.get(i1);
						System.out.println("paidAmount115634----"+paidAmount1);

						//i1 payout will be shown

						double remainingPayableAmount1 = (double)payOutTimeAndPayableAmountMap.get(i1);
						//reconciliationAmount


						System.out.println("remainingPayableAmount15634"+remainingPayableAmount1);
						System.out.println("reconciliationAmount5634"+reconciliationAmount);
						reconciliationAmount = reconciliationAmount+remainingPayableAmount1;

						records.add(new String[]{String.valueOf(i1), String.valueOf((double)payOutTimeAndPaidAmountMap.get(i1)),String.valueOf((double)reconciliationAmount) , "", "",  ""});
					}
				}

			}

			//If the current Date greater than 5th date
			if(currDate.getDate()>=5){

				//total logic
				// for donations variable
				double totalCreditAmountNo = 0.0;
				double totalCheckAmountNo = 0.0;
				totalCreditAccNo = 0;
				totalCheckAccNo = 0;
				double totalDonationsAmount = 0.0;
				double totalDonationsAccNo = 0.0;

				//for sponsors variable
				double totalCreditAmountNoForSponsor = 0.0;
				double totalCheckAmountNoForSponsor = 0.0;
				double totalCreditAccNoForSponsor = 0.0;
				double totalCheckAccNoForSponsor = 0.0;
				double totalSponsorAmount = 0.0;
				double totalSponsorsAccNo = 0.0;





				Calendar cal = Calendar.getInstance();
				System.out.println("------------@@@@@@" + eventy.fundraisingStart);

				//Date date
				cal.setTime(currDate);




				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
				Date eachMonthFirstDay = cal.getTime();
				System.out.println("---eachMonthFirstDay4----" + eachMonthFirstDay);
				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
				Date eachMonthLastDay = currDate;
				System.out.println("---eachMonthLastDay4---" + eachMonthLastDay);


				if (donations != null && donations.size() > 0) {


					for (Donation donation : donations) {
						if(donation !=null){
							if ((donation.dateCreated.equals(eachMonthFirstDay) || donation.dateCreated.after(eachMonthFirstDay)) && (donation.dateCreated.before(eachMonthLastDay) || donation.dateCreated.equals(eachMonthLastDay))) {
								//total donations calculation for i where i
								if (donation.donationType != Donation.DonationType.SPONSOR) {
									if (donation.paymentType == Donation.PaymentType.CREDIT) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCreditAmountNo = totalCreditAmountNo + donation.amount;
										//totalCreditAmount = String.valueOf(totalCreditAmountNo);
										totalCreditAccNo = totalCreditAccNo + 1;
										//totalCreditAcc = String.valueOf(totalCreditAccNo);

									} else if (donation.paymentType == Donation.PaymentType.CHECK) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCheckAmountNo = totalCheckAmountNo + donation.amount;
										//totalCheckAmount = String.valueOf(totalCheckAmountNo);
										totalCheckAccNo = totalCheckAccNo + 1;
										//totalCheckAcc = String.valueOf(totalCheckAccNo);

									}
								}
								if (donation.donationType == Donation.DonationType.SPONSOR) {
									if (donation.paymentType == Donation.PaymentType.CREDIT) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCreditAmountNoForSponsor = totalCreditAmountNoForSponsor + donation.amount;
										//totalCreditAmount = String.valueOf(totalCreditAmountNo);
										totalCreditAccNoForSponsor = totalCreditAccNoForSponsor + 1;
										//totalCreditAcc = String.valueOf(totalCreditAccNo);

									} else if (donation.paymentType == Donation.PaymentType.CHECK) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCheckAmountNoForSponsor = totalCheckAmountNoForSponsor + donation.amount;
										//totalCheckAmount = String.valueOf(totalCheckAmountNo);
										totalCheckAccNoForSponsor = totalCheckAccNoForSponsor + 1;
										//totalCheckAcc = String.valueOf(totalCheckAccNo);

									}
								}


							}
						}
					}

				}



				double  withheldAmount=totalCreditAmountNo+totalCheckAmountNo+totalCheckAmountNoForSponsor+totalCreditAmountNoForSponsor;
				withheldAmount=withheldAmount-((withheldAmount*scholasticChallengeFeeAmountPerct)/100);

				records.add(new String[]{"Final Remaining Amount", "",String.valueOf((double)withheldAmount) , "", "",  ""});





			}



			else{


				double totalCreditAmountNo = 0.0;
				double totalCheckAmountNo = 0.0;
				totalCreditAccNo = 0;
				totalCheckAccNo = 0;
				double totalDonationsAmount = 0.0;
				double totalDonationsAccNo = 0.0;

				//for sponsors variable
				double totalCreditAmountNoForSponsor = 0.0;
				double totalCheckAmountNoForSponsor = 0.0;
				double totalCreditAccNoForSponsor = 0.0;
				double totalCheckAccNoForSponsor = 0.0;
				double totalSponsorAmount = 0.0;
				double totalSponsorsAccNo = 0.0;





				Calendar cal = Calendar.getInstance();
				System.out.println("------------@@@@@@" + eventy.fundraisingStart);

				//Date date
				Date startDateCheck=new Date();

				//date2=new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth(), eventy.fundraisingStart.getDate());
				if(currDate.getMonth() == 1){
					startDateCheck = new Date(eventy.fundraisingStart.getYear()-1,  12, 1);
				}else {
					startDateCheck = new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth() - 1, 1);
				}









				cal.setTime(startDateCheck);




				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
				Date eachMonthFirstDay = cal.getTime();
				System.out.println("---eachMonthFirstDay5----" + eachMonthFirstDay);
				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
				Date eachMonthLastDay = currDate;
				System.out.println("---eachMonthLastDay5---" + eachMonthLastDay);


				if (donations != null && donations.size() > 0) {


					for (Donation donation : donations) {
						if(donation !=null){
							if ((donation.dateCreated.equals(eachMonthFirstDay) || donation.dateCreated.after(eachMonthFirstDay)) && (donation.dateCreated.before(eachMonthLastDay) || donation.dateCreated.equals(eachMonthLastDay))) {
								//total donations calculation for i where i
								if (donation.donationType != Donation.DonationType.SPONSOR) {
									if (donation.paymentType == Donation.PaymentType.CREDIT) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCreditAmountNo = totalCreditAmountNo + donation.amount;
										//totalCreditAmount = String.valueOf(totalCreditAmountNo);
										totalCreditAccNo = totalCreditAccNo + 1;
										//totalCreditAcc = String.valueOf(totalCreditAccNo);

									} else if (donation.paymentType == Donation.PaymentType.CHECK) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCheckAmountNo = totalCheckAmountNo + donation.amount;
										//totalCheckAmount = String.valueOf(totalCheckAmountNo);
										totalCheckAccNo = totalCheckAccNo + 1;
										//totalCheckAcc = String.valueOf(totalCheckAccNo);

									}
								}
								if (donation.donationType == Donation.DonationType.SPONSOR) {
									if (donation.paymentType == Donation.PaymentType.CREDIT) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCreditAmountNoForSponsor = totalCreditAmountNoForSponsor + donation.amount;
										//totalCreditAmount = String.valueOf(totalCreditAmountNo);
										totalCreditAccNoForSponsor = totalCreditAccNoForSponsor + 1;
										//totalCreditAcc = String.valueOf(totalCreditAccNo);

									} else if (donation.paymentType == Donation.PaymentType.CHECK) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCheckAmountNoForSponsor = totalCheckAmountNoForSponsor + donation.amount;
										//totalCheckAmount = String.valueOf(totalCheckAmountNo);
										totalCheckAccNoForSponsor = totalCheckAccNoForSponsor + 1;
										//totalCheckAcc = String.valueOf(totalCheckAccNo);

									}
								}


							}
						}
					}

				}



				double  withheldAmount=totalCreditAmountNo+totalCheckAmountNo+totalCheckAmountNoForSponsor+totalCreditAmountNoForSponsor;
				withheldAmount=withheldAmount-((withheldAmount*scholasticChallengeFeeAmountPerct)/100);

				records.add(new String[]{"Final Remaining Amount", "",String.valueOf((double)withheldAmount) , "", "",  ""});












			}










		}



		//If the Fundraising End date before current date-------------------------














	else{
			for( i = 0 ;i<diffMonthBetweenStartAndEndFundraising;i++) {

				System.out.println("--------------------------------------i---------------------" + i);

				Date dateStartFundraising = eventy.fundraisingStart;


				//fundraising start date+ i no of month
				Calendar calendar = Calendar.getInstance();


				//java.util.Date date= new Date();
				Calendar cal = Calendar.getInstance();
				System.out.println("------------@@@@@@" + eventy.fundraisingStart);

				//Date date
				cal.setTime(date2);
				//int month = eventy.fundraisingStart.getMonth();

				//System.out.println("---hjhhh-----"+month);
				//cal.add(month, i);
				//System.out.println("----------Jan--------"+cal.MONTH);
				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
				Date eachMonthFirstDay = cal.getTime();
				System.out.println("---eachMonthFirstDay6----" + eachMonthFirstDay);
				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
				Date eachMonthLastDay = cal.getTime();
				System.out.println("---eachMonthLastDay6---" + eachMonthLastDay);


				//current day
				Date currdate = new Date();


				//total logic
				// for donations variable
				double totalCreditAmountNo = 0.0;
				double totalCheckAmountNo = 0.0;
				totalCreditAccNo = 0;
				totalCheckAccNo = 0;
				double totalDonationsAmount = 0.0;
				double totalDonationsAccNo = 0.0;

				//for sponsors variable
				double totalCreditAmountNoForSponsor = 0.0;
				double totalCheckAmountNoForSponsor = 0.0;
				double totalCreditAccNoForSponsor = 0.0;
				double totalCheckAccNoForSponsor = 0.0;
				double totalSponsorAmount = 0.0;
				double totalSponsorsAccNo = 0.0;


				if (donations != null && donations.size() > 0) {


					for (Donation donation : donations) {
						if(donation !=null){
							if ((donation.dateCreated.equals(eachMonthFirstDay) || donation.dateCreated.after(eachMonthFirstDay)) && (donation.dateCreated.before(eachMonthLastDay) || donation.dateCreated.equals(eachMonthLastDay))) {
								//total donations calculation for i where i
								if (donation.donationType != Donation.DonationType.SPONSOR) {
									if (donation.paymentType == Donation.PaymentType.CREDIT) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCreditAmountNo = totalCreditAmountNo + donation.amount;
										//totalCreditAmount = String.valueOf(totalCreditAmountNo);
										totalCreditAccNo = totalCreditAccNo + 1;
										//totalCreditAcc = String.valueOf(totalCreditAccNo);

									} else if (donation.paymentType == Donation.PaymentType.CHECK) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCheckAmountNo = totalCheckAmountNo + donation.amount;
										//totalCheckAmount = String.valueOf(totalCheckAmountNo);
										totalCheckAccNo = totalCheckAccNo + 1;
										//totalCheckAcc = String.valueOf(totalCheckAccNo);

									}
								}
								if (donation.donationType == Donation.DonationType.SPONSOR) {
									if (donation.paymentType == Donation.PaymentType.CREDIT) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCreditAmountNoForSponsor = totalCreditAmountNoForSponsor + donation.amount;
										//totalCreditAmount = String.valueOf(totalCreditAmountNo);
										totalCreditAccNoForSponsor = totalCreditAccNoForSponsor + 1;
										//totalCreditAcc = String.valueOf(totalCreditAccNo);

									} else if (donation.paymentType == Donation.PaymentType.CHECK) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCheckAmountNoForSponsor = totalCheckAmountNoForSponsor + donation.amount;
										//totalCheckAmount = String.valueOf(totalCheckAmountNo);
										totalCheckAccNoForSponsor = totalCheckAccNoForSponsor + 1;
										//totalCheckAcc = String.valueOf(totalCheckAccNo);

									}
								}


							}
						}
					}

				}

				//calculations of donations
				totalDonationsAmount = totalCreditAmountNo+totalCheckAmountNo;
				System.out.println("----totalDonationsAmount---"+totalDonationsAmount);
				totalDonationsAccNo = totalCreditAccNo+totalCheckAccNo;
				System.out.println("----totalDonationsAccNo---"+totalDonationsAccNo);
				double totalCreditDonationsPerct = (totalCreditAmountNo/totalDonationsAmount)*100;
				double totalCheckDonationsPerct = (totalCheckAmountNo/totalDonationsAmount)*100;
				double totalCreditDonationsAccNoPerct = (totalCreditAccNo/totalDonationsAccNo)*100;
				double totalCheckDonationsAccNoPerct = (totalCheckAccNo/totalDonationsAccNo)*100;


				//total sponsorship calculations
				totalSponsorAmount = totalCreditAmountNoForSponsor+totalCheckAmountNoForSponsor;
				totalSponsorsAccNo = totalCreditAccNoForSponsor+totalCheckAccNoForSponsor;
				double totalCreditSponsorsPerct = (totalCreditAmountNoForSponsor/totalSponsorAmount)*100;
				double totalCheckSponsorsPerct = (totalCheckAmountNoForSponsor/totalSponsorAmount)*100;
				double totalCreditSponsorsAccNoPerct = (totalCreditAccNoForSponsor/totalSponsorsAccNo)*100;
				double totalCheckSponsorsAccNoPerct = (totalCheckAccNoForSponsor/totalSponsorsAccNo)*100;

				//gross income
				double grossIncomeAmount = totalDonationsAmount + totalSponsorAmount;

				//scholastic challenge fee
				scholasticChallengeFeeAmount = 0.0;

				/*double scholasticChallengeFeeAmountPerct = eventy.serviceFee;
				if(scholasticChallengeFeeAmountPerct == 0){
					scholasticChallengeFeeAmountPerct = 10.0;
				}*/

				scholasticChallengeFeeAmount = grossIncomeAmount*scholasticChallengeFeeAmountPerct/100;



				//net income

				double netIncomeAmount = grossIncomeAmount-scholasticChallengeFeeAmount;

				//first payout

				double remainingPayableAmount = 0.0;
				double paidoutAmount = 0.0;
				firstPayoutCount = firstPayoutCount + 1;
				//paid amout percentage
				double paidAmountPercent = 75;

				paidoutAmount = (netIncomeAmount*75)/100;
				remainingPayableAmount = netIncomeAmount - paidoutAmount;



				//Commend on 27.01.16---------

		/*		Formatter formatter=new Formatter();
				formatter.format("%tb",cal);

				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-month",formatter);
				*/





				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditAmountNo",totalCreditAmountNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckAmountNo",totalCheckAmountNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalDonationsAmount",totalDonationsAmount);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditAccNo",totalCreditAccNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckAccNo",totalCheckAccNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalDonationsAccNo",totalDonationsAccNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditDonationsPerct",totalCreditDonationsPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckDonationsPerct",totalCheckDonationsPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditDonationsAccNoPerct",totalCreditDonationsAccNoPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckDonationsAccNoPerct",totalCheckDonationsAccNoPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditAmountNoForSponsor",totalCreditAmountNoForSponsor);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckAmountNoForSponsor",totalCheckAmountNoForSponsor);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalSponsorAmount",totalSponsorAmount);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditAccNoForSponsor",totalCreditAccNoForSponsor);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckAccNoForSponsor",totalCheckAccNoForSponsor);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalSponsorsAccNo",totalSponsorsAccNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditSponsorsPerct",totalCreditSponsorsPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckSponsorsPerct",totalCheckSponsorsPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditSponsorsAccNoPerct",totalCreditSponsorsAccNoPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckSponsorsAccNoPerct",totalCheckSponsorsAccNoPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-grossIncomeAmount",grossIncomeAmount);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-netIncomeAmount",netIncomeAmount);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-scholasticChallengeFeeAmount",scholasticChallengeFeeAmount);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-netAmount",netIncomeAmount);
				payOutTimeAndPaidAmountMap.put(firstPayoutCount,paidoutAmount);
				payOutTimeAndPayableAmountMap.put(firstPayoutCount,remainingPayableAmount);


				/*localCalendar = Calendar.getInstance(TimeZone.getDefault());
				currentDayOfMonth = localCalendar.get(Calendar.DAY_OF_MONTH);
				if(currentDayOfMonth==5 || currentDayOfMonth>5){
					for(int i1=1;i1<=firstPayoutCount;i1++){
						//i1 payout will be shown

						double remainingPayableAmount1 = (double)payOutTimeAndPaidAmountMap.get(i1);
						//reconciliationAmount
						reconciliationAmount = reconciliationAmount+remainingPayableAmount1;


					}
				}else{
					for(int i1=1;i1<=firstPayoutCount-1;i1++){
						//i1 payout will be shown

						double remainingPayableAmount1 = (double)payOutTimeAndPaidAmountMap.get(i1);
						//reconciliationAmount
						reconciliationAmount = reconciliationAmount+remainingPayableAmount1;


					}
				}
*/



				if(date2.getMonth() == 11){
					date2 = new Date(date2.getYear()+1,  0, date2.getDate());
				}else {
					date2 = new Date(date2.getYear(), date2.getMonth() + 1, date2.getDate());
				}


				System.out.println("------------------date2----------------"+date2);


				//


			}




















			records.add(new String[]{"Event Start Date", String.valueOf(eventy.fundraisingStart), "", "", "",  ""});
			records.add(new String[]{"Event End Date", String.valueOf(eventy.fundraisingEnd), "", "", "",  ""});
			records.add(new String[]{"SC Fee", String.valueOf(scholasticChallengeFeeAmountPerct)+"%", "", "", "",  ""});
			records.add(new String[]{"Monthly payout ", "75%", "", "", "",  ""});
			records.add(new String[]{"", "", "", "", "",  ""});

			String[] string =  new String[100];
			string[0]="Collections Summary";

















			/********Show Month Between Start and end*********/
			Date date3=null;
			Calendar cal2 = Calendar.getInstance();
			//cal2.setTime();

			System.out.println("------diffMonthBetweenStartAndEndFundraising----"+diffMonthBetweenStartAndEndFundraising);
			for( i = 0 ;i<diffMonthBetweenStartAndEndFundraising;i++){
				date3=new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth(), eventy.fundraisingStart.getDate());
				cal2.setTime(date3);

				Formatter formatter=new Formatter();
				formatter.format("%tb", cal2);
				System.out.println("-----Formatter----"+formatter);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount + "-month", formatter);


				if(date3.getMonth() == 12){
					date3 = new Date(eventy.fundraisingStart.getYear()+1,  1, 5);
				}else {
					date3 = new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth() + 1, 5);
				}

			}




			/*Date date3=null;
			Calendar cal2 = Calendar.getInstance();
			//cal2.setTime();
			for( i = 0 ;i<diffMonthBetweenStartAndEndFundraising;i++){
				date3=new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth(), eventy.fundraisingStart.getDate());
				cal2.setTime(date3);

				Formatter formatter=new Formatter();
				formatter.format("%tb", cal2);

				payOutTimeAndAllAmountsMap.put(firstPayoutCount + "-month", formatter);


				if(date3.getMonth() == 12){
					date3 = new Date(date3.getYear()+1,  1, 1);
				}else {
					date3 = new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth() + 1, 1);
				}

			}
*/












			for(int i1=1;i1<=diffMonthBetweenStartAndEndFundraising;i1++){
				String element=payOutTimeAndAllAmountsMap.get(i1 + "-month").toString();
				string[i1]=element;
			}


/*

			for(int i1=1;i1<=firstPayoutCount;i1++){
				String element=payOutTimeAndAllAmountsMap.get(i1 + "-month").toString();
				string[i1]=element;
			}*/


			records.add(string);
			records.add(new String[]{"", "", "", "", "",  ""});


			String[] totalCheckDonations =  new String[100];
			totalCheckDonations[0] = "Cheque";

			String[]  totalCreditDonations = new String[100];
			totalCreditDonations[0] = "Credit Card";

			String[]  grossIncomeDonations = new String[100];
			grossIncomeDonations[0] = "Gross Income";


			String[] scFee = new String[100];
			scFee[0] = "SC Fee";


			String[] disbursibleAmount = new String[100];
			disbursibleAmount[0] = "Disbursible Amount";



			for(int i1=1;i1<=firstPayoutCount;i1++){
				//i1 payout will be shown
				System.out.println("-----------------------------------i1------------------------------------"+i1);
				System.out.println("totalCreditAmountNo::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAmountNo"));
				System.out.println("totalCheckAmountNo::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAmountNo"));
				System.out.println("totalDonationsAmount::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalDonationsAmount"));
				System.out.println("totalCreditAccNo::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAccNo"));
				System.out.println("totalCheckAccNo::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAccNo"));
				System.out.println("totalDonationsAccNo::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalDonationsAccNo"));
				System.out.println("totalCreditDonationsPerct::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditDonationsPerct"));
				System.out.println("totalCheckDonationsPerct::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckDonationsPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditDonationsAccNoPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckDonationsAccNoPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAmountNoForSponsor"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAmountNoForSponsor"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalSponsorAmount"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAccNoForSponsor"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAccNoForSponsor"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalSponsorsAccNo"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditSponsorsPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckSponsorsPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditSponsorsAccNoPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckSponsorsAccNoPerct"));
				System.out.println("grossIncomeAmount::"+payOutTimeAndAllAmountsMap.get(i1 + "-grossIncomeAmount"));
				System.out.println("netIncomeAmount::"+payOutTimeAndAllAmountsMap.get(i1 + "-netIncomeAmount"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-scholasticChallengeFeeAmount"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-netAmount"));
				double totalCreditAmounts = (double)payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAmountNoForSponsor")+(double)payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAmountNo");
				/*totalCheckDonations[i1] =String.valueOf(Double.parseDouble(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAmountNoForSponsor")+"") + Double.parseDouble(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAccNo")+"")) ;*/


				totalCheckDonations[i1] =String.valueOf(totalCreditAmounts) ;
				double totalChequeAmounts = (double)payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAmountNo")+(double)payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAmountNoForSponsor");
				totalCreditDonations[i1] = String.valueOf(totalChequeAmounts);
				grossIncomeDonations[i1] = String.valueOf((double)payOutTimeAndAllAmountsMap.get(i1 + "-grossIncomeAmount"));
				scFee[i1] = String.valueOf((double)payOutTimeAndAllAmountsMap.get(i1 + "-scholasticChallengeFeeAmount"));
				disbursibleAmount[i1] = String.valueOf((double)payOutTimeAndAllAmountsMap.get(i1 + "-netIncomeAmount"));

				//records.add(new String[]{"Balance Due", " ", "($" + remainingAmount + ")", "100%", "100%", remainingAmountPercent + "%"});


			}

			records.add(totalCheckDonations);
			records.add(totalCreditDonations);
			records.add(grossIncomeDonations);
			records.add(scFee);
			records.add(disbursibleAmount);
			localCalendar = Calendar.getInstance(TimeZone.getDefault());
			currentDayOfMonth = localCalendar.get(Calendar.DAY_OF_MONTH);
			records.add(new String[]{"column1", "payout", "Withheld amount", "", "",  ""});
			if(currentDayOfMonth==5 || currentDayOfMonth>5){
				for(int i1=1;i1<=firstPayoutCount;i1++){
					//i1 payout will be shown

					double paidAmount1 = (double)payOutTimeAndPaidAmountMap.get(i1);
					System.out.println("paidAmount1----"+paidAmount1);
					double remainingPayableAmount1 = (double)payOutTimeAndPayableAmountMap.get(i1);
					//reconciliationAmount
					System.out.println("remainingPayableAmount1890 "+remainingPayableAmount1);

					reconciliationAmount = reconciliationAmount+remainingPayableAmount1;
					System.out.println("reconciliationAmount890 "+reconciliationAmount);



					records.add(new String[]{String.valueOf(i1), String.valueOf((double)payOutTimeAndPaidAmountMap.get(i1)),String.valueOf((double)reconciliationAmount) , "", "",  ""});


				}
			}else{


				if(currentDayOfMonth==eventy.fundraisingStart.getDate()){
					for(int i1=1;i1<=firstPayoutCount-1;i1++){


						double paidAmount1 = (double)payOutTimeAndPaidAmountMap.get(i1);
						System.out.println("paidAmount1----"+paidAmount1);
						double remainingPayableAmount1 = (double)payOutTimeAndPayableAmountMap.get(i1);
						//reconciliationAmount
						System.out.println("remainingPayableAmount2890 "+remainingPayableAmount1);

						reconciliationAmount = reconciliationAmount+remainingPayableAmount1;
						System.out.println("reconciliationAmount3890 "+reconciliationAmount);



						records.add(new String[]{String.valueOf(i1), String.valueOf((double)payOutTimeAndPaidAmountMap.get(i1)),String.valueOf((double)reconciliationAmount) , "", "",  ""});


					}
				}
				else{
					for(int i1=1;i1<=firstPayoutCount;i1++){


						double paidAmount1 = (double)payOutTimeAndPaidAmountMap.get(i1);
						System.out.println("paidAmount115634----"+paidAmount1);

						//i1 payout will be shown

						double remainingPayableAmount1 = (double)payOutTimeAndPayableAmountMap.get(i1);
						//reconciliationAmount


						System.out.println("remainingPayableAmount15634"+remainingPayableAmount1);
						System.out.println("reconciliationAmount5634"+reconciliationAmount);
						reconciliationAmount = reconciliationAmount+remainingPayableAmount1;

						records.add(new String[]{String.valueOf(i1), String.valueOf((double)payOutTimeAndPaidAmountMap.get(i1)),String.valueOf((double)reconciliationAmount) , "", "",  ""});
					}
				}

			}


			if(eventy.fundraisingEnd.getDate()>=5){

				//total logic
				// for donations variable
				double totalCreditAmountNo = 0.0;
				double totalCheckAmountNo = 0.0;
				totalCreditAccNo = 0;
				totalCheckAccNo = 0;
				double totalDonationsAmount = 0.0;
				double totalDonationsAccNo = 0.0;

				//for sponsors variable
				double totalCreditAmountNoForSponsor = 0.0;
				double totalCheckAmountNoForSponsor = 0.0;
				double totalCreditAccNoForSponsor = 0.0;
				double totalCheckAccNoForSponsor = 0.0;
				double totalSponsorAmount = 0.0;
				double totalSponsorsAccNo = 0.0;





				Calendar cal = Calendar.getInstance();
				System.out.println("------------@@@@@@" + eventy.fundraisingStart);

				//Date date
				cal.setTime(eventy.fundraisingEnd);




				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
				Date eachMonthFirstDay = cal.getTime();
				System.out.println("---eachMonthFirstDay7----" + eachMonthFirstDay);
				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
				Date eachMonthLastDay = eventy.fundraisingEnd;
				System.out.println("---eachMonthLastDay7---" + eachMonthLastDay);


				if (donations != null && donations.size() > 0) {


					for (Donation donation : donations) {
						if(donation !=null){
							if ((donation.dateCreated.equals(eachMonthFirstDay) || donation.dateCreated.after(eachMonthFirstDay)) && (donation.dateCreated.before(eachMonthLastDay) || donation.dateCreated.equals(eachMonthLastDay))) {
								//total donations calculation for i where i
								if (donation.donationType != Donation.DonationType.SPONSOR) {
									if (donation.paymentType == Donation.PaymentType.CREDIT) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCreditAmountNo = totalCreditAmountNo + donation.amount;
										//totalCreditAmount = String.valueOf(totalCreditAmountNo);
										totalCreditAccNo = totalCreditAccNo + 1;
										//totalCreditAcc = String.valueOf(totalCreditAccNo);

									} else if (donation.paymentType == Donation.PaymentType.CHECK) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCheckAmountNo = totalCheckAmountNo + donation.amount;
										//totalCheckAmount = String.valueOf(totalCheckAmountNo);
										totalCheckAccNo = totalCheckAccNo + 1;
										//totalCheckAcc = String.valueOf(totalCheckAccNo);

									}
								}
								if (donation.donationType == Donation.DonationType.SPONSOR) {
									if (donation.paymentType == Donation.PaymentType.CREDIT) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCreditAmountNoForSponsor = totalCreditAmountNoForSponsor + donation.amount;
										//totalCreditAmount = String.valueOf(totalCreditAmountNo);
										totalCreditAccNoForSponsor = totalCreditAccNoForSponsor + 1;
										//totalCreditAcc = String.valueOf(totalCreditAccNo);

									} else if (donation.paymentType == Donation.PaymentType.CHECK) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCheckAmountNoForSponsor = totalCheckAmountNoForSponsor + donation.amount;
										//totalCheckAmount = String.valueOf(totalCheckAmountNo);
										totalCheckAccNoForSponsor = totalCheckAccNoForSponsor + 1;
										//totalCheckAcc = String.valueOf(totalCheckAccNo);

									}
								}


							}
						}
					}

				}



				double  withheldAmount=totalCreditAmountNo+totalCheckAmountNo+totalCheckAmountNoForSponsor+totalCreditAmountNoForSponsor;
				withheldAmount=withheldAmount-((withheldAmount*scholasticChallengeFeeAmountPerct)/100);

				records.add(new String[]{"Final Reconciliation ", String.valueOf((double)withheldAmount+(double)reconciliationAmount),"" , "", "",  ""});










			}



			else{


				double totalCreditAmountNo = 0.0;
				double totalCheckAmountNo = 0.0;
				totalCreditAccNo = 0;
				totalCheckAccNo = 0;
				double totalDonationsAmount = 0.0;
				double totalDonationsAccNo = 0.0;

				//for sponsors variable
				double totalCreditAmountNoForSponsor = 0.0;
				double totalCheckAmountNoForSponsor = 0.0;
				double totalCreditAccNoForSponsor = 0.0;
				double totalCheckAccNoForSponsor = 0.0;
				double totalSponsorAmount = 0.0;
				double totalSponsorsAccNo = 0.0;





				Calendar cal = Calendar.getInstance();
				System.out.println("------------@@@@@@" + eventy.fundraisingStart);

				//Date date
				Date startDateCheck=new Date();

				//date2=new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth(), eventy.fundraisingStart.getDate());
				if(eventy.fundraisingEnd.getMonth() == 1){
					startDateCheck = new Date(eventy.fundraisingStart.getYear()-1,  12, 1);
				}else {
					startDateCheck = new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth() - 1, 1);
				}









				cal.setTime(startDateCheck);




				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
				Date eachMonthFirstDay = cal.getTime();
				System.out.println("---eachMonthFirstDay8----" + eachMonthFirstDay);
				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
				Date eachMonthLastDay = eventy.fundraisingEnd;
				System.out.println("---eachMonthLastDay8---" + eachMonthLastDay);


				if (donations != null && donations.size() > 0) {


					for (Donation donation : donations) {
						if(donation !=null){
							if ((donation.dateCreated.equals(eachMonthFirstDay) || donation.dateCreated.after(eachMonthFirstDay)) && (donation.dateCreated.before(eachMonthLastDay) || donation.dateCreated.equals(eachMonthLastDay))) {
								//total donations calculation for i where i
								if (donation.donationType != Donation.DonationType.SPONSOR) {
									if (donation.paymentType == Donation.PaymentType.CREDIT) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCreditAmountNo = totalCreditAmountNo + donation.amount;
										//totalCreditAmount = String.valueOf(totalCreditAmountNo);
										totalCreditAccNo = totalCreditAccNo + 1;
										//totalCreditAcc = String.valueOf(totalCreditAccNo);

									} else if (donation.paymentType == Donation.PaymentType.CHECK) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCheckAmountNo = totalCheckAmountNo + donation.amount;
										//totalCheckAmount = String.valueOf(totalCheckAmountNo);
										totalCheckAccNo = totalCheckAccNo + 1;
										//totalCheckAcc = String.valueOf(totalCheckAccNo);

									}
								}
								if (donation.donationType == Donation.DonationType.SPONSOR) {
									if (donation.paymentType == Donation.PaymentType.CREDIT) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCreditAmountNoForSponsor = totalCreditAmountNoForSponsor + donation.amount;
										//totalCreditAmount = String.valueOf(totalCreditAmountNo);
										totalCreditAccNoForSponsor = totalCreditAccNoForSponsor + 1;
										//totalCreditAcc = String.valueOf(totalCreditAccNo);

									} else if (donation.paymentType == Donation.PaymentType.CHECK) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCheckAmountNoForSponsor = totalCheckAmountNoForSponsor + donation.amount;
										//totalCheckAmount = String.valueOf(totalCheckAmountNo);
										totalCheckAccNoForSponsor = totalCheckAccNoForSponsor + 1;
										//totalCheckAcc = String.valueOf(totalCheckAccNo);

									}
								}


							}
						}
					}

				}



				double  withheldAmount=totalCreditAmountNo+totalCheckAmountNo+totalCheckAmountNoForSponsor+totalCreditAmountNoForSponsor;
				withheldAmount=withheldAmount-((withheldAmount*scholasticChallengeFeeAmountPerct)/100);

				records.add(new String[]{"Final Reconciliation ", String.valueOf((double)withheldAmount+(double)reconciliationAmount),"" , "", "",  ""});















			}





















		}




















		//----rimi-----end----------------21.01.2016-------------------------------------------------//




		//diff between month of current date and fundraising start month


		///int diffMonth = diffYear * 12 + endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);
		return records;
	}
	/******end***********19.01.2016************************************/


	/*******start******27.01.2016*******************************************/


	private static List<String[]> payoutToStringArrayForMonth1(List<Donation> donations, Event eventy){
		List<String[]> records = new ArrayList<String[]>();
		Date currDate = new Date();
		long daysBetweenStartAndCurrDate = currDate.getTime()-eventy.fundraisingStart.getTime();
		Date date1=null;
		Date date2=null;
		//Calendar calendar=Calendar.getInstance(eventy.fundraisingStart.getTime());
		date2=new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth(), eventy.fundraisingStart.getDate());
		if(eventy.fundraisingStart.getMonth() == 11){
			date1 = new Date(eventy.fundraisingStart.getYear()+1,  0, 5);
		}else {
			date1 = new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth() + 1, 5);
		}

		System.out.println("date2----"+date2+"@@@@date1---"+date1+"@@@@currDate@@@"+currDate);




		long difffirstpayout=date1.getTime()-eventy.fundraisingStart.getTime();
		Map<Integer,Double> payout=new HashMap<Integer,Double>();
		Map<Integer,Double> consilation=new HashMap<Integer,Double>();
		int totalCreditAccNo=0;
		String totalCreditAcc=null;
		int totalCheckAccNo=0;
		String totalCheckAcc=null;

		double totalPaymentByCredit=0.0;
		double totalPaymentByCheck=0.0;
		double totalserviceTax=0.0;





		//----rimi-----start----------------21.01.2016-------------------------------------------------//

		double scholasticChallengeFeeAmount = 0.0;
		double scholasticChallengeFeeAmountPerct = eventy.serviceFee;
		if(scholasticChallengeFeeAmountPerct == 0){
			scholasticChallengeFeeAmountPerct = 10.0;
		}
		long daysBetweenStartAndEndFundraising = eventy.fundraisingEnd.getTime()-eventy.fundraisingStart.getTime();
		System.out.println("start day:: "+eventy.fundraisingStart);
		System.out.println("end day:: "+eventy.fundraisingEnd);
		System.out.println ("Days: " + TimeUnit.DAYS.convert(daysBetweenStartAndEndFundraising, TimeUnit.MILLISECONDS));
		int diffDays = (int)TimeUnit.DAYS.convert(daysBetweenStartAndEndFundraising, TimeUnit.MILLISECONDS);
		Calendar localCalendar = Calendar.getInstance(TimeZone.getDefault());
		int currentDayOfMonth = localCalendar.get(Calendar.DAY_OF_MONTH);
		System.out.println("currentDayOfMonth :: "+currentDayOfMonth);

		/***********Add Morning 28.01.16********************/
		int diffMonthBetweenStartAndEndFundraising =0;
		int monthBetweenStartAndCurrDate =0;

		int check1 = (int) diffDays/30;
			float check2=(float) diffDays/30;
		System.out.println("-----------check1--------check2------"+check1+"-----"+check2);


		    if((check2-check1)>0){
				diffMonthBetweenStartAndEndFundraising = ((int) diffDays/30) +1;
			}
            else{
				diffMonthBetweenStartAndEndFundraising = ((int) diffDays/30);
			}
		//int diffMonthBetweenStartAndEndFundraising = (int) diffDays/30;

		System.out.println("------------diffMonthBetweenStartAndEndFundraising-----------"+diffMonthBetweenStartAndEndFundraising);




		Date currDate1 = new Date();
		long daysBetweenStartAndCurrDate1 = currDate1.getTime()-eventy.fundraisingStart.getTime();
		int diffDaysBetweenStartAndCurrDate1 = (int)TimeUnit.DAYS.convert(daysBetweenStartAndCurrDate1, TimeUnit.MILLISECONDS);



		check1 = (int) diffDaysBetweenStartAndCurrDate1/30;
		check2=(float) diffDaysBetweenStartAndCurrDate1/30;
		if((check2-check1)>0){
			monthBetweenStartAndCurrDate = ((int) diffDaysBetweenStartAndCurrDate1/30) +1;
		}
		else{
			monthBetweenStartAndCurrDate = ((int) diffDaysBetweenStartAndCurrDate1/30);
		}

		System.out.println("------------monthBetweenStartAndCurrDate-----------"+monthBetweenStartAndCurrDate);
		/***********Add Morning 28.01.16********************/



		//int monthBetweenStartAndCurrDate = diffDaysBetweenStartAndCurrDate1/30;





		date1 = new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth() + 1, 5);
		//int diffMonthBetweenStartAndEndFundraising


		LocalDate start = new LocalDate(eventy.fundraisingStart.getTime());
		//LocalDate start = new LocalDate(firstDate.getTime());
		LocalDate end = new LocalDate(currDate1.getTime());

		//monthBetweenStartAndCurrDate =Months.monthsBetween(start, end).getMonths();






		//Date startDate = new Date("2015-02-14");


	/*	int months  = (firstDate.getTime().get(Calendar.YEAR) - secondDate.get(Calendar.YEAR)) * 12 +
				(firstDate.get(Calendar.MONTH)- secondDate.get(Calendar.MONTH)) +
				(firstDate.get(Calendar.DAY_OF_MONTH) >= secondDate.get(Calendar.DAY_OF_MONTH)? 0: -1);*/


		System.out.println("-----------------------Diff months is-------------------" + monthBetweenStartAndCurrDate);

		System.out.println();


//Calculate Difference between month---







		//Test


		System.out.println();

		int i = 0;
		int firstPayoutCount = 0;
		double reconciliationAmount = 0.0;
		HashMap payOutTimeAndPaidAmountMap = new HashMap();
		HashMap payOutTimeAndAllAmountsMap = new HashMap();
		HashMap payOutTimeAndPayableAmountMap = new HashMap();
		if(monthBetweenStartAndCurrDate<diffMonthBetweenStartAndEndFundraising){
			for( i = 0 ;i<monthBetweenStartAndCurrDate;i++) {

				System.out.println("--------------------------------------i---------------------" + i);

				Date dateStartFundraising = eventy.fundraisingStart;


				//fundraising start date+ i no of month
				Calendar calendar = Calendar.getInstance();


				//java.util.Date date= new Date();
				Calendar cal = Calendar.getInstance();
				System.out.println("------------@@@@@@" + eventy.fundraisingStart);

				//Date date
				cal.setTime(date2);
				//int month = eventy.fundraisingStart.getMonth();

				//System.out.println("---hjhhh-----"+month);
				//cal.add(month, i);
				//System.out.println("----------Jan--------"+cal.MONTH);
				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
				Date eachMonthFirstDay = cal.getTime();
				System.out.println("---eachMonthFirstDay9----" + eachMonthFirstDay);
				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
				Date eachMonthLastDay = cal.getTime();
				System.out.println("---eachMonthLastDay9---" + eachMonthLastDay);


				//current day
				Date currdate = new Date();


				//total logic
				// for donations variable
				double totalCreditAmountNo = 0.0;
				double totalCheckAmountNo = 0.0;
				totalCreditAccNo = 0;
				totalCheckAccNo = 0;
				double totalDonationsAmount = 0.0;
				double totalDonationsAccNo = 0.0;

				//for sponsors variable
				double totalCreditAmountNoForSponsor = 0.0;
				double totalCheckAmountNoForSponsor = 0.0;
				double totalCreditAccNoForSponsor = 0.0;
				double totalCheckAccNoForSponsor = 0.0;
				double totalSponsorAmount = 0.0;
				double totalSponsorsAccNo = 0.0;


				if (donations != null && donations.size() > 0) {


					for (Donation donation : donations) {
						if(donation !=null){
							if ((donation.dateCreated.equals(eachMonthFirstDay) || donation.dateCreated.after(eachMonthFirstDay)) && (donation.dateCreated.before(eachMonthLastDay) || donation.dateCreated.equals(eachMonthLastDay))) {
								//total donations calculation for i where i
								if (donation.donationType != Donation.DonationType.SPONSOR) {
									if (donation.paymentType == Donation.PaymentType.CREDIT) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCreditAmountNo = totalCreditAmountNo + donation.amount;
										//totalCreditAmount = String.valueOf(totalCreditAmountNo);
										totalCreditAccNo = totalCreditAccNo + 1;
										//totalCreditAcc = String.valueOf(totalCreditAccNo);

									} else if (donation.paymentType == Donation.PaymentType.CHECK) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCheckAmountNo = totalCheckAmountNo + donation.amount;
										//totalCheckAmount = String.valueOf(totalCheckAmountNo);
										totalCheckAccNo = totalCheckAccNo + 1;
										//totalCheckAcc = String.valueOf(totalCheckAccNo);

									}
								}
								if (donation.donationType == Donation.DonationType.SPONSOR) {
									if (donation.paymentType == Donation.PaymentType.CREDIT) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCreditAmountNoForSponsor = totalCreditAmountNoForSponsor + donation.amount;
										//totalCreditAmount = String.valueOf(totalCreditAmountNo);
										totalCreditAccNoForSponsor = totalCreditAccNoForSponsor + 1;
										//totalCreditAcc = String.valueOf(totalCreditAccNo);

									} else if (donation.paymentType == Donation.PaymentType.CHECK) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCheckAmountNoForSponsor = totalCheckAmountNoForSponsor + donation.amount;
										//totalCheckAmount = String.valueOf(totalCheckAmountNo);
										totalCheckAccNoForSponsor = totalCheckAccNoForSponsor + 1;
										//totalCheckAcc = String.valueOf(totalCheckAccNo);

									}
								}


							}
						}
					}

				}

				//calculations of donations
				totalDonationsAmount = totalCreditAmountNo+totalCheckAmountNo;
				System.out.println("----totalDonationsAmount---"+totalDonationsAmount);
				totalDonationsAccNo = totalCreditAccNo+totalCheckAccNo;
				System.out.println("----totalDonationsAccNo---"+totalDonationsAccNo);
				double totalCreditDonationsPerct = (totalCreditAmountNo/totalDonationsAmount)*100;
				double totalCheckDonationsPerct = (totalCheckAmountNo/totalDonationsAmount)*100;
				double totalCreditDonationsAccNoPerct = (totalCreditAccNo/totalDonationsAccNo)*100;
				double totalCheckDonationsAccNoPerct = (totalCheckAccNo/totalDonationsAccNo)*100;


				//total sponsorship calculations
				totalSponsorAmount = totalCreditAmountNoForSponsor+totalCheckAmountNoForSponsor;
				totalSponsorsAccNo = totalCreditAccNoForSponsor+totalCheckAccNoForSponsor;
				double totalCreditSponsorsPerct = (totalCreditAmountNoForSponsor/totalSponsorAmount)*100;
				double totalCheckSponsorsPerct = (totalCheckAmountNoForSponsor/totalSponsorAmount)*100;
				double totalCreditSponsorsAccNoPerct = (totalCreditAccNoForSponsor/totalSponsorsAccNo)*100;
				double totalCheckSponsorsAccNoPerct = (totalCheckAccNoForSponsor/totalSponsorsAccNo)*100;

				//gross income
				double grossIncomeAmount = totalDonationsAmount + totalSponsorAmount;

				//scholastic challenge fee
				scholasticChallengeFeeAmount = 0.0;

				/*double scholasticChallengeFeeAmountPerct = eventy.serviceFee;
				if(scholasticChallengeFeeAmountPerct == 0){
					scholasticChallengeFeeAmountPerct = 10.0;
				}*/

				scholasticChallengeFeeAmount = grossIncomeAmount*scholasticChallengeFeeAmountPerct/100;



				//net income

				double netIncomeAmount = grossIncomeAmount-scholasticChallengeFeeAmount;

				//first payout

				double remainingPayableAmount = 0.0;
				double paidoutAmount = 0.0;
				firstPayoutCount = firstPayoutCount + 1;
				//paid amout percentage
				double paidAmountPercent = 75;

				paidoutAmount = (netIncomeAmount*75)/100;
				remainingPayableAmount = netIncomeAmount - paidoutAmount;





				//Commend on 27.01.16--------
			/*	Formatter formatter=new Formatter();
				formatter.format("%tb",cal);

				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-month",formatter);*/






				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditAmountNo",totalCreditAmountNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckAmountNo",totalCheckAmountNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalDonationsAmount",totalDonationsAmount);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditAccNo",totalCreditAccNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckAccNo",totalCheckAccNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalDonationsAccNo",totalDonationsAccNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditDonationsPerct",totalCreditDonationsPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckDonationsPerct",totalCheckDonationsPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditDonationsAccNoPerct",totalCreditDonationsAccNoPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckDonationsAccNoPerct",totalCheckDonationsAccNoPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditAmountNoForSponsor",totalCreditAmountNoForSponsor);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckAmountNoForSponsor",totalCheckAmountNoForSponsor);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalSponsorAmount",totalSponsorAmount);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditAccNoForSponsor",totalCreditAccNoForSponsor);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckAccNoForSponsor",totalCheckAccNoForSponsor);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalSponsorsAccNo",totalSponsorsAccNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditSponsorsPerct",totalCreditSponsorsPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckSponsorsPerct",totalCheckSponsorsPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditSponsorsAccNoPerct",totalCreditSponsorsAccNoPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckSponsorsAccNoPerct",totalCheckSponsorsAccNoPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-grossIncomeAmount",grossIncomeAmount);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-netIncomeAmount",netIncomeAmount);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-scholasticChallengeFeeAmount",scholasticChallengeFeeAmount);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-netAmount",netIncomeAmount);
				payOutTimeAndPaidAmountMap.put(firstPayoutCount,paidoutAmount);
				payOutTimeAndPayableAmountMap.put(firstPayoutCount,remainingPayableAmount);


				/*localCalendar = Calendar.getInstance(TimeZone.getDefault());
				currentDayOfMonth = localCalendar.get(Calendar.DAY_OF_MONTH);
				if(currentDayOfMonth==5 || currentDayOfMonth>5){
					for(int i1=1;i1<=firstPayoutCount;i1++){
						//i1 payout will be shown

						double remainingPayableAmount1 = (double)payOutTimeAndPaidAmountMap.get(i1);
						//reconciliationAmount
						reconciliationAmount = reconciliationAmount+remainingPayableAmount1;


					}
				}else{
					for(int i1=1;i1<=firstPayoutCount-1;i1++){
						//i1 payout will be shown

						double remainingPayableAmount1 = (double)payOutTimeAndPaidAmountMap.get(i1);
						//reconciliationAmount
						reconciliationAmount = reconciliationAmount+remainingPayableAmount1;


					}
				}
*/



				if(date2.getMonth() == 11){
					date2 = new Date(date2.getYear()+1,  0, date2.getDate());
				}else {
					date2 = new Date(date2.getYear(), date2.getMonth() + 1, date2.getDate());
				}


				System.out.println("------------------date2----------------"+date2);


				//


			}






			records.add(new String[]{"Event Start Date", String.valueOf(eventy.fundraisingStart), "", "", "",  ""});
			records.add(new String[]{"Event End Date", String.valueOf(eventy.fundraisingEnd), "", "", "",  ""});
			records.add(new String[]{"SC Fee", String.valueOf(scholasticChallengeFeeAmountPerct)+"%", "", "", "",  ""});
			records.add(new String[]{"Monthly payout ", "75%", "", "", "",  ""});
			records.add(new String[]{"", "", "", "", "",  ""});

			String[] string =  new String[100];
			string[0]="Collections Summary";








			/********Show Month Between Start and end*********/
			Date date3=null;
			Calendar cal2 = Calendar.getInstance();
			//cal2.setTime();
			date3=new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth(), eventy.fundraisingStart.getDate());
			for( i = 0 ;i<monthBetweenStartAndCurrDate;i++){

				cal2.setTime(date3);

				Formatter formatter=new Formatter();
				formatter.format("%tb", cal2);

				payOutTimeAndAllAmountsMap.put(i + "-month", formatter);


				if(date3.getMonth() == 11){
					/*date3 = new Date(eventy.fundraisingStart.getYear()+1,  0, 5);*/
					date3 = new Date(date3.getYear()+1,  0, 5);
				}else {
					date3 = new Date(date3.getYear(), date3.getMonth() + 1, 5);
				}

			}




			for(int i1=0;i1<monthBetweenStartAndCurrDate;i1++){
				String element=payOutTimeAndAllAmountsMap.get(i1 + "-month").toString();
				string[i1+1]=element;
			}



			records.add(string);
			records.add(new String[]{"", "", "", "", "",  ""});


			String[] totalCheckDonations =  new String[100];
			totalCheckDonations[0] = "Cheque";

			String[]  totalCreditDonations = new String[100];
			totalCreditDonations[0] = "Credit Card";

			String[]  grossIncomeDonations = new String[100];
			grossIncomeDonations[0] = "Gross Income";


			String[] scFee = new String[100];
			scFee[0] = "SC Fee";


			String[] disbursibleAmount = new String[100];
			disbursibleAmount[0] = "Disbursible Amount";



			for(int i1=1;i1<=firstPayoutCount;i1++){
				//i1 payout will be shown
				System.out.println("-----------------------------------i1------------------------------------"+i1);
				System.out.println("totalCreditAmountNo::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAmountNo"));
				System.out.println("totalCheckAmountNo::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAmountNo"));
				System.out.println("totalDonationsAmount::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalDonationsAmount"));
				System.out.println("totalCreditAccNo::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAccNo"));
				System.out.println("totalCheckAccNo::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAccNo"));
				System.out.println("totalDonationsAccNo::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalDonationsAccNo"));
				System.out.println("totalCreditDonationsPerct::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditDonationsPerct"));
				System.out.println("totalCheckDonationsPerct::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckDonationsPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditDonationsAccNoPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckDonationsAccNoPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAmountNoForSponsor"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAmountNoForSponsor"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalSponsorAmount"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAccNoForSponsor"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAccNoForSponsor"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalSponsorsAccNo"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditSponsorsPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckSponsorsPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditSponsorsAccNoPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckSponsorsAccNoPerct"));
				System.out.println("grossIncomeAmount::"+payOutTimeAndAllAmountsMap.get(i1 + "-grossIncomeAmount"));
				System.out.println("netIncomeAmount::"+payOutTimeAndAllAmountsMap.get(i1 + "-netIncomeAmount"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-scholasticChallengeFeeAmount"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-netAmount"));
				double totalCreditAmounts = (double)payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAmountNoForSponsor")+(double)payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAmountNo");
				/*totalCheckDonations[i1] =String.valueOf(Double.parseDouble(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAmountNoForSponsor")+"") + Double.parseDouble(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAccNo")+"")) ;*/


				totalCheckDonations[i1] =String.valueOf(totalCreditAmounts) ;
				double totalChequeAmounts = (double)payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAmountNo")+(double)payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAmountNoForSponsor");
				totalCreditDonations[i1] = String.valueOf(totalChequeAmounts);
				grossIncomeDonations[i1] = String.valueOf((double)payOutTimeAndAllAmountsMap.get(i1 + "-grossIncomeAmount"));
				scFee[i1] = String.valueOf((double)payOutTimeAndAllAmountsMap.get(i1 + "-scholasticChallengeFeeAmount"));
				disbursibleAmount[i1] = String.valueOf((double)payOutTimeAndAllAmountsMap.get(i1 + "-netIncomeAmount"));

				//records.add(new String[]{"Balance Due", " ", "($" + remainingAmount + ")", "100%", "100%", remainingAmountPercent + "%"});


			}

			records.add(totalCheckDonations);
			records.add(totalCreditDonations);
			records.add(grossIncomeDonations);
			records.add(scFee);
			records.add(disbursibleAmount);
			localCalendar = Calendar.getInstance(TimeZone.getDefault());
			currentDayOfMonth = localCalendar.get(Calendar.DAY_OF_MONTH);
			records.add(new String[]{"column1", "payout", "Withheld amount", "", "",  ""});
			if(currentDayOfMonth==5 || currentDayOfMonth>5){
				for(int i1=1;i1<firstPayoutCount;i1++){
					//i1 payout will be shown

					double paidAmount1 = (double)payOutTimeAndPaidAmountMap.get(i1);
					System.out.println("paidAmount1----"+paidAmount1);
					double remainingPayableAmount1 = (double)payOutTimeAndPayableAmountMap.get(i1);
					//reconciliationAmount
					System.out.println("remainingPayableAmount1890 "+remainingPayableAmount1);

					reconciliationAmount = reconciliationAmount+remainingPayableAmount1;
					System.out.println("reconciliationAmount890 "+reconciliationAmount);

                //   double

					records.add(new String[]{String.valueOf(i1), String.valueOf((double)payOutTimeAndPaidAmountMap.get(i1)),String.valueOf((double)remainingPayableAmount1) , "", "",  ""});


				}
			}else{


				if(currentDayOfMonth==eventy.fundraisingStart.getDate()){
					for(int i1=1;i1<=firstPayoutCount-1;i1++){


						double paidAmount1 = (double)payOutTimeAndPaidAmountMap.get(i1);
						System.out.println("paidAmount1----"+paidAmount1);
						double remainingPayableAmount1 = (double)payOutTimeAndPayableAmountMap.get(i1);
						//reconciliationAmount
						System.out.println("remainingPayableAmount2890 "+remainingPayableAmount1);

						reconciliationAmount = reconciliationAmount+remainingPayableAmount1;
						System.out.println("reconciliationAmount3890 "+reconciliationAmount);



						records.add(new String[]{String.valueOf(i1), String.valueOf((double)payOutTimeAndPaidAmountMap.get(i1)),String.valueOf((double)remainingPayableAmount1) , "", "",  ""});


					}
				}
				else{
					for(int i1=1;i1<=firstPayoutCount;i1++){


						double paidAmount1 = (double)payOutTimeAndPaidAmountMap.get(i1);
						System.out.println("paidAmount115634----"+paidAmount1);

						//i1 payout will be shown

						double remainingPayableAmount1 = (double)payOutTimeAndPayableAmountMap.get(i1);
						//reconciliationAmount


						System.out.println("remainingPayableAmount15634"+remainingPayableAmount1);
						System.out.println("reconciliationAmount5634"+reconciliationAmount);
						reconciliationAmount = reconciliationAmount+remainingPayableAmount1;

						records.add(new String[]{String.valueOf(i1), String.valueOf((double)payOutTimeAndPaidAmountMap.get(i1)),String.valueOf((double)remainingPayableAmount1) , "", "",  ""});
					}
				}

			}

			//If the current Date greater than 5th date
			if(currDate.getDate()>=5){

				//total logic
				// for donations variable
				double totalCreditAmountNo = 0.0;
				double totalCheckAmountNo = 0.0;
				totalCreditAccNo = 0;
				totalCheckAccNo = 0;
				double totalDonationsAmount = 0.0;
				double totalDonationsAccNo = 0.0;

				//for sponsors variable
				double totalCreditAmountNoForSponsor = 0.0;
				double totalCheckAmountNoForSponsor = 0.0;
				double totalCreditAccNoForSponsor = 0.0;
				double totalCheckAccNoForSponsor = 0.0;
				double totalSponsorAmount = 0.0;
				double totalSponsorsAccNo = 0.0;





				Calendar cal = Calendar.getInstance();
				System.out.println("------------@@@@@@" + eventy.fundraisingStart);

				//Date date
				cal.setTime(currDate);




				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
				Date eachMonthFirstDay = cal.getTime();
				System.out.println("---eachMonthFirstDay10----" + eachMonthFirstDay);
				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
				Date eachMonthLastDay = currDate;
				System.out.println("---eachMonthLastDay10---" + eachMonthLastDay);


				if (donations != null && donations.size() > 0) {


					for (Donation donation : donations) {
						if(donation !=null){
							if ((donation.dateCreated.equals(eachMonthFirstDay) || donation.dateCreated.after(eachMonthFirstDay)) && (donation.dateCreated.before(eachMonthLastDay) || donation.dateCreated.equals(eachMonthLastDay))) {
								//total donations calculation for i where i
								if (donation.donationType != Donation.DonationType.SPONSOR) {
									if (donation.paymentType == Donation.PaymentType.CREDIT) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCreditAmountNo = totalCreditAmountNo + donation.amount;
										//totalCreditAmount = String.valueOf(totalCreditAmountNo);
										totalCreditAccNo = totalCreditAccNo + 1;
										//totalCreditAcc = String.valueOf(totalCreditAccNo);

									} else if (donation.paymentType == Donation.PaymentType.CHECK) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCheckAmountNo = totalCheckAmountNo + donation.amount;
										//totalCheckAmount = String.valueOf(totalCheckAmountNo);
										totalCheckAccNo = totalCheckAccNo + 1;
										//totalCheckAcc = String.valueOf(totalCheckAccNo);

									}
								}
								if (donation.donationType == Donation.DonationType.SPONSOR) {
									if (donation.paymentType == Donation.PaymentType.CREDIT) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCreditAmountNoForSponsor = totalCreditAmountNoForSponsor + donation.amount;
										//totalCreditAmount = String.valueOf(totalCreditAmountNo);
										totalCreditAccNoForSponsor = totalCreditAccNoForSponsor + 1;
										//totalCreditAcc = String.valueOf(totalCreditAccNo);

									} else if (donation.paymentType == Donation.PaymentType.CHECK) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCheckAmountNoForSponsor = totalCheckAmountNoForSponsor + donation.amount;
										//totalCheckAmount = String.valueOf(totalCheckAmountNo);
										totalCheckAccNoForSponsor = totalCheckAccNoForSponsor + 1;
										//totalCheckAcc = String.valueOf(totalCheckAccNo);

									}
								}


							}
						}
					}

				}



				double  withheldAmount=totalCreditAmountNo+totalCheckAmountNo+totalCheckAmountNoForSponsor+totalCreditAmountNoForSponsor;
				withheldAmount=withheldAmount-((withheldAmount*scholasticChallengeFeeAmountPerct)/100);

				records.add(new String[]{"Final Remaining Amount", "",String.valueOf((double)withheldAmount) , "", "",  ""});





			}



			else{


				double totalCreditAmountNo = 0.0;
				double totalCheckAmountNo = 0.0;
				totalCreditAccNo = 0;
				totalCheckAccNo = 0;
				double totalDonationsAmount = 0.0;
				double totalDonationsAccNo = 0.0;

				//for sponsors variable
				double totalCreditAmountNoForSponsor = 0.0;
				double totalCheckAmountNoForSponsor = 0.0;
				double totalCreditAccNoForSponsor = 0.0;
				double totalCheckAccNoForSponsor = 0.0;
				double totalSponsorAmount = 0.0;
				double totalSponsorsAccNo = 0.0;





				Calendar cal = Calendar.getInstance();
				System.out.println("------------@@@@@@" + eventy.fundraisingStart);

				//Date date
				Date startDateCheck=new Date();

				//date2=new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth(), eventy.fundraisingStart.getDate());
				if(currDate.getMonth() == 1){
					startDateCheck = new Date(eventy.fundraisingStart.getYear()-1,  12, 1);
				}else {
					startDateCheck = new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth() - 1, 1);
				}









				cal.setTime(startDateCheck);




				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
				Date eachMonthFirstDay = cal.getTime();
				System.out.println("---eachMonthFirstDay11----" + eachMonthFirstDay);
				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
				Date eachMonthLastDay = currDate;
				System.out.println("---eachMonthLastDay11---" + eachMonthLastDay);


				if (donations != null && donations.size() > 0) {


					for (Donation donation : donations) {
						if(donation !=null){
							if ((donation.dateCreated.equals(eachMonthFirstDay) || donation.dateCreated.after(eachMonthFirstDay)) && (donation.dateCreated.before(eachMonthLastDay) || donation.dateCreated.equals(eachMonthLastDay))) {
								//total donations calculation for i where i
								if (donation.donationType != Donation.DonationType.SPONSOR) {
									if (donation.paymentType == Donation.PaymentType.CREDIT) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCreditAmountNo = totalCreditAmountNo + donation.amount;
										//totalCreditAmount = String.valueOf(totalCreditAmountNo);
										totalCreditAccNo = totalCreditAccNo + 1;
										//totalCreditAcc = String.valueOf(totalCreditAccNo);

									} else if (donation.paymentType == Donation.PaymentType.CHECK) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCheckAmountNo = totalCheckAmountNo + donation.amount;
										//totalCheckAmount = String.valueOf(totalCheckAmountNo);
										totalCheckAccNo = totalCheckAccNo + 1;
										//totalCheckAcc = String.valueOf(totalCheckAccNo);

									}
								}
								if (donation.donationType == Donation.DonationType.SPONSOR) {
									if (donation.paymentType == Donation.PaymentType.CREDIT) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCreditAmountNoForSponsor = totalCreditAmountNoForSponsor + donation.amount;
										//totalCreditAmount = String.valueOf(totalCreditAmountNo);
										totalCreditAccNoForSponsor = totalCreditAccNoForSponsor + 1;
										//totalCreditAcc = String.valueOf(totalCreditAccNo);

									} else if (donation.paymentType == Donation.PaymentType.CHECK) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCheckAmountNoForSponsor = totalCheckAmountNoForSponsor + donation.amount;
										//totalCheckAmount = String.valueOf(totalCheckAmountNo);
										totalCheckAccNoForSponsor = totalCheckAccNoForSponsor + 1;
										//totalCheckAcc = String.valueOf(totalCheckAccNo);

									}
								}


							}
						}
					}

				}



				double  withheldAmount=totalCreditAmountNo+totalCheckAmountNo+totalCheckAmountNoForSponsor+totalCreditAmountNoForSponsor;
				withheldAmount=withheldAmount-((withheldAmount*scholasticChallengeFeeAmountPerct)/100);

				records.add(new String[]{"Final Remaining Amount", "",String.valueOf((double)withheldAmount) , "", "",  ""});












			}










		}



		//If the Fundraising End date before current date-------------------------














		else{
			System.out.println("diffMonthBetweenStartAndEndFundraising :: "+diffMonthBetweenStartAndEndFundraising);
			for( i = 0 ;i<diffMonthBetweenStartAndEndFundraising;i++) {

				System.out.println("--------------------------------------i---------------------" + i);

				Date dateStartFundraising = eventy.fundraisingStart;


				//fundraising start date+ i no of month
				Calendar calendar = Calendar.getInstance();


				//java.util.Date date= new Date();
				Calendar cal = Calendar.getInstance();
				System.out.println("------------@@@@@@" + eventy.fundraisingStart);

				//Date date
				cal.setTime(date2);
				//int month = eventy.fundraisingStart.getMonth();

				//System.out.println("---hjhhh-----"+month);
				//cal.add(month, i);
				//System.out.println("----------Jan--------"+cal.MONTH);
				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
				Date eachMonthFirstDay = cal.getTime();
				System.out.println("---eachMonthFirstDay12----" + eachMonthFirstDay);
				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
				Date eachMonthLastDay = cal.getTime();
				System.out.println("---eachMonthLastDay12---" + eachMonthLastDay);


				//current day
				Date currdate = new Date();


				//total logic
				// for donations variable
				double totalCreditAmountNo = 0.0;
				double totalCheckAmountNo = 0.0;
				totalCreditAccNo = 0;
				totalCheckAccNo = 0;
				double totalDonationsAmount = 0.0;
				double totalDonationsAccNo = 0.0;

				//for sponsors variable
				double totalCreditAmountNoForSponsor = 0.0;
				double totalCheckAmountNoForSponsor = 0.0;
				double totalCreditAccNoForSponsor = 0.0;
				double totalCheckAccNoForSponsor = 0.0;
				double totalSponsorAmount = 0.0;
				double totalSponsorsAccNo = 0.0;


				if (donations != null && donations.size() > 0) {


					for (Donation donation : donations) {
						if(donation !=null){
							if ((donation.dateCreated.equals(eachMonthFirstDay) || donation.dateCreated.after(eachMonthFirstDay)) && (donation.dateCreated.before(eachMonthLastDay) || donation.dateCreated.equals(eachMonthLastDay))) {
								//total donations calculation for i where i
								if (donation.donationType != Donation.DonationType.SPONSOR) {
									if (donation.paymentType == Donation.PaymentType.CREDIT) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCreditAmountNo = totalCreditAmountNo + donation.amount;
										//totalCreditAmount = String.valueOf(totalCreditAmountNo);
										totalCreditAccNo = totalCreditAccNo + 1;
										//totalCreditAcc = String.valueOf(totalCreditAccNo);

									} else if (donation.paymentType == Donation.PaymentType.CHECK) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCheckAmountNo = totalCheckAmountNo + donation.amount;
										//totalCheckAmount = String.valueOf(totalCheckAmountNo);
										totalCheckAccNo = totalCheckAccNo + 1;
										//totalCheckAcc = String.valueOf(totalCheckAccNo);

									}
								}
								if (donation.donationType == Donation.DonationType.SPONSOR) {
									if (donation.paymentType == Donation.PaymentType.CREDIT) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCreditAmountNoForSponsor = totalCreditAmountNoForSponsor + donation.amount;
										//totalCreditAmount = String.valueOf(totalCreditAmountNo);
										totalCreditAccNoForSponsor = totalCreditAccNoForSponsor + 1;
										//totalCreditAcc = String.valueOf(totalCreditAccNo);

									} else if (donation.paymentType == Donation.PaymentType.CHECK) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCheckAmountNoForSponsor = totalCheckAmountNoForSponsor + donation.amount;
										//totalCheckAmount = String.valueOf(totalCheckAmountNo);
										totalCheckAccNoForSponsor = totalCheckAccNoForSponsor + 1;
										//totalCheckAcc = String.valueOf(totalCheckAccNo);

									}
								}


							}
						}
					}

				}

				//calculations of donations
				totalDonationsAmount = totalCreditAmountNo+totalCheckAmountNo;
				System.out.println("----totalDonationsAmount---"+totalDonationsAmount);
				totalDonationsAccNo = totalCreditAccNo+totalCheckAccNo;
				System.out.println("----totalDonationsAccNo---"+totalDonationsAccNo);
				double totalCreditDonationsPerct = (totalCreditAmountNo/totalDonationsAmount)*100;
				double totalCheckDonationsPerct = (totalCheckAmountNo/totalDonationsAmount)*100;
				double totalCreditDonationsAccNoPerct = (totalCreditAccNo/totalDonationsAccNo)*100;
				double totalCheckDonationsAccNoPerct = (totalCheckAccNo/totalDonationsAccNo)*100;


				//total sponsorship calculations
				totalSponsorAmount = totalCreditAmountNoForSponsor+totalCheckAmountNoForSponsor;
				totalSponsorsAccNo = totalCreditAccNoForSponsor+totalCheckAccNoForSponsor;
				double totalCreditSponsorsPerct = (totalCreditAmountNoForSponsor/totalSponsorAmount)*100;
				double totalCheckSponsorsPerct = (totalCheckAmountNoForSponsor/totalSponsorAmount)*100;
				double totalCreditSponsorsAccNoPerct = (totalCreditAccNoForSponsor/totalSponsorsAccNo)*100;
				double totalCheckSponsorsAccNoPerct = (totalCheckAccNoForSponsor/totalSponsorsAccNo)*100;

				//gross income
				double grossIncomeAmount = totalDonationsAmount + totalSponsorAmount;

				//scholastic challenge fee
				scholasticChallengeFeeAmount = 0.0;

				/*double scholasticChallengeFeeAmountPerct = eventy.serviceFee;
				if(scholasticChallengeFeeAmountPerct == 0){
					scholasticChallengeFeeAmountPerct = 10.0;
				}*/

				scholasticChallengeFeeAmount = grossIncomeAmount*scholasticChallengeFeeAmountPerct/100;



				//net income

				double netIncomeAmount = grossIncomeAmount-scholasticChallengeFeeAmount;

				//first payout

				double remainingPayableAmount = 0.0;
				double paidoutAmount = 0.0;
				firstPayoutCount = firstPayoutCount + 1;
				//paid amout percentage
				double paidAmountPercent = 75;

				paidoutAmount = (netIncomeAmount*75)/100;
				remainingPayableAmount = netIncomeAmount - paidoutAmount;



				//Commend on 27.01.16---------

		/*		Formatter formatter=new Formatter();
				formatter.format("%tb",cal);

				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-month",formatter);
				*/





				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditAmountNo",totalCreditAmountNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckAmountNo",totalCheckAmountNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalDonationsAmount",totalDonationsAmount);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditAccNo",totalCreditAccNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckAccNo",totalCheckAccNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalDonationsAccNo",totalDonationsAccNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditDonationsPerct",totalCreditDonationsPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckDonationsPerct",totalCheckDonationsPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditDonationsAccNoPerct",totalCreditDonationsAccNoPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckDonationsAccNoPerct",totalCheckDonationsAccNoPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditAmountNoForSponsor",totalCreditAmountNoForSponsor);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckAmountNoForSponsor",totalCheckAmountNoForSponsor);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalSponsorAmount",totalSponsorAmount);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditAccNoForSponsor",totalCreditAccNoForSponsor);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckAccNoForSponsor",totalCheckAccNoForSponsor);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalSponsorsAccNo",totalSponsorsAccNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditSponsorsPerct",totalCreditSponsorsPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckSponsorsPerct",totalCheckSponsorsPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditSponsorsAccNoPerct",totalCreditSponsorsAccNoPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckSponsorsAccNoPerct",totalCheckSponsorsAccNoPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-grossIncomeAmount",grossIncomeAmount);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-netIncomeAmount",netIncomeAmount);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-scholasticChallengeFeeAmount",scholasticChallengeFeeAmount);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-netAmount",netIncomeAmount);
				payOutTimeAndPaidAmountMap.put(firstPayoutCount,paidoutAmount);
				payOutTimeAndPayableAmountMap.put(firstPayoutCount,remainingPayableAmount);


				/*localCalendar = Calendar.getInstance(TimeZone.getDefault());
				currentDayOfMonth = localCalendar.get(Calendar.DAY_OF_MONTH);
				if(currentDayOfMonth==5 || currentDayOfMonth>5){
					for(int i1=1;i1<=firstPayoutCount;i1++){
						//i1 payout will be shown

						double remainingPayableAmount1 = (double)payOutTimeAndPaidAmountMap.get(i1);
						//reconciliationAmount
						reconciliationAmount = reconciliationAmount+remainingPayableAmount1;


					}
				}else{
					for(int i1=1;i1<=firstPayoutCount-1;i1++){
						//i1 payout will be shown

						double remainingPayableAmount1 = (double)payOutTimeAndPaidAmountMap.get(i1);
						//reconciliationAmount
						reconciliationAmount = reconciliationAmount+remainingPayableAmount1;


					}
				}
*/



				if(date2.getMonth() == 11){
					date2 = new Date(date2.getYear()+1,  0, date2.getDate());
				}else {
					date2 = new Date(date2.getYear(), date2.getMonth() + 1, date2.getDate());
				}


				System.out.println("------------------date2----------------"+date2);


				//


			}




















			records.add(new String[]{"Event Start Date", String.valueOf(eventy.fundraisingStart), "", "", "",  ""});
			records.add(new String[]{"Event End Date", String.valueOf(eventy.fundraisingEnd), "", "", "",  ""});
			records.add(new String[]{"SC Fee", String.valueOf(scholasticChallengeFeeAmountPerct)+"%", "", "", "",  ""});
			records.add(new String[]{"Monthly payout ", "75%", "", "", "",  ""});
			records.add(new String[]{"", "", "", "", "",  ""});

			String[] string =  new String[100];
			string[0]="Collections Summary";

















			/********Show Month Between Start and end*********/
			Date date3=null;
			Calendar cal2 = Calendar.getInstance();
			//cal2.setTime();

			System.out.println("------diffMonthBetweenStartAndEndFundraising----"+diffMonthBetweenStartAndEndFundraising);
			date3=new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth(), eventy.fundraisingStart.getDate());
			for( i = 0 ;i<diffMonthBetweenStartAndEndFundraising;i++){
            /*****************start**************************28.01.2016***************/
				//date3 = new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth()+i, eventy.fundraisingStart.getDate());
				System.out.println("new date :: "+date3);
			/*****************end****************************28.01.2016***************/
				cal2.setTime(date3);

				Formatter formatter=new Formatter();
				formatter.format("%tb", cal2);
				System.out.println("-----Formatter----" + formatter);
				//System.out.println(i + "-month");
				payOutTimeAndAllAmountsMap.put(i + "-month", formatter);


				if(date3.getMonth() == 11){
					/*date3 = new Date(eventy.fundraisingStart.getYear()+1,  0, 5);*/
					date3 = new Date(date3.getYear()+1,  0, 5);
				}else {
					date3 = new Date(date3.getYear(), date3.getMonth() + 1, 5);
				}

			}




			/*Date date3=null;
			Calendar cal2 = Calendar.getInstance();
			//cal2.setTime();
			for( i = 0 ;i<diffMonthBetweenStartAndEndFundraising;i++){
				date3=new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth(), eventy.fundraisingStart.getDate());
				cal2.setTime(date3);

				Formatter formatter=new Formatter();
				formatter.format("%tb", cal2);

				payOutTimeAndAllAmountsMap.put(firstPayoutCount + "-month", formatter);


				if(date3.getMonth() == 12){
					date3 = new Date(date3.getYear()+1,  1, 1);
				}else {
					date3 = new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth() + 1, 1);
				}

			}
*/











				string[0]="";

			for(int i1=0;i1<diffMonthBetweenStartAndEndFundraising;i1++){
				System.out.println(i1 + "-month");
				String element=payOutTimeAndAllAmountsMap.get(i1 + "-month").toString();
				System.out.println("----------element--------------"+element);
				string[i1+1]=element;
			}


/*

			for(int i1=1;i1<=firstPayoutCount;i1++){
				String element=payOutTimeAndAllAmountsMap.get(i1 + "-month").toString();
				string[i1]=element;
			}*/

			records.add(string);
			//records.add(new String[]{"",string});

			//String[] both = (String[])ArrayUtils.addAll(first, second);
			//records.add("",string);
			records.add(new String[]{"", "", "", "", "",  ""});


			String[] totalCheckDonations =  new String[100];
			totalCheckDonations[0] = "Cheque";

			String[]  totalCreditDonations = new String[100];
			totalCreditDonations[0] = "Credit Card";

			String[]  grossIncomeDonations = new String[100];
			grossIncomeDonations[0] = "Gross Income";


			String[] scFee = new String[100];
			scFee[0] = "SC Fee";


			String[] disbursibleAmount = new String[100];
			disbursibleAmount[0] = "Disbursible Amount";

           double lastMonthNetIncome=0;

			for(int i1=1;i1<=firstPayoutCount;i1++){
				//i1 payout will be shown
				System.out.println("-----------------------------------i1------------------------------------"+i1);
				System.out.println("totalCreditAmountNo::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAmountNo"));
				System.out.println("totalCheckAmountNo::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAmountNo"));
				System.out.println("totalDonationsAmount::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalDonationsAmount"));
				System.out.println("totalCreditAccNo::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAccNo"));
				System.out.println("totalCheckAccNo::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAccNo"));
				System.out.println("totalDonationsAccNo::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalDonationsAccNo"));
				System.out.println("totalCreditDonationsPerct::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditDonationsPerct"));
				System.out.println("totalCheckDonationsPerct::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckDonationsPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditDonationsAccNoPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckDonationsAccNoPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAmountNoForSponsor"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAmountNoForSponsor"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalSponsorAmount"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAccNoForSponsor"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAccNoForSponsor"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalSponsorsAccNo"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditSponsorsPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckSponsorsPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditSponsorsAccNoPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckSponsorsAccNoPerct"));
				System.out.println("grossIncomeAmount::"+payOutTimeAndAllAmountsMap.get(i1 + "-grossIncomeAmount"));
				System.out.println("netIncomeAmount::"+payOutTimeAndAllAmountsMap.get(i1 + "-netIncomeAmount"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-scholasticChallengeFeeAmount"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-netAmount"));
				double totalCreditAmounts = (double)payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAmountNoForSponsor")+(double)payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAmountNo");
				/*totalCheckDonations[i1] =String.valueOf(Double.parseDouble(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAmountNoForSponsor")+"") + Double.parseDouble(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAccNo")+"")) ;*/


				totalCheckDonations[i1] =String.valueOf(totalCreditAmounts) ;
				double totalChequeAmounts = (double)payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAmountNo")+(double)payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAmountNoForSponsor");
				totalCreditDonations[i1] = String.valueOf(totalChequeAmounts);
				grossIncomeDonations[i1] = String.valueOf((double)payOutTimeAndAllAmountsMap.get(i1 + "-grossIncomeAmount"));
				scFee[i1] = String.valueOf((double)payOutTimeAndAllAmountsMap.get(i1 + "-scholasticChallengeFeeAmount"));
				disbursibleAmount[i1] = String.valueOf((double)payOutTimeAndAllAmountsMap.get(i1 + "-netIncomeAmount"));

				//records.add(new String[]{"Balance Due", " ", "($" + remainingAmount + ")", "100%", "100%", remainingAmountPercent + "%"});
               if(i1 == firstPayoutCount){
				   System.out.println("netIncomeAmount for last month ::"+payOutTimeAndAllAmountsMap.get(i1 + "-netIncomeAmount"));
				   lastMonthNetIncome = (double)payOutTimeAndAllAmountsMap.get(i1 + "-netIncomeAmount");
			   }

			}

			records.add(totalCheckDonations);
			records.add(totalCreditDonations);
			records.add(grossIncomeDonations);
			records.add(scFee);
			records.add(disbursibleAmount);
			localCalendar = Calendar.getInstance(TimeZone.getDefault());

					currentDayOfMonth = eventy.fundraisingEnd.getDate();
			//currentDayOfMonth = localCalendar.get(Calendar.DAY_OF_MONTH);
			records.add(new String[]{"column1", "payout", "Withheld amount", "", "",  ""});




			if(currentDayOfMonth==5 || currentDayOfMonth>5){
				System.out.println("within current day of month 11 :: "+currentDayOfMonth);
				double totalRemainingAmount = 0;
				for(int i1=1;i1<firstPayoutCount;i1++){
					//i1 payout will be shown

					double paidAmount1 = (double)payOutTimeAndPaidAmountMap.get(i1);
					System.out.println("paidAmount1----"+paidAmount1);
					double remainingPayableAmount1 = (double)payOutTimeAndPayableAmountMap.get(i1);
					//reconciliationAmount
					System.out.println("remainingPayableAmount1890 "+remainingPayableAmount1);
					totalRemainingAmount = totalRemainingAmount + remainingPayableAmount1;
					reconciliationAmount = reconciliationAmount+remainingPayableAmount1;
					System.out.println("reconciliationAmount890 " + reconciliationAmount);
					System.out.println("total remaining amount :: " + totalRemainingAmount);


					/*records.add(new String[]{String.valueOf(i1), String.valueOf((double) payOutTimeAndPaidAmountMap.get(i1)), String.valueOf((double) reconciliationAmount), "", "", ""});*/
					records.add(new String[]{String.valueOf(i1), String.valueOf((double) payOutTimeAndPaidAmountMap.get(i1)), String.valueOf((double) remainingPayableAmount1), "", "", ""});


				}
				totalRemainingAmount = totalRemainingAmount+lastMonthNetIncome;

				records.add(new String[]{"Final payout",String.valueOf(totalRemainingAmount)});
			}else{


				if(currentDayOfMonth==eventy.fundraisingStart.getDate()){
					double totalRemainingAmount = 0;
					for(int i1=1;i1<=firstPayoutCount-1;i1++){


						double paidAmount1 = (double)payOutTimeAndPaidAmountMap.get(i1);
						System.out.println("paidAmount1----"+paidAmount1);
						double remainingPayableAmount1 = (double)payOutTimeAndPayableAmountMap.get(i1);
						//reconciliationAmount
						System.out.println("remainingPayableAmount2890 "+remainingPayableAmount1);
						totalRemainingAmount = totalRemainingAmount + remainingPayableAmount1;
						reconciliationAmount = reconciliationAmount+remainingPayableAmount1;
						System.out.println("reconciliationAmount3890 "+reconciliationAmount);



						records.add(new String[]{String.valueOf(i1), String.valueOf((double)payOutTimeAndPaidAmountMap.get(i1)),String.valueOf((double)remainingPayableAmount1) , "", "",  ""});


					}
					totalRemainingAmount = totalRemainingAmount+lastMonthNetIncome;
					records.add(new String[]{"Final payout",String.valueOf(totalRemainingAmount)});
				}
				else{
					double totalRemainingAmount = 0;
					for(int i1=1;i1<firstPayoutCount;i1++){


						double paidAmount1 = (double)payOutTimeAndPaidAmountMap.get(i1);
						System.out.println("paidAmount115634----"+paidAmount1);

						//i1 payout will be shown

						double remainingPayableAmount1 = (double)payOutTimeAndPayableAmountMap.get(i1);
						//reconciliationAmount


						System.out.println("remainingPayableAmount15634"+remainingPayableAmount1);
						totalRemainingAmount = totalRemainingAmount + remainingPayableAmount1;
						System.out.println("reconciliationAmount5634"+reconciliationAmount);
						reconciliationAmount = reconciliationAmount+remainingPayableAmount1;

						records.add(new String[]{String.valueOf(i1), String.valueOf((double)payOutTimeAndPaidAmountMap.get(i1)),String.valueOf((double)remainingPayableAmount1) , "", "",  ""});
					}
					totalRemainingAmount = totalRemainingAmount+lastMonthNetIncome;
					records.add(new String[]{"Final payout",String.valueOf(totalRemainingAmount)});
				}

			}


			if(eventy.fundraisingEnd.getDate()>=5){

				//total logic
				// for donations variable
				double totalCreditAmountNo = 0.0;
				double totalCheckAmountNo = 0.0;
				totalCreditAccNo = 0;
				totalCheckAccNo = 0;
				double totalDonationsAmount = 0.0;
				double totalDonationsAccNo = 0.0;

				//for sponsors variable
				double totalCreditAmountNoForSponsor = 0.0;
				double totalCheckAmountNoForSponsor = 0.0;
				double totalCreditAccNoForSponsor = 0.0;
				double totalCheckAccNoForSponsor = 0.0;
				double totalSponsorAmount = 0.0;
				double totalSponsorsAccNo = 0.0;





				Calendar cal = Calendar.getInstance();
				System.out.println("------------@@@@@@" + eventy.fundraisingStart);

				//Date date
				cal.setTime(eventy.fundraisingEnd);




				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
				Date eachMonthFirstDay = cal.getTime();
				System.out.println("---eachMonthFirstDay1----" + eachMonthFirstDay);
				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
				Date eachMonthLastDay = eventy.fundraisingEnd;
				System.out.println("---eachMonthLastDay1---" + eachMonthLastDay);


				if (donations != null && donations.size() > 0) {


					for (Donation donation : donations) {
						if(donation !=null){
							if ((donation.dateCreated.equals(eachMonthFirstDay) || donation.dateCreated.after(eachMonthFirstDay)) && (donation.dateCreated.before(eachMonthLastDay) || donation.dateCreated.equals(eachMonthLastDay))) {
								//total donations calculation for i where i
								if (donation.donationType != Donation.DonationType.SPONSOR) {
									if (donation.paymentType == Donation.PaymentType.CREDIT) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCreditAmountNo = totalCreditAmountNo + donation.amount;
										//totalCreditAmount = String.valueOf(totalCreditAmountNo);
										totalCreditAccNo = totalCreditAccNo + 1;
										//totalCreditAcc = String.valueOf(totalCreditAccNo);

									} else if (donation.paymentType == Donation.PaymentType.CHECK) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCheckAmountNo = totalCheckAmountNo + donation.amount;
										//totalCheckAmount = String.valueOf(totalCheckAmountNo);
										totalCheckAccNo = totalCheckAccNo + 1;
										//totalCheckAcc = String.valueOf(totalCheckAccNo);

									}
								}
								if (donation.donationType == Donation.DonationType.SPONSOR) {
									if (donation.paymentType == Donation.PaymentType.CREDIT) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCreditAmountNoForSponsor = totalCreditAmountNoForSponsor + donation.amount;
										//totalCreditAmount = String.valueOf(totalCreditAmountNo);
										totalCreditAccNoForSponsor = totalCreditAccNoForSponsor + 1;
										//totalCreditAcc = String.valueOf(totalCreditAccNo);

									} else if (donation.paymentType == Donation.PaymentType.CHECK) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCheckAmountNoForSponsor = totalCheckAmountNoForSponsor + donation.amount;
										//totalCheckAmount = String.valueOf(totalCheckAmountNo);
										totalCheckAccNoForSponsor = totalCheckAccNoForSponsor + 1;
										//totalCheckAcc = String.valueOf(totalCheckAccNo);

									}
								}


							}
						}
					}

				}



				double  withheldAmount=totalCreditAmountNo+totalCheckAmountNo+totalCheckAmountNoForSponsor+totalCreditAmountNoForSponsor;
				withheldAmount=withheldAmount-((withheldAmount*scholasticChallengeFeeAmountPerct)/100);

				//***28.01.2016***records.add(new String[]{"Final Reconciliation ", String.valueOf((double)withheldAmount+(double)reconciliationAmount),"" , "", "",  ""});











			}



			else{


				double totalCreditAmountNo = 0.0;
				double totalCheckAmountNo = 0.0;
				totalCreditAccNo = 0;
				totalCheckAccNo = 0;
				double totalDonationsAmount = 0.0;
				double totalDonationsAccNo = 0.0;

				//for sponsors variable
				double totalCreditAmountNoForSponsor = 0.0;
				double totalCheckAmountNoForSponsor = 0.0;
				double totalCreditAccNoForSponsor = 0.0;
				double totalCheckAccNoForSponsor = 0.0;
				double totalSponsorAmount = 0.0;
				double totalSponsorsAccNo = 0.0;





				Calendar cal = Calendar.getInstance();
				System.out.println("------------@@@@@@" + eventy.fundraisingStart);

				//Date date
				Date startDateCheck=new Date();

				//date2=new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth(), eventy.fundraisingStart.getDate());
				if(eventy.fundraisingEnd.getMonth() == 1){
					startDateCheck = new Date(eventy.fundraisingStart.getYear()-1,  12, 1);
				}else {
					startDateCheck = new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth() - 1, 1);
				}









				cal.setTime(startDateCheck);




				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
				Date eachMonthFirstDay = cal.getTime();
				System.out.println("---eachMonthFirstDay2----" + eachMonthFirstDay);
				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
				Date eachMonthLastDay = eventy.fundraisingEnd;
				System.out.println("---eachMonthLastDay2---" + eachMonthLastDay);


				if (donations != null && donations.size() > 0) {


					for (Donation donation : donations) {
						if(donation !=null){
							if ((donation.dateCreated.equals(eachMonthFirstDay) || donation.dateCreated.after(eachMonthFirstDay)) && (donation.dateCreated.before(eachMonthLastDay) || donation.dateCreated.equals(eachMonthLastDay))) {
								//total donations calculation for i where i
								if (donation.donationType != Donation.DonationType.SPONSOR) {
									if (donation.paymentType == Donation.PaymentType.CREDIT) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCreditAmountNo = totalCreditAmountNo + donation.amount;
										//totalCreditAmount = String.valueOf(totalCreditAmountNo);
										totalCreditAccNo = totalCreditAccNo + 1;
										//totalCreditAcc = String.valueOf(totalCreditAccNo);

									} else if (donation.paymentType == Donation.PaymentType.CHECK) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCheckAmountNo = totalCheckAmountNo + donation.amount;
										//totalCheckAmount = String.valueOf(totalCheckAmountNo);
										totalCheckAccNo = totalCheckAccNo + 1;
										//totalCheckAcc = String.valueOf(totalCheckAccNo);

									}
								}
								if (donation.donationType == Donation.DonationType.SPONSOR) {
									if (donation.paymentType == Donation.PaymentType.CREDIT) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCreditAmountNoForSponsor = totalCreditAmountNoForSponsor + donation.amount;
										//totalCreditAmount = String.valueOf(totalCreditAmountNo);
										totalCreditAccNoForSponsor = totalCreditAccNoForSponsor + 1;
										//totalCreditAcc = String.valueOf(totalCreditAccNo);

									} else if (donation.paymentType == Donation.PaymentType.CHECK) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCheckAmountNoForSponsor = totalCheckAmountNoForSponsor + donation.amount;
										//totalCheckAmount = String.valueOf(totalCheckAmountNo);
										totalCheckAccNoForSponsor = totalCheckAccNoForSponsor + 1;
										//totalCheckAcc = String.valueOf(totalCheckAccNo);

									}
								}


							}
						}
					}

				}



				double  withheldAmount=totalCreditAmountNo+totalCheckAmountNo+totalCheckAmountNoForSponsor+totalCreditAmountNoForSponsor;
				withheldAmount=withheldAmount-((withheldAmount*scholasticChallengeFeeAmountPerct)/100);

				//***28.01.2016***records.add(new String[]{"Final Reconciliation ", String.valueOf((double)withheldAmount+(double)reconciliationAmount),"" , "", "",  ""});















			}





















		}




















		//----rimi-----end----------------21.01.2016-------------------------------------------------//




		//diff between month of current date and fundraising start month


		///int diffMonth = diffYear * 12 + endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);
		return records;
	}













	private static List<String[]> payoutToStringArrayForMonth2(List<Donation> donations, Event eventy){
		List<String[]> records = new ArrayList<String[]>();
		Date currDate = new Date();
		long daysBetweenStartAndCurrDate = currDate.getTime()-eventy.fundraisingStart.getTime();
		Date date1=null;
		Date date2=null;
		//Calendar calendar=Calendar.getInstance(eventy.fundraisingStart.getTime());
		date2=new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth(), eventy.fundraisingStart.getDate());
		if(eventy.fundraisingStart.getMonth() == 11){
			date1 = new Date(eventy.fundraisingStart.getYear()+1,  0, 5);
		}else {
			date1 = new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth() + 1, 5);
		}

		System.out.println("date2----"+date2+"@@@@date1---"+date1+"@@@@currDate@@@"+currDate);




		long difffirstpayout=date1.getTime()-eventy.fundraisingStart.getTime();
		Map<Integer,Double> payout=new HashMap<Integer,Double>();
		Map<Integer,Double> consilation=new HashMap<Integer,Double>();
		int totalCreditAccNo=0;
		String totalCreditAcc=null;
		int totalCheckAccNo=0;
		String totalCheckAcc=null;

		double totalPaymentByCredit=0.0;
		double totalPaymentByCheck=0.0;
		double totalserviceTax=0.0;





		//----rimi-----start----------------21.01.2016-------------------------------------------------//

		double scholasticChallengeFeeAmount = 0.0;
		double scholasticChallengeFeeAmountPerct = eventy.serviceFee;
		if(scholasticChallengeFeeAmountPerct == 0){
			scholasticChallengeFeeAmountPerct = 10.0;
		}
		long daysBetweenStartAndEndFundraising = eventy.fundraisingEnd.getTime()-eventy.fundraisingStart.getTime();
		System.out.println("start day:: "+eventy.fundraisingStart);
		System.out.println("end day:: "+eventy.fundraisingEnd);
		System.out.println ("Days: " + TimeUnit.DAYS.convert(daysBetweenStartAndEndFundraising, TimeUnit.MILLISECONDS));
		int diffDays = (int)TimeUnit.DAYS.convert(daysBetweenStartAndEndFundraising, TimeUnit.MILLISECONDS);
		Calendar localCalendar = Calendar.getInstance(TimeZone.getDefault());
		int currentDayOfMonth = localCalendar.get(Calendar.DAY_OF_MONTH);
		System.out.println("currentDayOfMonth :: "+currentDayOfMonth);

		/***********Add Morning 28.01.16********************/
		int diffMonthBetweenStartAndEndFundraising =0;
		int monthBetweenStartAndCurrDate =0;

		int check1 = (int) diffDays/30;
		float check2=(float) diffDays/30;
		System.out.println("-----------check1--------check2------"+check1+"-----"+check2);


		if((check2-check1)>0){


			if(eventy.fundraisingEnd.getDate()<eventy.fundraisingStart.getDate()){
				diffMonthBetweenStartAndEndFundraising = ((int) diffDays/30) +2;
			}

			else{
				diffMonthBetweenStartAndEndFundraising = ((int) diffDays/30) +1;
			}


		}
		else{
			diffMonthBetweenStartAndEndFundraising = ((int) diffDays/30);
		}
		//int diffMonthBetweenStartAndEndFundraising = (int) diffDays/30;

		System.out.println("------------diffMonthBetweenStartAndEndFundraising-----------"+diffMonthBetweenStartAndEndFundraising);




		Date currDate1 = new Date();
		long daysBetweenStartAndCurrDate1 = currDate1.getTime()-eventy.fundraisingStart.getTime();
		int diffDaysBetweenStartAndCurrDate1 = (int)TimeUnit.DAYS.convert(daysBetweenStartAndCurrDate1, TimeUnit.MILLISECONDS);



		check1 = (int) diffDaysBetweenStartAndCurrDate1/30;
		check2=(float) diffDaysBetweenStartAndCurrDate1/30;
		if((check2-check1)>0){

			if(currDate1.getDate()<eventy.fundraisingStart.getDate()){
				diffMonthBetweenStartAndEndFundraising = ((int) diffDays/30) +2;
			}
			else
			{
				monthBetweenStartAndCurrDate = ((int) diffDaysBetweenStartAndCurrDate1/30) +1;
			}

		}
		else{
			monthBetweenStartAndCurrDate = ((int) diffDaysBetweenStartAndCurrDate1/30);
		}

		System.out.println("------------monthBetweenStartAndCurrDate-----------"+monthBetweenStartAndCurrDate);
		/***********Add Morning 28.01.16********************/



		//int monthBetweenStartAndCurrDate = diffDaysBetweenStartAndCurrDate1/30;





		date1 = new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth() + 1, 5);
		//int diffMonthBetweenStartAndEndFundraising


		LocalDate start = new LocalDate(eventy.fundraisingStart.getTime());
		//LocalDate start = new LocalDate(firstDate.getTime());
		LocalDate end = new LocalDate(currDate1.getTime());

		//monthBetweenStartAndCurrDate =Months.monthsBetween(start, end).getMonths();






		//Date startDate = new Date("2015-02-14");


	/*	int months  = (firstDate.getTime().get(Calendar.YEAR) - secondDate.get(Calendar.YEAR)) * 12 +
				(firstDate.get(Calendar.MONTH)- secondDate.get(Calendar.MONTH)) +
				(firstDate.get(Calendar.DAY_OF_MONTH) >= secondDate.get(Calendar.DAY_OF_MONTH)? 0: -1);*/


		System.out.println("-----------------------Diff months is-------------------" + monthBetweenStartAndCurrDate);

		System.out.println();


//Calculate Difference between month---







		//Test


		System.out.println();

		int i = 0;
		int firstPayoutCount = 0;
		double reconciliationAmount = 0.0;
		HashMap payOutTimeAndPaidAmountMap = new HashMap();
		HashMap payOutTimeAndAllAmountsMap = new HashMap();
		HashMap payOutTimeAndPayableAmountMap = new HashMap();
		if(monthBetweenStartAndCurrDate<diffMonthBetweenStartAndEndFundraising){
			for( i = 0 ;i<monthBetweenStartAndCurrDate;i++) {

				System.out.println("--------------------------------------i---------------------" + i);

				Date dateStartFundraising = eventy.fundraisingStart;


				//fundraising start date+ i no of month
				Calendar calendar = Calendar.getInstance();


				//java.util.Date date= new Date();
				Calendar cal = Calendar.getInstance();
				System.out.println("------------@@@@@@" + eventy.fundraisingStart);

				//Date date
				cal.setTime(date2);
				//int month = eventy.fundraisingStart.getMonth();

				//System.out.println("---hjhhh-----"+month);
				//cal.add(month, i);
				//System.out.println("----------Jan--------"+cal.MONTH);
				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
				Date eachMonthFirstDay = cal.getTime();
				System.out.println("---eachMonthFirstDay9----" + eachMonthFirstDay);
				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
				Date eachMonthLastDay = cal.getTime();
				System.out.println("---eachMonthLastDay9---" + eachMonthLastDay);


				//current day
				Date currdate = new Date();


				//total logic
				// for donations variable
				double totalCreditAmountNo = 0.0;
				double totalCheckAmountNo = 0.0;
				totalCreditAccNo = 0;
				totalCheckAccNo = 0;
				double totalDonationsAmount = 0.0;
				double totalDonationsAccNo = 0.0;

				//for sponsors variable
				double totalCreditAmountNoForSponsor = 0.0;
				double totalCheckAmountNoForSponsor = 0.0;
				double totalCreditAccNoForSponsor = 0.0;
				double totalCheckAccNoForSponsor = 0.0;
				double totalSponsorAmount = 0.0;
				double totalSponsorsAccNo = 0.0;


				if (donations != null && donations.size() > 0) {


					for (Donation donation : donations) {
						if(donation !=null){
							if ((donation.dateCreated.equals(eachMonthFirstDay) || donation.dateCreated.after(eachMonthFirstDay)) && (donation.dateCreated.before(eachMonthLastDay) || donation.dateCreated.equals(eachMonthLastDay))) {
								//total donations calculation for i where i
								if (donation.donationType != Donation.DonationType.SPONSOR) {
									if (donation.paymentType == Donation.PaymentType.CREDIT) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCreditAmountNo = totalCreditAmountNo + donation.amount;
										//totalCreditAmount = String.valueOf(totalCreditAmountNo);
										totalCreditAccNo = totalCreditAccNo + 1;
										//totalCreditAcc = String.valueOf(totalCreditAccNo);

									} else if (donation.paymentType == Donation.PaymentType.CHECK) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCheckAmountNo = totalCheckAmountNo + donation.amount;
										//totalCheckAmount = String.valueOf(totalCheckAmountNo);
										totalCheckAccNo = totalCheckAccNo + 1;
										//totalCheckAcc = String.valueOf(totalCheckAccNo);

									}
								}
								if (donation.donationType == Donation.DonationType.SPONSOR) {
									if (donation.paymentType == Donation.PaymentType.CREDIT) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCreditAmountNoForSponsor = totalCreditAmountNoForSponsor + donation.amount;
										//totalCreditAmount = String.valueOf(totalCreditAmountNo);
										totalCreditAccNoForSponsor = totalCreditAccNoForSponsor + 1;
										//totalCreditAcc = String.valueOf(totalCreditAccNo);

									} else if (donation.paymentType == Donation.PaymentType.CHECK) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCheckAmountNoForSponsor = totalCheckAmountNoForSponsor + donation.amount;
										//totalCheckAmount = String.valueOf(totalCheckAmountNo);
										totalCheckAccNoForSponsor = totalCheckAccNoForSponsor + 1;
										//totalCheckAcc = String.valueOf(totalCheckAccNo);

									}
								}


							}
						}
					}

				}

				//calculations of donations
				totalDonationsAmount = totalCreditAmountNo+totalCheckAmountNo;
				System.out.println("----totalDonationsAmount---"+totalDonationsAmount);
				totalDonationsAccNo = totalCreditAccNo+totalCheckAccNo;
				System.out.println("----totalDonationsAccNo---"+totalDonationsAccNo);
				double totalCreditDonationsPerct = (totalCreditAmountNo/totalDonationsAmount)*100;
				double totalCheckDonationsPerct = (totalCheckAmountNo/totalDonationsAmount)*100;
				double totalCreditDonationsAccNoPerct = (totalCreditAccNo/totalDonationsAccNo)*100;
				double totalCheckDonationsAccNoPerct = (totalCheckAccNo/totalDonationsAccNo)*100;


				//total sponsorship calculations
				totalSponsorAmount = totalCreditAmountNoForSponsor+totalCheckAmountNoForSponsor;
				totalSponsorsAccNo = totalCreditAccNoForSponsor+totalCheckAccNoForSponsor;
				double totalCreditSponsorsPerct = (totalCreditAmountNoForSponsor/totalSponsorAmount)*100;
				double totalCheckSponsorsPerct = (totalCheckAmountNoForSponsor/totalSponsorAmount)*100;
				double totalCreditSponsorsAccNoPerct = (totalCreditAccNoForSponsor/totalSponsorsAccNo)*100;
				double totalCheckSponsorsAccNoPerct = (totalCheckAccNoForSponsor/totalSponsorsAccNo)*100;

				//gross income
				double grossIncomeAmount = totalDonationsAmount + totalSponsorAmount;

				//scholastic challenge fee
				scholasticChallengeFeeAmount = 0.0;

				/*double scholasticChallengeFeeAmountPerct = eventy.serviceFee;
				if(scholasticChallengeFeeAmountPerct == 0){
					scholasticChallengeFeeAmountPerct = 10.0;
				}*/

				scholasticChallengeFeeAmount = grossIncomeAmount*scholasticChallengeFeeAmountPerct/100;



				//net income

				double netIncomeAmount = grossIncomeAmount-scholasticChallengeFeeAmount;

				//first payout

				double remainingPayableAmount = 0.0;
				double paidoutAmount = 0.0;
				firstPayoutCount = firstPayoutCount + 1;
				//paid amout percentage
				double paidAmountPercent = 75;

				paidoutAmount = (netIncomeAmount*75)/100;
				remainingPayableAmount = netIncomeAmount - paidoutAmount;





				//Commend on 27.01.16--------
			/*	Formatter formatter=new Formatter();
				formatter.format("%tb",cal);

				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-month",formatter);*/






				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditAmountNo",totalCreditAmountNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckAmountNo",totalCheckAmountNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalDonationsAmount",totalDonationsAmount);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditAccNo",totalCreditAccNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckAccNo",totalCheckAccNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalDonationsAccNo",totalDonationsAccNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditDonationsPerct",totalCreditDonationsPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckDonationsPerct",totalCheckDonationsPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditDonationsAccNoPerct",totalCreditDonationsAccNoPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckDonationsAccNoPerct",totalCheckDonationsAccNoPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditAmountNoForSponsor",totalCreditAmountNoForSponsor);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckAmountNoForSponsor",totalCheckAmountNoForSponsor);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalSponsorAmount",totalSponsorAmount);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditAccNoForSponsor",totalCreditAccNoForSponsor);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckAccNoForSponsor",totalCheckAccNoForSponsor);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalSponsorsAccNo",totalSponsorsAccNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditSponsorsPerct",totalCreditSponsorsPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckSponsorsPerct",totalCheckSponsorsPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditSponsorsAccNoPerct",totalCreditSponsorsAccNoPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckSponsorsAccNoPerct",totalCheckSponsorsAccNoPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-grossIncomeAmount",grossIncomeAmount);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-netIncomeAmount",netIncomeAmount);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-scholasticChallengeFeeAmount",scholasticChallengeFeeAmount);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-netAmount",netIncomeAmount);
				payOutTimeAndPaidAmountMap.put(firstPayoutCount,paidoutAmount);
				payOutTimeAndPayableAmountMap.put(firstPayoutCount,remainingPayableAmount);


				/*localCalendar = Calendar.getInstance(TimeZone.getDefault());
				currentDayOfMonth = localCalendar.get(Calendar.DAY_OF_MONTH);
				if(currentDayOfMonth==5 || currentDayOfMonth>5){
					for(int i1=1;i1<=firstPayoutCount;i1++){
						//i1 payout will be shown

						double remainingPayableAmount1 = (double)payOutTimeAndPaidAmountMap.get(i1);
						//reconciliationAmount
						reconciliationAmount = reconciliationAmount+remainingPayableAmount1;


					}
				}else{
					for(int i1=1;i1<=firstPayoutCount-1;i1++){
						//i1 payout will be shown

						double remainingPayableAmount1 = (double)payOutTimeAndPaidAmountMap.get(i1);
						//reconciliationAmount
						reconciliationAmount = reconciliationAmount+remainingPayableAmount1;


					}
				}
*/



				if(date2.getMonth() == 11){
					date2 = new Date(date2.getYear()+1,  0, date2.getDate());
				}else {
					date2 = new Date(date2.getYear(), date2.getMonth() + 1, date2.getDate());
				}


				System.out.println("------------------date2----------------"+date2);


				//


			}






			records.add(new String[]{"Event Start Date", String.valueOf(eventy.fundraisingStart), "", "", "",  ""});
			records.add(new String[]{"Event End Date", String.valueOf(eventy.fundraisingEnd), "", "", "",  ""});
			records.add(new String[]{"SC Fee", String.valueOf(scholasticChallengeFeeAmountPerct)+"%", "", "", "",  ""});
			records.add(new String[]{"Monthly payout ", "75%", "", "", "",  ""});
			records.add(new String[]{"", "", "", "", "",  ""});

			String[] string =  new String[100];
			string[0]="Collections Summary";








			/********Show Month Between Start and end*********/
			Date date3=null;
			Calendar cal2 = Calendar.getInstance();
			//cal2.setTime();
			date3=new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth(), eventy.fundraisingStart.getDate());
			for( i = 0 ;i<monthBetweenStartAndCurrDate;i++){

				cal2.setTime(date3);

				Formatter formatter=new Formatter();
				formatter.format("%tb", cal2);

				payOutTimeAndAllAmountsMap.put(i + "-month", formatter);


				if(date3.getMonth() == 11){
					/*date3 = new Date(eventy.fundraisingStart.getYear()+1,  0, 5);*/
					date3 = new Date(date3.getYear()+1,  0, 5);
				}else {
					date3 = new Date(date3.getYear(), date3.getMonth() + 1, 5);
				}

			}




			for(int i1=0;i1<monthBetweenStartAndCurrDate;i1++){
				String element=payOutTimeAndAllAmountsMap.get(i1 + "-month").toString();
				string[i1+1]=element;
			}



			records.add(string);
			records.add(new String[]{"", "", "", "", "",  ""});


			String[] totalCheckDonations =  new String[100];
			totalCheckDonations[0] = "Cheque";

			String[]  totalCreditDonations = new String[100];
			totalCreditDonations[0] = "Credit Card";

			String[]  grossIncomeDonations = new String[100];
			grossIncomeDonations[0] = "Gross Income";


			String[] scFee = new String[100];
			scFee[0] = "SC Fee";


			String[] disbursibleAmount = new String[100];
			disbursibleAmount[0] = "Disbursible Amount";



			for(int i1=1;i1<=firstPayoutCount;i1++){
				//i1 payout will be shown
				System.out.println("-----------------------------------i1------------------------------------"+i1);
				System.out.println("totalCreditAmountNo::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAmountNo"));
				System.out.println("totalCheckAmountNo::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAmountNo"));
				System.out.println("totalDonationsAmount::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalDonationsAmount"));
				System.out.println("totalCreditAccNo::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAccNo"));
				System.out.println("totalCheckAccNo::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAccNo"));
				System.out.println("totalDonationsAccNo::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalDonationsAccNo"));
				System.out.println("totalCreditDonationsPerct::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditDonationsPerct"));
				System.out.println("totalCheckDonationsPerct::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckDonationsPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditDonationsAccNoPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckDonationsAccNoPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAmountNoForSponsor"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAmountNoForSponsor"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalSponsorAmount"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAccNoForSponsor"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAccNoForSponsor"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalSponsorsAccNo"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditSponsorsPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckSponsorsPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditSponsorsAccNoPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckSponsorsAccNoPerct"));
				System.out.println("grossIncomeAmount::"+payOutTimeAndAllAmountsMap.get(i1 + "-grossIncomeAmount"));
				System.out.println("netIncomeAmount::"+payOutTimeAndAllAmountsMap.get(i1 + "-netIncomeAmount"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-scholasticChallengeFeeAmount"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-netAmount"));
				double totalCreditAmounts = (double)payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAmountNoForSponsor")+(double)payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAmountNo");
				/*totalCheckDonations[i1] =String.valueOf(Double.parseDouble(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAmountNoForSponsor")+"") + Double.parseDouble(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAccNo")+"")) ;*/


				totalCheckDonations[i1] =String.valueOf(totalCreditAmounts) ;
				double totalChequeAmounts = (double)payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAmountNo")+(double)payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAmountNoForSponsor");
				totalCreditDonations[i1] = String.valueOf(totalChequeAmounts);
				grossIncomeDonations[i1] = String.valueOf((double)payOutTimeAndAllAmountsMap.get(i1 + "-grossIncomeAmount"));
				scFee[i1] = String.valueOf((double)payOutTimeAndAllAmountsMap.get(i1 + "-scholasticChallengeFeeAmount"));
				disbursibleAmount[i1] = String.valueOf((double)payOutTimeAndAllAmountsMap.get(i1 + "-netIncomeAmount"));

				//records.add(new String[]{"Balance Due", " ", "($" + remainingAmount + ")", "100%", "100%", remainingAmountPercent + "%"});


			}

			records.add(totalCheckDonations);
			records.add(totalCreditDonations);
			records.add(grossIncomeDonations);
			records.add(scFee);
			records.add(disbursibleAmount);
			localCalendar = Calendar.getInstance(TimeZone.getDefault());
			currentDayOfMonth = localCalendar.get(Calendar.DAY_OF_MONTH);
			records.add(new String[]{"column1", "payout", "Withheld amount", "", "",  ""});
			if(currentDayOfMonth==5 || currentDayOfMonth>5){
				for(int i1=1;i1<firstPayoutCount;i1++){
					//i1 payout will be shown

					double paidAmount1 = (double)payOutTimeAndPaidAmountMap.get(i1);
					System.out.println("paidAmount1----"+paidAmount1);
					double remainingPayableAmount1 = (double)payOutTimeAndPayableAmountMap.get(i1);
					//reconciliationAmount
					System.out.println("remainingPayableAmount1890 "+remainingPayableAmount1);

					reconciliationAmount = reconciliationAmount+remainingPayableAmount1;
					System.out.println("reconciliationAmount890 "+reconciliationAmount);

					//   double

					records.add(new String[]{String.valueOf(i1), String.valueOf((double)payOutTimeAndPaidAmountMap.get(i1)),String.valueOf((double)remainingPayableAmount1) , "", "",  ""});


				}
			}else{


				if(currentDayOfMonth==eventy.fundraisingStart.getDate()){
					for(int i1=1;i1<=firstPayoutCount-1;i1++){


						double paidAmount1 = (double)payOutTimeAndPaidAmountMap.get(i1);
						System.out.println("paidAmount1----"+paidAmount1);
						double remainingPayableAmount1 = (double)payOutTimeAndPayableAmountMap.get(i1);
						//reconciliationAmount
						System.out.println("remainingPayableAmount2890 "+remainingPayableAmount1);

						reconciliationAmount = reconciliationAmount+remainingPayableAmount1;
						System.out.println("reconciliationAmount3890 "+reconciliationAmount);



						records.add(new String[]{String.valueOf(i1), String.valueOf((double)payOutTimeAndPaidAmountMap.get(i1)),String.valueOf((double)remainingPayableAmount1) , "", "",  ""});


					}
				}
				else{
					for(int i1=1;i1<=firstPayoutCount;i1++){


						double paidAmount1 = (double)payOutTimeAndPaidAmountMap.get(i1);
						System.out.println("paidAmount115634----"+paidAmount1);

						//i1 payout will be shown

						double remainingPayableAmount1 = (double)payOutTimeAndPayableAmountMap.get(i1);
						//reconciliationAmount


						System.out.println("remainingPayableAmount15634"+remainingPayableAmount1);
						System.out.println("reconciliationAmount5634"+reconciliationAmount);
						reconciliationAmount = reconciliationAmount+remainingPayableAmount1;

						records.add(new String[]{String.valueOf(i1), String.valueOf((double)payOutTimeAndPaidAmountMap.get(i1)),String.valueOf((double)remainingPayableAmount1) , "", "",  ""});
					}
				}

			}

			//If the current Date greater than 5th date
			if(currDate.getDate()>=5){

				//total logic
				// for donations variable
				double totalCreditAmountNo = 0.0;
				double totalCheckAmountNo = 0.0;
				totalCreditAccNo = 0;
				totalCheckAccNo = 0;
				double totalDonationsAmount = 0.0;
				double totalDonationsAccNo = 0.0;

				//for sponsors variable
				double totalCreditAmountNoForSponsor = 0.0;
				double totalCheckAmountNoForSponsor = 0.0;
				double totalCreditAccNoForSponsor = 0.0;
				double totalCheckAccNoForSponsor = 0.0;
				double totalSponsorAmount = 0.0;
				double totalSponsorsAccNo = 0.0;





				Calendar cal = Calendar.getInstance();
				System.out.println("------------@@@@@@" + eventy.fundraisingStart);

				//Date date
				cal.setTime(currDate);




				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
				Date eachMonthFirstDay = cal.getTime();
				System.out.println("---eachMonthFirstDay10----" + eachMonthFirstDay);
				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
				Date eachMonthLastDay = currDate;
				System.out.println("---eachMonthLastDay10---" + eachMonthLastDay);


				if (donations != null && donations.size() > 0) {


					for (Donation donation : donations) {
						if(donation !=null){
							if ((donation.dateCreated.equals(eachMonthFirstDay) || donation.dateCreated.after(eachMonthFirstDay)) && (donation.dateCreated.before(eachMonthLastDay) || donation.dateCreated.equals(eachMonthLastDay))) {
								//total donations calculation for i where i
								if (donation.donationType != Donation.DonationType.SPONSOR) {
									if (donation.paymentType == Donation.PaymentType.CREDIT) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCreditAmountNo = totalCreditAmountNo + donation.amount;
										//totalCreditAmount = String.valueOf(totalCreditAmountNo);
										totalCreditAccNo = totalCreditAccNo + 1;
										//totalCreditAcc = String.valueOf(totalCreditAccNo);

									} else if (donation.paymentType == Donation.PaymentType.CHECK) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCheckAmountNo = totalCheckAmountNo + donation.amount;
										//totalCheckAmount = String.valueOf(totalCheckAmountNo);
										totalCheckAccNo = totalCheckAccNo + 1;
										//totalCheckAcc = String.valueOf(totalCheckAccNo);

									}
								}
								if (donation.donationType == Donation.DonationType.SPONSOR) {
									if (donation.paymentType == Donation.PaymentType.CREDIT) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCreditAmountNoForSponsor = totalCreditAmountNoForSponsor + donation.amount;
										//totalCreditAmount = String.valueOf(totalCreditAmountNo);
										totalCreditAccNoForSponsor = totalCreditAccNoForSponsor + 1;
										//totalCreditAcc = String.valueOf(totalCreditAccNo);

									} else if (donation.paymentType == Donation.PaymentType.CHECK) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCheckAmountNoForSponsor = totalCheckAmountNoForSponsor + donation.amount;
										//totalCheckAmount = String.valueOf(totalCheckAmountNo);
										totalCheckAccNoForSponsor = totalCheckAccNoForSponsor + 1;
										//totalCheckAcc = String.valueOf(totalCheckAccNo);

									}
								}


							}
						}
					}

				}



				double  withheldAmount=totalCreditAmountNo+totalCheckAmountNo+totalCheckAmountNoForSponsor+totalCreditAmountNoForSponsor;
				withheldAmount=withheldAmount-((withheldAmount*scholasticChallengeFeeAmountPerct)/100);

				records.add(new String[]{"Final Remaining Amount", "",String.valueOf((double)withheldAmount) , "", "",  ""});





			}



			else{


				double totalCreditAmountNo = 0.0;
				double totalCheckAmountNo = 0.0;
				totalCreditAccNo = 0;
				totalCheckAccNo = 0;
				double totalDonationsAmount = 0.0;
				double totalDonationsAccNo = 0.0;

				//for sponsors variable
				double totalCreditAmountNoForSponsor = 0.0;
				double totalCheckAmountNoForSponsor = 0.0;
				double totalCreditAccNoForSponsor = 0.0;
				double totalCheckAccNoForSponsor = 0.0;
				double totalSponsorAmount = 0.0;
				double totalSponsorsAccNo = 0.0;





				Calendar cal = Calendar.getInstance();
				System.out.println("------------@@@@@@" + eventy.fundraisingStart);

				//Date date
				Date startDateCheck=new Date();

				//date2=new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth(), eventy.fundraisingStart.getDate());
				if(currDate.getMonth() == 1){
					startDateCheck = new Date(eventy.fundraisingStart.getYear()-1,  12, 1);
				}else {
					startDateCheck = new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth() - 1, 1);
				}









				cal.setTime(startDateCheck);




				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
				Date eachMonthFirstDay = cal.getTime();
				System.out.println("---eachMonthFirstDay11----" + eachMonthFirstDay);
				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
				Date eachMonthLastDay = currDate;
				System.out.println("---eachMonthLastDay11---" + eachMonthLastDay);


				if (donations != null && donations.size() > 0) {


					for (Donation donation : donations) {
						if(donation !=null){
							if ((donation.dateCreated.equals(eachMonthFirstDay) || donation.dateCreated.after(eachMonthFirstDay)) && (donation.dateCreated.before(eachMonthLastDay) || donation.dateCreated.equals(eachMonthLastDay))) {
								//total donations calculation for i where i
								if (donation.donationType != Donation.DonationType.SPONSOR) {
									if (donation.paymentType == Donation.PaymentType.CREDIT) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCreditAmountNo = totalCreditAmountNo + donation.amount;
										//totalCreditAmount = String.valueOf(totalCreditAmountNo);
										totalCreditAccNo = totalCreditAccNo + 1;
										//totalCreditAcc = String.valueOf(totalCreditAccNo);

									} else if (donation.paymentType == Donation.PaymentType.CHECK) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCheckAmountNo = totalCheckAmountNo + donation.amount;
										//totalCheckAmount = String.valueOf(totalCheckAmountNo);
										totalCheckAccNo = totalCheckAccNo + 1;
										//totalCheckAcc = String.valueOf(totalCheckAccNo);

									}
								}
								if (donation.donationType == Donation.DonationType.SPONSOR) {
									if (donation.paymentType == Donation.PaymentType.CREDIT) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCreditAmountNoForSponsor = totalCreditAmountNoForSponsor + donation.amount;
										//totalCreditAmount = String.valueOf(totalCreditAmountNo);
										totalCreditAccNoForSponsor = totalCreditAccNoForSponsor + 1;
										//totalCreditAcc = String.valueOf(totalCreditAccNo);

									} else if (donation.paymentType == Donation.PaymentType.CHECK) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCheckAmountNoForSponsor = totalCheckAmountNoForSponsor + donation.amount;
										//totalCheckAmount = String.valueOf(totalCheckAmountNo);
										totalCheckAccNoForSponsor = totalCheckAccNoForSponsor + 1;
										//totalCheckAcc = String.valueOf(totalCheckAccNo);

									}
								}


							}
						}
					}

				}



				double  withheldAmount=totalCreditAmountNo+totalCheckAmountNo+totalCheckAmountNoForSponsor+totalCreditAmountNoForSponsor;
				withheldAmount=withheldAmount-((withheldAmount*scholasticChallengeFeeAmountPerct)/100);

				records.add(new String[]{"Final Remaining Amount", "",String.valueOf((double)withheldAmount) , "", "",  ""});












			}










		}



		//If the Fundraising End date before current date-------------------------














		else{
			System.out.println("diffMonthBetweenStartAndEndFundraising :: "+diffMonthBetweenStartAndEndFundraising);
			for( i = 0 ;i<diffMonthBetweenStartAndEndFundraising;i++) {

				System.out.println("--------------------------------------i---------------------" + i);

				Date dateStartFundraising = eventy.fundraisingStart;


				//fundraising start date+ i no of month
				Calendar calendar = Calendar.getInstance();


				//java.util.Date date= new Date();
				Calendar cal = Calendar.getInstance();
				System.out.println("------------@@@@@@" + eventy.fundraisingStart);

				//Date date
				cal.setTime(date2);
				//int month = eventy.fundraisingStart.getMonth();

				//System.out.println("---hjhhh-----"+month);
				//cal.add(month, i);
				//System.out.println("----------Jan--------"+cal.MONTH);
				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
				Date eachMonthFirstDay = cal.getTime();
				System.out.println("---eachMonthFirstDay12----" + eachMonthFirstDay);
				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
				Date eachMonthLastDay = cal.getTime();
				System.out.println("---eachMonthLastDay12---" + eachMonthLastDay);


				//current day
				Date currdate = new Date();


				//total logic
				// for donations variable
				double totalCreditAmountNo = 0.0;
				double totalCheckAmountNo = 0.0;
				totalCreditAccNo = 0;
				totalCheckAccNo = 0;
				double totalDonationsAmount = 0.0;
				double totalDonationsAccNo = 0.0;

				//for sponsors variable
				double totalCreditAmountNoForSponsor = 0.0;
				double totalCheckAmountNoForSponsor = 0.0;
				double totalCreditAccNoForSponsor = 0.0;
				double totalCheckAccNoForSponsor = 0.0;
				double totalSponsorAmount = 0.0;
				double totalSponsorsAccNo = 0.0;


				if (donations != null && donations.size() > 0) {


					for (Donation donation : donations) {
						if(donation !=null){
							if ((donation.dateCreated.equals(eachMonthFirstDay) || donation.dateCreated.after(eachMonthFirstDay)) && (donation.dateCreated.before(eachMonthLastDay) || donation.dateCreated.equals(eachMonthLastDay))) {
								//total donations calculation for i where i
								if (donation.donationType != Donation.DonationType.SPONSOR) {
									if (donation.paymentType == Donation.PaymentType.CREDIT) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCreditAmountNo = totalCreditAmountNo + donation.amount;
										//totalCreditAmount = String.valueOf(totalCreditAmountNo);
										totalCreditAccNo = totalCreditAccNo + 1;
										//totalCreditAcc = String.valueOf(totalCreditAccNo);

									} else if (donation.paymentType == Donation.PaymentType.CHECK) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCheckAmountNo = totalCheckAmountNo + donation.amount;
										//totalCheckAmount = String.valueOf(totalCheckAmountNo);
										totalCheckAccNo = totalCheckAccNo + 1;
										//totalCheckAcc = String.valueOf(totalCheckAccNo);

									}
								}
								if (donation.donationType == Donation.DonationType.SPONSOR) {
									if (donation.paymentType == Donation.PaymentType.CREDIT) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCreditAmountNoForSponsor = totalCreditAmountNoForSponsor + donation.amount;
										//totalCreditAmount = String.valueOf(totalCreditAmountNo);
										totalCreditAccNoForSponsor = totalCreditAccNoForSponsor + 1;
										//totalCreditAcc = String.valueOf(totalCreditAccNo);

									} else if (donation.paymentType == Donation.PaymentType.CHECK) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCheckAmountNoForSponsor = totalCheckAmountNoForSponsor + donation.amount;
										//totalCheckAmount = String.valueOf(totalCheckAmountNo);
										totalCheckAccNoForSponsor = totalCheckAccNoForSponsor + 1;
										//totalCheckAcc = String.valueOf(totalCheckAccNo);

									}
								}


							}
						}
					}

				}

				//calculations of donations
				totalDonationsAmount = totalCreditAmountNo+totalCheckAmountNo;
				System.out.println("----totalDonationsAmount---"+totalDonationsAmount);
				totalDonationsAccNo = totalCreditAccNo+totalCheckAccNo;
				System.out.println("----totalDonationsAccNo---"+totalDonationsAccNo);
				double totalCreditDonationsPerct = (totalCreditAmountNo/totalDonationsAmount)*100;
				double totalCheckDonationsPerct = (totalCheckAmountNo/totalDonationsAmount)*100;
				double totalCreditDonationsAccNoPerct = (totalCreditAccNo/totalDonationsAccNo)*100;
				double totalCheckDonationsAccNoPerct = (totalCheckAccNo/totalDonationsAccNo)*100;


				//total sponsorship calculations
				totalSponsorAmount = totalCreditAmountNoForSponsor+totalCheckAmountNoForSponsor;
				totalSponsorsAccNo = totalCreditAccNoForSponsor+totalCheckAccNoForSponsor;
				double totalCreditSponsorsPerct = (totalCreditAmountNoForSponsor/totalSponsorAmount)*100;
				double totalCheckSponsorsPerct = (totalCheckAmountNoForSponsor/totalSponsorAmount)*100;
				double totalCreditSponsorsAccNoPerct = (totalCreditAccNoForSponsor/totalSponsorsAccNo)*100;
				double totalCheckSponsorsAccNoPerct = (totalCheckAccNoForSponsor/totalSponsorsAccNo)*100;

				//gross income
				double grossIncomeAmount = totalDonationsAmount + totalSponsorAmount;

				//scholastic challenge fee
				scholasticChallengeFeeAmount = 0.0;

				/*double scholasticChallengeFeeAmountPerct = eventy.serviceFee;
				if(scholasticChallengeFeeAmountPerct == 0){
					scholasticChallengeFeeAmountPerct = 10.0;
				}*/

				scholasticChallengeFeeAmount = grossIncomeAmount*scholasticChallengeFeeAmountPerct/100;



				//net income

				double netIncomeAmount = grossIncomeAmount-scholasticChallengeFeeAmount;

				//first payout

				double remainingPayableAmount = 0.0;
				double paidoutAmount = 0.0;
				firstPayoutCount = firstPayoutCount + 1;
				//paid amout percentage
				double paidAmountPercent = 75;

				paidoutAmount = (netIncomeAmount*75)/100;
				remainingPayableAmount = netIncomeAmount - paidoutAmount;



				//Commend on 27.01.16---------

		/*		Formatter formatter=new Formatter();
				formatter.format("%tb",cal);

				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-month",formatter);
				*/





				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditAmountNo",totalCreditAmountNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckAmountNo",totalCheckAmountNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalDonationsAmount",totalDonationsAmount);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditAccNo",totalCreditAccNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckAccNo",totalCheckAccNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalDonationsAccNo",totalDonationsAccNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditDonationsPerct",totalCreditDonationsPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckDonationsPerct",totalCheckDonationsPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditDonationsAccNoPerct",totalCreditDonationsAccNoPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckDonationsAccNoPerct",totalCheckDonationsAccNoPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditAmountNoForSponsor",totalCreditAmountNoForSponsor);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckAmountNoForSponsor",totalCheckAmountNoForSponsor);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalSponsorAmount",totalSponsorAmount);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditAccNoForSponsor",totalCreditAccNoForSponsor);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckAccNoForSponsor",totalCheckAccNoForSponsor);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalSponsorsAccNo",totalSponsorsAccNo);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditSponsorsPerct",totalCreditSponsorsPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckSponsorsPerct",totalCheckSponsorsPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCreditSponsorsAccNoPerct",totalCreditSponsorsAccNoPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-totalCheckSponsorsAccNoPerct",totalCheckSponsorsAccNoPerct);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-grossIncomeAmount",grossIncomeAmount);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-netIncomeAmount",netIncomeAmount);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-scholasticChallengeFeeAmount",scholasticChallengeFeeAmount);
				payOutTimeAndAllAmountsMap.put(firstPayoutCount+"-netAmount",netIncomeAmount);
				payOutTimeAndPaidAmountMap.put(firstPayoutCount,paidoutAmount);
				payOutTimeAndPayableAmountMap.put(firstPayoutCount,remainingPayableAmount);


				/*localCalendar = Calendar.getInstance(TimeZone.getDefault());
				currentDayOfMonth = localCalendar.get(Calendar.DAY_OF_MONTH);
				if(currentDayOfMonth==5 || currentDayOfMonth>5){
					for(int i1=1;i1<=firstPayoutCount;i1++){
						//i1 payout will be shown

						double remainingPayableAmount1 = (double)payOutTimeAndPaidAmountMap.get(i1);
						//reconciliationAmount
						reconciliationAmount = reconciliationAmount+remainingPayableAmount1;


					}
				}else{
					for(int i1=1;i1<=firstPayoutCount-1;i1++){
						//i1 payout will be shown

						double remainingPayableAmount1 = (double)payOutTimeAndPaidAmountMap.get(i1);
						//reconciliationAmount
						reconciliationAmount = reconciliationAmount+remainingPayableAmount1;


					}
				}
*/



				if(date2.getMonth() == 11){
					date2 = new Date(date2.getYear()+1,  0, date2.getDate());
				}else {
					date2 = new Date(date2.getYear(), date2.getMonth() + 1, date2.getDate());
				}


				System.out.println("------------------date2----------------"+date2);


				//


			}




















			records.add(new String[]{"Event Start Date", String.valueOf(eventy.fundraisingStart), "", "", "",  ""});
			records.add(new String[]{"Event End Date", String.valueOf(eventy.fundraisingEnd), "", "", "",  ""});
			records.add(new String[]{"SC Fee", String.valueOf(scholasticChallengeFeeAmountPerct)+"%", "", "", "",  ""});
			records.add(new String[]{"Monthly payout ", "75%", "", "", "",  ""});
			records.add(new String[]{"", "", "", "", "",  ""});

			String[] string =  new String[100];
			string[0]="Collections Summary";

















			/********Show Month Between Start and end*********/
			Date date3=null;
			Calendar cal2 = Calendar.getInstance();
			//cal2.setTime();

			System.out.println("------diffMonthBetweenStartAndEndFundraising----"+diffMonthBetweenStartAndEndFundraising);
			date3=new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth(), eventy.fundraisingStart.getDate());
			for( i = 0 ;i<diffMonthBetweenStartAndEndFundraising;i++){
				/*****************start**************************28.01.2016***************/
				//date3 = new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth()+i, eventy.fundraisingStart.getDate());
				System.out.println("new date :: "+date3);
				/*****************end****************************28.01.2016***************/
				cal2.setTime(date3);

				Formatter formatter=new Formatter();
				formatter.format("%tb", cal2);
				System.out.println("-----Formatter----" + formatter);
				//System.out.println(i + "-month");
				payOutTimeAndAllAmountsMap.put(i + "-month", formatter);


				if(date3.getMonth() == 11){
					/*date3 = new Date(eventy.fundraisingStart.getYear()+1,  0, 5);*/
					date3 = new Date(date3.getYear()+1,  0, 5);
				}else {
					date3 = new Date(date3.getYear(), date3.getMonth() + 1, 5);
				}

			}




			/*Date date3=null;
			Calendar cal2 = Calendar.getInstance();
			//cal2.setTime();
			for( i = 0 ;i<diffMonthBetweenStartAndEndFundraising;i++){
				date3=new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth(), eventy.fundraisingStart.getDate());
				cal2.setTime(date3);

				Formatter formatter=new Formatter();
				formatter.format("%tb", cal2);

				payOutTimeAndAllAmountsMap.put(firstPayoutCount + "-month", formatter);


				if(date3.getMonth() == 12){
					date3 = new Date(date3.getYear()+1,  1, 1);
				}else {
					date3 = new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth() + 1, 1);
				}

			}
*/











			string[0]="";

			for(int i1=0;i1<diffMonthBetweenStartAndEndFundraising;i1++){
				System.out.println(i1 + "-month");
				String element=payOutTimeAndAllAmountsMap.get(i1 + "-month").toString();
				System.out.println("----------element--------------"+element);
				string[i1+1]=element;
			}


/*

			for(int i1=1;i1<=firstPayoutCount;i1++){
				String element=payOutTimeAndAllAmountsMap.get(i1 + "-month").toString();
				string[i1]=element;
			}*/

			records.add(string);
			//records.add(new String[]{"",string});

			//String[] both = (String[])ArrayUtils.addAll(first, second);
			//records.add("",string);
			records.add(new String[]{"", "", "", "", "",  ""});


			String[] totalCheckDonations =  new String[100];
			totalCheckDonations[0] = "Cheque";

			String[]  totalCreditDonations = new String[100];
			totalCreditDonations[0] = "Credit Card";

			String[]  grossIncomeDonations = new String[100];
			grossIncomeDonations[0] = "Gross Income";


			String[] scFee = new String[100];
			scFee[0] = "SC Fee";


			String[] disbursibleAmount = new String[100];
			disbursibleAmount[0] = "Disbursible Amount";

			double lastMonthNetIncome=0;

			for(int i1=1;i1<=firstPayoutCount;i1++){
				//i1 payout will be shown
				System.out.println("-----------------------------------i1------------------------------------"+i1);
				System.out.println("totalCreditAmountNo::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAmountNo"));
				System.out.println("totalCheckAmountNo::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAmountNo"));
				System.out.println("totalDonationsAmount::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalDonationsAmount"));
				System.out.println("totalCreditAccNo::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAccNo"));
				System.out.println("totalCheckAccNo::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAccNo"));
				System.out.println("totalDonationsAccNo::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalDonationsAccNo"));
				System.out.println("totalCreditDonationsPerct::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditDonationsPerct"));
				System.out.println("totalCheckDonationsPerct::"+payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckDonationsPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditDonationsAccNoPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckDonationsAccNoPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAmountNoForSponsor"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAmountNoForSponsor"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalSponsorAmount"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAccNoForSponsor"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAccNoForSponsor"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalSponsorsAccNo"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditSponsorsPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckSponsorsPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditSponsorsAccNoPerct"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckSponsorsAccNoPerct"));
				System.out.println("grossIncomeAmount::"+payOutTimeAndAllAmountsMap.get(i1 + "-grossIncomeAmount"));
				System.out.println("netIncomeAmount::"+payOutTimeAndAllAmountsMap.get(i1 + "-netIncomeAmount"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-scholasticChallengeFeeAmount"));
				System.out.println(payOutTimeAndAllAmountsMap.get(i1 + "-netAmount"));
				double totalCreditAmounts = (double)payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAmountNoForSponsor")+(double)payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAmountNo");
				/*totalCheckDonations[i1] =String.valueOf(Double.parseDouble(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAmountNoForSponsor")+"") + Double.parseDouble(payOutTimeAndAllAmountsMap.get(i1 + "-totalCheckAccNo")+"")) ;*/


				totalCheckDonations[i1] =String.valueOf(totalCreditAmounts) ;
				double totalChequeAmounts = (double)payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAmountNo")+(double)payOutTimeAndAllAmountsMap.get(i1 + "-totalCreditAmountNoForSponsor");
				totalCreditDonations[i1] = String.valueOf(totalChequeAmounts);
				grossIncomeDonations[i1] = String.valueOf((double)payOutTimeAndAllAmountsMap.get(i1 + "-grossIncomeAmount"));
				scFee[i1] = String.valueOf((double)payOutTimeAndAllAmountsMap.get(i1 + "-scholasticChallengeFeeAmount"));
				disbursibleAmount[i1] = String.valueOf((double)payOutTimeAndAllAmountsMap.get(i1 + "-netIncomeAmount"));

				//records.add(new String[]{"Balance Due", " ", "($" + remainingAmount + ")", "100%", "100%", remainingAmountPercent + "%"});
				if(i1 == firstPayoutCount){
					System.out.println("netIncomeAmount for last month ::"+payOutTimeAndAllAmountsMap.get(i1 + "-netIncomeAmount"));
					lastMonthNetIncome = (double)payOutTimeAndAllAmountsMap.get(i1 + "-netIncomeAmount");
				}

			}

			records.add(totalCheckDonations);
			records.add(totalCreditDonations);
			records.add(grossIncomeDonations);
			records.add(scFee);
			records.add(disbursibleAmount);
			localCalendar = Calendar.getInstance(TimeZone.getDefault());

			currentDayOfMonth = eventy.fundraisingEnd.getDate();
			//currentDayOfMonth = localCalendar.get(Calendar.DAY_OF_MONTH);
			records.add(new String[]{"column1", "payout", "Withheld amount", "", "",  ""});




			if(currentDayOfMonth==5 || currentDayOfMonth>5){
				System.out.println("within current day of month 11 :: "+currentDayOfMonth);
				double totalRemainingAmount = 0;
				for(int i1=1;i1<firstPayoutCount;i1++){
					//i1 payout will be shown

					double paidAmount1 = (double)payOutTimeAndPaidAmountMap.get(i1);
					System.out.println("paidAmount1----"+paidAmount1);
					double remainingPayableAmount1 = (double)payOutTimeAndPayableAmountMap.get(i1);
					//reconciliationAmount
					System.out.println("remainingPayableAmount1890 "+remainingPayableAmount1);
					totalRemainingAmount = totalRemainingAmount + remainingPayableAmount1;
					reconciliationAmount = reconciliationAmount+remainingPayableAmount1;
					System.out.println("reconciliationAmount890 " + reconciliationAmount);
					System.out.println("total remaining amount :: " + totalRemainingAmount);


					/*records.add(new String[]{String.valueOf(i1), String.valueOf((double) payOutTimeAndPaidAmountMap.get(i1)), String.valueOf((double) reconciliationAmount), "", "", ""});*/
					records.add(new String[]{String.valueOf(i1), String.valueOf((double) payOutTimeAndPaidAmountMap.get(i1)), String.valueOf((double) remainingPayableAmount1), "", "", ""});


				}
				totalRemainingAmount = totalRemainingAmount+lastMonthNetIncome;

				records.add(new String[]{"Final payout",String.valueOf(totalRemainingAmount)});
			}else{


				if(currentDayOfMonth==eventy.fundraisingStart.getDate()){
					double totalRemainingAmount = 0;
					for(int i1=1;i1<=firstPayoutCount-1;i1++){


						double paidAmount1 = (double)payOutTimeAndPaidAmountMap.get(i1);
						System.out.println("paidAmount1----"+paidAmount1);
						double remainingPayableAmount1 = (double)payOutTimeAndPayableAmountMap.get(i1);
						//reconciliationAmount
						System.out.println("remainingPayableAmount2890 "+remainingPayableAmount1);
						totalRemainingAmount = totalRemainingAmount + remainingPayableAmount1;
						reconciliationAmount = reconciliationAmount+remainingPayableAmount1;
						System.out.println("reconciliationAmount3890 "+reconciliationAmount);



						records.add(new String[]{String.valueOf(i1), String.valueOf((double)payOutTimeAndPaidAmountMap.get(i1)),String.valueOf((double)remainingPayableAmount1) , "", "",  ""});


					}
					totalRemainingAmount = totalRemainingAmount+lastMonthNetIncome;
					records.add(new String[]{"Final payout",String.valueOf(totalRemainingAmount)});
				}
				else{
					double totalRemainingAmount = 0;
					for(int i1=1;i1<firstPayoutCount;i1++){


						double paidAmount1 = (double)payOutTimeAndPaidAmountMap.get(i1);
						System.out.println("paidAmount115634----"+paidAmount1);

						//i1 payout will be shown

						double remainingPayableAmount1 = (double)payOutTimeAndPayableAmountMap.get(i1);
						//reconciliationAmount


						System.out.println("remainingPayableAmount15634"+remainingPayableAmount1);
						totalRemainingAmount = totalRemainingAmount + remainingPayableAmount1;
						System.out.println("reconciliationAmount5634"+reconciliationAmount);
						reconciliationAmount = reconciliationAmount+remainingPayableAmount1;

						records.add(new String[]{String.valueOf(i1), String.valueOf((double)payOutTimeAndPaidAmountMap.get(i1)),String.valueOf((double)remainingPayableAmount1) , "", "",  ""});
					}
					totalRemainingAmount = totalRemainingAmount+lastMonthNetIncome;
					records.add(new String[]{"Final payout",String.valueOf(totalRemainingAmount)});
				}

			}


			if(eventy.fundraisingEnd.getDate()>=5){

				//total logic
				// for donations variable
				double totalCreditAmountNo = 0.0;
				double totalCheckAmountNo = 0.0;
				totalCreditAccNo = 0;
				totalCheckAccNo = 0;
				double totalDonationsAmount = 0.0;
				double totalDonationsAccNo = 0.0;

				//for sponsors variable
				double totalCreditAmountNoForSponsor = 0.0;
				double totalCheckAmountNoForSponsor = 0.0;
				double totalCreditAccNoForSponsor = 0.0;
				double totalCheckAccNoForSponsor = 0.0;
				double totalSponsorAmount = 0.0;
				double totalSponsorsAccNo = 0.0;





				Calendar cal = Calendar.getInstance();
				System.out.println("------------@@@@@@" + eventy.fundraisingStart);

				//Date date
				cal.setTime(eventy.fundraisingEnd);




				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
				Date eachMonthFirstDay = cal.getTime();
				System.out.println("---eachMonthFirstDay1----" + eachMonthFirstDay);
				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
				Date eachMonthLastDay = eventy.fundraisingEnd;
				System.out.println("---eachMonthLastDay1---" + eachMonthLastDay);


				if (donations != null && donations.size() > 0) {


					for (Donation donation : donations) {
						if(donation !=null){
							if ((donation.dateCreated.equals(eachMonthFirstDay) || donation.dateCreated.after(eachMonthFirstDay)) && (donation.dateCreated.before(eachMonthLastDay) || donation.dateCreated.equals(eachMonthLastDay))) {
								//total donations calculation for i where i
								if (donation.donationType != Donation.DonationType.SPONSOR) {
									if (donation.paymentType == Donation.PaymentType.CREDIT) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCreditAmountNo = totalCreditAmountNo + donation.amount;
										//totalCreditAmount = String.valueOf(totalCreditAmountNo);
										totalCreditAccNo = totalCreditAccNo + 1;
										//totalCreditAcc = String.valueOf(totalCreditAccNo);

									} else if (donation.paymentType == Donation.PaymentType.CHECK) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCheckAmountNo = totalCheckAmountNo + donation.amount;
										//totalCheckAmount = String.valueOf(totalCheckAmountNo);
										totalCheckAccNo = totalCheckAccNo + 1;
										//totalCheckAcc = String.valueOf(totalCheckAccNo);

									}
								}
								if (donation.donationType == Donation.DonationType.SPONSOR) {
									if (donation.paymentType == Donation.PaymentType.CREDIT) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCreditAmountNoForSponsor = totalCreditAmountNoForSponsor + donation.amount;
										//totalCreditAmount = String.valueOf(totalCreditAmountNo);
										totalCreditAccNoForSponsor = totalCreditAccNoForSponsor + 1;
										//totalCreditAcc = String.valueOf(totalCreditAccNo);

									} else if (donation.paymentType == Donation.PaymentType.CHECK) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCheckAmountNoForSponsor = totalCheckAmountNoForSponsor + donation.amount;
										//totalCheckAmount = String.valueOf(totalCheckAmountNo);
										totalCheckAccNoForSponsor = totalCheckAccNoForSponsor + 1;
										//totalCheckAcc = String.valueOf(totalCheckAccNo);

									}
								}


							}
						}
					}

				}



				double  withheldAmount=totalCreditAmountNo+totalCheckAmountNo+totalCheckAmountNoForSponsor+totalCreditAmountNoForSponsor;
				withheldAmount=withheldAmount-((withheldAmount*scholasticChallengeFeeAmountPerct)/100);

				//***28.01.2016***records.add(new String[]{"Final Reconciliation ", String.valueOf((double)withheldAmount+(double)reconciliationAmount),"" , "", "",  ""});











			}



			else{


				double totalCreditAmountNo = 0.0;
				double totalCheckAmountNo = 0.0;
				totalCreditAccNo = 0;
				totalCheckAccNo = 0;
				double totalDonationsAmount = 0.0;
				double totalDonationsAccNo = 0.0;

				//for sponsors variable
				double totalCreditAmountNoForSponsor = 0.0;
				double totalCheckAmountNoForSponsor = 0.0;
				double totalCreditAccNoForSponsor = 0.0;
				double totalCheckAccNoForSponsor = 0.0;
				double totalSponsorAmount = 0.0;
				double totalSponsorsAccNo = 0.0;





				Calendar cal = Calendar.getInstance();
				System.out.println("------------@@@@@@" + eventy.fundraisingStart);

				//Date date
				Date startDateCheck=new Date();

				//date2=new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth(), eventy.fundraisingStart.getDate());
				if(eventy.fundraisingEnd.getMonth() == 1){
					startDateCheck = new Date(eventy.fundraisingStart.getYear()-1,  12, 1);
				}else {
					startDateCheck = new Date(eventy.fundraisingStart.getYear(), eventy.fundraisingStart.getMonth() - 1, 1);
				}









				cal.setTime(startDateCheck);




				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
				Date eachMonthFirstDay = cal.getTime();
				System.out.println("---eachMonthFirstDay2----" + eachMonthFirstDay);
				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
				Date eachMonthLastDay = eventy.fundraisingEnd;
				System.out.println("---eachMonthLastDay2---" + eachMonthLastDay);


				if (donations != null && donations.size() > 0) {


					for (Donation donation : donations) {
						if(donation !=null){
							if ((donation.dateCreated.equals(eachMonthFirstDay) || donation.dateCreated.after(eachMonthFirstDay)) && (donation.dateCreated.before(eachMonthLastDay) || donation.dateCreated.equals(eachMonthLastDay))) {
								//total donations calculation for i where i
								if (donation.donationType != Donation.DonationType.SPONSOR) {
									if (donation.paymentType == Donation.PaymentType.CREDIT) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCreditAmountNo = totalCreditAmountNo + donation.amount;
										//totalCreditAmount = String.valueOf(totalCreditAmountNo);
										totalCreditAccNo = totalCreditAccNo + 1;
										//totalCreditAcc = String.valueOf(totalCreditAccNo);

									} else if (donation.paymentType == Donation.PaymentType.CHECK) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCheckAmountNo = totalCheckAmountNo + donation.amount;
										//totalCheckAmount = String.valueOf(totalCheckAmountNo);
										totalCheckAccNo = totalCheckAccNo + 1;
										//totalCheckAcc = String.valueOf(totalCheckAccNo);

									}
								}
								if (donation.donationType == Donation.DonationType.SPONSOR) {
									if (donation.paymentType == Donation.PaymentType.CREDIT) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCreditAmountNoForSponsor = totalCreditAmountNoForSponsor + donation.amount;
										//totalCreditAmount = String.valueOf(totalCreditAmountNo);
										totalCreditAccNoForSponsor = totalCreditAccNoForSponsor + 1;
										//totalCreditAcc = String.valueOf(totalCreditAccNo);

									} else if (donation.paymentType == Donation.PaymentType.CHECK) {
										//rowHeadervalue = "Paid By Credit Card";
										totalCheckAmountNoForSponsor = totalCheckAmountNoForSponsor + donation.amount;
										//totalCheckAmount = String.valueOf(totalCheckAmountNo);
										totalCheckAccNoForSponsor = totalCheckAccNoForSponsor + 1;
										//totalCheckAcc = String.valueOf(totalCheckAccNo);

									}
								}


							}
						}
					}

				}



				double  withheldAmount=totalCreditAmountNo+totalCheckAmountNo+totalCheckAmountNoForSponsor+totalCreditAmountNoForSponsor;
				withheldAmount=withheldAmount-((withheldAmount*scholasticChallengeFeeAmountPerct)/100);

				//***28.01.2016***records.add(new String[]{"Final Reconciliation ", String.valueOf((double)withheldAmount+(double)reconciliationAmount),"" , "", "",  ""});















			}





















		}




















		//----rimi-----end----------------21.01.2016-------------------------------------------------//




		//diff between month of current date and fundraising start month


		///int diffMonth = diffYear * 12 + endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);
		return records;
	}























	/*******end*******27.01.2016*******************************************/
	/**
	 * Participant  report (added Suvadeep Datta).
	 *
	 * @return the result
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
			@Group(SecurityRole.EVENT_ASSIST) })
	@SubjectPresent(content = "/login")
	public static Result participantReport(String event) throws IOException {
		Event eventy=Event.findBySlug(event);
		String sql =
				"select	 pfp.Event, pfp.Name as 'Participant Name', " +
						"pfp.AccountOwner as 'Account Owner', pfp.AccountEmail as ' Account Owner Email'," +
						"pfp.Team,count(*) as'Number of Donation'," +
						"sum(amount) as'Total amount Raised',avg(amount) as'Average amount Raised'," +
						"cast(pfp.date_created as Char) as 'Page Create Date' from donation	join " +
						"(select id,(select distinct name from event where id= event_id) as 'Event'," +
						"name,date_created,(select distinct concat(first_name,' ',last_name)" +
						" from users where id=user_admin_id) as 'AccountOwner'," +
						"(select distinct email	from users where id=user_admin_id) as 'AccountEmail',"+
				"(select distinct name from team where id=team_id) as 'Team'" +
						"from pfp where pfp_type=1 and event_id=:id) pfp where donation.pfp_id =pfp.id " +
						"and donation.status=2 group by pfp.id ";
		SqlQuery bug = Ebean.createSqlQuery(sql)
				.setParameter("id", eventy.id);
		List<SqlRow> list = bug.findList();
    	JsonNode myJsonNode = Json.toJson(list);
		return ok(myJsonNode);

	}
	/**
	 * Donation  report (added Suvadeep Datta).
	 *
	 * @return the result
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
			@Group(SecurityRole.EVENT_ASSIST) })
	@SubjectPresent(content = "/login")
	public static Result donationReport(String event) throws IOException {
		Event eventy=Event.findBySlug(event);
		String sql =
				"select	pfp.Team,pfp.name as 'Participant Name' ,pfp.AccountOwner as 'Account Owner'," +
						"amount as 'Donation Amount'," +
						"first_name as 'Donor First Name',last_name as 'Donor Last Name',email as 'Donor Email'," +
						"phone as 'Donor Phone',cast(date_created as Char) as 'Date Created',cast(date_paid as Char) as 'Date Paid'," +
						"case payment_type when 1 then 'credit' when 2 then 'check' when 3 then 'cash' end as 'payment_type'," +
						"transaction_number	from donation	join " +
						"(select id,name,(select distinct concat(first_name,' ',last_name)" +
						"from users where id=user_admin_id) as 'AccountOwner'," +
						"(select distinct name from team where id=team_id) as 'Team'" +
						"from pfp where pfp_type=1 and event_id=:id) pfp	where donation.pfp_id =pfp.id " +
						"and donation.status=2 " +
						" union all "+
						"select	'','','<b>TOTAL : </b>',sum(amount),'','','','','','','','' from donation	join " +
						"(select id,name,(select distinct concat(first_name,' ',last_name) " +
						"from users where id=user_admin_id) as 'AccountOwner', " +
						"(select distinct name from team where id=team_id) as 'Team' " +
						"from pfp where pfp_type=1 and event_id=:id) pfp where donation.pfp_id =pfp.id " +
						"and donation.status=2"; // all cleared  donation
		SqlQuery bug = Ebean.createSqlQuery(sql)
				.setParameter("id", eventy.id);
		List<SqlRow> list = bug.findList();
		JsonNode myJsonNode = Json.toJson(list);
		return ok(myJsonNode);
	}
	/**
	 * To string array.
	 *
	 * @param volunteers (added Suvadeep Datta)
	 *            the donations
	 * @return the list
	 */




	/*********start*****Reconciliation report**************19.01.2016***********************/
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
			@Group(SecurityRole.EVENT_ASSIST) })
	@SubjectPresent(content = "/login")
	public static Result reconciliationReport(Event event) throws IOException {
		System.out.println("event.slug within reconciliationReport "+event.id);
		Event event1 = Event.findById(event.id);
		System.out.println("event.slug within reconciliationReport "+event1.slug);

	/*	Map<String, String> requestData = Form.form().bindFromRequest().data();
		if(StringUtils.isEmpty(requestData.get("paymentType"))) {
			requestData.remove("paymentType");
		}
		if(StringUtils.isEmpty(requestData.get("status"))) {
			requestData.remove("status");
		}
		if(StringUtils.isEmpty(requestData.get("fromDate")) || !DateUtils.parseDate(requestData.get("fromDate")).isDefined()) {
			requestData.remove("fromDate");
		}
		if(StringUtils.isEmpty(requestData.get("toDate")) || !DateUtils.parseDate(requestData.get("toDate")).isDefined()) {
			requestData.remove("toDate");
		}*/
		Map<String, String> requestData = Form.form().bindFromRequest().data();
		if(StringUtils.isEmpty(requestData.get("pfpType"))) {
			requestData.remove("pfpType");
		}
		if(StringUtils.isEmpty(requestData.get("fromDate")) || !DateUtils.parseDate(requestData.get("fromDate")).isDefined()) {
			requestData.remove("fromDate");
		}
		if(StringUtils.isEmpty(requestData.get("toDate")) || !DateUtils.parseDate(requestData.get("toDate")).isDefined()) {
			requestData.remove("toDate");
		}
		Event eventy=Event.findBySlug(event1.slug);
		List<Donation> donations = Donation.findAllByEventIdAndOptionsForReconcile(event.id, requestData);
	/*	List<Donation> donations = Donation.findByEventId(eventy.id);*/
		/*String sql ="select pfp.Team,pfp.name as 'Participant Name' ,pfp.AccountOwner as 'Account Owner',amount as 'Donation Amount',first_name as 'Donor First Name'," +
				"last_name as 'Donor Last Name',email as 'Donor Email',phone as 'Donor Phone',cast(date_created as Char) as 'Date Created',cast(date_paid as Char) as 'Date Paid',case payment_type when 1 then 'credit' when 2 then 'check' when 3 then 'cash' end as 'payment_type'," +
				"case status when 0 then 'Approved' when 1 then 'Pending' when 2 then 'Cleared' when 3 then 'Refunded' when 4 then 'Failed' end as 'status'," +
				"transaction_number as 'Invoice No',transaction.reason,transaction.transid"+
				"from transaction, donation join (select id,name,(select distinct concat(first_name,' ',last_name) from users where id=user_admin_id) as 'AccountOwner'," +
				"(select distinct name from team where id=team_id) as 'Team' from pfp where pfp_type=1 and event_id=:id) pfp"+
				"where donation.pfp_id =pfp.id " +
				"and transaction.donation_tran_id = donation.transaction_number ";
			 // all cleared  donation
		SqlQuery bug = Ebean.createSqlQuery(sql)
				.setParameter("id", eventy.id);
		List<SqlRow> list = bug.findList();
		JsonNode myJsonNode = Json.toJson(list);
		return ok(myJsonNode);*/


		final File file = new File("yourfile.xls");

		/*final File file = new File("yourfile.csv");*/
		final BufferedWriter out = new BufferedWriter(new FileWriter(file));
		final CSVWriter writer = new CSVWriter(out, ',');
		/*final List<String[]> data =payoutToStringArray(donations,eventy);*/

		final List<String[]> data = reconcileToStringArrayForMonth(donations,eventy);
		writer.writeAll(data);
		writer.close();
		/*response().setHeader("Content-Disposition",
				"attachment; filename=\"ReconciliationReport.csv\"");
		response().setContentType("text/csv");
		return ok(file).as("text/csv");*/


		response().setHeader("Content-Disposition",
				"attachment; filename=\"ReconciliationReport.xls\"");
		response().setContentType("text/xls");
		return ok(file).as("text/xls");


		/*if(StringUtils.isEmpty(requestData.get("paymentType"))) {
			requestData.remove("paymentType");
		}
		if(StringUtils.isEmpty(requestData.get("status"))) {
			requestData.remove("status");
		}
		if(StringUtils.isEmpty(requestData.get("fromDate")) || !DateUtils.parseDate(requestData.get("fromDate")).isDefined()) {
			requestData.remove("fromDate");
		}
		if(StringUtils.isEmpty(requestData.get("toDate")) || !DateUtils.parseDate(requestData.get("toDate")).isDefined()) {
			requestData.remove("toDate");
		}
		List<Donation> donations = Donation.findAllByEventIdAndOptions(event.id, requestData);
		Logger.debug("Donations size {}", donations.size());
		final File file = new File("yourfile.csv");
		final BufferedWriter out = new BufferedWriter(new FileWriter(file));
		final CSVWriter writer = new CSVWriter(out, ',');
		List<String[]> data = null;
		DebugUtils.callout();
		System.out.println("Total by PFP - " + requestData.get("totalByPfp"));
		System.out.println((StringUtils.isNotEmpty(requestData.get("totalByPfp")) && Boolean.getBoolean(requestData.get("totalByPfp"))));
		System.out.println(Boolean.parseBoolean(requestData.get("totalByPfp")));
		DebugUtils.callout();
		if(StringUtils.isNotEmpty(requestData.get("totalByPfp")) && Boolean.parseBoolean(requestData.get("totalByPfp"))) {
			data = donatioTotalToStringArray(donations);
		} else {
			data = donationToStringArray(donations);
		}
		writer.writeAll(data);
		writer.close();
		response().setHeader("Content-Disposition",
				"attachment; filename=\"DonationsReport.csv\"");
		response().setContentType("text/csv");
		return ok(file).as("text/csv");*/
	}
	/**********end******Reconciliation report**************19.01.2016***********************/



	/*********start*****Reconciliation report1**************28.01.2016***********************/
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
			@Group(SecurityRole.EVENT_ASSIST) })
	@SubjectPresent(content = "/login")
	public static Result reconciliationReport1() throws IOException {
		//System.out.println("event.slug within reconciliationReport "+event.id);
		//Event event1 = Event.findById(event.id);
		//System.out.println("event.slug within reconciliationReport "+event1.slug);

	/*	Map<String, String> requestData = Form.form().bindFromRequest().data();
		if(StringUtils.isEmpty(requestData.get("paymentType"))) {
			requestData.remove("paymentType");
		}
		if(StringUtils.isEmpty(requestData.get("status"))) {
			requestData.remove("status");
		}
		if(StringUtils.isEmpty(requestData.get("fromDate")) || !DateUtils.parseDate(requestData.get("fromDate")).isDefined()) {
			requestData.remove("fromDate");
		}
		if(StringUtils.isEmpty(requestData.get("toDate")) || !DateUtils.parseDate(requestData.get("toDate")).isDefined()) {
			requestData.remove("toDate");
		}*/
		Map<String, String> requestData = Form.form().bindFromRequest().data();
	/*	if(StringUtils.isEmpty(requestData.get("pfpType"))) {
			requestData.remove("pfpType");
		}*/
		if(StringUtils.isEmpty(requestData.get("fromDate")) || !DateUtils.parseDate(requestData.get("fromDate")).isDefined()) {
			requestData.remove("fromDate");
		}
		if(StringUtils.isEmpty(requestData.get("toDate")) || !DateUtils.parseDate(requestData.get("toDate")).isDefined()) {
			requestData.remove("toDate");
		}
		//Event eventy=Event.findBySlug(event1.slug);
		List<Donation> donations = Donation.findAllByOnlyOptionsForReconcile(requestData);
	/*	List<Donation> donations = Donation.findByEventId(eventy.id);*/
		/*String sql ="select pfp.Team,pfp.name as 'Participant Name' ,pfp.AccountOwner as 'Account Owner',amount as 'Donation Amount',first_name as 'Donor First Name'," +
				"last_name as 'Donor Last Name',email as 'Donor Email',phone as 'Donor Phone',cast(date_created as Char) as 'Date Created',cast(date_paid as Char) as 'Date Paid',case payment_type when 1 then 'credit' when 2 then 'check' when 3 then 'cash' end as 'payment_type'," +
				"case status when 0 then 'Approved' when 1 then 'Pending' when 2 then 'Cleared' when 3 then 'Refunded' when 4 then 'Failed' end as 'status'," +
				"transaction_number as 'Invoice No',transaction.reason,transaction.transid"+
				"from transaction, donation join (select id,name,(select distinct concat(first_name,' ',last_name) from users where id=user_admin_id) as 'AccountOwner'," +
				"(select distinct name from team where id=team_id) as 'Team' from pfp where pfp_type=1 and event_id=:id) pfp"+
				"where donation.pfp_id =pfp.id " +
				"and transaction.donation_tran_id = donation.transaction_number ";
			 // all cleared  donation
		SqlQuery bug = Ebean.createSqlQuery(sql)
				.setParameter("id", eventy.id);
		List<SqlRow> list = bug.findList();
		JsonNode myJsonNode = Json.toJson(list);
		return ok(myJsonNode);*/


		final File file = new File("yourfile.xls");

		/*final File file = new File("yourfile.csv");*/
		final BufferedWriter out = new BufferedWriter(new FileWriter(file));
		final CSVWriter writer = new CSVWriter(out, ',');
		/*final List<String[]> data =payoutToStringArray(donations,eventy);*/

		final List<String[]> data = reconcileToStringArrayForMonth1(donations);
		writer.writeAll(data);
		writer.close();
		/*response().setHeader("Content-Disposition",
				"attachment; filename=\"ReconciliationReport.csv\"");
		response().setContentType("text/csv");
		return ok(file).as("text/csv");*/


		response().setHeader("Content-Disposition",
				"attachment; filename=\"ReconciliationReport.xls\"");
		response().setContentType("text/xls");
		return ok(file).as("text/xls");


		/*if(StringUtils.isEmpty(requestData.get("paymentType"))) {
			requestData.remove("paymentType");
		}
		if(StringUtils.isEmpty(requestData.get("status"))) {
			requestData.remove("status");
		}
		if(StringUtils.isEmpty(requestData.get("fromDate")) || !DateUtils.parseDate(requestData.get("fromDate")).isDefined()) {
			requestData.remove("fromDate");
		}
		if(StringUtils.isEmpty(requestData.get("toDate")) || !DateUtils.parseDate(requestData.get("toDate")).isDefined()) {
			requestData.remove("toDate");
		}
		List<Donation> donations = Donation.findAllByEventIdAndOptions(event.id, requestData);
		Logger.debug("Donations size {}", donations.size());
		final File file = new File("yourfile.csv");
		final BufferedWriter out = new BufferedWriter(new FileWriter(file));
		final CSVWriter writer = new CSVWriter(out, ',');
		List<String[]> data = null;
		DebugUtils.callout();
		System.out.println("Total by PFP - " + requestData.get("totalByPfp"));
		System.out.println((StringUtils.isNotEmpty(requestData.get("totalByPfp")) && Boolean.getBoolean(requestData.get("totalByPfp"))));
		System.out.println(Boolean.parseBoolean(requestData.get("totalByPfp")));
		DebugUtils.callout();
		if(StringUtils.isNotEmpty(requestData.get("totalByPfp")) && Boolean.parseBoolean(requestData.get("totalByPfp"))) {
			data = donatioTotalToStringArray(donations);
		} else {
			data = donationToStringArray(donations);
		}
		writer.writeAll(data);
		writer.close();
		response().setHeader("Content-Disposition",
				"attachment; filename=\"DonationsReport.csv\"");
		response().setContentType("text/csv");
		return ok(file).as("text/csv");*/
	}
	/**********end******Reconciliation report1**************28.01.2016***********************/
	/*********start*****Payout report**************19.01.2016***********************/
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
			@Group(SecurityRole.EVENT_ASSIST) })
	@SubjectPresent(content = "/login")
	public static Result payoutReport(Event event) throws IOException {
		System.out.println("event.id within payoutReport "+event.id);
		System.out.println("event.name within payoutReport "+event.name);
		System.out.println("event.serviceFee within payoutReport "+event.serviceFee);
		Event event1 = Event.findById(event.id);
		System.out.println("event.slug within payoutReport "+event1.slug);


		//Map<String, String> requestData = Form.form().bindFromRequest().data();


	/*	Map<String, String> requestData = Form.form().bindFromRequest().data();
		if(StringUtils.isEmpty(requestData.get("paymentType"))) {
			requestData.remove("paymentType");
		}
		if(StringUtils.isEmpty(requestData.get("status"))) {
			requestData.remove("status");
		}
		if(StringUtils.isEmpty(requestData.get("fromDate")) || !DateUtils.parseDate(requestData.get("fromDate")).isDefined()) {
			requestData.remove("fromDate");
		}
		if(StringUtils.isEmpty(requestData.get("toDate")) || !DateUtils.parseDate(requestData.get("toDate")).isDefined()) {
			requestData.remove("toDate");
		}*/
		Map<String, String> requestData = Form.form().bindFromRequest().data();
		System.out.println("ID-------"+requestData.get("serviceFee"));
		System.out.println("requestData");
		if(StringUtils.isEmpty(requestData.get("pfpType"))) {
			requestData.remove("pfpType");
		}
		if(StringUtils.isEmpty(requestData.get("fromDate")) || !DateUtils.parseDate(requestData.get("fromDate")).isDefined()) {
			requestData.remove("fromDate");
		}
		if(StringUtils.isEmpty(requestData.get("toDate")) || !DateUtils.parseDate(requestData.get("toDate")).isDefined()) {
			requestData.remove("toDate");
		}
		Event eventy=Event.findBySlug(event1.slug);
		eventy.serviceFee = Double.parseDouble(requestData.get("serviceFee"));
		System.out.println("eventy");
		/*String sql ="select sum(amount) as total_credit_amount from donation where donation.payment_type = '2'";
		// all cleared  donation
		SqlQuery bug = Ebean.createSqlQuery(sql)
				.setParameter("id", eventy.id);
		List<SqlRow> list = bug.findList();
        List<String> totalCreditamountLists = new ArrayList<String>();
		System.out.println("list :: "+list);
		if (CollectionUtils.isNotEmpty(list)) {
			System.out.println("CollectionUtils.isNotEmpty(list)");
			for (SqlRow row : list) {
				System.out.println("row :: "+row);
				int creAmunt= row.getInteger("total_credit_amount");
				System.out.println("creAmunt :: "+creAmunt);
				String creditAmount = String.valueOf(row.getInteger("total_credit_amount"));
				System.out.println("creditAmount :: "+creditAmount);
				totalCreditamountLists.add(creditAmount);

			}
		}*/
 List<Donation> donations = Donation.findByEventId(eventy.id);


		final File file = new File("yourfile.csv");
		final BufferedWriter out = new BufferedWriter(new FileWriter(file));
		final CSVWriter writer = new CSVWriter(out, ',');
		/*final List<String[]> data =payoutToStringArray(donations,eventy);*/

		//final List<String[]> data =payoutToStringArrayForMonth(donations,eventy);
		//final List<String[]> data =payoutToStringArrayForMonth1(donations,eventy);
		final List<String[]> data =payoutToStringArrayForMonth2(donations,eventy);
		writer.writeAll(data);
		writer.close();
		response().setHeader("Content-Disposition",
				"attachment; filename=\"PayoutsReport.csv\"");
		response().setContentType("text/csv");
		return ok(file).as("text/csv");

		/*
		JsonNode myJsonNode = Json.toJson(list);
		return ok(myJsonNode);*/
	}
	/**********end******Payout report**************19.01.2016***********************/


	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
			@Group(SecurityRole.EVENT_ASSIST) })
	@SubjectPresent(content = "/login")
	public static Result volunteersReport(String event) throws IOException {
		Event eventy=Event.findBySlug(event);

		String sql =
				"select CAST(a.date as date) as ' Date of Job ',CAST(a.start_time AS time) as 'START TIME'," +
						"CAST(a.end_time AS time) as 'END TIME',a.name as 'Job Title',v.first_name as 'First Name'," +
						"v.last_name as 'Last Name',v.email,v.phone,v.mobile,null as 'Check In ',null as 'Signature Box '" +
						"from (select id,date,name,start_time,end_time from shift where " +
						"volunteers_id=(select id from volunteers where eventid=:id)) a join volunteer v on v.shift_id=a.id";
		SqlQuery bug = Ebean.createSqlQuery(sql)
				.setParameter("id", eventy.id);
		List<SqlRow> list = bug.findList();
		//List<Volunteers> allvolunteers = Volunteers.findAllVolunteer(event);
		JsonNode myJsonNode = Json.toJson(list);
		return ok(myJsonNode);
	}
	/**
	 * To string array.
	 *
	 * @param sponser (added Suvadeep Datta)
	 *            the donations
	 * @return the list
	 */

	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
			@Group(SecurityRole.EVENT_ASSIST) })
	@SubjectPresent(content = "/login")

	public static Result sponsorReport(String event) throws IOException {
		Event eventy=Event.findBySlug(event);
		String sql =
				"select donor_name sponsor, sum(amount) from donation where donation_type=3 and event_id=:id "+
						"group by id,donor_name order by sum(amount)desc";
		SqlQuery bug = Ebean.createSqlQuery(sql)
				.setParameter("id", eventy.id);
		List<SqlRow> list = bug.findList();
		JsonNode myJsonNode = Json.toJson(list);
		return ok(myJsonNode);


	}


	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
			@Group(SecurityRole.EVENT_ASSIST) })
	@SubjectPresent(content = "/login")
	public static Result donorReport(String event) throws IOException {
		Event eventy=Event.findBySlug(event);
		String sql =
				"select donor_name as 'Donor Name',first_name,last_name,sum(amount) amount from donation " +
						"where event_id=:id and donation_type=1 group by id,donor_name order by sum(amount)desc";
		SqlQuery bug = Ebean.createSqlQuery(sql)
				.setParameter("id", eventy.id);
		List<SqlRow> list = bug.findList();
		JsonNode myJsonNode = Json.toJson(list);
		return ok(myJsonNode);

	}

	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
			@Group(SecurityRole.EVENT_ASSIST) })
	@SubjectPresent(content = "/login")
	public static Result teamReport(String event) throws IOException {
		Event eventy=Event.findBySlug(event);
		String sql ="select  IFNULL(p.team,'<b>EVENT</b>') as team " +
				", IFNULL(p.name, '<b>TOTAL :</b>') as 'participant name',sum(d.amount) as 'amount' from" +
				" (select event_id,pfp_id,sum(amount) as amount from donation where status=2 group by pfp_id ) d ," +
				"(select id,(select distinct name from team where id=team_id) team,team_id ,event_id," +
				"name from pfp where pfp_type=1) p" +
				" where	d.event_id=:id	and p.event_id=d.event_id and d.pfp_id=p.id " +
				"GROUP BY  p.team,p.name,amount with ROLLUP";
		SqlQuery bug = Ebean.createSqlQuery(sql)
				.setParameter("id", eventy.id);
		List<SqlRow> list = bug.findList();
		JsonNode myJsonNode = Json.toJson(list);
		return ok(myJsonNode);


	}

}