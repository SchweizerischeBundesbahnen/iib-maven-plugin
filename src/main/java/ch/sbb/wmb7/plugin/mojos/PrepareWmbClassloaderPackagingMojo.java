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
 * Copies the dependencies into a directory for packaging later
 * 
 * Implemented with help from: https://github.com/TimMoore/mojo-executor/blob/master/README.md
 * 
 * requiresDependencyResolution below is required for the unpack-dependencies goal to work correctly. See https://github.com/TimMoore/mojo-executor/issues/3
 * 
 * @goal prepare-wmb-classloader-packaging
 * @requiresProject true
 * @requiresDependencyResolution test
 * 
 */
public class PrepareWmbClassloaderPackagingMojo extends AbstractMojo {

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
     * The path where the classloader jars will be copied to for packaging later.
     * 
     * @parameter expression="${wmb.classloader}" default-value="${project.build.directory}/wmb/classloader"
     * @read-only
     * @required
     */
    protected File classloader;

    public void execute() throws MojoExecutionException, MojoFailureException {
        // mvn
        // org.apache.maven.plugins:maven-dependency-plugin:2.1:copy-dependencies
        // -DoutputDirectory=${project.build.directory}/wmb/classloader

        getLog().info("Emptying " + new File(project.getBuild().getDirectory(), "wmb").getAbsolutePath());
        try {
            FileUtils.deleteDirectory(new File(project.getBuild().getDirectory(), "wmb"));
        } catch (IOException e) {
            // ignore
        }

        executeMojo(plugin(groupId("org.apache.maven.plugins"), artifactId("maven-dependency-plugin"), version("2.8")), goal("copy-dependencies"), configuration(element(name("outputDirectory"),
                classloader.getAbsolutePath()), element(name("includeScope"), "runtime"), element(name("includeTypes"), "jar")), executionEnvironment(project, session, buildPluginManager));

        // delete the dependency-maven-plugin-markers directory
        try {
            FileUtils.deleteDirectory(new File(project.getBuild().getDirectory(), "dependency-maven-plugin-markers"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
