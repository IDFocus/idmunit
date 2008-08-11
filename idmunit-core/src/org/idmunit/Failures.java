/* 
 * IdMUnit - Automated Testing Framework for Identity Management Solutions
 * Copyright (c) 2005-2008 TriVir, LLC
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
package org.idmunit;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Failures {
    private List<String> failures = new LinkedList<String>();

    public void add(String message) {
        failures.add(message);
    }

    public void add(String message, Collection expected, Collection actual) {
        String formatted = "";
        if (message != null)
            formatted = message + " ";
        failures.add(formatted + "expected:<" + expected + "> but was:<" + actual + ">");
    }

    public boolean hasFailures() {
        return failures.size() != 0;
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        for (Iterator i=failures.iterator(); i.hasNext(); ) {
            if (s.length() > 0) {
                s.append(System.getProperty("line.separator"));
            }
            s.append(i.next());
        }

        return s.toString();
    }
}
