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

package org.pustefixframework.webservices;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * @author mleidig@schlund.de
 */
public interface ServiceRequest {

	public String getServiceName();
		
	public Object getUnderlyingRequest();
	public String getParameter(String name);
	
	public String getCharacterEncoding();
	public String getMessage() throws IOException;
	public Reader getMessageReader() throws IOException;
	public InputStream getMessageStream() throws IOException;
	
    public String dump();
    
}
