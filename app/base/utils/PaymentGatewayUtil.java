package base.utils;


import net.authorize.Environment;
import net.authorize.api.contract.v1.*;
import net.authorize.api.controller.CreateTransactionController;
import net.authorize.api.controller.GetSettledBatchListController;
import net.authorize.api.controller.GetTransactionDetailsController;
import net.authorize.api.controller.GetTransactionListController;
import net.authorize.api.controller.base.ApiOperationBase;

import javax.xml.datatype.DatatypeFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by beassananda on 12/1/16.
 */
public class PaymentGatewayUtil {

  //  private static final String API_LOGIN_ID ="5Cdy5N57";//my cred test mode account credential
   // private static final String TRANSACTION_KEY = "726SBegW3a42w4p8";//my test mode account credential
    private static final String API_LOGIN_ID ="43f5zNUB";//scholastic cred test mode account credential
    private static final String TRANSACTION_KEY = "9L9H43Kny8s347z4";//scholastic mode account credential

    private static final Environment environmentType=Environment.PRODUCTION;
   //private static final Environment environmentType=Environment.SANDBOX;





    /*public static String  anotherRefundmethod(String cardNumber,String expDate,String tranNumber,String amount) {
        System.out.println("********* cardNumber"+cardNumber+"expDate"+expDate+"tranNumber"+tranNumber+"amount"+amount);
        String retVal="0";
        Random randomGenerator = new Random();
        int randomSequence = randomGenerator.nextInt(1000);
        // Fingerprint fingerprint = Fingerprint.createFingerprint(API_LOGIN_ID,TRANSACTION_KEY,randomSequence,"6");

        //Common code to set for all requests
        ApiOperationBase.setEnvironment(environmentType);

        MerchantAuthenticationType merchantAuthenticationType  = new MerchantAuthenticationType() ;
        merchantAuthenticationType.setName(API_LOGIN_ID);
        merchantAuthenticationType.setTransactionKey(TRANSACTION_KEY);
        ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);

        // Create a payment object, last 4 of the credit card and expiration date are required
        PaymentType paymentType = new PaymentType();
        CreditCardType creditCard = new CreditCardType();
        creditCard.setCardNumber(cardNumber);
        creditCard.setExpirationDate(expDate);
        paymentType.setCreditCard(creditCard);

        // Create the payment transaction request
        TransactionRequestType txnRequest = new TransactionRequestType();
        txnRequest.setTransactionType(TransactionTypeEnum.REFUND_TRANSACTION.value());
        txnRequest.setRefTransId(tranNumber);
        txnRequest.setAmount(new BigDecimal(amount));
        txnRequest.setPayment(paymentType);

        // Make the API Request
        CreateTransactionRequest apiRequest = new CreateTransactionRequest();
        apiRequest.setTransactionRequest(txnRequest);
        CreateTransactionController controller = new CreateTransactionController(apiRequest);
        controller.execute();

        CreateTransactionResponse response = controller.getApiResponse();


        if (response!=null) {

            // If API Response is ok, go ahead and check the transaction response
            if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {

                TransactionResponse result = response.getTransactionResponse();
                if (result.getResponseCode().equals("1")) {

                    System.out.println(result.getResponseCode());
                    System.out.println("Successfully Refunded Transaction");
                    System.out.println(result.getAuthCode());
                    System.out.println(result.getTransId());
                    retVal="1";
                }
                else
                {
                    System.out.println("Failed Transaction"+result.getResponseCode());
                }
            }
            else
            {
                System.out.println("Failed Transaction:  "+response.getMessages().getResultCode());
                if(!response.getMessages().getMessage().isEmpty())
                    System.out.println("Error: " + response.getMessages().getMessage().get(0).getCode() + "  " + response.getMessages().getMessage().get(0).getText());

                if (response.getTransactionResponse() != null)
                    if(!response.getTransactionResponse().getErrors().getError().isEmpty())
                        System.out.println("Transaction Error : " + response.getTransactionResponse().getErrors().getError().get(0).getErrorCode() + " " + response.getTransactionResponse().getErrors().getError().get(0).getErrorText());
            }
        }
        return retVal;
    }*/



