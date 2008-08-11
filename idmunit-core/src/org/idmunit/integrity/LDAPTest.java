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
package org.idmunit.integrity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.idmunit.IdMUnitException;
import org.idmunit.connector.Connector;
import org.idmunit.connector.LDAP;

import junit.framework.TestCase;

/**
 * This class is mostly a prototype for testing an LDAP connector.  It should be refactored to more accurately validate the existing LDAP connections.
 * <br>
 * Up to this point in the development of the project, integrity and basic functionality testing of IdMUnit has been conducted by running the test cases
 * developed for commercial IdMUnit users.  Regression testing in this manner has exposed any anomalies caused by refactoring or further development.  This 
 * process has become more and more infeasible as the number of commercial IdMUnit users has grown however.  The need for stand-alone, system-agnostic test cases
 * for IdMUnit exists and should better covered in future releases of the project.
 * @author Brent Kynaston, Software Engineer, TriVir LLC
 * @version %I%, %G%
 */
public class LDAPTest extends TestCase {
	private static final String CONFIG_KEYSTORE_PATH = "KeystorePath";
    private static final String CONFIG_PASSWORD = "Password";
    private static final String CONFIG_SERVER = "Server";
    private static final String CONFIG_USER = "User";

	Connector m_connection;
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testInstantiateLDAPClass() {
		new LDAP();
	}

	public void testAddObjectMultiAttr() {
		String dn = "cn=autoTestUser1,o=resources"; 
		Map<String, Collection<String>> dataRow = new HashMap<String, Collection<String>>();
        putSingleValue(dataRow, "dn",dn);
        dataRow.put("objectclass", Arrays.asList(new String[] {"top", "inetOrgPerson"}));
        putSingleValue(dataRow, "cn", "autoTestUser1");
        putSingleValue(dataRow, "sn", "autoLastName");
        putSingleValue(dataRow, "description", "Some description");

		try {
			Map<String, String> config = new HashMap<String, String>();
			config.put(CONFIG_PASSWORD, "trivir");
			config.put(CONFIG_SERVER, "10.10.10.10");
			config.put(CONFIG_USER, "cn=admin,o=resources");
			m_connection = new LDAP();
			m_connection.setup(config);
			m_connection.execute("addObject", dataRow);
		} catch (IdMUnitException e) {
			if(e.getMessage().indexOf("already exists")!=-1) {
				Map<String, Collection<String>> attrs = new HashMap<String, Collection<String>>();
				putSingleValue(attrs, "dn", dn);
				try {
					m_connection.execute("deleteObject", attrs);
					m_connection.execute("addObject", dataRow);
				} catch (IdMUnitException e1) {
					fail("Failed to clean up and re-add object: " + e.getMessage());
				}
			} else {
				fail("Failed to add object: " + e.getMessage());
			}
		}
	}

	public void testAddObject() {
		String dn = "cn=autoTestUser1,o=users"; 
		Map<String, Collection<String>> dataRow = new HashMap<String, Collection<String>>();
        putSingleValue(dataRow, "dn",dn);
        dataRow.put("objectclass", Arrays.asList(new String[] {"top", "inetOrgPerson"}));
        putSingleValue(dataRow, "cn", "autoTestUser1");
        putSingleValue(dataRow, "sn", "autoLastName");
        putSingleValue(dataRow, "description", "Some description");

		try {
			m_connection.execute("addObject", dataRow);
		} catch (IdMUnitException e) {
			if(e.getMessage().indexOf("already exists")!=-1) {
				Map<String, Collection<String>> attrs = new HashMap<String, Collection<String>>();
				putSingleValue(attrs, "dn", dn);
				try {
					m_connection.execute("deleteObject", attrs);
					m_connection.execute("addObject", dataRow);
				} catch (IdMUnitException e1) {
					fail("Failed to clean up and re-add object: " + e.getMessage());
				}
			} else {
				fail("Failed to add object: " + e.getMessage());
			}
		}
	}
	
