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

package de.schlund.pfixcore.util;


import de.schlund.pfixxml.util.XPath;
import de.schlund.pfixxml.util.Xml;
import de.schlund.util.statuscodes.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import org.apache.log4j.*;
import org.w3c.dom.*;

/**
 *
 *
 */

public class MakeStatusMessageXML {
    private static       Category               CAT              = Category.getInstance(MakeStatusMessageXML.class.getName());
    private static final String                 INCPARTS         = "include_parts";
    private static final String                 PART             = "part";
    private static final String                 DEFPROD          = "product";
    private static final String                 DEFLANG          = "lang";
    private static final String                 NULL_WS          = "\n";
    private static final String                 SINGLE_WS_NOWRAP = "  ";
    private static final String                 SINGLE_WS        = "\n  ";
    private static final String                 DOUBLE_WS        = "\n    ";
    private static final String                 TRIPLE_WS        = "\n      ";
    private static final String                 WARNING          = "NEVER CHANGE THIS NODE!";

    private static final boolean                do_remove        = false; 
    private static final int                    ERR_NOMSGFILE    = -1;    
    private static final int                    ERR_NOPROPFILE   = -2;
    private static final int                    ERR_LOAD         = -3;
    private static final int                    ERR_PARSE        = -4;
    private static final int                    ERR_SER          = -5;
    private static final int                    ERR_NOSCODE      = -6; 
    private static final int                    ERR_NOTVALID     = -7;
    private static final int                    ERR_REMNA        = -8;

    public static void main(String args[]) throws IOException {
        MakeStatusMessageXML maker = new MakeStatusMessageXML();
        StatusCodeFactory    scfac = new StatusCodeFactory();
        Properties           props = new Properties();
        
        // init von log4j
        BasicConfigurator.configure();
        CAT.debug(">>> MakeStatusMessageXML - update statusmessage xml-file <<<");
        
        String messagefile = args[0];
        if (messagefile == null) {
            System.err.println("Need messagefile to write statuscodes into. Exiting....");
            System.exit(ERR_NOMSGFILE);
        }
        
        String propertyfile = System.getProperty("propertyfile"); 
        if (propertyfile == null) {
            System.err.println("Need propertyfile to get statuscode files from. Exiting....");
            System.exit(ERR_NOPROPFILE);
        }

        try {
            props.load(new FileInputStream(propertyfile));
        } catch (Exception e) {
            CAT.error("Couldn't load propertyfile " + propertyfile + "\n" + e.toString());
            System.exit(ERR_LOAD);
        }

        File    tsfile  = new File(messagefile + ".TIMESTAMP");
        long    lastmod = maker.needToGenerate(props, tsfile);

        if (lastmod != 0) {
            try {
                scfac.init(props);
            } catch (Exception e) {
                CAT.error("Couldn't init Factory\n" + e.toString());
                System.exit(ERR_LOAD);
            }

            TreeSet allscodes = new TreeSet(scfac.getAllSCodes().keySet());
            
            Document        doc               = null;
            Element         incroot           = null;
            File            statusmessagefile = null;
            boolean         filecreate        = false;
            
            // try to open and parse an existing statusmessage file or create a new dom tree
            statusmessagefile = new File(messagefile);
            if (statusmessagefile.exists()) {
                try {
                    // try to parse the existing statusmessage xml-file
                    // load statusmessages xml file
                    CAT.warn( ">>> parsing statusmessages xml-file ...");
                    doc = Xml.parse(statusmessagefile);
                    maker.updateFile(scfac, allscodes, doc, messagefile);
                } catch (Exception e) {
                    CAT.error("FATAL1: " + e.toString());
                    System.exit(ERR_PARSE);
                }
            } else {   
                // if not found -> generate one
                doc = Xml.createDocument();
                maker.createFile(scfac, allscodes, doc, messagefile);
            }

            tsfile.setLastModified(lastmod);
        }
    }
        
    protected void createFile(StatusCodeFactory scfac, TreeSet allscodes, Document doc, String filename) {
        CAT.warn(">>> Creating NEW messagefile " + filename);
        Element root = doc.createElement(INCPARTS);
        root.setAttribute("xmlns:ixsl", "http://www.w3.org/1999/XSL/Transform");
        root.setAttribute("xmlns:pfx",  "http://www.schlund.de/pustefix/core");
        doc.appendChild(root);
        for (Iterator i = allscodes.iterator(); i.hasNext(); ) {
            String     scodename = (String) i.next();
            StatusCode scode     = scfac.getStatusCode(scodename);
            String     message   = scode.getDefaultMessage();

            Element part = createNewPart(doc, scodename, message, false);
            root.appendChild(part);
        }
        writeFile(filename, doc, true);
    }

