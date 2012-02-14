/* 
 * IdMUnit - Automated Testing Framework for Identity Management Solutions
 * Copyright (c) 2008 TriVir, LLC
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
package org.idmunit.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.idmunit.EncTool;
import org.idmunit.IdMUnitException;
import org.idmunit.connector.BasicConnector;
import org.idmunit.parser.ExcelParser;

import sun.security.provider.X509Factory;


/*
 *  @author Kenneth Rawlings
 */
public class LdapConnectionHelper
{
    private static Log logger = LogFactory.getLog(LdapConnectionHelper.class);
    public final static String CONFIG_USE_TLS = "use-tls";
    public final static String CONFIG_TRUST_ALL_CERTS = "trust-all-certs";
    public final static String CONFIG_TRUSTED_CERT_FILE = "trusted-cert-file";

    public static InitialDirContext createLdapConnection(Map<String,String> config) throws IdMUnitException {
    	return createLdapConnection("", config);
    }
    
    public static InitialDirContext createLdapConnection(String configPrefix, Map<String,String> config)  throws IdMUnitException {
    	// normalize configPrefix
    	if(configPrefix == null) {
    		configPrefix = "";
    	}
    	
        String server = config.get(configPrefix + BasicConnector.CONFIG_SERVER);
        String keystorePath = config.get(configPrefix + BasicConnector.CONFIG_KEYSTORE_PATH);
        String trustedCertFile = config.get(configPrefix + CONFIG_TRUSTED_CERT_FILE);
        
        boolean trustAll = false;
        if (config.get(configPrefix + CONFIG_TRUST_ALL_CERTS) != null) {
            trustAll = Boolean.parseBoolean(config.get(configPrefix + CONFIG_TRUST_ALL_CERTS));
        }
        
        boolean useTLS = false;
        if (config.get(configPrefix + CONFIG_USE_TLS) != null) {
            useTLS = Boolean.parseBoolean(config.get(configPrefix + CONFIG_USE_TLS));
        } else if (keystorePath != null || trustedCertFile != null || trustAll) {
            useTLS = true;
        }

        Properties properties;
		try {
			properties = ExcelParser.loadProperties();
		} catch (IOException e) {
			throw new IdMUnitException("Unable to load IdMUnit properties.", e);
		}
        String userDN = config.get(configPrefix + BasicConnector.CONFIG_USER);
        String password = config.get(configPrefix + BasicConnector.CONFIG_PASSWORD);
    	String encryptionKey = null;
        String decryptPasswords = properties.getProperty(ExcelParser.DECRYPT_PASSWORDS);
       if (decryptPasswords == null || Boolean.parseBoolean(decryptPasswords)) {
        	encryptionKey = properties.getProperty(ExcelParser.ENCRYPTION_KEY);
        	if (encryptionKey == null) {
        		encryptionKey = ExcelParser.DEFAULT_ENCRYPTION_KEY;
        	}

            if (encryptionKey != null && password != null) {
                //decrypt the password first
                EncTool encryptionManager = new EncTool(encryptionKey);
                password = encryptionManager.decryptCredentials(password);
            }
       }

        
        Map<String,String> envOverride = Collections.emptyMap();
        return createLdapConnection(server, userDN, password, keystorePath, trustedCertFile, useTLS, trustAll, envOverride);
    }
    
