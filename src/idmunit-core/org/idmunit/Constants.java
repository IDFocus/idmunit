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

/**
 * Defines literals and constant values for use throughout the project
 * @author Brent Kynaston, Software Engineer, TriVir LLC
 * @version %I%, %G%
 */
public class Constants {
	/**
	 * IdMUnit versions should always directly correspond to the major release versions of Designer for Novell Identity Manager 
	 * in order to be clear on which releases of IdMUnit have been certified for use with which versions of Designer.
	 */
	public final static String VERSION = "v.1.2"; 
	public final static int DTF_BUFFER = 1000; //allocate up to this many bytes for the output to insert into the delimited text file
	public final static long DEFAULT_DELAY = 5000; //5 seconds default
	public final static long DEFAULT_MAX_RETRY_COUNT = 10; //if an event fails and retry mode is enabled, the test step will be retried this many times before failing. Overrides in the properites and spreadsheet layers. 
	
	public final static String ERROR_NOT_IMPLEMENTED = "This feature has not yet been implemented.";
	public final static String ERROR_DN_FAILED = "Failed to resolve target DN: ";
	public final static String ERROR_BAD_LDAP_FILTER = "Check the dn or LDAP filter specified in the spreadsheet.";
	public final static String ERROR_MISSING_OP = "Missing operation - Please check the operation column for this row and try again.";
	public final static String ERROR_UNKNOWN_OP = "Unknown operation - Please check the operation column for this row and try again.";
	public final static String ERROR_MISSING_TARGET = "Missing target type definition.  Please add a type specifier for all targets defined.";
	public final static String ERROR_MISSING_LIB = "Missing library.  Please ensure that the jar file that contains the following class exists: ";
	public final static String ERROR_MISSING_PROPERTIES_FILE = "Please add the folder containing idmunit-defaults.properties to the classpath of the project.";
	public final static String ERROR_CLASS_INSTATIATION = "Failed to instantiate connection class of type: ";
	public final static String ERROR_ILLEGAL_ACCESS = "Illegal access error when attempting to instantiate connection of type: ";
	public final static String ERROR_CLASS_NOT_FOUND = "Specified target connection module not found: ";
	public final static String ERROR_INJECTOR_CLASS_NOT_FOUND = "Specified DataInjector class not found.  Please ensure it is included in the classpath. Classname: ";
	public final static String ERROR_INJECTOR_CLASS_FAILED_TO_LOAD = "Specified DataInjector class failed to load. Error: ";
	public final static String ERROR_INJECTOR_CLASS_ILLEGAL_ACCESS = "Specified DataInjector class accessed illegally. Error: ";
	public final static String ERROR_BAD_CONFIG = "Configuration file is invalid. Ensure that the target profile has connections defined according to the idmunit.dtd)";
	public final static String ERROR_DTF_CONFIG = "Configuration error - the DTF connector serverUrl should contain inputFilePath=VALUE1|outputFilePath=VALUE2 where value1 and value2 provide the correct path with full filename (including extension).";
	public final static String ERROR_NO_CONFIG = "Configuration not found. Ensure that idmunit-config.xml is in the location specified by idmunit-defaults.properites.";
	public final static String ERROR_NO_TARGET = "Live target profile not specified. Ensure that the idmunit root node in idmunit-config.xml contains a live-profile specification. The target must be defined in the same xml file along with its corresponding connections.";
	public final static String ERROR_BAD_TARGET = "Live target profile not found. Ensure that the idmunit root node in idmunit-config.xml contains a VALID live-profile specification.";
	public final static String ERROR_NO_CONFIG_FOR_SPECIFIC_CONNECTION = "No connection configuration information found for the specified target: ";
	public final static String ERROR_MISSING_CREDENTIALS = "Missing administrative credentials. Is the correct IdMUnit live-profile specified?  Please refer to the idmunit-config.dtd for the specification of configuration data.  Note that if the DTF connector used, the connector type should be org.idmunit.connector.DTF.";
	public final static String CHECK_DTD = "Please check the idmunit.dtd and update the idmunit.xml configuration file.";
	public final static String ERROR_MISSING_SERVER= "Missing server - " + CHECK_DTD;
	public final static String ERROR_MISSING_TARGET_TYPE = "Missing target type- " + CHECK_DTD;
	public final static String ERROR_MISSING_DN = "Move/Rename failure: Must specify a 'newdn' in a column of the spreadsheet for this test step.";
	public final static String ERROR_MISSING_ALERT_CONFIG = "Must configure at least one alert configuration if alerts are enabled.  Please see idmunit-config.dtd for more details.";
	public final static String ERROR_FAILED_MOVE = "Move/Rename failure: Error: ";
	
