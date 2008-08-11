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
package org.idmunit;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ddsteps.junit.behaviour.DdRowBehaviour;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.idmunit.Alert;
import org.idmunit.connector.ConnectionConfigData;
import org.idmunit.injector.Injection;
import org.idmunit.injector.InjectionConfigData;

/**
 * Loads/parses XML credential and config data for the target system (see idmunit.dtd for config data layout)
 * @author Brent Kynaston, Software Engineer, TriVir LLC
 * @version %I%, %G%
 * @see ConnectionConfigData
 * @see org.idmunit.connector.Connection
 * @see Alert
 * @see DdRowBehaviour
 */
public class ConfigLoader {
    static Log LOG = LogFactory.getLog(ConfigLoader.class);
    static boolean displayedConfig; //config is currently loaded per row, but we only want to display config info on the first row.  TODO: we'll refactor config to be loaded once per test rather than row later
    static boolean displayedAlerts;
	private ConfigLoader() {}
	
/**
 * Load XML configuration data from the file system (i.e. idmunit-config.xml)
 * @param fullPath The full filesystem path to the XML document to be read (ex: C:/IdMUnit/src/src/test-resources/idmunit-config.xml)
 * @return JDOMDocument 
 * @throws IdMUnitException
 */
	private static Document loadXMLFromFS(String fullPath) throws IdMUnitException {
        SAXBuilder fileSystemDoc = new SAXBuilder();
        Document inputDoc = null;
	    try {
	        inputDoc = fileSystemDoc.build(new FileReader(fullPath));
	    } catch (JDOMException jde) {
		    throw new IdMUnitException(Constants.ERROR_BAD_CONFIG + " - " + jde.getMessage());
		} catch (FileNotFoundException fnfe) {
		    throw new IdMUnitException(Constants.ERROR_NO_CONFIG );
		}
		return inputDoc;
    }
	
	/**
	 * Retrieves configured connection information 
	 * @param connection The top level connection node
	 * @return Collection of substitutions
	 */
	private static Map getSubstitutions(Element connection) {
		String replaceVal;
		String newVal;
		Iterator substitutionItr = connection.getChild(Constants.XML_SUBSTITUTIONS).getChildren().iterator();
		Map substitutionMap = new LinkedHashMap();
		while(substitutionItr.hasNext()) {
			Element substitution = (Element)substitutionItr.next();
			replaceVal = substitution.getChildText(Constants.XML_REPLACE);
			newVal = substitution.getChildText(Constants.XML_NEW);
			if(replaceVal != null && replaceVal.length() > 0 
					&& newVal != null && newVal.length() > 0) {
				log("###\t\tValue to replace", replaceVal);
				log("###\t\tNew value", newVal);
				substitutionMap.put(replaceVal,newVal);
			}
		}
		return substitutionMap;
	}

	/**
	 * Returns a dynamically generated data value to inject into the test data. For example, today's date may be generated and formated for use within the data.
	 * @param className Name of the class to load that will generate the dynamic data (should extend the org.idmunit.injector.injection interface)
	 * @param mutator May be used to modify the value of the dynamic data.  In the date example, the date value can be pushed ahead or backwards.
	 * @param format Formatter for the value.  For a date the standard SimpleDateFormat applies (i.e. yyyyMMdd)
	 * @return String that contains the dynamically generated data value
	 * @throws IdMUnitException
	 */
	private static String getInjectorValueString(String className, String mutator, String format) throws IdMUnitException {
		if(className==null) return null;
		Injection injector;
		try {
			injector = (Injection)Class.forName(className).newInstance();
		} catch (IllegalAccessException e) {
			throw new IdMUnitException(Constants.ERROR_INJECTOR_CLASS_FAILED_TO_LOAD + className);					
		} catch (InstantiationException e) {
			throw new IdMUnitException(Constants.ERROR_INJECTOR_CLASS_FAILED_TO_LOAD + className);					
		} catch (ClassNotFoundException e) {
			throw new IdMUnitException(Constants.ERROR_INJECTOR_CLASS_NOT_FOUND + className);					
		}
		if(mutator!=null && mutator.length()>1) {
			injector.mutate(mutator); //An example of a mutator is a date offset that would push a dynamic date forward or backward
		}
		String dynamicInjectionDataValue = injector.getDataInjection(format);
		log("###\t\tDynamicaly generated value", dynamicInjectionDataValue);
		return dynamicInjectionDataValue;
	}
	
