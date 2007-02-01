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

import org.ddsteps.dataset.bean.DataRowBean;
import org.ddsteps.step.TestStep;
import org.idmunit.connector.Connection;

/**
 * Implements an IdMUnit test step for Add object operations
 * @author Brent Kynaston, Software Engineer, TriVir LLC
 * @version %I%, %G%
 * @see TestStep
 */
public class TestStepAddObject implements TestStep {
	
	private Connection m_connection;
    private DataRowBean m_data;
    
	public TestStepAddObject(Connection idmUnitConnection, DataRowBean dataRow) {
        super(); 
		m_connection = idmUnitConnection;
		m_data = dataRow;
    }

	public void runStep() throws Exception {
		m_connection.addObject(m_data);
	}
}