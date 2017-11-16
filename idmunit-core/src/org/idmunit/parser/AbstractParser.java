/*
 * Abstract class that defines properties for Parsers that retrieve testdata from other sources. 
 */
package org.idmunit.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.Test;

/**
 * Abstract class for defining different types of parsers for testdata.<br/>
 * Parsers should extend this class and return a valid Test object from the createSuite() method.
 * @author mvreijn
 *
 */
public abstract class AbstractParser implements Parser {

    private static Log LOG = LogFactory.getLog(AbstractParser.class);

	public abstract Test createSuite(Class<?> testClass);

	// This has been separated from the excel parser
	protected static Properties loadProperties() throws IOException {
		Properties properties = System.getProperties();
        InputStream propertiesFile = AbstractParser.class.getClassLoader().getResourceAsStream("idmunit-defaults.properties");
        if (propertiesFile == null) {
        	LOG.warn("Unable to find idmunit-defaults.properties");
        } else {
            Properties defaultProperties = new Properties();
            defaultProperties.load(propertiesFile);
            for (Enumeration<?> en = defaultProperties.propertyNames(); en.hasMoreElements();) {
                String key = (String) en.nextElement();
                if (properties.containsKey(key) == false) {
                    properties.put(key, defaultProperties.getProperty(key));
                }
            }
        }
		return properties;
	}

}