	/**
	 * Reads and returns a collection of all data injections for the current connection
	 * @param connection The top connection node
	 * @return Collection of data injections
	 * @throws IdMUnitException
	 */
	private static List getDataInjections(Element connection) throws IdMUnitException {
		String className;
		String keyName;
		String format;
		String mutator;
		Element dataInjections = connection.getChild(Constants.XML_INJECTIONS);
		//Data injections are optional (not required in the DTD) so if null, return
		if(dataInjections==null) return null;
		Iterator injectionItr = dataInjections.getChildren().iterator();
		List<InjectionConfigData> injectionList = new ArrayList<InjectionConfigData>();
		while(injectionItr.hasNext()) {
			Element injection = (Element)injectionItr.next();
			className = injection.getChildText(Constants.XML_TYPE);
			keyName = injection.getChildText(Constants.XML_KEY);
			format = injection.getChildText(Constants.XML_FORMAT);
			mutator = injection.getChildText(Constants.XML_MUTATOR);
			if(className != null && className.length() > 0 
					&& keyName != null && keyName.length() > 0) {
				log("###\t\tClass used to inject data", className);
				log("###\t\tKey value to replace", keyName);
				log("###\t\tFormat of injected data", format);
				log("###\t\tMutator", mutator);
				InjectionConfigData injectionConfig = new InjectionConfigData(className, keyName, format);
				injectionConfig.setMutator(mutator);
				getInjectorValueString(className, mutator, format);
				injectionList.add(injectionConfig);
			}
		}
		return injectionList;
	}
	/**
	 * Reads and returns a collection of profiles.  A profile is a collection of like connections for a targeted environment (i.e. dev, test or prod)
	 * @param doc The JDOMDocument that contains configuration information
	 * @param connectionMap Collection of connection configuration information
	 * @param liveTarget The system target for testing (i.e. dev may be selected as the live-target out of dev, test and prod)
	 * @param enableEmailAlerts When true, email alerts will be sent for test steps that have the //IsCritical column set to true
	 * @param enableLogAlerts When true, log alerts will be written for test steps that have the //IsCritical column set to true
	 * @return Map collection of profile configurations
	 * @throws IdMUnitException
	 */
	private static Map getProfiles(Document doc, Map connectionMap, String liveTarget, String enableEmailAlerts, String enableLogAlerts) throws IdMUnitException {
		ConnectionConfigData configurationData = null;		
		Iterator profileItr = doc.getRootElement().getChild(Constants.XML_PROFILES).getChildren().iterator();
		while(profileItr.hasNext()) {
			Element profile = (Element)profileItr.next();
			String profileName = profile.getAttributeValue(Constants.XML_NAME);
			if(!liveTarget.equalsIgnoreCase(profileName)) {
				continue;
			}
			log("### Selected profile: = [" + profileName + "]");
			Iterator connectionItr = profile.getChildren().iterator();
			while(connectionItr.hasNext()) {
				Element connection = (Element)connectionItr.next();
				String name = connection.getChildText(Constants.XML_NAME);
				String description = connection.getChildText(Constants.XML_DESCRIPTION);
				String type = connection.getChildText(Constants.XML_TYPE);
				String server = connection.getChildText(Constants.XML_SERVER);
				String user = connection.getChildText(Constants.XML_USER);
				String password = connection.getChildText(Constants.XML_PASSWORD);
				String keystore = connection.getChildText(Constants.XML_KEYSTORE);
				
				Element multiplier = connection.getChild(Constants.XML_MULTI);
				String retryMultiplier = multiplier.getChildText(Constants.XML_RETRY);
				String waitMultiplier = multiplier.getChildText(Constants.XML_WAIT);
				
				log("###\t---------------------------------");
				log("### \tConnection Name: " + name);
				log("### \tDescription", description);
				log("### \tType" ,type);
				log("### \tServer" ,server);
				log("### \tUser" ,user);
				log("### \tPassword" ,"***************"); 
				log("### \tKeystore Path" ,keystore);
				log("### \tMultipliers:");
				log("###\t\tRetries multiplied by", retryMultiplier);
				log("###\t\tWaits multiplied by", waitMultiplier);
				log("### \tData Substitutions:");
				Map substitutionMap = getSubstitutions(connection);
				log("### \tData Injections:");
				List injectionList = getDataInjections(connection);
				configurationData = setupConfigData(server, user, password, keystore, type, substitutionMap);
				if(retryMultiplier!=null && retryMultiplier.length() > 0) {
					int retryMulti = Integer.parseInt(retryMultiplier);
					if(retryMulti > 0) {
						configurationData.setMultiplierRetry(Integer.parseInt(retryMultiplier));
					}
				}
				if(waitMultiplier!=null && waitMultiplier.length() > 0) {
					int waitMulti = Integer.parseInt(waitMultiplier);
					if(waitMulti > 0) {
						configurationData.setMultiplierWait(Integer.parseInt(waitMultiplier));
					}
				}
				if(enableEmailAlerts.equalsIgnoreCase(Constants.STR_TRUE) || enableLogAlerts.equalsIgnoreCase(Constants.STR_TRUE)) {
					Map alerts = getAlerts(doc, enableEmailAlerts, enableLogAlerts);
					if(alerts!=null) {
						configurationData.setIdmunitAlerts(alerts); //TODO: All alerts are stored here (for every connection) so that in the future we can tie a particular alert to a specific system error, and override alert config from the connector config level at one point
					}
				}
				if(injectionList!=null) {
					configurationData.setDataInjections(injectionList);
				}
				connectionMap.put(name, configurationData);
			}
			//We don't need to process any more profiles, we found the live profile and grabed the config data
			return connectionMap;
		}
		throw new IdMUnitException(Constants.ERROR_BAD_TARGET);
	}

