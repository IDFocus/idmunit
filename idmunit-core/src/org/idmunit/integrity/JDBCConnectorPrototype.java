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

import junit.framework.TestCase;
//Package for JDBC classes
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Connection;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

import org.idmunit.Constants;
import org.idmunit.IdMUnitException;
import org.idmunit.connector.Oracle;


/**
 * This class is mostly a prototype for testing a JDBC connector.  It should be refactored to more accurately validate the existing JDBC connections.
 * <br>
 * Up to this point in the development of the project, integrity and basic functionality testing of IdMUnit has been conducted by running the test cases
 * developed for commercial IdMUnit users.  Regression testing in this manner has exposed any anomalies caused by refactoring or further development.  This 
 * process has become more and more infeasible as the number of commercial IdMUnit users has grown however.  The need for stand-alone, system-agnostic test cases
 * for IdMUnit exists and should better covered in future releases of the project.
 * @author Brent Kynaston, Software Engineer, TriVir LLC
 * @version %I%, %G%
 */
public class JDBCConnectorPrototype extends TestCase {
	private Connection m_connection;
	protected void setUp() throws Exception {
		super.setUp();
		m_connection = new Oracle().getConnection("SPECIFY", "SPECIFY", "SPECIFY");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		closeJDBCConnection();
	}

	public void testQueryDumpResults() {
	    ResultSet resultSet = null;
	    Statement stmt      = null;
	    try {
	      stmt = m_connection.createStatement();
	      resultSet = stmt.executeQuery("select * from eventlog");
			resultSet.next(); //only compare first returned row for now
			ResultSetMetaData metaData = resultSet.getMetaData();
			for(int ctr=1;ctr<metaData.getColumnCount();++ctr) {
				String upperCaseAttrName = metaData.getColumnName(ctr).toUpperCase();
				String attrVal = resultSet.getString(ctr);
				System.out.println("Column Name: " + upperCaseAttrName);
				if(attrVal != null && attrVal.length() > 0) {
					String upperCaseAttrVal = attrVal.toUpperCase();
					System.out.println("Column Val: " + upperCaseAttrVal);
				}
			}
	    } catch( SQLException ex ) { 
	      System.out.println("Error in querying the database " + '\n' + ex.toString());
	    } finally {
	      try {
	        stmt.close(); // Close statement which also closes open result sets
	      } catch(SQLException ex) {
	        
	      }
	    }
	}

	public void testQuery() {
		Attributes queryAttrs= new BasicAttributes();
		//the sql query statement
		queryAttrs.put("query", "select * from indirect.indirect_process");
		queryAttrs.put("record_ID", "2");
		queryAttrs.put("table_NaMe", "test");
		try {
			validateObject(queryAttrs);
		} catch (IdMUnitException e) {
			fail("testQuery failure: " + e.getMessage());
		}
	}
	
	public void testSQLInsert() {
	    PreparedStatement stmt      = null;
	    try {
	      stmt = m_connection.prepareStatement( 
	    		  "insert into indirect.indirect_process(RECORD_ID, TABLE_KEY, STATUS, EVENT_TYPE, TABLE_NAME,EVENT_TIME)" 
	    		  + "values(indirect.seq_usr_idu.nextval, 'KEY3', 'N', 7, 'test3', TO_DATE('2003-03-03 03:03:03', 'YYYY-MM-DD HH24:MI:SS'))");

	      boolean success = stmt.execute();
	      System.out.println("Success was: " + success);

            System.out.println("Updated");
	    } catch( SQLException ex ) { 
	      System.out.println("Error in querying the database " + '\n' + ex.toString());
	    } finally {
	      try {
	        stmt.close(); // Close statement which also closes open result sets
	      } catch(SQLException ex) {
	        
	      }
	    }
	}

	private Attributes toUpper(ResultSet lowerCaseAttrs) throws IdMUnitException {
		Attributes upperCaseAttrs = new BasicAttributes();
		try {
			lowerCaseAttrs.next();//only compare the first row for now
			ResultSetMetaData metaData = lowerCaseAttrs.getMetaData();
			for(int ctr=1;ctr<metaData.getColumnCount();++ctr) {
				String upperCaseAttrName = metaData.getColumnName(ctr).toUpperCase();
				System.out.println("Column Name: " + upperCaseAttrName);
				String attrVal = lowerCaseAttrs.getString(ctr);
				if(attrVal != null && attrVal.length() > 0) {
					BasicAttribute upperCaseAttr = new BasicAttribute(upperCaseAttrName);
					String upperCaseAttrVal = attrVal.toUpperCase();
					upperCaseAttr.add(upperCaseAttrVal);
					upperCaseAttrs.put(upperCaseAttr);
					System.out.println("Column Val: " + upperCaseAttrVal);
				}
			}
		} catch (SQLException e) {
			throw new IdMUnitException("Failed toUpper JDBC attrs: " + e.getMessage());
		}
		return upperCaseAttrs;
	}

	public void validateObject(Attributes assertedAttrs) throws IdMUnitException {
	    Statement stmt      = null;
		try {
			String query = (String)assertedAttrs.get(Constants.STR_SQL).get();
	        //System.out.println("...performing LDAP validation for: [" + dn + "]");
			ResultSet resultSet = null;
		    stmt = m_connection.createStatement();
		    resultSet = stmt.executeQuery(query);
			Attributes upperCaseAttrs = toUpper(resultSet); 
			NamingEnumeration ne = assertedAttrs.getAll();
			while(ne.hasMoreElements()) {
				Attribute attribute = (Attribute)ne.next();
				String attrName = attribute.getID().toUpperCase();
				if(!(attrName.equalsIgnoreCase(Constants.STR_SQL))) {
					String attrVal = (String)attribute.get();
					Attribute appAttr = upperCaseAttrs.get(attrName);
					//adAttr.contains()
					if(appAttr!=null) {
						System.out.println(".....validating attribute: [" + attrName + "] EXPECTED: [" + attrVal +"] ACTUAL: [" + appAttr.toString() + "]");
						attrVal = attrVal.toUpperCase();
						if(!appAttr.contains(attrVal)) {
							fail("Validation failed: Attribute [" + attrName + "] not equal.  Expected dest value: [" + attrVal+"] Actual dest value(s): [" + appAttr.toString() + "]");
							} else if(attrVal==null) {
							//Swallow this exception: we simply won't attempt to validate an attribute that was excluded from this sheet in the spreadsheet
							}
						System.out.println(Constants.STR_SUCCESS);
					} else {
						fail("Validation failed: Attribute [" + attrName + "] not equal.  Expected dest value: [" + attrVal+"] but the attribute value did not exist in the application.");
					}
				}
			}
		} catch (NamingException e) {
			fail("Validation failure: " + e.getMessage());
		} catch (SQLException se) {
			throw new IdMUnitException("Validation exception: " + se.getMessage());
		} finally {
	      try {
		        stmt.close(); 
		      } catch(SQLException ex) {
		    	  throw new IdMUnitException("Failed to close prepared statement: " + ex.getMessage());
		      }

		}
	}

	private void closeJDBCConnection() throws IdMUnitException {
		if(m_connection==null) {
			return;
		}
		try {
			m_connection.close();
		} catch (SQLException e) {
			throw new IdMUnitException("Failed to close jdbc connection: " + e.getMessage());
		}
		m_connection = null;
	}

	
}
