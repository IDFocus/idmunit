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
 * Provides an interface for IdMUnit users to inject dynamically generated data into the test data.
 * @author Brent Kynaston, Software Engineer, TriVir LLC
 * @version %I%, %G%
 */
public interface Injection {
	/**
	 * Exposes a field from the idmunit-config.xml layer that may be used to manipulate the dynamically genereated value (i.e. a formatter for a date for example)
	 * @param mutation The directive to modify the dynamically generated value
	 */
	public abstract void mutate(String mutation);
	/**
	 * Returns the dynamically generated value for injection into the data for a test step
	 * @param formatter Defines the format for the dynamic data (i.e. SimpleDateFormat syntax for a date value)
	 * @return The dynamic value that was generated and formatted according to the formatter and possibly the mutator
	 */
	public abstract String getDataInjection(String formatter);
}