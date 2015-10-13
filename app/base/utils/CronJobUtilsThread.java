package base.utils;

import controllers.ReceiptMgmt;
import models.Donation;
import models.Transaction;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

/**
 * Created by beassananda on 15/9/15.
 */



public class CronJobUtilsThread implements Runnable {

    private static final org.slf4j.Logger MAIL_LOGGER = LoggerFactory.getLogger("ScholasticReceiptsLogger");


    public void run() {

              while(true){


                 // System.out.println("within CronJobUtilsThread :: "+Thread.currentThread().getName() + "  ");
                  MAIL_LOGGER.info("*** Enter within CronJobUtilsThread for tax letter and donation receipt letter :: "+Thread.currentThread().getName() + " ***");



                  String transactionNo = null;
                  //call the transaction table to get the donation id(unique id)
                 List<Transaction> transactions = Transaction.findAll();

                  System.out.println("the size of transactions :: "+transactions.size());
                   Iterator transactionItr = transactions.iterator();


                  while(transactionItr.hasNext()) {
                      Transaction transaction = (Transaction) transactionItr.next();
                      System.out.println("transaction.mailSent :: " + transaction.mailSent);
                      System.out.println("transaction.transid2:: " + transaction.donationTranId);
                      MAIL_LOGGER.info("*** For transaction number :: " + transaction.donationTranId +" :: mail sent flag status is :: " + transaction.mailSent + " ***");
                      if (transaction.mailSent == false) {
                          System.out.println("transaction.mailSent == false  : for transactionId :: " + transaction.donationTranId);
                          // MAIL_LOGGER.info("*** Enter within CronJobUtilsThread for tax letter and donation receipt letter :: " + Thread.currentThread().getName() + " ***");
                          Donation donation = Donation.findByTransactionNumber(transaction.donationTranId);
                          if(donation!=null){
                              MAIL_LOGGER.info("*** Donation is not null ***");
                          if (donation.status == Donation.PaymentStatus.CLEARED) {

                              MAIL_LOGGER.info("*** Donation.PaymentStatus.CLEARED ***");

                              String creditCardNumber = transaction.accountNumber;

                              if (creditCardNumber != null) {
                                  creditCardNumber = creditCardNumber.substring(creditCardNumber.length() - 4, creditCardNumber.length());
                                  System.out.println("creditCardNumber :: " + creditCardNumber);
                                  donation.ccNum = creditCardNumber;
                              }

                              donation.ccName = transaction.ccname;
                              ReceiptMgmt receiptMgmt = new ReceiptMgmt();
                              System.out.println(("before calling getAndSendCCReceipt"));
                              receiptMgmt.sendCCReceiptForCron(donation);
                              //System.out.println("result :: " + result);
                              System.out.println(("after calling getAndSendCCReceipt"));

                              System.out.println("before calling sendCCReceiptForPfp...");
                              receiptMgmt.sendCCReceiptForPfp(donation);
                              System.out.println("after calling sendCCReceiptForPfp");

                              //update
                              transaction.mailSent = true;
                              transaction.update();


                          }
                      }



                      }


                      //===for testing=========//
                      /*ReceiptMgmt receiptMgmt = new ReceiptMgmt();
                      Donation donation = Donation.findByTransactionNumber(transaction.transid);
                      System.out.println("before calling sendCCReceiptForPfp...");
                      receiptMgmt.sendCCReceiptForPfp(donation);
                      System.out.println("after calling sendCCReceiptForPfp");*/
                      //===for testing=========//
                  }



/*
                  for ( Transaction transaction : transactions){
                      //System.out.println("transaction.transid :: "+transaction.transid);
                      System.out.println("transaction.mailSent :: "+transaction.mailSent);

                      if(transaction.mailSent == false){
                          System.out.println("transaction.mailSent == false  : for transactionId :: "+transaction.transid);
                          Donation donation =   Donation.findByTransactionNumber(transaction.transid);

                          String creditCardNumber = transaction.accountNumber;

                          if(creditCardNumber!=null){
                              creditCardNumber = creditCardNumber.substring(creditCardNumber.length()-4,creditCardNumber.length());
                              System.out.println("creditCardNumber :: "+creditCardNumber);
                              donation.ccNum = creditCardNumber;
                          }

                          donation.ccName = transaction.ccname;
                          ReceiptMgmt receiptMgmt = new ReceiptMgmt();
                          System.out.println(("before calling getAndSendCCReceipt"));
                          receiptMgmt.sendCCReceipt(donation);
                          //System.out.println("result :: " + result);
                          System.out.println(("after calling getAndSendCCReceipt"));

                          //update
                          transaction.mailSent = true;
                          transaction.update();
                         /* if(!StringUtils.equals(volunteer.firstName, updatedVolunteer.firstName))
                          {
                              volunteer.firstName = updatedVolunteer.firstName;

                          }*/

/*
                      }


                  }*/





                  //get the all donations

      /* if(transactionNo != null){
           Donation donation =   Donation.findByTransactionNumber(transactionNo);
           ReceiptMgmt receiptMgmt = new ReceiptMgmt();
           Result result = receiptMgmt.getAndSendCCReceipt(donation.event, donation);
          // return redirect(routes.ReceiptMgmt.getAndSendCCReceipt(event, updatedDonation));
       }
*/


                  System.out.println("within CronJobUtilsThread111 :: "+Thread.currentThread().getName() + "  ");



                  try {
                      // thread to sleep for 1000 milliseconds
                      Thread.currentThread().sleep(600000);

                  } catch (Exception e) {
                      e.printStackTrace();
                      System.out.println("Error"+ e.getMessage());
                      MAIL_LOGGER.error("*** Error in CronJobUtilsThread for tax letter and donation receipt letter :: " +e+ " ***");
                  }
                  MAIL_LOGGER.info("*** Exit from CronJobUtilsThread for tax letter and donation receipt letter :: "+Thread.currentThread().getName() + " ***");
              }


    }

    public static void main(String[] args) throws Exception {
        Thread t = new Thread(new CronJobUtilsThread());
        // this will call run() function
        t.start();

        Thread t2 = new Thread(new CronJobUtilsThread());
        // this will call run() function
        t2.start();
    }
}
