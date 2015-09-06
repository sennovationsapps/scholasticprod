package models;

/**
 * Created by edatsuv on 9/6/15.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import play.i18n.Messages;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Query;

@Entity
public class WorldPay extends Model {


   // typically required in a WebPay
    public String ccname;
    public String billaddr1;
    public String billaddr2;
    public String billcity;
    public String billstate;
    public String billzip;

    // Always returned to post-back URL:
   public String  action ;
   public String  acctid;
   public String  ci_ipaddress;
   public String  orderid;
   public String  ACCOUNTNUMBER;
   public String  Status;
   public String  transid;

// Always returned for declined transaction:
   public String  Declined;
   public String  Reason;
   public String  rcode;

// Always returned for accepted transactions:
   public String  Accepted;
   public String  authcode;

}
