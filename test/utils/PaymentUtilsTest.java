package utils;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import models.Donation;
import models.Event;
import models.Pfp;
import models.Donation.DonationType;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.NumberUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.message.BasicNameValuePair;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.H2Platform;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.ddl.DdlGenerator;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import play.Logger;
import base.utils.PaymentUtils;
import play.mvc.*;
import play.test.*;
import play.libs.F.*;
import static play.test.Helpers.*;

public class PaymentUtilsTest {
	
//	static FakeApplication app;
//	static EbeanServer server;
//	static DdlGenerator ddl;
//	
//	@BeforeClass
//	public static void setup() throws IOException {
//	    app = Helpers.fakeApplication(Helpers.inMemoryDatabase());
//	    Helpers.start(app);
//
//	    server = Ebean.getServer("default");
//
//	    ServerConfig config = new ServerConfig();
////	    config.setDebugSql(true);
//
//	    ddl = new DdlGenerator();
//	    ddl.setup((SpiEbeanServer) server, new H2Platform(), config);
//	    
//	    // drop
//	    String dropScript = ddl.generateDropDdl();
//	    ddl.runScript(false, dropScript);
//
//	    // create
//	    String createScript = ddl.generateCreateDdl();
//	    ddl.runScript(false, createScript);
//	}
//
//	@AfterClass
//	public static void stopApp() {
//	    // drop
//	    String dropScript = ddl.generateDropDdl();
//	    ddl.runScript(false, dropScript);
//
//	    Helpers.stop(app);
//	}
	
//	Card Number = 5000300020003003
//					Cvv = 123
//					Zip = 37920
//					Exp Date = 1225
	
	public static FakeApplication app;
	 
	  @BeforeClass
	  public static void startApp() {
	    app = Helpers.fakeApplication(getAppProperties());
	    Helpers.start(app);
	  }
	 
	  @AfterClass
	  public static void stopApp() {
	    Helpers.stop(app);
	  }
	
	@Test
	public void testSendCCPaymentInvalidPin() {
		Map<String, String> gatewayConfig = getAppProperties();
		System.out.println("This is the url - " + gatewayConfig.get(PaymentUtils.GATEWAY_URL));
		gatewayConfig.put(PaymentUtils.GATEWAY_SSL_PIN, "111111");
		Map<String, String> response = PaymentUtils.sendCCPayment(getValidDonation(100), gatewayConfig);
		Map<String, String> errors = PaymentUtils.validateCreditPayment(response);
		assertTrue("There should be errors from the invalid gateway config", MapUtils.isNotEmpty(errors));
		assertTrue("There should be an error for the invalid ssn pin", errors.containsKey("_4015"));
	}
	
	@Test
	public void testSendCCPaymentInvalidUserId() {
		Map<String, String> gatewayConfig = getAppProperties();
		gatewayConfig.put(PaymentUtils.GATEWAY_SSL_USER_ID, "111111");
		Map<String, String> response = PaymentUtils.sendCCPayment(getValidDonation(101), gatewayConfig);
		Map<String, String> errors = PaymentUtils.validateCreditPayment(response);
		assertTrue("There should be errors from the invalid gateway config", MapUtils.isNotEmpty(errors));
		assertTrue("There should be an error for the invalid user id", errors.containsKey("_4001"));
	}
	
	@Test
	public void testSendCCPaymentInvalidMerchantId() {
		Map<String, String> gatewayConfig = getAppProperties();
		gatewayConfig.put(PaymentUtils.GATEWAY_SSL_MERCHANT_ID, "111111");
		Map<String, String> response = PaymentUtils.sendCCPayment(getValidDonation(102), gatewayConfig);
		Map<String, String> errors = PaymentUtils.validateCreditPayment(response);
		assertTrue("There should be errors from the invalid gateway config", MapUtils.isNotEmpty(errors));
		assertTrue("There should be an error for the invalid merchant id", errors.containsKey("_4012"));
	}
	
	@Test
	public void testSendCCPaymentCvvCode() {
		Map<String, String> gatewayConfig = getAppProperties();
		Donation donation = getValidDonation(103);
		donation.ccCvvCode = "111";
		Map<String, String> response = PaymentUtils.sendCCPayment(donation, gatewayConfig);
		Map<String, String> errors = PaymentUtils.validateCreditPayment(response);
		assertTrue("There should be errors from the invalid gateway config", MapUtils.isNotEmpty(errors));
		assertTrue("There should be an error for the invalid cvv code", errors.containsKey("_DECLINED"));
	}	
	
