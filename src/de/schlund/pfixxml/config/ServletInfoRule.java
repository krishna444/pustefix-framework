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
 */

package de.schlund.pfixxml.config;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;

public class ServletInfoRule extends CheckedRule {

    private CommonServletConfig config;

    public ServletInfoRule(CommonServletConfig config) {
        this.config = config;
    }

    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        check(namespace, name, attributes);
        String servletName = attributes.getValue("name");
        if (servletName == null) {
            throw new Exception("Mandatory attribute \"name\" is missing!");
        }
        config.setServletName(servletName);
        if (config instanceof ContextXMLServletConfig) {
            String dependFile = attributes.getValue("depend");
            if (dependFile == null) {
                throw new Exception("Mandatory attribute \"depend\" is missing!");
            }
            config.setDependFile(dependFile);
        }
        this.getDigester().push(config);
    }
    
    public void end(String namespace, String name) throws Exception {
        this.getDigester().pop();
    }
    
    protected Map<String, Boolean> wantsAttributes() {
        HashMap<String, Boolean> atts = new HashMap<String, Boolean>();
        atts.put("name", true);
        if (config instanceof ContextXMLServletConfig) {
            atts.put("depend", true);
        } else {
            atts.put("depend", false);
        }
        return atts;
    }
}