	public void testModifyObject() {
		String dn = "cn=autoTestUser1,o=users"; 
		Map<String, Collection<String>> dataRow = new HashMap<String, Collection<String>>();
        putSingleValue(dataRow, "dn",dn);
        dataRow.put("objectclass", Arrays.asList(new String[] {"top", "inetOrgPerson"}));
        putSingleValue(dataRow, "cn", "autoTestUser1");
        putSingleValue(dataRow, "sn", "autoLastName");
        putSingleValue(dataRow, "description", "Some description");

		Map<String, Collection<String>> modificationAttrs = new HashMap<String, Collection<String>>();
		putSingleValue(modificationAttrs, "dn", dn);
		putSingleValue(modificationAttrs, "givenname", "Updated Given Name");
		putSingleValue(modificationAttrs, "sn", "New Surname");
		try {
			m_connection.execute("addObject", dataRow);
			m_connection.execute("modObject", modificationAttrs);
		} catch (IdMUnitException e) {
			if(e.getMessage().indexOf("already exists")!=-1) {
				Map<String, Collection<String>> attrs = new HashMap<String, Collection<String>>();
				putSingleValue(attrs, "dn", dn);
				try {
					m_connection.execute("deleteObject", attrs);
					m_connection.execute("addObject", dataRow);
					m_connection.execute("modObject", modificationAttrs);
				} catch (IdMUnitException e1) {
					fail("Failed to clean up and re-add object: " + e.getMessage());
				}
			} else {
				fail("Failed to add object: " + e.getMessage());
			}
		}
	}

	public void testModifyAddAttr() {
		String dn = "cn=autoTestUser1,o=users"; 
		Map<String, Collection<String>> dataRow = new HashMap<String, Collection<String>>();
        putSingleValue(dataRow, "dn",dn);
        dataRow.put("objectclass", Arrays.asList(new String[] {"top", "inetOrgPerson"}));
        putSingleValue(dataRow, "cn", "autoTestUser1");
        putSingleValue(dataRow, "sn", "autoLastName");
        putSingleValue(dataRow, "description", "Some description");

		Map<String, Collection<String>> modificationAttrs = new HashMap<String, Collection<String>>();
		putSingleValue(modificationAttrs, "dn", dn);
		putSingleValue(modificationAttrs, "givenname", "Updated Given Name");
		putSingleValue(modificationAttrs, "sn", "New Surname");

		Map<String, Collection<String>> multiValueAttr = new HashMap<String, Collection<String>>();
		putSingleValue(multiValueAttr, "dn", dn);
		putSingleValue(multiValueAttr, "description", "myTestDescription1");
		try {
			m_connection.execute("addObject", dataRow);
			m_connection.execute("modObject", modificationAttrs);
			m_connection.execute("addAttr", multiValueAttr);
		} catch (IdMUnitException e) {
			if(e.getMessage().indexOf("already exists")!=-1) {
				Map<String, Collection<String>> attrs = new HashMap<String, Collection<String>>();
				putSingleValue(attrs, "dn", dn);
				try {
					m_connection.execute("deleteObject", attrs);
					m_connection.execute("addObject", dataRow);
					m_connection.execute("modObject", modificationAttrs);
					m_connection.execute("addAttr", multiValueAttr);
				} catch (IdMUnitException e1) {
					fail("Failed to clean up and re-add object: " + e.getMessage());
				}
			} else {
				fail("Failed to add object: " + e.getMessage());
			}
		}
	}
	
	public void testModifyClearAttr() {
		String dn = "cn=autoTestUser1,o=users"; 
		Map<String, Collection<String>> dataRow = new HashMap<String, Collection<String>>();
        putSingleValue(dataRow, "dn",dn);
        dataRow.put("objectclass", Arrays.asList(new String[] {"top", "inetOrgPerson"}));
        putSingleValue(dataRow, "cn", "autoTestUser1");
        putSingleValue(dataRow, "sn", "autoLastName");
        putSingleValue(dataRow, "description", "Some description");

		Map<String, Collection<String>> modificationAttrs = new HashMap<String, Collection<String>>();
		putSingleValue(modificationAttrs, "dn", dn);
		putSingleValue(modificationAttrs, "givenname", "Updated Given Name");
		putSingleValue(modificationAttrs, "sn", "New Surname");

		Map<String, Collection<String>> multiValueAttr = new HashMap<String, Collection<String>>();
		putSingleValue(multiValueAttr, "dn", dn);
		putSingleValue(multiValueAttr, "description", "Some description");
		try {
			Map<String, Collection<String>> attrs = new HashMap<String, Collection<String>>();
			putSingleValue(attrs, "dn", dn);
			m_connection.execute("deleteObject", attrs);
			Thread.sleep(1000);
			m_connection.execute("addObject", dataRow);
			m_connection.execute("modObject", modificationAttrs);
			m_connection.execute("removeAttr", multiValueAttr);
		} catch (IdMUnitException e) {
				fail("Failed to mod object: " + e.getMessage());
		} catch (InterruptedException ie) {
			fail("Op Interuppted");
		}
	}

