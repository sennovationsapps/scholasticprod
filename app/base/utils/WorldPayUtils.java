package base.utils;

import security.ThreeDES;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;

/**
 * Created by edatsuv on 9/5/15.
 */
public class WorldPayUtils {


    public static String checkout(String donationamount, String TranscationId ,String MailId)
    throws BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, UnsupportedEncodingException,
           NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException

    {
        String decodevalue= ThreeDES.hexToASCII(ThreeDES.decrypt(ThreeDES.FORM_ID));
        String[] split = decodevalue.split(":");
        String ACC=split[0];
        String SUB_ACC=split[1];
        String FORM_ID=split[2];
        String AMOUNT=split[3];
        if (!donationamount.isEmpty())
        {
            AMOUNT=donationamount;
        }
        //rebuild url
        StringBuilder url = new StringBuilder();
        String $webpay_url	= "https://trans.worldpay.us/cgi-bin/WebPay.cgi?formid=";
        // string to encrpyt, colon (:) deliminated
        String $encodeString = ACC+":"+SUB_ACC+":"+FORM_ID+":"+AMOUNT+":";
        String $sessionIdString="&sessionid=";
        String $sessionId=ThreeDES.SESSION_ID;
        String $customdataString= "&customdata=";
        String $cdata1="cdata1="+TranscationId+"&"+"cdata2="+MailId;

        //append the url string
        url.append($webpay_url);
        url.append(ThreeDES.encrypt($encodeString));
        url.append($sessionIdString);
        url.append($sessionId);
        url.append($customdataString);
        url.append(ThreeDES.encrypt($cdata1));
        return url.toString();
    }

}
