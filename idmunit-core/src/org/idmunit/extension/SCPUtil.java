/* 
 * IdMUnit - Automated Testing Framework for Identity Management Solutions
 * Copyright (c) 2005-2007 TriVir, LLC
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

/**
 * Implements SCP functionality to push files to UNIX IDM servers (created for use with the DTF connector)
 * @author Brent Kynaston, Software Engineer, TriVir LLC
 * @version %I%, %G%
 */
public class SCPUtil {
	public static void scpSendFile(String winSCPExecutable, String winSCPProfileID, String localSCPScriptFile, String localTargetFile, String destinationPath) {
		System.out.println("Executing push of DTF File to server profile: " + winSCPProfileID);
		generateSCPScriptFile(localSCPScriptFile, localTargetFile, destinationPath);
		
		executeCmdLine(winSCPExecutable + winSCPProfileID + " /script=" + localSCPScriptFile);
		
	}

	private static void executeCmdLine(String command) {
		try {
			Process cmdLineProcess = Runtime.getRuntime().exec(command);
			cmdLineProcess.waitFor();
			System.out.println("Exit value: " + cmdLineProcess.exitValue());
		} catch (Exception e) {
			System.out.println("### Failed to execute cmd-line statement with error: [ " + e.getMessage() + " ###");
		}		
	}

	private static void generateSCPScriptFile(String fileName, String localFile, String remotePath) {
		BasicLogger.removeFile(fileName);
		BasicLogger.appendData("option batch on", fileName);
		BasicLogger.appendData("option confirm off", fileName);
		BasicLogger.appendData("put " + localFile + " " + remotePath, fileName);
		BasicLogger.appendData("close", fileName);
		BasicLogger.appendData("exit", fileName);
	}
}