	public final static String WARNING_NO_DN = "---> Wild-card deletion found no DNs matching the specified filter, or the DN in the spreadsheet is blank.";

	public final static String STR_LOGGING_DATE_FORMAT = "yyyyMMdd HH:mm:ss"; 
	public final static String STR_SQL = "sql";
	public final static String STR_DN = "dn";
	public final static String STR_NEW_DN = "newdn";
	public final static String STR_ATTRMAP = "attributeMap";
	public final static String STR_OPERATION_DATAMAP = "operationalDataMap";
	public final static String STR_TRUE = "true";
	public final static String STR_FALSE = "false";
	public final static String STR_SUCCESS = "...SUCCESS";
	public final static String STR_FAILURE = "...FAILURE";
	public final static String STR_DETECTED_OPERATION = "Detected operation: ";
	
	public final static String STR_DTF_CONNECTOR = "org.idmunit.connector.DTF"; //If the target connector is this, IdMUnit will not enforce the connection to have an admin name/password/etc.
	
	public final static String STR_UNICODE_PASSWORD = "unicodePwd";
	public final static String STR_DXML_ASSOC = "DirXML-Associations";
	public final static String STR_PROCESSED_ASSOC = "#1#";
	public final static String STR_DISABLED_ASSOC = "#0#";
	public final static String STR_MIGRATE_ASSOC = "#4#";
	
	public final static String STR_METADATA_DELIM = "//";
	public final static String STR_OPERATION = "Operation";
	public final static String STR_WAIT_INTERVAL = "WaitInterval";
	public final static String STR_EXPECT_FAILURE = "ExpectFailure";
	public final static String STR_RETRY_COUNT = "RetryCount";
	public final static String STR_DISABLE_STEP = "DisableStep";
	public final static String STR_IS_CRITICAL = "IsCritical";
	
	
	public final static String STR_CONFIG_LOCATION = "ConfigLocation";
	public final static String STR_TARGET_PROFILE = "TargetProfile";
	public final static String STR_APPLY_SUBST_TO_OPERATIONS = "ApplySubstitutionsToOperations";
	public final static String STR_APPLY_SUBST_TO_DATA = "ApplySubstitutionsToData";
	
	
	public final static String STR_TARGET = "Target";
	public final static String STR_TARGET_PREFIX = "Target_";
	public final static String STR_ALERT_PREFIX = "idmunitAlert_";
	public final static String STR_TARGET_SERVER = "Server";
	public final static String STR_TARGET_ADMIN = "Admin";
	public final static String STR_TARGET_PASSWORD = "AdminPassword";
	public final static String STR_TARGET_TYPE = "Type";
	public final static String STR_TARGET_KEYSTORE_PATH = "KeystorePath";
	
	public final static String STR_USER_PASSWORD = "userPassword";
	
	public final static String STR_RETRY = "RETRY";
	public final static String STR_FINAL_TRY_ERROR = "...FAILURE";
	public final static String STR_TRIES_REMAINING = "REMAINING ATTEMPTS:";
	public final static String STR_SKIPPING_ROW = "Row disabled... skipping.";
	public final static String STR_COMMENT = "Comment data: (this row will not be processed)";
	
	
	public final static String OP_EXEC_SQL = "execSQL";
	public final static String OP_WRITE_DTF = "writeDTF";
	public final static String OP_ADD_OBJECT = "addObject";
	public final static String OP_MOD_OBJECT = "modObject";
	public final static String OP_MODIFY_OBJECT = "modifyObject";
	public final static String OP_ADD_ATTR = "addAttr";
	public final static String OP_MOD_ATTR = "modAttr";
	public final static String OP_REMOVE_ATTR = "removeAttr";
	public final static String OP_CLEAR_ATTR = "clearAttr";
	public final static String OP_DEL_OBJECT = "delObject";
	public final static String OP_REN_OBJECT = "renObject";
	public final static String OP_RENAME_OBJECT = "renameObject";
	public final static String OP_MOV_OBJECT = "moveObject";
	public final static String OP_VALIDATE_OBJECT = "validateObject";
	public final static String OP_VALIDATE_PASSWORD = "validatePassword";
	public final static String OP_VALIDATE_ATTR = "validateAttr";
	public final static String OP_COMMENT = "Comment";
	public final static String OP_WAIT = "Wait";
	public final static String OP_PAUSE = "Pause";
	