	/**
	 * Reads and returns a collection of configured alerts
	 * @param doc JDOMDocument that contains configuration information 
	 * @param enableEmailAlerts When true, email alerts will be sent for test steps that have the //IsCritical column set to true
	 * @param enableLogAlerts When true, log alerts will be written for test steps that have the //IsCritical column set to true
	 * @return Map collection of alerts
	 * @throws IdMUnitException
	 */
	private static Map getAlerts(Document doc, String enableEmailAlerts, String enableLogAlerts) throws IdMUnitException {
		if((enableEmailAlerts==null || enableEmailAlerts.equalsIgnoreCase(Constants.STR_FALSE)) 
				&& (enableLogAlerts ==null || enableLogAlerts.equalsIgnoreCase(Constants.STR_FALSE))) {
			return null; //Do not process alert config information if alerts are disabled
		}
		Map alerts = new HashMap<String, Alert>();
		Iterator alertItr = doc.getRootElement().getChild(Constants.XML_ALERTS).getChildren().iterator();
		while(alertItr.hasNext()) {
			Element alert = (Element)alertItr.next();
			String alertName = alert.getAttributeValue(Constants.XML_NAME);
			String alertDescription = alert.getChildText(Constants.XML_DESCRIPTION);
			String smtpServer = alert.getChildText(Constants.XML_ALERT_SMTP_SERVER);
			String alertSender = alert.getChildText(Constants.XML_ALERT_SENDER);
			String alertRecipient = alert.getChildText(Constants.XML_ALERT_RECIPIENT);
			String alertSubjectPrefix = alert.getChildText(Constants.XML_ALERT_SUBJECT_PREFIX);
			String alertLogPath = alert.getChildText(Constants.XML_ALERT_LOG_PATH);
			log("###\t---------------------------------");
			logAlerts("### \tAlert Name: ", alertName);
			logAlerts("### \tDescription", alertDescription);
			logAlerts("### \tSMTP Mail Server" ,smtpServer);
			logAlerts("### \tSender Name" ,alertSender);
			logAlerts("### \tAlert Recipient" ,alertRecipient);
			logAlerts("### \tSubject Prefix" ,alertSubjectPrefix); //TODO: hide
			logAlerts("### \tLog Path" ,alertLogPath);
			//org.idmunit.Alert idmunitAlert = new Alert();
			Alert idmunitAlert = new Alert(alertName, alertDescription, smtpServer, alertSender, alertRecipient, alertSubjectPrefix, alertLogPath);
			idmunitAlert.setEmailAlertingEnabled(Boolean.valueOf(enableEmailAlerts)); //TODO: extend the idmunit-config.xml DTD to support overriding of enable/disable log/email message at the connector level
			idmunitAlert.setLogAlertingEnabled(Boolean.valueOf(enableLogAlerts));
			alerts.put(Constants.STR_ALERT_PREFIX+alertName, idmunitAlert);
		}
		displayedAlerts = true;
		return alerts;
	}
	/**
	 * Reads and returns a collection of configuration data 
	 * @see "idmunit-config.xml and idmunit-comfig.dtd"
	 * @param configFileLocation The full filesystem path to the XML document to be read (ex: C:/IdMUnit/src/src/test-resources/idmunit-config.xml)
	 * @return Map collection of configuration data
	 * @throws IdMUnitException
	 */
	public static Map getConfigData(String configFileLocation) throws IdMUnitException {
		Map connectionMap = new HashMap();
		Document doc = loadXMLFromFS(configFileLocation);
		String liveTarget = doc.getRootElement().getAttributeValue(Constants.XML_LIVE_PROFILE);
		String enableEmailAlerts = doc.getRootElement().getAttributeValue(Constants.XML_ENABLE_EMAIL_ALERTS);
		if(enableEmailAlerts==null) enableEmailAlerts=Constants.STR_FALSE;
		String enableLogAlerts = doc.getRootElement().getAttributeValue(Constants.XML_ENABLE_LOG_ALERTS);
		if(enableLogAlerts==null) enableLogAlerts= Constants.STR_FALSE;
		log("IdMUnit Properties Entry: ConfigLocation = [" + configFileLocation + "]");
		log("### Loading IdMUnit configuration profiles....");
		log("### Enable email alerts: = [" + enableEmailAlerts + "]");
		log("### Enable log alerts: = [" + enableLogAlerts + "]");
		log("### Attempting to select live target: [" + liveTarget + "]");
		if(liveTarget == null || liveTarget.length() < 1) throw new IdMUnitException(Constants.ERROR_NO_TARGET);
		 getProfiles(doc, connectionMap, liveTarget, enableEmailAlerts, enableLogAlerts);
		displayedConfig = true;
		return connectionMap;
	}

