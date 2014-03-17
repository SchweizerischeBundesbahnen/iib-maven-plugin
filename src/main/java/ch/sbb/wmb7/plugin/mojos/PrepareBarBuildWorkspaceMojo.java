package ch.sbb.wmb7.plugin.mojos;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import java.io.File;
import java.io.IOException;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

/**
 * Unpacks the dependent WebSphere Message Broker Projects.
 * 
 * Implemented with help from: https://github.com/TimMoore/mojo-executor/blob/master/README.md
 * 
 * requiresDependencyResolution below is required for the unpack-dependencies goal to work correctly. See https://github.com/TimMoore/mojo-executor/issues/3
 * 
 * @goal prepare-bar-build-workspace
 * @requiresProject true
 * @requiresDependencyResolution test
 * 
 */
public class PrepareBarBuildWorkspaceMojo extends AbstractMojo {

    /**
     * The Maven Project Object
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * The Maven Session Object
     * 
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
    protected MavenSession session;

    /**
     * The Maven PluginManager Object
     * 
     * @component
     * @required
     */
    protected BuildPluginManager buildPluginManager;

    /**
     * The path of the workspace in which the projects are extracted to be built.
     * 
     * @parameter expression="${wmb.workspace}" default-value="${project.build.directory}/wmb/workspace"
     * @required
     */
    protected File workspace;

    /**
     * The path of the workspace in which the projects will be unpacked.
     * 
     * @parameter expression="${wmb.unpackDependenciesDirectory}" default-value="${project.build.directory}/wmb/dependencies"
     * @required
     * @read-only
     */
    protected File unpackDependenciesDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        // mvn org.apache.maven.plugins:maven-dependency-plugin:2.1:unpack-dependencies -DoutputDirectory=${project.build.directory}/wmb/workspace

        // unpack all the dependencies into the workspace directory for compilation
        // and bar file packaging.
        unpackWmbDependencies("compile", workspace);

        // the following code might come in useful for the packaging of specific artifacts in the bar file

        // unpackWmbDependencies("compile", new File(unpackDependenciesDirectory, "compile"));

        // is not needed for anything
        // unpackWmbDependencies("runtime");

        // unpack the provided dependencies, so that the can be excluded from
        // the bar file creation.
        // unpackWmbDependencies("provided", new File(unpackDependenciesDirectory, "provided"));

        // unpackWmbDependencies("test", new File(unpackDependenciesDirectory, "test"));

    }

    /**
     * @param scope dependency Scope to be unpacked
     * @throws MojoExecutionException
     */
    private void unpackWmbDependencies(String scope, File unpackDir) throws MojoExecutionException {

        // define the directory to be unpacked into and create it
        unpackDir.mkdirs();

        // unpack all dependencies that match the given scope
        executeMojo(plugin(groupId("org.apache.maven.plugins"), artifactId("maven-dependency-plugin"), version("2.8")), goal("unpack-dependencies"), configuration(element(name("outputDirectory"),
                unpackDir.getAbsolutePath()), element(name("includeTypes"), "zip"), element(name("includeScope"), scope)), executionEnvironment(project, session, buildPluginManager));

        // delete the dependency-maven-plugin-markers directory
        try {
            FileUtils.deleteDirectory(new File(project.getBuild().getDirectory(), "dependency-maven-plugin-markers"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
