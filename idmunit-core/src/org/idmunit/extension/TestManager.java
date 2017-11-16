/*
 * IdMUnit (c) Extension for Test Management tooling integration
 */
package org.idmunit.extension;

import java.util.Map;

import org.idmunit.IdMUnitException;

/**
 * Interface for integration of third-party test management tools. <br/>
 * These tools generally allow test runs to be started and test results to be posted in some way. <br/>
 * Extensions that integrate such tools should use this interface.  
 * @author mvreijn
 *
 */
public interface TestManager {

	/**
	 * IdMUnit passes the complete configuration to the setup method of the extension.<br/>
	 * The extension should filter its own settings and prepare the connection. 
	 * @param config the complete parsed IdMUnit configuration
	 * @throws IdMUnitException
	 */
	void setup( Map<String,String> config ) throws IdMUnitException;

	/**
	 * Called when a new testrun is started. IdMUnit will take the resulting string and pass it back into the interface methods. 
	 * @param testSetId the configured identifier of the testset. 
	 * @return testRunId: a string value that represents the current testrun. <br/>
	 * Will only be used for communication with this interface.
	 * @throws IdMUnitException
	 */
	String startTestRun( String testSetId ) throws IdMUnitException; 

	/**
	 * Called when the current testrun is finished. IdMUnit will pass the id string that was returned from startTestRun().  
	 * @param testRunId
	 * @throws IdMUnitException
	 */
	void endTestRun( String testRunId ) throws IdMUnitException;

	/**
	 * Called when a testCaseId is found in the testdata. The result is a string representation of boolean "true/false".
	 * @param testRunId
	 * @param testCaseId
	 * @param result
	 * @throws IdMUnitException
	 */
	void addTestResult( String testRunId, String testCaseId, String result ) throws IdMUnitException;

}