	@Test
	public void testSendCCPaymentInvalidCreditNumber() {
		Map<String, String> gatewayConfig = getAppProperties();
		Donation donation = getValidDonation(104);
		donation.ccNum = "1111111111111111";
		Map<String, String> response = PaymentUtils.sendCCPayment(donation, gatewayConfig);
		Map<String, String> errors = PaymentUtils.validateCreditPayment(response);
		assertTrue("There should be errors from the invalid gateway config", MapUtils.isNotEmpty(errors));
		assertTrue("There should be an error for the invalid ccNum", errors.containsKey("ccNum"));
	}
	
	@Test
	public void testSendCCPaymentInvalidExpDate() {
		Map<String, String> gatewayConfig = getAppProperties();
		Donation donation = getValidDonation(105);
		donation.ccExpDate = new Date();
		Map<String, String> response = PaymentUtils.sendCCPayment(donation, gatewayConfig);
		Map<String, String> errors = PaymentUtils.validateCreditPayment(response);
		assertTrue("There should be errors from the invalid gateway config", MapUtils.isNotEmpty(errors));
		assertTrue("There should be an error for the invalid exp date", errors.containsKey("_DECLINED"));
	}
	
	@Test
	public void testSendCCPaymentInvalidZip() {
		Map<String, String> gatewayConfig = getAppProperties();
		Donation donation = getValidDonation(107);
		donation.ccZip = "99999";
		Map<String, String> response = PaymentUtils.sendCCPayment(donation, gatewayConfig);
		Map<String, String> errors = PaymentUtils.validateCreditPayment(response);
		assertTrue("There should be errors from the invalid gateway config", MapUtils.isNotEmpty(errors));
		printMapKey(1, errors);
		assertTrue("There should be an error for the invalid zip", errors.containsKey("_DECLINED"));
	}
	
	
	@Test
	public void testSendCCRefundDonationMapOfStringString() {
//		fail("Not yet implemented");
	}
	
	
	@Test
	public void testSendCCReconcileDateDateMapOfStringString() {
//		fail("Not yet implemented");
	}
	
//	if (ccResponse.containsKey("errorCode")) {
//		Long errorCode = NumberUtils.createLong(ccResponse.get("errorCode"));
//		if (errorCode == 5001) {
//			Logger.debug("There was an error - 5001");
//			errors.put("ccExpDate", ccResponse.get("errorName"));
//		} else if (errorCode == 5000) {
//			Logger.debug("There was an error - 5000");
//			errors.put("ccNum", ccResponse.get("errorName"));
//		} else if (errorCode == 5002) {
//			Logger.debug("There was an error - 5002");
//			errors.put("amount", ccResponse.get("errorName"));
//		} else if (errorCode == 5021) {
//			Logger.debug("There was an error - 5021");
//			errors.put("ccCvvCode", ccResponse.get("errorName"));
//		} else {
//			Logger.error("Failed to submit transaction to Virtual Merchant for CCNum with error code [{}] and error message [{}]",
//							ccResponse.get("errorCode"), ccResponse.get("errorName"));
//			errors.put("_" + errorCode,
//							"Unable to process donation ["
//											+ ccResponse.get("errorName")
//											+ "], please try your request again.  If the issue persists, please contact Scholastic Challenge.");
//		}
//	}
//	if (ccResponse.containsKey("ssl_result") && StringUtils.equals(ccResponse.get("ssl_result"), "1")) {
	
	@Test
	public void testValidateCreditPayment() {
		Map<String, String> response = getResponse("errorCode", "5001");
		Map<String, String> errors = PaymentUtils.validateCreditPayment(response);
		assertTrue("There should be an error for the ccExpDate", errors.containsKey("ccExpDate"));
		response = getResponse("errorCode", "5000");
		errors = PaymentUtils.validateCreditPayment(response);
		assertTrue("There should be an error for the ccNum", errors.containsKey("ccNum"));
		response = getResponse("errorCode", "5002");
		errors = PaymentUtils.validateCreditPayment(response);
		assertTrue("There should be an error for the amount", errors.containsKey("amount"));
		response = getResponse("errorCode", "5021");
		errors = PaymentUtils.validateCreditPayment(response);
		assertTrue("There should be an error for the ccCvvCode", errors.containsKey("ccCvvCode"));
		response = getResponse("errorCode", "9999");
		errors = PaymentUtils.validateCreditPayment(response);
		assertTrue("There should be an error for the ccCvvCode", errors.containsKey("_9999"));
		response = getResponse("ssl_result", "1");
		response.put("ssl_result_message", "1111");
		errors = PaymentUtils.validateCreditPayment(response);
		assertTrue("There should be an error for the ccCvvCode", errors.containsKey("_1111"));
		response = getResponse("ssl_txn_id", "1");
		errors = PaymentUtils.validateCreditPayment(response);
		assertTrue("There should be no errors from this valid form", MapUtils.isEmpty(errors));
	}
	
