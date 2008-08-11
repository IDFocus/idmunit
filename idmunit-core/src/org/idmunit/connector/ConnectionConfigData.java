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

import java.util.List;
import java.util.Map;

import org.ddsteps.junit.behaviour.DdRowBehaviour;
import org.idmunit.Alert;
import org.idmunit.ConfigLoader;
import org.idmunit.EncTool;
import org.idmunit.IdMUnitException;
import org.idmunit.injector.InjectionConfigData;

/**
 * Holds credential data for the target system
 * @author Brent Kynaston, Software Engineer, TriVir LLC
 * @version %I%, %G%
 * @see ConfigLoader
 * @see DdRowBehaviour
 */
public class ConnectionConfigData {
	private String type;
	private String serverURL;
	private String adminCtx;
	private String adminPwd;
	private String keystorePath;
	private Map dataSubstitutions;
	private List<InjectionConfigData> dataInjections;
	private Map<String, Alert> idmunitAlerts;
	private int multiplierRetry;
	private int multiplierWait;
	
	public boolean ifMultiplierRetry() {
		if(multiplierRetry > 1) {
			return true;
		}
		return false;
	}

	public boolean ifMultiplierWait() {
		if(multiplierWait > 1) {
			return true;
		}
		return false;
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

	public Map getSubstitutions() {
		return dataSubstitutions;
	}

	public void setSubstitutions(Map substitutions) {
		this.dataSubstitutions = substitutions;
	}

	public String getKeystorePath() {
		return keystorePath;
	}

	public void setKeystorePath(String keystorePath) {
		this.keystorePath = keystorePath;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAdminCtx() {
		return adminCtx;
	}
	
	public ConnectionConfigData() {
		this.serverURL = null;
		this.adminCtx = null;
		this.adminPwd = null;
	}
	
	public ConnectionConfigData(String server, String user, String password) throws IdMUnitException {
		setServerURL(server);
		setAdminCtx(user);
		setAdminPwd(password);
	}
	
	public void setAdminCtx(String adminCtx) {
		this.adminCtx = adminCtx;
	}
	public String getAdminPwd() {
		return adminPwd;
	}

	public void setClearAdminPwd(String clearPwd) throws IdMUnitException {
		this.adminPwd = clearPwd;
	}
	
	public void setAdminPwd(String adminPwd) throws IdMUnitException {
		//decrypt the password first
		EncTool encryptionManager = new EncTool("IDMUNIT1");
		this.adminPwd = encryptionManager.decryptCredentials(adminPwd);
	}
	public String getServerURL() {
		return serverURL;
	}
	void setServerURL(String serverURL) {
		this.serverURL = serverURL;
	}

	public Map getIdmunitAlerts() {
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
}
