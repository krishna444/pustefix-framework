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
 *
 */

package de.schlund.pfixxml;

import de.schlund.pfixxml.config.ServletManagerConfig;
import de.schlund.pfixxml.serverutil.SessionHelper;
import de.schlund.pfixxml.util.MD5Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.axis.encoding.Base64;
import org.apache.log4j.Category;

/**
 * This class implements a "Dereferer" servlet to get rid of Referer
 * headers. <b>ALL LINKS THAT GO TO AN OUTSIDE DOMAIN MUST USE THIS SERVLET!</b>
 * If this servlet is bound to e.g. /xml/deref than every outside link
 * (say to http://www.gimp.org) must be written like <a href="/xml/deref?link=http://www.gimp.org">Gimp</a>
 *
 */

public class DerefServer extends ServletManager {
    protected static Category DEREFLOG        = Category.getInstance("LOGGER_DEREF");
    protected static Category CAT             = Category.getInstance(DerefServer.class);
    public static String      PROP_DEREFKEY   = "derefserver.signkey";
    public static String      PROP_IGNORESIGN = "derefserver.ignoresign";
    private ServletManagerConfig config;
    
    protected boolean allowSessionCreate() {
        return (false);
    }

    protected boolean needsSession() {
        return (false);
    }

    public static String signString(String input, String key) {
        return MD5Utils.hex_md5(input+key, "utf8");
    }

    public static boolean checkSign(String input, String key, String sign) {
        if (input == null || sign == null) {
            return false;
        }
        return MD5Utils.hex_md5(input+key, "utf8").equals(sign);
    }
    
    protected void process(PfixServletRequest preq, HttpServletResponse res) throws Exception {
        RequestParam linkparam    = preq.getRequestParam("link");
        RequestParam enclinkparam = preq.getRequestParam("enclink");
        RequestParam signparam    = preq.getRequestParam("sign");
        String       key          = config.getProperties().getProperty(PROP_DEREFKEY);
        String       ign          = config.getProperties().getProperty(PROP_IGNORESIGN);

        // This is currently set to true by default for backward compatibility. 
        boolean ignoresign = true;
        if (ign != null && ign.equals("false")) {
            ignoresign = false;
        }

        HttpServletRequest req     = preq.getRequest();
        String             referer = req.getHeader("Referer");

        CAT.debug("===> sign key: " + key);
        CAT.debug("===> Referer: " + referer);
        
        if (linkparam != null && linkparam.getValue() != null) {
            CAT.debug(" ==> Handle link: " + linkparam.getValue());
            if (signparam != null && signparam.getValue() != null) {
                CAT.debug("     with sign: " + signparam.getValue());
            }
            handleLink(linkparam.getValue(), signparam, ignoresign, preq, res, key);
            return;
        } else if (enclinkparam != null && enclinkparam.getValue() != null &&
                   signparam != null && signparam.getValue() != null) {
            CAT.debug(" ==> Handle enclink: " + enclinkparam.getValue());
            CAT.debug("     with sign: " + signparam.getValue());
            handleEnclink(enclinkparam.getValue(), signparam.getValue(), preq, res, key);
            return;
        } else {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
    }


    private void handleLink(String link, RequestParam signparam, boolean ignoresign,
                            PfixServletRequest preq, HttpServletResponse res, String key) throws Exception {
        boolean checked = false;
        boolean signed  = false;
        if  (signparam != null && signparam.getValue() != null) {
            signed = true;
        }
        if (signed && checkSign(link, key, signparam.getValue())) {
            checked = true;
        }

        // We don't currently enforce the signing at this stage. We may change this to enforcing mode,
        // or maybe we will use some clear warning pages in the case of a not signed request.
        if (checked || (!signed && ignoresign)) {
            OutputStream       out      = res.getOutputStream();
            OutputStreamWriter writer   = new OutputStreamWriter(out, res.getCharacterEncoding());
            String             enclink  = Base64.encode(link.getBytes("utf8"));
            String             reallink = preq.getScheme() + "://" + preq.getServerName() + ":" + preq.getServerPort() +
                SessionHelper.getClearedURI(preq) + "?enclink=" + URLEncoder.encode(enclink, "utf8") +
                "&sign=" + signString(enclink, key);
            
            CAT.debug("===> Meta refresh to link: " + reallink);
            
            writer.write("<html><head>");
            writer.write("<meta http-equiv=\"refresh\" content=\"0; URL=" + reallink +  "\">");
            writer.write("</head><body bgcolor=\"#ffffff\"><center>");
            writer.write("<a style=\"color:#cccccc;\" href=\"" + reallink + "\">" + "-> Redirect ->" + "</a>");
            writer.write("</center></body></html>");
            writer.flush();
        } else {
            CAT.warn("===> No meta refresh because signature is wrong.");
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
    }

    private void handleEnclink(String enclink, String sign, PfixServletRequest preq, HttpServletResponse res, String key) throws Exception {
        if (checkSign(enclink, key, sign)) {
            String link = new String( Base64.decode(enclink), "utf8");
            if (link.startsWith("/")) {
                link = preq.getScheme() + "://" + preq.getServerName() + ":" + preq.getServerPort() + link;
            }
            CAT.debug("===> Relocate to link: " + link);
            DEREFLOG.info(preq.getServerName() + "|" + link + "|" + preq.getRequest().getHeader("Referer"));
            res.setHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT");
            res.setHeader("Pragma", "no-cache");
            res.setHeader("Cache-Control", "no-cache, no-store, private, must-revalidate");
            res.setHeader("Location", link);
            res.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
        } else {
            CAT.warn("===> Won't relocate because signature is wrong.");
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
    }

    protected ServletManagerConfig getServletManagerConfig() {
        return this.config;
    }

    protected void reloadServletConfig(File configFile, Properties globalProperties) throws ServletException {
        // Deref server does not use a servlet specific configuration
        // So simply initialize configuration with global properties
        ServletManagerConfig sConf = new ServletManagerConfig();
        sConf.setProperties(globalProperties);
        sConf.setSSL(false);
        this.config = sConf;
    }
}
