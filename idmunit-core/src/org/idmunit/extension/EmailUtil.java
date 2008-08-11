/* 
 * IdMUnit - Automated Testing Framework for Identity Management Solutions
 * Copyright (c) 2005-2006 TriVir, LLC
 *
 * This program is licensed under the terms of the GNU General Public License
 * Version 2 (the "License") as published by the Free Software Foundation, and 
 * the TriVir Licensing Policies (the "License Policies").  A copy of the License 
 * and the Policies were distributed with this program.  
 *
 * The License is available at:
 * http://www.gnu.org/copyleft/gpl.html
 *
 * The Policies are available at:
 * http://www.idmunit.org/licensing/index.html
 *
 * Unless required by applicable law or agreed to in writing, this program is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied.  See the License and the Policies
 * for specific language governing the use of this program.
 *
 * www.TriVir.com
 * TriVir LLC
 * 11570 Popes Head View Lane
 * Fairfax, Virginia 22030
 *
 */
package org.idmunit.extension;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.idmunit.Alert;
import org.idmunit.Constants;
import org.idmunit.IdMUnitException;
import org.idmunit.IdMUnitTestCase;


/**
 * Provides an email tool for IdMUnit alerts.  This version supports text email messages.  The next version will support HTML content
 * based off an HTML file template.
 * @author Brent Kynaston, Software Engineer, TriVir LLC
 * @version %I%, %G%
 * @see Alert
 * @see IdMUnitTestCase
 */
public class EmailUtil {


	/**
	 * Sends an email 
	 * @param alert Contains alert configuration
	 * @param content Email content - generally used in the subject line.  May be pre-pended to the message body as well.
	 * @param exceptionMessage The actual Java Exception message buffer describing the problem and/or error
	 * @param dynamicContent (RESERVED FOR FUTURE USE) This will provide a collection of substitution information for the dynamic keys/variables located throughout the HTML template
	 * @see IdMUnitTestCase 
	 */
	public static void sendEmailNotification(Alert alert, String content, String exceptionMessage, Map dynamicContent) throws IdMUnitException {
		if(alert.getAlertReipient()== null || alert.getAlertReipient().length()<1) throw new IdMUnitException("sendEmailNotification(): Bad parameters");
		try {
		    Properties connectionProps = System.getProperties();
		    connectionProps.put("mail.smtp.host", alert.getSmtpServer());

		    //Connect to the target SMTP server
		    Session session = Session.getInstance(connectionProps, null);
		    if (true) { //TODO: add config to enable/disable email debug
				session.setDebug(true);
		    }

		    //Construct the message
		    Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(alert.getAlertSender()));
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(alert.getAlertReipient(), false));
		    
		    //String bcVal = config.getProperty(Constants.PROP_EMAIL_BC); //TODO: Add BC to config
			//if ((bcVal != null) && bcVal.length()>1)
			//msg.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcVal, false));
		    msg.setSubject(alert.getSubjectPrefix() + content);

//		    buildHtml(msg, config, dynamicContent);
	    	msg.setDataHandler(new DataHandler(new ByteArrayDataSource(content + " | Additional Info: "+exceptionMessage, "text/html")));

		    msg.setHeader("X-Mailer", "IdMUnit Alerts");
		    msg.setSentDate(new Date());

		    //Send 
		    Transport.send(msg);

		    BasicLogger.consoleLog("### Mail was sent successfully.");
		} catch (Throwable e) {
		    if(true) {//TODO: add true log4j entries here
		    	BasicLogger.fileLog("Alert Exception thrown: " + e.getMessage()+" | IdMUnit exception:"+content + " | Additional Info: "+exceptionMessage, alert.getLogPath());
		    	e.printStackTrace();
		    } throw new IdMUnitException(e.getMessage());
		}
		
	}
  /*  private static void buildHtml(Message msg, Properties config, Map dynamicContent) throws MessagingException, IOException {
    	String subject = msg.getSubject();
    	StringBuffer sb = new StringBuffer();
    	 
    	BufferedReader htmlTopReader = new BufferedReader(new FileReader(config.getProperty(Constants.PROP_EMAIL_MSG_HTML)));
    	String line = null;

    	//Read the top of the  HTML file
    	while ((line=htmlTopReader.readLine()) != null) {
    		if(!(line.equals(Constants.DATA_INTERPOLATION_STR))) {
        		sb.append(line);
    		} else {
    	    	//Add dynamic content:
    			parseDynamicContent(sb, dynamicContent, config);
    		}
        }
    	htmlTopReader.close();  // Close to unlock.
    	msg.setDataHandler(new DataHandler(new ByteArrayDataSource(sb.toString(), "text/html")));
    }*/
/*    
    private static void parseDynamicContent(StringBuffer sb, Map dynamicContent, Properties config) {
    	sb.append(Constants.HTML_OPEN_DYNAMIC_DATA);
    	sb.append(Constants.HTML_HARD_RETURN);
		sb.append("<p style=\"color:red\">Users being de-provisioned NOW: </p>");
		Iterator usersToDeleteNow = dynamicContent.keySet().iterator();
		Iterator usersToDeleteLater = dynamicContent.keySet().iterator();
		if(dynamicContent.isEmpty()) {
			sb.append("NO USERS TO DISPLAY");
		} else {
			//Enumerate users to be deleted today
			int deleteUsersCtr = 0;
			while (usersToDeleteNow.hasNext()) {
				String hashKey = (String)usersToDeleteNow.next();
				if(hashKey.indexOf(Constants.DELIN_IMMEDIATE_DELETION) != -1) {
					++deleteUsersCtr;
    				String hashVal = (String)dynamicContent.get(hashKey);
    				//strip off the immediate deletion deliniator
    				hashKey = hashKey.substring(Constants.DELIN_IMMEDIATE_DELETION.length());
    				BasicLogger.consoleLog("Adding user: " + hashKey + " Info: " + hashVal); //TODO remove
    				sb.append(hashKey + " " + hashVal);
    				sb.append(Constants.HTML_HARD_RETURN);
				}
			}
			if(deleteUsersCtr==0) {
				sb.append("-- No immediately expiring users found at this time. --");
				sb.append(Constants.HTML_HARD_RETURN);
			}
			sb.append("<p style=\"color:red\">Users scheduled to be de-provisioned SOON: </p>");
			//Enumerate users to be deleted in the near future
			int usersSoonToDeleteCtr = 0;
			while (usersToDeleteLater.hasNext()) { 
				String hashKey = (String)usersToDeleteLater.next();
				if(hashKey.indexOf(Constants.DELIN_IMMEDIATE_DELETION) == -1) {
    				++usersSoonToDeleteCtr;
					String hashVal = (String)dynamicContent.get(hashKey);
    				BasicLogger.consoleLog("Adding user: " + hashKey + " Info: " + hashVal); //TODO remove
    				sb.append(hashKey + " " + hashVal);
    				sb.append(Constants.HTML_HARD_RETURN);
				}
			}
			if(usersSoonToDeleteCtr==0) {
				sb.append("-- No users found to be expiring within the notification interval of " 
						+ config.getProperty(Constants.PROP_ADVANCED_NOTICE) + " day(s). --");
				sb.append(Constants.HTML_HARD_RETURN);
			}
		}
    	sb.append(Constants.HTML_CLOSE_DYNAMIC_DATA);
    } */
}
