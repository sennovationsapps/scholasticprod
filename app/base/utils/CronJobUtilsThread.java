package base.utils;

import controllers.ReceiptMgmt;
import models.Donation;
import play.mvc.Result;

/**
 * Created by beassananda on 15/9/15.
 */



public class CronJobUtilsThread implements Runnable {



    public void run() {

              while(true){


                  System.out.println("within CronJobUtilsThread :: "+Thread.currentThread().getName() + "  ");


                  //call the transaction table to get the donation id(unique id)

                  String transactionNo = null;
                  //get the all donations

       if(transactionNo != null){
           Donation donation =   Donation.findByTransactionNumber(transactionNo);
           ReceiptMgmt receiptMgmt = new ReceiptMgmt();
           Result result = receiptMgmt.getAndSendCCReceipt(donation.event, donation);
          // return redirect(routes.ReceiptMgmt.getAndSendCCReceipt(event, updatedDonation));
       }



                  System.out.println("within CronJobUtilsThread111 :: "+Thread.currentThread().getName() + "  ");



                  try {
                      // thread to sleep for 1000 milliseconds
                      Thread.currentThread().sleep(60000);

                  } catch (Exception e) {
                      System.out.println(e);
                  }
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