	public void testMoveRenameObject() {
		String dn = "cn=autoTestUser2,o=users"; 
		Map<String, Collection<String>> dataRow = new HashMap<String, Collection<String>>();
        putSingleValue(dataRow, "dn", dn);
        dataRow.put("objectclass", Arrays.asList(new String[] {"top", "inetOrgPerson"}));
        putSingleValue(dataRow, "cn", "autoTestUser1");
        putSingleValue(dataRow, "sn", "autoLastName");
        putSingleValue(dataRow, "description", "Some description");

		try {
			Map<String, Collection<String>> deleteAttrs = new HashMap<String, Collection<String>>();
			putSingleValue(deleteAttrs, "dn", dn);
			m_connection.execute("deleteObject", deleteAttrs);
			putSingleValue(deleteAttrs, "dn", "cn=autoTestUser2NEW,ou=test,o=users");
			m_connection.execute("deleteObject", deleteAttrs);
			m_connection.execute("addObject", dataRow);
			Map<String, Collection<String>> modificationAttrs = new HashMap<String, Collection<String>>();
			putSingleValue(modificationAttrs, "dn", dn);
			putSingleValue(modificationAttrs, "newdn", "cn=autoTestUser2NEW,ou=test,o=users");
			m_connection.execute("moveObject", modificationAttrs);
		} catch (IdMUnitException e) {
			if(e.getMessage().indexOf("already exists")!=-1) {
				Map<String, Collection<String>> attrs = new HashMap<String, Collection<String>>();
				putSingleValue(attrs, "dn", dn);
				try {
					m_connection.execute("deleteObject", attrs);
					m_connection.execute("addObject", dataRow);
					Map<String, Collection<String>> modificationAttrs = new HashMap<String, Collection<String>>();
					putSingleValue(modificationAttrs, "dn", dn);
					putSingleValue(modificationAttrs, "newdn", "cn=autoTestUser2NEW,ou=test,o=users");
					m_connection.execute("moveObject", modificationAttrs);
				} catch (IdMUnitException e1) {
					fail("Failed to clean up and re-add object: " + e.getMessage());
				}
			} else {
				fail("Failed to add object: " + e.getMessage());
			}
		}
	}

	private void putSingleValue(Map<String, Collection<String>> map, String name, String value) {
		Collection<String> values = new ArrayList<String>();
		map.put(name, values);
	}

	public void testModifyPassword() {
		String dn = "cn=autoTestUser1,o=users";
		Map<String, Collection<String>> dataRow = new HashMap<String, Collection<String>>();
        putSingleValue(dataRow, "dn", dn);
        dataRow.put("objectclass", Arrays.asList(new String[] {"top", "inetOrgPerson"}));
        putSingleValue(dataRow, "cn", "autoTestUser1");
        putSingleValue(dataRow, "sn", "autoLastName");
        putSingleValue(dataRow, "description", "Some description");

		Map<String, Collection<String>> modificationAttrs = new HashMap<String, Collection<String>>();
		putSingleValue(modificationAttrs, "dn", dn);
		putSingleValue(modificationAttrs, "userPassword", "trivir#111");

		Connector adMgr = null;
		try {
			Map<String, String> config = new HashMap<String, String>();
			config.put(CONFIG_PASSWORD, "trivir");
			config.put(CONFIG_SERVER, "192.168.189.135");
			config.put(CONFIG_USER, "cn=administrator,cn=users,dc=trivir,dc=com");
			adMgr = new LDAP();
			adMgr.setup(config);
			m_connection.execute("addObject", dataRow);
			m_connection.execute("modObject", modificationAttrs);
			putSingleValue(modificationAttrs, "dn", "cn=autoTestUser1,cn=users,dc=trivir,dc=com");
			adMgr.execute("validatePassword", modificationAttrs);
		} catch (IdMUnitException e) {
			if(e.getMessage().indexOf("already exists")!=-1) {
				Map<String, Collection<String>> attrs = new HashMap<String, Collection<String>>();
				putSingleValue(attrs, "dn", dn);
				try {
					m_connection.execute("deleteObject", attrs);
					m_connection.execute("addObject", dataRow);
					m_connection.execute("modObject", modificationAttrs);
					Thread.sleep(5000);
					putSingleValue(modificationAttrs, "dn", "cn=autoTestUser1,cn=users,dc=trivir,dc=com");
					adMgr.execute("validatePassword", modificationAttrs);
				} catch (IdMUnitException e1) {
					fail("Failed to clean up and re-add object: " + e1.getMessage());
				} catch (InterruptedException ie) {
					System.out.println("interupted");
				}
			} else {
				fail("Failed to add object: " + e.getMessage());
			}
		}
	}

