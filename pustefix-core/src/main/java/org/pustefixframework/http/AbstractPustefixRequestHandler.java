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

package org.pustefixframework.http;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.pustefixframework.admin.mbeans.WebappAdmin;
import org.pustefixframework.config.contextxmlservice.ServletManagerConfig;
import org.pustefixframework.container.spring.http.UriProvidingHttpRequestHandler;
import org.pustefixframework.http.internal.PustefixInit;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.ServletContextAware;

import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.PfixServletRequestImpl;
import de.schlund.pfixxml.exceptionprocessor.ExceptionConfig;
import de.schlund.pfixxml.exceptionprocessor.ExceptionProcessingConfiguration;
import de.schlund.pfixxml.exceptionprocessor.ExceptionProcessor;
import de.schlund.pfixxml.serverutil.SessionAdmin;
import de.schlund.pfixxml.serverutil.SessionHelper;
import de.schlund.pfixxml.serverutil.SessionInfoStruct;
import de.schlund.pfixxml.serverutil.SessionInfoStruct.TrailElement;
import de.schlund.pfixxml.util.CookieUtils;
import de.schlund.pfixxml.util.MD5Utils;

/**
 * ServletManager.java
 *
 *
 * Created: Wed May  8 16:39:06 2002
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 */

public abstract class AbstractPustefixRequestHandler implements SessionStrategyContext, UriProvidingHttpRequestHandler, ServletContextAware, InitializingBean {

    public static final String           VISIT_ID                      = "__VISIT_ID__";
    public static final String           PROP_LOADINDEX                = "__PROPERTIES_LOAD_INDEX";
    
    
    
    
    
    
    
    
    public static final String           PROP_COOKIE_SEC_NOT_ENFORCED  = "servletmanager.cookie_security_not_enforced";
    public static final String           PROP_P3PHEADER                = "servletmanager.p3p";
    public static final String           PROP_SSL_REDIRECT_PORT        = "pfixcore.ssl_redirect_port.for.";
    protected static final String        DEF_CONTENT_TYPE              = "text/html";
    private static final String          DEFAULT_ENCODING              = "UTF-8";
    private static final String          SERVLET_ENCODING              = "servlet.encoding";
    
    
    
    public static Logger                       LOGGER_VISIT                  = Logger.getLogger("LOGGER_VISIT");
    private static Logger                       LOG                           = Logger.getLogger(AbstractPustefixRequestHandler.class);
    private String                       servletEncoding;
    private ServletContext servletContext;
    protected String handlerURI;
    private SessionAdmin sessionAdmin;
    private WebappAdmin webappAdmin;
    private ExceptionProcessingConfiguration exceptionProcessingConfig;
    private SessionTrackingStrategy sessionTrackingStrategy = new URLRewriteSessionStrategy(this);
    
    public abstract ServletManagerConfig getServletManagerConfig();

    public boolean needsSSL(PfixServletRequest preq) throws ServletException {
        return this.getServletManagerConfig().isSSL();
    }

    public abstract boolean needsSession();

    public abstract boolean allowSessionCreate();
    
    protected int validateRequest(HttpServletRequest req) {
        return 0;
    }

    public static String getServerName(HttpServletRequest req) {
        String forward = req.getHeader("X-Forwarded-Server");
        if (forward != null && !forward.equals("")) {
            return forward;
        } else {
            return req.getServerName();
        }
    }
    
    public void setHandlerURI(String uri) {
        this.handlerURI = uri;
    }
    
    public String[] getRegisteredURIs() {
        if(handlerURI != null) return new String[] { handlerURI };
        return new String[0];
    }

    public void handleRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        
        if(webappAdmin!=null) webappAdmin.refreshIfTriggered();
        
        req.setCharacterEncoding(servletEncoding);
        res.setCharacterEncoding(servletEncoding);
        if (LOG.isDebugEnabled()) {
            LOG.debug("\n ------------------- Start of new Request ---------------");
            LOG.debug("====> Scheme://Server:Port " + req.getScheme() + "://" + getServerName(req) + ":" + req.getServerPort());
            LOG.debug("====> URI:   " + req.getRequestURI());
            LOG.debug("====> Query: " + req.getQueryString());
            LOG.debug("----> needsSession=" + needsSession() + " allowSessionCreate=" + allowSessionCreate());
            LOG.debug("====> Sessions: " + sessionAdmin.toString());
            LOG.debug("\n");

            Enumeration<?> headers = req.getHeaderNames();

            while (headers.hasMoreElements()) {
                String header = (String) headers.nextElement();
                String headerval = req.getHeader(header);
                LOG.debug("+++ Header: " + header + " -> " + headerval);
            }

        }

        int httpStatus = validateRequest(req);
        if(validateRequest(req) >= 400) {
            res.sendError(httpStatus);
            if(LOG.isInfoEnabled()) LOG.info("Rejecting invalid request to path (" + httpStatus + "): " + req.getPathInfo());
            return;
        }
        
        // Set P3P-Header if needed to make sure it is 
        // set for every response (even redirects).
        String p3pHeader = getServletManagerConfig().getProperties().getProperty(PROP_P3PHEADER);
        if (p3pHeader != null && p3pHeader.length() > 0) {
            res.addHeader("P3P", p3pHeader);
        }
        
