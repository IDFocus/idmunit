/* 
 * IdMUnit - Automated Testing Framework for Identity Management Solutions
 * Copyright (c) 2008-2009 TriVir, LLC
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.idmunit.IdMUnitException;

public abstract class AbstractConnector implements Connector {
    Map<String, Method> methods = new TreeMap<String, Method>(String.CASE_INSENSITIVE_ORDER);
    
    public AbstractConnector() {
        Method[] allMethods = getClass().getMethods();
        for (Method m : allMethods) {
            if (m.getName().startsWith("op") == false) {
                continue;
            }

            if (m.getGenericReturnType() != void.class) {
                continue;
            }

            Type[] paramTypes = m.getGenericParameterTypes();
            if (paramTypes.length != 1 || Map.class.isAssignableFrom(paramTypes[0].getClass())) {
                continue;
            }
            
            methods.put(m.getName(), m);
        }
    }

    public void tearDown() throws IdMUnitException {
    }

    public void execute(String operation, Map<String, Collection<String>> data) throws IdMUnitException {
        String methodName = "op" + operation;
        Method m = methods.get(methodName);
        if (m == null) {
            throw new IdMUnitException("Invalid operation '" + operation + "'");
        }
    
        Object[] args = {data};
        try {
            m.invoke(this, args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Error) {
                throw (Error)cause;
            } else if (cause instanceof IdMUnitException) {
                throw (IdMUnitException)cause;
            }
            
            throw new IdMUnitException(cause);
        } catch (IllegalArgumentException e) {
            throw new IdMUnitException(e);
        } catch (IllegalAccessException e) {
            throw new IdMUnitException(e);
        }
    }
}