/* 
 * IdMUnit - Automated Testing Framework for Identity Management Solutions
 * Copyright (c) 2005-2009 TriVir, LLC
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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.idmunit.IdMUnitException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * Implements an IdMUnit connector for Delimited Text File drivers
 * @author Brent Kynaston, Software Engineer, TriVir LLC
 * @version %I%, %G%
 * @see org.idmunit.connector.Connection
 */
public class DTF extends AbstractConnector {
    private static final String INPUT_FILE = "input-file";
    private static final String OUTPUT_FILE = "output-file";
    private static final String DELIM = "delimiter";
    private static final int DTF_BUFFER = 1000; //allocate up to this many bytes for the output to insert into the delimited text file

    private static Log log = LogFactory.getLog(DTF.class);
    protected String m_driverInputFilePath;
    protected String m_driverOutputFilePath;
    protected String m_fileDeletePrefix;
    protected String m_delim;

    /*
     *     <connection>
     *         <name>DTF</name>
     *         <description>Connector to output of DTF driver</description>
     *         <type>com.trivir.idmunit.connector.DTF</type>
     *         <input-file>x:/input/inputFile.csv</input-file>
     *         <output-file>x:/output/outputFile.csv</output-file>
     *         <delimiter>,</delimiter>
     *         <multiplier/>
     *         <substitutions/>
     *         <data-injections/>
     *     </connection>
     */
    public void setup(Map<String, String> config) throws IdMUnitException {
        if (config.get(OUTPUT_FILE) == null) {
            throw new IdMUnitException("'" + OUTPUT_FILE + "' not configured");
        }

        m_driverInputFilePath = getFilePathAndTimeStamp(config.get(OUTPUT_FILE));
        if (!new File(m_driverInputFilePath).exists()) {
            throw new IdMUnitException("'" + OUTPUT_FILE + "' (" + config.get(OUTPUT_FILE) + ") does not exist");
        }

        //Setup name to clean up files after testing with delObject
        m_fileDeletePrefix = config.get(OUTPUT_FILE).substring(0, config.get(OUTPUT_FILE).length()-".csv".length());
        if (m_fileDeletePrefix.length() < 1) {
            throw new IdMUnitException("'" + OUTPUT_FILE + "' (" + config.get(OUTPUT_FILE) + ") is less than 4 characters long.");
        }

        if (config.get(INPUT_FILE) == null) {
            throw new IdMUnitException("'" + INPUT_FILE + "' not configured");
        }
        m_driverOutputFilePath = getFilePathAndTimeStamp(config.get(INPUT_FILE));
        if (!new File(m_driverOutputFilePath).exists()) {
            throw new IdMUnitException("'" + INPUT_FILE + "' (" + config.get(INPUT_FILE) + ") does not exist");
        }

        m_delim = config.get(DELIM);
        if (m_delim == null) {
            throw new IdMUnitException("'" + DELIM + "' not configured");
        }

        log.info("##### Input File Path: " + m_driverInputFilePath);
        log.info("##### Output File Path: " + m_driverOutputFilePath);
        log.info("##### Field Delimiter: " + m_delim);
    }

	private String getFilePathAndTimeStamp(String targetPath) {
		int inputIdx = targetPath.indexOf('=');
		targetPath = targetPath.substring(inputIdx+1);
		//Add timestamp to file
		//String pathWithTimeStamp = targetPath.substring(0, targetPath.length()-4) + getCurrentTimeStamp() + targetPath.substring(targetPath.length()-4);

		return targetPath.substring(0, targetPath.length()-".csv".length()) + getCurrentTimeStamp() + targetPath.substring(targetPath.length()-".csv".length());
	}

	private String buildFileData(Map<String, Collection<String>> data) {
		Set<String> keySet = data.keySet();
		StringBuffer fileData = new StringBuffer(DTF_BUFFER);
		for (Iterator<String> iter = keySet.iterator(); iter.hasNext();) {
			String attrName = iter.next();
			Collection<String> attrVal = data.get(attrName);
			//Append the data to the data entry here
			for (Iterator<String> iter2 = attrVal.iterator(); iter2.hasNext();) {
				fileData.append(iter2.next());
			}
			fileData.append(m_delim);
		}
		return fileData.toString();
	}

	private static String getCurrentTimeStamp() {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(
				"yyyyMMddHHmmss");
		Date timestamp = new Date();
		return dateFormatter.format(timestamp);
	}

    public void opAddObject(Map<String, Collection<String>> data) throws IdMUnitException {
        log.warn("The operation AddObject is deprecated. Please use the Add operation instead.");
        opAdd(data);
    }

	public void opAdd(Map<String, Collection<String>> data) throws IdMUnitException {

		String fileData = buildFileData(data);
		log.info("...inserting delimited text file entry: ");
		log.info(fileData);
		BufferedWriter outputFile = null;
		try {
			//TODO: add time stamp to the file name to differentiate between test executions
			//SimpleDateFormat dateFormatter = new SimpleDateFormat(Constants.DATE_FORMAT);
//			Date timestamp = new Date();
//			outputFile.write(dateFormatter.format(timestamp) + " " + logData);

			//Write the data entry to the DTF file (note that once this file has a single row it may be picked up by IDM and processed)
			outputFile = new BufferedWriter(new FileWriter(m_driverInputFilePath, false));
			outputFile.write(fileData);
			outputFile.newLine();
			outputFile.flush();
			outputFile.close();
		} catch (IOException e) {
			throw new IdMUnitException("...Failed to write to the log file: " + m_driverInputFilePath + " Error: " + e.getMessage());
		}
		log.info("...SUCCESS");
	}	

    public void opDeleteObject(Map<String, Collection<String>> data) throws IdMUnitException {
        log.warn("The operation DeleteObject is deprecated. Please use the Delete operation instead.");
        opDelete(data);
    }

	public void opDelete(Map<String, Collection<String>> data) throws IdMUnitException {
		//Find and delete IdMUnit-generated DTF files
		int lastSlashIndex = m_fileDeletePrefix.lastIndexOf("/");
		String dtfPath = m_fileDeletePrefix.substring(0,lastSlashIndex);
		String inputFileName = m_fileDeletePrefix.substring(lastSlashIndex+1);

		File deleteTargets = new File(dtfPath);
	    String[] potentialFileTargets = deleteTargets.list();
	    if(potentialFileTargets == null){
	    	throw new IdMUnitException("...Failed to delete temp DTF files: [" + m_fileDeletePrefix + "] please clean up manually after testing.");
	    }else{
			for(int i = 0; i < potentialFileTargets.length; i++){
				String fileName = potentialFileTargets[i];
				if(fileName.indexOf(inputFileName)!=-1) {
					File deleteFile = new File(dtfPath + "/" + fileName);
					if(deleteFile.delete()) {
						log.info("...deleted DTF file: " + fileName);
					} else {
						log.info("...unable to delete DTF file: " + fileName);
					}
				}
			}
		}
	}
}