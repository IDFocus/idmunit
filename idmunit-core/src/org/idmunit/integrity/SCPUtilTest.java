package org.idmunit.integrity;

import org.idmunit.extension.SCPUtil;

import junit.framework.TestCase;

public class SCPUtilTest extends TestCase {

	public void testSCPFile() {
		SCPUtil.scpSendFile("C:\\Program Files\\WinSCP3\\WinSCP3.exe ", "brent", "c:\\test\\script1.txt", "c:\\test\\test10.txt", "/home/brent/");
	}
}
