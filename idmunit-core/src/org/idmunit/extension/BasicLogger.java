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
package org.idmunit.extension;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Implements simple synchronous single-threaded logging mechanism that requires no additional configuration or dependencies
 * @author Brent Kynaston, Software Engineer, TriVir LLC
 * @version %I%, %G%
 */
public class BasicLogger {
	private static String getCurrentTimeStamp() {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(
                "yyyyMMdd HH:mm:ss");
		Date timestamp = new Date();
		return dateFormatter.format(timestamp);
	}

	/**
	 * Writes a log message to standard out
	 * @param logData Message to log
	 */
	public static void consoleLog(String logData) {
		System.out.println(getCurrentTimeStamp() + " -- " + logData);
	}

	/**
	 * Writes a timestamp + logmessage to a file in the specified path
	 * @param logData Log message
	 * @param fileName Full filesystem path of the log file. New entries will be appended to existing log files
	 */
	public static void fileLog(String logData, String fileName){
		// TODO: Roll the log file after configured size!
		consoleLog(logData);
		BufferedWriter outputFile = null;
		try {
			if (fileName == null || fileName.length() < 1) {
				BasicLogger.consoleLog("### Failure: please specify log file path in the configuration properties ###");
			}
			outputFile = new BufferedWriter(new FileWriter(fileName, true));
			outputFile.write(getCurrentTimeStamp() + " -- " + logData);
			outputFile.newLine();
			outputFile.flush();
			outputFile.close();
		} catch (IOException e) {
			consoleLog("...Failed to write to the log file: " + fileName
					+ " Error: " + e.getMessage());
		}
	}

	/**
	 * Writes logmessage to a file in the specified path
	 * @param logData Log message
	 * @param fileName Full filesystem path of the log file. New entries will be appended to existing log files
	 */
	public static void appendData(String logData, String fileName){
		consoleLog(logData);
		BufferedWriter outputFile = null;
		try {
			if (fileName == null || fileName.length() < 1) {
				BasicLogger.consoleLog("### Failure: please specify log file path in the configuration properties ###");
			}
			outputFile = new BufferedWriter(new FileWriter(fileName, true));
			outputFile.write(logData);
			outputFile.newLine();
			outputFile.flush();
			outputFile.close();
		} catch (IOException e) {
			consoleLog("...Failed to write to the log file: " + fileName
					+ " Error: " + e.getMessage());
		}
	}
	
	/**
	 * Writes logmessage to a file in the specified path
	 * @param logData Log message
	 * @param fileName Full filesystem path of the log file. New entries will be appended to existing log files
	 */
	public static void removeFile(String fileName){
		try {
			if (fileName == null || fileName.length() < 1) {
				BasicLogger.consoleLog("### Failure: please specify log file path and name in the configuration properties ###");
			}
			File targetFile = new File(fileName);
			targetFile.delete();
		} catch (SecurityException e) {
			consoleLog("...Failed to write to the log file: " + fileName
					+ " Error: " + e.getMessage());
		}
	}
}
