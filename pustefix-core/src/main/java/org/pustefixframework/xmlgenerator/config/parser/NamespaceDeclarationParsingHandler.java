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
package org.pustefixframework.xmlgenerator.config.parser;

import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.xmlgenerator.config.model.Configuration;
import org.pustefixframework.xmlgenerator.config.model.NamespaceDeclaration;
import org.pustefixframework.xmlgenerator.config.model.XMLExtension;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class NamespaceDeclarationParsingHandler implements ParsingHandler {
	
    public void handleNode(HandlerContext context) throws ParserException {
        
        Element element = (Element)context.getNode();
        ParsingUtils.checkAttributes(element, new String[] {"prefix", "url"}, null);
        
        String prefix = element.getAttribute("prefix");
        String url = element.getAttribute("url");
        
        
        NamespaceDeclaration decl = new NamespaceDeclaration(prefix, url);
        Configuration configuration = ParsingUtils.getFirstTopObject(Configuration.class, context, false);
        if(configuration != null) {
        	configuration.addNamespaceDeclaration(decl);
        } else {
        	XMLExtension<NamespaceDeclaration> ext = XMLExtensionParsingUtils.getListExtension(context);
        	ext.add(decl);
        }
        
       
    }

}
