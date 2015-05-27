package ch.sbb.maven.plugins.iib.mojos;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.dependency.utils.DependencyUtil;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

/**
 * Unpacks the dependent WebSphere Message Broker Projects.
 * 
 * Implemented with help from: https://github.com/TimMoore/mojo-executor/blob/master/README.md
 * 
 * requiresDependencyResolution below is required for the unpack-dependencies goal to work correctly. See https://github.com/TimMoore/mojo-executor/issues/3
 */
@Mojo(name = "prepare-bar-build-workspace", requiresDependencyResolution = ResolutionScope.TEST)
public class PrepareBarBuildWorkspaceMojo extends AbstractMojo {

    /**
     * a comma separated list of dependency types to be unpacked
     */
    private static final String UNPACK_IIB_DEPENDENCY_TYPES = "zip";

    private static final String UNPACK_IIB_DEPENDENCY_SCOPE = "compile";

    /**
     * The Maven Project Object
     */
    @Parameter(property = "project", required = true, readonly = true)
    protected MavenProject project;

    /**
     * The Maven Session Object
     */
    @Parameter(property = "session", required = true, readonly = true)
    protected MavenSession session;

    /**
     * The Maven PluginManager Object
     */
    @Component
    protected BuildPluginManager buildPluginManager;

    /**
     * The path of the workspace in which the projects are extracted to be built.
     */
    @Parameter(property = "iib.workspace", defaultValue = "${project.build.directory}/iib/workspace", required = true)
    protected File workspace;

    /**
     * The path of the workspace in which the projects will be unpacked.
     */
    @Parameter(property = "iib.unpackDependenciesDirectory", defaultValue = "${project.build.directory}/iib/dependencies", required = true, readonly = true)
    protected File unpackDependenciesDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {

        unpackIibDependencies();

        deleteAppAndLibPoms();
    }

    /**
     * deletes the unrequired pom.xml files. pom.xml's appear in all projects, but are only really required for java projects for .bar packaging
     * 
     * @throws MojoExecutionException
     */
    @SuppressWarnings("unchecked")
    private void deleteAppAndLibPoms() throws MojoExecutionException {
        try {
            getLog().debug("Deleting pom.xml from Applications and Libraries...");
            List<String> appAndLibProjects = FileUtils.getDirectoryNames(workspace, "*-app,*-lib", null, false);
            for (String project : appAndLibProjects) {
                getLog().debug("  Found " + project);

                // find the pom file
                File pomFile = new File(new File(workspace, project), "pom.xml");
                if (!pomFile.exists()) {
                    getLog().warn("Trying to delete pom.xml, but couldn't find it: " + pomFile.getAbsolutePath());
                }

                boolean deleted = pomFile.delete();
                if (deleted) {
                    getLog().debug("    Deleted " + pomFile.getAbsolutePath());
                } else {
                    getLog().warn("Trying to delete pom.xml, but couldn't: " + pomFile.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            // FIXME handle exception
            throw new RuntimeException(e);
        }

    }

    /**
     * unpacks dependencies of a given scope to the specified directory
     * 
     * @throws MojoExecutionException
     */
    private void unpackIibDependencies() throws MojoExecutionException {

        // define the directory to be unpacked into and create it
        workspace.mkdirs();

        // unpack all dependencies that match the given scope
        executeMojo(plugin(groupId("org.apache.maven.plugins"), artifactId("maven-dependency-plugin"), version("2.8")), goal("unpack-dependencies"), configuration(element(name("outputDirectory"),
                workspace.getAbsolutePath()), element(name("includeTypes"), UNPACK_IIB_DEPENDENCY_TYPES), element(name("includeScope"), UNPACK_IIB_DEPENDENCY_SCOPE)),
                executionEnvironment(project, session, buildPluginManager));

        // delete the dependency-maven-plugin-markers directory
        try {
            FileUtils.deleteDirectory(new File(project.getBuild().getDirectory(), "dependency-maven-plugin-markers"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @return the types that will be unpacked when preparing the Bar Build Workspace
     */
    public static Set<String> getUnpackIibDependencyTypes() {
        HashSet<String> types = new HashSet<String>();
        for (String type : DependencyUtil.tokenizer(UNPACK_IIB_DEPENDENCY_TYPES)) {
            types.add(type);
        }
        return types;
    }
}
