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

package de.schlund.pfixcore.editor2.core.spring.internal;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

import de.schlund.pfixcore.editor2.core.dom.IncludePart;
import de.schlund.pfixcore.editor2.core.dom.Theme;
import de.schlund.pfixcore.editor2.core.dom.ThemeList;
import de.schlund.pfixcore.editor2.core.exception.EditorParsingException;
import de.schlund.pfixcore.editor2.core.exception.EditorSecurityException;
import de.schlund.pfixcore.editor2.core.spring.BackupService;
import de.schlund.pfixcore.editor2.core.spring.ConfigurationService;
import de.schlund.pfixcore.editor2.core.spring.FileSystemService;
import de.schlund.pfixcore.editor2.core.spring.PathResolverService;
import de.schlund.pfixcore.editor2.core.spring.SecurityManagerService;

/**
 * Implementation of IncludePartThemeVariant for DynIncludes
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class DynIncludePartThemeVariantImpl extends
        CommonIncludePartThemeVariantImpl {

    private SecurityManagerService securitymanager;

    public DynIncludePartThemeVariantImpl(ConfigurationService configuration,
            BackupService backup, FileSystemService filesystem,
            PathResolverService pathresolver,
            SecurityManagerService securitymanager, Theme theme,
            IncludePart part) {
        super(filesystem, pathresolver, configuration, backup, theme, part);
        this.securitymanager = securitymanager;
    }

    protected void securityCheckCreateIncludePartThemeVariant()
            throws EditorSecurityException {
        this.securitymanager.checkEditDynInclude();
    }

    protected void securityCheckEditIncludePartThemeVariant()
            throws EditorSecurityException {
        this.securitymanager.checkEditDynInclude();
    }

    public Collection getIncludeDependencies(boolean recursive)
            throws EditorParsingException {
        return new ArrayList();
    }

    public Collection getImageDependencies(boolean recursive)
            throws EditorParsingException {
        return new ArrayList();
    }

    public Collection getIncludeDependencies(ThemeList themes, boolean recursive)
            throws EditorParsingException {
        return new ArrayList();
    }

    public Collection getImageDependencies(ThemeList themes, boolean recursive)
            throws EditorParsingException {
        return new ArrayList();
    }

    public Collection getAffectedPages() {
        return new ArrayList();
    }

    public Collection getAffectedProjects() {
        return new ArrayList();
    }

    protected void writeChangeLog() {
        Logger.getLogger("LOGGER_EDITOR").warn(
                "DYNTXT: " + this.securitymanager.getPrincipal().getName()
                        + ": " + this.toString());
    }

}