    public static HashMap anotherRefundmethod(String cardNumber,String expDate,String tranNumber,String amount) {
        System.out.println("********* cardNumber"+cardNumber+"expDate"+expDate+"tranNumber"+tranNumber+"amount"+amount);
        String retVal="0";
        Random randomGenerator = new Random();
        int randomSequence = randomGenerator.nextInt(1000);
        HashMap refundHashMap = new HashMap();

        // Fingerprint fingerprint = Fingerprint.createFingerprint(API_LOGIN_ID,TRANSACTION_KEY,randomSequence,"6");

        //Common code to set for all requests
        ApiOperationBase.setEnvironment(environmentType);

        MerchantAuthenticationType merchantAuthenticationType  = new MerchantAuthenticationType() ;
        merchantAuthenticationType.setName(API_LOGIN_ID);
        merchantAuthenticationType.setTransactionKey(TRANSACTION_KEY);
        ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);

        // Create a payment object, last 4 of the credit card and expiration date are required
        PaymentType paymentType = new PaymentType();
        CreditCardType creditCard = new CreditCardType();
        creditCard.setCardNumber(cardNumber);
        creditCard.setExpirationDate(expDate);
        paymentType.setCreditCard(creditCard);

        // Create the payment transaction request
        TransactionRequestType txnRequest = new TransactionRequestType();
        txnRequest.setTransactionType(TransactionTypeEnum.REFUND_TRANSACTION.value());
        txnRequest.setRefTransId(tranNumber);
        txnRequest.setAmount(new BigDecimal(amount));
        txnRequest.setPayment(paymentType);

        // Make the API Request
        CreateTransactionRequest apiRequest = new CreateTransactionRequest();
        apiRequest.setTransactionRequest(txnRequest);
        CreateTransactionController controller = new CreateTransactionController(apiRequest);
        controller.execute();

        CreateTransactionResponse response = controller.getApiResponse();


        if (response!=null) {

            // If API Response is ok, go ahead and check the transaction response
            if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {

                TransactionResponse result = response.getTransactionResponse();
                if (result.getResponseCode().equals("1")) {

                    System.out.println(result.getResponseCode());
                    System.out.println("Successfully Refunded Transaction");
                    System.out.println(result.getAuthCode());
                    System.out.println(result.getTransId());
                    retVal = "1";
                    refundHashMap.put("status", "successful");
                    refundHashMap.put("transactionId", result.getTransId());
                } else {
                    System.out.println("Failed Transaction" + result.getResponseCode());
                    refundHashMap.put("status", "failed");
                }
            } else {
                System.out.println("Failed Transaction:  " + response.getMessages().getResultCode());
                if (!response.getMessages().getMessage().isEmpty()) {
                    System.out.println("Error: " + response.getMessages().getMessage().get(0).getCode() + "  " + response.getMessages().getMessage().get(0).getText());

                }


                if (response.getTransactionResponse() != null) {
                    if (response.getTransactionResponse().getErrors() != null && response.getTransactionResponse().getErrors().getError() != null) {
                        if (!response.getTransactionResponse().getErrors().getError().isEmpty()) {
                            System.out.println("Transaction Error : " + response.getTransactionResponse().getErrors().getError().get(0).getErrorCode() + " " + response.getTransactionResponse().getErrors().getError().get(0).getErrorText());
                        }


                    }

                }
                refundHashMap.put("status", "failed");
            }
        }
        return refundHashMap;
    }




