package models;

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





   public static List<Transaction> findAll() {
      final List<Transaction> transactions = find.all();
      if (transactions != null) {
         return transactions;
      }
      return new ArrayList<Transaction>();
   }

}
