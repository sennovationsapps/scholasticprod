package models;

import com.avaje.ebean.ExpressionList;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by beassananda on 17/9/15.
 */

@Entity
public class Transaction extends Model {

    public static final Finder<Long, Transaction> find = new Finder<Long, Transaction>(
            Long.class, Transaction.class);

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public int id;




    public String ccname;


    public String billaddr1;

    public String billaddr2;

    public String billcity;

    public String billstate;

    public String billzip;

    public String action1;

    public String ciIpaddress;

    public String accountNumber;


    public String status1;


    public String transid;

    public String declined;

    public String reason;

    public String rcode;

    public String accepted;

    public String authcode;

    public String myField;

    public boolean mailSent;

    public boolean activityCheck;

    public String donationTranId;

    public String email;




   public static Transaction findById(int id) {

      return  find.where().eq("id",id).findUnique();
   }



   public static Transaction findByTransid(String transid) {

      return find.where().eq("transid", transid).findUnique();
   }


    public static Transaction findByDonationTranId(String donationTranId) {
        Transaction transaction=null;
        System.out.println("findByDonationTranId");
        System.out.println("transaction :: "+find.where().eq("donationTranId", donationTranId).findList());
        List<Transaction> transactionList = null;
        transactionList= find.where().eq("donationTranId", donationTranId).findList();
        if(transactionList!=null && transactionList.size()>0){
            transaction =transactionList.get(0);
        }
        //if(find.where().eq("donationTranId", donationTranId).findList().size()>0)
        return transaction;
    }


    public static List<Transaction> findAll() {
        System.out.println("within findAll ");
        final ExpressionList<Transaction> transactionExpressionList = find.where().eq("mailSent", false).ne("donationTranId",null).ne("donationTranId","");
        final List<Transaction> transactions ;
        if(transactionExpressionList!=null){
            transactions = transactionExpressionList.findList();
            if (transactions != null) {
                System.out.println("transactions :: "+transactions);
                return transactions;
            }
        }


        return new ArrayList<Transaction>();
    }


    /******start*********************refund donations******************************14.01.2016************************************/

    public static List<Transaction> findAllTransactions() {
        return find.all();
    }

    /*******end**********************refund donations******************************14.01.2016************************************/
}
