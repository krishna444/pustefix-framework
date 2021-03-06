package org.pustefixframework.samples.moduletest.contextresources;

import org.pustefixframework.samples.moduletest.User;
import org.w3c.dom.Element;

import de.schlund.pfixcore.beans.InsertStatus;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.ResultDocument;

public class ContextUserImpl implements ContextUser {

    private User user;
    
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void init(Context context) throws Exception {
        // nothing to do here

    }

    @InsertStatus
    public void insertStatus(ResultDocument document, Element element)
            throws Exception {
        if (this.user == null) {
            return;
        }
        ResultDocument.addTextChild(element, "sex", this.user.getSex());
        ResultDocument.addTextChild(element, "name", this.user.getName());
        ResultDocument.addTextChild(element, "email", this.user.getEmail());
        ResultDocument.addTextChild(element, "homepage", String.valueOf(this.user.getHomepage()));
        ResultDocument.addTextChild(element, "birthday", String.valueOf(this.user.getBirthday()));
        ResultDocument.addTextChild(element, "admin", String.valueOf(this.user.getAdmin()));
    }
}