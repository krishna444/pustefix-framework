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
package de.schlund.pfixxml.targets;

import java.io.File;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Category;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.icl.saxon.Controller;
import com.icl.saxon.aelfred.SAXDriver;
import com.icl.saxon.om.Builder;
import com.icl.saxon.om.DocumentInfo;
import com.icl.saxon.tinytree.TinyDocumentImpl;

import de.schlund.util.*;


/**
 * This class implements the singleton pattern and handles all XSLT transformations
 * in the PUSTEFIX-system. Currently our favoured XSLT processor is saxon. That's why
 * this class includes some saxon-specfic stuff. However, its interface should be
 * stable enough to intergrate other XSLT processors (e.g. XSLTC) easy.
 */
public final class TraxXSLTProcessor implements PustefixXSLTProcessor {

    //~ Instance/static variables ..................................................................

    private static Category          CAT             = Category.getInstance(TraxXSLTProcessor.class.getName());
    private static TraxXSLTProcessor instance        = new TraxXSLTProcessor();
  /*  public static final String       TRANS_FAC_SAXON = "com.icl.saxon.TransformerFactoryImpl";
    public static final String       DOCB_FAC_XERCES = "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl";
    public static final String       SAXP_FAC_XERCES = "org.apache.xerces.jaxp.SAXParserFactoryImpl";
    public static final String       DOCB_FAC_SAXON  = "com.icl.saxon.om.DocumentBuilderFactoryImpl";
    private static final String      SAXP_FAC_SAXON  = "com.icl.saxon.aelfred.SAXParserFactoryImpl";
    public static final String       TRANS_FAC_KEY   = "javax.xml.transform.TransformerFactory";
    public static final String       DOCB_FAC_KEY    = "javax.xml.parsers.DocumentBuilderFactory";
    public static final String       SAXP_FAC_KEY    = "javax.xml.parsers.SAXParserFactory";
    public static final String       TRANS_FAC_VALUE = TRANS_FAC_SAXON;
    public static final String       DOCB_FAC_VALUE  = DOCB_FAC_XERCES;
    public static final String       SAXP_FAC_VALUE  = SAXP_FAC_XERCES;*/

    //~ Initializers ...............................................................................

  /*  static {
        if (CAT.isInfoEnabled()) {
            StringBuffer b = new StringBuffer(100);
            b.append("\nSetting ").append(TRANS_FAC_KEY).append(" to ").append(TRANS_FAC_VALUE).append(
                    "\n").append("Setting ").append(DOCB_FAC_KEY).append(" to ").append(
                    DOCB_FAC_VALUE).append("\n").append("Setting ").append(SAXP_FAC_KEY).append(
                    " to ").append(SAXP_FAC_VALUE);
            CAT.info(b.toString());
        }
        //wuerg!
        System.getProperties().put(TRANS_FAC_KEY, TRANS_FAC_VALUE);
        System.getProperties().put(DOCB_FAC_KEY, DOCB_FAC_VALUE);
        System.getProperties().put(SAXP_FAC_KEY, SAXP_FAC_VALUE);
    }*/

    //~ Methods ....................................................................................

    /**
     * Get the one and only instance of this class
     * @return the instance
     */
    public static final TraxXSLTProcessor getInstance() {
        return instance;
    }

    /**
     * Do a transformation with a given source document, a stylesheet, parameters for
     * the transformator and write the result to a given outputstream.
     * @param xmlobj the source document. Note: Currently an instance of saxons
     * TinyDocumentImpl must be passed.  
     * @param xslobj the stylesheet. Note: Currently an instance of saxons
     * PerparedStyleSheet must be passed.
     * @param params parameters for the transformator
     * @param out the outputstream where the result is written to
     * @throws exception on all errors
     */
    public final void applyTrafoForOutput(Object xmlobj, Object xslobj, Map params, 
                                          OutputStream out) throws TransformerException   {
        Templates   xsl   = (Templates) xslobj;
       
        Transformer trafo = xsl.newTransformer();
        
        long        start = 0;
        if (params != null) {
            for (Iterator e = params.keySet().iterator(); e.hasNext();) {
                String name  = (String) e.next();
                String value = (String) params.get(name);
                if (name != null && value != null) {
                    //if(CAT.isDebugEnabled())
                    //  CAT.debug("*** Setting param " + name + " to value " + value);
                    trafo.setParameter(name, value);
                }
            }
        }
        if (CAT.isDebugEnabled())
            start = System.currentTimeMillis();
        // do the transformation
        StreamResult stream_result = new StreamResult(out);
        trafo.transform((TinyDocumentImpl) xmlobj, stream_result);
        if (CAT.isDebugEnabled()) {
            long stop = System.currentTimeMillis();
            CAT.debug("      ===========> Transforming and serializing took " + (stop - start)
                      + " ms.");
        }
        stream_result = null;
    }