	public void testAddADUser() {
		String dn = "cn=testuser,CN=Users,DC=trivir,DC=com"; 
		Map<String, Collection<String>> dataRow = new HashMap<String, Collection<String>>();
        putSingleValue(dataRow, "dn",dn);
        putSingleValue(dataRow, "objectClass", "user");
        putSingleValue(dataRow, "samAccountName", "testuser");
        putSingleValue(dataRow, "cn", "testuser");
       // dataRow.addValue(new DataValueBean("userPassword", "testuser#1"));
        //dataRow.addValue(new DataValueBean("userPrincipalName", "testuser@idmunit.org"));
        putSingleValue(dataRow, "sn", "autoLastName");
        //dataRow.addValue(new DataValueBean("memberOf", "CN=UK-Intranet,CN=Users,DC=trivir,DC=com"));
        putSingleValue(dataRow, "givenName", "firstVal");
        //dataRow.addValue(new DataValueBean("userAccountControl", "512"));
        
        
        //dataRow.addValue(new DataValueBean("fullName", "Fall Childs"));
        //dataRow.addValue(new DataValueBean("description", "Some description"));

		Map<String, Collection<String>> modificationAttrs = new HashMap<String, Collection<String>>();
		putSingleValue(modificationAttrs, "dn", "CN=UK-Intranet,CN=Users,DC=trivir,DC=com");
		putSingleValue(modificationAttrs, "member", dn);

		Connector adMgr = null;
		try {
			Map<String, String> config = new HashMap<String, String>();
			config.put(CONFIG_PASSWORD, "trivir");
			config.put(CONFIG_SERVER, "192.168.189.135");
			config.put(CONFIG_USER, "cn=administrator,cn=users,dc=trivir,dc=com");
			adMgr = new LDAP();
			adMgr.setup(config);
			adMgr.execute("addObject", dataRow);
			adMgr.execute("modObject", modificationAttrs);
		} catch (IdMUnitException e) {
			if(e.getMessage().indexOf("EXISTS")!=-1) {
				Map<String, Collection<String>> attrs = new HashMap<String, Collection<String>>();
				putSingleValue(attrs, "dn", dn);
				try {
					adMgr.execute("deleteObject", attrs);
					adMgr.execute("addObject", dataRow);
					adMgr.execute("modObject", modificationAttrs);
				} catch (IdMUnitException e1) {
					fail("Failed to clean up and re-add object: " + e1.getMessage());
				} 
			} else {
				fail("Failed to add object: " + e.getMessage());
			}
		}
	}
	public void testAddUserInGtEnv() {
		String keyStorePath = "D:\\IdMUnit\\util\\security\\keystore";
		String dn = "cn=testuser,CN=Users,dc=test,dc=tst"; 
		Map<String, Collection<String>> dataRow = new HashMap<String, Collection<String>>();
        putSingleValue(dataRow, "dn",dn);
        putSingleValue(dataRow, "objectClass", "user");
        putSingleValue(dataRow, "samAccountName", "testuser");
        putSingleValue(dataRow, "cn", "testuser");
        
        putSingleValue(dataRow, "sn", "autoLastName");
        putSingleValue(dataRow, "givenName", "firstVal");
        putSingleValue(dataRow, "userAccountControl", "512");
        putSingleValue(dataRow, "unicodePwd", "myPassword#1");
        
		Map<String, Collection<String>> modificationAttrs = new HashMap<String, Collection<String>>();
		putSingleValue(modificationAttrs, "dn", "CN=test,DC=test,DC=tst");
		putSingleValue(modificationAttrs, "member", dn);

		Connector adMgr = null;
		try {
			Map<String, String> config = new HashMap<String, String>();
			config.put(CONFIG_KEYSTORE_PATH, keyStorePath);
			config.put(CONFIG_PASSWORD, "ENCRYPTEDPASSWORDVAL");
			config.put(CONFIG_SERVER, "xxx.xxx.xxx.xxx");
			config.put(CONFIG_USER, "cn=Administrator,CN=test,DC=test,DC=tst");
			adMgr = new LDAP();
			adMgr.setup(config);
			Map<String, Collection<String>> attrs = new HashMap<String, Collection<String>>();
			putSingleValue(attrs, "dn", dn);
			adMgr.execute("deleteObject", attrs);
			adMgr.execute("addObject", dataRow);
		//	adMgr.modObject(modificationAttrs, DirContext.REPLACE_ATTRIBUTE);
		} catch (IdMUnitException e) {
			if(e.getMessage().indexOf("EXISTS")!=-1) {
				Map<String, Collection<String>> attrs = new HashMap<String, Collection<String>>();
				putSingleValue(attrs, "dn", dn);
				try {
					adMgr.execute("deleteObject", attrs);
					adMgr.execute("addObject", dataRow);
					adMgr.execute("modObject", modificationAttrs);
				} catch (IdMUnitException e1) {
					fail("Failed to clean up and re-add object: " + e1.getMessage());
				} 
			} else {
				fail("Failed to add object: " + e.getMessage());
			}
		}
	}
	public void testAddUserInQCADEnv() {
		for (int ctr=0;;++ctr) {
			System.out.println("######### Counter: " + ctr);
			String keyStorePath = "E:\\IdMUnit\\util\\security\\keystore";
			String dn = "cn=AutoTestUser4,ou=test,o=tst"; 
			Map<String, Collection<String>> dataRow = new HashMap<String, Collection<String>>();
	        putSingleValue(dataRow, "dn",dn);
	        putSingleValue(dataRow, "objectClass", "user");
	        putSingleValue(dataRow, "samAccountName", "TestUser4");
	        putSingleValue(dataRow, "cn", "TestUser4");
	        
	        putSingleValue(dataRow, "sn", "autoLastName");
	        putSingleValue(dataRow, "givenName", "firstVal");
	        putSingleValue(dataRow, "userAccountControl", "512");
	        putSingleValue(dataRow, "unicodePwd", "myPassword#1");
	        
			Connector adMgr = null;
			try {
				Map<String, String> config = new HashMap<String, String>();
				config.put(CONFIG_KEYSTORE_PATH, keyStorePath);
				config.put(CONFIG_PASSWORD, "ENCRYPTEDPASSWORDVAL");
				config.put(CONFIG_SERVER, "xxx.xxx.xxx.xxx");
				config.put(CONFIG_USER, "cn=Administrator,CN=test,DC=test,DC=tst");
				adMgr = new LDAP();
				adMgr.setup(config);
				Map<String, Collection<String>> attrs = new HashMap<String, Collection<String>>();
				putSingleValue(attrs, "dn", dn);
				adMgr.execute("deleteObject", attrs);
				adMgr.execute("addObject", dataRow); 
				adMgr.tearDown();
	
			//	adMgr.modObject(modificationAttrs, DirContext.REPLACE_ATTRIBUTE);
			} catch (IdMUnitException e) {
				if(e.getMessage().indexOf("EXISTS")!=-1) {
					Map<String, Collection<String>> attrs = new HashMap<String, Collection<String>>();
					putSingleValue(attrs, "dn", dn);
					try {
						adMgr.execute("deleteObject", attrs);
						adMgr.execute("addObject", dataRow);
					} catch (IdMUnitException e1) {
						fail("Failed to clean up and re-add object: " + e1.getMessage());
					} 
				} else {
					fail("Failed to add object: " + e.getMessage());
				}
			}
		}
	}

