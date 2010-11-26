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
package de.schlund.pfixxml.resources;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.schlund.pfixcore.util.ModuleInfo;
import de.schlund.pfixxml.IncludeDocument;
import de.schlund.pfixxml.IncludeDocumentFactory;
import de.schlund.pfixxml.util.URIParameters;
import de.schlund.pfixxml.util.XPath;
import de.schlund.pfixxml.util.XsltProvider;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class DynamicResourceProvider implements ResourceProvider {
    
    private Logger LOG = Logger.getLogger(DynamicResourceProvider.class);
    
    private static String DYNAMIC_SCHEME = "dynamic";
    private static String[] supportedSchemes = {DYNAMIC_SCHEME};
    
    public String[] getSupportedSchemes() {
        return supportedSchemes;
    }
    
    public Resource getResource(URI uri) throws ResourceProviderException {
        Resource res = getResource(uri, null);
        DynamicResourceInfo info = new DynamicResourceInfo(uri);
        
        Resource res2 = getResource(uri, info);
        System.out.println("--------------");
        System.out.println(info);
        
        if(!res.toURI().equals(res2.toURI())) throw new RuntimeException("XXXXXXXXXXXXXXXX: " + res.toURI() + " " +res2.toURI());
        return res;
        //return getResource(uri, null);
    }
        
    public Resource getResource(URI uri, DynamicResourceInfo info) throws ResourceProviderException {
        if(uri.getScheme()==null) 
            throw new ResourceProviderException("Missing URI scheme: "+uri);
        if(!uri.getScheme().equals(DYNAMIC_SCHEME)) 
            throw new ResourceProviderException("URI scheme not supported: "+uri);
        
        URIParameters params;
        try {
            params = new URIParameters(uri.getQuery(), "utf-8");
        } catch(Exception x) {
            throw new ResourceProviderException("Error reading URI parameters: "+uri.toString(), x);
        }
        String module = params.getParameter("module");
        if(module == null) module = uri.getAuthority();
        String part = params.getParameter("part");
        String themes[] = null;
        if(params.getParameter("themes")!=null) {
            themes = params.getParameter("themes").split(",");
        }
        if(themes==null) themes = new String[] {""};
        
        Resource infoRes = null;
        
        //search in local project
        for(String theme:themes) {
            try {
                String uriPath = uri.getPath();
                uriPath = uriPath.replace("THEME", theme);
                URI  prjUri = new URI("docroot:"+uriPath);
                if(LOG.isDebugEnabled()) LOG.debug("trying "+prjUri.toString());
                Resource resource = ResourceUtil.getResource(prjUri);
                if(resource.exists()) {
                    resource.setOriginatingURI(uri);
                    if(part == null) {
                        if(info == null) return resource;
                        else {
                            if(infoRes == null) infoRes = resource;
                            info.addEntry(prjUri, true, false);
                        }
                    } else if(containsPart(resource, part)) {
                        if(info == null) return resource;
                        else {
                            if(infoRes == null) infoRes = resource;
                            info.addEntry(prjUri, true, true);
                        }
                    } else if(info != null) info.addEntry(prjUri, true, false);
                } else if(info != null) info.addEntry(prjUri, false, false);
            } catch(URISyntaxException x) {
                throw new ResourceProviderException("Error while searching project resource: " + uri, x);
            }
        }
        
        ModuleInfo moduleInfo = ModuleInfo.getInstance();
        String path = uri.getPath();
        if(path.startsWith("/")) path = path.substring(1);
        
        //search in defaultSearchModules
        List<String> defaultSearchModules = moduleInfo.getDefaultSearchModules();
        for(String theme:themes) {
            for(String defaultSearchModule: defaultSearchModules) {
                try {
                    String uriPath = uri.getPath();
                    uriPath = uriPath.replace("THEME", theme);
                    URI modUri = new URI("module://" + defaultSearchModule + uriPath);
                    if(LOG.isDebugEnabled()) LOG.debug("trying "+modUri.toString());
                    Resource resource = ResourceUtil.getResource(modUri);
                    if(resource.exists()) {
                        resource.setOriginatingURI(uri);
                        if(part==null) {
                            if(info == null) return resource;
                            else {
                                if(infoRes == null) infoRes = resource;
                                info.addEntry(modUri, true, false);
                            }
                        } else if(containsPart(resource, part)) {
                            if(info == null) return resource;
                            else {
                                if(infoRes == null) infoRes = resource;
                                info.addEntry(modUri, true,true);
                            }
                        } else if(info != null) info.addEntry(modUri, true, false);
                    } else if(info != null) info.addEntry(modUri, false, false);
                } catch(URISyntaxException x) {
                    throw new ResourceProviderException("Error while searching defaultsearch module resource: " + uri, x);
                }
            }
        }
        
        if(module != null) {

            //search in overriding modules
            List<String> overMods = moduleInfo.getOverridingModules(module, path);
            if(overMods.size()>1) {
                LOG.warn("Multiple modules found which override resource '"+path+"' from module '"+module+"'.");
            }
            for(String theme:themes) {
                for(String overMod:overMods) {
                    try {
                        String uriPath = uri.getPath();
                        uriPath = uriPath.replace("THEME", theme);
                        URI modUri = new URI("module://" + overMod + uriPath);
                        if(LOG.isDebugEnabled()) LOG.debug("trying "+modUri.toString());
                        Resource resource = ResourceUtil.getResource(modUri);
                        if(resource.exists()) {
                            resource.setOriginatingURI(uri);
                            if(part==null) {
                                if(info == null) return resource;
                                else {
                                    if(infoRes == null) infoRes = resource;
                                    info.addEntry(modUri, true, false);
                                }
                            } else if(containsPart(resource, part)) {
                                if(info == null) return resource;
                                else {
                                    if(infoRes == null) infoRes = resource;
                                    info.addEntry(modUri, true, true);
                                }
                            } else if(info != null) info.addEntry(modUri, true, false);
                        } else if(info != null) info.addEntry(modUri, false, false);
                    } catch(URISyntaxException x) {
                        throw new ResourceProviderException("Error while searching overrided module resource: " + uri, x);
                    }
                }
            }
            
            //use resource from specified module
            for(String theme:themes) {
                try {
                    String uriPath = uri.getPath();
                    uriPath = uriPath.replace("THEME", theme);
                    URI modUri = new URI("module://" + module + uriPath);
                    if(LOG.isDebugEnabled()) LOG.debug("trying "+modUri.toString());
                    Resource resource = ResourceUtil.getResource(modUri);
                    if(resource.exists()) {
                        resource.setOriginatingURI(uri);
                        if(info == null) return resource;
                        else {
                            if(infoRes == null) infoRes = resource;
                            if(part != null && containsPart(resource, part)) info.addEntry(modUri, true, true);
                            else info.addEntry(modUri, true, false);
                        }
                    } else if(info != null) info.addEntry(modUri, false, false);
                } catch(URISyntaxException x) {
                    throw new ResourceProviderException("Error while getting module resource: " + uri, x);
                }
            }
        
        }
        
        if(infoRes != null) {
            info.setResolvedURI(infoRes.toURI());
            return infoRes;
        }
        
        //Return non-existing project resource if search failed
        try {
            URI  prjUri = new URI("docroot:"+uri.getPath());
            Resource resource = ResourceUtil.getResource(prjUri);
            return resource;
        } catch(URISyntaxException x) {
            throw new ResourceProviderException("Error while getting project resource: " + uri, x);
        }
        
    }
 
    private boolean containsPart(Resource res, String part) throws ResourceProviderException {
        try {
            IncludeDocument incDoc = IncludeDocumentFactory.getInstance().getIncludeDocument(XsltProvider.getPreferredXsltVersion(), res, false);
            Document doc = incDoc.getDocument();
            List<Node> ns = XPath.select(doc, "/include_parts/part[@name='" + part + "']");
            if(ns.size()>0) return true;
            return false;
        } catch (Exception x) {
            throw new ResourceProviderException("Error while searching part in document: " + res.toURI(), x);
        }
    }
    
}