    public static InitialDirContext createLdapConnection(String server, String userDN, String password, String keystorePath, String trustedCertFile, boolean useTLS, boolean trustAll, Map<String,String> envOverride) throws IdMUnitException {
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
        env.put("com.sun.jndi.ldap.connect.pool", "true");
        env.put("com.sun.jndi.ldap.connect.pool.protocol", "plain ssl");
        env.put("com.sun.jndi.ldap.connect.pool.timeout", "1000");
        env.put("com.sun.jndi.ldap.connect.pool.maxsize", "3");
        env.put("com.sun.jndi.ldap.connect.pool.prefsize", "1");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, userDN);
        env.put(Context.SECURITY_CREDENTIALS, password);
        env.put("com.sun.jndi.ldap.connect.timeout", "5000");
        env.put(Context.REFERRAL, "follow");
        if (useTLS == false) {
            env.put(Context.PROVIDER_URL, "ldap://" + server);
        } else {
            env.put(Context.PROVIDER_URL, "ldaps://" + server);
            if (keystorePath != null) {
                CustomSocketFactory.setTrustedKeyStore(keystorePath, null);
                env.put("java.naming.ldap.factory.socket", CustomSocketFactory.class.getName());
            } else if (trustedCertFile != null) {
                CustomSocketFactory.setTrustedCert(trustedCertFile);
                env.put("java.naming.ldap.factory.socket", CustomSocketFactory.class.getName());
            } else if (trustAll) {
                CustomSocketFactory.setTrustAll();
                env.put("java.naming.ldap.factory.socket", CustomSocketFactory.class.getName());
            }
        }
        
        for(Map.Entry<String,String> entry : env.entrySet()) {
        	if(env.containsKey(entry.getKey())) {
        		logger.info(String.format("Overriding configured LDAP connection option '%s' containing value '%s' with value '%s'.", entry.getKey(), env.get(entry.getKey()), entry.getValue()));
        	} else {
        		logger.info(String.format("Adding additional LDAP connection option '%s' with value '%s'.", entry.getKey(), entry.getValue()));
        	}
        	
        	env.put(entry.getKey(), entry.getValue());
        }

