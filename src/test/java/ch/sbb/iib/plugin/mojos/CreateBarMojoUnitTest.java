package ch.sbb.iib.plugin.mojos;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.maven.plugin.MojoFailureException;
import org.junit.Test;

import ch.sbb.iib.plugin.mojos.CreateBarMojo;

public class CreateBarMojoUnitTest {

    /**
     * Validates that the right exception is thrown when the workspace parameter points to a file instead of a directory.
     */
    @Test(expected = MojoFailureException.class)
    public void workspaceIsFile() throws IOException, MojoFailureException {
        CreateBarMojo mojo = new CreateBarMojo();
        mojo.workspace = File.createTempFile(UUID.randomUUID().toString(), "");
        mojo.workspace.deleteOnExit();
        mojo.createWorkspaceDirectory();
    }
}
