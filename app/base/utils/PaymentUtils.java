package base.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Donation;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import play.Logger;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import play.libs.F;
import play.libs.ws.*;
import play.libs.F.Function;
import play.libs.F.Promise;

public class PaymentUtils {
	
	public static final String	GATEWAY_SSL_PIN			= "gateway.ssl.pin";
	public static final String	GATEWAY_SSL_USER_ID		= "gateway.ssl.userId";
	public static final String	GATEWAY_URL_KEYVAL		= "gateway.url.keyval";
	public static final String	GATEWAY_SSL_MERCHANT_ID	= "gateway.ssl.merchantId";
	public static final String	GATEWAY_URL				= "gateway.url";
	public static Map<String, String> PAYMENT_GATEWAY_CONFIG;
	
	private PaymentUtils() {
	}
	
	static {
		if(PAYMENT_GATEWAY_CONFIG == null) {
			PAYMENT_GATEWAY_CONFIG = new HashMap<String, String>();
			Config conf = ConfigFactory.load();
			PAYMENT_GATEWAY_CONFIG.put(GATEWAY_URL, conf.getString(GATEWAY_URL_KEYVAL));
			PAYMENT_GATEWAY_CONFIG.put(GATEWAY_SSL_MERCHANT_ID, conf.getString(GATEWAY_SSL_MERCHANT_ID));
			PAYMENT_GATEWAY_CONFIG.put(GATEWAY_SSL_USER_ID, conf.getString(GATEWAY_SSL_USER_ID));
			PAYMENT_GATEWAY_CONFIG.put(GATEWAY_SSL_PIN, conf.getString(GATEWAY_SSL_PIN));
		}
	}
	
