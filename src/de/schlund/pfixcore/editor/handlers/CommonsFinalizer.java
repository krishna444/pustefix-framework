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

package de.schlund.pfixcore.editor.handlers;
import de.schlund.pfixcore.editor.*;
import de.schlund.pfixcore.editor.interfaces.*;
import de.schlund.pfixcore.editor.resources.*;
import de.schlund.pfixcore.generator.*;
import de.schlund.pfixcore.workflow.*;
import de.schlund.pfixcore.workflow.Navigation.*;
import de.schlund.pfixcore.workflow.app.*;
import de.schlund.pfixxml.*;
import de.schlund.pfixxml.targets.*;
import de.schlund.util.*;
import de.schlund.util.statuscodes.*;
import java.util.*;
import org.w3c.dom.*;
import java.io.*;

/**
 * CommonsFinalizer.java
 *
 *
 * Created: Wed Dec 13 13:00:33 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class CommonsFinalizer extends ResdocSimpleFinalizer {

    protected void renderDefault(IWrapperContainer container) throws Exception {
        Context                context  = container.getAssociatedContext();
        ContextResourceManager crm      = context.getContextResourceManager();
        EditorSessionStatus    esess    = EditorRes.getEditorSessionStatus(crm);
        EditorSearch           esearch  = EditorRes.getEditorSearch(crm);
        ResultDocument         resdoc   = container.getAssociatedResultDocument();
        EditorProduct          eprod    = esess.getProduct();
        TargetGenerator        tgen     = eprod.getTargetGenerator();
        AuxDependency          currcomm = esess.getCurrentCommon();
        
        // Render the current status of the editor session
        esess.insertStatus(resdoc, resdoc.createNode("cr_editorsession"));
        
        // Render all dynamic includes
        TreeSet commons = EditorCommonsFactory.getInstance().getAllCommons();
        Element root    = resdoc.createNode("allcommons"); 
        EditorHelper.renderAllIncludesForNavigation(commons, resdoc, root);

        TreeSet searchcom = esearch.getDynResultSet();
        if (searchcom != null && searchcom.size() > 0) {
            root = resdoc.createNode("currentsearchcommons");
            EditorHelper.renderAllIncludesForNavigation(searchcom, resdoc, root);
        }

        // Render detailed view of currently selected include
        if (currcomm != null) {
            boolean lock    = esess.getLock(currcomm); 
            String  dir     = currcomm.getDir();
            long    mod     = currcomm.getModTime();
            String  path    = currcomm.getPath();
            String  part    = currcomm.getPart();
            String  product = currcomm.getProduct();
            root = resdoc.createNode("currentcommoninfo");
            root.setAttribute("path",    path);
            root.setAttribute("part",    part);
            root.setAttribute("product", product);
            root.setAttribute("modtime", "" + mod);
            root.setAttribute("havelock", "" + lock);
            if (!lock) {
                try {
                    EditorSessionStatus foreign = EditorLockFactory.getInstance().getLockingEditorSessionStatus(currcomm);
                    if (foreign != null) {
                        EditorUser user = foreign.getUser();
                        Element    elem = resdoc.createSubNode(root, "lockinguser");
                        user.insertStatus(resdoc, elem);
                    }
                } catch (IllegalStateException e) {
                    CAT.warn("*** Eeeeek, seems the EditorSessionStatus was suddenly GC'ed...");
                }
            }
            
            Element elem = resdoc.createSubNode(root, "backup");
            EditorHelper.renderBackupOptions(esess, currcomm, resdoc, elem);

            elem = resdoc.createSubNode(root, "content");
            EditorHelper.renderIncludeContent(tgen, currcomm, resdoc, elem);
        }
    }

    public void onSuccess(IWrapperContainer container) throws Exception{
        renderDefault(container);
    }
        
}// CommonsFinalizer
