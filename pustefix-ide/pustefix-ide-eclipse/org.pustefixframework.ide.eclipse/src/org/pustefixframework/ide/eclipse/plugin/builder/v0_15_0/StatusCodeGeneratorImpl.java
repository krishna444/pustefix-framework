package org.pustefixframework.ide.eclipse.plugin.builder.v0_15_0;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import org.pustefixframework.ide.eclipse.plugin.builder.StatusCodeGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class StatusCodeGeneratorImpl implements StatusCodeGenerator {
	
    public String getModulePath(String relPath, String module) {
        if(module==null) return relPath;
        int ind = relPath.lastIndexOf('.');
        if(ind == -1) throw new RuntimeException("Illegal file name: "+relPath);
        relPath = relPath.substring(0,ind)+"-merged"+relPath.substring(ind);
        String modulePath="";
        if(module.equals("pfixcore")) {
            if(!relPath.startsWith("core")) throw new RuntimeException("Illegal core file name: "+relPath);
            modulePath = "core-override/"+relPath.substring(5);
        } else {
            modulePath = "modules-override/"+module+"/"+relPath;
        }
        return modulePath;
    }
	
	private String convertToFieldName(String part) {
		return part.replace('.', '_').replace(':', '_').toUpperCase();
	}
	
    public void createResources(Writer writer, List<String> docRelPaths) throws IOException {
        writer.write("    public static final DocrootResource[] __RES = {\n");
        Iterator<String> it = docRelPaths.iterator();
        while(it.hasNext()) {
            writer.write("        ResourceUtil.getFileResourceFromDocroot(\""+it.next()+"\")");
            if(it.hasNext()) writer.write(",");
            writer.write("\n");
        }
        writer.write("    };\n\n");
    }
    
    public void createStatusCodes(Writer writer, Document doc, int resIndex) throws IOException {
        NodeList list  = doc.getElementsByTagName("part");
        for (int i = 0; i < list.getLength() ; i++) {
            Element node      = (Element) list.item(i);
            String  name      = node.getAttribute("name");
            String  classname = convertToFieldName(name);
            writer.write("    public static final StatusCode " + classname +
                    " = new StatusCode(\"" + name + "\", __RES["+resIndex+"]);\n");
        }
    }
	
    public void createHeader(Writer writer, String className) throws IOException {
        int ind = className.lastIndexOf('.');
        if(ind == -1) throw new RuntimeException("Class name must contain package: "+className);
        String pkgName = className.substring(0,ind);
        String simpleName = className.substring(ind+1);
        writer.write("/*\n");
        writer.write(" * This file is AUTOGENERATED. Do not change by hand.\n");
        writer.write(" */\n");
        writer.write("\n");
        writer.write("\n");
        writer.write("package "+pkgName+";\n\n");
        writer.write("import de.schlund.pfixxml.resources.DocrootResource;\n");
        writer.write("import de.schlund.pfixxml.resources.ResourceUtil;\n");
        if(!pkgName.equals("de.schlund.util.statuscodes")) {
            writer.write("import de.schlund.util.statuscodes.StatusCode;\n");
            writer.write("import de.schlund.util.statuscodes.StatusCodeException;\n");
        }
        writer.write("import java.lang.reflect.Field;\n");
        writer.write("\n");
        writer.write("public class "+simpleName+" {\n\n");
        writer.write("    public static StatusCode getStatusCodeByName(String name) throws StatusCodeException {\n");
        writer.write("        return getStatusCodeByName(name, false);\n");
        writer.write("    }\n");        
        writer.write("\n");        
        writer.write("    public static StatusCode getStatusCodeByName(String name, boolean optional) throws StatusCodeException {\n");
        writer.write("        String     fieldname = StatusCode.convertToFieldName(name);\n");
        writer.write("        StatusCode scode     = null;\n");
        writer.write("        try {\n");
        writer.write("            Field field = "+simpleName+".class.getField(fieldname);\n");
        writer.write("            scode = (StatusCode) field.get(null);\n");
        writer.write("        } catch (NoSuchFieldException e) {\n");
        writer.write("            //\n");
        writer.write("        } catch (SecurityException e) {\n");
        writer.write("            //\n");
        writer.write("        } catch (IllegalAccessException e) {\n");
        writer.write("            //\n");
        writer.write("        }\n");
        writer.write("        if (scode == null && optional == false) {\n");
        writer.write("            throw new StatusCodeException(\"StatusCode \" + name + \" is not defined.\");\n");
        writer.write("        }\n");
        writer.write("        return scode;\n");
        writer.write("    }\n\n");
    }
    
}
