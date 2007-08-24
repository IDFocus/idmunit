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

public class EDMStoEAP extends IdMUnitTestCase {
	/* TO BE MODIFIED BY IDMUNIT USER */
	public void testDestroyFixture() throws Exception {executeTest();}
	public void testSetupFixture() throws Exception {executeTest();}
/*	public void testEntitlementAdd() throws Exception {executeTest();}
	public void testEntitlementMod() throws Exception {executeTest();}
	public void testEntitlementDel() throws Exception {executeTest();}
*/
	public static Test suite() {
		return DDStepsSuiteFactory.createSuite(EDMStoEAP.class);
	}
}