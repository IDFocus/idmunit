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
package org.idmunit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.LinkedHashMap;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ddsteps.DDStepsTestCase;
import org.ddsteps.testcase.support.DDStepsExcelTestCase;

import org.idmunit.connector.ConnectionConfigData;
import org.idmunit.connector.ConnectionToConnectorAdapter;
import org.idmunit.connector.Connector;
import org.idmunit.extension.BasicLogger;
import org.idmunit.extension.EmailUtil;
import org.idmunit.teststep.TestStepExecute;

/**
 * Implements the core functionality of IdMUnit.
 * @author Brent Kynaston, Software Engineer, TriVir LLC
 * @version %I%, %G%
 * @see TestCase
 * @see DDStepsTestCase
 */
public abstract class IdMUnitTestCase extends DDStepsExcelTestCase {
    private final static String ERROR_MISSING_TARGET = "Missing target type definition.  Please add a type specifier for all targets defined.";
    private final static String STR_MULTI_ATTR_DELIMITER = "|"; //TODO: Enable override operational column in the spreadsheet to override this value if requested 
    private final static String STR_OPERATION = "Operation";
    private final static String STR_WAIT_INTERVAL = "WaitInterval";
    private final static String STR_EXPECT_FAILURE = "ExpectFailure";
    private final static String STR_RETRY_COUNT = "RetryCount";
    private final static String STR_DISABLE_STEP = "DisableStep";
    private final static String STR_IS_CRITICAL = "IsCritical";

    private final static String STR_TARGET = "Target";

    private final static String STR_SKIPPING_ROW = "Row disabled... skipping.";
    private final static String STR_COMMENT = "Comment data: (this row will not be processed)";

    private final static String OP_COMMENT = "Comment";
    private final static String OP_WAIT = "Wait";
    private final static String OP_PAUSE = "Pause";

    private final static String XML_CONNECTIONS = "connections";

    private final static long DEFAULT_DELAY = 5000; //5 seconds default
    private final static long DEFAULT_MAX_RETRY_COUNT = 10; //if an event fails and retry mode is enabled, the test step will be retried this many times before failing. Overrides in the properites and spreadsheet layers. 

    private static Log LOG = LogFactory.getLog(IdMUnitTestCase.class);
    private Map<String, Object> operationalDataMap;
	private Map<String, String> attributeMap;
    private Connector connector = null;
	private ConnectionConfigData configData;

    public void setUpBeforeData() throws Exception {
		super.setUpBeforeData();
	}
	
	private void cleanupMap(Map targetMap) {
		if(targetMap!=null) {
			targetMap.clear();
			targetMap = null;
		}
	}
	 
	/**
	 * Cleans up configuration and fixture data constructed in memory prior to the test.  Note that this is for operational use. Actual destruction of any test fixtures
	 * is generally done by a testDestroyFixture sheet in the test spreadsheet.
	 */
	public void tearDownBeforeData() throws Exception {
		super.tearDownBeforeData();
		cleanupMap(this.attributeMap);
		cleanupMap(this.operationalDataMap);
	}
	
	/**
	 * Returns a collection containing the data fields from the spreadsheet (i.e. DN, Surname, Description, Telephone Number/etc.)
	 * @return Map Collection of test data for this test step
	 */
	public Map getAttributeMap() {
		return attributeMap;
	}

	/**
	 * Stores a collection containing the data fields from the spreadsheet (i.e. DN, Surname, Description, Telephone Number/etc.)
	 * @param attrMap Collection of test data for this test step
	 */
	public void setAttributeMap(Map<String, String> attrMap) {
		if(this.attributeMap!=null && this.attributeMap.size() > 0) {
			this.attributeMap.putAll(attrMap);
		} else {
			this.attributeMap = attrMap;
		}
	}

	/**
	 * Returns a collection of operational data for this test step (i.e. the connection target name, test step description, number of retries/waits/etc.)
	 * @return Collection of operation data for this test step
	 */
	public Map getOperationalDataMap() {
		return operationalDataMap;
	}

