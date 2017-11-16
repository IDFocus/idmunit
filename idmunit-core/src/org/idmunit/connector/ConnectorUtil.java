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

import org.idmunit.IdMUnitException;

public class ConnectorUtil {
    public static String getFirstValue(Map<String, Collection<String>> attrs, String attrName) {
        Collection<String> values = attrs.get(attrName);
        if (values == null) {
            return null;
        } else {
            Iterator<String> i = values.iterator();

            if (i.hasNext()) {
                return i.next();
            } else {
                return null;
            }
        }
    }

    public static String getSingleValue(Map<String, Collection<String>> attrs, String attrName) throws IdMUnitException {
        Collection<String> values = attrs.get(attrName);
        if (values == null) {
            return null;
        }
        
        if (values.size() > 1) {
            throw new IdMUnitException("Error: '" + attrName + "' is single valued but more than one value was specified.");
        }

        Iterator<String> i = values.iterator();
        if (i.hasNext()) {
            return i.next();
        } else {
            return null;
        }
    }
}
