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

import org.ddsteps.dataset.bean.DataRowBean;
import org.ddsteps.step.TestStep;
import org.idmunit.IdMUnitTestCase;
import org.idmunit.connector.Connection;

/**
 * Constructs and returns the appropriate test step. The actual test step to use is specified in the //Operation column of the test spreadsheet.
 * @author Brent Kynaston, Software Engineer, TriVir LLC
 * @version %I%, %G%
 * @see TestStep
 * @see IdMUnitTestCase
 */
public class TestStepDispatcher {
	/**
	 * Returns the test step
	 * @param data Collection of data to be used when adding the object
	 * @param connection The connection to the target system where the object will be added
	 * @return {@link TestStepAddObject} The actual test step object.  //TODO: This should be refactored to be a generic TestStep. 
	 */
	public static TestStepAddObject getTestStepAddObject(DataRowBean data, Connection connection) {
    	return new TestStepAddObject(connection, data);
    }

	/**
	 * Returns the test step
	 * @param data Collection of data to be used when modifying the object
	 * @param connection The connection to the target system where the object will be modified
	 * @return {@link TestStepModObject} The actual test step object.  //TODO: This should be refactored to be a generic TestStep. 
	 */
    public static TestStepModObject getTestStepModObject(Attributes data, Connection connection) {
    	LdapOperations emptyOps=null;//TODO: refactor and eliminate this dependency
    	return new TestStepModObject(connection, data, emptyOps);
    }

	/**
	 * Returns the test step
	 * @param data Collection of data to be used when validating the object
	 * @param connection The connection to the target system where the object will be validated
	 * @return {@link TestStepValidateObject} The actual test step object.  //TODO: This should be refactored to be a generic TestStep. 
	 */
    public static TestStepValidateObject getTestStepValidateObject(Attributes data, Connection connection) {
    	LdapOperations emptyOps=null;//TODO: refactor and eliminate this dependency
    	return new TestStepValidateObject(connection, data, emptyOps);
    }

	/**
	 * Returns the test step
	 * @param data Collection of data to be used when validating the object
	 * @param connection The connection to the target system where the object will be validated
	 * @return {@link TestStepValidatePassword} The actual test step object.  //TODO: This should be refactored to be a generic TestStep. 
	 */
    public static TestStepValidatePassword getTestStepValidatePassword(Attributes data, Connection connection) {
    	LdapOperations emptyOps=null;//TODO: refactor and eliminate this dependency
    	return new TestStepValidatePassword(connection, data, emptyOps);
    }

    /**
	 * Returns the test step
	 * @param data Collection of data to be used when moving the object
	 * @param connection The connection to the target system where the object will be moved
	 * @return {@link TestStepMoveObject} The actual test step object.  //TODO: This should be refactored to be a generic TestStep. 
	 */
    public static TestStepMoveObject getTestStepMoveObject(Attributes data, Connection connection) {
    	LdapOperations emptyOps=null;//TODO: refactor and eliminate this dependency
    	return new TestStepMoveObject(connection, data, emptyOps);
    }

    /**
	 * Returns the test step
	 * @param data Collection of data to be used when deleting the object
	 * @param connection The connection to the target system where the object will be deleted
	 * @return {@link TestStepDeleteObject} The actual test step object.  //TODO: This should be refactored to be a generic TestStep. 
	 */
    public static TestStepDeleteObject getTestStepDeleteObject(Attributes data, Connection connection) {
    	LdapOperations emptyOps=null;//TODO: refactor and eliminate this dependency
    	return new TestStepDeleteObject(connection, data, emptyOps);
    }

}
