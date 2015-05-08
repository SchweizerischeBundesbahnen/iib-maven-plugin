package ch.sbb.maven.plugins.iib.mojos;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Test;

public class CreateBarMojoUnitTest {

    @Test
    public void createWorkspaceDirectory_success() throws IOException {
        CreateBarMojo mojo = new CreateBarMojo();
        mojo.workspace = new File("target", UUID.randomUUID().toString());
        try {
            mojo.createWorkspaceDirectory();
        } catch (MojoFailureException e) {
            assertTrue("The method createWorkspaceDirectory threw an MojoFailureException", false);
        }
        FileUtils.deleteDirectory(mojo.workspace);
    }

    /**
     * Validates that the right exception is thrown when the workspace parameter points to a file instead of a directory.
     * 
     * @throws IOException
     */
    @Test
    public void createWorkspaceDirectory_workspaceIsFile() throws IOException {
        CreateBarMojo mojo = new CreateBarMojo();
        mojo.workspace = File.createTempFile(UUID.randomUUID().toString(), "");
        mojo.workspace.deleteOnExit();
        try {
            mojo.createWorkspaceDirectory();
        } catch (MojoFailureException e) {
            return;
        }

        assertTrue("The method createWorkspaceDirectory should have thrown a MojoFailureException but didn't", false);

    }

}
