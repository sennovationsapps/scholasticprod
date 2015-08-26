package base.utils;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Created by beasrimi on 30/7/15.
 */
public class MailUtils {
    public void sendMail(String user,String password, String recipientEmail){
        System.out.println("in sendmails");

        //  public void postMail( String recipients[ ], String subject, String message , String from) throws MessagingException {
        boolean debug = false;
        boolean ismailSend = false;
        //  String recipients[]=toAddress.split(",");
        //  setServerinfo();
        //getServerinfo();
        //      getServerinfo(true);
//Set the host smtp address
        String recipients[]= new String[1];
        recipients[0]=recipientEmail;

        try{
            Properties props = new Properties();
/*            props.put("mail.smtp.host", host);
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");

            props.put("mail.smtp.port",smtpPortNo);*/



          /*the below mentioned properties are for gmail specific notification*/

            props.put("mail.smtp.quitwait", "true");
            props.put("mail.smtp.host", " smtp.emailsrvr.com");
            props.put("mail.debug", "true");
            props.setProperty("mail.smtp.user", user);
            props.setProperty("mail.smtp.password", password);
            props.setProperty("mail.smtp.auth", "true");
            // props.put("mail.smtp.starttls.enable","true");
            props.put("mail.smtp.socketFactory.port", 587);
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");
            props.put("mail.smtp.port", 587);

            Authenticator auth = new SMTPAuthenticator(user,password);

            Session session = Session.getInstance(props, auth);
            session.setDebug(debug);

// create a message
            Message msg = new MimeMessage(session);
            InternetAddress addressFrom=null;
            // addressFrom = new InternetAddress(authuserId);
            msg.setFrom(addressFrom);
            System.out.println("recipients.length::" + recipients.length);
            InternetAddress[] addressTo = new InternetAddress[recipients.length];
            for (int i = 0; i < recipients.length; i++) {
                addressTo[i] = new InternetAddress(recipients[i]);
            }
            msg.setRecipients(Message.RecipientType.TO,addressTo );


            msg.setSubject("Test Mail");
            // System.out.println("mailBody::" + mailBody);
            //  msg.setContent(mailBody, "text/plain");
            msg.setText("Test");
//            log("BEFORE TRANSPORTING MSG>>>>>>>>The host name :"+host);
            Transport.send(msg);
            System.out.println("Mail sent successfully");
            ismailSend=true;

        }catch (AddressException ex) {
//            log("mail exception here1");
            ex.printStackTrace();
//            logStackTrace(ex);
        } catch (MessagingException ex) {
            //           log("mail exception here2");
            ex.printStackTrace();
//            logStackTrace(ex);
        }catch(Exception e){
            e.printStackTrace();
        }
        //return ismailSend;
    }


    private static class SMTPAuthenticator extends javax.mail.Authenticator {
        private String username = null;
        private String password = null;
        public SMTPAuthenticator(String user, String pwd) {
            username = user;
            password = pwd;
        }
        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password);
        }
    }
}
