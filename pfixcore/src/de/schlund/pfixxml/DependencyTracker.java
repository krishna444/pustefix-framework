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


import java.io.File;

import de.schlund.pfixxml.targets.*;
import de.schlund.util.*;

import org.apache.log4j.Category;


public class DependencyTracker {
    private static Category CAT = Category.getInstance(DependencyTracker.class.getName());
    
    /** saxon extension */
    public static String log(String type, String path, String part, String product, String docroot,
                             String parent_path, String parent_part, String parent_product,
                             String targetGen, String targetKey) throws Exception {
        File targetFile = Path.create(docroot, targetGen).resolve();
        TargetGenerator gen = TargetGeneratorFactory.getInstance().createGenerator(targetFile);
		TargetImpl target = (TargetImpl) gen.getTarget(targetKey);
		if (path.length() == 0) {
	        CAT.error("Error adding Dependency: empty path"); 
	        return "1"; 
		}
        try {
    		logTyped(type, relative(docroot, path), part, product, relative(docroot, parent_path), parent_part, parent_product, target);
    		return "0";
        } catch (Exception e) {
            CAT.error("Error adding Dependency: ",e); 
            return "1"; 
        }
    }

    private static Path relative(String docroot, String path) {
		if (path.startsWith(File.separator)) {
			path = path.substring(1); // TODO: kind of ugly - fix gif src attributes instead!!
		} 
		return Path.createOpt(docroot, path);
	}

    public static void logTyped(String type,Path path, String part, String product,
                                Path parent_path, String parent_part, String parent_product,
                                Target target) {
        if (CAT.isDebugEnabled()) {
            CAT.debug("Adding dependency to AuxdependencyManager :+\n"+
                      "Type        = " + type + "\n" +
                      "Path        = " + path.getRelative() + "\n" +
                      "Part        = " + part + "\n" +
                      "Product     = " + product + "\n" +
                      "ParentPath  = " + parent_path.getRelative() + "\n" +
                      "ParentPart  = " + parent_part + "\n" +
                      "ParentProd  = " + parent_product + "\n" +
                      "TargetGen   = " + target.getTargetGenerator().getConfigname() + "\n");
        }
        DependencyType  thetype   = DependencyType.getByTag(type);
        target.getAuxDependencyManager().addDependency(thetype, path, part, product,
                                                       parent_path, parent_part,
                                                       parent_product);
    }
}
