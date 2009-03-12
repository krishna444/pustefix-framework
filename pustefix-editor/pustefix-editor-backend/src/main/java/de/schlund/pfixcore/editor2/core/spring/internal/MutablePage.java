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
 */

package de.schlund.pfixcore.editor2.core.spring.internal;

import java.util.Collection;

import org.pustefixframework.editor.common.dom.Page;


/**
 * Extends the Page interface with a method to add sub-pages
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface MutablePage extends Page {
    /**
     * Sets the specified pages as the children of this page
     * 
     * @param page Collection containing Page objects to add as sub-pages
     */
    public void setSubPages(Collection<Page> pages);
    
    /**
     * Set the path to the handler that is used for this page
     * 
     * @param path String specifying the path (e.g. "/xml/handler")
     */
    public void setHandlerPath(String path);
}