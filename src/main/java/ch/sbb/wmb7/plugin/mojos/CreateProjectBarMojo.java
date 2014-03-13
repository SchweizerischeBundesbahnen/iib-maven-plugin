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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

/**
 * Creates a .bar file from a wmb-bar Project.
 * 
 * Implemented with help from: https://github.com/TimMoore/mojo-executor/blob/master/README.md
 * 
 * @goal create-project-bar
 * @requiresProject true
 * 
 */
public class CreateProjectBarMojo extends CreateBarMojo {

    /**
     * The name of the BAR (compressed file format) archive file where the result is stored.
     * 
     * @parameter expression="${wmb.barName}" default-value="${project.build.directory}/wmb/${project.artifactId}-${project.version}.bar"
     * @required
     */
    protected File barName;

    /**
     * Refreshes the projects in the workspace and then invokes a clean build before new items are added to the BAR file.
     * 
     * @parameter expression="${wmb.cleanBuild}" default-value="true"
     * @required
     */
    protected boolean cleanBuild;

    /**
     * Compile ESQL for brokers at Version 2.1 of the product.
     * 
     * @parameter expression="${wmb.esql21}" default-value="false"
     * @required
     */
    protected boolean esql21;

    /**
     * Exclude artifacts pattern (or patterns, comma separated)
     * 
     * @parameter expression="${wmb.excludeArtifactsPattern}" default-value=""
     */
    protected String excludeArtifactsPattern;

    /**
     * Include artifacts pattern (or patterns, comma separated)
     * 
     * @parameter expression="${wmb.includeArtifactsPattern}" default-value="**\/*\.msgflow,**\/*\.mset"
     * @required
     */
    protected String includeArtifactsPattern;

    /**
     * Projects containing files to include in the BAR file in the workspace. Required for a new workspace. A new workspace is a system folder which don't contain a .metadata folder.
     * 
     * @parameter expression="${wmb.projectName}" default-value=""
     */
    protected String projectName;

    /**
     * Installation directory of the WMB Toolkit
     * 
     * @parameter expression="${wmb.toolkitInstallDir}"
     * @required
     */
    protected File toolkitInstallDir;

    /**
     * Major Version number of the WMB Toolkit. (Current not used, but will be needed when support for difference Versions with different options is supported)
     * 
     * @parameter expression="${wmb.toolkitVersion}" default-value="7"
     */
    protected String toolkitVersion;

    /**
     * Appends the _ (underscore) character and the value of VersionString to the names of the compiled versions of the message flows (.cmf) files added to the BAR file, before the file extension.
     * 
     * @parameter expression="${wmb.versionString}" default-value=""
     */
    protected String versionString;

    /**
     * The path of the workspace in which the projects are extracted to be built.
     * 
     * @parameter expression="${wmb.workspace}" default-value="${project.build.directory}/wmb/workspace"
     * @required
     */
    protected File workspace;

    /**
     * The path to write the assemblies/wmb-bar-project.xml file to before invoking the maven-assembly-plugin.
     * 
     * @parameter default-value="${project.build.directory}/assemblies/wmb-bar-project.xml"
     * @readonly
     */
    private File buildAssemblyFile;

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

    @Override
    public void execute() throws MojoFailureException, MojoExecutionException {

        executeCreateBar();

        packageWmbBarArtifact();

    }

    /**
     * @throws MojoFailureException
     * @throws MojoExecutionException
     */
    private void executeCreateBar() throws MojoFailureException, MojoExecutionException {
        // pass up the modified default values
        super.barName = this.barName;
        super.cleanBuild = this.cleanBuild;
        super.esql21 = this.esql21;
        super.excludeArtifactsPattern = this.excludeArtifactsPattern;
        super.includeArtifactsPattern = this.includeArtifactsPattern;
        super.projectName = this.projectName;
        super.toolkitInstallDir = this.toolkitInstallDir;
        super.toolkitVersion = this.toolkitVersion;
        super.versionString = this.versionString;
        super.workspace = this.workspace;

        super.execute();
    }

    private void packageWmbBarArtifact() throws MojoFailureException, MojoExecutionException {
        InputStream is = this.getClass().getResourceAsStream("/assemblies/wmb-bar-project.xml");
        FileOutputStream fos;
        buildAssemblyFile.getParentFile().mkdirs();
        try {
            fos = new FileOutputStream(buildAssemblyFile);
        } catch (FileNotFoundException e) {
            // should never happen, as the file is packaged in this plugin's jar
            throw new MojoFailureException("Error creating the build assembly file: " + buildAssemblyFile);
        }
        try {
            IOUtil.copy(is, fos);
        } catch (IOException e) {
            // should never happen
            throw new MojoFailureException("Error creating the assembly file: " + buildAssemblyFile.getAbsolutePath());
        }

        // mvn org.apache.maven.plugins:maven-assembly-plugin:2.4:single -Ddescriptor=target\assemblies\wmb-bar-project.xml -Dassembly.appendAssemblyId=false

        executeMojo(plugin(groupId("org.apache.maven.plugins"), artifactId("maven-assembly-plugin"), version("2.4")), goal("single"), configuration(element(name("descriptor"),
                "${project.build.directory}/assemblies/wmb-bar-project.xml"), element(name("appendAssemblyId"), "false")), executionEnvironment(project, session, buildPluginManager));

        // delete the archive-tmp directory
        try {
            FileUtils.deleteDirectory(new File(project.getBuild().getDirectory(), "archive-tmp"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