    // FIXME: We need to change the processing in AbstractXMLServer to not simply use spdoc.getDocument(), but instead
    //        route the Document through this method first. But we also need to make sure that this happens only once,
    //        and the resulting Object is stored for reuse (this applies to frame handling).
    //

    /**
     * Convert the document implementation which is used for write-access 
     * by {@link SPDocument} to the document implementation which is used 
     * by the XSLTProcessor. Note: Currently we convert here from a mutable
     * DOM implementation to an immutable NodeInfo implementation(saxon).
     * @param doc the document as source for conversion(mostly a Node implementation
     * when using xerces)
     * @return a document as result of conversion(currently saxons TinyDocumentImpl)
     * @throws Exception on all errors 
     */
    public final Document xmlObjectFromDocument(Document doc) throws TransformerException {
        return tinyTreeFromDocument(doc);
    }

    /**
     * Create a stylesheet from a sourcefile in the filesystem
     * @param path the path to the source file in the filesystem
     * @return the created stylesheet(currently saxons PreparedStyleSheet)
     * @throws TransformerConfigurationException on errors
     */
    public final Object xslObjectFromDisc(String docroot, String path) throws TransformerConfigurationException {
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("absolute path expected: " + path);
            // otherwise, I'd construct an wrong uri
        }
        // TransformerFactory.newInstance() does not work with ant, since the Factory
        // does not seem to pick the correct classloader with saxon in its classpath.
        // Simple instantiation or classloading works, since the current classloader is defined
        // by ant and therefore has saxon in its classpath.
        // TODO_AH check if object creation is really necessary here
        TransformerFactory transFac      = new com.icl.saxon.TransformerFactoryImpl();
        
