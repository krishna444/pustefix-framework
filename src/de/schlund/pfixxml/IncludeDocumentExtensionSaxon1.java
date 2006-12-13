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

import com.icl.saxon.Context;

import de.schlund.pfixxml.util.XsltContext;
import de.schlund.pfixxml.util.xsltimpl.XsltContextSaxon1;

/**
 * @author mleidig@schlund.de
 */
public class IncludeDocumentExtensionSaxon1 {
    
    public static Object get(Context context,String path_str,String part,String targetgen,String targetkey,
            String parent_part_in,String parent_theme_in,String computed_inc) throws Exception {    
        XsltContext xsltContext=new XsltContextSaxon1(context);
        return IncludeDocumentExtension.get(xsltContext,path_str,part,targetgen,targetkey,
                parent_part_in,parent_theme_in,computed_inc);
    }
    
    public static String makeSystemIdRelative(Context context) {
        XsltContext xsltContext=new XsltContextSaxon1(context);
        return IncludeDocumentExtension.makeSystemIdRelative(xsltContext,"dummy");
    }
    
    public static String makeSystemIdRelative(Context context,String dummy) {
        XsltContext xsltContext=new XsltContextSaxon1(context);
        return IncludeDocumentExtension.makeSystemIdRelative(xsltContext,dummy);
    }
    
    public static boolean isIncludeDocument(Context context) {
        XsltContext xsltContext=new XsltContextSaxon1(context);
        return IncludeDocumentExtension.isIncludeDocument(xsltContext);
    }

}
