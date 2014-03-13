package ch.sbb.wmb7.plugin.mojos;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;

/**
 * Cleans up the ${wmb.workspace} directory. Build errors will appear in the WMB Toolkit if .msgflow files are left under the ${wmb.workspace} - the path determines the Namespace of the flow and that
 * certainly won't match the original directory structure.
 * 
 * @goal clean-bar-build-workspace
 * @requiresProject false
 * 
 */
public class CleanBarBuildWorkspaceMojo extends AbstractMojo {

    /**
     * The path of the workspace in which the projects were created.
     * 
     * @parameter expression="${wmb.workspace}" default-value="${project.build.directory}/wmb/workspace"
     * @required
     */
    protected File workspace;

    /**
     * set to true to disable the workspace cleaning
     * 
     * @parameter expresseion="${debugWorkspace}" default-value="false"
     */
    protected boolean debugWorkspace;

    public void execute() throws MojoFailureException {
        if (debugWorkspace) {
            getLog().info("debugWorkspace enabled - workspace will not be cleaned");
        } else {
            getLog().info("Cleaning up the workspace directory: " + workspace);
            if (workspace.exists()) {
                try {
                    FileUtils.deleteDirectory(workspace);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

}
