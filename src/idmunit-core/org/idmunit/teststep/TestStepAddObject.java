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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ddsteps.dataset.bean.DataRowBean;
import org.ddsteps.step.TestStep;
import org.idmunit.CommonUtil;
import org.idmunit.Constants;
import org.idmunit.IdMUnitTestCase;
import org.idmunit.connector.Connection;

import com.sun.corba.se.impl.orbutil.closure.Constant;

/**
 * Implements an IdMUnit test step for Add object operations
 * @author Brent Kynaston, Software Engineer, TriVir LLC
 * @version %I%, %G%
 * @see TestStep
 */
public class TestStepAddObject implements TestStep {
    static Log LOG = LogFactory.getLog(TestStepAddObject.class);
	private Connection m_connection;
    private DataRowBean m_data;
    
/**
 * Instantiate and initialize the transaction object
 */
    public TestStepAddObject(Connection idmUnitConnection, DataRowBean dataRow) {
        super(); 
		m_connection = idmUnitConnection;
		m_data = dataRow;
    }
/**
 * Execute the test step.  If the data contains a RepeatOpRange, the test step will be
 * repeated for each iteration through the specified range (i.e. ten through one hundred specified as 10-100)
 */	public void runStep() throws Exception {
		//Determine whether or not a repeat range has been specified
	 	boolean rangeInputDetected = CommonUtil.keyExists(Constants.OP_REPEAT_RANGE, m_data);
	 	LOG.info("DEBUG: ######### Range Input detected: " + rangeInputDetected); 
	 	if(!rangeInputDetected) {
	 		//Process a single non-repeated transaction
		 	m_connection.addObject(m_data);
	 	} else {
		 	LOG.info("DEBUG: ######### Operation repeater to be implemented.....");
	 	}
	}
 
 
}