	public void testAddUserInQCIVEnv() {
		for (int ctr=0;;++ctr) {
			System.out.println("######### Counter: " + ctr);
			String dn = "cn=AutoTestUser4,ou=test,o=tst"; 
			Map<String, Collection<String>> dataRow = new HashMap<String, Collection<String>>();
	        putSingleValue(dataRow, "dn",dn);
	        putSingleValue(dataRow, "objectClass", "inetorgperson");
	        putSingleValue(dataRow, "cn", "AutoTestUser4");
	        
	        putSingleValue(dataRow, "sn", "autoLastName");
	        putSingleValue(dataRow, "givenName", "firstVal");
	        
			Connector connectionManager = null;
			try {
				Map<String, String> config = new HashMap<String, String>();
				config.put(CONFIG_PASSWORD, "ENCRYPTEDPASSWORDVAL");
				config.put(CONFIG_SERVER, "xxx.xxx.xxx.xxx");
				config.put(CONFIG_USER, "cn=Administrator,CN=test,DC=test,DC=tst");
				connectionManager = new LDAP();
				connectionManager.setup(config);
				Map<String, Collection<String>> attrs = new HashMap<String, Collection<String>>();
				putSingleValue(attrs, "dn", dn);
				Map<String, Collection<String>> modificationAttrs = new HashMap<String, Collection<String>>();
				putSingleValue(modificationAttrs, "dn", dn);
				putSingleValue(modificationAttrs, "description", "tempval");
				connectionManager.execute("validateObject", modificationAttrs);
				connectionManager.tearDown();
			} catch (IdMUnitException e) {
				fail("Failed to add object: " + e.getMessage());
			}
		}
	}

