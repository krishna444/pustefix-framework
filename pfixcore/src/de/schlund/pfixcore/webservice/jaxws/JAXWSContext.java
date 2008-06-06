/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package de.schlund.pfixcore.webservice.jaxws;

import de.schlund.pfixcore.webservice.ProcessingInfo;

/**
 * Request processing context stored threadlocal to get access to internal
 * information (invocation time, exception).
 * 
 * @author mleidig@schlund.de
 */
public class JAXWSContext {

    private Throwable throwable;
    private ProcessingInfo procInfo;

    public JAXWSContext(ProcessingInfo procInfo) {
        this.procInfo = procInfo;
    }

    public static JAXWSContext getCurrentContext() {
        return JAXWSProcessor.getCurrentContext();
    }

    public void startInvocation() {
        procInfo.startInvocation();
    }

    public void endInvocation() {
        procInfo.endInvocation();
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

}
