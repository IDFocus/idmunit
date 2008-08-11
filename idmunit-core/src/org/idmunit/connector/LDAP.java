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
package org.idmunit.connector;

import java.util.Collection;
import java.util.Map;

import org.idmunit.IdMUnitException;

/**
 * This class is provided for backwards compatability with older versions of
 * IdMUnit. All new tests should use LdapConnector instead of this class.
 * 
 * @deprecated
 */
public class LDAP extends LdapConnector {
    public void opModObject(Map<String, Collection<String>> data) throws IdMUnitException {
        opReplaceAttr(data);
    }

    public void opModifyObject(Map<String, Collection<String>> data) throws IdMUnitException {
        opReplaceAttr(data);
    }

    public void opModAttr(Map<String, Collection<String>> data) throws IdMUnitException {
        opReplaceAttr(data);
    }

    public void opDelObject(Map<String, Collection<String>> data) throws IdMUnitException {
    	opDeleteObject(data);
    }

    public void opRenObject(Map<String, Collection<String>> data) throws IdMUnitException {
    	opRenameObject(data);
    }
}