//refund transaction
    public static String refundtrantion(String cardNumber,String expirydate,String transRefID,String amount) {
        String returnValue="";
        //Common code to set for all requests
        ApiOperationBase.setEnvironment(environmentType);

        MerchantAuthenticationType merchantAuthenticationType  = new MerchantAuthenticationType() ;
        merchantAuthenticationType.setName(API_LOGIN_ID);
        merchantAuthenticationType.setTransactionKey(TRANSACTION_KEY);
        ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);

        // Create a payment object, last 4 of the credit card and expiration date are required
        PaymentType paymentType = new PaymentType();
        CreditCardType creditCard = new CreditCardType();

        //value=0015
        creditCard.setCardNumber(cardNumber);
        //value=1220
        creditCard.setExpirationDate(expirydate);
        paymentType.setCreditCard(creditCard);

        // Create the payment transaction request
        TransactionRequestType txnRequest = new TransactionRequestType();
        txnRequest.setTransactionType(TransactionTypeEnum.REFUND_TRANSACTION.value());
        //value=2245803597
        txnRequest.setRefTransId(transRefID);
        //value=1.00
        txnRequest.setAmount(new BigDecimal(amount));
        txnRequest.setPayment(paymentType);

        // Make the API Request
        CreateTransactionRequest apiRequest = new CreateTransactionRequest();
        apiRequest.setTransactionRequest(txnRequest);
        CreateTransactionController controller = new CreateTransactionController(apiRequest);
        controller.execute();

        CreateTransactionResponse response = controller.getApiResponse();


        if (response!=null) {

            // If API Response is ok, go ahead and check the transaction response
            if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {

                TransactionResponse result = response.getTransactionResponse();
                if (result.getResponseCode().equals("1")) {
                    System.out.println(result.getResponseCode());
                    System.out.println("Successfully Refunded Transaction");
                    System.out.println(result.getAuthCode());
                    System.out.println(result.getTransId());
                    returnValue="1";
                }
                else
                {
                    System.out.println("Failed Transaction"+result.getResponseCode());
                    returnValue="2";
                }
            }
            else
            {
                System.out.println("Failed Transaction:  "+response.getMessages().getResultCode());
                if(!response.getMessages().getMessage().isEmpty())
                    System.out.println("Error: " + response.getMessages().getMessage().get(0).getCode() + "  " + response.getMessages().getMessage().get(0).getText());

                if (response.getTransactionResponse() != null)
                    if(!response.getTransactionResponse().getErrors().getError().isEmpty())
                        System.out.println("Transaction Error : " + response.getTransactionResponse().getErrors().getError().get(0).getErrorCode() + " " + response.getTransactionResponse().getErrors().getError().get(0).getErrorText());
                returnValue="3";
            }
        }
        return returnValue ;

    }

    //get transaction list
    /*public static void getTransactionList(String apiLoginId, String transactionKey,String batchId) {
        ApiOperationBase.setEnvironment(environmentType);

        MerchantAuthenticationType merchantAuthenticationType  = new MerchantAuthenticationType() ;
        merchantAuthenticationType.setName(apiLoginId);
        merchantAuthenticationType.setTransactionKey(transactionKey);
        ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);

        //  String batchId = "4594221";

        GetTransactionListRequest getRequest = new GetTransactionListRequest();
        getRequest.setMerchantAuthentication(merchantAuthenticationType);
        getRequest.setBatchId(batchId);

        GetTransactionListController controller = new GetTransactionListController(getRequest);
        controller.execute();

        GetTransactionListResponse getResponse = controller.getApiResponse();
        if (getResponse!=null) {

            if (getResponse.getMessages().getResultCode() == MessageTypeEnum.OK) {
                System.out.println(getResponse.getMessages().getMessage().get(0).getCode());
                System.out.println(getResponse.getMessages().getMessage().get(0).getText());
            }
            else
            {
                System.out.println("Failed to get transaction list1:  " + getResponse.getMessages().getResultCode());
                System.out.println("Failed to get transaction list2:  " + getResponse.getMessages());

                System.out.println("Failed to get transaction list3:  " + getResponse.getMessages().getMessage().toString());

                System.out.println("Failed to get transaction list4:  " + getResponse.getMessages().getResultCode());

            }
        }

    }*/
    public static List<String> getTransactionList(String apiLoginId, String transactionKey,String batchId) {
        System.out.println("within getTransactionList apiLoginId :: "+apiLoginId+" : transactionKey : "+transactionKey+" : batchId : "+batchId);
        ApiOperationBase.setEnvironment(environmentType);

        MerchantAuthenticationType merchantAuthenticationType  = new MerchantAuthenticationType() ;
        merchantAuthenticationType.setName(apiLoginId);
        merchantAuthenticationType.setTransactionKey(transactionKey);
        ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);
         List<String> settledTransIdList = new ArrayList<String>();
        //  String batchId = "4594221";

        GetTransactionListRequest getRequest = new GetTransactionListRequest();
        getRequest.setMerchantAuthentication(merchantAuthenticationType);
        getRequest.setBatchId(batchId);

        GetTransactionListController controller = new GetTransactionListController(getRequest);
        controller.execute();

        GetTransactionListResponse getResponse = controller.getApiResponse();
        if (getResponse!=null) {

            if (getResponse.getMessages().getResultCode() == MessageTypeEnum.OK) {
                System.out.println(getResponse.getMessages().getMessage().get(0).getCode());
                System.out.println(getResponse.getMessages().getMessage().get(0).getText());
                getResponse.getTransactions().getTransaction().get(0).getTransId();
                Iterator iterator =  getResponse.getTransactions().getTransaction().iterator();
               /* getResponse.getTransactions().getTransaction().iterator();*/
                String setteledTransId = null;
                while(iterator.hasNext()){
                    setteledTransId = (String)iterator.next();
                    System.out.println("setteledTransId :: "+setteledTransId);
                    settledTransIdList.add(setteledTransId);
                }
            }
            else
            {
                System.out.println("Failed to get transaction list1:  " + getResponse.getMessages().getResultCode());
                System.out.println("Failed to get transaction list2:  " + getResponse.getMessages());

                System.out.println("Failed to get transaction list3:  " + getResponse.getMessages().getMessage().toString());

                System.out.println("Failed to get transaction list4:  " + getResponse.getMessages().getResultCode());

            }
        }
        return settledTransIdList;

    }
