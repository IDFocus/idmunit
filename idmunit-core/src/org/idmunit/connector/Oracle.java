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
package org.idmunit.connector;

import javax.naming.*;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.BasicAttribute;

import oracle.jdbc.pool.OracleDataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ddsteps.dataset.DataRow;
import org.ddsteps.testcase.support.DDStepsExcelTestCase;
import org.idmunit.IdMUnitException;
import org.idmunit.IdMUnitFailureException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * Implements an IdMUnit connector for Oracle
 * @author Brent Kynaston, Software Engineer, TriVir LLC
 * @version %I%, %G%
 * @see org.idmunit.connector.Connection
 */
public class Oracle extends DDStepsExcelTestCase implements org.idmunit.connector.Connection {
    private final static String STR_SQL = "sql";

    private final static String STR_SUCCESS = "...SUCCESS";

    private static Log log = LogFactory.getLog(Oracle.class);
	private Connection m_connection;
	
	public Oracle() {}
	
	public Oracle(ConnectionConfigData creds) throws IdMUnitException {
		setupConnection(creds);
	}
	
	public void setupConnection(ConnectionConfigData creds) throws IdMUnitException {
		this.m_connection = getConnection(creds.getServerURL(), creds.getAdminCtx(), creds.getAdminPwd());
	}

	public java.sql.Connection getConnection(String serverUrl, String user, String password) throws IdMUnitException {
		java.sql.Connection connection = null;
		try {
		  OracleDataSource oracleDataSource = new OracleDataSource();
		  oracleDataSource.setUser(user);
		  oracleDataSource.setPassword(password);
		  oracleDataSource.setURL(serverUrl);
		  log.debug(" Connected to " + serverUrl + " Database as " + user);
		  connection = oracleDataSource.getConnection();
		  connection.setAutoCommit(true);

		    } catch(SQLException ex) {
		        log.info("Error in Connecting to the Database "+'\n'+ex.toString());
		        throw new IdMUnitException("Error in Connecting to the Database "+'\n'+ex.toString());
		    } 
		    return connection;
	}
	
