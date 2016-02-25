package base.utils;

import controllers.DonationMgmt;
import models.Donation;
import net.authorize.Environment;
import net.authorize.api.contract.v1.*;
import net.authorize.api.controller.CreateTransactionController;
import net.authorize.api.controller.base.ApiOperationBase;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by beassananda on 23/2/16.
 */
public class AuthorisedNetPaymentUtil {


    public static String makeCreditCardPayment(Donation donation,String userId) {


//Common code to set for all requests
        ApiOperationBase.setEnvironment(Environment.SANDBOX);

        MerchantAuthenticationType merchantAuthenticationType  = new MerchantAuthenticationType() ;
        merchantAuthenticationType.setName("347HgbCTh5");
        merchantAuthenticationType.setTransactionKey("8q8a44YT4PUG4j7k");
        ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);

        // Populate the payment data
        PaymentType paymentType = new PaymentType();

        CreditCardType creditCard = new CreditCardType();
        creditCard.setCardNumber(donation.ccNum);///4242424242424242
        creditCard.setExpirationDate(donation.expDate);//0822
        creditCard.setCardCode(donation.ccCvvCode);


        paymentType.setCreditCard(creditCard);

        // Create the payment transaction request
        TransactionRequestType txnRequest = new TransactionRequestType();
        txnRequest.setTransactionType(TransactionTypeEnum.AUTH_CAPTURE_TRANSACTION.value());
        txnRequest.setPayment(paymentType);
        //txnRequest.setCardholderAuthentication(value);
        txnRequest.setAmount(new BigDecimal(Double.parseDouble(donation.amount+"")).setScale(2, RoundingMode.CEILING));

        // Make the API Request
        CreateTransactionRequest apiRequest = new CreateTransactionRequest();
        apiRequest.setTransactionRequest(txnRequest);
        CreateTransactionController controller = new CreateTransactionController(apiRequest);
        controller.execute();


        CreateTransactionResponse response = controller.getApiResponse();
        String msg="";
        String errorCode="";
        Integer code=0;
        if (response!=null) {
            //response.getTransactionResponse().getMessages().getMessage().get(0).getDescription();//correct successful
            response.getTransactionResponse().getResponseCode();

            /** response.getTransactionResponse().getErrors() null for correct
             * else list of errors*/
            response.getTransactionResponse().getErrors();
        /*for(int i =0; response.getMessages().getMessage().size() > i;i++){
        System.out.println();
        }*/
            System.out.println("cvv response ==>"+response.getTransactionResponse().getCvvResultCode());//p
            // all cvv of three digit is taking
            // If API Response is ok, go ahead and check the transaction response
            if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {

                TransactionResponse result = response.getTransactionResponse();
                if (result.getResponseCode().equals("1") && response.getTransactionResponse().getCvvResultCode().equals("M")) {
                    System.out.println(result.getResponseCode());
                    System.out.println("Successful Credit Card Transaction");
                    msg="Successful Credit Card Transaction your Transaction Id is "+result.getTransId();
                    System.out.println("Authentication Id: "+result.getAuthCode());
                    System.out.println("Transaction Id: "+result.getTransId());
                }
                else
                {
                    System.out.println("Failed Transaction"+result.getResponseCode());
                    System.out.println("Failed Transaction getMessages => "+result.getMessages());

                    System.out.println("Failed Transaction getErrors=> "+result.getErrors());
                    if(response.getTransactionResponse().getErrors()!=null){
                        Integer extremeErrorIndx=response.getTransactionResponse().getErrors().getError().size()-1;
                        response.getTransactionResponse().getErrors().getError().get(0).getErrorText();
                        if(!response.getTransactionResponse().getCvvResultCode().equals("M") && !response.getTransactionResponse().getCvvResultCode().isEmpty()){

                            msg=response.getTransactionResponse().getErrors().getError().get(extremeErrorIndx).getErrorText()+" and cvv not matches.";
                            errorCode=response.getTransactionResponse().getErrors().getError().get(extremeErrorIndx).getErrorCode();
                            code=Integer.parseInt(errorCode);
                        }else
                            msg=response.getTransactionResponse().getErrors().getError().get(extremeErrorIndx).getErrorText();
                            errorCode=response.getTransactionResponse().getErrors().getError().get(extremeErrorIndx).getErrorCode();
                            code=Integer.parseInt(errorCode);
                    }else{
                        msg="cvv missmatches.";
                    }

                }
            }
            else
            {
                System.out.println("Failed Transaction:  "+response.getMessages().getResultCode());
                System.out.println("Failed Transaction: msgg  "+response.getTransactionResponse().getMessages());
                if(response.getTransactionResponse().getErrors()!=null){
                    Integer extremeErrorIndx=response.getTransactionResponse().getErrors().getError().size()-1;
                    response.getTransactionResponse().getErrors().getError().get(0).getErrorText();
                    if(!response.getTransactionResponse().getCvvResultCode().equals("M") && !response.getTransactionResponse().getCvvResultCode().isEmpty()){
                        msg=response.getTransactionResponse().getErrors().getError().get(extremeErrorIndx).getErrorText()+" and cvv not matches.";
                        errorCode=response.getTransactionResponse().getErrors().getError().get(extremeErrorIndx)
                                .getErrorCode();
                        code=Integer.parseInt(errorCode);}
                    else {
                        msg = response.getTransactionResponse().getErrors().getError().get(extremeErrorIndx).getErrorText();
                        errorCode=response.getTransactionResponse().getErrors().getError().get(extremeErrorIndx).getErrorCode();
                        code=Integer.parseInt(errorCode);
                    }
                }else{
                    msg="cvv missmatches.";
                }
            }



        }else{
            msg="Internal Server Error. ";
        }
        if(code!=6 && code!=8 && !msg.equals("cvv missmatches.") && !msg.endsWith(" and cvv not matches.")){
            System.out.println("==================Saving Donation========code=="+code);
            donation.save();
            DonationMgmt.getResponseFromAuthorize(response,donation, userId);
        }

        return msg;
    }

}