        try {
            return new InitialDirContext(env);
        }catch (Exception e) {
            if (useTLS) {
                if (keystorePath != null) {
                    logger.info("Using configured keystore (" + keystorePath + ").");
                } else if (trustedCertFile != null) {
                    logger.info("Using configured certificate (" + trustedCertFile + ").");
                } else if (trustAll) {
                    logger.info("Configured to trust all certificates, this should not have happened.");
                } else {
                    if (System.getProperty("javax.net.ssl.trustStore") != null) {
                        logger.info("Using keystore configured from javax.net.ssl.trustStore (" + System.getProperty("javax.net.ssl.trustStore") + ").");
                    } else {
                        final char SEP = File.separatorChar;
                        final String jssecacerts = System.getProperty("java.home") + SEP + "lib" + SEP + "security" + SEP + "jssecacerts";
                        String defaultKeystore;
                        if (new File(jssecacerts).exists()) {
                            defaultKeystore = jssecacerts;
                        } else {
                            defaultKeystore = System.getProperty("java.home") + SEP + "lib" + SEP + "security" + SEP + "cacerts";
                        }
                        logger.info("Using JRE default keystore (" + defaultKeystore + ").");
                    }
                }

                exportCertificate(server, trustedCertFile);
                throw new IdMUnitException("Failed to obtain an SSL LDAP Connection: " + e.getMessage(), e);
            } else {
                logger.debug("### Failed to obtain an LDAP server connection to: [" + server + "].");
                throw new IdMUnitException("Failed to obtain an LDAP Connection: " + e.getMessage(), e);
            }
        }
    }

    private static void exportCertificate(String server, String trustedCertFile) throws IdMUnitException {
        String host;
        int port = 636;
        if (server.indexOf(':') == -1) {
            host = server;
        } else {
            int j = server.indexOf(':');
            host = server.substring(0, j);
            port = Integer.parseInt(server.substring(j+1));
        }

        String certFilePath;
        if (trustedCertFile != null) {
            certFilePath = trustedCertFile;
        } else {
            if (port == 636) {
                certFilePath = host + ".cer";
            } else {
                certFilePath = host + "_" + port + ".cer";
            }
        }

        writeCertificatesToFile(certFilePath, host, port);
        logger.info("Writing certificates for '" + server + "' to file '" + certFilePath + "'");
    }

    private static void writeCertificatesToFile(String certFilePath, String host, int port) throws IdMUnitException {
        X509Certificate[] chain = getCertificates(host, port);

        FileOutputStream os;
        try {
            os = new FileOutputStream(certFilePath);
        } catch (FileNotFoundException e) {
            throw new IdMUnitException("Error opening to file '" + certFilePath + "'");
        }

        try {
            for (int i = 0; i < chain.length; i++) {
                byte[] buf;
                try {
                    buf = chain[i].getEncoded();
                } catch (CertificateEncodingException e) {
                    throw new IdMUnitException("Error encoding certificate.", e);
                }

                Writer wr = new OutputStreamWriter(os, Charset.forName("UTF-8"));
                try {
                    wr.write(X509Factory.BEGIN_CERT + "\n");
                    wr.write(new sun.misc.BASE64Encoder().encode(buf) + "\n");
                    wr.write(X509Factory.END_CERT + "\n");
                    wr.flush();
                } catch (IOException e) {
                    throw new IdMUnitException("Error writing certificate to file '" + certFilePath + "'");
                }
            }
        } finally {
            try {
                os.close();
            } catch (IOException e) {
            }
        }
    }

    private static X509Certificate[] getCertificates(String host, int port) throws IdMUnitException {
        TrustManagerFactory tmf;
        try {
            tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            throw new IdMUnitException("Error getting TrustManagerFactory.", e);
        }

        try {
            tmf.init((KeyStore)null);
        } catch (KeyStoreException e) {
            throw new IdMUnitException("Error initializing TrustManagerFactory.", e);
        }

        X509TrustManager defaultTrustManager = (X509TrustManager)tmf.getTrustManagers()[0];

        SavingTrustManager tm = new SavingTrustManager(defaultTrustManager);

        SSLContext context;
        try {
            context = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            throw new IdMUnitException("Error getting SSLContext.", e);
        }

        try {
            context.init(null, new TrustManager[] {tm}, null);
        } catch (KeyManagementException e) {
            throw new IdMUnitException("Error initializing SSLContext.", e);
        }

        SSLSocketFactory factory = context.getSocketFactory();

        SSLSocket socket;
        try {
            socket = (SSLSocket)factory.createSocket(host, port);
        } catch (UnknownHostException e) {
            throw new IdMUnitException("Error creating socket.", e);
        } catch (IOException e) {
            throw new IdMUnitException("Error creating socket.", e);
        }

        try {
            socket.setSoTimeout(5000);
        } catch (SocketException e) {
            throw new IdMUnitException("Error setting socket timeout.", e);
        }

        try {
            socket.startHandshake();
            socket.close();
        } catch (SSLException e) {
        } catch (IOException e) {
        }

        if (tm.chain == null) {
            throw new IdMUnitException("No certificate chain received.");
        }

        return tm.chain;
    }

    public static class CustomSocketFactory extends SocketFactory {
        private static SocketFactory factory = null;
        private static TrustManager[] tm;
        private SSLSocketFactory sf;

        private CustomSocketFactory() throws KeyManagementException, NoSuchAlgorithmException {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, tm, null);
            sf = sc.getSocketFactory();
        }

        static void setTrustAll() {
            tm = new TrustManager[] {new TrustAllX509TrustManager()};
        }

        static void setTrustedKeyStore(String keyStorePath, char[] passphrase) throws IdMUnitException {
            KeyStore caKeystore;
            try {
                caKeystore = KeyStore.getInstance(KeyStore.getDefaultType());
            } catch (KeyStoreException e) {
                throw new IdMUnitException("Error creating keystore.", e);
            }

            InputStream in;
            try {
                in = new FileInputStream(keyStorePath);
            } catch (FileNotFoundException e) {
                throw new IdMUnitException("Error reading keystore '" + keyStorePath + "'.", e);
            }
            try {
                try {
                    caKeystore.load(in, passphrase);
                } catch (NoSuchAlgorithmException e) {
                    throw new IdMUnitException("Error loading keystore '" + keyStorePath + "'.", e);
                } catch (CertificateException e) {
                    throw new IdMUnitException("Error loading keystore '" + keyStorePath + "'.", e);
                } catch (IOException e) {
                    throw new IdMUnitException("Error loading keystore '" + keyStorePath + "'.", e);
                }
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            setTrustManager(caKeystore);
        }

        static void setTrustedCert(String certFilePath) throws IdMUnitException {
            InputStream is;
            try {
                is = new FileInputStream(certFilePath);
            } catch (FileNotFoundException e) {
                throw new IdMUnitException("Error reading certificate '" + certFilePath + "'.", e);
            }
            try {
                CertificateFactory cf;
                try {
                    cf = CertificateFactory.getInstance("X.509");
                } catch (CertificateException e) {
                    throw new IdMUnitException("Error getting CertificateFactory.", e);
                }
                Collection<? extends Certificate> x509Certs;
                try {
                    x509Certs = cf.generateCertificates(is);
                } catch (CertificateException e) {
                    throw new IdMUnitException("Error parsing certificates '" + certFilePath + "'.", e);
                }

                KeyStore ks;
                try {
                    ks = KeyStore.getInstance(KeyStore.getDefaultType());
                } catch (KeyStoreException e) {
                    throw new IdMUnitException("Error creating KeyStore.", e);
                }

                try {
                    ks.load(null, null);
                } catch (NoSuchAlgorithmException e) {
                    throw new IdMUnitException("Error initializing KeyStore", e);
                } catch (CertificateException e) {
                    throw new IdMUnitException("Error initializing KeyStore", e);
                } catch (IOException e) {
                    throw new IdMUnitException("Error initializing KeyStore", e);
                }

                int count = 0;
                for (Iterator<? extends Certificate> it = x509Certs.iterator(); it.hasNext();) {
                    X509Certificate cert = (X509Certificate) it.next();
//                    cert.checkValidity();

                    String subjectPrincipal = cert.getSubjectX500Principal().toString();
                    StringTokenizer st = new StringTokenizer(subjectPrincipal, ",");
                    String cn = "";
                    while (st.hasMoreTokens()) {
                        String tok = st.nextToken();
                        int x = tok.indexOf("CN=");
                        if (x >= 0) {
                            cn = tok.substring(x + "CN=".length());
                        }
                    }
                    String alias = cn + "_" + count;
                    try {
                        ks.setCertificateEntry(alias, cert);
                    } catch (KeyStoreException e) {
                        throw new IdMUnitException("Error adding certificate to KeyStore", e);
                    }
                    count++;
                }
                setTrustManager(ks);
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private static void setTrustManager(KeyStore caKeystore) {
            String defaultTrustAlgorithm = TrustManagerFactory.getDefaultAlgorithm();

            TrustManagerFactory caTrustManagerFactory;
            try {
                caTrustManagerFactory = TrustManagerFactory.getInstance(defaultTrustAlgorithm);
            } catch (NoSuchAlgorithmException e) {
                caTrustManagerFactory = null;
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            try {
                caTrustManagerFactory.init(caKeystore);
            } catch (KeyStoreException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            tm = caTrustManagerFactory.getTrustManagers();
        }

        public static SocketFactory getDefault() {
            synchronized (CustomSocketFactory.class) {
                if (factory == null) {
                    try {
                        factory = new CustomSocketFactory();
                    } catch (KeyManagementException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            return factory;
        }

        public Socket createSocket() throws IOException {
            return sf.createSocket();
        }

        public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
            return sf.createSocket(host,port);
        }

        public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
            return sf.createSocket(host, port, localHost, localPort);
        }

        public Socket createSocket(InetAddress host, int port) throws IOException {
            return sf.createSocket(host,port);
        }

        public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
            return sf.createSocket(address, port, localAddress, localPort);
        }
    }

    private static class TrustAllX509TrustManager implements X509TrustManager {
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType) {
            return;
        }
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

	public static void destroyLdapConnection(DirContext context) throws IdMUnitException {
        try {
            if(context!=null) {
                context.close();
                context = null;
            }
        } catch (NamingException e) {
            throw new IdMUnitException("Failed to close ldap connection: " + e.getMessage());
        }
	}
}