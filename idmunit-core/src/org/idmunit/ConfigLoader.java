/* 
 * IdMUnit - Automated Testing Framework for Identity Management Solutions
 * Copyright (c) 2005-2010 TriVir, LLC
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
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
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
    private final static String XML_NAME = "name";
    private final static String XML_PROFILES = "profiles";
    private final static String XML_ALERTS = "alerts";
    private final static String XML_DESCRIPTION = "description";
    private final static String XML_TYPE = "type";
    private final static String XML_SERVER = "server";
    private final static String XML_USER = "user";
    private final static String XML_PASSWORD = "password";
    private final static String XML_KEYSTORE = "keystore-path";
    private final static String XML_MULTI = "multiplier";
    private final static String XML_RETRY = "retry";
    private final static String XML_WAIT = "wait";
    private final static String XML_ALERT_SMTP_SERVER = "smtp-server";
    private final static String XML_ALERT_SENDER = "alert-sender";
    private final static String XML_ALERT_RECIPIENT = "alert-recipient";
    private final static String XML_ALERT_SUBJECT_PREFIX = "subject-prefix";
    private final static String XML_ALERT_LOG_PATH = "log-path";

    
    private final static String XML_SUBSTITUTIONS = "substitutions";
    private final static String XML_INJECTIONS = "data-injections";
    private final static String XML_REPLACE = "replace";
    private final static String XML_NEW = "new";
    private final static String XML_KEY = "key";
    private final static String XML_FORMAT = "format";
    private final static String XML_MUTATOR = "mutator";
    private final static String XML_LIVE_PROFILE = "live-profile";
    private final static String XML_ENABLE_EMAIL_ALERTS = "enable-email-alerts";
    private final static String XML_ENABLE_LOG_ALERTS = "enable-log-alerts";

    private final static String STR_ALERT_PREFIX = "idmunitAlert_";
    
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
		    throw new IdMUnitException("Configuration file is invalid. Ensure that the target profile has connections defined according to the idmunit.dtd) - " + jde.getMessage());
		} catch (FileNotFoundException fnfe) {
		    throw new IdMUnitException("Configuration not found. Ensure that idmunit-config.xml is in the location specified by idmunit-defaults.properites.");
		}
		return inputDoc;
    }
	
	/**
	 * Retrieves configured connection information 
	 * @param connection The top level connection node
	 * @return Collection of substitutions
	 */
	private static Map<String, String> getSubstitutions(Element connection) {
		String replaceVal;
		String newVal;
		Iterator substitutionItr = connection.getChild(XML_SUBSTITUTIONS).getChildren().iterator();
		Map<String, String> substitutionMap = new LinkedHashMap<String, String>();
		while(substitutionItr.hasNext()) {
			Element substitution = (Element)substitutionItr.next();
			replaceVal = substitution.getChildText(XML_REPLACE);
			newVal = substitution.getChildText(XML_NEW);
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
			throw new IdMUnitException("Specified DataInjector class failed to load. Error: " + className);					
		} catch (InstantiationException e) {
			throw new IdMUnitException("Specified DataInjector class failed to load. Error: " + className);					
		} catch (ClassNotFoundException e) {
			throw new IdMUnitException("Specified DataInjector class not found.  Please ensure it is included in the classpath. Classname: " + className);					
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
	private static List<InjectionConfigData> getDataInjections(Element connection) throws IdMUnitException {
		String className;
		String keyName;
		String format;
		String mutator;
		Element dataInjections = connection.getChild(XML_INJECTIONS);
		//Data injections are optional (not required in the DTD) so if null, return
		if(dataInjections==null) return null;
		Iterator injectionItr = dataInjections.getChildren().iterator();
		List<InjectionConfigData> injectionList = new ArrayList<InjectionConfigData>();
		while(injectionItr.hasNext()) {
			Element injection = (Element)injectionItr.next();
			className = injection.getChildText(XML_TYPE);
			keyName = injection.getChildText(XML_KEY);
			format = injection.getChildText(XML_FORMAT);
			mutator = injection.getChildText(XML_MUTATOR);
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
	private static Map getProfiles(Document doc, Map<String, ConnectionConfigData> connectionMap, String liveTarget, String enableEmailAlerts, String enableLogAlerts) throws IdMUnitException {
		Iterator profileItr = doc.getRootElement().getChild(XML_PROFILES).getChildren().iterator();
		while(profileItr.hasNext()) {
			Element profile = (Element)profileItr.next();
			String profileName = profile.getAttributeValue(XML_NAME);
			if(!liveTarget.equalsIgnoreCase(profileName)) {
				continue;
			}
			log("### Selected profile: = [" + profileName + "]");
			Iterator connectionItr = profile.getChildren().iterator();
			while(connectionItr.hasNext()) {
				Element connection = (Element)connectionItr.next();
				String name = connection.getChildText(XML_NAME);
				String type = connection.getChildText(XML_TYPE);
                ConnectionConfigData configurationData = new ConnectionConfigData(name, type);

                for (Iterator i = connection.getChildren().iterator(); i.hasNext(); ) {
                	Element param = (Element)i.next();
                	if (param.getName().equals(XML_MULTI) ||
                    	param.getName().equals(XML_SUBSTITUTIONS) ||
                    	param.getName().equals(XML_INJECTIONS) ||
                    	param.getName().equals(XML_ALERTS) ||
                    	param.getName().equals(XML_NAME) ||
                		param.getName().equals(XML_TYPE))
                	{
                		continue;
                	}
                	if (param.getName().equals(XML_PASSWORD)) {
                		//decrypt the password first
                		EncTool encryptionManager = new EncTool("IDMUNIT1");
                    	configurationData.setParam(XML_PASSWORD, encryptionManager.decryptCredentials(param.getText()));
                	} else {
                    	configurationData.setParam(param.getName(), param.getText());
                	}
//    				log("### \t" + param.getName() + ": " + param.getText());
                }

				Element multiplier = connection.getChild(XML_MULTI);
				String retryMultiplier = multiplier.getChildText(XML_RETRY);
				String waitMultiplier = multiplier.getChildText(XML_WAIT);
				
				log("###\t---------------------------------");
				log("### \tConnection Name: " + configurationData.getName());
				log("### \tDescription", configurationData.getParam(XML_DESCRIPTION));
				log("### \tType", configurationData.getType());
				log("### \tServer", configurationData.getParam(XML_SERVER));
				log("### \tUser", configurationData.getParam(XML_USER));
				log("### \tPassword" ,"***************"); 
				log("### \tKeystore Path", configurationData.getParam(XML_KEYSTORE));
				log("### \tMultipliers:");
				log("###\t\tRetries multiplied by", retryMultiplier);
				log("###\t\tWaits multiplied by", waitMultiplier);
				log("### \tData Substitutions:");
				Map<String, String> substitutionMap = getSubstitutions(connection);
				log("### \tData Injections:");
				List<InjectionConfigData> injectionList = getDataInjections(connection);
                if(type == null || type.length() < 1) {
                    throw new IdMUnitException("Missing target type- Please check the idmunit.dtd and update the idmunit.xml configuration file.");
                }

                if(substitutionMap!=null && substitutionMap.size() > 0) {
                    configurationData.setSubstitutions(substitutionMap);
                }

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

				if(enableEmailAlerts.equalsIgnoreCase("true") || enableLogAlerts.equalsIgnoreCase("true")) {
					Map<String, Alert> alerts = getAlerts(doc, enableEmailAlerts, enableLogAlerts);
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
		throw new IdMUnitException("Live target profile not found. Ensure that the idmunit root node in idmunit-config.xml contains a VALID live-profile specification.");
	}

	/**
	 * Reads and returns a collection of configured alerts
	 * @param doc JDOMDocument that contains configuration information 
	 * @param enableEmailAlerts When true, email alerts will be sent for test steps that have the //IsCritical column set to true
	 * @param enableLogAlerts When true, log alerts will be written for test steps that have the //IsCritical column set to true
	 * @return Map collection of alerts
	 * @throws IdMUnitException
	 */
	private static Map<String, Alert> getAlerts(Document doc, String enableEmailAlerts, String enableLogAlerts) throws IdMUnitException {
		if((enableEmailAlerts==null || enableEmailAlerts.equalsIgnoreCase("false")) 
				&& (enableLogAlerts ==null || enableLogAlerts.equalsIgnoreCase("false"))) {
			return null; //Do not process alert config information if alerts are disabled
		}
		Map<String, Alert> alerts = new HashMap<String, Alert>();
		Iterator alertItr = doc.getRootElement().getChild(XML_ALERTS).getChildren().iterator();
		while(alertItr.hasNext()) {
			Element alert = (Element)alertItr.next();
			String alertName = alert.getAttributeValue(XML_NAME);
			String alertDescription = alert.getChildText(XML_DESCRIPTION);
			String smtpServer = alert.getChildText(XML_ALERT_SMTP_SERVER);
			String alertSender = alert.getChildText(XML_ALERT_SENDER);
			String alertRecipient = alert.getChildText(XML_ALERT_RECIPIENT);
			String alertSubjectPrefix = alert.getChildText(XML_ALERT_SUBJECT_PREFIX);
			String alertLogPath = alert.getChildText(XML_ALERT_LOG_PATH);
			log("###\t---------------------------------");
			logAlerts("### \tAlert Name: ", alertName);
			logAlerts("### \tDescription", alertDescription);
			logAlerts("### \tSMTP Mail Server" ,smtpServer);
			logAlerts("### \tSender Name" ,alertSender);
			logAlerts("### \tAlert Recipient" ,alertRecipient);
			logAlerts("### \tSubject Prefix" ,alertSubjectPrefix); //TODO: hide
			logAlerts("### \tLog Path" ,alertLogPath);
			Alert idmunitAlert = new Alert(alertName, alertDescription, smtpServer, alertSender, alertRecipient, alertSubjectPrefix, alertLogPath);
			idmunitAlert.setEmailAlertingEnabled(Boolean.valueOf(enableEmailAlerts)); //TODO: extend the idmunit-config.xml DTD to support overriding of enable/disable log/email message at the connector level
			idmunitAlert.setLogAlertingEnabled(Boolean.valueOf(enableLogAlerts));
			alerts.put(STR_ALERT_PREFIX+alertName, idmunitAlert);
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
	public static Map<String, ConnectionConfigData> getConfigData(String configFileLocation) throws IdMUnitException {
		Map<String, ConnectionConfigData> connectionMap = new HashMap<String, ConnectionConfigData>();
		Document doc = loadXMLFromFS(configFileLocation);
		String liveTarget = doc.getRootElement().getAttributeValue(XML_LIVE_PROFILE);
		String enableEmailAlerts = doc.getRootElement().getAttributeValue(XML_ENABLE_EMAIL_ALERTS);
		if(enableEmailAlerts==null) enableEmailAlerts = "false";
		String enableLogAlerts = doc.getRootElement().getAttributeValue(XML_ENABLE_LOG_ALERTS);
		if(enableLogAlerts==null) enableLogAlerts = "false";
		log("IdMUnit Properties Entry: ConfigLocation = [" + configFileLocation + "]");
		log("### Loading IdMUnit configuration profiles....");
		log("### Enable email alerts: = [" + enableEmailAlerts + "]");
		log("### Enable log alerts: = [" + enableLogAlerts + "]");
		log("### Attempting to select live target: [" + liveTarget + "]");
//      public final static String ERROR_NO_TARGET = 
		if(liveTarget == null || liveTarget.length() < 1) throw new IdMUnitException("Live target profile not specified. Ensure that the idmunit root node in idmunit-config.xml contains a live-profile specification. The target must be defined in the same xml file along with its corresponding connections.");
		 getProfiles(doc, connectionMap, liveTarget, enableEmailAlerts, enableLogAlerts);
		displayedConfig = true;
		return connectionMap;
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
