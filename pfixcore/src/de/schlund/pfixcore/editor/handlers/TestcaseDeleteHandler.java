package de.schlund.pfixcore.editor.handlers;

import de.schlund.pfixcore.editor.interfaces.TestcaseDelete;
import de.schlund.pfixcore.editor.resources.CRTestcase;
import de.schlund.pfixcore.editor.resources.EditorRes;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResourceManager;

/**
 * Handler for deleting testcases.
 * 
 * <br/>
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
public class TestcaseDeleteHandler implements IHandler {

    /**
     * @see de.schlund.pfixcore.generator.IHandler#handleSubmittedData(Context, IWrapper)
     */
    public void handleSubmittedData(Context context, IWrapper wrapper)
        throws Exception {
        TestcaseDelete tcd = (TestcaseDelete) wrapper;
        
        String[] ids = tcd.getSelect();
        ContextResourceManager crm = context.getContextResourceManager();
        CRTestcase crtc = (CRTestcase) EditorRes.getCRTestcase(crm);
        crtc.removeTestcase(ids);
    }

    /**
     * @see de.schlund.pfixcore.generator.IHandler#retrieveCurrentStatus(Context, IWrapper)
     */
    public void retrieveCurrentStatus(Context context, IWrapper wrapper)
        throws Exception {
    }

    /**
     * @see de.schlund.pfixcore.generator.IHandler#prerequisitesMet(Context)
     */
    public boolean prerequisitesMet(Context context) throws Exception {
        return true;
    }

    /**
     * @see de.schlund.pfixcore.generator.IHandler#isActive(Context)
     */
    public boolean isActive(Context context) throws Exception {
        return true;
    }

    /**
     * @see de.schlund.pfixcore.generator.IHandler#needsData(Context)
     */
    public boolean needsData(Context context) throws Exception {
        return true;
    }

}
