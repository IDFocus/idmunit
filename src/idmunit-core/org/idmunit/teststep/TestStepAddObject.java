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

import java.util.Map;

import org.apache.commons.lang.StringUtils;
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
    private Map m_operationData;
    
/**
 * Instantiate and initialize the transaction object
 */
    public TestStepAddObject(Map operationalDataMap, Connection idmUnitConnection, DataRowBean dataRow) {
        super(); 
		m_connection = idmUnitConnection;
		m_data = dataRow;
		m_operationData = operationalDataMap;
		
    }
/**
 * Execute the test step.  If the data contains a RepeatOpRange, the test step will be
 * repeated for each iteration through the specified range (i.e. ten through one hundred specified as 10-100)
 */	public void runStep() throws Exception {
		//Determine whether or not a repeat range has been specified
		String repeatRange = (String)m_operationData.get(Constants.OP_REPEAT_RANGE);	
		boolean rangeInputDetected = (repeatRange!=null && repeatRange.length()>0);
	 	if(!rangeInputDetected) {
	 		//Process a single non-repeated transaction
		 	m_connection.addObject(m_data);
	 	} else {
	 		//Repeat operation range was detected, for each iteration perform the following:
	 		//  1. Replace range counter for each data field
	 		//  2. Execute the test step
	 		//  3. If an error occurs add it to a list
	 		//  4. If there are any errors in the list after completion, fail the test with a report of broken iterations
	 		int rangeStart = Integer.parseInt(StringUtils.substringBefore(repeatRange, Constants.STR_RANGE_DELIMITER));
	 		int rangeEnd = Integer.parseInt(StringUtils.substringAfter(repeatRange, Constants.STR_RANGE_DELIMITER));
	 		LOG.info("### Repeat Operation Range detected: Start:" + rangeStart + " End: " + rangeEnd);
	 		for(int ctr=rangeStart;ctr<=rangeEnd;++ctr) {
		 		LOG.info("### Execute repeated operation iteration: " + ctr);
		 		//  1. Replace range counter for each data field //TODO: Leverage Data Injectors for this purpose if possible
		 		CommonUtil.interpolateVariables(m_data);
	 		}
	 	}
	}
 
 
}
