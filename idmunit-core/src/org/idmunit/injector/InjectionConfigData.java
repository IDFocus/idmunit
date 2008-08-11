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
package org.idmunit.injector;

/**
 * Encapsulates configuration information used by a data injector
 * @author Brent Kynaston, Software Engineer, TriVir LLC
 * @version %I%, %G%
 * @see Injection
 */ 
public class InjectionConfigData {
	private String type;
	private String key;
	private String format;
	private String mutator;
	
	/**
	 *  Used to manipulate the dynamically genereated value (i.e. a formatter for a date for example)
	 * @return The mutator to apply to the dynamically generated value
	 */
	public String getMutator() {
		return mutator;
	}

	/**
	 *  Used to manipulate the dynamically genereated value (i.e. a formatter for a date for example)
	 */
	public void setMutator(String mutator) {
		this.mutator = mutator;
	}

	/**
	 * This class may be instantiated with basic configuration information
	 * @param type The fully distinguised class name of the injector implementation (i.e. org.idmunit.injector.DateInjection)
	 * @param key The key used to search and replace test data with the dynamically generated value
	 * @param format The format specification for the the dynamically generated value
	 */
	public InjectionConfigData(String type, String key, String format) {
		this.type = type;
		this.key = key;
		this.format = format;
	}
	
	/**
	 * @return The format specification for the the dynamically generated value
	 */
	public String getFormat() {
		return format;
	}
	
	/**
	 * @param format The format specification for the the dynamically generated value
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * @return The key used to search and replace test data with the dynamically generated value
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key The key used to search and replace test data with the dynamically generated value
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return The fully distinguised class name of the injector implementation (i.e. org.idmunit.injector.DateInjection)
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type The fully distinguised class name of the injector implementation (i.e. org.idmunit.injector.DateInjection)
	 */
	public void setType(String type) {
		this.type = type;
	}
}
