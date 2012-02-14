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

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.idmunit.Alert;
import org.idmunit.ConfigLoader;
import org.idmunit.injector.InjectionConfigData;

/**
 * Holds credential data for the target system
 * @author Brent Kynaston, Software Engineer, TriVir LLC
 * @version %I%, %G%
 * @see ConfigLoader
 * @see DdRowBehaviour
 */
public class ConnectionConfigData {
    private final static String XML_KEYSTORE = "keystore-path";
    private final static String XML_USER = "user";
    private final static String XML_PASSWORD = "password";
    private final static String XML_SERVER = "server";
    public static final String DISABLED = "disabled";

    private Map<String, String> configParams = new HashMap <String, String> ();
    private String name;
	private String type;
	private Map<String, String> dataSubstitutions;
	private List<InjectionConfigData> dataInjections;
	private Map<String, Alert> idmunitAlerts;
	private int multiplierRetry;
	private int multiplierWait;
	
	public ConnectionConfigData(String name, String type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

    public String getParam(String name) {
        return configParams.get(name);
    }

    public void setParam(String name, String value) {
        configParams.put(name, value);
    }

    public Map<String, String> getParams() {
        return configParams;
    }

	public String getAdminCtx() {
		return configParams.get(XML_USER);
	}
	
	public String getAdminPwd() {
		return configParams.get(XML_PASSWORD);
	}

	public String getKeystorePath() {
		return configParams.get(XML_KEYSTORE);
	}

	public String getServerURL() {
		return configParams.get(XML_SERVER);
	}

	public Map<String, Alert> getIdmunitAlerts() {
		return idmunitAlerts;
	}

	public void setIdmunitAlerts(Map<String, Alert> idmunitAlerts) {
		this.idmunitAlerts = idmunitAlerts;
	}

	public void setDataInjections(List<InjectionConfigData> dataInjections) {
		this.dataInjections = dataInjections;
	}

	public List<InjectionConfigData> getDataInjections() {
		return dataInjections;
	}
	
	public boolean ifMultiplierRetry() {
        return multiplierRetry > 1;
    }

	public boolean ifMultiplierWait() {
        return multiplierWait > 1;
    }
	
	public int getMultiplierRetry() {
		return multiplierRetry;
	}

	public void setMultiplierRetry(int multiplierRetry) {
		this.multiplierRetry = multiplierRetry;
	}

	public int getMultiplierWait() {
		return multiplierWait;
	}

	public void setMultiplierWait(int multiplierWait) {
		this.multiplierWait = multiplierWait;
	}

	public Map<String, String> getSubstitutions() {
		return dataSubstitutions;
	}

	public void setSubstitutions(Map<String, String> substitutions) {
		this.dataSubstitutions = substitutions;
	}
}
