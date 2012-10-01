/*
 * IdMUnit - Automated Testing Framework for Identity Management Solutions
 * Copyright (c) 2008-2009 TriVir, LLC
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
package org.idmunit.parser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import jxl.BooleanCell;
import jxl.Cell;
import jxl.CellReferenceHelper;
import jxl.CellType;
import jxl.DateCell;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.idmunit.ConfigLoader;
import org.idmunit.ConnectorManager;
import org.idmunit.IdMUnitException;
import org.idmunit.IdMUnitTestCase;
import org.idmunit.connector.ConnectionConfigData;
import org.idmunit.injector.Injection;
import org.idmunit.injector.InjectionConfigData;

public class ExcelParser {
    private static final String XML_CONNECTIONS = "connections";
    private static final String OPERATION_HEADERS = "[Operation]";
    private static final String DEFAULT_HEADERS = "[Default]";
    private static final String STR_CONFIG_LOCATION = "ConfigLocation";
    private static final String STR_APPLY_SUBST_TO_OPERATIONS = "ApplySubstitutionsToOperations";
    private static final String STR_APPLY_SUBST_TO_DATA = "ApplySubstitutionsToData";
    private static final String STR_TARGET = "Target";
	private static final String MULTI_VALUE_DELIMITER = "MultiValueDelimiter";
	private static final String ENCRYPTION_KEY = "EncryptionKey";
	private static final String DECRYPT_PASSWORDS = "DecryptPasswords";
	private static final String DEFAULT_ENCRYPTION_KEY = "IDMUNIT1";

    private static Log LOG = LogFactory.getLog(ExcelParser.class);

    public static Test createSuite(Class<?> testClass) {
        Properties properties;
		try {
			properties = loadProperties();
		} catch (final IOException e) {
        	return new TestCase(testClass.getSimpleName()) {
				@Override
				public void runTest() throws Exception {
					throw new Exception("Error loading idmunit-defaults.properties", e);
				}};
		}

        boolean applySubstToOperations = Boolean.parseBoolean((properties.getProperty(STR_APPLY_SUBST_TO_OPERATIONS)));
        boolean applySubstToAttrData = Boolean.parseBoolean((properties.getProperty(STR_APPLY_SUBST_TO_DATA)));
        String multiValueDelimiter = properties.getProperty(MULTI_VALUE_DELIMITER);
        if (multiValueDelimiter == null) {
            multiValueDelimiter = "|";
        }

        String configLocation = properties.getProperty(STR_CONFIG_LOCATION);
        if(configLocation == null || configLocation.length() == 0) {
        	return new TestCase(testClass.getSimpleName()) {
				@Override
				public void runTest() throws Exception {
					throw new Exception("No value specified for '" + STR_CONFIG_LOCATION + "'");
				}};
        }

    	String encryptionKey = null;
        String decryptPasswords = properties.getProperty(DECRYPT_PASSWORDS);
        if (decryptPasswords == null || Boolean.parseBoolean(decryptPasswords)) {
        	encryptionKey = properties.getProperty(ENCRYPTION_KEY);
        	if (encryptionKey == null) {
        		encryptionKey = DEFAULT_ENCRYPTION_KEY;
        	}
        }

        Map<String, ConnectionConfigData> connectionMap = null;
		try {
			connectionMap = ConfigLoader.getConfigData(configLocation, encryptionKey);
		} catch (final IdMUnitException e) {
        	return new TestCase(testClass.getSimpleName()) {
				@Override
				public void runTest() throws Exception {
					throw new Exception("Error loading configuration file", e);
				}};
		}

		if (connectionMap.size() < 1) {
        	return new TestCase(testClass.getSimpleName()) {
				@Override
				public void runTest() throws Exception {
		        	throw new IdMUnitException("No profiles in configuration");
				}};
        }
        
    	String name = testClass.getSimpleName();
        final String fileName = name + ".xls";
        InputStream is = testClass.getResourceAsStream(fileName);
        if (is == null) {
            URL url = testClass.getResource(fileName);
            final String l = url.toExternalForm();
        	return new TestCase(testClass.getSimpleName()) {
				@Override
				public void runTest() throws Exception {
		        	throw new IdMUnitException("Unable to find workbook '" + l + "'");
				}};
		}

        Workbook workbook;

        try {
            workbook = Workbook.getWorkbook(is);

            if (workbook == null) {
            	return new TestCase(testClass.getSimpleName()) {
    				@Override
    				public void runTest() throws Exception {
    	            	throw new IdMUnitException("Error loading workbook '" + fileName + "'");
    				}};
            }
		} catch (final BiffException e) {
        	return new TestCase(testClass.getSimpleName()) {
				@Override
				public void runTest() throws Exception {
	            	throw new IdMUnitException("Error parsing workbook '" + fileName + "'", e);
				}};
		} catch (final IOException e) {
        	return new TestCase(testClass.getSimpleName()) {
				@Override
				public void runTest() throws Exception {
	            	throw new IdMUnitException("Error reading workbook '" + fileName + "'", e);
				}};
		}

		try {
			TestSuite oldSuite = new TestSuite(testClass);
			TestSuite newSuite = new TestSuite(name);

	    	Enumeration<?> e = oldSuite.tests();
	    	while (e.hasMoreElements()) {
	    		final TestCase test = (TestCase)e.nextElement();
	    		try {
					newSuite.addTest(parseSheet(workbook, test.getName(), connectionMap, applySubstToOperations, applySubstToAttrData, multiValueDelimiter));
				} catch (final Exception e1) {
		        	return new TestCase(testClass.getSimpleName()) {
						@Override
						public void runTest() throws Exception {
			            	throw new IdMUnitException("Error parsing sheet '" + test.getName() + "'", e1);
						}};
				}
	    	}

	    	return newSuite;
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				LOG.error("Error closing file", e);
			}
		}
    }
    
    // This function is only used by unit tests for the parser.
    static Test loadSuite(String workbookFileName, String sheetName) throws IOException, IdMUnitException {
        Properties properties = loadProperties();

        boolean applySubstToOperations = Boolean.parseBoolean((properties.getProperty(STR_APPLY_SUBST_TO_OPERATIONS)));
        boolean applySubstToAttrData = Boolean.parseBoolean((properties.getProperty(STR_APPLY_SUBST_TO_DATA)));
        String multiValueDelimiter = properties.getProperty(MULTI_VALUE_DELIMITER);
        if (multiValueDelimiter == null) {
            multiValueDelimiter = "|";
        }

        String configLocation = properties.getProperty(STR_CONFIG_LOCATION);
        if(configLocation == null || configLocation.length() == 0) {
			throw new IdMUnitException("No value specified for '" + STR_CONFIG_LOCATION + "'");
        }

    	String encryptionKey = null;
        String decryptPasswords = properties.getProperty(DECRYPT_PASSWORDS);
        if (decryptPasswords == null || Boolean.parseBoolean(decryptPasswords)) {
        	encryptionKey = properties.getProperty(ENCRYPTION_KEY);
        	if (encryptionKey == null) {
        		encryptionKey = DEFAULT_ENCRYPTION_KEY;
        	}
        }

        Map<String, ConnectionConfigData> connectionMap = ConfigLoader.getConfigData(configLocation, encryptionKey);
        if(connectionMap.size() < 1) {
        	throw new IdMUnitException("No profiles in configuration");
        }
        
        FileInputStream is = new FileInputStream(workbookFileName);
        try {
            Workbook workbook = Workbook.getWorkbook(is);

            if (workbook == null) {
            	throw new IdMUnitException("Error loading workbook '" + workbookFileName + "'");
            }

            return parseSheet(workbook, sheetName, connectionMap, applySubstToOperations, applySubstToAttrData, multiValueDelimiter);
        } catch (BiffException e) {
        	throw new IdMUnitException("Error parsing workbook '" + workbookFileName + "'", e);
        } finally {
        	is.close();
        }
    }

    private static Test parseSheet(Workbook workbook, String sheetName, Map<String, ConnectionConfigData> connectionMap, boolean applySubstToOperations, boolean applySubstToAttrData, String multiValueDelimiter) throws IdMUnitException {
        TestSuite suite = new TestSuite(sheetName);
        Sheet sheet = workbook.getSheet(sheetName);

        if (sheet == null) {
        	throw new IdMUnitException("No sheet found with the name '" + sheetName + "'");
        }

        // Count header and data rows
        Cell[] column = sheet.getColumn(0);

        int[] delims = getSections(column);
        if (delims.length < 3) {
        	throw new IdMUnitException("Missing delimiter \"'--\" in column A.");
        }
        final int headerFirst = delims[0] + 1;
        final int headerLast = delims[1] - 1;
        final int dataFirst = delims[1] + 1;
        final int dataLast = delims[2] - 1;

        Map<String, List<Header>> headers = parseHeaders(sheet, headerFirst, headerLast);

        for (int row = dataFirst; row <= dataLast; ++row) {
            IdMUnitTestCase test = new IdMUnitTestCase(sheet.getName() + "[Excel row " + (row+1) + "]");
            
            Map<String, String> operationDataMap = new HashMap<String, String>();

            List<Header> operationHeaders = headers.get(OPERATION_HEADERS);

            boolean blankRowFlag = true;

            for (Header header : operationHeaders) {
            	Cell cell = sheet.getCell(header.column, row);
                if (cell == null || cell.getType() == CellType.EMPTY) {
                    continue;
                }

                blankRowFlag = false;

                operationDataMap.put(header.name, getStringRepresentation(cell));
            }
 
    		if (blankRowFlag) {
    			throw new IdMUnitException("Blank rows are not allowed - Please remove " + test.getName() + " and try again.");
    		}

            String target = operationDataMap.get(STR_TARGET);
            ConnectionConfigData connection = null;
            if (target != null) {
                connection = connectionMap.get(target);
                if (connection == null) {
                	throw new IdMUnitException("No configuration defined for target '" + target + "'");
                }

                test.setConfig(connection);
            }

            if (connection != null && applySubstToOperations) {
                applySubstitutionsToExcelData(connection.getSubstitutions(), operationDataMap);
            }

            setOperationData(test, operationDataMap);

            List<Header> dataHeaders = headers.get(target);
            if (dataHeaders == null) {
            	dataHeaders = headers.get(DEFAULT_HEADERS);
            }

            Map<String, Collection<String>> attrMap = new LinkedHashMap<String, Collection<String>>();
            for (Header header : dataHeaders) {
            	Cell cell = sheet.getCell(header.column, row);
                if (cell == null || cell.getType() == CellType.EMPTY) {
                    continue;
                }

                attrMap.put(header.name, parseMultiValueAttr(getStringRepresentation(cell), multiValueDelimiter));
            }

            if(connection != null) {
                if(applySubstToAttrData) {
                	applySubstitutionsToAttrData(connection.getSubstitutions(), attrMap);
                }

                processInjections(connection.getDataInjections(), attrMap);
            }
            
            test.setAttributeMap(attrMap);

            suite.addTest(test);
        }

        return new ConnectorManager(suite);
    }
    
    private static Collection<String> parseMultiValueAttr(String attrVal, String multiValueDelimiter) {
        if (attrVal.indexOf(multiValueDelimiter) == -1) {
            return Arrays.asList(new String[] {attrVal});
        } else {
            List<String> valueList = new ArrayList<String>();
            StringTokenizer tokenizer = new StringTokenizer(attrVal, multiValueDelimiter);
            while(tokenizer.hasMoreTokens()) {
                valueList.add(tokenizer.nextToken().trim());
            }
            return valueList;
        }
    }
    
    private static void setOperationData(IdMUnitTestCase test, Map<String, String> operationData) throws IdMUnitException {
		String selectedOperation = operationData.get("Operation");
		if (selectedOperation == null || selectedOperation.length() < 1) {
			throw new IdMUnitException("Missing operation - Please check the 'operation' column for this row (" + test.getName() + ") and try again.");
		}
		test.setOperation(selectedOperation);

		String comment = operationData.get("Comment");
		if (comment != null) {
			test.setComment(comment);
		}

		String disabled = operationData.get("DisableStep");
		if (disabled != null && Boolean.parseBoolean(disabled)) {
			test.setDisabled(true);
		}

		String expectFailure = operationData.get("ExpectFailure");
		if (expectFailure != null && Boolean.parseBoolean(expectFailure)) {
			test.setFailureExpected(true);
		}

		String critical = operationData.get("IsCritical");
		if (critical != null && Boolean.parseBoolean(critical)) {
			test.setCritical(true);
		}

		String repeatRange = operationData.get("RepeatOpRange");
		if (repeatRange != null) {
			test.setRepeatRange(repeatRange);
		}

        String retryCount = operationData.get("RetryCount");
        if (retryCount != null) {
    		try {
    			test.setRetryCount(Long.parseLong(retryCount.trim()));
    		} catch (NumberFormatException e) {
//    			LOG.error("Failed to parse " + STR_RETRY_COUNT + " into a value of type long.");
    		}
        }

        String waitInterval = operationData.get("WaitInterval");
        if (waitInterval != null) {
    		try {
    			test.setWaitInterval(Long.parseLong(waitInterval.trim()));
    		} catch (NumberFormatException e) {
//				LOG.error("Failed to parse " + STR_WAIT_INTERVAL + " into a value of type long: " + e.getMessage() + ": delayVal: [" + delayVal + "]");
    		}
        }
    }

	private static Map<String, List<Header>> parseHeaders(Sheet sheet, final int headerFirst, final int headerLast) throws IdMUnitException {
	    final String STR_METADATA_DELIM = "//";

		Map<String, List<Header>> headers = new HashMap<String, List<Header>>();
		Cell[] defaultHeaderRow = sheet.getRow(headerFirst);
		List<Header> opHeaders = new ArrayList<Header>();
		List<Header> defaultHeaders = new ArrayList<Header>();
    	for (int j=0; j<defaultHeaderRow.length; ++j) {
            if (defaultHeaderRow[j] == null)
                continue;

            CellType type = defaultHeaderRow[j].getType();

            if (type == CellType.LABEL || type == CellType.STRING_FORMULA) {
            	String headerName = defaultHeaderRow[j].getContents();
				if (headerName.startsWith(STR_METADATA_DELIM)) {
					opHeaders.add(new Header(headerName.substring(STR_METADATA_DELIM.length()), defaultHeaderRow[j].getColumn()));
				} else {
					defaultHeaders.add(new Header(headerName, defaultHeaderRow[j].getColumn()));
				}
            } else if (type == CellType.EMPTY) {
            	continue;
            } else {
                throw new IdMUnitException("Bad cell type for a header: "
                        + CellReferenceHelper.getCellReference(defaultHeaderRow[j].getColumn(), defaultHeaderRow[j].getRow()));
            }
    	}

    	headers.put(OPERATION_HEADERS, opHeaders);
    	headers.put(DEFAULT_HEADERS, defaultHeaders);

    	int targetColumn = -1;
    	for (Header header : opHeaders) {
    		if (header.name.equals(STR_TARGET)) {
        		targetColumn = header.column;
        		break;
    		}
    	}
    	
    	if (targetColumn == -1) {
    		throw new IdMUnitException("No operation header for 'target'");
    	}

        for (int i = headerFirst + 1; i <= headerLast; i++) {
        	String target = null;
        	List<Header> targetHeaders = new ArrayList<Header>();
        	Cell[] headerRow = sheet.getRow(i);
        	for (int j=0; j<headerRow.length; ++j) {
                if (headerRow[j] == null)
                    continue;

                CellType type = headerRow[j].getType();

                if (type == CellType.LABEL || type == CellType.STRING_FORMULA) {
                	if (headerRow[j].getColumn() == targetColumn) {
                		target = headerRow[j].getContents();
                	} else {
                		targetHeaders.add(new Header(headerRow[j].getContents(), headerRow[j].getColumn()));
                	}
                } else if (type == CellType.EMPTY) {
                	continue;
                } else {
                    throw new IdMUnitException("Bad cell type for a header: "
                            + CellReferenceHelper.getCellReference(headerRow[j].getColumn(), headerRow[j].getRow()));
                }
        	}
        	headers.put(target, targetHeaders);
        }
		return headers;
	}

    private static int[] getSections(Cell[] column) {
        final String DELIMITER = "---";

        List<Cell> delims = new ArrayList<Cell>(3);
        for (int j = 0; j < column.length; j++) {
            Cell cell = column[j];
            if (DELIMITER.equals(cell.getContents())) {
            	delims.add(cell);
            }
        }

        int[] delimRows = new int[delims.size()];
        int rowIndex = 0;
        for (Cell row : delims) {
        	delimRows[rowIndex++] = row.getRow();
        }
        
        return delimRows;
    }

    private static String getStringRepresentation(Cell cell) throws IdMUnitException {
        CellType type = cell.getType();

        if (type == CellType.ERROR || type == CellType.FORMULA_ERROR) {
            throw new IdMUnitException("Error in cell "
                    + CellReferenceHelper.getCellReference(cell.getColumn(), cell.getRow()));
        } else if (type == CellType.LABEL || type == CellType.STRING_FORMULA) {
        	return cell.getContents();
        } else if (type == CellType.BOOLEAN || type == CellType.BOOLEAN_FORMULA) {
            return Boolean.toString(((BooleanCell)cell).getValue());
        } else if (type == CellType.NUMBER || type == CellType.NUMBER_FORMULA) {
            return Integer.toString(Double.valueOf(((NumberCell)cell).getValue()).intValue());
        } else if (type == CellType.DATE || type == CellType.DATE_FORMULA) {
            return ((DateCell)cell).getDate().toString();
        } else {
            throw new IdMUnitException("Unknown data in cell "
                    + CellReferenceHelper.getCellReference(cell.getColumn(), cell.getRow()));
        }
    }

    private static void applySubstitutionsToExcelData(Map<String, String> substitutions, Map<String, String> dataAttrs) {
        if(substitutions == null) {
        	return;
        }

        Iterator<String> subItr = substitutions.keySet().iterator();
        while(subItr.hasNext()) {
            String replaceVal = subItr.next();
            String newVal = (String)substitutions.get(replaceVal);
            replaceValues(dataAttrs, replaceVal, newVal);
        }
    }

    private static void applySubstitutionsToAttrData(Map<String, String> substitutions, Map<String, Collection<String>> dataAttrs) {
        if(substitutions == null) {
        	return;
        }

        for (String replaceVal : substitutions.keySet()) {
            String newVal = substitutions.get(replaceVal);
            replaceAttrValues(dataAttrs, replaceVal, newVal);
        }
    }

    private static void processInjections(List<InjectionConfigData> injections, Map<String, Collection<String>> dataAttrs) throws IdMUnitException {
        if(injections != null && injections.size() > 0) {
			Iterator<InjectionConfigData> injectionItr = injections.iterator();
			while(injectionItr.hasNext()) {
			    InjectionConfigData dataInjectionConfigData = injectionItr.next();
			    String dynamicInjectionDataValue = getInjectorValueString(dataInjectionConfigData.getType()
			            , dataInjectionConfigData.getMutator()
			            , dataInjectionConfigData.getFormat());
			    replaceAttrValues(dataAttrs, dataInjectionConfigData.getKey(), dynamicInjectionDataValue);
			}
        }
    }
    
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
        return dynamicInjectionDataValue;
    }

	private static void replaceValues(Map<String, String> dataAttrs, String replaceVal, String newVal) {
		Iterator<String> dataItr = dataAttrs.keySet().iterator();
		while(dataItr.hasNext()) {
		    String attrName = dataItr.next();
		    if(attrName.equals(XML_CONNECTIONS)) continue; //substituions are not supported on the connection name as this is the key for the connection config data
		    String attrVal = (String)dataAttrs.get(attrName);
			String updatedAttrVal = attrVal;
			Matcher matcher = Pattern.compile(replaceVal, Pattern.CASE_INSENSITIVE).matcher(attrVal); 
	        if(matcher.find()) {
	        	updatedAttrVal = matcher.replaceAll(newVal);
	        }
		    if(!attrVal.equals(updatedAttrVal)) {
		        dataAttrs.put(attrName, updatedAttrVal);
		    }
		}
	}

	private static void replaceAttrValues(Map<String, Collection<String>> dataAttrs, String replaceVal, String newVal) {
		for (String attrName : dataAttrs.keySet()) {
			List<String> newValues = new ArrayList<String>();
			for (String attrVal : dataAttrs.get(attrName)) {
				String updatedAttrVal = attrVal;
				Matcher matcher = Pattern.compile(replaceVal, Pattern.CASE_INSENSITIVE).matcher(attrVal); 
		        if(matcher.find()) {
		        	updatedAttrVal = matcher.replaceAll(newVal);
		        }
		        newValues.add(updatedAttrVal);
			}
			dataAttrs.put(attrName, newValues);
		}
	}

	//TODO: This should be separated from the excel parser eventually.
	private static Properties loadProperties() throws IOException {
		Properties properties = System.getProperties();
        InputStream propertiesFile = ExcelParser.class.getClassLoader().getResourceAsStream("idmunit-defaults.properties");
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

    private static class Header {
    	private final String name;
    	private final int column;
    	Header(String name, int column) {
    		this.name = name;
    		this.column = column;
    	}
    }
}
