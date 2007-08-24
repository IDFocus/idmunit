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

public class EAPConnectorUseCaseScenariosAD extends IdMUnitTestCase {
	/* TO BE MODIFIED BY IDMUNIT USER
	 * These test methods should correspond to the sheets in the spreadsheet 
	 * that corrsponds to the name of this test class
	 */

	public void testUserAdd() throws Exception {executeTest();}

	public static Test suite() {
		return DDStepsSuiteFactory.createSuite(EAPConnectorUseCaseScenariosAD.class);
	}
}
