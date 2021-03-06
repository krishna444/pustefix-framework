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

import java.io.File;
import java.net.URL;

import org.pustefixframework.live.LiveResolver;

import de.schlund.pfixxml.config.GlobalConfig;
import de.schlund.pfixxml.resources.ModuleSourceResource;
import de.schlund.pfixxml.resources.Resource;
import de.schlund.pfixxml.resources.ResourceUtil;

/**
 * Implementation using {@link de.schlund.pfixxml.config.GlobalConfig} to get
 * Pustefix docroot
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class PathResolverServiceImpl implements PathResolverService {
    private String docroot;
    private LiveResolver live;

    /**
     * Constructor makes use of <code>PathFactory</code> to get docroot.
     */
    public PathResolverServiceImpl() {
        docroot = GlobalConfig.getDocroot();
        live = new LiveResolver();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.spring.PathResolverService#resolve(java.lang.String)
     */
    public File resolve(String path) {
        URL url;

        if (path.startsWith("docroot:")) {
            path = path.substring(9);
        } else if (path.startsWith("module:")) {
            Resource res = ResourceUtil.getResource(path);
            if(res != null && res instanceof ModuleSourceResource){
                return ((ModuleSourceResource)res).getFile();
            }
            return null;
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        try {
            url = live.resolveLiveDocroot(docroot, path);
        } catch (Exception e) {
            throw new RuntimeException("TODO", e);
        }
        if (url != null) {
            if (url.getProtocol().equals("file")) {
                return new File(url.getFile() + path);
            } else {
                throw new IllegalStateException("file protocol expected, got " + url.getProtocol());
            }
        } else {
            return new File(docroot + path);
        }
        
    }
}
