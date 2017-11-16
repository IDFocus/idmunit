/* 
 * IdMUnit - Automated Testing Framework for Identity Management Solutions
 * Copyright (c) 2008 TriVir, LLC
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
package org.idmunit.connector;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.idmunit.Failures;
import org.idmunit.IdMUnitException;

/**
 * This class is provided for backwards compatability with older versions of
 * IdMUnit. All new tests should use LdapConnector instead of this class.
 * 
 * @deprecated
 */
public class LDAP extends LdapConnector {
    private static Log logger = LogFactory.getLog(LDAP.class);

    public void opModObject(Map<String, Collection<String>> data) throws IdMUnitException {
        opReplaceAttr(data);
    }

    public void opModifyObject(Map<String, Collection<String>> data) throws IdMUnitException {
        opReplaceAttr(data);
    }

    public void opModAttr(Map<String, Collection<String>> data) throws IdMUnitException {
        opReplaceAttr(data);
    }

    public void opDelObject(Map<String, Collection<String>> data) throws IdMUnitException {
    	opDeleteObject(data);
    }

    public void opRenObject(Map<String, Collection<String>> data) throws IdMUnitException {
    	opRenameObject(data);
    }

    protected void compareAttributeValues(String attrName, Collection<String> expected, Collection<String> actual, Failures failures) throws IdMUnitException {
        // Support validation of whether or not an attribute exists (using a wildcard in the spreadsheet - '*')
        if (expected.size() == 1 && expected.iterator().next().equals("*") && actual.size() > 0) {
            logger.info(STR_SUCCESS + ": validating attribute: [" + attrName + "] EXPECTED: [*] ACTUAL: [" + actual.iterator().next() + "]");
            return;
        }

        outer:
        for (String expectedValue : expected) {
        	for (Iterator<String> i=actual.iterator(); i.hasNext(); ) {
        		// Trim white space to be backwards compatable with
        		// previous versions of this connector, this was originally
        		// added to work around JDBC drivers that don't trim white
        		// space on values sent to eDir
        		String actualValue = i.next().trim();

                if (actualValue.equalsIgnoreCase(expectedValue)) {
                    logger.info(STR_SUCCESS + ": validating attribute: [" + attrName + "] EXPECTED: [" + expectedValue +"] ACTUAL: [" + actualValue + "]");
                    continue outer;
                }

				// Allow DirXML-Associations to match
                if (attrName.equalsIgnoreCase(STR_DXML_ASSOC) && actualValue.startsWith(expectedValue)) {
                    logger.info(STR_SUCCESS + ": validating attribute: [" + attrName + "] EXPECTED: [" + expectedValue +"] ACTUAL: [" + actualValue + "]");
                    continue outer;
                }
        	}

            failures.add(attrName, expected, actual);
        	return;
        }
    }
}