//get batchlist
   /* public static void GetSettledBatchList(String apiLoginId, String transactionKey) {

        ApiOperationBase.setEnvironment(environmentType);

        MerchantAuthenticationType merchantAuthenticationType = new MerchantAuthenticationType();
        merchantAuthenticationType.setName(apiLoginId);
        merchantAuthenticationType.setTransactionKey(transactionKey);
        ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);

        GetSettledBatchListRequest getRequest = new GetSettledBatchListRequest();
        getRequest.setMerchantAuthentication(merchantAuthenticationType);

        try {
            // Set first settlement date in format (year, month, day)(should not be less that 31 days since last settlement date)
            GregorianCalendar pastDate = new GregorianCalendar();
            pastDate.add(Calendar.DAY_OF_YEAR, -7);
            getRequest.setFirstSettlementDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(pastDate));

            // Set last settlement date in format (year, month, day) (should not be greater that 31 days since first settlement date)
            GregorianCalendar currentDate = new GregorianCalendar();
            getRequest.setLastSettlementDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(currentDate));

        } catch (Exception ex) {
            System.out.println("Error : while setting dates");
            ex.printStackTrace();
        }

        GetSettledBatchListController controller = new GetSettledBatchListController(getRequest);
        controller.execute();
        GetSettledBatchListResponse getResponse = controller.getApiResponse();
        if (getResponse != null) {

            if (getResponse.getMessages().getResultCode() == MessageTypeEnum.OK) {

                System.out.println(getResponse.getMessages().getMessage().get(0).getCode());
                System.out.println(getResponse.getMessages().getMessage().get(0).getText());

                ArrayOfBatchDetailsType batchList = getResponse.getBatchList();
                if (batchList != null) {
                    System.out.println("List of Settled Transaction :");
                    for (BatchDetailsType batch : batchList.getBatch()) {
                        System.out.println(batch.getBatchId() + " - " + batch.getMarketType() + " - " + batch.getPaymentMethod() + " - " + batch.getProduct() + " - " + batch.getSettlementState());
                    }
                }
            } else {
                System.out.println("Failed to get settled batch list:  " + getResponse.getMessages().getResultCode());
                System.out.println(getResponse.getMessages().getMessage().get(0).getText());
            }
        }
    }*/

    public static List<String> GetSettledBatchList(String apiLoginId, String transactionKey) {
        System.out.println("within GetSettledBatchList apiLoginId :: "+apiLoginId+" :: transactionKey :: "+transactionKey);
        ApiOperationBase.setEnvironment(environmentType);
        List<String> settledBatchListId = new ArrayList<String>();
        MerchantAuthenticationType merchantAuthenticationType = new MerchantAuthenticationType();
        merchantAuthenticationType.setName(apiLoginId);
        merchantAuthenticationType.setTransactionKey(transactionKey);
        ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);

        GetSettledBatchListRequest getRequest = new GetSettledBatchListRequest();
        getRequest.setMerchantAuthentication(merchantAuthenticationType);

        try {
            // Set first settlement date in format (year, month, day)(should not be less that 31 days since last settlement date)
            GregorianCalendar pastDate = new GregorianCalendar();
            pastDate.add(Calendar.DAY_OF_YEAR, -7);
            getRequest.setFirstSettlementDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(pastDate));

            // Set last settlement date in format (year, month, day) (should not be greater that 31 days since first settlement date)
            GregorianCalendar currentDate = new GregorianCalendar();
            getRequest.setLastSettlementDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(currentDate));

        } catch (Exception ex) {
            System.out.println("Error : while setting dates");
            ex.printStackTrace();
        }

        GetSettledBatchListController controller = new GetSettledBatchListController(getRequest);
        controller.execute();
        GetSettledBatchListResponse getResponse = controller.getApiResponse();
        if (getResponse != null) {

            if (getResponse.getMessages().getResultCode() == MessageTypeEnum.OK) {

                System.out.println(getResponse.getMessages().getMessage().get(0).getCode());
                System.out.println(getResponse.getMessages().getMessage().get(0).getText());

                ArrayOfBatchDetailsType batchList = getResponse.getBatchList();
                if (batchList != null) {
                    System.out.println("List of Settled Transaction :");
                    for (BatchDetailsType batch : batchList.getBatch()) {
                        System.out.println(batch.getBatchId() + " - " + batch.getMarketType() + " - " + batch.getPaymentMethod() + " - " + batch.getProduct() + " - " + batch.getSettlementState());
                        settledBatchListId.add(batch.getBatchId());
                    }
                }
            } else {
                System.out.println("Failed to get settled batch list:  " + getResponse.getMessages().getResultCode());
                System.out.println(getResponse.getMessages().getMessage().get(0).getText());
            }
        }
        return settledBatchListId;
    }

    //get transaction details
    public void transactionDetails(String transId){


        ApiOperationBase.setEnvironment(environmentType);

        // Provide the merchant credentials
        MerchantAuthenticationType merchantAuthenticationType = new MerchantAuthenticationType() ;
        merchantAuthenticationType.setName(API_LOGIN_ID);
        merchantAuthenticationType.setTransactionKey(TRANSACTION_KEY);
        ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);

        // Provide a VALID transaction ID
       // String transId = "2226677139";


        // Make the API Request
        GetTransactionDetailsRequest getRequest = new GetTransactionDetailsRequest();
        getRequest.setMerchantAuthentication(merchantAuthenticationType);
        getRequest.setTransId(transId);
        GetTransactionDetailsController controller = new GetTransactionDetailsController(getRequest);
        controller.execute();

        GetTransactionDetailsResponse apiResponse = controller.getApiResponse();

        if (apiResponse != null) {
            // If API response code is ok (transaction is successful),
            // go ahead and check the transaction response
            if (apiResponse.getMessages().getResultCode() == MessageTypeEnum.OK) {

                System.out.println(apiResponse.getMessages().getMessage().get(0).getCode());
                System.out.println(apiResponse.getMessages().getMessage().get(0).getText());
                System.out.println(apiResponse.getTransaction().getCustomerIP());
                System.out.println(apiResponse.getTransaction().getAuthAmount());

                // Any other required fields can be accessed from the transaction response
                // present in the API response.
                // To access the transaction response, use the getTransactionResponse().
            }
            else
            {
                System.out.println("Failed to get transaction details: " );
                //  apiResponse.getTransactionResponse().getErrors().getError().get(0).getErrorCode() +
                //  apiResponse.getTransactionResponse().getErrors().getError().get(0).getErrorText());
            }
        }
    }

    //get transactionlist in date range
    public static void run(String apiLoginId, String transactionKey) {

        ApiOperationBase.setEnvironment(environmentType);

        MerchantAuthenticationType merchantAuthenticationType = new MerchantAuthenticationType();
        merchantAuthenticationType.setName(apiLoginId);
        merchantAuthenticationType.setTransactionKey(transactionKey);
        ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);

        GetSettledBatchListRequest getRequest = new GetSettledBatchListRequest();
        getRequest.setMerchantAuthentication(merchantAuthenticationType);

        try {
            // Set first settlement date in format (year, month, day)(should not be less that 31 days since last settlement date)
            GregorianCalendar pastDate = new GregorianCalendar();
            pastDate.add(Calendar.DAY_OF_YEAR, -170);
            getRequest.setFirstSettlementDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(pastDate));

            // Set last settlement date in format (year, month, day) (should not be greater that 31 days since first settlement date)
            GregorianCalendar currentDate = new GregorianCalendar();
            currentDate.add(Calendar.DAY_OF_YEAR, -150);
            getRequest.setLastSettlementDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(currentDate));

        } catch (Exception ex) {
            System.out.println("Error : while setting dates");
            ex.printStackTrace();
        }


        GetSettledBatchListController controller = new GetSettledBatchListController(getRequest);
        controller.execute();
        GetSettledBatchListResponse getResponse = controller.getApiResponse();
        if (getResponse != null) {

            if (getResponse.getMessages().getResultCode() == MessageTypeEnum.OK) {

                System.out.println(getResponse.getMessages().getMessage().get(0).getCode());
                System.out.println(getResponse.getMessages().getMessage().get(0).getText());

                ArrayOfBatchDetailsType batchList = getResponse.getBatchList();
                if (batchList != null) {
                    System.out.println("List of Settled Transaction :");
                    for (BatchDetailsType batch : batchList.getBatch()) {
                        System.out.println(batch.getBatchId() + " - " + batch.getMarketType() + " - " + batch.getPaymentMethod() + " - " + batch.getProduct() + " - " + batch.getSettlementState());
                    }
                }
            } else {
                System.out.println("Failed to get settled batch list:  " + getResponse.getMessages().getResultCode());
                System.out.println(getResponse.getMessages().getMessage().get(0).getText());
            }
        }
    }



















    /**********************************************************************************************************************************************/
    public static void main( String[] args )
    {

        if (args.length == 0)
        {
            SelectMethod();
        }
        else if (args.length == 1)
        {
            RunMethod(args[0]);
            return;
        }
        else
        {
            ShowUsage();
        }

        System.out.println("");
        System.out.print("Press <Return> to finish ...");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try{
            int i = Integer.parseInt(br.readLine());
        }catch(Exception ex){
        }

    }

    private static void ShowUsage()
    {
        System.out.println("Usage : java -jar SampleCode.jar [CodeSampleName]");
        System.out.println("");
        System.out.println("Run with no parameter to select a method.  Otherwise pass a method name.");
        System.out.println("");
        System.out.println("Code Sample Names: ");
        ShowMethods();


    }

    private static void SelectMethod()
    {
        System.out.println("Code Sample Names: ");
        System.out.println("");
        ShowMethods();
        System.out.println("");
        System.out.print("Type a sample name & then press <Return> : ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try{
            RunMethod(br.readLine());
        }catch(Exception ex){
            System.out.print("Error no such method");
        }
    }

    private static void ShowMethods()
    {
        System.out.println("    refundtrantion");
        System.out.println("    getTransactionList");
        System.out.println("    getSettledBatchList");

    }

    private static void RunMethod(String methodName)
    {
        // These are default transaction keys.
        // You can create your own keys in seconds by signing up for a sandbox account here: https://developer.authorize.net/sandbox/
        //Update the payedId with which you want to run the sample code
        String payerId 				= "";
        //Update the transactionId with which you want to run the sample code
        String transactionId 		= "";

        switch (methodName) {
            case "refundtrantion":
                refundtrantion("0015","1220","2245803597","1.00");
                break;
            case "getTransactionList":
                getTransactionList(API_LOGIN_ID, TRANSACTION_KEY,"4594221");
                break;
            case "getSettledBatchList":
                GetSettledBatchList (API_LOGIN_ID, TRANSACTION_KEY);
                break;

            default:
                ShowUsage();
                break;
        }
    }


}
