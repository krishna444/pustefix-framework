/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.pustefixframework.config.contextxmlservice.parser.internal;

import java.util.LinkedList;
import java.util.List;

import org.pustefixframework.extension.AuthConstraintExtension;
import org.pustefixframework.extension.AuthConstraintExtensionPoint;
import org.pustefixframework.extension.support.AbstractExtension;

import de.schlund.pfixcore.auth.AuthConstraint;

/**
 * Extension for authconstraint extension point.  
 * 
 * @author mleidig@schlund.de
 * 
 */
public class AuthConstraintExtensionImpl extends AbstractExtension<AuthConstraintExtensionPoint, AuthConstraintExtensionImpl> implements AuthConstraintExtension {

    private InternalAuthConstraintMap authConstraintMap = new InternalAuthConstraintMap();

    public AuthConstraintExtensionImpl() {
        setExtensionPointType(AuthConstraintExtensionPoint.class);
    }

    public List<AuthConstraint> getAuthConstraints() {
        return new LinkedList<AuthConstraint>(authConstraintMap.values());
    }

    public void setAuthConstraintObjects(List<Object> authConstraintObjects) {
        authConstraintMap.setAuthConstraintObjects(authConstraintObjects);
    }

    private class InternalAuthConstraintMap extends AuthConstraintMap {

        @Override
        protected void updateCache() {
            super.updateCache();
            synchronized (registrationLock) {
                for (AuthConstraintExtensionPoint extensionPoint : extensionPoints) {
                    extensionPoint.updateExtension(AuthConstraintExtensionImpl.this);
                }
            }
        }

    }

}
