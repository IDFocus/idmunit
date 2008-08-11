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
package org.idmunit;

/**
 * Encapsulates information used for generating email/log alerts
 * @author Brent Kynaston, Software Engineer, TriVir LLC
 * @version %I%, %G%
 * 
 */
public class Alert {
	private String name;
	private String description;
	private String smtpServer;
	private String alertSender;
	private String alertReipient;
	private String subjectPrefix;
	private String logPath;
	private boolean isEmailAlertingEnabled;
	private boolean isLogAlertingEnabled;

	public Alert(String name, String description, String smtpServer, String alertSender, String alertReipient, String subjectPrefix, String logPath) {
		super();
		this.name = name;
		this.description = description;
		this.smtpServer = smtpServer;
		this.alertSender = alertSender;
		this.alertReipient = alertReipient;
		this.subjectPrefix = subjectPrefix;
		this.logPath = logPath;
	}
	public String getAlertReipient() {
		return alertReipient;
	}
	public void setAlertReipient(String alertReipient) {
		this.alertReipient = alertReipient;
	}
	public String getAlertSender() {
		return alertSender;
	}
	public void setAlertSender(String alertSender) {
		this.alertSender = alertSender;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getLogPath() {
		return logPath;
	}
	public void setLogPath(String logPath) {
		this.logPath = logPath;
	}
	public String getSmtpServer() {
		return smtpServer;
	}
	public void setSmtpServer(String smtpServer) {
		this.smtpServer = smtpServer;
	}
	public String getSubjectPrefix() {
		return subjectPrefix;
	}
	public void setSubjectPrefix(String subjectPrefix) {
		this.subjectPrefix = subjectPrefix;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isEmailAlertingEnabled() {
		return isEmailAlertingEnabled;
	}
	public void setEmailAlertingEnabled(boolean isEmailAlertingEnabled) {
		this.isEmailAlertingEnabled = isEmailAlertingEnabled;
	}
	public boolean isLogAlertingEnabled() {
		return isLogAlertingEnabled;
	}
	public void setLogAlertingEnabled(boolean isLogAlertingEnabled) {
		this.isLogAlertingEnabled = isLogAlertingEnabled;
	}
}
