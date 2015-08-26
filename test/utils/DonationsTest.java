package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Donation;
import models.Donation.PaymentStatus;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import play.test.FakeApplication;
import play.test.Helpers;


public class DonationsTest {

	public static FakeApplication app;
	 
	  @BeforeClass
	  public static void startApp() {
		  Map<String, String> settings = new HashMap<String, String>();
		    settings.put("db.default.url", "jdbc:mysql://scdbprod.csrrxcugviuk.us-east-1.rds.amazonaws.com:3306/scdbprod?characterEncoding=UTF-8");
		    settings.put("db.default.driver", "com.mysql.jdbc.Driver");
		    settings.put("db.default.user", "scdbuserprod");
		    settings.put("db.default.password", "scdbpasswordprod");
		    settings.put("db.default.dbname", "scdbprod");
//		    settings.put("db.default.jndiName", "DefaultDS");
	    app = Helpers.fakeApplication(settings);
	    Helpers.start(app);
	  }
	 
	  @AfterClass
	  public static void stopApp() {
	    Helpers.stop(app);
	  }
	private static final Logger LOGGER = LoggerFactory.getLogger(DonationsTest.class);
	private static final Logger DB_LOGGER = LoggerFactory.getLogger("DonationsInDbLogger");
	private static final Logger VM_LOGGER = LoggerFactory.getLogger("DonationsInVmLogger");
	
//	@Test
//	public void testFormatExpDate() {
//		BufferedReader br = null;
//		String line = "";
//		String cvsSplitBy = ",";
//	 
//		try {
//			File csvFile = ResourceUtils.getFile("D:\\Development\\git\\scholastic-copy\\test\\resources\\batchreport.csv");
//			File inDb = ResourceUtils.getFile("D:\\Development\\git\\scholastic-copy\\logs\\indatabase.csv");
//			File inVm = ResourceUtils.getFile("D:\\Development\\git\\scholastic-copy\\logs\\invm.csv");
//			List<String> inDbLines = new ArrayList<String>();
//			List<String> inVmLines = new ArrayList<String>();
//			br = new BufferedReader(new FileReader(csvFile));
//			int count = 1;
//			while ((line = br.readLine()) != null) {
//	 
//			        // use comma as separator
//				String[] donation = line.split(cvsSplitBy);
//	 
//				if(Donation.existsByTransactionNumber(donation[4])) {
//					inDbLines.add(Arrays.deepToString(donation));
//					System.out.println(count + " - Writing to db lines - " + donation[4]);	
//				} else {
//					inVmLines.add(Arrays.deepToString(donation));
//					System.out.println(count + " - Writing to vm lines - " + donation[4]);
//				}
//				
//	 
//				count = count + 1;
//			}
//			FileUtils.writeLines(inDb, inDbLines);
//			FileUtils.writeLines(inVm, inVmLines);
//	 
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			if (br != null) {
//				try {
//					br.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//	 
//		System.out.println("Done");
//	  }

//	@Test
//	public void testReconcile() {
//		BufferedReader br = null;
//		String line = "";
//		String cvsSplitBy = ",";
//	 
//		try {
//			File csvFile = ResourceUtils.getFile("D:\\Development\\git\\scholastic-copy\\test\\resources\\indatabase-complete-500-end.csv");
//			br = new BufferedReader(new FileReader(csvFile));
//			int count = 1;
//			while ((line = br.readLine()) != null) {
//	 
//			        // use comma as separator
//				String[] vmDonation = line.split(cvsSplitBy);
//				String transNumber = StringUtils.trim(vmDonation[4]);
//				String transStatus = StringUtils.trim(vmDonation[21]);
//				LOGGER.info(transNumber + " and status of " + transStatus);
//				if(Donation.existsByTransactionNumber(transNumber)) {
//					Donation donation = Donation.findByTransactionNumber(transNumber);
//						if(StringUtils.equalsIgnoreCase(transStatus, "Sale")) {
//							if(donation.status == PaymentStatus.APPROVED) {
//								LOGGER.info(count + " - Record is being updated with a Sale to our db - " + donation.transactionNumber);
//								donation.status = PaymentStatus.CLEARED;
//								donation.datePaid = new Date();
//								donation.update();
//							} else {
//								System.out.println(count + " - Record has already been updated with a Sale to our db - " + donation.transactionNumber);
//							}
//						}
//						else if(StringUtils.equalsIgnoreCase(transStatus, "Void") || StringUtils.equalsIgnoreCase(transStatus, "Return")) {
//							if(donation.status != PaymentStatus.REFUNDED) {
//								LOGGER.info(count + " - Record is being updated with a Void/Return to our db - " + donation.transactionNumber);
//								donation.status = PaymentStatus.REFUNDED;
//								donation.datePaid = new Date();
//								donation.update();
//							} else {
//								LOGGER.info(count + " - Record has already been updated with a Void/Return to our db - " + donation.transactionNumber);
//							}
//						}
//						else {
//							LOGGER.info("Nothing happened");
//						}
//				} else {
//					LOGGER.info(count + " - Record is not being updated or is in vm only - " + transNumber);
//				}
//				
//	 
//				count = count + 1;
//			}
//	 
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			if (br != null) {
//				try {
//					br.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//	 
//		LOGGER.info("Done");
//	  }

//	@Test
//	public void testForDuplicates() {
//		BufferedReader br = null;
//		String line = "";
//		String cvsSplitBy = ",";
//	 
//		try {
//			File csvFile = ResourceUtils.getFile("D:\\Development\\git\\scholastic-copy\\test\\resources\\batchreport.csv");
//			List<String> transNum = new ArrayList<String>();
//			br = new BufferedReader(new FileReader(csvFile));
//			int count = 1;
//			while ((line = br.readLine()) != null) {
//	 
//			        // use comma as separator
//				String[] donation = line.split(cvsSplitBy);
//	 
//				if(Donation.existsByTransactionNumber(donation[4])) { 
//					if(transNum.contains(donation[4])) {
//						System.out.println("This transaction number is duplicated in the file - " + donation[4]);
//					} else {
//						transNum.add(donation[4]);
//					}
//				}
//				
//	 
//				count = count + 1;
//			}
//	 
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			if (br != null) {
//				try {
//					br.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//	 
//		System.out.println("Done");
//	  }
}