	/**
	 * Stores a collection of operational data for this test step (i.e. the connection target name, test step description, number of retries/waits/etc.)
	 */
	public void setOperationalDataMap(Map<String, Object> dataMap) {
		if(this.operationalDataMap != null && this.operationalDataMap.size() > 0) {
			this.operationalDataMap.putAll(dataMap);
		} else {
			this.operationalDataMap = dataMap;
		}
	}

	private long getRetryCount() {
		long maxRetryCount=0;
		Object longObject = getOperationData(STR_RETRY_COUNT);
		if(longObject != null) {
    		try {
    			String longVal = (String)longObject;
    			//Strip whitespace
    			longVal = longVal.trim();
    			maxRetryCount = Long.parseLong(longVal);
			} catch (NumberFormatException e) {
		    	maxRetryCount = DEFAULT_MAX_RETRY_COUNT;
				LOG.error("Failed to parse " + STR_RETRY_COUNT + " into a value of type long.");
			}
		}
		if(this.configData!=null && this.configData.ifMultiplierRetry()) {
			return (maxRetryCount * this.configData.getMultiplierRetry());
		} else {
			return maxRetryCount;
		}
	}
	
	private boolean isRowEnabled() {
    	String op = getOperation();
		if(op.equalsIgnoreCase(OP_COMMENT)) {
    		LOG.info(STR_COMMENT + "\n\n---------------------------------"
    				+ "\nTest Case: " + this.getName()
    				+ "\nDescription:\n" 
    				+ getOperationData(OP_COMMENT)
    				+ "\n---------------------------------\n");
    		return false;
    	} 
    	String disableOp = getOperationData(STR_DISABLE_STEP);
		if (disableOp != null && disableOp.equalsIgnoreCase("true")) {
    		LOG.info(this.getName() + " " + STR_SKIPPING_ROW);
			return false;
    	} 
   		return true;
	}

    private void initializeConfigData() throws IdMUnitException {
		String target = getOperationData(STR_TARGET);
		//Obtain the collection of connections
		Map connectionMap = (Map)this.operationalDataMap.get(XML_CONNECTIONS);
		configData = (ConnectionConfigData)connectionMap.get(target); 
		if(configData == null) throw new IdMUnitException("Configuration not found. Ensure that idmunit-config.xml is in the location specified by idmunit-defaults.properites.");
    }
    
