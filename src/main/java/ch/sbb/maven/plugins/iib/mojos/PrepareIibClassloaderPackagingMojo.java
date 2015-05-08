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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

/**
 * Copies the dependencies into a directory for packaging later
 * 
 * Implemented with help from: https://github.com/TimMoore/mojo-executor/blob/master/README.md
 * 
 * requiresDependencyResolution below is required for the unpack-dependencies goal to work correctly. See https://github.com/TimMoore/mojo-executor/issues/3
 */
@Mojo(name = "prepare-iib-classloader-packaging", requiresDependencyResolution = ResolutionScope.TEST)
public class PrepareIibClassloaderPackagingMojo extends AbstractMojo {

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
     * The path where the classloader jars will be copied to for packaging later.
     */
    @Parameter(property = "iib.classloaderPath", defaultValue = "${project.build.directory}/iib/classloader", required = true, readonly = true)
    protected File classloaderPath;

    public void execute() throws MojoExecutionException, MojoFailureException {
        // mvn
        // org.apache.maven.plugins:maven-dependency-plugin:2.1:copy-dependencies
        // -DoutputDirectory=${project.build.directory}/iib/classloader

        getLog().info("Emptying " + new File(project.getBuild().getDirectory(), "iib").getAbsolutePath());
        try {
            FileUtils.deleteDirectory(new File(project.getBuild().getDirectory(), "iib"));
        } catch (IOException e) {
            // ignore
        }

        executeMojo(plugin(groupId("org.apache.maven.plugins"), artifactId("maven-dependency-plugin"), version("2.8")), goal("copy-dependencies"), configuration(element(name("outputDirectory"),
                classloaderPath.getAbsolutePath()), element(name("includeScope"), "runtime"), element(name("includeTypes"), "jar")), executionEnvironment(project, session, buildPluginManager));

        // delete the dependency-maven-plugin-markers directory
        try {
            FileUtils.deleteDirectory(new File(project.getBuild().getDirectory(), "dependency-maven-plugin-markers"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
