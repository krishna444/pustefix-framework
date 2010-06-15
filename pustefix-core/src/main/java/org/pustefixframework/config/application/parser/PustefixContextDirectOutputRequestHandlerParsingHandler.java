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

package org.pustefixframework.config.application.parser;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.pustefixframework.config.Constants;
import org.pustefixframework.config.customization.CustomizationAwareParsingHandler;
import org.pustefixframework.config.customization.CustomizationInfo;
import org.pustefixframework.config.directoutputservice.DirectOutputRequestHandlerConfigHolder;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.http.PustefixContextDirectOutputRequestHandler;
import org.pustefixframework.resource.InputStreamResource;
import org.pustefixframework.resource.ResourceLoader;
import org.pustefixframework.util.io.StreamUtils;
import org.pustefixframework.util.xml.NamespaceUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.osgi.context.ConfigurableOsgiBundleApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.marsching.flexiparse.objecttree.ObjectTreeElement;
import com.marsching.flexiparse.objecttree.SubObjectTree;
import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.OSGiAwareParser;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixcore.workflow.ContextImpl;
import de.schlund.pfixxml.serverutil.SessionAdmin;

public class PustefixContextDirectOutputRequestHandlerParsingHandler extends CustomizationAwareParsingHandler {

    private static String DEPRECATED_CONFIG_NS = "http://www.pustefix-framework.org/2008/namespace/direct-output-service-config";
    private static String CONFIG_NS = "http://www.pustefix-framework.org/2009/namespace/direct-output-service-config";
    
    private static Logger LOG = Logger.getLogger(PustefixContextDirectOutputRequestHandlerParsingHandler.class);
    
    @Override
    public void handleNodeIfActive(HandlerContext context) throws ParserException {
        ResourceLoader resourceLoader = ParsingUtils.getSingleTopObject(ResourceLoader.class, context);

        Element serviceElement = (Element) context.getNode();
        Element pathElement = (Element) serviceElement.getElementsByTagNameNS(Constants.NS_APPLICATION, "path").item(0);
        if (pathElement == null) {
            throw new ParserException("Could not find expected <path> element");
        }
        Element configurationFileElement = (Element) serviceElement.getElementsByTagNameNS(Constants.NS_APPLICATION, "config-file").item(0);
        if (configurationFileElement == null) {
            throw new ParserException("Could not find expected <config-file> element");
        }
        String path = pathElement.getTextContent().trim();
        String configurationFile = configurationFileElement.getTextContent().trim();
        URI configurationURI;
        try {
            configurationURI = new URI(configurationFile);
        } catch (URISyntaxException e) {
            throw new ParserException("Not a valid URI: " + configurationFile, e);
        }
        InputStreamResource configurationResource = resourceLoader.getResource(configurationURI, InputStreamResource.class);
        if (configurationResource == null) {
            throw new ParserException("Resource at URI \"" + configurationURI.toASCIIString() + "\" could not be found");
        }

        Collection<CustomizationInfo> infoCollection = context.getObjectTreeElement().getObjectsOfTypeFromTopTree(CustomizationInfo.class);
        if (infoCollection.isEmpty()) {
            throw new ParserException("Could not find instance of CustomizationInfo");
        }
        CustomizationInfo info = infoCollection.iterator().next();
        
        BeanDefinitionRegistry registry = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);

        ConfigurableOsgiBundleApplicationContext appContext = ParsingUtils.getSingleTopObject(ConfigurableOsgiBundleApplicationContext.class, context);
        
        OSGiAwareParser configParser = new OSGiAwareParser(appContext.getBundleContext(), "META-INF/org/pustefixframework/config/direct-output-service/parser/direct-output-service-config-application.xml");
        
        InputStream in = null;
        try {
            String namespace = NamespaceUtils.getNamespace(configurationResource.getInputStream());
            if(DEPRECATED_CONFIG_NS.equals(namespace)) {
                LOG.warn("Direct output configuration uses deprecated namespace '" + DEPRECATED_CONFIG_NS + "'. " +
                         "You should replace it by '" + CONFIG_NS +"'.");
                LOG.warn("Trying to continue by replacing the namespace on the fly.");
                String content = StreamUtils.load(configurationResource.getInputStream(), "utf-8");
                content = content.replaceAll(DEPRECATED_CONFIG_NS, CONFIG_NS);
                in = new ByteArrayInputStream(content.getBytes("utf8"));
            }
        } catch(Exception x) {
            throw new ParserException("Error checking namespace", x);
        }
        
        final ObjectTreeElement root;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setXIncludeAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            if(in == null) in = configurationResource.getInputStream();
            Document doc = db.parse(in); 
            root = configParser.parse(doc, info, registry, appContext, resourceLoader);
        } catch (FileNotFoundException e) {
            throw new ParserException("Could not find referenced configuration file: " + configurationFile, e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Could not initialize XML parser: " + e.getMessage(), e);
        } catch (SAXException e) {
            throw new ParserException("Error while parsing referenced file: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new ParserException("Error while reading file: " + e.getMessage(), e);
        }
        SubObjectTree subtree = new SubObjectTree() {

            public ObjectTreeElement getRoot() {
                return root;
            }
            
        };
        context.getObjectTreeElement().addObject(subtree);
        
        DirectOutputRequestHandlerConfigHolder configHolder = root.getObjectsOfTypeFromSubTree(DirectOutputRequestHandlerConfigHolder.class).iterator().next();
        
        BeanNameGenerator nameGenerator = new DefaultBeanNameGenerator();
        BeanDefinitionBuilder beanBuilder;
        BeanDefinition beanDefinition;
        
        beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(PustefixContextDirectOutputRequestHandler.class);
        beanBuilder.setScope("singleton");
        beanBuilder.setInitMethodName("init");
        beanBuilder.addPropertyValue("handlerURI", path + "/**");
        beanBuilder.addPropertyValue("context", new RuntimeBeanReference(ContextImpl.class.getName()));
        beanBuilder.addPropertyValue("configuration", configHolder.getDirectOutputServiceConfigObject());
        beanBuilder.addPropertyValue("sessionAdmin", new RuntimeBeanReference(SessionAdmin.class.getName()));
        beanDefinition = beanBuilder.getBeanDefinition();
        registry.registerBeanDefinition(nameGenerator.generateBeanName(beanDefinition, registry), beanDefinition);
    }

}