        sessionTrackingStrategy.handleRequestByStrategy(req, res);
        
    }
    
    
    public void afterPropertiesSet() throws Exception {
        init();
    }
    
    public void init() throws ServletException {
        ServletContext ctx = getServletContext();
        LOG.debug("*** Servlet container is '" + ctx.getServerInfo() + "'");
        int major = ctx.getMajorVersion();
        int minor = ctx.getMinorVersion();
        if ((major == 2 && minor >= 3) || (major > 2)) {
            LOG.warn("*** Servlet container with support for Servlet API " + major + "." + minor + " detected");
        } else {
            throw new ServletException("*** Can't detect servlet container with support for Servlet API 2.3 or higher");
        }
        
        initServletEncoding();
    }



    public void callProcess(PfixServletRequest preq, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            res.setContentType(DEF_CONTENT_TYPE);
            process(preq, res);
        } catch (Throwable e) {
            LOG.error("Exception in process", e);
            ExceptionConfig exconf = exceptionProcessingConfig.getExceptionConfigForThrowable(e.getClass());
            if(exconf != null && exconf.getProcessor()!= null) { 
                if ( preq.getLastException() == null ) {  
                    ExceptionProcessor eproc = exconf.getProcessor();
                    eproc.processException(e, exconf, preq,
                                       getServletContext(),
                                       req, res, this.getServletManagerConfig().getProperties());
                }
            } 
            if(!res.isCommitted()) throw new ServletException("Exception in process.",e);
        }
    }

    /**
     * Sets the servlet's encoding, which is used as character encoding for decoding/encoding 
     * requests/responses. Be aware that this setting only applies to the appropriate Readers, 
     * Writers and body request parameters. It has no effect on the byte streams. The URI
     * encoding (which is set on Tomcat connector level and can't be changed here) is set always
     * be the same as the body encoding.
     */
    private void initServletEncoding() {
        //Try to get servlet encoding from properties:
        String encoding = this.getServletManagerConfig().getProperties().getProperty(SERVLET_ENCODING);
        if (encoding == null || encoding.trim().equals(""))
            LOG.warn("No servlet encoding property set");
        else if (!Charset.isSupported(encoding))
            LOG.error("Servlet encoding '" + encoding + "' is not supported.");
        else
            servletEncoding = encoding;

        //Try to get servlet encoding from init parameters:
        if (servletEncoding == null) {
            encoding = getServletEncoding();
            if (encoding == null || encoding.trim().equals(""))
                LOG.warn("No servlet encoding init parameter set");
            else if (!Charset.isSupported(encoding))
                LOG.error("Servlet encoding '" + encoding + "' is not supported.");
            else
                servletEncoding = encoding;
        }
        //Use default servlet encoding:
        if (servletEncoding == null) {
            servletEncoding = DEFAULT_ENCODING;
            LOG.warn("Using default servlet encoding: " + DEFAULT_ENCODING);
        }

        LOG.debug("Servlet encoding was set to '" + servletEncoding + "'.");
    }

    protected abstract void process(PfixServletRequest preq, HttpServletResponse res) throws Exception;

    public static final int HTTP_PORT  = 80;
    public static final int HTTPS_PORT = 443;

    public static boolean isDefault(String scheme, int port) {
        if (scheme.equals("http") && port == HTTP_PORT) {
            return true;
        } else if (scheme.equals("https") && port == HTTPS_PORT) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Can be overridden by a subclass in order to disable the check
     * whether a session id provided by a request is valid.
     * 
     * @return <code>true</code> if and only if the request handler should
     * check whether the session id is valid for every request
     */
    public boolean wantsCheckSessionIdValid() {
        return true;
    }

    public static void relocate(HttpServletResponse res, String reloc_url) {
        relocate(res, HttpServletResponse.SC_MOVED_TEMPORARILY, reloc_url);
    }
    
    public static void relocate(HttpServletResponse res, int type, String reloc_url) {
        LOG.debug("\n\n        ======> relocating to " + reloc_url + "\n");
        res.setHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT");
        res.setHeader("Pragma", "no-cache");
        res.setHeader("Cache-Control", "no-cache, no-store, private, must-revalidate");
        res.setStatus(type);
        res.setHeader("Location", reloc_url);
    }
    
    public void setServletEncoding(String encoding) {
        this.servletEncoding = encoding;
    }
    
    public String getServletEncoding() {
        return servletEncoding;
    }
    
    public void setServletContext(ServletContext context) {
        this.servletContext = context;
    }
    
    public ServletContext getServletContext() {
        return this.servletContext;
    }
    
    public void setSessionAdmin(SessionAdmin sessionAdmin) {
        this.sessionAdmin = sessionAdmin;
    }
    
    public SessionAdmin getSessionAdmin() {
        return sessionAdmin;
    }
    
    public void setWebappAdmin(WebappAdmin webappAdmin) {
        this.webappAdmin = webappAdmin;
    }
    
    public void setExceptionProcessingConfiguration(ExceptionProcessingConfiguration exceptionProcessingConfig) {
        this.exceptionProcessingConfig = exceptionProcessingConfig;
    }
    
}
