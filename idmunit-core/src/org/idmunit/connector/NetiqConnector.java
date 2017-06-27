/* 
 * IdMUnit - Automated Testing Framework for Identity Management Solutions
 * Copyright (c) 2017 IDFocus B.V.
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
 *
 */
package org.idmunit.connector;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.idmunit.IdMUnitException;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPJSSESecureSocketFactory;
import com.novell.nds.dirxml.ldap.StartDriverRequest;
import com.novell.nds.dirxml.ldap.StartJobRequest;
import com.novell.nds.dirxml.ldap.StopDriverRequest;

public class NetiqConnector extends LdapConnector {
    private final static String STR_DN = "dn";
	private LDAPConnection ldpconn;

    private static Log logger = LogFactory.getLog(NetiqConnector.class);

    public void setup(Map<String, String> config) throws IdMUnitException {
        super.setup(config);
        this.ldpconn = createNDAPConnection();
    }

	public void opStartDriver( Map<String, Collection<String>> dataRow ) throws IdMUnitException
	{
        String dn = getTargetDn( dataRow );
        if (dn == null) {
            throw new IdMUnitException("A Distinguished Name must be supplied in column '" + STR_DN + "'");
        }
        logger.debug("...starting driver: [" + dn + "]");
        try {
			StartDriverRequest req = new StartDriverRequest( dn );
	        this.ldpconn.extendedOperation( req );
		} catch (LDAPException e) {
			throw new IdMUnitException( "Error starting driver: "+e.getLDAPErrorMessage() );
		}
	}

	public void opStopDriver( Map<String, Collection<String>> dataRow ) throws IdMUnitException
	{
        String dn = getTargetDn( dataRow );
        if (dn == null) {
            throw new IdMUnitException("A Distinguished Name must be supplied in column '" + STR_DN + "'");
        }
        logger.debug("...stopping driver: [" + dn + "]");
        try {
			StopDriverRequest req = new StopDriverRequest( dn );
	        this.ldpconn.extendedOperation( req );
		} catch (LDAPException e) {
			throw new IdMUnitException( "Error stopping driver: "+e.getLDAPErrorMessage() );
		}
	}

	public void opStartJob( Map<String, Collection<String>> dataRow ) throws IdMUnitException
	{
        String dn = getTargetDn( dataRow );
        if (dn == null) {
            throw new IdMUnitException("A Distinguished Name must be supplied in column '" + STR_DN + "'");
        }
        logger.debug("...starting job: [" + dn + "]");
        try {
        	StartJobRequest req = new StartJobRequest( dn );
	        this.ldpconn.extendedOperation( req );
		} catch (LDAPException e) {
			throw new IdMUnitException( "Error starting job: "+e.getLDAPErrorMessage() );
		}
	}

	public void opClearCache( Map<String, Collection<String>> dataRow ) throws IdMUnitException
	{
		logger.debug("...not implemented yet");
		throw new IdMUnitException( "Not implemented yet" );
	}
	
	public void opMigrateFrom( Map<String, Collection<String>> dataRow ) throws IdMUnitException
	{
		logger.debug("...not implemented yet");
		throw new IdMUnitException( "Not implemented yet" );
	}

	private LDAPConnection createNDAPConnection() throws IdMUnitException
	{
        String userDN = config.get(CONFIG_USER);
        String password = config.get(CONFIG_PASSWORD);

        return createNDAPConnection(userDN, password);		
	}

	private LDAPConnection createNDAPConnection(String userDN, String password) throws IdMUnitException
	{
        String server = config.get(CONFIG_SERVER);
        String keystorePath = config.get(CONFIG_KEYSTORE_PATH);
        LDAPConnection lc;
        int port;
        if (keystorePath != null && keystorePath.length() > 0) 
        {
        	// SSL setup; keystore as system property
        	LDAPJSSESecureSocketFactory ssf = new LDAPJSSESecureSocketFactory();
        	lc = new LDAPConnection( ssf );
        	port=636;
        } else {
        	lc = new LDAPConnection();
        	port=389;
        }
        try {
			lc.connect(server, port);
			lc.bind( LDAPConnection.LDAP_V3, userDN, password.getBytes() );
		} catch (LDAPException e) 
		{
			throw new IdMUnitException( "Error connecting to server: "+e.getLDAPErrorMessage() );
		}
        return lc;
	}

}
