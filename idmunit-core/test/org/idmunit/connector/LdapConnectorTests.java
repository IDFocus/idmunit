package org.idmunit.connector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.BasicConfigurator;
import org.idmunit.IdMUnitException;


import junit.framework.TestCase;

public class LdapConnectorTests extends TestCase {
    private static final String adServerHost = "172.17.2.173";
    private static final int adServerPort = 636;
    private static final String eDirServerHost = "172.17.2.173";
    private static final int eDirServerPort = 1636;
    private static final String keystore = "testkeystore";
     private static final String keyStorePassphrase = "changeit";
    private static final char SEP = File.separatorChar;
    private static final String jssecacerts = System.getProperty("java.home") + SEP + "lib" + SEP + "security" + SEP + "cacerts";

    private LdapConnector connector;
    static SSLContext context;
    
    @Override
    protected void setUp() throws Exception {
        BasicConfigurator.configure();
        connector = new LdapConnector();
    }

    @Override
    protected void tearDown() throws Exception {
        connector.tearDown();
    }

    public void testAddAttrOperation() throws IdMUnitException, Exception {

    	Map<String, String> config = new TreeMap<String, String>();
        config.put("server", eDirServerHost + ":" + eDirServerPort);
        config.put("user", "cn=admin,o=au");
        config.put("password", "trivir");
        config.put("keystore-path", keystore);
        
        writeCertificatesToKeyStore(keystore, keyStorePassphrase.toCharArray(), "test", eDirServerHost, eDirServerPort);
  
        try {
            connector.setup(config);
        } catch (IdMUnitException e) {
        	throw e;
        } finally {
            new File(keystore).delete();
        }

        Map<String, Collection<String>> attrs  = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList("CN=gmathis,OU=users,O=AU"));
        attrs.put("objectClass", Arrays.asList("user"));
        attrs.put("cn", Arrays.asList("gmathis"));
        attrs.put("givenName", Arrays.asList("Gordon"));
        attrs.put("sn", Arrays.asList("Mathis"));
        attrs.put("fullName", Arrays.asList("Gordon Mathis"));
        attrs.put("userPassword", Arrays.asList("trivir"));

        connector.opDeleteObject(attrs);

        connector.opAddObject(attrs);

        attrs  = new TreeMap<String, Collection<String>>();
        
        attrs.put("dn", Arrays.asList("CN=gmathis,OU=users,O=AU"));
        attrs.put("telephoneNumber", Arrays.asList("222-333-4444"));

        connector.opAddAttr(attrs);
  		
        attrs.put("cn", Arrays.asList("gmathis"));
        attrs.put("givenName", Arrays.asList("Gordon"));
        attrs.put("sn", Arrays.asList("Mathis"));
        attrs.put("fullName", Arrays.asList("Gordon Mathis"));
 
  		connector.opValidateObject(attrs);   
    }

    public void testRemoveAttrOperation() throws IdMUnitException, Exception {

    	Map<String, String> config = new TreeMap<String, String>();
        config.put("server", eDirServerHost + ":" + eDirServerPort);
        config.put("user", "cn=admin,o=au");
        config.put("password", "trivir");
        config.put("keystore-path", keystore);
        
        writeCertificatesToKeyStore(keystore, keyStorePassphrase.toCharArray(), "test", eDirServerHost, eDirServerPort);
  
        try {
            connector.setup(config);
        } catch (IdMUnitException e) {
        	throw e;
        } finally {
            new File(keystore).delete();
        }

        Map<String, Collection<String>> attrs  = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList("CN=gmathis,OU=users,O=AU"));
        attrs.put("objectClass", Arrays.asList("user"));
        attrs.put("cn", Arrays.asList("gmathis"));
        attrs.put("givenName", Arrays.asList("Gordon"));
        attrs.put("sn", Arrays.asList("Mathis"));
        attrs.put("fullName", Arrays.asList("Gordon Mathis"));
        attrs.put("telephoneNumber", Arrays.asList("222-333-4444"));
        attrs.put("userPassword", Arrays.asList("trivir"));

        connector.opDeleteObject(attrs);

        connector.opAddObject(attrs);

        attrs  = new TreeMap<String, Collection<String>>();
        
        attrs.put("dn", Arrays.asList("CN=gmathis,OU=users,O=AU"));
        attrs.put("telephoneNumber", Arrays.asList("222-333-4444"));

        connector.opRemoveAttr(attrs);

		attrs.remove("telephoneNumber");
  		
        attrs.put("cn", Arrays.asList("gmathis"));
        attrs.put("givenName", Arrays.asList("Gordon"));
        attrs.put("sn", Arrays.asList("Mathis"));
        attrs.put("fullName", Arrays.asList("Gordon Mathis"));
 
  		connector.opValidateObject(attrs);   
    }
    
    
    
    public void testExceptionChainOnConnection() throws IdMUnitException, Exception {
        Map<String, String> config = new TreeMap<String, String>();
        config.put("server", adServerHost + ":" + adServerPort);
        config.put("user", "CN=Administrator,CN=users,DC=american,DC=edu");
        config.put("password", "trivir");
  
        try {
            connector.setup(config);
            fail();
        } catch (IdMUnitException e) {
        	IdMUnitException exception = new IdMUnitException();
        	assertTrue (e.getClass() == exception.getClass());
		}
    } 

  
    public void testAllowUserToChangeTheirPassword() throws IdMUnitException, Exception {
        Map<String, String> config = new TreeMap<String, String>();
        config.put("server", adServerHost + ":" + adServerPort);
        config.put("user", "CN=Administrator,CN=users,DC=american,DC=edu");
        config.put("password", "trivir");
        config.put("keystore-path", keystore);
        
        writeCertificatesToKeyStore(keystore, keyStorePassphrase.toCharArray(), "test", adServerHost, adServerPort);
  
        try {
            connector.setup(config);
        } catch (IdMUnitException e) {
        	throw e;
        } finally {
            new File(keystore).delete();
        }

        Map<String, Collection<String>> attrs  = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList("CN=gmathis,OU=users,OU=AU,DC=american,DC=edu"));
        attrs.put("objectClass", Arrays.asList("user"));
        attrs.put("cn", Arrays.asList("gmathis"));
        attrs.put("givenName", Arrays.asList("Gordon"));
        attrs.put("sn", Arrays.asList("Mathis"));
        attrs.put("displayName", Arrays.asList("Gordon Mathis"));
        attrs.put("sAMAccountName", Arrays.asList("gmathis"));
        attrs.put("userAccountControl", Arrays.asList("512"));
        attrs.put("userPrincipalName", Arrays.asList("gmathis@american.edu"));
        attrs.put("unicodePwd", Arrays.asList("trivir"));

        connector.opDeleteObject(attrs);

        connector.opAddObject(attrs);
                
        attrs  = new TreeMap<String, Collection<String>>();
             
        attrs.put("dn", Arrays.asList("CN=gmathis,OU=users,OU=AU,DC=american,DC=edu") );
  		attrs.put("userPassword", Arrays.asList("trivir"));
  		attrs.put("unicodePwd", Arrays.asList("novell"));
        	  
  		connector.opChangeUserPassword(attrs);
  	
  		// Now login with new password
        config.put("user", "CN=gmathis,OU=users,OU=AU,DC=american,DC=edu");
        config.put("server", adServerHost + ":389");
        config.put("use-tls", "false");
        config.put("password", "novell");
        
        try {
            connector.setup(config);
        } finally {
            new File(jssecacerts).delete();
        }
  		
  		attrs.remove("unicodePwd");
  		attrs.put("userPassword", Arrays.asList("novell"));

  		connector.opValidateObject(attrs);
    }

    
