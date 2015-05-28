package ch.sbb.maven.plugins.iib.mojos;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.dependency;
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
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
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
 * Creates a .par file from a iib-par Project
 * 
 * Implemented with help from: https://github.com/TimMoore/mojo-executor/blob/master/README.md
 */
@Mojo(name = "package-par")
public class PackageIibParMojo extends AbstractMojo {

    /**
     * The path to write the assemblies/iib-bar-project.xml file to before invoking the maven-assembly-plugin.
     */
    @Parameter(defaultValue = "${project.build.directory}/assemblies/iib-par-project.xml", readonly = true)
    private File buildAssemblyFile;

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

    public void execute() throws MojoFailureException, MojoExecutionException {

        packageIibBarArtifact();

    }

    private void packageIibBarArtifact() throws MojoFailureException, MojoExecutionException {
        InputStream is = this.getClass().getResourceAsStream("/assemblies/iib-par-project.xml");
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

        // mvn org.apache.maven.plugins:maven-assembly-plugin:2.4:single -Ddescriptor=target\assemblies\iib-classloader-project.xml -Dassembly.appendAssemblyId=false

        // for par packaging, the assembly plugin needs the par-archiver dependency
        List<Dependency> dependencies = new ArrayList<Dependency>();
        dependencies.add(dependency("ch.sbb.wmb.utils", "par-archiver", "1.1.0"));

        executeMojo(plugin(groupId("org.apache.maven.plugins"), artifactId("maven-assembly-plugin"), version("2.4"), dependencies), goal("single"),
                configuration(
                        element(name("descriptorRefs"), element("descriptorRef", "par")),
                        element(name("ignoreDirFormatExtensions"), "false"),
                        element(name("appendAssemblyId"), "false")), executionEnvironment(project, session, buildPluginManager));

        // delete the archive-tmp directory
        try {
            FileUtils.deleteDirectory(new File(project.getBuild().getDirectory(), "archive-tmp"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
