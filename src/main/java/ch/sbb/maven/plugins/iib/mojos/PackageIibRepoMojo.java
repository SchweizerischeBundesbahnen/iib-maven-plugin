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
import org.twdata.maven.mojoexecutor.MojoExecutor.Attribute;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;

/**
 * Packages a WebSphere Message Broker Project.
 * 
 * Implemented with help from: https://github.com/TimMoore/mojo-executor/blob/master/README.md
 */
@Mojo(name = "package-iib-repo")
public class PackageIibRepoMojo extends AbstractMojo {

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
    @Parameter(defaultValue = "${project.build.directory}/assemblies/iib-repo-project.xml", readonly = true)
    private File buildAssemblyFile;

    public void execute() throws MojoExecutionException, MojoFailureException {
        InputStream is = this.getClass().getResourceAsStream("/assemblies/iib-repo-project.xml");
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

        // setup configuration:
        // <configuration>
        // <tasks>
        // <copy todir="${project.build.directory}/tmp">
        // <fileset dir="${basedir}/tmp">
        // <include name="**/*.xsd"/>
        // <include name="**/*.wsdl"/>
        // <include name="**/*.wadl"/>
        // <include name="**/pom.xml"/>
        // <include name="**/.project"/>
        // </fileset>
        // <flattenmapper/>
        // </copy>
        // </tasks>
        // </configuration>

        Element includeXsd = element(name("include"), new Attribute("name", "**/*.xsd"));
        Element includeWsdl = element(name("include"), new Attribute("name", "**/*.wsdl"));
        Element includeWadl = element(name("include"), new Attribute("name", "**/*.wadl"));
        Element includePom = element(name("include"), new Attribute("name", "**/pom.xml"));
        Element includeProjectFile = element(name("include"), new Attribute("name", "**/.project"));

        Element fileSet = element(name("fileset"), new Attribute("dir", "${basedir}/"), includePom, includeWadl, includeWsdl, includeXsd, includeProjectFile);
        Element flattenMapper = element(name("flattenmapper"));

        Element copy = element(name("copy"), new Attribute("todir", "${project.build.directory}/tmp"), fileSet, flattenMapper);

        Element task = element(name("tasks"), copy);

        executeMojo(plugin(groupId("org.apache.maven.plugins"), artifactId("maven-antrun-plugin"), version("1.3")), goal("run"),
                configuration(task),
                executionEnvironment(project, session, buildPluginManager));

        executeMojo(plugin(groupId("org.apache.maven.plugins"), artifactId("maven-assembly-plugin"), version("2.4")), goal("single"), configuration(element(name("descriptor"),
                "${project.build.directory}/assemblies/iib-repo-project.xml"), element(name("appendAssemblyId"), "false")),
                executionEnvironment(project, session, buildPluginManager));

        // delete the archive-tmp directory
        try {
            FileUtils.deleteDirectory(new File(project.getBuild().getDirectory(), "archive-tmp"));
            FileUtils.deleteDirectory(new File(project.getBuild().getDirectory(), "tmp"));
        } catch (IOException e) {
            // Fail silently
        }
    }

}
