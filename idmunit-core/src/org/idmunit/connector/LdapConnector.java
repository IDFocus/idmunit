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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.naming.Context;
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

import org.idmunit.Failures;
import org.idmunit.IdMUnitException;
import org.idmunit.IdMUnitFailureException;

public class LdapConnector extends BasicConnector {
    private final static String STR_DN = "dn";
    private final static String STR_NEW_DN = "newdn";
    private final static String STR_USER_PASSWORD = "userPassword";
    private final static String STR_SUCCESS = "...SUCCESS";
    
    private final static String STR_UNICODE_PASSWORD = "unicodePwd";
    private final static String STR_DXML_ASSOC = "DirXML-Associations";

    private static Logger logger = Logger.getLogger(LdapConnector.class.getName());

    private TreeSet<String> operationalAttributes = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);

    private Map config;

    private DirContext m_context;

    public LdapConnector() {
        operationalAttributes.add("nsaccountlock");
        operationalAttributes.add("DirXML-State");
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

            if (dataValue.size() == 0) {
                continue;
            }

            if (name.equalsIgnoreCase(STR_DN)){
                dn = dataValue.iterator().next();
            } else if (name.equalsIgnoreCase(STR_UNICODE_PASSWORD)) {
                byte[] unicodePwdVal = getUnicodeBytes(dataValue.iterator().next());
                createAttrs.put(name, unicodePwdVal);
            } else {
                BasicAttribute multiValuedAttr = new BasicAttribute(name);
                for (Iterator i=dataValue.iterator(); i.hasNext(); ) {
                    multiValuedAttr.add(i.next());
                }
                createAttrs.put(multiValuedAttr);
            }
        }

        if (dn == null) {
            throw new IdMUnitException("A Distinguished Name must be supplied in column '" + STR_DN + "'");
        }

        logger.finer("Binding DN " + dn);
        try {
            DirContext tmpCtx = this.m_context.createSubcontext(dn, createAttrs);
            if(tmpCtx!=null) { 
                tmpCtx.close();//this is necessary in order to keep the parent connection ctx clean enough to be pooled/managed as week references inside of the parent DirContext will prevent proper pooling
            }
            logger.finer(">>>" + dn + " created");
        } catch (NamingException  e) {                   
            throw new IdMUnitException("Failed to create object: " + dn + " with error: " + e.getMessage(), e);
        }            
    }

    public void opClearAttr(Map<String, Collection<String>> data) throws IdMUnitException {
        String dn = getTargetDn(data);
        logger.finer("...performing LDAP modification for: [" + dn + "]");

        List<ModificationItem> mods = new ArrayList<ModificationItem>();
        for (String attrName : data.keySet() ) {
            if (attrName.equalsIgnoreCase(STR_DN)) {
                continue;
            }

            for (String attrVal : data.get(attrName)) {
                if (attrVal.equals("*") == false) {
                    throw new IdMUnitException("You must specify '*' as the attribute value for the clearAttr operation.");
                }
            }
            mods.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute(attrName)));
        }

        if (mods.size() > 0) {
            try {
                m_context.modifyAttributes(dn, (ModificationItem[])mods.toArray(new ModificationItem[mods.size()]));
            } catch (NameNotFoundException e) {
            	logger.fine ("...WARNING: object doesn't exist, continuing.");
            	// TODO: send warning here?
            } catch (NamingException e) {
                if(e.getMessage().contains("16")) {
                    logger.fine("...already removed, operation unnecessary.");
                } else {
                    throw new IdMUnitException("Modification failure: Error: " + e.getMessage(), e);
                }
            }
        } else {
            logger.fine("...No attributes to update");
        }

        logger.finer(STR_SUCCESS);
    }

    public void opDeleteObject(Map<String, Collection<String>> data) throws IdMUnitException {
        String dn;
        try {
            dn = getTargetDn(data);
        } catch (IdMUnitException e) {
            //A wild-card search failed to find an entry, or the DN field in the spreadsheet is blank
            logger.fine("---> Wild-card deletion found no DNs matching the specified filter, or the DN in the spreadsheet is blank.");
            return; //Never fail when told to delete a DN that isn't there
        }

        try {
            m_context.lookup(dn);
            logger.finer("...performing LDAP deletion for: [" + dn + "]");
            m_context.unbind(dn);
            logger.finer(STR_SUCCESS);
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
        String dn = getSingleValue(data, STR_DN);
        if (dn == null) {
            throw new IdMUnitException("A Distinguished Name must be supplied in column '" + STR_DN + "'");
        }

        String newDn = getSingleValue(data, STR_NEW_DN);
        if (newDn == null) {
            throw new IdMUnitException("A Distinguished Name must be supplied in column '" + STR_NEW_DN + "'");
        }

        logger.finer("...performing LDAP move/rename for: [" + dn + "] to [" + newDn + "].");
        try {
            m_context.rename(dn, newDn);
            logger.finer(STR_SUCCESS);
        } catch (NamingException e) {
            throw new IdMUnitException("Move/Rename failure: Error: " + e.getMessage(), e);
        }
    }

    public void opRemoveAttr(Map<String, Collection<String>> data) throws IdMUnitException {
        String dn = getTargetDn(data);
        logger.finer("...performing LDAP modification for: [" + dn + "]");

        TreeMap<String, Collection<String>> curAttrs;
		try {
			curAttrs = getAttributes(dn);
		} catch (IdMUnitException e) {
			logger.info(e.toString());
			return;
		}		

        List<ModificationItem> mods = new ArrayList<ModificationItem>();
        for (String attrName : data.keySet()) {
            if (attrName.equalsIgnoreCase(STR_DN)) {
                continue;
            } else if(attrName.equals(STR_UNICODE_PASSWORD)) {
                byte[] unicodePwdVal = getUnicodeBytes(getSingleValue(data, STR_UNICODE_PASSWORD));
                mods.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute(attrName, unicodePwdVal)));
            } else if (attrName.equals(STR_DXML_ASSOC)) {
                Collection<String> values = data.get(attrName);
                Collection curAssociations = (Collection) curAttrs.get(STR_DXML_ASSOC);
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
                Collection<String> values = data.get(attrName);
                Attribute modValues = new BasicAttribute(attrName);

                Collection<String> curValues = curAttrs.get(attrName);
                if (curValues != null) {
	                for (String curValue : curValues) {
	                    for (String value : values) {
	                        if (curValue.matches(value)) {
	                            modValues.add(curValue);
	                        }
	                    }
	                }
                }
                mods.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE, modValues));
                logger.finer("...preparing to remove [" + modValues + "] from '" + attrName + "'.");
            }
        }

        if (mods.size() > 0) {
            try {
                m_context.modifyAttributes(dn, (ModificationItem[])mods.toArray(new ModificationItem[mods.size()]));
            } catch (NamingException e) {
                if(e.getMessage().contains("16")) {
                    logger.finer("...already removed, operation unnecessary.");
                } else {
                    throw new IdMUnitException("Modification failure: Error: " + e.getMessage(), e);
                }
            }
        } else {
            logger.fine("...No attributes to update");
        }

        logger.finer(STR_SUCCESS);
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
    }
    
    public void doValidate(Map<String, Collection<String>> expectedAttrs, boolean bIsAttrDoesNotExistTest) throws IdMUnitException {
        String dn = getTargetDn(expectedAttrs);

        Map<String, Collection<String>> appAttrs = getAttributes(dn);

        Failures failures = new Failures();
        for (String attrName : expectedAttrs.keySet()) {
            if (attrName.equalsIgnoreCase(STR_DN)) {
                continue;
            }

            Collection<String> expectedValues = expectedAttrs.get(attrName);
            Collection<String> actualValues;
            if (operationalAttributes.contains(attrName)) {
                // Operational attributes are not returned from the server when
                // all attributes are requested so we need to read them
                // explicitly.
                actualValues = getAttributes(dn, new String[]{attrName}).get(attrName);
                continue;
            } else {
                actualValues = appAttrs.get(attrName);
            }

            if (actualValues == null || actualValues.size() == 0) {
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
            String passwordVal = getSingleValue(expectedAttrs, STR_USER_PASSWORD);
            logger.finer("...performing LDAP password validation for: [" + dn + "]");
            tempConn = createLDAPConnection(dn, passwordVal);
            logger.finer(STR_SUCCESS);
        } catch (IdMUnitException e) {
            throw new IdMUnitFailureException("Password validation failure: Error: " + e.getMessage());
        } finally {
            if (tempConn != null) { 
                try {
                    tempConn.close();
                } catch (NamingException e) {
                    logger.info("...Failed to close validatePassword context.");
                }
            }
        }
    }
    
    public void setup(Map config) throws IdMUnitException {
        this.config = config;
        this.m_context = createLDAPConnection();
    }

    public void tearDown() throws IdMUnitException {
        try {
            if(m_context!=null) {
                this.m_context.close();
                this.m_context = null;
            }
        } catch (NamingException e) {
            throw new IdMUnitException("Failed to close ldap connection: " + e.getMessage());
        }
    }

    private InitialDirContext createLDAPConnection() throws IdMUnitException {
        String userDN = (String)config.get(CONFIG_USER);
        String password = (String)config.get(CONFIG_PASSWORD);

        return createLDAPConnection(userDN, password);
    }

    private InitialDirContext createLDAPConnection(String userDN, String password) throws IdMUnitException {
        String server = (String)config.get(CONFIG_SERVER);
        String keystorePath = (String)config.get(CONFIG_KEYSTORE_PATH);

        Hashtable<String, String> env = new Hashtable<String, String>();
        if (keystorePath != null && keystorePath.length() > 0) {
            System.setProperty("javax.net.ssl.trustStore", keystorePath);
            env.put(Context.SECURITY_PROTOCOL, "ssl");
        }
        env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
        env.put("com.sun.jndi.ldap.connect.pool", "true");
        env.put("com.sun.jndi.ldap.connect.pool.protocol", "plain ssl");
        env.put("com.sun.jndi.ldap.connect.pool.timeout", "1000");
        env.put("com.sun.jndi.ldap.connect.pool.maxsize", "3");
        env.put("com.sun.jndi.ldap.connect.pool.prefsize", "1");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.PROVIDER_URL, "ldap://" + server);
        env.put(Context.SECURITY_PRINCIPAL, userDN);
        env.put(Context.SECURITY_CREDENTIALS, password);
        env.put("com.sun.jndi.ldap.connect.timeout", "5000");

        try {
            return new InitialDirContext(env);
        }catch (Exception e) {
            if (keystorePath != null && keystorePath.length() > 0) {
                logger.finer("### Failed to obtain an SSL LDAP server connection to: [" + server + "].");
                throw new IdMUnitException("Failed to obtain an SSL LDAP Connection: " + e.getMessage(), e);
            } else {
                logger.finer("### Failed to obtain an LDAP server connection to: [" + server + "].");
                throw new IdMUnitException("Failed to obtain an LDAP Connection: " + e.getMessage());
            }
        }
    }

    private String findUser(String base, String filter) throws IdMUnitException {
        String resolvedDn = null;
        
        SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        try {
            //Find users with a matching ID
            NamingEnumeration results = this.m_context.search(base, filter, ctls);
            SearchResult sr;
            int resultCtr = 0;
            while (results.hasMore()) {
                ++resultCtr;
                sr = (SearchResult) results.next();
                if(resultCtr==1) {
                	if(sr.getName()!=null && sr.getName().length()>0) {
                		resolvedDn = sr.getName() + "," + base;
                		logger.finer("---> Target DN for validation: [" + resolvedDn + "]");
	        		} else resolvedDn = base;
                		logger.finer("---> Target DN for validation: [" + resolvedDn + "]");
                } else { 
                    logger.finer("---> Other objects found matching filter: [" + sr.getName() + "," + base + "].");
                }
            }
        } catch (NamingException ne) {
            throw new IdMUnitException("Object Lookup for filter [" + filter +"], base ["+base+"] Failed: " + ne.getMessage());
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
            throw new IdMUnitException("Error reading attributes for '" + dn + "'.");
        }
    }

    private String getTargetDn(Map<String, Collection<String>> data) throws IdMUnitException {
        String STR_BASE_DN_DELIMITER = ",base=";

        String dn = getSingleValue(data, STR_DN);
        if (dn == null) {
            throw new IdMUnitException("A Distinguished Name must be supplied in column '" + STR_DN + "'");
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
            
            dn = findUser(base, filter);
        } else {
            //Detect standard wildcard token * in the ID
            String[] nameComponents = dn.split("(?<!\\\\),");
            String idVal = nameComponents[0];
            if (idVal.indexOf("*")==-1) return dn;
            // cn=TIDMTST*1,ou=users,o=myorg
            logger.finer("---> ID to search: " + idVal);

            String base = dn.substring(dn.indexOf(nameComponents[1]));
            String filter = "("+idVal+")";
            logger.finer("---> Synthesized filter: " + filter + " from the base: " + base);
                
            dn = findUser(base, filter);
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
        logger.finer("...performing LDAP modification for: [" + dn + "]");

        List<ModificationItem> mods = new ArrayList<ModificationItem>();
        for (String attrName : dataRow.keySet()) {
            if (attrName.equalsIgnoreCase(STR_DN)) {
                continue;
            } else if(attrName.equals(STR_UNICODE_PASSWORD)) {
                byte[] unicodePwdVal = getUnicodeBytes(getSingleValue(dataRow, STR_UNICODE_PASSWORD));
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
                Collection<String> values = dataRow.get(attrName);
                Attribute modValues = new BasicAttribute(attrName);                       
                for (Iterator j=values.iterator(); j.hasNext(); ) {
                    modValues.add(j.next());
                }
                mods.add(new ModificationItem(operationType, modValues));
                logger.finer("...preparing to update attr: [" + attrName + "] with value [" + values + "]");
            }
        }

        if (mods.size() > 0) {
            try {
                m_context.modifyAttributes(dn, (ModificationItem[])mods.toArray(new ModificationItem[mods.size()]));
            } catch (NamingException e) {
                if(e.getMessage().contains("16")) {
                    logger.finer("...already removed, operation unnecessary.");
                } else {
                    throw new IdMUnitException("Modification failure: Error: " + e.getMessage(), e);
                }
            }
        } else {
            logger.fine("...No attributes to update");
        }

        logger.finer(STR_SUCCESS);
    }

    private static TreeMap<String, Collection<String>> attributesToMap(Attributes attributes) throws NamingException {
        TreeMap<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
        NamingEnumeration i = null;
        try {
            for (i=attributes.getAll(); i.hasMore(); ) {
                Attribute attr = (Attribute)i.next();
                String attrName = attr.getID();
                List<String> attrValues = new LinkedList<String>();
                for (NamingEnumeration j=attr.getAll(); j.hasMore(); ) {
                    Object value = j.next();
                    if (value instanceof String) {
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

    private static void compareAttributeValues(String attrName, Collection<String> expected, Collection<String> actual, Failures failures) throws IdMUnitException {
        for (String actualValue : actual) {
            for (String expectedValue : expected) {
                // Support validation of whether or not an attribute exists (using a wildcard in the spreadsheet - '*')
                if (expectedValue.equals("*")) {
                    logger.finest(STR_SUCCESS + ": validating attribute: [" + attrName + "] EXPECTED: [" + expectedValue + "] ACTUAL: [" + actualValue + "]");
                    return;
                }
                
                if (expectedValue.equals(actualValue)) {
                    logger.finest(STR_SUCCESS + ": validating attribute: [" + attrName + "] EXPECTED: [" + expectedValue +"] ACTUAL: [" + actualValue + "]");
                    return;
                }
            }
        }
        
        failures.add(attrName, expected, actual);
    }

    private static String getDXMLAssocByDriverName(String driverDn, Collection attr) {
        for (Iterator i=attr.iterator(); i.hasNext(); ) {
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
