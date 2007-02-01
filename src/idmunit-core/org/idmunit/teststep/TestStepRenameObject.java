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
package org.idmunit.teststep;

import javax.naming.directory.Attributes;
import net.sf.ldaptemplate.LdapOperations;
import org.ddsteps.step.TestStep;
import org.idmunit.IdMUnitException;
import org.idmunit.connector.Connection;
import org.idmunit.connector.ConnectionConfigData;
import org.idmunit.connector.LDAP;

/**
 * Implements an IdMUnit test step for Rename object operations
 * @author Brent Kynaston, Software Engineer, TriVir LLC
 * @version %I%, %G%
 * @see TestStep
 */public class TestStepRenameObject implements TestStep{
	private Connection m_connection;
    private Attributes m_data;
    
	public TestStepRenameObject(ConnectionConfigData creds, Attributes dataRows, LdapOperations ops) {
        try {
			m_connection = new LDAP(creds);
			m_data = dataRows;
		} catch (IdMUnitException e) {
			throw new AssertionError("Failed to authenticate: " + e.getMessage());
		}
    }

	public void runStep() throws Exception {
		m_connection.renameObject(m_data);
	}
}