/*
    private static void writeCertificatesToFile(String certFilePath, String host, int port) throws Exception {
        X509Certificate[] chain = getCertificates(host, port);

        FileOutputStream os = new FileOutputStream(certFilePath);

        try {
            for (int i = 0; i < chain.length; i++) {
                byte[] buf = chain[i].getEncoded();

                Writer wr = new OutputStreamWriter(os, Charset.forName("UTF-8"));
                try {
                    wr.write(X509Factory.BEGIN_CERT + "\n");
                    wr.write(new sun.misc.BASE64Encoder().encode(buf) + "\n");
                    wr.write(X509Factory.END_CERT + "\n");
                    wr.flush();
                } catch (IOException e) {
                    throw new Exception("Error writing certificate to file '" + certFilePath + "'");
                }
            }
        } finally {
            os.close();
        }
    }
*/
    private static void writeCertificatesToKeyStore(String keyStorePath, char[] keyStorePassphrase, String alias, String host, int port) throws Exception {
        X509Certificate[] chain = getCertificates(host, port);

        File file = new File(keyStorePath);

        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        if (file.exists()) {
            InputStream in = new FileInputStream(file);
            ks.load(in, keyStorePassphrase);
            in.close();
        } else {
            ks.load(null, null);
        }

        for (int i=0; i<chain.length; ++i) {
            ks.setCertificateEntry(alias + "-" + i, chain[i]);
        }

        OutputStream out = new FileOutputStream(keyStorePath);
        ks.store(out, keyStorePassphrase);
        out.close();
    }

    private static X509Certificate[] getCertificates(String host, int port) throws Exception {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init((KeyStore)null);

        X509TrustManager defaultTrustManager = (X509TrustManager)tmf.getTrustManagers()[0];

        SavingTrustManager tm = new SavingTrustManager(defaultTrustManager);

        context = SSLContext.getInstance("TLS");

        context.init(null, new TrustManager[] {tm}, null);

        SSLSocketFactory factory = context.getSocketFactory();

        SSLSocket socket = (SSLSocket)factory.createSocket(host, port);

        socket.setSoTimeout(5000);

        try {
            socket.startHandshake();
            socket.close();
            throw new Exception("Certificate is already trusted.");
        } catch (SSLException e) {
        } catch (IOException e) {
        }

        if (tm.chain == null) {
            throw new Exception("No certificate chain received.");
        }

        return tm.chain;
    }

    private static class SavingTrustManager implements X509TrustManager {
        private final X509TrustManager tm;
        private X509Certificate[] chain;

        SavingTrustManager(X509TrustManager tm) {
            this.tm = tm;
        }

        public X509Certificate[] getAcceptedIssuers() {
            throw new UnsupportedOperationException();
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            throw new UnsupportedOperationException();
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            this.chain = chain;
            tm.checkServerTrusted(chain, authType);
        }
    }
}
