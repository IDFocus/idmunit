package org.idmunit.parser;

import junit.framework.Test;
import junit.framework.TestCase;

public class DatabaseParser extends AbstractParser {

	public Test createSuite(Class<?> testClass)
	{
		// TODO Implement Database parser which gets the classname from the DB and parses the tests from there. 
    	return new TestCase(testClass.getSimpleName()) {
			@Override
			public void runTest() throws Exception {
				throw new Exception("DatabaseParser not implemented yet");
			}
		};
	}

}