	public void testReadnsaccountlockFromiPlanet() {
		String dn = "uid=TestTSTLOCK,ou=test,o=tst"; 
		Map<String, Collection<String>> dataRow = new HashMap<String, Collection<String>>();
        putSingleValue(dataRow, "dn",dn);
        putSingleValue(dataRow, "cn","LOCKUser, Test");
        putSingleValue(dataRow, "objectClass", "qcPerson");
        
        putSingleValue(dataRow, "sn", "autoLastName");
        putSingleValue(dataRow, "givenName", "firstVal");
	        
		Connector connectionMgr = null;
		try {
			Map<String, String> config = new HashMap<String, String>();
			config.put(CONFIG_PASSWORD, "ENCRYPTEDPASSWORDVAL");
			config.put(CONFIG_SERVER, "xxx.xxx.xxx.xxx");
			config.put(CONFIG_USER, "cn=dirmgr");
			connectionMgr = new LDAP();
			connectionMgr.setup(config);
			Map<String, Collection<String>> attrs = new HashMap<String, Collection<String>>();
			putSingleValue(attrs, "dn", dn);
			connectionMgr.execute("deleteObject", attrs);
			connectionMgr.execute("addObject", dataRow);
			//Set the nsaccountlock attribute
			Map<String, Collection<String>> modAttrs = new HashMap<String, Collection<String>>();
			putSingleValue(modAttrs, "nsaccountlock", "true");
			putSingleValue(modAttrs, "dn", dn);
			connectionMgr.execute("modObject", modAttrs);
			//Read the nsaccountlock attribute
			Map<String, Collection<String>> newAttrs = new HashMap<String, Collection<String>>();
			putSingleValue(newAttrs, "dn", dn);
			putSingleValue(newAttrs, "uid", "TestTSTLOCK");
			connectionMgr.execute("validateObject", newAttrs);
			connectionMgr.tearDown();
	
		//	adMgr.modObject(modificationAttrs, DirContext.REPLACE_ATTRIBUTE);
		} catch (IdMUnitException e) {
			if(e.getMessage().indexOf("EXISTS")!=-1) {
				Map<String, Collection<String>> attrs = new HashMap<String, Collection<String>>();
				putSingleValue(attrs, "dn", dn);
				try {
					connectionMgr.execute("deleteObject", attrs);
					connectionMgr.execute("addObject", dataRow);
				} catch (IdMUnitException e1) {
					fail("Failed to clean up and re-add object: " + e1.getMessage());
				} 
			} else {
				fail("Failed to add object: " + e.getMessage());
			}
		}
	}
}
