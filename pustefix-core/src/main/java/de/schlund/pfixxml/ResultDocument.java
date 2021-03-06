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

package de.schlund.pfixxml;

import java.util.Properties;

import javax.xml.transform.dom.DOMResult;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.schlund.pfixcore.oxm.impl.MarshallerFactory;
import de.schlund.pfixcore.workflow.State;
import de.schlund.pfixxml.util.Xml;
import de.schlund.util.statuscodes.StatusCode;

/**
 * @author jtl
 *
 *
 */

public class ResultDocument {
    
    public static final String PFIXCORE_NS = "http://www.schlund.de/pustefix/core";
    public static final String IXSL_NS     = "http://www.w3.org/1999/XSL/Transform";
    
    protected Element    formresult;
    protected Element    formvalues;
    protected Element    formerrors;
    protected Element    formhiddenvals;
    protected Document   doc;
    protected SPDocument spdoc;
    private ModelAndView modelAndView;
    
    public ResultDocument() {
        
        spdoc     = new SPDocument();
        doc       = Xml.createDocument();
        spdoc.setDocument(doc);
        formresult = doc.createElement("formresult");
        formresult.setAttribute("xmlns:pfx", PFIXCORE_NS);
        formresult.setAttribute("serial", "" + spdoc.getTimestamp());
        doc.appendChild(formresult);

        formvalues = doc.createElement("formvalues");
        formresult.appendChild(formvalues);
        formerrors = doc.createElement("formerrors");
        formresult.appendChild(formerrors);
        formhiddenvals = doc.createElement("formhiddenvals");
        formresult.appendChild(formhiddenvals);
    }

    
    public void addUsedNamespace(String prefix, String uri) {
        formresult.setAttribute("xmlns:" + prefix, uri);
    }
    
    public SPDocument getSPDocument() {
        return spdoc;
    }
    
    public void setSPDocument(SPDocument spdoc) {
        this.spdoc = spdoc;
    }

    public Element getRootElement() {
        return formresult;
    }
    
    public void addValue(String name, String value) {
        if (value == null) return;
        Element param = doc.createElement("param");
        param.setAttribute("name", name);
        param.appendChild(doc.createTextNode(value));
        formvalues.appendChild(param);
    }

    public void addHiddenValue(String name, String value) {
        if (value == null) {
            return;
        }
        Element param = doc.createElement("hidden");
        param.setAttribute("name", name);
        param.appendChild(doc.createTextNode(value));
        formhiddenvals.appendChild(param);
    }

    public void addStatusCode(Properties props, StatusCode code, String[] args, String level, String field) {
        Element elem  = ResultDocument.createIncludeFromStatusCode(doc, props, code, args);
        Element param = doc.createElement("error");
        param.setAttribute("name", field);
        param.appendChild(elem);
        if (level != null) {
            param.setAttribute("level", level);
        }
        formerrors.appendChild(param);
    }

    public void setModelAndView(ModelAndView modelAndView) {
        if(modelAndView != null) {
            this.modelAndView = modelAndView;
            spdoc.setViewName(modelAndView.getViewName());
            spdoc.addAllObjects(modelAndView.getModel());
        }
    }

    /**
     * Returns the {@link ModelAndView} instance created by Spring MVC while processing {@link State}
     * methods with matching {@link RequestMapping} annotations.
     *
     * @return {@link ModelAndView} instance, or null if no {@link RequestMapping} used or matching.
     */
    public ModelAndView getModelAndView() {
        return modelAndView;
    }

    // -----------------------------------------------------------------
    
    public Element createNode(String name) {
        return createSubNode(formresult, name);
    }

    public Element createSubNode(Element el, String name) {
        Element node = doc.createElement(name);
        el.appendChild(node);
        return node;
    }

    public static Element addTextChild(Element element, String name, String text) {
        Document owner = element.getOwnerDocument();
        if (text == null) {
            return null;
        }
	
        Element tmp = owner.createElement(name);
        tmp.appendChild(owner.createTextNode(text));
        element.appendChild(tmp);
        return tmp;
    }

    public Element createIncludeFromStatusCode(Properties props, StatusCode code) {
        return createIncludeFromStatusCode(doc, props, code, null);
    }

    public Element createIncludeFromStatusCode(Properties props, StatusCode code, String[] args) {
        return createIncludeFromStatusCode(doc, props, code, args);
    }

    public static Element createIncludeFromStatusCode(Document thedoc, Properties props, StatusCode code, String[] args) {
        //TODO: support statuscodes from arbitrary resources
        String incfile = null;
        String module = null;
        boolean dynamic = false;
        if("dynamic".equals(code.getStatusCodeURI().getScheme())) {
            incfile = code.getStatusCodeURI().getPath();
            module = code.getStatusCodeURI().getAuthority();
            dynamic = true;
        } else {
            incfile = code.getStatusCodeURI().toASCIIString();
        }
        String part    = code.getStatusCodeId();
        Element include = thedoc.createElementNS(ResultDocument.PFIXCORE_NS, "pfx:include");
        include.setAttribute("href", incfile);
        include.setAttribute("part", part);
        if(module != null) {
            include.setAttribute("module", module);
        }
        if(dynamic) {
            include.setAttribute("search", "dynamic");
        }
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                Element arg   = thedoc.createElementNS(ResultDocument.PFIXCORE_NS, "pfx:arg");
                arg.setAttribute("value", args[i]);
                include.appendChild(arg);
            }
        }
        return include;
    }
    
    public static Element addTextIncludeChild(Element element, String name,
                                              String incfile, String part) {
        Document owner = element.getOwnerDocument();
        
        Element include = owner.createElementNS(PFIXCORE_NS, "pfx:include");
        include.setAttribute("href", incfile);
        include.setAttribute("part", part);
	
        Element tmp = owner.createElement(name);
        tmp.appendChild(include);
        element.appendChild(tmp);
        return tmp;
    }
    
    public static Element addObject(Element element, Object object) {
        DOMResult result = new DOMResult(element);
        MarshallerFactory.getMarshaller(object).marshal(object, result);
        return element;
    }
    
    public static Element addObject(Element element, String name, Object object) {
        Document owner = element.getOwnerDocument();
        Element tmp = owner.createElement(name);
        element.appendChild(tmp);
        addObject(tmp, object);
        return tmp;
    }
    
}