	public final static String XML_TARGET = "target";
	public final static String XML_NAME = "name";
	public final static String XML_PROFILES = "profiles";
	public final static String XML_ALERTS = "alerts";
	public final static String XML_DESCRIPTION = "description";
	public final static String XML_TYPE = "type";
	public final static String XML_SERVER = "server";
	public final static String XML_USER = "user";
	public final static String XML_PASSWORD = "password";
	public final static String XML_KEYSTORE = "keystore-path";
	public final static String XML_MULTI = "multiplier";
	public final static String XML_RETRY = "retry";
	public final static String XML_WAIT = "wait";
	public final static String XML_ALERT_SMTP_SERVER = "smtp-server";
	public final static String XML_ALERT_SENDER = "alert-sender";
	public final static String XML_ALERT_RECIPIENT = "alert-recipient";
	public final static String XML_ALERT_SUBJECT_PREFIX = "subject-prefix";
	public final static String XML_ALERT_LOG_PATH = "log-path";

	
	public final static String XML_SUBSTITUTIONS = "substitutions";
	public final static String XML_INJECTIONS = "data-injections";
	public final static String XML_REPLACE = "replace";
	public final static String XML_NEW = "new";
	public final static String XML_KEY = "key";
	public final static String XML_FORMAT = "format";
	public final static String XML_MUTATOR = "mutator";
	public final static String XML_CONNECTIONS = "connections";
	public final static String XML_LIVE_PROFILE = "live-profile";
	public final static String XML_ENABLE_EMAIL_ALERTS = "enable-email-alerts";
	public final static String XML_ENABLE_LOG_ALERTS = "enable-log-alerts";
	
	public final static String TOKEN_WILDCARD = "*";
	
	public static final String KEY_VALUE_SEPARATOR = "==";

	public final static int CLEAR_ATTRIBUTE = 4;
	
	public static final String PROP_ENABLE_EMAIL = "EnableEmailNotifications";
	public static final String PROP_ENABLE_POLLING = "EnablePolling";
	public static final String PROP_SMTP_SERVER = "SMTPServer";
	public static final String PROP_EMAIL_SUBJ = "EmailSubject";
	public static final String PROP_EMAIL_TO = "AdminEmailAddress";
	public static final String PROP_EMAIL_FROM = "EmailFromAddress";
	public static final String PROP_EMAIL_BC = "EmailBlindCopy";
	public static final String PROP_DELETE_MODE = "EnableDelete";
	public static final String PROP_EXECUTION_MODE = "ExecutionMode";
	public static final String PROP_MARK_FOR_DEPROV_MODE = "EnableMarkForDeprovisioning";
	public static final String PROP_ATTR_DEPROV_MARK = "DeprovisionedAttribute";
	public static final String PROP_EMAIL_MSG_HTML = "EmailMessageBodyFile";
	public static final String PROP_ADVANCED_NOTICE = "AdvancedNoticeMaxDays";
	public static final String PROP_LDAP_SERVER = "LDAPServer";
	public static final String PROP_LDAP_ADMIN = "LDAPAdmin";
	public static final String PROP_LDAP_PSWD = "LDAPPassword";
	public static final String PROP_LDAP_SEARCH_BASE = "SearchBase";
	public static final String PROP_LDAP_FILTER = "LDAPFilter";
	public static final String PROP_LDAP_RETURN_ATTRS = "ReturnAttrs";
	public static final String PROP_KEYSTORE_PATH = "KeystorePath";
	public static final String PROP_LOG_FILE = "LogFile";
	public static final String PROP_DEBUG = "Debug";
	public static final String PROP_DATE_FORMAT = "yyyyMMdd";
	

	
}
