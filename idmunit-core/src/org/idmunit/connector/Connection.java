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

import java.util.Map;

import javax.naming.directory.Attributes;
import org.ddsteps.dataset.DataRow;
import org.idmunit.IdMUnitException;

/**
 * Provides the basic interface for an IdMUnit Connector implementation.  If a connector is implemented
 * according to this interface, IdMUnit will be able to make full use of it!
 * @author Brent Kynaston, Software Engineer, TriVir LLC
 * @version %I%, %G%
 */public interface Connection {
	public void setupConnection(ConnectionConfigData creds) throws IdMUnitException;

	public abstract void closeConnection() throws IdMUnitException;

	public abstract void addObject(DataRow dataRow) throws IdMUnitException;

	public abstract void modObject(Attributes assertedAttrs, int operationType)
	throws IdMUnitException;

	//TODO: refactor into add object, after eliminating complex add structure
	public abstract void insertObject(Attributes assertedAttrs)
	throws IdMUnitException;

	public abstract void deleteObject(Attributes assertedAttrs)
			throws IdMUnitException;

	public abstract void renameObject(Attributes assertedAttrs)
			throws IdMUnitException;

	public abstract void moveObject(Attributes assertedAttrs)
			throws IdMUnitException;

	public abstract void validatePassword(Attributes assertedAttrs)
			throws IdMUnitException;

	public abstract void validateObject(Attributes assertedAttrs)
			throws IdMUnitException;

	public abstract Map<String, String> search(String filter, String base, String[] collisionAttrs)
	throws IdMUnitException;
}