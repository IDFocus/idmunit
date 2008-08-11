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

import java.util.Iterator;
import java.util.Map;

import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;

import org.ddsteps.dataset.bean.DataRowBean;
import org.ddsteps.dataset.bean.DataValueBean;
import org.idmunit.IdMUnitException;
import org.idmunit.connector.Connection;
import org.idmunit.connector.ConnectionConfigData;
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
	Connection m_connection;
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testInstantiateLDAPClass() {
		Connection ldapConnection = new LDAP();
	}

	public void testAddObjectMultiAttr() {
		String dn = "cn=autoTestUser1,o=resources"; 
		DataRowBean dataRow = new DataRowBean();
	        dataRow.addValue(new DataValueBean("dn",dn));
	        dataRow.addValue(new DataValueBean("objectclass", new String[] { "entitlement", "inetOrgPerson", "organizationalPerson"}));
	        dataRow.addValue(new DataValueBean("cn", "autoTestUser1"));
	        dataRow.addValue(new DataValueBean("sn", "autoLastName"));
	        dataRow.addValue(new DataValueBean("description", new String[] { "Some description1", "Some description2", "Some description3" }));

		try {
			ConnectionConfigData creds = new ConnectionConfigData("10.10.10.10", "cn=admin,o=resources", "trivir");
			m_connection = new LDAP(creds);
			m_connection.addObject(dataRow);
		} catch (IdMUnitException e) {
			if(e.getMessage().indexOf("already exists")!=-1) {
				Attributes attrs = new BasicAttributes();
				attrs.put("dn", dn);
				try {
					m_connection.deleteObject(attrs);
					m_connection.addObject(dataRow);
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
		DataRowBean dataRow = new DataRowBean();
	        dataRow.addValue(new DataValueBean("dn",dn));
	        dataRow.addValue(new DataValueBean("objectclass", new String[] { "top", "inetOrgPerson" }));
	        dataRow.addValue(new DataValueBean("cn", "autoTestUser1"));
	        dataRow.addValue(new DataValueBean("sn", "autoLastName"));
	        dataRow.addValue(new DataValueBean("description", "Some description"));

		try {
			m_connection.addObject(dataRow);
		} catch (IdMUnitException e) {
			if(e.getMessage().indexOf("already exists")!=-1) {
				Attributes attrs = new BasicAttributes();
				attrs.put("dn", dn);
				try {
					m_connection.deleteObject(attrs);
					m_connection.addObject(dataRow);
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
		DataRowBean dataRow = new DataRowBean();
	        dataRow.addValue(new DataValueBean("dn",dn));
	        dataRow.addValue(new DataValueBean("objectclass", new String[] { "top", "inetOrgPerson" }));
	        dataRow.addValue(new DataValueBean("cn", "autoTestUser1"));
	        dataRow.addValue(new DataValueBean("sn", "autoLastName"));
	        dataRow.addValue(new DataValueBean("description", "Some description"));

			Attributes modificationAttrs= new BasicAttributes();
			modificationAttrs.put("dn", dn);
			modificationAttrs.put("givenname", "Updated Given Name");
			modificationAttrs.put("sn", "New Surname");
		try {
			m_connection.addObject(dataRow);
			m_connection.modObject(modificationAttrs, DirContext.REPLACE_ATTRIBUTE);
		} catch (IdMUnitException e) {
			if(e.getMessage().indexOf("already exists")!=-1) {
				Attributes attrs = new BasicAttributes();
				attrs.put("dn", dn);
				try {
					m_connection.deleteObject(attrs);
					m_connection.addObject(dataRow);
					m_connection.modObject(modificationAttrs, DirContext.REPLACE_ATTRIBUTE);
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
		DataRowBean dataRow = new DataRowBean();
	        dataRow.addValue(new DataValueBean("dn",dn));
	        dataRow.addValue(new DataValueBean("objectclass", new String[] { "top", "inetOrgPerson" }));
	        dataRow.addValue(new DataValueBean("cn", "autoTestUser1"));
	        dataRow.addValue(new DataValueBean("sn", "autoLastName"));
	        dataRow.addValue(new DataValueBean("description", "Some description"));

			Attributes modificationAttrs= new BasicAttributes();
			modificationAttrs.put("dn", dn);
			modificationAttrs.put("givenname", "Updated Given Name");
			modificationAttrs.put("sn", "New Surname");

			Attributes multiValueAttr= new BasicAttributes();
			multiValueAttr.put("dn", dn);
			multiValueAttr.put("description", "myTestDescription1");
		try {
			m_connection.addObject(dataRow);
			m_connection.modObject(modificationAttrs, DirContext.REPLACE_ATTRIBUTE);
			m_connection.modObject(multiValueAttr, DirContext.ADD_ATTRIBUTE);
		} catch (IdMUnitException e) {
			if(e.getMessage().indexOf("already exists")!=-1) {
				Attributes attrs = new BasicAttributes();
				attrs.put("dn", dn);
				try {
					m_connection.deleteObject(attrs);
					m_connection.addObject(dataRow);
					m_connection.modObject(modificationAttrs, DirContext.REPLACE_ATTRIBUTE);
					m_connection.modObject(multiValueAttr, DirContext.ADD_ATTRIBUTE);
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
		DataRowBean dataRow = new DataRowBean();
	        dataRow.addValue(new DataValueBean("dn",dn));
	        dataRow.addValue(new DataValueBean("objectclass", new String[] { "top", "inetOrgPerson" }));
	        dataRow.addValue(new DataValueBean("cn", "autoTestUser1"));
	        dataRow.addValue(new DataValueBean("sn", "autoLastName"));
	        dataRow.addValue(new DataValueBean("description", "Some description"));

			Attributes modificationAttrs= new BasicAttributes();
			modificationAttrs.put("dn", dn);
			modificationAttrs.put("givenname", "Updated Given Name");
			modificationAttrs.put("sn", "New Surname");

			Attributes multiValueAttr= new BasicAttributes();
			multiValueAttr.put("dn", dn);
			multiValueAttr.put(new BasicAttribute("description", "Some description"));
		try {
			Attributes attrs = new BasicAttributes();
			attrs.put("dn", dn);
			m_connection.deleteObject(attrs);
			Thread.sleep(1000);
			m_connection.addObject(dataRow);
			m_connection.modObject(modificationAttrs, DirContext.REPLACE_ATTRIBUTE);
			m_connection.modObject(multiValueAttr, DirContext.REMOVE_ATTRIBUTE);
		} catch (IdMUnitException e) {
				fail("Failed to mod object: " + e.getMessage());
		} catch (InterruptedException ie) {
			fail("Op Interuppted");
		}
	}

	public void testMoveRenameObject() {
		String dn = "cn=autoTestUser2,o=users"; 
		DataRowBean dataRow = new DataRowBean();
	        dataRow.addValue(new DataValueBean("dn",dn));
	        dataRow.addValue(new DataValueBean("objectclass", new String[] { "top", "inetOrgPerson" }));
	        dataRow.addValue(new DataValueBean("cn", "autoTestUser1"));
	        dataRow.addValue(new DataValueBean("sn", "autoLastName"));
	        dataRow.addValue(new DataValueBean("description", "Some description"));

		try {
			Attributes deleteAttrs= new BasicAttributes();
			deleteAttrs.put("dn", dn);
			m_connection.deleteObject(deleteAttrs);
			deleteAttrs.put("dn", "cn=autoTestUser2NEW,ou=test,o=users");
			m_connection.deleteObject(deleteAttrs);
			m_connection.addObject(dataRow);
			Attributes modificationAttrs= new BasicAttributes();
			modificationAttrs.put("dn", dn);
			modificationAttrs.put("newdn", "cn=autoTestUser2NEW,ou=test,o=users");
			m_connection.moveObject(modificationAttrs);
		} catch (IdMUnitException e) {
			if(e.getMessage().indexOf("already exists")!=-1) {
				Attributes attrs = new BasicAttributes();
				attrs.put("dn", dn);
				try {
					m_connection.deleteObject(attrs);
					m_connection.addObject(dataRow);
					Attributes modificationAttrs= new BasicAttributes();
					modificationAttrs.put("dn", dn);
					modificationAttrs.put("newdn", "cn=autoTestUser2NEW,ou=test,o=users");
					m_connection.moveObject(modificationAttrs);
				} catch (IdMUnitException e1) {
					fail("Failed to clean up and re-add object: " + e.getMessage());
				}
			} else {
				fail("Failed to add object: " + e.getMessage());
			}
		}
	}

	public void testModifyPassword() {
		String dn = "cn=autoTestUser1,o=users"; 
		DataRowBean dataRow = new DataRowBean();
	        dataRow.addValue(new DataValueBean("dn",dn));
	        dataRow.addValue(new DataValueBean("objectclass", new String[] { "top", "inetOrgPerson" }));
	        dataRow.addValue(new DataValueBean("cn", "autoTestUser1"));
	        dataRow.addValue(new DataValueBean("sn", "autoLastName"));
	        dataRow.addValue(new DataValueBean("description", "Some description"));

			Attributes modificationAttrs= new BasicAttributes();
			modificationAttrs.put("dn", dn);
			modificationAttrs.put("userPassword", "trivir#111");

			Connection adMgr = null;
			try {
			ConnectionConfigData creds = new ConnectionConfigData("192.168.189.135", "cn=administrator,cn=users,dc=trivir,dc=com", "trivir");
				adMgr = new LDAP(creds);
			m_connection.addObject(dataRow);
			m_connection.modObject(modificationAttrs, DirContext.REPLACE_ATTRIBUTE);
			modificationAttrs.put("dn", "cn=autoTestUser1,cn=users,dc=trivir,dc=com");
			adMgr.validatePassword(modificationAttrs);
		} catch (IdMUnitException e) {
			if(e.getMessage().indexOf("already exists")!=-1) {
				Attributes attrs = new BasicAttributes();
				attrs.put("dn", dn);
				try {
					m_connection.deleteObject(attrs);
					m_connection.addObject(dataRow);
					m_connection.modObject(modificationAttrs, DirContext.REPLACE_ATTRIBUTE);
					Thread.sleep(5000);
					modificationAttrs.put("dn", "cn=autoTestUser1,cn=users,dc=trivir,dc=com");
					adMgr.validatePassword(modificationAttrs);
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
		DataRowBean dataRow = new DataRowBean();
	        dataRow.addValue(new DataValueBean("dn",dn));
	        dataRow.addValue(new DataValueBean("objectClass", "user"));
	        dataRow.addValue(new DataValueBean("samAccountName", "testuser"));
	        dataRow.addValue(new DataValueBean("cn", "testuser"));
	       // dataRow.addValue(new DataValueBean("userPassword", "testuser#1"));
	        //dataRow.addValue(new DataValueBean("userPrincipalName", "testuser@idmunit.org"));
	        dataRow.addValue(new DataValueBean("sn", "autoLastName"));
	        //dataRow.addValue(new DataValueBean("memberOf", "CN=UK-Intranet,CN=Users,DC=trivir,DC=com"));
	        dataRow.addValue(new DataValueBean("givenName", "firstVal"));
	        //dataRow.addValue(new DataValueBean("userAccountControl", "512"));
	        
	        
	        //dataRow.addValue(new DataValueBean("fullName", "Fall Childs"));
	        //dataRow.addValue(new DataValueBean("description", "Some description"));

			Attributes modificationAttrs= new BasicAttributes();
			modificationAttrs.put("dn", "CN=UK-Intranet,CN=Users,DC=trivir,DC=com");
			modificationAttrs.put("member", dn);

			Connection adMgr = null;
			try {
				ConnectionConfigData creds = new ConnectionConfigData("192.168.189.135", "cn=administrator,cn=users,dc=trivir,dc=com", "trivir");
				adMgr = new LDAP(creds);
			adMgr.addObject(dataRow);
			adMgr.modObject(modificationAttrs, DirContext.REPLACE_ATTRIBUTE);
		} catch (IdMUnitException e) {
			if(e.getMessage().indexOf("EXISTS")!=-1) {
				Attributes attrs = new BasicAttributes();
				attrs.put("dn", dn);
				try {
					adMgr.deleteObject(attrs);
					adMgr.addObject(dataRow);
					adMgr.modObject(modificationAttrs, DirContext.REPLACE_ATTRIBUTE);
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
		DataRowBean dataRow = new DataRowBean();
	        dataRow.addValue(new DataValueBean("dn",dn));
	        dataRow.addValue(new DataValueBean("objectClass", "user"));
	        dataRow.addValue(new DataValueBean("samAccountName", "testuser"));
	        dataRow.addValue(new DataValueBean("cn", "testuser"));
	        
	        dataRow.addValue(new DataValueBean("sn", "autoLastName"));
	        dataRow.addValue(new DataValueBean("givenName", "firstVal"));
	        dataRow.addValue(new DataValueBean("userAccountControl", "512"));
	        dataRow.addValue(new DataValueBean("unicodePwd", "myPassword#1"));
	        
			Attributes modificationAttrs= new BasicAttributes();
			modificationAttrs.put("dn", "CN=test,DC=test,DC=tst");
			modificationAttrs.put("member", dn);

			Connection adMgr = null;
			try {
				ConnectionConfigData creds = new ConnectionConfigData("xxx.xxx.xxx.xxx", "cn=Administrator,CN=test,DC=test,DC=tst", "ENCRYPTEDPASSWORDVAL");
				creds.setKeystorePath(keyStorePath);
				adMgr = new LDAP(creds);
			Attributes attrs = new BasicAttributes();
			attrs.put("dn", dn);
			adMgr.deleteObject(attrs);
			adMgr.addObject(dataRow);
		//	adMgr.modObject(modificationAttrs, DirContext.REPLACE_ATTRIBUTE);
		} catch (IdMUnitException e) {
			if(e.getMessage().indexOf("EXISTS")!=-1) {
				Attributes attrs = new BasicAttributes();
				attrs.put("dn", dn);
				try {
					adMgr.deleteObject(attrs);
					adMgr.addObject(dataRow);
					adMgr.modObject(modificationAttrs, DirContext.REPLACE_ATTRIBUTE);
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
				DataRowBean dataRow = new DataRowBean();
			        dataRow.addValue(new DataValueBean("dn",dn));
			        dataRow.addValue(new DataValueBean("objectClass", "user"));
			        dataRow.addValue(new DataValueBean("samAccountName", "TestUser4"));
			        dataRow.addValue(new DataValueBean("cn", "TestUser4"));
			        
			        dataRow.addValue(new DataValueBean("sn", "autoLastName"));
			        dataRow.addValue(new DataValueBean("givenName", "firstVal"));
			        dataRow.addValue(new DataValueBean("userAccountControl", "512"));
			        dataRow.addValue(new DataValueBean("unicodePwd", "myPassword#1"));
			        
					Connection adMgr = null;
					try {
						ConnectionConfigData creds = new ConnectionConfigData("xxx.xxx.xxx.xxx", 
								"cn=edirectory,ou=test,dc=test,dc=tst", 
								"ENCRYPTEDPASSWORDVAL");
						creds.setKeystorePath(keyStorePath);
						adMgr = new LDAP(creds);
					Attributes attrs = new BasicAttributes();
					attrs.put("dn", dn);
					adMgr.deleteObject(attrs);
					adMgr.addObject(dataRow); 
					adMgr.closeConnection();
		
				//	adMgr.modObject(modificationAttrs, DirContext.REPLACE_ATTRIBUTE);
				} catch (IdMUnitException e) {
					if(e.getMessage().indexOf("EXISTS")!=-1) {
						Attributes attrs = new BasicAttributes();
						attrs.put("dn", dn);
						try {
							adMgr.deleteObject(attrs);
							adMgr.addObject(dataRow);
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
				DataRowBean dataRow = new DataRowBean();
			        dataRow.addValue(new DataValueBean("dn",dn));
			        dataRow.addValue(new DataValueBean("objectClass", "inetorgperson"));
			        dataRow.addValue(new DataValueBean("cn", "AutoTestUser4"));
			        
			        dataRow.addValue(new DataValueBean("sn", "autoLastName"));
			        dataRow.addValue(new DataValueBean("givenName", "firstVal"));
			        
					Connection connectionManager = null;
					try {
						ConnectionConfigData creds = new ConnectionConfigData("xxx.xxx.xxx.xxx", 
								"cn=admin,o=services", 
								"ENCRYPTEDPASSWORDVAL");
						connectionManager = new LDAP(creds);
					Attributes attrs = new BasicAttributes();
					attrs.put("dn", dn);
					Attributes modificationAttrs= new BasicAttributes();
					modificationAttrs.put("dn", dn);
					modificationAttrs.put("description", "tempval");
					connectionManager.validateObject(modificationAttrs);
					connectionManager.closeConnection();
				} catch (IdMUnitException e) {
					fail("Failed to add object: " + e.getMessage());
				}
		}
}
	public void testReadnsaccountlockFromiPlanet() {
		String dn = "uid=TestTSTLOCK,ou=test,o=tst"; 
		DataRowBean dataRow = new DataRowBean();
        dataRow.addValue(new DataValueBean("dn",dn));
        dataRow.addValue(new DataValueBean("cn","LOCKUser, Test"));
	        dataRow.addValue(new DataValueBean("objectClass", "qcPerson"));
	        
	        dataRow.addValue(new DataValueBean("sn", "autoLastName"));
	        dataRow.addValue(new DataValueBean("givenName", "firstVal"));
	        
			Connection connectionMgr = null;
			try {
				ConnectionConfigData creds = new ConnectionConfigData("xxx.xxx.xxx.xxx", 
						"cn=dirmgr", 
						"ENCRYPTEDPASSWORDVAL");
				connectionMgr = new LDAP(creds);
			Attributes attrs = new BasicAttributes();
			attrs.put("dn", dn);
			connectionMgr.deleteObject(attrs);
			connectionMgr.addObject(dataRow);
			//Set the nsaccountlock attribute
			Attributes modAttrs = new BasicAttributes();
			modAttrs.put("nsaccountlock", "true");
			modAttrs.put("dn", dn);
			connectionMgr.modObject(modAttrs, DirContext.REPLACE_ATTRIBUTE);
			//Read the nsaccountlock attribute
			Attributes newAttrs = new BasicAttributes();
			newAttrs.put("dn", dn);
			newAttrs.put("uid", "TestTSTLOCK");
			connectionMgr.validateObject(newAttrs);
			connectionMgr.closeConnection();
	
		//	adMgr.modObject(modificationAttrs, DirContext.REPLACE_ATTRIBUTE);
		} catch (IdMUnitException e) {
			if(e.getMessage().indexOf("EXISTS")!=-1) {
				Attributes attrs = new BasicAttributes();
				attrs.put("dn", dn);
				try {
					connectionMgr.deleteObject(attrs);
					connectionMgr.addObject(dataRow);
				} catch (IdMUnitException e1) {
					fail("Failed to clean up and re-add object: " + e1.getMessage());
				} 
		} else {
			fail("Failed to add object: " + e.getMessage());
		}
	}
}

	public void testFindUsers() {
		DataRowBean dataRow = new DataRowBean();
	        dataRow.addValue(new DataValueBean("objectclass", new String[] { "top", "inetOrgPerson" }));
	        dataRow.addValue(new DataValueBean("cn", "autoTestUser1"));
	        dataRow.addValue(new DataValueBean("sn", "autoLastName"));
	        dataRow.addValue(new DataValueBean("description", "Some description"));

			Connection adMgr = null;
			try {
			ConnectionConfigData creds = new ConnectionConfigData("SPECIFY", "SPECIFY", "SPECIFY");
				adMgr = new LDAP(creds);
				Map foundUsers = adMgr.search("(objectclass=inetOrgPerson)", "ou=users,o=test", new String[]{"date"});
			System.out.println("Number found: " + foundUsers.size());
			Iterator<String> itr = foundUsers.keySet().iterator();
			while(itr.hasNext()) {
				String dn = itr.next();
				String attrs = (String)foundUsers.get(dn);
				
				System.out.println("Dn: " + dn);
				System.out.println("Attrs: " + attrs);
			}
		} catch (IdMUnitException e) {
			fail("Failed search: " + e.getMessage());
		} 
	}

	
}