    /**
     * Entry point for test step execution. This method is called by a class instance who's name
     * directly corresponds to the spreadsheet being tested.  (i.e. TriVirADDomainDriverTests.java testing TriVirADDomainDriverTests.xls)
     * @throws IdMUnitException
     */
    public void executeTest() throws IdMUnitException {
    	if(!isRowEnabled()) {
    		return;
    	}

    	initializeConfigData();

        if(getOperation().equalsIgnoreCase(OP_COMMENT)) {
            return;
        } else if(getOperation().equalsIgnoreCase(OP_WAIT) || getOperation().equalsIgnoreCase(OP_PAUSE)) {
            delay();
            return;
        }

        if (isNewConnection()) {
            connector = setupTargetConnector();
        } else {
            connector = setupTargetConnection();
        }

        try {
        	for (long retriesRemaining = getRetryCount() - 1;; --retriesRemaining) {
                try {
                    try {
                        new TestStepExecute(operationalDataMap, connector, getOperation(), convertData()).runStep();
                    } finally {
                        //Wait to allow for standard latency (interval override in spread sheet)
                        delay();
                    }

                    if (expectedFailure() == false) {
                    	return;
                    }
                } catch (AssertionFailedError e) {
                	LOG.warn("The connector you are using incorrectly threw an AssertionFailedError. It needs to be updated to use an IdMUnitFailureException.");
                    if (expectedFailure()) {
                    	return;
                    }

                    if (retriesRemaining > 0) {
                        //If retries are enabled, retry the test after the defined wait interval
                        delay();
                        LOG.info("RETRY (" + retriesRemaining + ") " + e.getMessage() + " " + this.getName());
                        continue;
                    } else {
                        LOG.info("...FAILURE: " + e.getMessage());
                        logCriticalErrors(e.getMessage());
                        throw e;
                    }
                } catch (IdMUnitFailureException e) {
                    if (expectedFailure()) {
                    	return;
                    }

                    if (retriesRemaining > 0) {
                        //If retries are enabled, retry the test after the defined wait interval
                        delay();
                        LOG.info("RETRY (" + retriesRemaining + ") " + e.getMessage() + " " + this.getName());
                        continue;
                    } else {
                        LOG.info("...FAILURE: " + e.getMessage());
                        logCriticalErrors(e.getMessage());
                        throw e;
                    }
                } catch (IdMUnitException e) {
                    if (expectedFailure()) {
                    	return;
                    }

                    String msg = "";
                    if (e.getMessage() != null && e.getCause() != null && e.getCause().getMessage() != null) {
                    	msg = e.getMessage() + " " + e.getCause().getMessage();
                    } else if (e.getMessage() != null) {
                    	msg = e.getMessage();
                    } else if (e.getCause() != null && e.getCause().getMessage() != null) {
                    	msg = e.getCause().getMessage();
                    }

                    if (retriesRemaining > 0) {
                        //If retries are enabled, retry the test after the defined wait interval
                        delay();
                        LOG.info("RETRY (" + retriesRemaining + ") " + msg + " " + this.getName());
                        continue;
                    } else {
                        LOG.info("...FAILURE: " + msg);
                        logCriticalErrors(msg);
                        throw e;
                    }
                }

                String msg = "Test failed: would have succeeded, but ExpectFailure was set to TRUE";
                if (retriesRemaining > 0) {
                    //If retries are enabled, retry the test after the defined wait interval
                    delay();
                    LOG.info("RETRY (" + retriesRemaining + ") " + msg + " " + this.getName());
                    continue;
                } else {
                    LOG.info("...FAILURE: " + msg);
                    logCriticalErrors(msg);
                    fail(msg);
                }
        	}
        } finally {
            connector.tearDown();
            connector = null;
        }
    }
    
    private void delay() {
    	long delayInterval = DEFAULT_DELAY;
    	//check for delay interval override
    	Object delayObject = getOperationData(STR_WAIT_INTERVAL);
    	if(delayObject != null) {
    		String delayVal = null;
    		try {
    			delayVal = (String)delayObject;
    			//Trim whitespace
    			delayVal = delayVal.trim();
	    		delayInterval = Long.parseLong(delayVal);
			} catch (NumberFormatException e) {
				LOG.error("Failed to parse " + STR_WAIT_INTERVAL + " into a value of type long: " + e.getMessage() + ": delayVal: [" + delayVal + "]");
			}
    	}

    	try {
        	if(configData.ifMultiplierWait()) {
        		Thread.sleep((delayInterval * configData.getMultiplierWait()));
        	} else {
        		Thread.sleep(delayInterval);
        	}
    	} catch (InterruptedException ie) {
			LOG.info("delay interrupted");
    	}
    }
    
    private boolean expectedFailure() {
        String s = getOperationData(STR_EXPECT_FAILURE);
        boolean expectedFailure = false;
        if(s!=null && s.length()>0) {
        	expectedFailure = Boolean.parseBoolean(s);
        }
    	
        return expectedFailure;
    }
    
	private String getOperation() {
		return getOperationData(STR_OPERATION);
	}

	private String getOperationData(String op) {
		String selectedOperation = (String)this.operationalDataMap.get(op);
		if((selectedOperation == null || selectedOperation.length() < 1) && op.equalsIgnoreCase(STR_OPERATION)) {
			fail("Missing operation - Please check the operation column for this row and try again.");
		}
		return selectedOperation;
	}

    private boolean isNewConnection() throws IdMUnitException {
        String type = configData.getType();
        
        if(type==null || type.length() < 1) {
            throw new IdMUnitException(ERROR_MISSING_TARGET);
        }

        try {
            return Connector.class.isAssignableFrom(Class.forName(type));
        } catch (ClassNotFoundException e1) {
            throw new IdMUnitException("Specified target connection module not found: " + type);
        }
    }

