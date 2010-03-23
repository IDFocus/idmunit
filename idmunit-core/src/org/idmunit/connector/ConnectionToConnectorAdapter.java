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

import java.util.Collection;
import java.util.Map;

import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;

import org.ddsteps.dataset.bean.DataRowBean;
import org.ddsteps.dataset.bean.DataValueBean;
import org.idmunit.IdMUnitException;

public class ConnectionToConnectorAdapter implements Connector {
    public final static int CLEAR_ATTRIBUTE = 4;
    
    private final static String ERROR_UNKNOWN_OP = "Unknown operation - Please check the operation column for this row and try again.";
    private final static String OP_EXEC_SQL = "execSQL";
    private final static String OP_ADD_OBJECT = "addObject";
    private final static String OP_MOD_OBJECT = "modObject";
    private final static String OP_MODIFY_OBJECT = "modifyObject";
    private final static String OP_ADD_ATTR = "addAttr";
    private final static String OP_MOD_ATTR = "modAttr";
    private final static String OP_REMOVE_ATTR = "removeAttr";
    private final static String OP_CLEAR_ATTR = "clearAttr";
    private final static String OP_DEL_OBJECT = "delObject";
    private final static String OP_REN_OBJECT = "renObject";
    private final static String OP_RENAME_OBJECT = "renameObject";
    private final static String OP_MOV_OBJECT = "moveObject";
    private final static String OP_VALIDATE_OBJECT = "validateObject";
    private final static String OP_VALIDATE_PASSWORD = "validatePassword";

    private Connection connection;

    public ConnectionToConnectorAdapter(String className) throws IdMUnitException {
        try {
            connection = (Connection)Class.forName(className).newInstance();
        } catch (InstantiationException e) {
            throw new IdMUnitException("Failed to instantiate connection class of type: " + className + " " + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new IdMUnitException("Illegal access error when attempting to instantiate connection of type: " + className);
        } catch (ClassNotFoundException e) {
            throw new IdMUnitException("Specified target connection module not found: " + className);
        }
    }

    public ConnectionToConnectorAdapter(Connection connection) {
        this.connection = connection;
    }

    public void execute(String op, Map<String, Collection<String>> data) throws IdMUnitException {
        if(op.equalsIgnoreCase(OP_ADD_OBJECT)) {
            connection.addObject(getRowDataBean(data));
        } else if(op.equalsIgnoreCase(OP_MOD_OBJECT)
                || op.equalsIgnoreCase(OP_MODIFY_OBJECT)
                || op.equalsIgnoreCase(OP_MOD_ATTR))
        {
            connection.modObject(getRowData(data), DirContext.REPLACE_ATTRIBUTE);
        }else if(op.equalsIgnoreCase(OP_EXEC_SQL)) {
            connection.insertObject(getRowData(data));
        } else if(op.equalsIgnoreCase(OP_ADD_ATTR)) {
            connection.modObject(getRowData(data), DirContext.ADD_ATTRIBUTE);
        } else if(op.equalsIgnoreCase(OP_REMOVE_ATTR)) {
            connection.modObject(getRowData(data), DirContext.REMOVE_ATTRIBUTE);
        }else if(op.equalsIgnoreCase(OP_CLEAR_ATTR)) {
            connection.modObject(getRowData(data), CLEAR_ATTRIBUTE);
        } else if(op.equalsIgnoreCase(OP_DEL_OBJECT)) {
            connection.deleteObject(getRowData(data));
        } else if(op.equalsIgnoreCase(OP_VALIDATE_OBJECT)) {
            connection.validateObject(getRowData(data));
        } else if(op.equalsIgnoreCase(OP_VALIDATE_PASSWORD)) {
            connection.validatePassword(getRowData(data));
        } else if(op.equalsIgnoreCase(OP_MOV_OBJECT) 
                || op.equalsIgnoreCase(OP_REN_OBJECT) 
                || op.equalsIgnoreCase(OP_RENAME_OBJECT)) {
            connection.moveObject(getRowData(data));
        } else if("startServer".equalsIgnoreCase(op)) {
            System.out.println("Starting SMTP server . .");
        } else {
//          public final static String STR_DETECTED_OPERATION = ;
            throw new IdMUnitException(ERROR_UNKNOWN_OP + " Detected operation: " + op);
        }
    }

    public void setup(Map<String, String> config) throws IdMUnitException {
        ConnectionConfigData configData = new ConnectionConfigData("", "");
        for (String key : config.keySet()) {
        	configData.setParam(key, config.get(key));
        }
        connection.setupConnection(configData);
    }

    public void tearDown() throws IdMUnitException {
        connection.closeConnection();
    }

    private static DataRowBean getRowDataBean(Map<String, Collection<String>> data) {
        DataRowBean dataRow = new DataRowBean();
        for (String attrName : data.keySet()) {
            dataRow.addValue(new DataValueBean(attrName, concatValues(data.get(attrName))));
        }
        return dataRow;
    }

    private static Attributes getRowData(Map<String, Collection<String>> data) {
        BasicAttributes attrs = new BasicAttributes();
        for (String attrName : data.keySet()) {
            attrs.put(attrName, concatValues(data.get(attrName)));
        }        
        return attrs; 
    }
    
    private static String concatValues(Collection<String> values) {
        StringBuffer s = new StringBuffer();
        for (String value : values) {
            if (s.length() > 0) {
                s.append('|');
            }
            s.append(value);
        }
        return s.toString();
    }
}
