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

package org.pustefixframework.editor.webui.remote.dom;

import org.pustefixframework.editor.common.dom.IncludePart;
import org.pustefixframework.editor.common.dom.IncludePartThemeVariant;
import org.pustefixframework.editor.common.remote.service.RemoteCommonIncludeService;
import org.pustefixframework.editor.common.remote.transferobjects.IncludePartThemeVariantReferenceTO;
import org.pustefixframework.editor.webui.remote.dom.util.RemoteServiceUtil;



public class IncludePartThemeVariantImpl extends CommonIncludePartThemeVariantImpl {
    
    protected IncludePartThemeVariantImpl(RemoteServiceUtil remoteServiceUtil, String path, String part, String theme) {
        super(remoteServiceUtil, path, part, theme);
    }

    @Override
    protected RemoteCommonIncludeService getRemoteService() {
        return remoteServiceUtil.getRemoteIncludeService();
    }
    
    @Override
    protected IncludePartThemeVariant newInstance(IncludePartThemeVariantReferenceTO reference) {
        return new IncludePartThemeVariantImpl(remoteServiceUtil, reference.path, reference.part, reference.theme);
    }
    
    public IncludePart getIncludePart() {
        return new IncludePartImpl(remoteServiceUtil, path, part);
    }
    
}
