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
import java.util.ArrayList;

import org.apache.log4j.Category;

import de.schlund.pfixcore.editor.EditorPageUpdater;
import de.schlund.pfixcore.editor.EditorUser;
import de.schlund.pfixcore.editor.auth.EditorUserInfo;
import de.schlund.pfixcore.editor.interfaces.DeleteUser;
import de.schlund.pfixcore.editor.resources.EditorRes;
import de.schlund.pfixcore.editor.resources.EditorSessionStatus;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResourceManager;

/**
 * DeleteUserHandler.java
 *
 *
 * Created: Sun Dec 12 03:05:24 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class DeleteUserHandler implements IHandler {
    private static Category CAT = Category.getInstance(EditorPageUpdater.class.getName());

    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        ContextResourceManager crm     = context.getContextResourceManager();
        DeleteUser             deluser = (DeleteUser) wrapper;
        EditorSessionStatus    esess   = EditorRes.getEditorSessionStatus(crm);
        String[]               delid   = deluser.getId();
        ArrayList              users   = new ArrayList();
        
        for (int i = 0; i < delid.length; i++) {
            String     id  = delid[i];
            EditorUserInfo tmp = EditorUser.getUserInfoByLogin(id);
            if (tmp != null) {
                CAT.debug("*****  Deleting user " + id);
                EditorUser.removeUser(tmp);
            }
        }

       /* if (!users.isEmpty()) {
            EditorUserFactory.getInstance().delEditorUser((EditorUser[]) users.toArray(new EditorUser[] {}));
        }*/
       
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        // Never
    }
        
    public boolean prerequisitesMet(Context context) {
        ContextResourceManager crm   = context.getContextResourceManager();
        EditorSessionStatus    esess = EditorRes.getEditorSessionStatus(crm);
        EditorUser             user  = esess.getUser();
        if (user != null && user.getUserInfo().isAdmin()) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean isActive(Context context) {
        return true;
    }
    
    public boolean needsData(Context context) {
        return false;
    }
    
}// DeleteUserHandler
