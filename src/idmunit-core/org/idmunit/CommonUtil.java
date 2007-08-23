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
package org.idmunit;

import java.util.Iterator;
import java.util.Map;

import net.sf.ldaptemplate.BadLdapGrammarException;
import net.sf.ldaptemplate.support.DistinguishedName;

import org.apache.commons.lang.StringUtils;
import org.ddsteps.dataset.DataValue;
import org.ddsteps.dataset.bean.DataRowBean;

/**
 * Provides parsing functionality for common data-structures manipulated in the IdMUnit test case engine
 * @author Brent Kynaston, Software Engineer, TriVir LLC
 * @version %I%, %G%
 */
public class CommonUtil {
	/**
	 * @deprecated
	 * @param keyName
	 * @param data
	 * @return
	 */
	public static boolean keyExists(String keyName, DataRowBean data) {
		for (Iterator iter = data.iterator(); iter.hasNext();) {
			DataValue dataValue = (DataValue) iter.next();

			String name = dataValue.getName();
			Object value = dataValue.getValue();
			if (value == null) {
				continue;
			}
			value = transformArrayValue(value);
			if (StringUtils.equals(name, keyName)) {
				return true;
			}
		}
		return false;
	}
		
	/**
	 * Normalize data values to string types
	 * @param o
	 * @return
	 */
	public static Object transformArrayValue(Object o) {
		if (o instanceof String[]) {
			return o;
		} else if (o instanceof String) {
			String string = (String) o;
			if (StringUtils.contains(string, "\n")) {
				return StringUtils.split(string, "\n");
			} else {
				return string;
			}
		} else {
			return o;
		}
	}
/**
 * Replace the replace-character (i.e. "~") with the current counter position value
 * @param data
 * @param counter
 */	
	public static void interpolateCounter(DataRowBean data, int counter) {
		//For each key/value pair, translate the data in the value component to insert the current variable value
		Iterator dataIterator = data.iterator();
		Map.Entry mapEntry;
		while(dataIterator.hasNext()) {
			DataValue dataValue = (DataValue) dataIterator.next();
			String name = dataValue.getName();
			String value = (String)dataValue.getValue();
			String updatedValue = value.replaceAll(Constants.STR_RANGE_COUNTER_SYMBOL, String.valueOf(counter));
			//dataValue.setValue(updatedValue);//TODO: update the data structure to contain the transformed value
		}
	}

	
}