    protected void updateFile(StatusCodeFactory scfac, TreeSet allscodes,
                              Document doc, String filename) throws Exception {

        NodeList  allpartsnl = doc.getElementsByTagName("part");
        Node[]    allparts   = new Node[allpartsnl.getLength()];
        for (int i = 0; i < allparts.length; i++) {
            allparts[i] = allpartsnl.item(i);
        }
        Arrays.sort(allparts, new PartNodeComparator());
        
        Iterator iter   = allscodes.iterator();
        String   scname = (String) iter.next();

        for (int i = 0; i < allparts.length; i++) {
            Element part  = (Element) allparts[i];
            String  pname = part.getAttribute("name");

            // CAT.debug("Looking at part " + pname + " and scode " + scname);
            if (pname.equals(scname)) {   // equal
                updatePart(part, scfac);
                if (iter.hasNext() && i < (allparts.length - 1)) { // make sure we are not at the last part already
                    scname = (String) iter.next();
                }
            } else if (pname.compareTo(scname) < 0) { // smaller
                removePart(part);
            } else if (pname.compareTo(scname) > 0) { // bigger
                insertNewBefore(scname, doc, part, scfac);
                if (iter.hasNext()) {
                    scname = (String) iter.next();
                    i--; // loop again with current part
                }
            }
        }
        for (; iter.hasNext(); ) { // handle all remaining statuscodes
            scname = (String) iter.next();
            insertNewAtEnd(scname, doc, scfac);
        }
        writeFile(filename, doc, false);
    }

    private void writeFile(String filename, Document doc, boolean newfile) {
        try {
            if (newfile) {
                CAT.warn(">>> Writing NEW messagefile " + filename);
            } else {
                CAT.warn(">>> Writing messagefile " + filename + " back");
            }
            Xml.serialize(doc, filename, newfile, true);
        } catch (Exception e) {
            CAT.error("FATAL2: " + e.toString());
            System.exit(ERR_SER);
        }
    }

    private void insertNewAtEnd(String scname, Document doc, StatusCodeFactory scfac) {
        CAT.warn("*** Inserting NEW part " + scname + " at end of Messagefile");
        StatusCode scode   = scfac.getStatusCode(scname);
        Element    newpart = createNewPart(doc, scname, scode.getDefaultMessage(), true);
        doc.getDocumentElement().appendChild(doc.createTextNode(SINGLE_WS_NOWRAP));
        doc.getDocumentElement().appendChild(newpart);
        doc.getDocumentElement().appendChild(doc.createTextNode(NULL_WS));
    }
    
    private void insertNewBefore(String scname, Document doc, Element part, StatusCodeFactory scfac) {
        CAT.warn("*** Inserting NEW part " + scname + " before part " + part.getAttribute("name"));
        StatusCode scode   = scfac.getStatusCode(scname);
        Element    newpart = createNewPart(doc, scname, scode.getDefaultMessage(), true);
        doc.getDocumentElement().insertBefore(newpart, part);
        doc.getDocumentElement().insertBefore(doc.createTextNode(SINGLE_WS), part);
    }
    
    private void updatePart(Element part, StatusCodeFactory scfac) throws Exception {
        String     name    = part.getAttribute("name");
        // CAT.debug("*** Updating part " + name);
        StatusCode scode   = scfac.getStatusCode(name);
        if (scode == null) {
            CAT.error("No Scode for Part: " + name);
            System.exit(ERR_NOSCODE);
        }
        Element    deflang = (Element) XPath.selectNode(part, "./product/lang[@name = 'default']");
        List   nl      = XPath.select(deflang, "./node()");
        for (int i = 0; i < nl.size(); i++) {
            Node node = (Node) nl.get(i);
            // CAT.warn("----> " + node.getNodeName() + " => " + node.getNodeValue());
            deflang.removeChild(node);
        }
        deflang.setAttribute("warning", WARNING);
        addMessageToLangElem(deflang, scode.getDefaultMessage());
    }

