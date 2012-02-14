package org.idmunit.connector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
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

import sun.security.provider.X509Factory;

import junit.framework.TestCase;

public class LdapConnectorSetupTests extends TestCase {
    private static final String server = "localhost";
    private static final int port = 637;
    private static final String trustedCertFile = "test.cer";
    private static final String keystore = "testkeystore";
    private static final String keyStorePassphrase = "changeit";
    private static final char SEP = File.separatorChar;
    private static final String jssecacerts = System.getProperty("java.home") + SEP + "lib" + SEP + "security" + SEP + "jssecacerts";

    private LdapConnector connector;

    @Override
    protected void setUp() throws Exception {
        BasicConfigurator.configure();

        connector = new LdapConnector();
    }

    @Override
    protected void tearDown() throws Exception {
        connector.tearDown();
    }

    public void testTlsConnection() throws IdMUnitException, Exception {
        Map<String, String> config = new TreeMap<String, String>();
        config.put("server", server + ":" + port);
        config.put("use-tls", "true");
        config.put("user", "cn=admin,o=Services");
        config.put("password", "trivir");

        writeCertificatesToKeyStore(jssecacerts, "changeit".toCharArray(), "test", server, port);
        try {
            connector.setup(config);
        } finally {
            new File(jssecacerts).delete();
        }
    }

    // @TODO This test currently fails if run with the other tests.
    public void testTlsConnectionWithoutCert() throws IdMUnitException, Exception {
        Map<String, String> config = new TreeMap<String, String>();
        config.put("server", server + ":" + port);
        config.put("use-tls", "true");
        config.put("user", "cn=admin,o=Services");
        config.put("password", "trivir");

        String certFilePath;
        if (port == 636) {
            certFilePath = server + ".cer";
        } else {
            certFilePath = server + "_" + port + ".cer";
        }

        try {
            assertTrue("Certificate file does not exist", new File(certFilePath).exists() == false);
            connector.setup(config);
            fail("Should have failed to connect");
        } catch(Exception e) {
            assertTrue("Certificate file exists", new File(certFilePath).exists());
        } finally {
            new File(certFilePath).delete();
        }
    }

    public void testTlsConnectionUsingKeyStore() throws IdMUnitException, Exception {
        Map<String, String> config = new TreeMap<String, String>();
        config.put("server", server + ":" + port);
        config.put("user", "cn=admin,o=Services");
        config.put("password", "trivir");
        config.put("keystore-path", keystore);

        writeCertificatesToKeyStore(keystore, keyStorePassphrase.toCharArray(), "test", server, port);
        try {
            connector.setup(config);
        } finally {
            new File(keystore).delete();
        }
    }

    public void testTlsConnectionUsingCertFile() throws IdMUnitException, Exception {
        Map<String, String> config = new TreeMap<String, String>();
        config.put("server", server + ":" + port);
        config.put("user", "cn=admin,o=Services");
        config.put("password", "trivir");
        config.put("trusted-cert-file", trustedCertFile);

        writeCertificatesToFile(trustedCertFile, server, port);
        try {
            connector.setup(config);
        } finally {
            new File(trustedCertFile).delete();
        }
    }

    public void testTlsConnectionTrustAll() throws IdMUnitException, Exception {
        Map<String, String> config = new TreeMap<String, String>();
        config.put("server", server + ":" + port);
        config.put("user", "cn=admin,o=Services");
        config.put("password", "trivir");
        config.put("trust-all-certs", "true");

        connector.setup(config);
    }

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

        SSLContext context = SSLContext.getInstance("TLS");

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
