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
package org.idmunit.integrity;

import java.util.Map;

import org.idmunit.ConfigLoader;
import org.idmunit.IdMUnitException;
import org.idmunit.connector.ConnectionConfigData;

import junit.framework.TestCase;

/**
 * Validates basic functionality of the ConfigLoader
 * <br>
 * Up to this point in the development of the project, integrity and basic functionality testing of IdMUnit has been conducted by running the test cases
 * developed for commercial IdMUnit users.  Regression testing in this manner has exposed any anomalies caused by refactoring or further development.  This 
 * process has become more and more infeasible as the number of commercial IdMUnit users has grown however.  The need for stand-alone, system-agnostic test cases
 * for IdMUnit exists and should better covered in future releases of the project.
 * @author Brent Kynaston, Software Engineer, TriVir LLC
 * @version %I%, %G%
 */
public class ConfigLoaderTest extends TestCase {
	public void testGetConfigData() {
		try {
			String configFilePath = "c:\\IdMUnit\\src\\src\\test-resources\\idmunit-config.xml";
			Map configMap = ConfigLoader.getConfigData(configFilePath);
			ConnectionConfigData configData = (ConnectionConfigData)configMap.get("TESS");
			System.out.println("Admin: " + configData.getAdminCtx());
		} catch(IdMUnitException e) {
			fail(e.getMessage());
		}
	}
}
