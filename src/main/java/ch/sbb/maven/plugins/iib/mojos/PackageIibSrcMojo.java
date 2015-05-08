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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

/**
 * Packages a WebSphere Message Broker Project.
 * 
 * Implemented with help from: https://github.com/TimMoore/mojo-executor/blob/master/README.md
 */
@Mojo(name = "package-src")
public class PackageIibSrcMojo extends AbstractMojo {

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
     * The path to write the assemblies/iib-src-project.xml file to before invoking the maven-assembly-plugin.
     */
    @Parameter(defaultValue = "${project.build.directory}/assemblies/iib-src-project.xml", readonly = true)
    private File buildAssemblyFile;

    public void execute() throws MojoExecutionException, MojoFailureException {
        InputStream is = this.getClass().getResourceAsStream("/assemblies/iib-src-project.xml");
        FileOutputStream fos;
        buildAssemblyFile.getParentFile().mkdirs();
        try {
            fos = new FileOutputStream(buildAssemblyFile);
        } catch (FileNotFoundException e) {
            // should never happen, as the file is packaged in this plugin's jar
            throw new MojoFailureException("Error creating the build assembly file: " + buildAssemblyFile, e);
        }
        try {
            IOUtil.copy(is, fos);
        } catch (IOException e) {
            // should never happen
            throw new MojoFailureException("Error creating the assembly file: " + buildAssemblyFile.getAbsolutePath(), e);
        }

        // mvn org.apache.maven.plugins:maven-assembly-plugin:2.4:single -Ddescriptor=target\assemblies\iib-src-project.xml -Dassembly.appendAssemblyId=false

        executeMojo(plugin(groupId("org.apache.maven.plugins"), artifactId("maven-assembly-plugin"), version("2.4")), goal("single"), configuration(element(name("descriptor"),
                "${project.build.directory}/assemblies/iib-src-project.xml"), element(name("appendAssemblyId"), "false")), executionEnvironment(project, session, buildPluginManager));

        // delete the archive-tmp directory
        try {
            FileUtils.deleteDirectory(new File(project.getBuild().getDirectory(), "archive-tmp"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