    private void removePart(Element part) {
        CAT.warn("*** Part " + part.getAttribute("name") + " is no longer defined in the statuscodes.\n" +
                 "*** Trying to remove it...");
        if (do_remove) {
            Node next = part.getNextSibling();
            if (next.getNodeType() == Node.TEXT_NODE) {
                part.getOwnerDocument().getDocumentElement().removeChild(next);
            }
            part.getOwnerDocument().getDocumentElement().removeChild(part);
        } else {
            CAT.error("*** I'm not allowed to automatically remove parts...Please do it by hand. Exiting...");
            System.exit(ERR_REMNA);
        }
    }

    private Element createNewPart(Document doc, String name, String message, boolean addwhite) {
        Element part    = doc.createElement(PART);
        Element defprod = doc.createElement(DEFPROD);
        defprod.setAttribute("name", "default");
        Element deflang = doc.createElement(DEFLANG);
        deflang.setAttribute("name", "default");
        deflang.setAttribute("warning", WARNING);
        part.setAttribute("name", name);
        // Text     text    = doc.createTextNode(message);
        if (addwhite) {
            part.appendChild(doc.createTextNode(DOUBLE_WS));
        }
        part.appendChild(defprod);
        if (addwhite) {
            part.appendChild(doc.createTextNode(SINGLE_WS));
        }
        if (addwhite) {
            defprod.appendChild(doc.createTextNode(TRIPLE_WS));
        }
        defprod.appendChild(deflang);
        if (addwhite) {
            defprod.appendChild(doc.createTextNode(DOUBLE_WS));
        }
        // deflang.appendChild(text);
        addMessageToLangElem(deflang, message);
        return part;
    }

    private void addMessageToLangElem(Element langnode, String message) {
        Document doc     = langnode.getOwnerDocument(); 
        String   text = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
            "<foo xmlns:pfx=\"http://www.schlund.de/pustefix/core\">" + message + "</foo>";
        NodeList impelems = null;
        try {
            impelems = Xml.parseString(text).getDocumentElement().getChildNodes();
        } catch (Exception exp) {
            CAT.error("*** " + message + " *** " + exp.toString());
            System.exit(ERR_NOTVALID);
        }
        for (int i = 0; i < impelems.getLength(); i++) {
            Node node = impelems.item(i);
            langnode.appendChild(doc.importNode(node, true));
        }
    }

    private class PartNodeComparator implements Comparator {
        public int compare(Object param1, Object param2) {
            if (!(param1 instanceof Node && param2 instanceof Node)) {
                return 0;
            }
            Element node1 = (Element) param1;
            Element node2 = (Element) param2;
            
            String name1 = node1.getAttribute("name");
            String name2 = node2.getAttribute("name");
            return name1.compareTo(name2);
        }
    }// PartNodeComparator


    private long needToGenerate(Properties props, File tsfile) throws IOException {
        long    timestamp;
        long    max = 0l;
        
        if (!tsfile.exists()) {
            tsfile.createNewFile();
            timestamp = 0l;
        } else {
            timestamp = tsfile.lastModified();
        }
        HashMap propfiles = selectProperties(props,  "statuscodefactory.propertyfile");
        URL     url;
        
        for (Iterator i = propfiles.values().iterator(); i.hasNext(); ) {
            String name = (String) i.next();
            if (!name.startsWith("/")) {
                url = getClass().getClassLoader().getResource(name);
                String urlstr = url.getFile();
                if (url.getProtocol().equals("jar")) {
                    name = urlstr.substring(urlstr.indexOf("/"), urlstr.indexOf("!"));
                }  else {
                    name = urlstr.substring(urlstr.indexOf("/"));
                }
                // CAT.warn("URL is: " + url + " Name is: " + name);
            }
            File scfile = new File(name);
            if (scfile.exists() && scfile.canRead()) {
                max = Math.max(max, scfile.lastModified());
            }
        }
        if (max > timestamp) {
            return max;
        } else {
            return 0l;
        }
    }

    
    private static HashMap selectProperties(Properties props, String prefix) {
    	String p;
    	Enumeration enm;
    	HashMap     result = new HashMap();
        
    	prefix += '.';
    	enm = props.propertyNames();
    	while (enm.hasMoreElements()) {
            p = (String) enm.nextElement();
            if (p.startsWith(prefix)) {
                String suffix = p.substring(prefix.length(),p.length());
                result.put(suffix,props.get(p));
            }
    	}
        
    	return result;
    }


}
