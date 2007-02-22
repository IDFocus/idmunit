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
package org.idmunit.connector;

import javax.naming.*;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.DirContext;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import net.sf.ldaptemplate.BadLdapGrammarException;
import net.sf.ldaptemplate.support.DistinguishedName;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ddsteps.dataset.DataRow;
import org.ddsteps.dataset.DataValue;
import org.ddsteps.testcase.support.DDStepsExcelTestCase;
import org.idmunit.Constants;
import org.idmunit.IdMUnitException;
import org.springframework.dao.DataIntegrityViolationException;


import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * Implements an IdMUnit connector for LDAP directories.  This connector has been tested with
 * the following directories: eDirectory, Active Directory, Oracle Internet Directory, and SunOne.
 * @author Brent Kynaston, Software Engineer, TriVir LLC
 * @version %I%, %G%
 * @see org.idmunit.connector.Connection
 */
public class LDAP extends DDStepsExcelTestCase implements org.idmunit.connector.Connection {
	private static Log log = LogFactory.getLog(LDAP.class);
	private DirContext m_context;
	private String m_serverURL;
	private ConnectionConfigData m_credentials;
	
	public LDAP() {}
	public void insertObject(Attributes assertedAttrs) throws IdMUnitException {
		throw new IdMUnitException(Constants.ERROR_NOT_IMPLEMENTED);
	}

	public LDAP(ConnectionConfigData creds) throws IdMUnitException {
		setupConnection(creds);
	}
	
	public void setupConnection(ConnectionConfigData creds) throws IdMUnitException {
		this.m_credentials = creds;
		this.m_serverURL = creds.getServerURL();	
		//Go secure if a certificate keystore was passed in
		if(creds.getKeystorePath()!=null && creds.getKeystorePath().length() > 0) {
			this.m_context = getLDAPSSLConnection(creds.getServerURL(), 
					creds.getAdminCtx(), creds.getAdminPwd(), creds.getKeystorePath());
		} else {
			this.m_context = getLDAPConnection(creds.getServerURL(), creds.getAdminCtx(), creds.getAdminPwd());
		}
	}

	public void closeConnection() throws IdMUnitException {
		try {
			if(m_context!=null) {
				this.m_context.close();
				this.m_context = null;
			}
		} catch (NamingException e) {
			throw new IdMUnitException("Failed to close ldap connection: " + e.getMessage());
		}
	}
	
