/**************************************************************************
 *  IdMUnit - Automated Testing Framework for Identity Management Solutions
 *
 * Purpose of this file: This is a sample test runner that provides an interface for
 * the selection and execution of a spreadsheet and the sheets therein.
 * 
 * @author Brent Kynaston, Software Engineer, TriVir LLC
 *
 *******************************************************************************/
package org.idmunit;

import junit.framework.Test;

import org.ddsteps.junit.suite.DDStepsSuiteFactory;

public class TrivirDTFDriverTests extends IdMUnitTestCase {
	/* TO BE MODIFIED BY IDMUNIT USER */
	public void testUserAdd() throws Exception {executeTest();}

	
	//Used for operational purposes - ensure that the classname used is the same as the name of this class
	public static Test suite() {
		return DDStepsSuiteFactory.createSuite(TrivirDTFDriverTests.class);
	}
}