        transFac.setErrorListener(new PFErrorListener());
        transFac.setURIResolver(new FileResolver(new File(docroot)));
        StreamSource stream_source = new StreamSource("file://" + path);
        Templates    val           = null;
        try {
            val = transFac.newTemplates(stream_source);
        } catch (TransformerConfigurationException e) {
            StringBuffer sb = new StringBuffer();
            sb.append("TransformerConfigurationException in xslObjectFromDisc!\n");
            sb.append("Path: ").append(path).append("\n");
            sb.append("Message and Location: ").append(e.getMessageAndLocation()).append("\n");
            Throwable cause = e.getException();
            if(cause == null)
                cause = e.getCause();
            sb.append("Cause: ").append((cause != null) ? cause.getMessage() : "none").append("\n");
            CAT.error(sb.toString());
            //System.out.println(e.getLocator().getSystemId());
            throw e;
        }
        stream_source = null;
        return val;
    }

    /**
     * Create a document from a sourcefile in the filesystem. Note: currently we use
     * the aelfred sax parser shipped with saxon here.
     * @param path the path to the source file in the filesystem
     * @return the created document(currenly saxons TinyDocumentImpl)
     * @throws TransformerException on errors
     */
    public final Document xmlObjectFromDisc(String path) throws TransformerException {
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("absolute path expected: " + path);
            // otherwise, I'd construct an wrong uri
        }
        InputSource input = new InputSource("file://" + path);
        // use the com.icl.saxon.aelfred.SAXDriver here
        XMLReader    xml_reader = new SAXDriver();
        SAXSource    saxsource  = new SAXSource(xml_reader, input);
        Controller   controller = new Controller();
        Builder      builder    = controller.makeBuilder();
        DocumentInfo dInfo      = null;
        try {
            dInfo = builder.build(saxsource);
        } catch (TransformerException e) {
            StringBuffer sb = new StringBuffer();
            sb.append("TransformerException in xmlObjectFromDisc!\n");
            sb.append("Path: ").append(path).append("\n");
            sb.append("Message and Location: ").append(e.getMessageAndLocation()).append("\n");
            Throwable cause = e.getException();
            sb.append("Cause: ").append((cause != null) ? cause.getMessage() : "none").append("\n");
            CAT.error(sb.toString());
            throw e;
        }
        return (Document) dInfo;
    }

    /**
     * Convert from a W3C DOM to saxons TinyTree.
     */
    private final TinyDocumentImpl tinyTreeFromDocument(Document doc) throws TransformerException  {
        if (doc == null) {
            // thats a request to an unkown page!
            // return null, cause we  want a 404 and no NPExpection
            if (CAT.isDebugEnabled()) {
                CAT.debug("Having a null-document as parameter. Unkown page? Returning null...");
            }
            return null;
        }
        long start = 0;
        long stop = 0;
        if (CAT.isInfoEnabled())
            start = System.currentTimeMillis();
        
        DOMSource  domsource  = new DOMSource(doc);
        Controller controller = new Controller();
        controller.setTreeModel(Builder.TINY_TREE);
        SAXSource        saxsource = controller.getTransformerFactory().getSAXSource(domsource, 
                                                                                     false);
        Builder          bi    = controller.makeBuilder();
        DocumentInfo     dInfo = bi.build(saxsource);
        TinyDocumentImpl tiny  = (TinyDocumentImpl) dInfo;
        if (CAT.isInfoEnabled()) {
            stop = System.currentTimeMillis();
            StringBuffer b = new StringBuffer(100);
            b.append("Conversion from ").append(doc.getClass().getName()).append(" to ").append(tiny.getClass().getName())
             .append(" took ").append(stop - start).append("ms");
            CAT.info(b.toString());
            b = null;
        }
        domsource  = null;
        controller = null;
        return tiny;
    }
}

class FileResolver implements URIResolver {
	private static final String SEP = File.separator; 
    
    // always with tailing /
    private final String root;
    
    public FileResolver(File root) {
        this.root = root.getAbsolutePath() + "/";
    }
    
    /**
     * Resolve file url relative to root. 
     * @param base ignored, always relative to root 
     * */
    public Source resolve(String href, String base) throws TransformerException {
        URI uri;
        String path;
        File file;
        
        try {
            uri = new URI(href);
        } catch (URISyntaxException e) {
        	return new StreamSource(href);
        }
        if (uri.getScheme() != null) {
            // we don't handle uris with an explicit scheme
        	return new StreamSource(href);
        }
      	path = 	uri.getPath();
       	try {
       	    file = Path.create(root, path).resolve();
       	} catch (IllegalArgumentException e) {
       	    throw new TransformerException("cannot resolve " + href, e);
       	}
       	return new StreamSource(file);
    }
}

/**
 * Implementation of ErrorListener interface.
 */
class PFErrorListener implements ErrorListener {

    /**
     * @see javax.xml.transform.ErrorListener#warning(javax.xml.transform.TransformerException)
     */
    public void warning(TransformerException arg0) throws TransformerException {
        // TODO Auto-generated method stub
       // print("warning", arg0);
        throw arg0;
    }

    /**
     * @see javax.xml.transform.ErrorListener#error(javax.xml.transform.TransformerException)
     */
    public void error(TransformerException arg0) throws TransformerException {
        // TODO Auto-generated method stub
       // print("error", arg0);
        throw arg0;
    }

    /**
     * @see javax.xml.transform.ErrorListener#fatalError(javax.xml.transform.TransformerException)
     */
    public void fatalError(TransformerException arg0) throws TransformerException {
        // TODO Auto-generated method stub
       // print("fatal", arg0);
        throw arg0;
    }

    /*private void print(String msg, TransformerException ex) {
        System.out.println(msg+": hallo:"+ex.getMessage()+"-->"+ex.getLocationAsString()+"-->"+ex.getException());
    }*/
}
