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

package de.schlund.pfixcore.editor2.core.spring;

import org.pustefixframework.editor.common.dom.Project;

/**
 * Provides methods to retrieve the project.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 * @see org.pustefixframework.editor.common.dom.Project
 */
public interface ProjectFactoryService {
    /**
     * Returns the Project object.
     * 
     * @return Project object for this application
     */
    Project getProject();
}