	@Test
	public void testValidateCreditForm() {
		Map<String, String> creditForm = getDefaultForm();
		Map<String, String> errors = PaymentUtils.validateCreditForm(creditForm);
		assertTrue("There should be errors from this empty form", MapUtils.isNotEmpty(errors));
		assertTrue("The ccCvvCode should return an error required code", StringUtils.equals("error.required", errors.get("ccCvvCode")));
		assertTrue("The ccExpDate should return an error required code", StringUtils.equals("error.required", errors.get("ccExpDate")));
		assertTrue("The ccNum should return an error required code", StringUtils.equals("error.required", errors.get("ccNum")));
		assertTrue("The ccZip should return an error required code", StringUtils.equals("error.required", errors.get("ccZip")));
		creditForm.put("ccCvvCode", "22");
		errors = PaymentUtils.validateCreditForm(creditForm);
		assertTrue("This ccCvvCode should return a patter error (3 or 4 length)", StringUtils.equals("cvv.pattern", errors.get("ccCvvCode")));
		creditForm.put("ccCvvCode", "22222");
		errors = PaymentUtils.validateCreditForm(creditForm);
		assertTrue("This ccCvvCode should return a patter error (3 or 4 length)", StringUtils.equals("cvv.pattern", errors.get("ccCvvCode")));
		creditForm.put("ccCvvCode", "aaa");
		creditForm.put("ccNum", "aaa");
		errors = PaymentUtils.validateCreditForm(creditForm);
		assertTrue("This ccCvvCode should return a number error (must be numeric)", StringUtils.equals("error.number", errors.get("ccCvvCode")));
		assertTrue("This ccNum should return a number error (must be numeric)", StringUtils.equals("error.number", errors.get("ccNum")));
		creditForm = getValidForm();
		errors = PaymentUtils.validateCreditForm(creditForm);
		assertTrue("There should be no errors from this valid form", MapUtils.isEmpty(errors));
		
	}
	
	private Map<String, String> getResponse(String key, String value) {
		Map<String, String> response = new HashMap<String, String>();
		response.put(key, value);
		return response;
	}
	
	private Map<String, String> getDefaultForm() {
		Map<String, String> creditForm = new HashMap<String, String>();
		creditForm.put("ccCvvCode", "");
		creditForm.put("ccExpDate", "");
		creditForm.put("ccNum", "");
		creditForm.put("ccZip", "");
		return creditForm;
	}
	
	private Map<String, String> getValidForm() {
		Map<String, String> creditForm = new HashMap<String, String>();
		creditForm.put("ccCvvCode", "111");
		creditForm.put("ccExpDate", "1225");
		creditForm.put("ccNum", "1111111111111111");
		creditForm.put("ccZip", "99999");
		return creditForm;
	}
	
	private Donation getValidDonation(int amount) {
		Donation donation = new Donation();
		donation.amount = amount;
		donation.ccCvvCode = "123";
		donation.ccExpDate = new Date(114, 11, 25);
		donation.ccNum = "5000300020003003";
		donation.ccZip = "37920";
		donation.firstName = "Sample";
		donation.lastName = "Sample";
		donation.donationType = DonationType.PFP;
		donation.invoiceNumber = "1111111111111";
		
		Pfp pfp = new Pfp();
		pfp.id = 1L;
		pfp.name = "Sample Pfp";
		donation.pfp = pfp;
		
		Event event = new Event();
		event.name = "Sample Event";
		donation.event = event;
		
		return donation;
	}
	
	public static Map<String, String> getAppProperties() {
		Map<String, String> paymentGateway = new HashMap<String, String>();
		paymentGateway.put(PaymentUtils.GATEWAY_URL, "https://demo.myvirtualmerchant.com/VirtualMerchantDemo/process.do");
		paymentGateway.put(PaymentUtils.GATEWAY_SSL_MERCHANT_ID, "006285");
		paymentGateway.put(PaymentUtils.GATEWAY_SSL_USER_ID, "gateway");
		paymentGateway.put(PaymentUtils.GATEWAY_SSL_PIN, "A21XH0D7ZF7L3OI9310K4R4JBUMJKMKXIIW77F8IT6KHZOT9ZZE9F0CMXOH2MX5H");
		paymentGateway.putAll(Helpers.inMemoryDatabase());
		return paymentGateway;
	}
	
	private void printMapKey(int i, Map<String, String> errors) {
		for(String key: errors.keySet()) {
			System.out.println(i + "- Key - [" + key + "] Value - [" + errors.get(key) + "]");
		}
		System.out.println("-----------------------------");
		System.out.println("-----------------------------");
	}
}
