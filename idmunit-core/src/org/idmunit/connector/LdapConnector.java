/* 
 * IdMUnit - Automated Testing Framework for Identity Management Solutions
 * Copyright (c) 2005-2012 TriVir, LLC
 * Copyright (c) 2017 IDFocus B.V.
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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.idmunit.Failures;
import org.idmunit.IdMUnitException;
import org.idmunit.IdMUnitFailureException;
import org.idmunit.util.LdapConnectionHelper;

import com.novell.ldap.util.Base64;

public class LdapConnector extends BasicConnector {
    
    private final static String STR_DN = "dn";
    private final static String STR_NEW_DN = "newdn";
    private final static String STR_USER_PASSWORD = "userPassword";
    private final static String STR_BASE_DN_DELIMITER = ",base=";
    protected final static String STR_SUCCESS = "...SUCCESS";

    private final static String STR_UNICODE_PASSWORD = "unicodePwd";
    protected final static String STR_DXML_ASSOC = "DirXML-Associations";
    
    private static Log logger = LogFactory.getLog(LdapConnector.class);

    private TreeSet<String> operationalAttributes = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    private TreeSet<String> dnAttributes = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

	private boolean insensitive = false;
    protected String server;

    private DirContext m_context;
    protected Map<String,String> config; //Storing config for use in opValidatePassword.

    public LdapConnector() {
        operationalAttributes.add("nsaccountlock");
        operationalAttributes.add("DirXML-State");
        operationalAttributes.add("pwdaccountlockedtime");
        operationalAttributes.add("members");
        // FIXME Should come from config in setup() 
        dnAttributes.add("manager");
        dnAttributes.add("directReports");
        dnAttributes.add("secretary");
    }

    public void opAddAttr(Map<String, Collection<String>> dataRow) throws IdMUnitException {
        modifyObject(dataRow, DirContext.ADD_ATTRIBUTE);
    }

    public void opAddObject(Map<String, Collection<String>> dataRow) throws IdMUnitException {
        String dn = null; 
        Attributes createAttrs = new BasicAttributes();
        // insert attributes from incoming map
        for (String name : dataRow.keySet()) {
            Collection<String> dataValue = dataRow.get(name);

            if (dataValue.isEmpty()) {
                continue;
            }

            if (name.equalsIgnoreCase(STR_DN)){
                dn = dataValue.iterator().next();
				logger.info("...performing LDAP creation for: [" + dn + "]");
            } else if (name.equalsIgnoreCase(STR_UNICODE_PASSWORD)) {
                byte[] unicodePwdVal = getUnicodeBytes(dataValue.iterator().next());
                createAttrs.put(name, unicodePwdVal);
            } else if (dnAttributes.contains(name)) {
                createAttrs.put(name, getTargetDn( dataRow, name ));
            } else {
                BasicAttribute multiValuedAttr = new BasicAttribute(name);
                for (Iterator<String> i=dataValue.iterator(); i.hasNext(); ) {
                    multiValuedAttr.add(i.next());
                }
                createAttrs.put(multiValuedAttr);
            }
        }

        if (dn == null) {
            throw new IdMUnitException("A Distinguished Name must be supplied in column '" + STR_DN + "'");
        }

		// logger.debug("Binding DN " + dn);
        try {
            DirContext tmpCtx = this.m_context.createSubcontext(dn, createAttrs);
            if(tmpCtx!=null) { 
                tmpCtx.close();//this is necessary in order to keep the parent connection ctx clean enough to be pooled/managed as week references inside of the parent DirContext will prevent proper pooling
            }
		//	logger.debug(">>>" + dn + " created");
			logger.info(STR_SUCCESS);
        } catch (NamingException  e) {                   
            throw new IdMUnitException("Failed to create object: " + dn + " with error: " + e.getMessage(), e);
        }            
    }

    public void opClearAttr(Map<String, Collection<String>> data) throws IdMUnitException {
        String dn = getTargetDn(data);
        logger.debug("...performing LDAP modification for: [" + dn + "]");

        List<ModificationItem> mods = new ArrayList<>();
        for (String attrName : data.keySet() ) {
            if (attrName.equalsIgnoreCase(STR_DN)) {
                continue;
            }

            for (String attrVal : data.get(attrName)) {
                if (!"*".equals(attrVal)) {
                    throw new IdMUnitException("You must specify '*' as the attribute value for the clearAttr operation.");
                }
            }
            mods.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute(attrName)));
        }

        if (!mods.isEmpty()) {
            try {
                m_context.modifyAttributes(dn, (ModificationItem[])mods.toArray(new ModificationItem[mods.size()]));
            } catch (NameNotFoundException e) {
            	logger.warn("...WARNING: object doesn't exist, continuing.");
            	// TODO: send warning here?
            } catch (NamingException e) {
                if(e.getMessage().contains("16")) {
                    logger.warn("...already removed, operation unnecessary.");
                } else {
                    throw new IdMUnitException("Modification failure: Error: " + e.getMessage(), e);
                }
            }
        } else {
            logger.debug("...No attributes to update");
        }

        logger.info(STR_SUCCESS);
    }

    public void opDeleteObject(Map<String, Collection<String>> data) throws IdMUnitException {
        String dn;
        try {
            dn = getTargetDn(data);
        } catch (IdMUnitException e) {
            //A wild-card search failed to find an entry, or the DN field in the spreadsheet is blank
            logger.warn("---> Wild-card deletion found no DNs matching the specified filter, or the DN in the spreadsheet is blank.");
            return; //Never fail when told to delete a DN that isn't there
        }

        try {
            m_context.lookup(dn);
            logger.info("...performing LDAP deletion for: [" + dn + "]");
            m_context.unbind(dn);
            logger.info(STR_SUCCESS);
        } catch (NamingException e) {
            String errorMessage = e.getMessage().toUpperCase();
            if(errorMessage.indexOf("NO SUCH ENTRY")!= -1 || errorMessage.indexOf("NO_OBJECT")!=-1 || errorMessage.indexOf("OBJECT")!=-1) {
                return;
            } else {
                throw new IdMUnitException("Deletion failure: Invalid DN: " + e.getMessage());
            }
        }
    }

    public void opMoveObject(Map<String, Collection<String>> data) throws IdMUnitException {
        String dn = ConnectorUtil.getSingleValue(data, STR_DN);
        if (dn == null) {
            throw new IdMUnitException("A Distinguished Name must be supplied in column '" + STR_DN + "'");
        }

        String newDn = ConnectorUtil.getSingleValue(data, STR_NEW_DN);
        if (newDn == null) {
            throw new IdMUnitException("A Distinguished Name must be supplied in column '" + STR_NEW_DN + "'");
        }

        logger.info("...performing LDAP move/rename for: [" + dn + "] to [" + newDn + "].");
        try {
            m_context.rename(dn, newDn);
            logger.info(STR_SUCCESS);
        } catch (NamingException e) {
            throw new IdMUnitException("Move/Rename failure: Error: " + e.getMessage(), e);
        }
    }

    public void opRemoveAttr(Map<String, Collection<String>> data) throws IdMUnitException {
        String dn = getTargetDn(data);
		logger.info("...performing LDAP remove attribute for: [" + dn + "]");

        TreeMap<String, Collection<String>> curAttrs;
		try {
			curAttrs = getAttributes(dn);
		} catch (IdMUnitException e) {
			logger.info(e.toString());
			return;
		}

        List<ModificationItem> mods = new ArrayList<>();
        for (String attrName : data.keySet()) {
            if (attrName.equalsIgnoreCase(STR_DN)) {
                continue;
            } else if(attrName.equals(STR_UNICODE_PASSWORD)) {
                byte[] unicodePwdVal = getUnicodeBytes(ConnectorUtil.getSingleValue(data, STR_UNICODE_PASSWORD));
                mods.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute(attrName, unicodePwdVal)));
            } else if (attrName.equals(STR_DXML_ASSOC)) {
                Collection<String> values = data.get(attrName);
                Collection<String> curAssociations = curAttrs.get(STR_DXML_ASSOC);
                for (String attrVal : values) {
                    String[] association = attrVal.split("#", 3);

                    String oldAttrVal = getDXMLAssocByDriverName(association[0], curAssociations);
                    if(oldAttrVal == null) {
                        throw new IdMUnitException("Failed to update attribute [" + STR_DXML_ASSOC + "] because a value matching the given driver DN was not found in the target object.");
                    }
                    assert(oldAttrVal.length() > 0);

                    mods.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute(attrName, oldAttrVal)));
                }
            } else {
                Collection<String> values;
                // Resolve dn attributes if needed
                if (dnAttributes.contains(attrName))
                { 
                    values = Arrays.asList( getTargetDn(data, attrName) );
                } else {
                    values = data.get(attrName);
                }
                Attribute modValues = new BasicAttribute(attrName);

                Collection<String> curValues = curAttrs.get(attrName);
                //Only remove values if they are actually present on the object
                if (curValues != null) {
	                for (String curValue : curValues) {
	                    for (String value : values) {
	                    	//TODO: Add configuration option at connector definition level to enable/disable case sensitivity for rex-ex comparisons
	                        if (curValue.matches(value)) {
	                            modValues.add(curValue);
	                        }
	                    }
	                }
                }
                //Only apply the modification if there are explicit values to remove, otherwise all values will be removed!
                if(modValues.size()>0) {
                	mods.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE, modValues)); 
                	logger.debug("...preparing to remove [" + modValues + "] from '" + attrName + "'.");
                } else {
                	logger.warn("...WARNING: specified value doesn't exist, continuing.");
                }
            }
        }

        if (!mods.isEmpty()) {
            try {
                m_context.modifyAttributes(dn, (ModificationItem[])mods.toArray(new ModificationItem[mods.size()]));
            } catch (NamingException e) {
                if(e.getMessage().contains("16")) {
                    logger.warn("...already removed, operation unnecessary.");
                } else {
                    throw new IdMUnitException("Modification failure: Error: " + e.getMessage(), e);
                }
            }
        } else {
            logger.debug("...No attributes to update");
        }

        logger.info(STR_SUCCESS);
    }

    public void opRenameObject(Map<String, Collection<String>> assertedAttrs) throws IdMUnitException {
        opMoveObject(assertedAttrs);
    }

    public void opReplaceAttr(Map<String, Collection<String>> dataRow) throws IdMUnitException {
        modifyObject(dataRow, DirContext.REPLACE_ATTRIBUTE);
    }

    public void opAttrDoesNotExist(Map<String, Collection<String>> expectedAttrs) throws IdMUnitException {
    	doValidate(expectedAttrs, true);
    }

    public void opValidateObject(Map<String, Collection<String>> expectedAttrs) throws IdMUnitException {
    	doValidate(expectedAttrs, false);
		String passwordVal = ConnectorUtil.getSingleValue(expectedAttrs, STR_USER_PASSWORD);

		if (passwordVal != null) {
			opValidatePassword(expectedAttrs);
		}
	}

    public void opValidateObjectDoesNotExist(Map<String, Collection<String>> expectedAttrs) throws IdMUnitException {
    	String entryIdentifier = ConnectorUtil.getSingleValue(expectedAttrs, STR_DN);
    	
    	String dn = null;

    	if (entryIdentifier.startsWith("(")) {
    		// we have an ldap filter - see if we have any objects:
    		String[] filterAndBase = entryIdentifier.split(STR_BASE_DN_DELIMITER);
    		if (filterAndBase.length != 2 ) {
    			throw new IdMUnitException("Error: value provied for DN appears to be an invalid ldap filter - it must be in the format of [LDAP FILTER]" + STR_BASE_DN_DELIMITER + "[LDAP BASE DN TO SEARCH]");
    		}
    		String filter = filterAndBase[0];
    		String base = filterAndBase[1];
    		if ( (dn = findObject(base, filter)) != null) {
    			throw new IdMUnitFailureException("An object was found when it was not expected. Object found: [" + dn + "]");
    		}    		
    	} else {
    		// we appear to have a dn, see if it exists: 
    		dn = entryIdentifier;
    		try {
				if (m_context.lookup(dn) != null) {
					throw new IdMUnitFailureException("An object was found when it was not expected. Object found: ["+ dn + "]");
				}
			}
    		catch (NameNotFoundException e) {
    			return;
    		}
    		catch (NamingException e) {
				throw new IdMUnitException("Failed while looking for dn: [" + dn + "]", e);
			}
    	}
    	
    }    
    
    public void doValidate(Map<String, Collection<String>> expectedAttrs, boolean bIsAttrDoesNotExistTest) throws IdMUnitException {
        String dn = getTargetDn(expectedAttrs);

		logger.info("...performing LDAP attribute validation for: [" + dn + "]");
        Map<String, Collection<String>> appAttrs = getAttributes(dn);

        Failures failures = new Failures();
        for (String attrName : expectedAttrs.keySet()) {
            if (attrName.equalsIgnoreCase(STR_DN)) {
                continue;
            }
			if(attrName.equalsIgnoreCase(STR_USER_PASSWORD)) {
				continue;
			}
            // Get expected values, resolve dn attributes if needed
            Collection<String> expectedValues;
            if (dnAttributes.contains(attrName))
            {
                expectedValues = Arrays.asList( getTargetDn( expectedAttrs, attrName) );
            }
            else
            {
                expectedValues = expectedAttrs.get(attrName);               
            }
            Collection<String> actualValues;
            if (operationalAttributes.contains(attrName)) {
                // Operational attributes are not returned from the server when
                // all attributes are requested so we need to read them
                // explicitly.
                actualValues = getAttributes(dn, new String[]{attrName}).get(attrName);
            } else {
                actualValues = appAttrs.get(attrName);
            }

            if (actualValues == null || actualValues.isEmpty()) {
            	if (bIsAttrDoesNotExistTest) {
            		return;
            	}
                failures.add("Validation failed: Attribute [" + attrName + "] not equal.  Expected value: [" + expectedValues + "] but the attribute value did not exist in the application.");
            } else {
                compareAttributeValues(attrName, expectedValues, actualValues, failures);
            }
        }

        if (failures.hasFailures()) {
            throw new IdMUnitFailureException(failures.toString());
        }
    }

    public void opValidatePassword(Map<String, Collection<String>> expectedAttrs) throws IdMUnitException {
        InitialDirContext tempConn = null;
        try {
            String dn = getTargetDn(expectedAttrs);
            String passwordVal = ConnectorUtil.getSingleValue(expectedAttrs, STR_USER_PASSWORD);
			if (passwordVal == null || passwordVal.length() == 0) {
				throw new IdMUnitFailureException("Missing " + STR_USER_PASSWORD + " attribute");
			}
            logger.info("...performing LDAP password validation for: [" + dn + "]");
            Map<String,String> config = new HashMap<>(this.config);
            config.put(CONFIG_USER, dn);
            config.put(CONFIG_PASSWORD, passwordVal);
            tempConn = LdapConnectionHelper.createLdapConnection(config);
            logger.info(STR_SUCCESS);
        } catch (IdMUnitException e) {
            throw new IdMUnitFailureException("Password validation failure: Error: " + e.getMessage());
        } finally {
            if (tempConn != null) { 
                try {
                    tempConn.close();
                } catch (NamingException e) {
                    logger.warn("...Failed to close validatePassword context.");
                }
            }
        }
    }
    
	public void opChangeUserPassword(
			Map<String, Collection<String>> expectedAttrs)
			throws IdMUnitException {

		try {
			String dn = getTargetDn(expectedAttrs);

			String oldPassword = ConnectorUtil.getSingleValue(expectedAttrs, STR_USER_PASSWORD);
			if (oldPassword == null || oldPassword.length() == 0) {
				throw new IdMUnitException("Missing " + STR_USER_PASSWORD + " attribute");
			}
			String newPassword = ConnectorUtil.getSingleValue(expectedAttrs, STR_UNICODE_PASSWORD);
			if (newPassword == null || newPassword.length() == 0) {
				throw new IdMUnitException("Missing " + STR_UNICODE_PASSWORD + " attribute");
			}
			// change password is a single ldap modify operation
			// that deletes the old password and adds the new password
			ModificationItem[] mods = new ModificationItem[2];

			// Firstly delete the "unicdodePwd" attribute, using the old
			// password
			// Then add the new password,Passwords must be both Unicode and a
			// quoted string
			String oldQuotedPassword = "\"" + oldPassword + "\"";
			byte[] oldUnicodePassword = oldQuotedPassword.getBytes("UTF-16LE");
			String newQuotedPassword = "\"" + newPassword + "\"";
			byte[] newUnicodePassword = newQuotedPassword.getBytes("UTF-16LE");

			mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
					new BasicAttribute(STR_UNICODE_PASSWORD, oldUnicodePassword));
			mods[1] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute(STR_UNICODE_PASSWORD, newUnicodePassword));

			// Perform the password change
			m_context.modifyAttributes(dn, mods);

		} catch (IdMUnitException e) {
			throw new IdMUnitFailureException("Password validation failure: Error: " + e.getMessage());
		} catch (NamingException e) {
			throw new IdMUnitException("Failed changing password: "	+ e.getMessage());
		} catch (UnsupportedEncodingException e) {
			throw new IdMUnitException("Failed encoding password: "	+ e.getMessage());
		}
	}

    public void setup(Map<String, String> config) throws IdMUnitException {
        server = config.get(CONFIG_SERVER);

        // create a defensive copy of the config.
        this.config = Collections.unmodifiableMap(config); // Config is stored for use in opValidatePassword.
        this.m_context = LdapConnectionHelper.createLdapConnection(new HashMap<String,String>(config));
    }

    public void tearDown() throws IdMUnitException {
    	LdapConnectionHelper.destroyLdapConnection(m_context);
    }

    private String findObject(String base, String filter) throws IdMUnitException {
        String resolvedDn = null;

        SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        try {
            //Find users with a matching ID
            NamingEnumeration<SearchResult> results = this.m_context.search(base, filter, ctls);
            SearchResult sr;
            int resultCtr = 0;
            while (results.hasMoreElements()) {
                ++resultCtr;
                sr = results.next();
                if(resultCtr == 1) {
                	if(sr.getName()!= null && sr.getName().length() > 0) {
                		resolvedDn = sr.getName() + "," + base;
	        		} else { 
	        			resolvedDn = base;
	        		}
            		logger.debug("---> Target DN for validation: [" + resolvedDn + "]");
                } else { 
                    logger.debug("---> Other objects found matching filter: [" + sr.getName() + "," + base + "].");
                }
            }
        } catch (NamingException ne) {
            throw new IdMUnitException("Object Lookup for filter [" + filter +"], base ["+base+"] Failed: " + ne.getMessage(), ne);
        }
        return resolvedDn;
    }

    private TreeMap<String, Collection<String>> getAttributes(String dn) throws IdMUnitException {    	
        try {
            DirContext tmp = (DirContext)m_context.lookup(dn);
            if (tmp != null) {
                tmp.close(); //this is necessary in order to keep the parent connection ctx clean enough to be pooled/managed as week references inside of the parent DirContext will prevent proper pooling 
            }
        } catch (NameNotFoundException e) {
        	throw new IdMUnitException("Could not find object: [" + dn + "] to retrieve attributes.", e);
        } catch (NamingException e) {
            throw new IdMUnitException("Error resolving '" + dn + "'.", e);
        }

        try {
            return attributesToMap(m_context.getAttributes(dn));
        } catch (NamingException e) {
            throw new IdMUnitException("Error reading attributes for '" + dn + "'.", e);
        }
    }

    private TreeMap<String, Collection<String>> getAttributes(String dn, String[] attrs) throws IdMUnitException {
        try {
            Attributes operationalAttrs = m_context.getAttributes(dn, attrs);
            return attributesToMap(operationalAttrs);
        } catch (NamingException e) {
            throw new IdMUnitException("Error reading attributes for '" + dn + "'.", e);
        }
    }

    protected String getTargetDn(Map<String, Collection<String>> data) throws IdMUnitException {
        return getTargetDn(data, STR_DN);
    }

    protected String getTargetDn(Map<String, Collection<String>> data, String attrname) throws IdMUnitException {
        String dn = ConnectorUtil.getSingleValue(data, attrname);

        if (dn == null) {
            throw new IdMUnitException("A Distinguished Name must be supplied in column '" + STR_DN + "'");
        }

        if (!dn.trim().equalsIgnoreCase(dn)) {
        	throw new IdMUnitException("WARNING: your DN specified: [" + dn + "] is either prefixed or postfixed with whitespace!  Please correct, then retest.");
        }

        //Detect LDAP filter in the DN
        if(dn.startsWith("(")) {
            //Search for the user by a standard LDAP filter
            // The format for this field is "<filter>,base=<container dn>"
            int startOfBase = dn.indexOf(STR_BASE_DN_DELIMITER);
            if (startOfBase == -1) {
                throw new IdMUnitException("Check the dn or LDAP filter specified in the spreadsheet. Should be listed in the form (LDAPFilter),base=LDAPSearchBase.  Example: (&(objectClass=inetOrgPerson)(cn=testuser1)),base=o=users.");
            }

            String filter = dn.substring(0, startOfBase);
            String base = dn.substring(startOfBase + STR_BASE_DN_DELIMITER.length());

            dn = findObject(base, filter);
        } else {
            //Detect standard wildcard token * in the ID
            String[] nameComponents = dn.split("(?<!\\\\),");
            String idVal = nameComponents[0];
            if (idVal.indexOf('*')==-1) return dn;
            // cn=TIDMTST*1,ou=users,o=myorg
            logger.debug("---> ID to search: " + idVal);

            String base = dn.substring(dn.indexOf(nameComponents[1]));
            String filter = "("+idVal+")";
            logger.debug("---> Synthesized filter: " + filter + " from the base: " + base);

            dn = findObject(base, filter);
        }

        if (dn==null || dn.length()<1) {
            throw new IdMUnitException("Failed to resolve target DN: Check the dn or LDAP filter specified in the spreadsheet to ensure it returns results.  Recommended: test the filter in an LDAP browser first.");
        }

        dn = dn.replaceAll("/", "\\\\/");
        dn = dn.replaceAll("\"", "");

        return dn;
    }

    private void modifyObject(Map<String, Collection<String>> dataRow, int operationType) throws IdMUnitException {
        String dn = getTargetDn(dataRow);
        logger.debug("...performing LDAP modification for: [" + dn + "]");

        List<ModificationItem> mods = new ArrayList<>();
        for (String attrName : dataRow.keySet()) {
            if (attrName.equalsIgnoreCase(STR_DN)) {
                continue;
            } else if(attrName.equals(STR_UNICODE_PASSWORD)) {
                byte[] unicodePwdVal = getUnicodeBytes(ConnectorUtil.getSingleValue(dataRow, STR_UNICODE_PASSWORD));
                mods.add(new ModificationItem(operationType, new BasicAttribute(attrName, unicodePwdVal)));
            } else if (attrName.equals(STR_DXML_ASSOC) && operationType == DirContext.REPLACE_ATTRIBUTE) {
                Collection<String> curAssociations = getAttributes(dn, new String[]{STR_DXML_ASSOC}).get(STR_DXML_ASSOC);
                for (String attrVal : dataRow.get(attrName)) {
                    String[] association = attrVal.split("#", 3);

                    if (curAssociations != null) {
                        String oldAttrVal = getDXMLAssocByDriverName(association[0], curAssociations);
                        if(oldAttrVal != null) {
                            assert(oldAttrVal.length() > 0);

                            // If the new association doesn't have a the path
                            // component (the third component), use the path
                            // component from the current association.
                            if (association.length == 2) {
                                String[] oldAssociation = oldAttrVal.split("#", 3);
                                if (oldAssociation.length == 3) {
                                    attrVal = association[0] + "#" + association[1] + "#" + oldAssociation[2];
                                }
                            }

                            mods.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute(attrName, oldAttrVal)));
                        }
                    }
                    mods.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute(attrName, attrVal)));
                    continue;
                }
            } else {
                Collection<String> values;
                // Resolve dn attributes if needed
                if (dnAttributes.contains(attrName))
                {
                    values = Arrays.asList( getTargetDn(dataRow, attrName) );
                } else {
                    values = dataRow.get(attrName);
                }
                Attribute modValues = new BasicAttribute(attrName);
                for (Iterator<String> j=values.iterator(); j.hasNext(); ) {
                    modValues.add(j.next());
                }
                mods.add(new ModificationItem(operationType, modValues));
                logger.debug("...preparing to update attr: [" + attrName + "] with value [" + values + "]");
            }
        }

        if (!mods.isEmpty()) {
            try {
                m_context.modifyAttributes(dn, (ModificationItem[])mods.toArray(new ModificationItem[mods.size()]));
            } catch (NamingException e) {
                if(e.getMessage().contains("16")) {
                    logger.warn("...already removed, operation unnecessary.");
                } else {
                    throw new IdMUnitException("Modification failure: Error: " + e.getMessage(), e);
                }
            }
        } else {
            logger.debug("...No attributes to update");
        }

        logger.info(STR_SUCCESS);
    }

    private static TreeMap<String, Collection<String>> attributesToMap(Attributes attributes) throws NamingException {
        TreeMap<String, Collection<String>> attrs = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        NamingEnumeration<? extends Attribute> i = null;
        try {
            for (i=attributes.getAll(); i.hasMore(); ) {
                Attribute attr = i.next();
                String attrName = attr.getID();
                List<String> attrValues = new LinkedList<>();
                for (NamingEnumeration<?> j=attr.getAll(); j.hasMore(); ) {
                    Object value = j.next();
                    if (attrName.equals("msExchUMPinChecksum")) {
                    	attrValues.add(Base64.encode(((String)value).getBytes()));                    	
                    } else if (value instanceof String) {
                        attrValues.add((String)value);
                    } else {
                        logger.info("Not adding value for '" + attrName + "' because it is not a String.");
                    }
                }
                attrs.put(attrName, attrValues);
            }
        } catch (NamingException e) {
            try {
                if (i != null) {
                    i.close();
                }
            } catch (NamingException e1) {
                logger.info("An error occurred closing the NamingEnumeration. '" + e.getMessage() + "'");
            }
            throw e;
        }

        return attrs;
    }

    protected void compareAttributeValues(String attrName, Collection<String> expected, Collection<String> actual, Failures failures) throws IdMUnitException {
        Collection<String> unmatched = new LinkedList<>(actual);
        outer:
        for (String expectedValue : expected) {
        	Pattern p = Pattern.compile(expectedValue, insensitive ? Pattern.CASE_INSENSITIVE : Pattern.DOTALL);
        	for (Iterator<String> i=unmatched.iterator(); i.hasNext(); ) {
        		String actualValue = i.next();

                if (p.matcher(actualValue).matches()) {
                    logger.info(STR_SUCCESS + ": validating attribute: [" + attrName + "] EXPECTED: [" + expectedValue +"] ACTUAL: [" + actualValue + "]");
                    i.remove();
                    continue outer;
                }
        	}

            failures.add(attrName, expected, actual);
        	return;
        }
    }

    private static String getDXMLAssocByDriverName(String driverDn, Collection<String> attr) {
        for (Iterator<String> i=attr.iterator(); i.hasNext(); ) {
            Object attrVal = i.next();
            if (attrVal != null) {
                if(attrVal instanceof String) {
                    String attrValStr = (String)attrVal;
                    if (attrValStr.toUpperCase().startsWith(driverDn.toUpperCase())) {
                        return attrValStr;
                    }
                } 
            } else {
                logger.info("...Detected null value in for DXML association attribute, can't modify or remove a non-existing value.");
            }
        }
        return null;
    }

    private static byte[] getUnicodeBytes(String password) {
        //Replace the "unicdodePwd" attribute with a new value
        //Password must be both Unicode and a quoted string
        String newQuotedPassword = null;
        byte[] newUnicodePassword = null; 
        try {
            newQuotedPassword = "\"" + password + "\"";
            newUnicodePassword = newQuotedPassword.getBytes("UTF-16LE");
        } catch (UnsupportedEncodingException e2) {
            throw new Error("UTF-16LE encoding not supported.");
        }
        return newUnicodePassword;
    }
}
