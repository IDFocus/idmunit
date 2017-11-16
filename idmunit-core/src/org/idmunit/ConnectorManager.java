/*
 * IdMUnit - Automated Testing Framework for Identity Management Solutions
 * Copyright (c) 2009 TriVir, LLC
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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.idmunit.connector.ConnectionConfigData;
import org.idmunit.connector.Connector;

import junit.extensions.TestSetup;
import junit.framework.TestSuite;

public class ConnectorManager extends TestSetup {

    private static Log LOG = LogFactory.getLog(ConnectorManager.class);
	private TestSuite suite;
	private Map<String, Connector> connectors = new HashMap<String, Connector>();

	
	public ConnectorManager(TestSuite suite) {
		super(suite);
		this.suite = suite;
	}

    @Override
	protected void setUp() throws Exception {
		Enumeration<?> i = suite.tests();
		while (i.hasMoreElements()) {
			IdMUnitTestCase test = (IdMUnitTestCase)i.nextElement();
			if (test.isDisabled() == false) {
				ConnectionConfigData config = test.getConfig();
				if (config != null) {
	                String disabled = config.getParam(ConnectionConfigData.DISABLED); 
	                if (disabled == null || Boolean.parseBoolean(disabled) == false) {
						Connector connector = connectors.get(config.getName());
						if (connector == null) {
							try {
								connector = setupConnector(config);
							} catch (IdMUnitException e) {
								LOG.error("Error setting up connector '" + config.getName() + "'. " + e.getMessage());
								throw e;
							}
							connectors.put(config.getName(), connector);
						}
						test.setConnector(connector);
	                }
				}
			}
		}
	}

	@Override
	protected void tearDown() throws Exception {
		for (String name : connectors.keySet()) {
			connectors.get(name).tearDown();
		}
		connectors.clear();
	}
	
    private Connector setupConnector(ConnectionConfigData configData) throws IdMUnitException {
        String type = configData.getType();
        
        if (type == null || type.length() < 1) {
            throw new IdMUnitException("Missing target type definition.  Please add a type specifier for all targets defined.");
        }

        Connector connector;
        try {
            Object obj = Class.forName(type).newInstance();
            if (Connector.class.isAssignableFrom(obj.getClass())) {
            	connector = (Connector)obj;
            } else {
            	throw new IdMUnitException("The class '" + type + "' does not implement the Connector interface.");
            }
        } catch (InstantiationException e) {
            throw new IdMUnitException("Failed to instantiate connection class of type: " + type + " " + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new IdMUnitException("Illegal access error when attempting to instantiate connection of type: " + type);
        } catch (ClassNotFoundException e) {
            throw new IdMUnitException("Specified target connection module not found: " + type);
        }
        connector.setup(configData.getParams());
        return connector;
    }
    
    public TestSuite getSuite() {
    	return suite;
    }
}
