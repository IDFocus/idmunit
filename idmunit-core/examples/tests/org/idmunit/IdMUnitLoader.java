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

import org.ddsteps.junit.suite.DDStepsDdMethodTestSuite;
import org.ddsteps.junit.suite.DDStepsSuiteFactory;

import junit.framework.Test;
import junit.framework.TestSuite;

public class IdMUnitLoader extends IdMUnitTestCase  {
/*	public static Test suite() {
		  return DDStepsSuiteFactory.createSuite(TrivirADDomainDriverTests.class);
		}*/

	public static Test suite ( ) {
		//Configure the name of the test suite here
		//TestSuite suite= new TestSuite("Sample IdMUnit Tests");
		TestSuite suite = DDStepsSuiteFactory.createSuite(TrivirADDomainDriverTests.class);

		//Add desired individual sheets to run here
		DDStepsDdMethodTestSuite.createTest(TrivirADDomainDriverTests.class, "testDestroyFixture");

		//Add desired test spreadsheets here
		//suite.addTestSuite(TrivirADDomainDriverTests.class);

		//Post-operations (run individual clean-up sheets)
//		suite.addTest(TestSuite.createTest(ADDriver.class, "testDestroyFixture"));
		
		return suite;
}

}
