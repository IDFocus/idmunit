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

import java.util.Iterator;
import java.util.Map;

import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;

import junit.framework.TestCase;

import org.apache.commons.collections.iterators.CollatingIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ddsteps.DDStepsTestCase;
import org.ddsteps.testcase.support.DDStepsExcelTestCase;

import org.ddsteps.dataset.bean.DataRowBean;
import org.ddsteps.dataset.bean.DataValueBean;
import org.ddsteps.junit.behaviour.DdRowBehaviour;
import org.idmunit.connector.Connection;
import org.idmunit.connector.ConnectionConfigData;
import org.idmunit.extension.BasicLogger;
import org.idmunit.extension.EmailUtil;
import org.idmunit.teststep.TestStepDispatcher;

/**
 * Implements the core functionality of IdMUnit.
 * @author Brent Kynaston, Software Engineer, TriVir LLC
 * @version %I%, %G%
 * @see TestCase
 * @see DDStepsTestCase
 */
public abstract class IdMUnitTestCase extends DDStepsExcelTestCase {
    static Log LOG = LogFactory.getLog(IdMUnitTestCase.class);
    Map operationalDataMap;
	Map attributeMap;
    Connection targetConnection;
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
	public void setAttributeMap(Map attrMap) {
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
	public void setOperationalDataMap(Map dataMap) {
		if(this.operationalDataMap != null && this.operationalDataMap.size() > 0) {
			this.operationalDataMap.putAll(dataMap);
		} else {
		this.operationalDataMap = dataMap;
		}
	}

	private void insertAttr(Attributes attrs, String attrName, String attrVal) {
		if(attrs!=null) {
			if(attrName!=null && attrVal!=null) {
				attrs.put(attrName, attrVal);
			}
		}
	}

	private Attributes getRowData() {
		BasicAttributes attrs = new BasicAttributes();
		
		Iterator itr = this.attributeMap.keySet().iterator();
		while(itr.hasNext()) {
			String attrName = (String)itr.next();
			String attrVal = (String)this.attributeMap.get(attrName);
			insertAttr(attrs, attrName, attrVal);
		}
        return attrs; 
	}

	private DataRowBean getRowDataBean() {
		DataRowBean dataRow = new DataRowBean();
		Iterator itr = this.attributeMap.keySet().iterator();
		while(itr.hasNext()) {
			String attrName = (String)itr.next();
			String attrVal = (String)this.attributeMap.get(attrName);
	        dataRow.addValue(new DataValueBean(attrName,attrVal));
		}
        return dataRow;
	}
	
	private String getOperation() {
		return getOperation(Constants.STR_OPERATION);
	}

	private String getOperation(String op) {
		String selectedOperation = (String)this.operationalDataMap.get(op);
		if((selectedOperation == null || selectedOperation.length() < 1) && op.equalsIgnoreCase(Constants.STR_OPERATION)) {
			fail(Constants.ERROR_MISSING_OP);
		}
		return selectedOperation;
	}

	private long getRetryCount() {
		long maxRetryCount=0;
		Object longObject = getOperation(Constants.STR_RETRY_COUNT);
		if(longObject != null) {
    		try {
    			String longVal = (String)longObject;
    			//Strip whitespace
    			longVal = longVal.trim();
    			maxRetryCount = Long.parseLong(longVal);
			} catch (NumberFormatException e) {
		    	maxRetryCount = Constants.DEFAULT_MAX_RETRY_COUNT;
				LOG.error("Failed to parse " + Constants.STR_RETRY_COUNT + " into a value of type long.");
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
		if(op.equalsIgnoreCase(Constants.OP_COMMENT)) {
    		LOG.info(Constants.STR_COMMENT + "\n\n---------------------------------"
    				+ "\nTest Case: " + this.getName()
    				+ "\nDescription:\n" 
    				+ getOperation(Constants.OP_COMMENT)
    				+ "\n---------------------------------\n");
    		return false;
    	} 
    	String disableOp = getOperation(Constants.STR_DISABLE_STEP);
		if (disableOp != null && disableOp.equalsIgnoreCase(Constants.STR_TRUE)) {
    		LOG.info(this.getName() + " " + Constants.STR_SKIPPING_ROW);
			return false;
    	} 
   		return true;
	}

	private void generateEmailAlert(Alert alert, String content, String exceptionMessage) throws IdMUnitException {
		LOG.info("### Sending email alert...");
    	EmailUtil.sendEmailNotification(alert, content, exceptionMessage, null);
    }
    
    private void generateLogAlert(Alert alert, String content) {
    	LOG.info("### Writing log alert...");
    	BasicLogger.fileLog(content, alert.getLogPath());
    }
    
    private void sendAlerts(Alert idmunitAlert, String exceptionMessage) throws IdMUnitException {
    	try {
        	String alertMessage = this.getName()+" failed.";
    		if(idmunitAlert.isEmailAlertingEnabled()) {
    			generateEmailAlert(idmunitAlert, alertMessage, exceptionMessage);
    		}
    		if(idmunitAlert.isLogAlertingEnabled()) {
    			generateLogAlert(idmunitAlert, alertMessage + exceptionMessage);
    		}
		} catch (IdMUnitException e) {
			throw new IdMUnitException(e.getMessage()+" "+"### Email alert failed!!");
		}
    }
    
    private void delay(long delayInterval) {
    	try {
    		Thread.sleep(delayInterval);
    	} catch (InterruptedException ie) {
			LOG.info("delay interrupted");
    	}
    }

    private void delay() {
    	long delayInterval = Constants.DEFAULT_DELAY;
    	//check for delay interval override
    	Object delayObject = getOperation(Constants.STR_WAIT_INTERVAL);
    	if(delayObject != null) {
    		String delayVal = null;
    		try {
    			delayVal = (String)delayObject;
    			//Trim whitespace
    			delayVal = delayVal.trim();
	    		delayInterval = Long.parseLong(delayVal);
			} catch (NumberFormatException e) {
				LOG.error("Failed to parse " + Constants.STR_WAIT_INTERVAL + " into a value of type long: " + e.getMessage() + ": delayVal: [" + delayVal + "]");
			}
    	}
    	if(configData.ifMultiplierWait()) {
    		delay((delayInterval * configData.getMultiplierWait()));
    	} else {
    		delay(delayInterval);
    	}
    }
    
    private void initializeConfigData() throws IdMUnitException {
		String target = getOperation(Constants.STR_TARGET);
		//Obtain the collection of connections
		Map connectionMap = (Map)this.operationalDataMap.get(Constants.XML_CONNECTIONS);
		configData = (ConnectionConfigData)connectionMap.get(target); 
		if(configData == null) throw new IdMUnitException(Constants.ERROR_NO_CONFIG);
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
    	long maxRetryCount = getRetryCount();
    	executeTest(--maxRetryCount);
    }
	
    private void evalutateExpectedResult(boolean shouldIFail, Throwable exceptionInfo) {
		String negateExpectation = getOperation(Constants.STR_EXPECT_FAILURE);
		boolean negateTestResult = false;
		if(negateExpectation!=null && negateExpectation.length()>0) {
			negateTestResult = Boolean.parseBoolean(negateExpectation);
		}
		if(shouldIFail == negateTestResult) {
			return; //allow the test to be reported as successful because there was a failure, but it was expected
		} else {
			if(exceptionInfo!=null) {
				fail(exceptionInfo.getMessage());
			} else {
				fail("Test failed: would have succeeded, but ExpectFailure was set to TRUE");
			}
		}

    }
    
    private void setupTargetConnection() throws IdMUnitException {
		if(getOperation().equalsIgnoreCase(Constants.OP_WAIT)) {
			return;
		}		
		String type = configData.getType();
		
		if(type==null || type.length() < 1) {
			throw new IdMUnitException(Constants.ERROR_MISSING_TARGET);
		}
		
		try {
			this.targetConnection = (Connection)Class.forName(type).newInstance();
			this.targetConnection.setupConnection(configData);
		} catch (InstantiationException e) {
			throw new IdMUnitException(Constants.ERROR_CLASS_INSTATIATION + type + " " + e.getMessage());
		} catch (IllegalAccessException e) {
			throw new IdMUnitException(Constants.ERROR_ILLEGAL_ACCESS + type);
		} catch (ClassNotFoundException e) {
			throw new IdMUnitException(Constants.ERROR_CLASS_NOT_FOUND + type);
		}
	}

    /**
     * This method makes the decision as to which test step will be executed and calls the TestStepDispatcher accordingly.
     * Additional IdMUnit test step verbs may be added here.
     * @see TestStepDispatcher
     * @throws IdMUnitException
     */
    private void executeTestIMPL() throws IdMUnitException {
    	//check the operation and dispatch the appropriate test step
    	String op = getOperation();
    	LOG.info(this.getName() + ": " + getOperation(Constants.OP_COMMENT));

    	//obtain target system connection info
    	setupTargetConnection();

    	boolean alreadyEvaluated=false;
    	try {
	    	if(op.equalsIgnoreCase(Constants.OP_ADD_OBJECT)) {
	        	//add user to source AD domain
	    		DataRowBean dataRow = getRowDataBean();
	    	    TestStepDispatcher.getTestStepAddObject(operationalDataMap, dataRow,
	       				this.targetConnection).runStep();
	    	} else if(op.equalsIgnoreCase(Constants.OP_MOD_OBJECT)
	    			|| op.equalsIgnoreCase(Constants.OP_MODIFY_OBJECT)
	    			|| op.equalsIgnoreCase(Constants.OP_MOD_ATTR)) {
	    		Attributes dataRow = getRowData();
	    	    TestStepDispatcher.getTestStepModObject(dataRow, 
	       				this.targetConnection).runStep(DirContext.REPLACE_ATTRIBUTE);
	    	}else if(op.equalsIgnoreCase(Constants.OP_EXEC_SQL)) {
	    		Attributes dataRow = getRowData();
	    	    this.targetConnection.insertObject(dataRow);
	    	} else if(op.equalsIgnoreCase(Constants.OP_ADD_ATTR)) {
	    		Attributes dataRow = getRowData();
	    	    TestStepDispatcher.getTestStepModObject(dataRow, 
	       				this.targetConnection).runStep(DirContext.ADD_ATTRIBUTE);
	    	} else if(op.equalsIgnoreCase(Constants.OP_REMOVE_ATTR)) {
	    		Attributes dataRow = getRowData();
	    	    TestStepDispatcher.getTestStepModObject(dataRow, 
	       				this.targetConnection).runStep(DirContext.REMOVE_ATTRIBUTE);
	    	}else if(op.equalsIgnoreCase(Constants.OP_CLEAR_ATTR)) {
	    		Attributes dataRow = getRowData();
	    	    TestStepDispatcher.getTestStepModObject(dataRow, 
	       				this.targetConnection).runStep(Constants.CLEAR_ATTRIBUTE);
	    	} else if(op.equalsIgnoreCase(Constants.OP_DEL_OBJECT)) {
	    		Attributes dataRow = getRowData();
	    	    TestStepDispatcher.getTestStepDeleteObject(dataRow, 
	       				this.targetConnection).runStep();
	    	} else if(op.equalsIgnoreCase(Constants.OP_VALIDATE_OBJECT)) {
	    		Attributes dataRow = getRowData();
	    	    TestStepDispatcher.getTestStepValidateObject(dataRow, 
	       				this.targetConnection).runStep();
	    	} else if(op.equalsIgnoreCase(Constants.OP_VALIDATE_PASSWORD)) {
	    		Attributes dataRow = getRowData();
	    	    TestStepDispatcher.getTestStepValidatePassword(dataRow, 
	       				this.targetConnection).runStep();
		    } else if(op.equalsIgnoreCase(Constants.OP_MOV_OBJECT) 
		    		|| op.equalsIgnoreCase(Constants.OP_REN_OBJECT) 
		    		|| op.equalsIgnoreCase(Constants.OP_RENAME_OBJECT)) {
	    		Attributes dataRow = getRowData();
	    	    TestStepDispatcher.getTestStepMoveObject(dataRow, 
	       				this.targetConnection).runStep();
		    } else if(op.equalsIgnoreCase(Constants.OP_COMMENT)) {
		    	return;
		    } else if(op.equalsIgnoreCase(Constants.OP_WAIT) || op.equalsIgnoreCase(Constants.OP_PAUSE)) {
		    	delay();
		    	return;
		    } else {
		    	fail(Constants.ERROR_UNKNOWN_OP + " " + Constants.STR_DETECTED_OPERATION + op);
		    }
    	} catch(Throwable t) {
    		boolean expectFailure = true; //Expect failure because we hit an exceptional case
    		evalutateExpectedResult(expectFailure, t);
    		alreadyEvaluated = true;
    	} finally {
    		if(targetConnection!=null) {
    			this.targetConnection.closeConnection();
    			this.targetConnection = null;
    		}
    	}
    	//Wait to allow for standard latency (interval override in spread sheet)
    	delay();
    	//Report appropriately the result of the test (may be negated)
    	if(!alreadyEvaluated) {
    		boolean expectFailure = false; //We should succeed because the test did not throw a failure up to this point
    		evalutateExpectedResult(expectFailure, null);
    	}
	}

    private void executeTest(long retriesRemaining) throws IdMUnitException {
    	try {
			executeTestIMPL();
		} catch (Throwable t) {
			//If retries are enabled, retry the test after the defined wait interval
			if(retriesRemaining > 0) {
				delay();
				LOG.info(Constants.STR_RETRY + " (" 
						+ retriesRemaining + "} "  
						+ t.getMessage() + " " + this.getName());
					executeTest(--retriesRemaining);
			} else {
				LOG.info(Constants.STR_FINAL_TRY_ERROR + ": " + t.getMessage());
				//Generate email/log alerts if this row was marked as "critical" and if alerts are enabled.
				Boolean isCritical = Boolean.valueOf(getOperation(Constants.STR_IS_CRITICAL));
				boolean isAlertingEnabled = (this.configData.getIdmunitAlerts()!=null) ? true : false;
				 
				if(isCritical && isAlertingEnabled) {
					//Iterate through each configured alert for this connection configuration (may contain globally-defined alerts as well) and fire off each enabled alert
					Map alerts = configData.getIdmunitAlerts();
					Iterator alertItr = alerts.keySet().iterator();
					while(alertItr.hasNext()){
						String alertName = (String)alertItr.next();
						Alert idmunitAlert = (Alert)alerts.get(alertName);
						sendAlerts(idmunitAlert, t.getMessage());
					}
				}
				//TODO: make all failures of exception type IDMUnitFailure, then leverage IDMUnitException only for error results
				if(t.getClass().getName().indexOf("junit") == -1) {
					throw new IdMUnitException(t.getMessage());
				} else {
					fail(t.getMessage());
				}
			}
		}
    }
}