	public static Map<String, String> sendCCPayment(Donation donation) {
		String gatewaySslMerchantId = PAYMENT_GATEWAY_CONFIG.get(GATEWAY_SSL_MERCHANT_ID);
		String gatewaySslUserId = PAYMENT_GATEWAY_CONFIG.get(GATEWAY_SSL_USER_ID);
		String gatewaySslPin = PAYMENT_GATEWAY_CONFIG.get(GATEWAY_SSL_PIN);

		BufferedReader rd = null;
		Map<String, String> responseProps = new HashMap<String, String>();
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			
			String sslCardNumber = donation.ccNum;
			String sslExpDate = DateUtils.formatExpDate(donation.ccExpDate);
			String sslCvv = donation.ccCvvCode;
			String sslZip = donation.ccZip;

			nameValuePairs.add(new BasicNameValuePair("ssl_transaction_type", "ccsale"));
			nameValuePairs.add(new BasicNameValuePair("ssl_merchant_id", gatewaySslMerchantId));
			nameValuePairs.add(new BasicNameValuePair("ssl_user_id", gatewaySslUserId));
			nameValuePairs.add(new BasicNameValuePair("ssl_pin", gatewaySslPin));
			nameValuePairs.add(new BasicNameValuePair("ssl_show_form", "false"));
			nameValuePairs.add(new BasicNameValuePair("ssl_card_number", sslCardNumber));
			nameValuePairs.add(new BasicNameValuePair("ssl_exp_date", sslExpDate));
			nameValuePairs.add(new BasicNameValuePair("ssl_amount", donation.amount + ""));
			nameValuePairs.add(new BasicNameValuePair("ssl_avs_zip", sslZip));		
			nameValuePairs.add(new BasicNameValuePair("ssl_cvv2cvc2", sslCvv));
			nameValuePairs.add(new BasicNameValuePair("ssl_cvv2cvc2_indicator", "1"));
			nameValuePairs.add(new BasicNameValuePair("ssl_result_format", "ASCII"));
			
			// These are additional fields to help us identify transactions in
			// the VM System
			nameValuePairs.add(new BasicNameValuePair("name", donation.firstName + " " + donation.lastName));
			nameValuePairs.add(new BasicNameValuePair("participant", StringUtils.left(donation.pfp.name, 20)));
			nameValuePairs.add(new BasicNameValuePair("participant_id", donation.pfp.id + ""));
			nameValuePairs.add(new BasicNameValuePair("event", StringUtils.left(donation.event.name, 20)));
			nameValuePairs.add(new BasicNameValuePair("donation_type", donation.donationType.value));
			nameValuePairs.add(new BasicNameValuePair("ssl_invoice_number", donation.invoiceNumber));
			
			String response = PaymentUtils.sendRequest(nameValuePairs).get(60000);
			
			rd = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(response.getBytes())));
			
			String line = "";
			while ((line = rd.readLine()) != null) {
				Logger.debug("Virtual Merchant payment response message - " + line);
				if (StringUtils.contains(line, "META http-equiv")) {
					responseProps.put("errorCode", "9999");
					responseProps.put("errorName", "Invalid data was sent in the credit card request");
					Logger.error("Failed to submit transaction to Virtual Merchant for CCNum [{}] with error code [{}] and error message [{}]",
									donation.ccDigits, responseProps.get("errorCode"), line);
					return responseProps;
				}
				String[] responseLine = line.split("=");
				if (responseLine.length == 2) {
					responseProps.put(responseLine[0], responseLine[1]);
				} else {
					responseProps.put(responseLine[0], "");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(rd);
		}
		return responseProps;
	}
	
	public static Promise<String> sendRequest(List<NameValuePair> nameValuePairs) {
		return WS.url(PAYMENT_GATEWAY_CONFIG.get(GATEWAY_URL)).setContentType("application/x-www-form-urlencoded")
			        .post(URLEncodedUtils.format(nameValuePairs, HTTP.DEF_CONTENT_CHARSET)).map(new Function<WSResponse, String>() {
			            public String apply(WSResponse response) {
			            	return response.getBody();
			            }
			        });
	
	}
	
	public static Map<String, String> sendCCRefund(Donation donation) {
		String gatewaySslMerchantId = PAYMENT_GATEWAY_CONFIG.get(GATEWAY_SSL_MERCHANT_ID);
		String gatewaySslUserId = PAYMENT_GATEWAY_CONFIG.get(GATEWAY_SSL_USER_ID);
		String gatewaySslPin = PAYMENT_GATEWAY_CONFIG.get(GATEWAY_SSL_PIN);
		BufferedReader rd = null;
		Map<String, String> responseProps = new HashMap<String, String>();
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			
			nameValuePairs.add(new BasicNameValuePair("ssl_transaction_type", "ccreturn"));
			nameValuePairs.add(new BasicNameValuePair("ssl_merchant_id", gatewaySslMerchantId));
			nameValuePairs.add(new BasicNameValuePair("ssl_user_id", gatewaySslUserId));
			nameValuePairs.add(new BasicNameValuePair("ssl_pin", gatewaySslPin));
			nameValuePairs.add(new BasicNameValuePair("ssl_show_form", "false"));
			nameValuePairs.add(new BasicNameValuePair("ssl_amount", donation.amount + ""));
			nameValuePairs.add(new BasicNameValuePair("ssl_txn_id", donation.transactionNumber));
			nameValuePairs.add(new BasicNameValuePair("ssl_result_format", "ASCII"));
			
			// These are additional fields to help us identify transactions in
			// the VM System
			nameValuePairs.add(new BasicNameValuePair("name", donation.firstName + " " + donation.lastName));
			nameValuePairs.add(new BasicNameValuePair("participant", StringUtils.left(donation.pfp.name, 20)));
			nameValuePairs.add(new BasicNameValuePair("participant_id", donation.pfp.id + ""));
			nameValuePairs.add(new BasicNameValuePair("event", StringUtils.left(donation.event.name, 20)));
			nameValuePairs.add(new BasicNameValuePair("donation_type", donation.donationType.value));
			nameValuePairs.add(new BasicNameValuePair("ssl_invoice_number", donation.invoiceNumber));
			
			String response = PaymentUtils.sendRequest(nameValuePairs).get(60000);
			
			rd = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(response.getBytes())));
			
			String line = "";
			while ((line = rd.readLine()) != null) {
				Logger.debug("Virtual Merchant return response message - {}", line);
				String[] responseLine = line.split("=");
				if (responseLine.length == 2) {
					responseProps.put(responseLine[0], responseLine[1]);
				} else {
					responseProps.put(responseLine[0], "");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(rd);
		}
		return responseProps;
	}
	
	public static Map<String, Map<String, String>> sendCCReconcile(Date from, Date to) {
		String gatewaySslMerchantId = PAYMENT_GATEWAY_CONFIG.get(GATEWAY_SSL_MERCHANT_ID);
		String gatewaySslUserId = PAYMENT_GATEWAY_CONFIG.get(GATEWAY_SSL_USER_ID);
		String gatewaySslPin = PAYMENT_GATEWAY_CONFIG.get(GATEWAY_SSL_PIN);
		BufferedReader rd = null;
		Map<String, Map<String, String>> responseMap = new HashMap<String, Map<String, String>>();
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			
			nameValuePairs.add(new BasicNameValuePair("ssl_transaction_type", "txnquery"));
			nameValuePairs.add(new BasicNameValuePair("ssl_merchant_id", gatewaySslMerchantId));
			nameValuePairs.add(new BasicNameValuePair("ssl_user_id", gatewaySslUserId));
			nameValuePairs.add(new BasicNameValuePair("ssl_pin", gatewaySslPin));
			nameValuePairs.add(new BasicNameValuePair("ssl_show_form", "false"));
			nameValuePairs.add(new BasicNameValuePair("ssl_search_start_date", DateUtils.formatReconcileDate(from)));
			nameValuePairs.add(new BasicNameValuePair("ssl_search_end_date", DateUtils.formatReconcileDate(to)));
			nameValuePairs.add(new BasicNameValuePair("ssl_result_format", "ASCII"));

			String response = PaymentUtils.sendRequest(nameValuePairs).get(60000);
			
			rd = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(response.getBytes())));
			
			String line = "";
			rd.readLine(); // this reads the txn count
			Logger.debug("The count for the number of transactions: " + rd.readLine());
			Map<String, String> responseProps = null;
			while ((line = rd.readLine()) != null) {
				Logger.debug("Virtual Merchant return response message - {}", line);
				String[] responseLine = line.split("=");
				if (StringUtils.equals(responseLine[0], "ssl_txn_id")) {
					if (responseProps == null) {
						responseProps = new HashMap<String, String>();
					} else {
						responseMap.put(responseProps.get("ssl_txn_id"), responseProps);
						responseProps = new HashMap<String, String>();
					}
				}
				
				if (responseLine.length == 2) {
					responseProps.put(responseLine[0], responseLine[1]);
				} else {
					responseProps.put(responseLine[0], "");
				}
				
			}
			if (!responseProps.isEmpty()) {
				responseMap.put(responseProps.get("ssl_txn_id"), responseProps);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(rd);
		}
		return responseMap;
	}
	
	public static Map<String, String> validateCreditPayment(Map<String, String> ccResponse) {
		Map<String, String> errors = new HashMap<String, String>();
		if (ccResponse.containsKey("errorCode")) {
			Long errorCode = NumberUtils.createLong(ccResponse.get("errorCode"));
			if (errorCode == 5001) {
				Logger.debug("There was an error - 5001");
				errors.put("ccExpDate", ccResponse.get("errorName"));
			} else if (errorCode == 5000) {
				Logger.debug("There was an error - 5000");
				errors.put("ccNum", ccResponse.get("errorName"));
			} else if (errorCode == 5002) {
				Logger.debug("There was an error - 5002");
				errors.put("amount", ccResponse.get("errorName"));
			} else if (errorCode == 5021) {
				Logger.debug("There was an error - 5021");
				errors.put("ccCvvCode", ccResponse.get("errorName"));
			} else {
				Logger.error("Failed to submit transaction to Virtual Merchant for CCNum with error code [{}] and error message [{}]",
								ccResponse.get("errorCode"), ccResponse.get("errorName"));
				errors.put("_" + errorCode,
								"Unable to process donation ["
												+ ccResponse.get("errorName")
												+ "], please try your request again.  If the issue persists, please contact Scholastic Challenge.");
			}
		}
		if (ccResponse.containsKey("ssl_result") && StringUtils.equals(ccResponse.get("ssl_result"), "1")) {
			Logger.error("Failed to submit transaction to Virtual Merchant for CCNum with error code [{}] and error message [{}]",
							ccResponse.get("errorCode"), ccResponse.get("ssl_result_message"));
			errors.put("_" + ccResponse.get("ssl_result_message"),
							"Unable to process donation ["
											+ ccResponse.get("ssl_result_message")
											+ "], please try your request again.  If the issue persists, please contact Scholastic Challenge.");
		}
		return errors;
	}
	
	public static Map<String, String> validateCreditForm(Map<String, String> form) {
		Map<String, String> errors = new HashMap<String, String>();
		if (StringUtils.isEmpty(form.get("ccCvvCode"))) {
			Logger.debug("ccCvvCode is null");
			errors.put("ccCvvCode", "error.required");
		} else {
			if (!NumberUtils.isDigits(form.get("ccCvvCode"))) {
				Logger.debug("ccCvvCode is not a number");
				errors.put("ccCvvCode", "error.number");
			}
			if (!(form.get("ccCvvCode").length() == 3 || form.get("ccCvvCode").length() == 4)) {
				Logger.debug("ccCvvCode is not the right length");
				errors.put("ccCvvCode", "cvv.pattern");
			}
		}
		
		if (StringUtils.isEmpty(form.get("ccExpDate"))) {
			Logger.debug("ccExpDate is null");
			errors.put("ccExpDate", "error.required");
		}
		if (StringUtils.isEmpty(form.get("ccNum"))) {
			Logger.debug("ccNum is null");
			errors.put("ccNum", "error.required");
		} else {
			if (!NumberUtils.isDigits(form.get("ccNum"))) {
				Logger.debug("ccNum is not a number");
				errors.put("ccNum", "error.number");
			}
		}
		if (StringUtils.isEmpty(form.get("ccZip"))) {
			Logger.debug("ccZip is null");
			errors.put("ccZip", "error.required");
		}
		return errors;
	}
}