	/** 
	 * Builds and returns an object instance to contain connection configuration data after some basic configuration checking. See {@link ConnectionConfigData}.
	 * @param server Connection server
	 * @param admin Administrative distinguished name or username
	 * @param password DES encrypted, base64 encoded password value @see {@link EncTool}
	 * @param keystorePath Full filesystem path to the keystore where a base64 encoded server certificate has been imported by the Java keytool utility.  If set, a connector should attempt an SSL connection.
	 * @param type The fully distinguished class name that will be loaded to implement the connector (i.e. org.idmunit.connector.ISeries)
	 * @param substitutionMap Collection of substitutions.  A substitution is a key/value pair that allows for the replacement of test data while it is executed.
	 * @return ConnectionConfigurationData Encapsulates configuration information for a single connection
	 * @throws IdMUnitException
	 */
	private static ConnectionConfigData setupConfigData(String server, String admin, String password, String keystorePath, String type, Map substitutionMap) throws IdMUnitException {
		//Validate target input (and don't require credentials if it's a DTF connection
		if(server == null || server.length() < 1) {
			throw new IdMUnitException(Constants.ERROR_MISSING_SERVER);
		}
		if(type == null || type.length() < 1) {
			throw new IdMUnitException(Constants.ERROR_MISSING_TARGET_TYPE);
		}
		
		if(type!=null && !type.equalsIgnoreCase(Constants.STR_DTF_CONNECTOR)) {
			//Only check credentials if it's not a DTF file connection
			if(admin == null || admin.length() < 1
					|| password == null || password.length() < 1) {
				throw new IdMUnitException(Constants.ERROR_MISSING_CREDENTIALS);
			}
		}
			
		ConnectionConfigData newCreds = new ConnectionConfigData(server, admin,password);
		newCreds.setType(type);

		//TODO: refactor to instantiate com.idmunit.connection.LDAPSSLConnection, or configured target class
		if(keystorePath!=null && keystorePath.length()>0) {
			newCreds.setKeystorePath(keystorePath);
		}

		if(substitutionMap!=null && substitutionMap.size() > 0) {
			newCreds.setSubstitutions(substitutionMap);
		}
		return newCreds;
	}
	/**
	 * Writes a single string out to the log4j log (if it has not already been written in a group of configuration data)
	 * @param value
	 */
	private static void log(String value) {
		if(!displayedConfig) {
			log(null, value);
		}
	}

	/**
	 * Writes a single string out to the log4j log (if it has not already been written in a group of alert data)
	 * @param name
	 * @param value
	 */
	private static void logAlerts(String name, String value) {
		if(!displayedAlerts) {
			log(name, value);
		}
	}
	private static void log(String name, String value) {
		if(!displayedConfig) {
			if(name !=null && name.length() > 0 && value!=null && value.length() > 0) {
				LOG.info(name + ": " + value);
			} else if(value!=null && value.length() > 0) {
				LOG.info(value); 
			}
		}
	}
}
