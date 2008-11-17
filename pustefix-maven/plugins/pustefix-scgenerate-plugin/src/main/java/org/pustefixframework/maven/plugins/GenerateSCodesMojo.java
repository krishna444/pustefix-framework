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
package org.pustefixframework.maven.plugins;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;

import de.schlund.pfixcore.util.GenerateSCodes;
import de.schlund.pfixcore.util.GenerateSCodes.Result;
import de.schlund.pfixxml.config.GlobalConfig;
import de.schlund.pfixxml.config.GlobalConfigurator;
import de.schlund.pfixxml.resources.DocrootResource;
import de.schlund.pfixxml.resources.ResourceUtil;

/**
 * Generate StatusCode constant classes from statusmessage files.
 *
 * @author mleidig@schlund.de
 *
 * @goal scgenerate
 * @phase generate-sources
 */
public class GenerateSCodesMojo extends AbstractMojo {
    
    /**
     * @parameter default-value="target/generated-sources/statuscodes"
     * @required
     */
    private File genDir;
    
    /**
     * @parameter default-value="src/main/resources"
     * @required
     */
    private File docRoot;
    
    /**
     * @parameter
     */
    private String module;
    
    /**
     * @parameter
     * @required
     */
    private String[] includes;
    
    /**
     * @parameter
     */
    private String[] excludes;
    
    /**
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;
    
    public void execute() throws MojoExecutionException {
        
        if(GlobalConfig.getDocroot()==null) GlobalConfigurator.setDocroot(docRoot.getAbsolutePath());
        
        DirectoryScanner ds = new DirectoryScanner();
        if(includes!=null) ds.setIncludes(includes);
        if(excludes!=null) ds.setExcludes(excludes);
        ds.setBasedir(docRoot);
        ds.setCaseSensitive(true);
        ds.scan();
        String[] files = ds.getIncludedFiles();

        List<DocrootResource> resList = new ArrayList<DocrootResource>();        
        for (int i = 0; i < files.length; i++) {
          DocrootResource res = ResourceUtil.getFileResourceFromDocroot(files[i]);
          resList.add(res);
        }
        
        try {
            Result result = GenerateSCodes.generateFromInfo(resList, docRoot.getAbsolutePath(), genDir, module);
            if(result.generatedClasses.size()>0) {
                getLog().info("Generated "+result.generatedClasses.size()+" statuscode class"+
                        (result.generatedClasses.size()>1?"es":""));
            }
            if(result.allClasses.size()>0) project.addCompileSourceRoot(genDir.getAbsolutePath());
            
        } catch(Exception x) {
            throw new MojoExecutionException("Can't generate StatusCode constant classes",x);
        }

    }
}