	private Object transformArrayValue(Object o) {
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

	private byte[] getUnicodeBytes(String password) {
        //Replace the "unicdodePwd" attribute with a new value
		//Password must be both Unicode and a quoted string
		String newQuotedPassword = null;
		byte[] newUnicodePassword = null; 
		try {
			newQuotedPassword = "\"" + password + "\"";
			newUnicodePassword = newQuotedPassword.getBytes("UTF-16LE");
		} catch (UnsupportedEncodingException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		return newUnicodePassword;
	}
	
	public void addObject(DataRow dataRow) throws IdMUnitException {
		DistinguishedName dn = null;
		Attributes createAttrs = new BasicAttributes();
		// insert attributes from incoming map
		for (Iterator iter = dataRow.iterator(); iter.hasNext();) {
			DataValue dataValue = (DataValue) iter.next();

			String name = dataValue.getName();
			Object value = dataValue.getValue();
			if (value == null) {
				continue;
			}
			value = transformArrayValue(value);
			if (StringUtils.equals(name, Constants.STR_DN)) {
				if (value instanceof String) {
					dn = new DistinguishedName((String) value);
				} else {
					throw new BadLdapGrammarException("Invalid Distinguished Name: "
							+ "Must be a valid LDAP DN but was :" + value);
				}
			} else if (StringUtils.equals(name, Constants.STR_UNICODE_PASSWORD)) {
				//TODO: refactor this block into the connection class specific to AD SSL (com.idmunit.connection.ADLDAPSSLConnection)
				if (value instanceof String) {
					byte[] unicodePwdVal = getUnicodeBytes((String)value);
					createAttrs.put(name, unicodePwdVal);
				} else {
					throw new BadLdapGrammarException("Invalid unicodePwd specified: "
							+ "Must be type String!" + value);
				}
			} else {
				if (value instanceof Object[]) {
					Object[] values = (Object[])value;
					for(int i=0;i<values.length;++i) {
						createAttrs.put(name, values[i]);
					}
				} else {
					createAttrs.put(name, value);
				}
			}
		}

		if (dn == null) {
			throw new DataIntegrityViolationException(
					"A Distinguished Name must be supplied in column '" + Constants.STR_DN + "'");
		}
		log.info("Binding DN " + dn);
		try {
			DirContext tmpCtx = this.m_context.createSubcontext(dn, createAttrs);
			if(tmpCtx!=null) { 
				tmpCtx.close();//this is necessary in order to keep the parent connection ctx clean enough to be pooled/managed as week references inside of the parent DirContext will prevent proper pooling
			}
			log.info(">>>" + dn + " created");
		} catch (NamingException  e) {                   
			throw new IdMUnitException("Failed to create object: " + dn + " with error: " + e.getMessage());
		}            
	}

	public void modObject(Attributes assertedAttrs, int operationType) throws IdMUnitException {
		try {
			String dn = (String)assertedAttrs.get(Constants.STR_DN).get();
	        log.info("...performing LDAP modification for: [" + dn + "]");
	        NamingEnumeration attributesToProcess = assertedAttrs.getIDs();
	        Attributes modificationAttrs = new BasicAttributes();
	        while(attributesToProcess.hasMore()) {
	        	String attrName = (String)attributesToProcess.next();
	        	String attrVal = (String)assertedAttrs.get(attrName).get();
	        	if(attrName==null || attrVal==null || attrName.equalsIgnoreCase(Constants.STR_DN))
	        		continue;
        		//TODO: refactor and move into generic exceptional attr handler
	        	if(attrName.equals(Constants.STR_UNICODE_PASSWORD)) {
	        		byte[] unicodePwdVal = getUnicodeBytes(attrVal);
		        	modificationAttrs.put(attrName, unicodePwdVal);	        	
	        	} else {
	        		//TODO: refactor and move into generic exceptional attr handler
	        		if (attrName.equals(Constants.STR_DXML_ASSOC)) {
	    				//If the association contains a disabled or migrate value, simply continue.  Otherwise, attempt to resolve the full DXML association value
	        			if(!attrVal.endsWith(Constants.STR_DISABLED_ASSOC) && !attrVal.endsWith(Constants.STR_MIGRATE_ASSOC)) {
	        				Attribute assocAttrFromDirectory = (m_context.getAttributes(dn, new String[]{Constants.STR_DXML_ASSOC})).get(Constants.STR_DXML_ASSOC);
	        				attrVal = getDXMLAssocByDriverName(attrVal, assocAttrFromDirectory);
	        				if(attrVal==null || attrVal.length()<1) {
	        					fail("Failed to update attribute [" + Constants.STR_DXML_ASSOC + "] because a value matching the given driver DN was not found in the target object.");
	        				}
	        			}
	        		} 
	        		if(operationType==Constants.CLEAR_ATTRIBUTE && attrVal.equals(Constants.TOKEN_WILDCARD)) {
		        		attrVal = null;
		        		operationType = DirContext.REMOVE_ATTRIBUTE;
	        		} 
	        		modificationAttrs.put(attrName, attrVal);
	        	}
	        log.info("...preparing to update attr: [" + attrName + "] with value [" + attrVal + "]");
	        }
			if(modificationAttrs.size()>0) {
				m_context.modifyAttributes(dn, operationType, modificationAttrs);
			} else {
				log.info("...No attributes to update");
			}
	        
			log.info(Constants.STR_SUCCESS);
		} catch (NamingException e) {
			if(e.getMessage().contains("16")) {
				log.info("...already removed, operation unnecessary.");
			} else {
				throw new IdMUnitException("Modification failure: Error: " + e.getMessage());
			}
		} catch (NullPointerException npe) {
			throw new IdMUnitException("Null pointer exception: did you specify a DN for the operation?");
		}
	}

	public void deleteObject(Attributes assertedAttrs) throws IdMUnitException {
		try {
			String dn = getTargetDn(assertedAttrs);
			if(dn==null || dn.length()<1) {
				//A wild-card search failed to find an entry, or the DN field in the spreadsheet is blank
				log.info(Constants.WARNING_NO_DN);
				return; //Never fail when told to delete a DN that isn't there
			}
			DirContext tmp = (DirContext)m_context.lookup(dn);
		        log.info("...performing LDAP deletion for: [" + dn + "]");
			m_context.unbind(dn);
			log.info(Constants.STR_SUCCESS);
		} catch (NamingException e) {
			String errorMessage = e.getMessage().toUpperCase();
			if(errorMessage.indexOf("NO SUCH ENTRY")!= -1 || errorMessage.indexOf("NO_OBJECT")!=-1 || errorMessage.indexOf("OBJECT")!=-1) {
				return;
			} else {
			throw new IdMUnitException("Deletion failure: Invalid DN: " + e.getMessage());
			}
		}
	}

	public void modDn(Attributes assertedAttrs) throws IdMUnitException {
		try {

			String dn = (String)assertedAttrs.get(Constants.STR_DN).get();
			String newDn;
			try {
				newDn = (String)assertedAttrs.get(Constants.STR_NEW_DN).get();
			} catch (NullPointerException e) {
				try {
					newDn = (String)assertedAttrs.get("newDn").get(); //TODO: We need to avoid this case issue: fix this potential bug!! The actual contant STR_NEW_DN is newdn, so a newDn would fail to obtain the target - this might be a common mistake!
				} catch (NullPointerException e1) {
					throw new IdMUnitException(Constants.ERROR_MISSING_DN);
				}
			}
	        log.info("...performing LDAP move/rename for: [" + dn + "] to [" + newDn + "].");
	        m_context.rename(dn, newDn);
			log.info(Constants.STR_SUCCESS);
		} catch (NamingException e) {
			throw new IdMUnitException(Constants.ERROR_FAILED_MOVE + e.getMessage());
		}
	}

	public void renameObject(Attributes assertedAttrs) throws IdMUnitException {
		modDn(assertedAttrs);
	}
	
	public void moveObject(Attributes assertedAttrs) throws IdMUnitException {
		modDn(assertedAttrs);
	}

	private ConnectionConfigData setupCredentials(String userName, String password) throws IdMUnitException {
		ConnectionConfigData creds = new ConnectionConfigData();
		creds.setServerURL(m_credentials.getServerURL());
        creds.setKeystorePath(m_credentials.getKeystorePath());
        creds.setSubstitutions(m_credentials.getSubstitutions());
        creds.setAdminCtx(userName);
        creds.setClearAdminPwd(password);
        return creds;
	}
	
	public void validatePassword(Attributes assertedAttrs) throws IdMUnitException {
        ConnectionConfigData tempCredentials=null;
		try {
			String dn = (String)assertedAttrs.get(Constants.STR_DN).get();
			String passwordVal = (String)assertedAttrs.get(Constants.STR_USER_PASSWORD).get();
	        log.info("...performing LDAP password validation for: [" + dn + "]");
	        tempCredentials = setupCredentials(dn, passwordVal);
	        setupConnection(tempCredentials);
	        if(tempCredentials==null) {
				fail("Modification failure: Bad credentials: ");
	        }
	        
	        log.info(Constants.STR_SUCCESS);
		} catch (NamingException e) {
			fail("Password validation failure: Error: " + e.getMessage());
		} finally {
			if(m_context!=null){ 
	        	try {
					m_context.close();
				} catch (NamingException e) {
					log.info("...Failed to close validatePassword context.");
				}
		}
	}

	private Attributes attrPreProcessor(Attributes lowerCaseAttrs) throws NamingException {
		Attributes upperCaseAttrs = new BasicAttributes();
		NamingEnumeration attrEnum = lowerCaseAttrs.getAll();
		while(attrEnum.hasMore()) {
			Attribute attr = (Attribute)attrEnum.next();
			String upperCaseAttrName = attr.getID().toUpperCase();
			NamingEnumeration attrValEnum = attr.getAll();
			BasicAttribute upperCaseAttr = new BasicAttribute(upperCaseAttrName);
			while(attrValEnum.hasMore()) {
				Object attrVal = attrValEnum.next();
				if(attrVal!=null) {
					if(attrVal instanceof String) {
						String attrValStr = (String)attrVal;
						//Insert value-less DirXML-Associations if they are processed in order to be referrenced without the assoc value in the test step
						if(upperCaseAttrName.equalsIgnoreCase(Constants.STR_DXML_ASSOC) && attrValStr.contains(Constants.STR_PROCESSED_ASSOC)) {
							int endIndex = attrValStr.indexOf(Constants.STR_PROCESSED_ASSOC)+3;
							upperCaseAttr.add(attrValStr.toUpperCase().substring(0, endIndex));
						}
						upperCaseAttr.add(attrValStr.toUpperCase());
					} else {
						upperCaseAttr.add(attrVal);
					}
				} else {
					log.trace("...Detected null value in LDAP attribute: " + upperCaseAttrName);
				}
				
			}
			upperCaseAttrs.put(upperCaseAttr);
			attrValEnum.close();
		}
		attrEnum.close();
		return upperCaseAttrs;
	}
	
	private String getDXMLAssocByDriverName(String driverDn, Attribute attr) throws NamingException {
			NamingEnumeration attrValEnum = attr.getAll();
			while(attrValEnum.hasMore()) {
				Object attrVal = attrValEnum.next();
				if(attrVal!=null) {
					if(attrVal instanceof String) {
						String attrValStr = (String)attrVal;
						if (attrValStr.toUpperCase().startsWith(driverDn.toUpperCase())) {
							return attrValStr;
						}
					} 
				} else {
					log.trace("...Detected null value in for DXML association attribute, can't modify or remove a non-existing value.");
				}
			}
		return null;
	}

	protected String findUserbyLDAPFilter(String idVal, String base, String filter) throws IdMUnitException {
		return findUser(idVal, base, filter);
	}
	
	protected String findUserByID(String idVal, String base) throws IdMUnitException {
		return findUser(idVal, base, null);
	}
	
	protected String findUser(String idVal, String base, String filter) throws IdMUnitException {
		String resolvedDn = null;
        
		SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);

       	if(filter==null || filter.length()<1) {
            filter = "("+idVal+")";
       	}
       	log.info("---> Synthesized filter: " + filter + " from the base: " + base);
        	
        try {
        	//Find users with a matching ID
        	NamingEnumeration results = this.m_context.search(base, filter, ctls);
            SearchResult sr;
            int resultCtr = 0;
	        while (results.hasMore()) {
	        	++resultCtr;
	        	sr = (SearchResult) results.next();
	        	if(resultCtr==1) {
	        		resolvedDn = sr.getName() + "," + base;
	    			log.info("---> Target DN for validation: [" + resolvedDn + "]");
	        	} else { 
		        	log.info("---> Other objects found matching filter: [" + sr.getName() + "," + base + "]."); //TODO: refactor text into Constants
	        	}
	        }
        } catch (NamingException ne) {
			throw new IdMUnitException("Object Lookup for filter [" + filter +"], base ["+base+"] Failed: " + ne.getMessage());
        }
		return resolvedDn;
	}

	public Map search(String filter, String base, String[] collisionAttrs) throws IdMUnitException {
		Map foundIDs = new HashMap<String, String>();
        
		SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        ctls.setReturningAttributes(collisionAttrs);

        try {
        	//Find users matching the given filter
        	NamingEnumeration results = m_context.search(base, filter, ctls);
            SearchResult sr;
	        while (results.hasMore()) {
	        	sr = (SearchResult) results.next();
                StringBuffer userAttrs = new StringBuffer(128);
	        	Attributes attributes = sr.getAttributes();
                NamingEnumeration attrValsEnum = attributes.getAll();
                while (attrValsEnum.hasMore()) {
                    String tempAttr = attrValsEnum.next().toString();
                    if(tempAttr!=null && tempAttr.length()>1)
                    	userAttrs.append(tempAttr);
                    	if(attrValsEnum.hasMore()) 
                    		userAttrs.append(Constants.KEY_VALUE_SEPARATOR);
                }
                foundIDs.put(sr.getName() + "," + base, userAttrs.toString());
	        }
        } catch (NamingException ne) {
			throw new IdMUnitException("User Lookup Failed: " + ne.getMessage());
        }
		
		return foundIDs;
	}

	private String getTargetDn(Attributes assertedAttrs) throws NamingException, IdMUnitException {
		String dn = (String)assertedAttrs.get(Constants.STR_DN).get();
		if(dn==null || dn.length() < 1) return dn;
		
		//Detect LDAP filter in the DN
		int ldapFilterStartIdx = dn.indexOf("(");
		if(ldapFilterStartIdx!=-1) {
			//Search for the user by a standard LDAP filter
			dn = findUserbyLDAPFilter(null, dn.substring(dn.indexOf(",base=")+6), dn.substring(0,dn.indexOf(",")));
		} else {
			//Detect standard wildcard token * in the ID
			int baseIdx = dn.indexOf(",");
			String idVal = dn.substring(0, baseIdx);
			if(idVal.indexOf(Constants.TOKEN_WILDCARD)==-1) return dn;
			log.info("---> ID to search: " + idVal);
			dn = findUserByID(idVal, dn.substring(baseIdx+1));
		}
		return dn;
	}

	public void validateObject(Attributes assertedAttrs) throws IdMUnitException {
		NamingEnumeration ne = null;
		Attributes appAttrs = null;
		try {
			String dn = getTargetDn(assertedAttrs);
			if(dn==null || dn.length()<1) throw new IdMUnitException(Constants.ERROR_DN_FAILED + " Check the dn or LDAP filter specified in the spreadsheet.");
			
			DirContext tmp = (DirContext)m_context.lookup(dn);
			if(tmp!=null) {
				tmp.close(); //this is necessary in order to keep the parent connection ctx clean enough to be pooled/managed as week references inside of the parent DirContext will prevent proper pooling 
			}
			appAttrs = m_context.getAttributes(dn);
			//TODO: extract ability to flag attrs as operational into spreadsheet.
			if(assertedAttrs.get("nsaccountlock")!=null) {
				Attributes OperationalAttrs = m_context.getAttributes(dn, new String[]{"nsaccountlock"});
				appAttrs.put(OperationalAttrs.get("nsaccountlock"));
			} else if(assertedAttrs.get("DirXML-State")!=null) {
				Attributes OperationalAttrs = m_context.getAttributes(dn, new String[]{"DirXML-State"});
				appAttrs.put(OperationalAttrs.get("DirXML-State"));
			}

			Attributes upperCaseAttrs = attrPreProcessor(appAttrs); 
			ne = assertedAttrs.getAll();
			while(ne.hasMoreElements()) {
				Attribute attribute = (Attribute)ne.next();
				String attrName = attribute.getID().toUpperCase();
				if(!(attrName.equalsIgnoreCase(Constants.STR_DN))) {
					String attrVal = (String)attribute.get();
					Attribute appAttr = upperCaseAttrs.get(attrName);
					//adAttr.contains()
					if(appAttr!=null) {
						attrVal = attrVal.toUpperCase();
						//Support validation of whether or not an attribute exists (using a wildcard in the spreadsheet - '*')
						if(attrVal.equalsIgnoreCase(Constants.TOKEN_WILDCARD)) {
							if(upperCaseAttrs.get(attrName)!=null) {
								return; //Found an instance of that attr returned (equivalent to the IDM3 is-dest-attr-available command)
							} else {
								fail("Validation failed: Attribute [" + attrName + "] not found in the application.");
							}
						} else if(!appAttr.contains(attrVal)) {
							fail("Validation failed: Attribute [" + attrName + "] not equal.  Expected dest value: [" + attrVal+"] Actual dest value(s): [" + appAttr.toString() + "]");
							} else if(attrVal==null) {
							//Swallow this exception: we simply won't attempt to validate an attribute that was excluded from this sheet in the spreadsheet
							}
						log.info(Constants.STR_SUCCESS+": validating attribute: [" + attrName + "] EXPECTED: [" + attrVal +"] ACTUAL: [" + appAttr.toString() + "]");
					} else {
						fail("Validation failed: Attribute [" + attrName + "] not equal.  Expected dest value: [" + attrVal+"] but the attribute value did not exist in the application.");
					}
				}
			}
		} catch (NamingException e) {
			fail("Validation failure: " + e.getMessage());
		} finally {
			try {
				if(appAttrs!=null) {
					appAttrs = null;
				}
				if(ne!=null) {
					ne.close();
				}
			} catch (NamingException e) {
				fail("Validation failure - failed to close NamingEnumeration");
			}
		}
	}
	
	private InitialDirContext getLDAPConnection(String serverUrl, String adminUser, String pswd) throws IdMUnitException {
		Hashtable env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
		env.put("com.sun.jndi.ldap.connect.pool", "true");
		env.put("com.sun.jndi.ldap.connect.pool.protocol", "plain ssl");
		env.put("com.sun.jndi.ldap.connect.pool.timeout", "1000");
		env.put("com.sun.jndi.ldap.connect.pool.maxsize", "3");
		env.put("com.sun.jndi.ldap.connect.pool.prefsize", "1");
		env.put(Context.PROVIDER_URL, "ldap://" + serverUrl);
		env.put(Context.SECURITY_PRINCIPAL, adminUser);
		env.put(Context.SECURITY_CREDENTIALS, pswd);
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put("com.sun.jndi.ldap.connect.timeout", "5000");
		try {
			return new InitialDirContext(env);
		}catch (Exception e) {
			log.info("### Failed to obtain an LDAP server connection to: [" + serverUrl+"].");
			throw new IdMUnitException("Failed to obtain an LDAP Connection: " + e.getMessage());
		}
	}
	private  InitialDirContext getLDAPSSLConnection(String serverUrl, String adminUser, String pswd, String keyStorePath) throws IdMUnitException {
		System.setProperty("javax.net.ssl.trustStore", keyStorePath);
		Hashtable env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
		env.put("com.sun.jndi.ldap.connect.pool", "true");
		env.put("com.sun.jndi.ldap.connect.pool.protocol", "plain ssl");
		env.put("com.sun.jndi.ldap.connect.pool.timeout", "1000");
		env.put("com.sun.jndi.ldap.connect.pool.maxsize", "3");
		env.put("com.sun.jndi.ldap.connect.pool.prefsize", "1");
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.PROVIDER_URL, "ldap://" + serverUrl);
		env.put(Context.SECURITY_PRINCIPAL, adminUser);
		env.put(Context.SECURITY_CREDENTIALS, pswd);
		env.put(Context.SECURITY_PROTOCOL, "ssl");
		env.put("com.sun.jndi.ldap.connect.timeout", "5000");

		try {
			return new InitialDirContext(env);
		}catch (Exception e) {
			log.info("### Failed to obtain an SSL LDAP server connection to: [" + serverUrl+"].");
			e.printStackTrace();
			throw new IdMUnitException("Failed to obtain an SSL LDAP Connection: " + e.getMessage());
		}
	}
	
}