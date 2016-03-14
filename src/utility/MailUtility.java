package utility;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import context.DBConnectionUtilityContext;
import entity.ResourceInfo;

public class MailUtility {
	static Logger log = LogManager.getRootLogger();
	private static String mailsubject = "DB Connection Utility Tool Daily Alert";
	private static String mailfrom = "TLR.OSSWATBangalore@thomsonreuters.com";
	private static String mailto = "gaurav.rawat@thomsonreuters.com";
	private static String mailhost = "localhost";
	static boolean debug = false;
	
	public static void mail(Map<String, List<ResourceInfo>> suspectJvms) {
		log.debug("sending mail suspectJvmInfo size=" + suspectJvms.size());
		StringBuilder mailBodyAlertRow = new StringBuilder();
		for ( Entry<String, List<ResourceInfo>> jvm : suspectJvms.entrySet()) {
			mailBodyAlertRow.append("<tr>");
			mailBodyAlertRow.append("<td>");
			mailBodyAlertRow.append(jvm.getKey());
			mailBodyAlertRow.append("</td>");
			List<ResourceInfo> suspectResourceList = jvm.getValue();
			for (ResourceInfo resourceInfo : suspectResourceList) {
				mailBodyAlertRow.append("<td>");
				mailBodyAlertRow.append(resourceInfo.getResource() + " - " + resourceInfo.getOpenedConnections());
				mailBodyAlertRow.append("</td>");
			}
			mailBodyAlertRow.append("</tr>");
		}
		String mailBody = "<html><head><title>DB Connection Utility Tool Alert Report </title></head><body><table border=\"1\"><tr><th>Engine</th><th  colspan=\"100%\">Resource and Opened connections </th></tr> table-content </table> </body></html>";
		mailBody = mailBody.replace("table-content", mailBodyAlertRow.toString());
		log.trace("sending mail mailBody =" + mailBody);
		

		//Set the host smtp address
		Properties props = new Properties();
		props.put("mail.smtp.host", mailhost);

		// create some properties and get the default Session
		Session session = Session.getDefaultInstance(props, null);
		session.setDebug(debug);

		// create a message
		Message msg = new MimeMessage(session);

		try {
			// set the from and to address
			InternetAddress addressFrom = new InternetAddress(mailfrom);
			msg.setFrom(addressFrom);
			mailto=DBConnectionUtilityContext.getMailTO();
			String[] recipients = mailto.split(",");
			InternetAddress[] addressTo = new InternetAddress[recipients.length];
			for (int i = 0; i < recipients.length; i++) {
				addressTo[i] = new InternetAddress(recipients[i]);
			}
			msg.setRecipients(Message.RecipientType.TO, addressTo);

			// Setting the Subject and Content Type
			msg.setSubject(mailsubject);
			msg.setContent(mailBody, "text/html");
			Transport.send(msg);
		} catch (MessagingException e) {
			log.debug(" ", e);
		}
	}
}