	public void closeConnection() throws IdMUnitException {
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

	private Attributes toUpper(ResultSet lowerCaseAttrs) throws IdMUnitException {
		Attributes upperCaseAttrs = new BasicAttributes();
		try {  
			lowerCaseAttrs.next();//only compare the first row for now
			ResultSetMetaData metaData = lowerCaseAttrs.getMetaData();
			for(int ctr=1;ctr<metaData.getColumnCount()+1;++ctr) {
				String upperCaseAttrName = metaData.getColumnName(ctr).toUpperCase();
				log.info("Column Name: " + upperCaseAttrName);
				String attrVal = lowerCaseAttrs.getString(ctr);
				if(attrVal != null && attrVal.length() > 0) {
					BasicAttribute upperCaseAttr = new BasicAttribute(upperCaseAttrName);
					String upperCaseAttrVal = attrVal.toUpperCase();
					upperCaseAttrVal = upperCaseAttrVal.trim();
					upperCaseAttr.add(upperCaseAttrVal);
					upperCaseAttrs.put(upperCaseAttr);
					log.info("Column Val: " + upperCaseAttrVal);
				}
			}
		} catch (SQLException e) {
			//Translate the error message to english for the case where the returned row was empty (query yeilded no results)
			String errorMessage = (e.getMessage().indexOf("Cursor position not valid")!=-1)?"Error: no results found for the SQL query provided" : "Failed toUpper JDBC attrs: " + e.getMessage(); 
			throw new IdMUnitException(errorMessage);
		}
		return upperCaseAttrs;
	}

	public void validateObject(Attributes assertedAttrs) throws IdMUnitException {
	    Statement stmt      = null;
		ArrayList<String> errorsFound = new ArrayList<String>();
		try {
			Attribute sqlAttr = assertedAttrs.get(STR_SQL);
			if (sqlAttr == null) {
				// TODO: should be a test error, not a failure . .
				throw new IdMUnitException("Please include your validation query in a column titled [" + STR_SQL + "] to use validation in this connector.");
			}
			String sql = (String)sqlAttr.get();
	        //log.info("...performing LDAP validation for: [" + dn + "]");
			ResultSet resultSet = null;
		    stmt = m_connection.createStatement();
		    resultSet = stmt.executeQuery(sql);
			Attributes upperCaseAttrs = toUpper(resultSet);
			NamingEnumeration ne = assertedAttrs.getAll();
			while(ne.hasMoreElements()) {
				Attribute attribute = (Attribute)ne.next();
				String attrName = attribute.getID().toUpperCase();
				if(!(attrName.equalsIgnoreCase(STR_SQL))) {
					String attrVal = (String)attribute.get();
					Attribute appAttr = upperCaseAttrs.get(attrName);
					//adAttr.contains()
					if(appAttr!=null) {
						log.info(".....validating attribute: [" + attrName + "] EXPECTED: [" + attrVal +"] ACTUAL: [" + appAttr.toString() + "]");
						attrVal = attrVal.toUpperCase();
						if(!appAttr.contains(attrVal)) {
							//fail("Validation failed: Attribute [" + attrName + "] not equal.  Expected dest value: [" + attrVal+"] Actual dest value(s): [" + appAttr.toString() + "]");
							errorsFound.add("Validation failed: Attribute [" + attrName + "] not equal.  Expected dest value: [" + attrVal+"] Actual dest value(s): [" + appAttr.toString() + "]");
							continue;
							} else if(attrVal==null) {
							//Swallow this exception: we simply won't attempt to validate an attribute that was excluded from this sheet in the spreadsheet
							}
						log.info(STR_SUCCESS);
					} else {
						//fail("Validation failed: Attribute [" + attrName + "] not equal.  Expected dest value: [" + attrVal+"] but the attribute value did not exist in the application.");
						errorsFound.add("Validation failed: Attribute [" + attrName + "] not equal.  Expected dest value: [" + attrVal+"] but the attribute value did not exist in the application.");
					}
				}
			}
		} catch (NamingException e) {
			throw new IdMUnitFailureException("Validation failure: " + e.getMessage());
		} catch (SQLException se) {
			throw new IdMUnitException("Validation exception: " + se.getMessage());
		} finally {
	      try {
		        stmt.close(); 
		      } catch(SQLException ex) {
		    	  throw new IdMUnitException("Failed to close prepared statement: " + ex.getMessage());
		      }

		}
		if (errorsFound.size() > 0) {
			StringBuffer failMessages = new StringBuffer("");
			for(Iterator itErrors = errorsFound.iterator(); itErrors.hasNext();) {
				failMessages.append(itErrors.next());
				failMessages.append("\r\n");
			}
			throw new IdMUnitFailureException(failMessages.toString() + "\r\n[" + errorsFound.size() + "] errors found.");
		}
	}

	public void insertObject(Attributes assertedAttrs) throws IdMUnitException {
	    Statement stmt      = null;
		try {
			String sql = (String)assertedAttrs.get(STR_SQL).get();
		    log.info("...apply SQL statement: " + sql);
			stmt = m_connection.createStatement();
		    stmt.executeQuery(sql);
		    stmt.close();
		    log.info("..successful.");
		} catch (NamingException e) {
			fail("Validation failure: " + e.getMessage());
		} catch (SQLException se) {
			throw new IdMUnitException("SQL Execution exception: " + se.getMessage());
		} finally {
	      try {
	    	  if (stmt != null) {
				stmt.close(); 
	    	  }
		      } catch(SQLException ex) {
		    	  throw new IdMUnitException("Failed to close prepared statement: " + ex.getMessage());
		      }

		}
	}

	public void addObject(DataRow dataRow) throws IdMUnitException {
		// TODO Auto-generated method stub
		
	}

	public void deleteObject(Attributes assertedAttrs) throws IdMUnitException {
		// TODO Auto-generated method stub
		
	}

	public void modObject(Attributes assertedAttrs, int operationType) throws IdMUnitException {
		// TODO Auto-generated method stub
		
	}

	public void moveObject(Attributes assertedAttrs) throws IdMUnitException {
		// TODO Auto-generated method stub
		
	}

	public void renameObject(Attributes assertedAttrs) throws IdMUnitException {
		// TODO Auto-generated method stub
		
	}

	public void validatePassword(Attributes assertedAttrs) throws IdMUnitException {
		// TODO Auto-generated method stub
		
	}

	public Map<String, String> search(String filter, String base, String[] collisionAttrs) throws IdMUnitException {
		// TODO Auto-generated method stub
		return null;
	}
}