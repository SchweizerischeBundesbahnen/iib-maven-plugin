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
 * Copies dependent jar files into ${project.build.directory}/dependencies
 * 
 * @goal prepare-jar-classpath
 * @requiresProject true
 * @requiresDependencyResolution test
 * 
 */
public class PrepareJarClasspathMojo extends AbstractMojo {

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

    public void execute() throws MojoExecutionException, MojoFailureException {
        // mvn org.apache.maven.plugins:maven-dependency-plugin:2.1:unpack-dependencies -DoutputDirectory=${project.build.directory}/wmb/workspace

        copyJarDependencies("compile", new File(project.getBuild().getDirectory(), "dependency"));
        copyJarDependencies("test", new File(project.getBuild().getDirectory(), "test-dependency"));

    }

    /**
     * @param scope dependency Scope to be unpacked
     * @throws MojoExecutionException
     */
    private void copyJarDependencies(String scope, File targetDir) throws MojoExecutionException {

        // define the directory to be unpacked into and create it
        targetDir.mkdirs();

        // unpack all dependencies that match the given scope
        executeMojo(plugin(groupId("org.apache.maven.plugins"), artifactId("maven-dependency-plugin"), version("2.8")), goal("copy-dependencies"), configuration(element(name("outputDirectory"),
                targetDir.getAbsolutePath()), element(name("includeTypes"), "jar"), element(name("includeScope"), scope), element(name("stripVersion"), "true")), executionEnvironment(project,
                session, buildPluginManager));

        // delete the dependency-maven-plugin-markers directory
        try {
            FileUtils.deleteDirectory(new File(project.getBuild().getDirectory(), "dependency-maven-plugin-markers"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
