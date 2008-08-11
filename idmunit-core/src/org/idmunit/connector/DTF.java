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
package org.idmunit.connector;

import javax.naming.directory.Attributes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ddsteps.dataset.DataRow;
import org.ddsteps.dataset.DataValue;
import org.ddsteps.testcase.support.DDStepsExcelTestCase;
import org.idmunit.IdMUnitException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;


/**
 * Implements an IdMUnit connector for Delimited Text File drivers
 * @author Brent Kynaston, Software Engineer, TriVir LLC
 * @version %I%, %G%
 * @see org.idmunit.connector.Connection
 */
public class DTF extends DDStepsExcelTestCase implements org.idmunit.connector.Connection {
    private final static String ERROR_DTF_CONFIG = "Configuration error - the DTF connector serverUrl should contain inputFilePath=VALUE1|outputFilePath=VALUE2 where value1 and value2 provide the correct path with full filename (including extension).";
    private final static String STR_SUCCESS = "...SUCCESS";
    private final static String STR_DN = "dn";
    private final static int DTF_BUFFER = 1000; //allocate up to this many bytes for the output to insert into the delimited text file

	private static Log log = LogFactory.getLog(Oracle.class);
	private String m_driverInputFilePath;
	private String m_driverOutputFilePath;
	private String m_fileDeletePrefix;
	private String m_delim;
	
	public DTF() {}
	
	public DTF(ConnectionConfigData creds) throws IdMUnitException {
		setupConnection(creds);
	}
	
	public void setupConnection(ConnectionConfigData creds) throws IdMUnitException {
		getConnection(creds.getServerURL(), creds.getAdminCtx(), creds.getAdminPwd());
	}
	
	private String getFilePath(String targetPath) {
		int inputIdx = targetPath.indexOf('=');
		targetPath = targetPath.substring(inputIdx+1);
		//Add timestamp to file
		//String pathWithTimeStamp = targetPath.substring(0, targetPath.length()-4) + getCurrentTimeStamp() + targetPath.substring(targetPath.length()-4);
		
		return targetPath;
	}

	private String getFilePathAndTimeStamp(String targetPath) {
		int inputIdx = targetPath.indexOf('=');
		targetPath = targetPath.substring(inputIdx+1);
		//Add timestamp to file
		//String pathWithTimeStamp = targetPath.substring(0, targetPath.length()-4) + getCurrentTimeStamp() + targetPath.substring(targetPath.length()-4);
		
		return targetPath.substring(0, targetPath.length()-4) + getCurrentTimeStamp() + targetPath.substring(targetPath.length()-4);
	}

	public void getConnection(String serverUrl, String user, String password) throws IdMUnitException {
		//Provide limited validation of the format for the config: inputFilePath=VALUE|outputFilePath=VALUE
		if(serverUrl == null || serverUrl.length() < 1 
			|| serverUrl.indexOf("|") == -1) {
			throw new IdMUnitException(ERROR_DTF_CONFIG);
		}
		
		//Parse out the input file path
		StringTokenizer tokenizer = new StringTokenizer(serverUrl, "|");
		String inputFilePath = tokenizer.nextToken();
		m_driverInputFilePath = getFilePath(inputFilePath);
		
		//Setup name to clean up files after testing with delObject
		m_fileDeletePrefix = m_driverInputFilePath.substring(0, m_driverInputFilePath.length()-4);

		//Add timestamp to path
		m_driverInputFilePath = getFilePathAndTimeStamp(inputFilePath);
		
		//Parse out the output file path and add timestamp
		String outputFilePath = tokenizer.nextToken();
		m_driverOutputFilePath = getFilePathAndTimeStamp(outputFilePath);
		
		//Parse out the delimiter for the DTF file
		String delimiter = tokenizer.nextToken();
		int delimIdx = delimiter.indexOf('=');
		m_delim = delimiter.substring(delimIdx+1);
		
		log.info("##### Input File Path: " + m_driverInputFilePath);
		log.info("##### Output File Path: " + m_driverOutputFilePath);
		log.info("##### Field Delimiter: " + m_delim);
	}
	
	public void insertObject(Attributes assertedAttrs) throws IdMUnitException {
		//TODO: implement
		log.info("### DTF connector insertObject verb not yet implemented ###");
	}
	
	public void closeConnection() throws IdMUnitException {
		//TODO: close file handles
	}

	public void validateObject(Attributes assertedAttrs) throws IdMUnitException {
		//TODO: implement
		log.info("### DTF connector validateObject verb not yet implemented ###");
	}

	private String buildFileData(DataRow assertedAttrs) {
		StringBuffer fileData = new StringBuffer(DTF_BUFFER);
		for (Iterator iter = assertedAttrs.iterator(); iter.hasNext();) {
			DataValue dataValue = (DataValue) iter.next();
			String attrName = dataValue.getName();
			Object attrVal = dataValue.getValue();
			if(attrName==null || attrVal==null || attrName.equalsIgnoreCase(STR_DN))
				continue;
			//Append the data to the data entry here
			fileData.append(attrVal);
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

	public void addObject(DataRow dataRow) throws IdMUnitException {
		String fileData = buildFileData(dataRow);
		log.info("...inserting delimited text file entry: ");
		log.info(fileData);
		BufferedWriter outputFile = null;
		try {
			if(m_driverInputFilePath==null || m_driverInputFilePath.length()<1) {
				throw new IdMUnitException(ERROR_DTF_CONFIG);
			}
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
		log.info(STR_SUCCESS);
	}

	public void deleteObject(Attributes assertedAttrs) throws IdMUnitException {
		//Find and delete IdMUnit-generated DTF files
		if(m_fileDeletePrefix==null || m_fileDeletePrefix.length()<1) throw new IdMUnitException(ERROR_DTF_CONFIG);  
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

	public void modObject(Attributes assertedAttrs, int operationType) throws IdMUnitException {
		// TODO Auto-generated method stub
		
	}

	public void moveObject(Attributes assertedAttrs) throws IdMUnitException {
		// TODO Auto-generated method stub
		
	}

	public void renameObject(Attributes assertedAttrs) throws IdMUnitException {
		// TODO Auto-generated method stub
		
	}

	public void validatePassword(Attributes assertedAttrs) throws IdMUnitException {
		// TODO Auto-generated method stub
		
	}
	public Map<String, String> search(String filter, String base, String[] collisionAttrs) throws IdMUnitException {
		// TODO Auto-generated method stub
		return null;
	}
}