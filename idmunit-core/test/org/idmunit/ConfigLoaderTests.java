package org.idmunit;

import java.util.Map;

import org.idmunit.connector.ConnectionConfigData;

import junit.framework.TestCase;

public class ConfigLoaderTests extends TestCase {
	public void testGetConfigData() throws IdMUnitException {
		Map<String, ConnectionConfigData> config = ConfigLoader.getConfigData("./examples/profiles/idmunit-config.xml", "IDMUNIT1");
		ConnectionConfigData idv = config.get("IDV");
		assertEquals("172.17.2.237:636", idv.getServerURL());
		assertEquals("cn=admin,o=services", idv.getAdminCtx());
		assertEquals("trivir", idv.getAdminPwd());
		assertEquals("", idv.getKeystorePath());

		assertEquals("IDV", idv.getName());
		assertEquals("org.idmunit.connector.LDAP", idv.getType());
		assertEquals("Connector for the identity vault", idv.getParam("description"));
		assertEquals("172.17.2.237:636", idv.getParam("server"));
		assertEquals("cn=admin,o=services", idv.getParam("user"));
		assertEquals("trivir", idv.getParam("password"));
		assertEquals("trivir", idv.getParam("encryptedFieldExample"));
		assertEquals("", idv.getParam("keystore-path"));
		assertEquals(0, idv.getMultiplierRetry());
		assertEquals(0, idv.getMultiplierWait());
		assertEquals(2, idv.getSubstitutions().size());
		assertEquals("172.17.2.131", idv.getSubstitutions().get("%SMTP-IP-SYSTEM1%"));
		assertEquals("172.17.2.132", idv.getSubstitutions().get("%SMTP-IP-SYSTEM2%"));
		assertEquals(2, idv.getDataInjections().size());
		assertEquals("%TODAY%", idv.getDataInjections().get(0).getKey());
		assertEquals("org.idmunit.injector.DateInjection", idv.getDataInjections().get(0).getType());
		assertEquals("yyyyMMdd", idv.getDataInjections().get(0).getFormat());
		assertEquals(null, idv.getDataInjections().get(0).getMutator());
		assertEquals("%TODAY+30%", idv.getDataInjections().get(1).getKey());
		assertEquals("org.idmunit.injector.DateInjection", idv.getDataInjections().get(1).getType());
		assertEquals("yyyyMMdd", idv.getDataInjections().get(1).getFormat());
		assertEquals("30", idv.getDataInjections().get(1).getMutator());
		assertEquals(1, idv.getIdmunitAlerts().size());
		assertEquals("Alert", idv.getIdmunitAlerts().get("idmunitAlert_Alert").getName());
		assertEquals("Email recipients will be notified if a test marked as \"Critical\" fails", idv.getIdmunitAlerts().get("idmunitAlert_Alert").getDescription());
		assertEquals("smtp.EXAMPLE.com", idv.getIdmunitAlerts().get("idmunitAlert_Alert").getSmtpServer());
		assertEquals("idmunitAlerts@example.org", idv.getIdmunitAlerts().get("idmunitAlert_Alert").getAlertSender());
		assertEquals("idmunitAlerts@example.org", idv.getIdmunitAlerts().get("idmunitAlert_Alert").getAlertReipient());
		assertEquals("IdMUnit Test Failed: ", idv.getIdmunitAlerts().get("idmunitAlert_Alert").getSubjectPrefix());
		assertEquals("c:/idmunitAlerts.log", idv.getIdmunitAlerts().get("idmunitAlert_Alert").getLogPath());

		ConnectionConfigData ad = (ConnectionConfigData)config.get("AD");
		assertEquals("AD", ad.getName());
		assertEquals("org.idmunit.connector.LDAP", ad.getType());
		assertEquals("Connector for Active Directory", ad.getParam("description"));
		assertEquals("172.17.2.173:636", ad.getParam("server"));
		assertEquals("CN=Administrator,CN=Users,DC=EXAMPLE,DC=ORG", ad.getParam("user"));
		assertEquals("trivir", ad.getParam("password"));
		assertEquals("", ad.getParam("keystore-path"));
		assertEquals(0, ad.getMultiplierRetry());
		assertEquals(0, ad.getMultiplierWait());
		assertEquals(1, ad.getSubstitutions().size());
		assertEquals("DC=EXAMPLE,DC=ORG", ad.getSubstitutions().get("%AD_DOMAIN%"));
		assertEquals(2, ad.getDataInjections().size());
		assertEquals("%TODAY%", ad.getDataInjections().get(0).getKey());
		assertEquals("org.idmunit.injector.DateInjection", ad.getDataInjections().get(0).getType());
		assertEquals("yyyyMMdd", ad.getDataInjections().get(0).getFormat());
		assertEquals(null, ad.getDataInjections().get(0).getMutator());
		assertEquals("%TODAY+30%", ad.getDataInjections().get(1).getKey());
		assertEquals("org.idmunit.injector.DateInjection", ad.getDataInjections().get(1).getType());
		assertEquals("yyyyMMdd", ad.getDataInjections().get(1).getFormat());
		assertEquals("30", ad.getDataInjections().get(1).getMutator());
		assertEquals(1, ad.getIdmunitAlerts().size());
		assertEquals("Alert", ad.getIdmunitAlerts().get("idmunitAlert_Alert").getName());
		assertEquals("Email recipients will be notified if a test marked as \"Critical\" fails", idv.getIdmunitAlerts().get("idmunitAlert_Alert").getDescription());
		assertEquals("smtp.EXAMPLE.com", idv.getIdmunitAlerts().get("idmunitAlert_Alert").getSmtpServer());
		assertEquals("idmunitAlerts@example.org", idv.getIdmunitAlerts().get("idmunitAlert_Alert").getAlertSender());
		assertEquals("idmunitAlerts@example.org", idv.getIdmunitAlerts().get("idmunitAlert_Alert").getAlertReipient());
		assertEquals("IdMUnit Test Failed: ", idv.getIdmunitAlerts().get("idmunitAlert_Alert").getSubjectPrefix());
		assertEquals("c:/idmunitAlerts.log", idv.getIdmunitAlerts().get("idmunitAlert_Alert").getLogPath());

//		ConnectionConfigData orcl = (ConnectionConfigData)config.get("ORCL");
//		assertEquals("ORCL", orcl.getName());
//		assertEquals("com.trivir.idmunit.connector.Oracle", orcl.getType());
//		assertEquals("Connector to an Remedy database on an Oracle server", orcl.getParam("description"));
//		assertEquals("jdbc:oracle:thin:@192.168.1.119:1526:REMEDY01", orcl.getParam("server"));
//		assertEquals("idmunit", orcl.getParam("user"));
//		assertEquals("trivir", orcl.getParam("password"));
//		assertEquals("", orcl.getParam("keystore-path"));
//		assertEquals(0, orcl.getMultiplierRetry());
//		assertEquals(0, orcl.getMultiplierWait());
//		assertEquals(null, orcl.getSubstitutions());
//		assertEquals(null, orcl.getDataInjections());
//		assertEquals(1, orcl.getIdmunitAlerts().size());
//		assertEquals("TriVir", orcl.getIdmunitAlerts().get("idmunitAlert_TriVir").getName());
//		assertEquals("TriVir personnel will be notified if a test marked as \"Critical\" fails", orcl.getIdmunitAlerts().get("idmunitAlert_TriVir").getDescription());
//		assertEquals("smtp.MYSERVER.com", orcl.getIdmunitAlerts().get("idmunitAlert_TriVir").getSmtpServer());
//		assertEquals("idmunitAlerts@idmunit.org", orcl.getIdmunitAlerts().get("idmunitAlert_TriVir").getAlertSender());
//		assertEquals("bkynaston@trivir.com", orcl.getIdmunitAlerts().get("idmunitAlert_TriVir").getAlertReipient());
//		assertEquals("IdMUnit Test Failed: ", orcl.getIdmunitAlerts().get("idmunitAlert_TriVir").getSubjectPrefix());
//		assertEquals("c:/idmunitAlerts.log", orcl.getIdmunitAlerts().get("idmunitAlert_TriVir").getLogPath());
//
//		ConnectionConfigData dtf = (ConnectionConfigData)config.get("DTF");
//		assertEquals("DTF", dtf.getName());
//		assertEquals("org.idmunit.connector.DTF", dtf.getType());
//		assertEquals("Connector to TriVirDTF data feed - must  map drive/share or UNC to IDM server or remote loader running the DTF driver", dtf.getParam("description"));
//		assertEquals("DriverInputFilePath=x:/input/inputFile.csv|DriverOutputFilePath=x:/output/outputFile.csv|delimiter=$", dtf.getParam("server"));
//		assertEquals("", dtf.getParam("user"));
//		assertEquals("", dtf.getParam("password"));
//		assertEquals("", dtf.getParam("keystore-path"));
//		assertEquals(0, dtf.getMultiplierRetry());
//		assertEquals(0, dtf.getMultiplierWait());
//		assertEquals(3, dtf.getSubstitutions().size());
//		assertEquals("333-333-3333", dtf.getSubstitutions().get("X3"));
//		assertEquals("222-222-2222", dtf.getSubstitutions().get("X2"));
//		assertEquals("111-111-1111", dtf.getSubstitutions().get("X1"));
//		assertEquals(null, dtf.getDataInjections());
//		assertEquals(1, dtf.getIdmunitAlerts().size());
//		assertEquals("TriVir", dtf.getIdmunitAlerts().get("idmunitAlert_TriVir").getName());
//		assertEquals("TriVir personnel will be notified if a test marked as \"Critical\" fails", dtf.getIdmunitAlerts().get("idmunitAlert_TriVir").getDescription());
//		assertEquals("smtp.MYSERVER.com", dtf.getIdmunitAlerts().get("idmunitAlert_TriVir").getSmtpServer());
//		assertEquals("idmunitAlerts@idmunit.org", dtf.getIdmunitAlerts().get("idmunitAlert_TriVir").getAlertSender());
//		assertEquals("bkynaston@trivir.com", dtf.getIdmunitAlerts().get("idmunitAlert_TriVir").getAlertReipient());
//		assertEquals("IdMUnit Test Failed: ", dtf.getIdmunitAlerts().get("idmunitAlert_TriVir").getSubjectPrefix());
//		assertEquals("c:/idmunitAlerts.log", dtf.getIdmunitAlerts().get("idmunitAlert_TriVir").getLogPath());
	}
}
