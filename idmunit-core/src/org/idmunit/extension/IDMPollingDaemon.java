/* 
 *  TriVir IDMPollingDaemon (This is a non-open-source commercial plug-in to IdMUnit)
 *  Copyright (C) 2004-2008 TriVir, LLC.  All Rights Reserved.
 *  www.TriVir.com
 *  TriVir LLC
 *  11570 Popes Head View Lane
 *  Fairfax, Virginia 22030
 * 
 * Changes Made:
 * 20051007 Added SSL, encryption of admin password (DES) and modified LDAP filfter to exclude MailBox Exp Time 1970
 * 20061101 Converted to IdMUnit libs and deprecated PSS calls, extended functionality for commercial user requests
 *
 */
package org.idmunit.extension;

/**
 * This is a commercial plug-in to IdMUnit and Novell Identity Manager that <br>
 * provides state-based and date-based provisioning/deprovisioning capability.<br>
 * For example, this daemon may be used to identify users that have not logged in for X<br>
 * number of days and flag them for deprovisioning.  A corresponding driver would be configured<br>
 * to detect such a flag and deprovision the user.
 * <hr>
 * In order to obtain a license for this plug-in, please email a request to info@trivir.com.  
 * @author Brent Kynaston, Software Engineer, TriVir LLC
 * @version 2.0
 */
public class IDMPollingDaemon {
	/** 
	 * Typically, this will be CRONed, crontabbed, or executed by the Windows Task Scheduler or MOM.
	 * @param args A single String argument to contain the full filesystem path of the<br>
	 * configuration file is required.
	 */
	public static void main(String[] args){	}
	
	//Proprietary class contents omitted
}
