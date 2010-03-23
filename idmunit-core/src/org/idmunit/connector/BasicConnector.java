/* 
 * IdMUnit - Automated Testing Framework for Identity Management Solutions
 * Copyright (c) 2008-2010 TriVir, LLC
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

public abstract class BasicConnector extends AbstractConnector {
    public static final String CONFIG_KEYSTORE_PATH = "keystore-path";
    public static final String CONFIG_PASSWORD = "password";
    public static final String CONFIG_SERVER = "server";
    public static final String CONFIG_USER = "user";

    /**
     * @deprecated
     */
    protected void opAddObject(Map<String, Collection<String>> data) throws IdMUnitException {opAdd(data);}
    /**
     * @deprecated
     */
    protected void opDeleteObject(Map<String, Collection<String>> data) throws IdMUnitException {opDelete(data);}
    /**
     * @deprecated
     */
    protected void opMoveObject(Map<String, Collection<String>> data) throws IdMUnitException {opMove(data);}
    /**
     * @deprecated
     */
    protected void opRenameObject(Map<String, Collection<String>> data) throws IdMUnitException {opRename(data);}
    /**
     * @deprecated
     */
    protected void opValidateObject(Map<String, Collection<String>> data) throws IdMUnitException {opValidate(data);}

    protected void opAdd(Map<String, Collection<String>> data) throws IdMUnitException {
        throw new IdMUnitException("Invalid operation 'Add'");
    }

    protected void opDelete(Map<String, Collection<String>> data) throws IdMUnitException {
        throw new IdMUnitException("Invalid operation 'Delete'");
    }

    protected void opMove(Map<String, Collection<String>> data) throws IdMUnitException {
        throw new IdMUnitException("Invalid operation 'Move'");
    }

    protected void opRename(Map<String, Collection<String>> data) throws IdMUnitException {
        throw new IdMUnitException("Invalid operation 'Rename'");
    }

    protected void opValidate(Map<String, Collection<String>> data) throws IdMUnitException {
        throw new IdMUnitException("Invalid operation 'Validate'");
    }
}
