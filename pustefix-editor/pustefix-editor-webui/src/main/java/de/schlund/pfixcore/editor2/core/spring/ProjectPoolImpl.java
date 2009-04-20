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

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.pustefixframework.editor.common.dom.Project;
import org.pustefixframework.editor.webui.remote.dom.ProjectImpl;
import org.pustefixframework.editor.webui.remote.dom.util.RemoteServiceUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.util.Xml;


public class ProjectPoolImpl implements ProjectPool {
    private LinkedHashMap<String, Project> locationToProject = new LinkedHashMap<String, Project>();
    private LinkedHashMap<Project, String> projectToLocation = new LinkedHashMap<Project, String>();
    private LinkedHashMap<Project, RemoteServiceUtil> projectToRemoteServiceUtil = new LinkedHashMap<Project, RemoteServiceUtil>();
    private Object mapsLock = new Object();
    
    public ProjectPoolImpl() {
        loadFromFile();
    }
    
    public Project getProjectForURI(String uri) {
        if (!uri.endsWith("/")) {
            uri = uri + "/";
        }
        synchronized (mapsLock) {
            return locationToProject.get(uri);
        }
    }
    
    public String getURIForProject(Project project) {
        synchronized (mapsLock) {
            return projectToLocation.get(project);
        }
    }
    
    public Collection<Project> getProjects() {
        synchronized (mapsLock) {
            return new LinkedList<Project>(projectToLocation.keySet());
        }
    }
    
    public RemoteServiceUtil getRemoteServiceUtil(Project project) {
        synchronized (mapsLock) {
            return projectToRemoteServiceUtil.get(project);
        }
    }
    
    public void reloadConfiguration() {
        synchronized (mapsLock) {
            locationToProject.clear();
            projectToLocation.clear();
            projectToRemoteServiceUtil.clear();
            loadFromFile();
        }
    }
    
    private void loadFromFile() {
        FileResource file = ResourceUtil.getFileResourceFromDocroot("WEB-INF/editor-locations.xml");
        Document doc;
        try {
            doc = Xml.parseMutable(file);
        } catch (IOException e) {
            throw new RuntimeException("Could not read project list from " + file, e);
        } catch (SAXException e) {
            throw new RuntimeException("Could not read project list from " + file, e);
        }
        Element docElement = doc.getDocumentElement();
        NodeList projectElements = docElement.getElementsByTagName("project");
        for (int i = 0; i < projectElements.getLength(); i++) {
            Element projectElement = (Element) projectElements.item(i);
            Element locationElement = (Element) projectElement.getElementsByTagName("location").item(0);
            if (locationElement == null) {
                throw new RuntimeException("Could not find location element within project element in file " + file);
            }
            String location = locationElement.getTextContent();
            Element secretElement = (Element) projectElement.getElementsByTagName("secret").item(0);
            if (secretElement == null) {
                throw new RuntimeException("Could not find secret element within project element in file " + file);
            }
            String secret = secretElement.getTextContent();
            LinkedList<String> aliasLocations = new LinkedList<String>();
            NodeList aliasElements = projectElement.getElementsByTagName("aliasLocation");
            for (int j= 0; j < aliasElements.getLength(); j++) {
                Element aliasElement = (Element) aliasElements.item(j);
                String aliasLocation = aliasElement.getTextContent();
                aliasLocations.add(aliasLocation);
            }
            registerProject(location, aliasLocations, secret);
        }
    }
    
    private void registerProject(String location, LinkedList<String> aliasLocations, String password) {
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        RemoteServiceUtil remoteServiceUtil = new RemoteServiceUtil(location, password);
        Project project = new ProjectImpl(remoteServiceUtil);
        locationToProject.put(location, project);
        for (String aliasLocation : aliasLocations) {
            locationToProject.put(aliasLocation, project);
        }
        projectToLocation.put(project, location);
        projectToRemoteServiceUtil.put(project, remoteServiceUtil);
    }
    
}
