package ch.sbb.maven.plugins.iib.mojos;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;

/**
 * Cleans up the ${iib.workspace} directory. Build errors will appear in the IIB Toolkit if .msgflow files are left under the ${iib.workspace} - the path determines the Namespace of the flow and that
 * certainly won't match the original directory structure.
 */
@Mojo(name = "clean-bar-build-workspace", requiresProject = false)
public class CleanBarBuildWorkspaceMojo extends AbstractMojo {

    /**
     * The path of the workspace in which the projects were created.
     */
    @Parameter(property = "iib.workspace", defaultValue = "${project.build.directory}/iib/workspace", required = true)
    protected File workspace;

    /**
     * set to true to disable the workspace cleaning
     */
    @Parameter(property = "iib.debugWorkspace", defaultValue = "false")
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