    private void logCriticalErrors(String msg) throws IdMUnitException {
        //Generate email/log alerts if this row was marked as "critical" and if alerts are enabled.
        Boolean isCritical = Boolean.valueOf(getOperationData(STR_IS_CRITICAL));
        boolean isAlertingEnabled = (this.configData.getIdmunitAlerts()!=null) ? true : false;
         
        if(isCritical && isAlertingEnabled) {
            //Iterate through each configured alert for this connection configuration (may contain globally-defined alerts as well) and fire off each enabled alert
            Map alerts = configData.getIdmunitAlerts();
            Iterator alertItr = alerts.keySet().iterator();
            while(alertItr.hasNext()){
                String alertName = (String)alertItr.next();
                Alert idmunitAlert = (Alert)alerts.get(alertName);
                sendAlerts(idmunitAlert, msg);
            }
        }
    }

    private Connector setupTargetConnection() throws IdMUnitException {
        String type = configData.getType();
        
        if(type==null || type.length() < 1) {
            throw new IdMUnitException(ERROR_MISSING_TARGET);
        }

        Connector connector = new ConnectionToConnectorAdapter(configData.getType());
        connector.setup(configDataToMap(configData));
        return connector;
    }

    private Connector setupTargetConnector() throws IdMUnitException {
        String type = configData.getType();
        
        if(type==null || type.length() < 1) {
            throw new IdMUnitException(ERROR_MISSING_TARGET);
        }
        
        try {
            Connector connector = (Connector)Class.forName(type).newInstance();
            connector.setup(configDataToMap(configData));
            return connector;
        } catch (InstantiationException e) {
            throw new IdMUnitException("Failed to instantiate connection class of type: " + type + " " + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new IdMUnitException("Illegal access error when attempting to instantiate connection of type: " + type);
        } catch (ClassNotFoundException e) {
            throw new IdMUnitException("Specified target connection module not found: " + type);
        }
    }

    private static Map<String, String> configDataToMap(ConnectionConfigData configData) {
        Map<String, String> config = new HashMap<String, String>();
        config.put("Server", configData.getServerURL());
        config.put("User", configData.getAdminCtx());
        config.put("Password", configData.getAdminPwd());
        config.put("KeystorePath", configData.getKeystorePath());
        return config;
    }

    private Map<String, Collection<String>> convertData() {
       Map<String, Collection<String>> data = new LinkedHashMap<String, Collection<String>>();
        Iterator itr = attributeMap.keySet().iterator();
        while(itr.hasNext()) {
            String attrName = (String)itr.next();
            String attrVal = (String)attributeMap.get(attrName);

            if (attrVal.indexOf(STR_MULTI_ATTR_DELIMITER) == -1) {
                data.put(attrName, Arrays.asList(new String[] {attrVal}));
            } else {
                List<String> valueList = new ArrayList<String>();
                StringTokenizer tokenizer = new StringTokenizer(attrVal, STR_MULTI_ATTR_DELIMITER);
                while(tokenizer.hasMoreTokens()) {
                    valueList.add(tokenizer.nextToken().trim());
                }
                
                data.put(attrName, valueList);
            }
        }
        return data;
    }
    
    private void sendAlerts(Alert idmunitAlert, String exceptionMessage) throws IdMUnitException {
    	try {
        	String alertMessage = this.getName()+" failed.";
    		if(idmunitAlert.isEmailAlertingEnabled()) {
    			LOG.info("### Sending email alert...");
    	    	EmailUtil.sendEmailNotification(idmunitAlert, alertMessage, exceptionMessage, null);
    		}
    		if(idmunitAlert.isLogAlertingEnabled()) {
    	    	LOG.info("### Writing log alert...");
    	    	BasicLogger.fileLog(alertMessage + exceptionMessage, idmunitAlert.getLogPath());
    		}
		} catch (IdMUnitException e) {
			throw new IdMUnitException(e.getMessage()+" "+"### Email alert failed!!");
		}
    }
}