package si.noemus.boatguard.sms;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 
public class MailClient {
	private static Log log = LogFactory.getLog(MailClient.class); 
	/**
	 * @param args
	 */
	 
	public static String smtpServer;
	public static String user;
	public static String pass;
	public static String smtp_auth;
	public static String smtp_port;
	public static String smtp_socketFactory_port;
	public static String smtp_starttls_enable;
	public static String transport_protocol;
	public static String use_ssl;


	public static void sendMail(String to, String subject, String body)
    {
		System.out.println(to+":"+subject+":"+body+":"+user);
        try
        {
          Properties props = System.getProperties();
          //props.setProperty("mail.transport.protocol", transport_protocol);
          props.put("mail.smtp.host", smtpServer);
          props.put("mail.smtp.auth", smtp_auth);
          props.put("mail.smtp.port", smtp_port);
          props.put("mail.smtp.starttls.enable",smtp_starttls_enable);
  		
          if (use_ssl.equals("true")) {
              props.put("mail.smtp.socketFactory.port", smtp_socketFactory_port);
              props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
              props.put("mail.smtp.socketFactory.fallback", "false");
          }
          props.setProperty("mail.smtp.quitwait", "false");

          Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() 
          {
            protected PasswordAuthentication getPasswordAuthentication()
            { return new PasswordAuthentication(user,pass);    }
          });
            
          //Session session = Session.getDefaultInstance(props, null);
          Message msg = new MimeMessage(session);
          msg.setFrom(new InternetAddress(user));
          if (to.indexOf(',') > 0) 
                msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
          else
              msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));

          msg.setSubject(subject);
          //msg.setText(body);
          msg.setSentDate(new Date());
          
          msg.setContent(body, "text/html; charset=UTF-8");
          
          Transport.send(msg);
          System.out.println("Message sent OK.");
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
        }
    }


}
