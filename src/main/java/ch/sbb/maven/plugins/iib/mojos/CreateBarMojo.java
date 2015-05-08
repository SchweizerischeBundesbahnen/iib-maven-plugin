package ch.sbb.maven.plugins.iib.mojos;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import ch.sbb.maven.plugins.iib.utils.EclipseProjectUtils;
import ch.sbb.maven.plugins.iib.utils.ProcessOutputLogger;
import ch.sbb.maven.plugins.iib.utils.ZipUtils;

/**
 * Creates a .bar file from a iib-bar Project.
 * 
 * Implemented with help from: https://github.com/TimMoore/mojo-executor/blob/master/README.md
 */

@Mojo(name = "create-bar", defaultPhase = LifecyclePhase.COMPILE)
public class CreateBarMojo extends AbstractMojo {

    /**
     * The name of the BAR (compressed file format) archive file where the
     * result is stored.
     */
    @Parameter(property = "iib.barName", defaultValue = "${project.build.directory}/iib/${project.artifactId}-${project.version}.bar", required = true)
    protected File barName;

    /**
     * Refreshes the projects in the workspace and then invokes a clean build before new items are added to the BAR file.
     */
    @Parameter(property = "iib.cleanBuild", defaultValue = "true", required = true)
    protected boolean cleanBuild;

    /**
     * The name of the trace file to use when creating bar files
     */
    @Parameter(property = "iib.createBarTraceFile", defaultValue = "${project.build.directory}/createbartrace.txt", required = true)
    protected File createBarTraceFile;

    /**
     * Include "-deployAsSource" parameter?
     */
    @Parameter(property = "iib.deployAsSource", defaultValue = "true", required = true)
    protected boolean deployAsSource;

    /**
     * Compile ESQL for brokers at Version 2.1 of the product.
     */
    @Parameter(property = "iib.esql21", defaultValue = "false", required = true)
    protected boolean esql21;

    /**
     * Exclude artifacts pattern (or patterns, comma separated). By default, exclude pom.xml's as each project will have one and this causes a packaging error.
     */
    @Parameter(property = "iib.excludeArtifactsPattern", defaultValue = "**/pom.xml")
    protected String excludeArtifactsPattern;

    /**
     * Include artifacts pattern (or patterns, comma separated). By default, the default value used for mqsipackagebar, except .esql & .subflow, which as not compilable
     * 
     * @see <a href="http://www-01.ibm.com/support/knowledgecenter/SSMKHH_9.0.0/com.ibm.etools.mft.doc/bc31720_.htm">IIB9 Documentation</a>
     */
    @Parameter(property = "iib.includeArtifactsPattern", defaultValue = "**/*.xsdzip,**/*.tblxmi,**/*.xsd,**/*.wsdl,**/*.dictionary,**/*.xsl,**/*.xslt,**/*.xml,**/*.jar,**/*.inadapter,**/*.outadapter,**/*.insca,**/*.outsca,**/*.descriptor,**/*.php,**/*.idl,**/*.map,**/*.msgflow", required = true)
    protected String includeArtifactsPattern;

    /**
     * Projects containing files to include in the BAR file in the workspace. Required for a new workspace. A new workspace is a system folder which don't contain a .metadata folder.
     */
    @Parameter(property = "iib.projectName", defaultValue = "")
    protected String projectName;

    /**
     * Whether classloaders are in use with this bar
     */
    @Parameter(property = "iib.skipWSErrorCheck", defaultValue = "false")
    protected Boolean skipWSErrorCheck;

    /**
     * Installation directory of the IIB Toolkit
     */
    @Parameter(property = "iib.toolkitInstallDir", required = true)
    protected File toolkitInstallDir;

    /**
     * Major Version number of the IIB Toolkit. (Current not used, but will be needed when support for difference Versions with different options is supported)
     */
    @Parameter(property = "iib.toolkitVersion", defaultValue = "9")
    protected String toolkitVersion;

    /**
     * Appends the _ (underscore) character and the value of VersionString to the names of the compiled versions of the message flows (.cmf) files added to the BAR file, before the file extension.
     */
    @Parameter(property = "iib.versionString", defaultValue = "${project.version}")
    protected String versionString;

    /**
     * The path of the workspace in which the projects are extracted to be built.
     */
    @Parameter(property = "iib.workspace", defaultValue = "${project.build.directory}/iib/workspace", required = true)
    protected File workspace;

    /**
     * Pattern (or patterns, comma separated) of jars to be excluded from the generated bar file
     */
    @Parameter(property = "iib.discardJarsPattern", defaultValue = "**/javacompute_**.jar,**/jplugin2_**.jar")
    protected String discardJarsPattern;

    /**
     * Whether classloaders are in use with this bar
     */
    @Parameter(property = "iib.useClassloaders", defaultValue = "false", required = true)
    protected Boolean useClassloaders;

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

    private List<String> addObjectsAppsLibs() throws MojoFailureException {
        List<String> params = new ArrayList<String>();
        List<String> apps = new ArrayList<String>();
        List<String> libs = new ArrayList<String>();

        // loop through the projects, adding them as "-a" Applications, "-l"
        // libraries or the deployable artefacts as "-o" objects

        // List<String> workspaceProjects = EclipseProjectUtils.getWorkspaceProjects(workspace);

        // only direct dependencies of the current bar project will be added as Applications or Libraries
        // loop through them
        for (Dependency dependency : project.getDependencies()) {

            // the projectName is the directoryName is the artifactId
            projectName = dependency.getArtifactId();

            if (EclipseProjectUtils.isApplication(new File(workspace, projectName), getLog())) {
                apps.add(projectName);
            } else if (EclipseProjectUtils.isLibrary(new File(workspace, projectName), getLog())) {
                libs.add(projectName);
            }
        }

        // if there are applications, add them
        if (!apps.isEmpty()) {
            params.add("-a");
            params.addAll(apps);
        }

        // deployAsSource?
        if (deployAsSource) {
            params.add("-deployAsSource");
        }

        // if there are libraries, add them
        if (!libs.isEmpty()) {
            params.add("-l");
            params.addAll(libs);
        }

        // if there are no applications and no libraries, add "unmanaged" objects
        if (apps.isEmpty() && libs.isEmpty()) {
            params.add("-o");
            params.addAll(getObjectNames());
        }

        return params;
    }

    protected List<String> constructParams() throws MojoFailureException {
        List<String> params = new ArrayList<String>();

        // workspace parameter - required
        createWorkspaceDirectory();
        params.add("-data");
        params.add(workspace.toString());

        // bar file name - required
        params.add("-b");
        params.add(barName.getAbsolutePath());

        if (cleanBuild) {
            params.add("-cleanBuild");
        }

        if (versionString != null && versionString.length() != 0) {
            params.add("-version");
            params.add(versionString);
        }

        // esql21 - optional
        if (esql21) {
            params.add("-esql21");
        }

        // project name - optional

        params.add("-p");
        if (projectName != null) {
            params.add(projectName);
        } else {
            List<String> workspaceProjects = EclipseProjectUtils.getWorkspaceProjects(workspace);

            params.addAll(workspaceProjects);
        }

        // object names - required
        params.addAll(addObjectsAppsLibs());

        if (skipWSErrorCheck) {
            params.add("-skipWSErrorCheck");
        }

        // always trace into the file target/iib/mqsicreatebartrace.txt
        params.add("-trace");
        params.add("-v");
        params.add(createBarTraceFile.getAbsolutePath());

        return params;
    }

    /**
     * @throws MojoFailureException
     */
    protected void createWorkspaceDirectory() throws MojoFailureException {
        if (!workspace.exists()) {
            workspace.mkdirs();
        }
        if (!workspace.isDirectory()) {
            throw new MojoFailureException(
                    "Workspace parameter is not a directory: "
                            + workspace.toString());
        }
    }

    public void execute() throws MojoFailureException, MojoExecutionException {

        getLog().info("Creating bar file: " + barName);

        File barDir = barName.getParentFile();
        if (!barDir.exists()) {
            barDir.getParentFile().mkdirs();
        }

        List<String> params = constructParams();

        executeMqsiCreateBar(params);

        try {
            // if classloaders are in use, all jars are to be removed
            if (useClassloaders) {
                getLog().info(
                        "Classloaders in use. All jars will be removed from the bar file.");
                ZipUtils.removeFiles(barName, "**/*.jar");
            } else {
                // remove the jars specified with discardJarsPattern
                if (discardJarsPattern != null
                        && !"".equals(discardJarsPattern)) {
                    getLog().info(
                            "Classloaders are not in use. The following jars will be removed from the bar file: "
                                    + discardJarsPattern);
                    ZipUtils.removeFiles(barName, discardJarsPattern);
                }
            }
        } catch (IOException e) {
            throw new MojoFailureException(
                    "Error removing jar files from bar file", e);
        }

    }

    /**
     * executes mqsicreatebar. Since mqsicreatebar does something strange with
     * stdOut & stdErr, command must be written to a temporary file and executed
     * from there.
     * 
     * @param params
     * @throws MojoFailureException
     */
    private void executeMqsiCreateBar(List<String> params)
            throws MojoFailureException {

        File cmdFile = new File(System.getProperty("java.io.tmpdir")
                + File.separator + "createbarCommand-" + UUID.randomUUID()
                + ".cmd");

        // make sure that it will be cleaned up on exit
        cmdFile.deleteOnExit();

        // construct the command - very windows-centric for now
        List<String> command = new ArrayList<String>();
        String executable = "\"" + toolkitInstallDir + File.separator
                + "mqsicreatebar\"";
        command.add(executable);
        command.addAll(params);
        // command.add("> " + outFile.getAbsolutePath() + " 2>&1");

        if (getLog().isDebugEnabled()) {
            getLog().debug(
                    "executing command file: " + cmdFile.getAbsolutePath());
        }
        getLog().info(
                "executeMqsiCreateBar command: " + getCommandLine(command));

        try {
            FileUtils.fileWrite(cmdFile, getCommandLine(command));

            // make sure it can be executed on Unix
            cmdFile.setExecutable(true);
        } catch (IOException e1) {
            throw new MojoFailureException("Could not create command file: "
                    + cmdFile.getAbsolutePath(), e1);
        }

        // ProcessBuilder pb = new ProcessBuilder(command);
        ProcessBuilder pb = new ProcessBuilder(cmdFile.getAbsolutePath());

        pb.directory(workspace);
        // redirect subprocess stderr to stdout
        pb.redirectErrorStream(true);
        Process process;
        ProcessOutputLogger stdOutHandler = null;
        ProcessOutputLogger stdErrorHandler = null;
        try {
            process = pb.start();
            stdOutHandler = new ProcessOutputLogger(process.getInputStream(), getLog());
            stdErrorHandler = new ProcessOutputLogger(process.getErrorStream(), getLog());
            stdOutHandler.start();
            stdErrorHandler.start();
            process.waitFor();
        } catch (IOException e) {
            throw new MojoFailureException("Error executing: "
                    + getCommandLine(command), e);
        } catch (InterruptedException e) {
            throw new MojoFailureException("Error executing: "
                    + getCommandLine(command), e);
        } finally {
            if (stdOutHandler != null) {
                stdOutHandler.interrupt();
                try {
                    stdOutHandler.join();
                } catch (InterruptedException e) {
                    // this should never happen, so ignore this one
                }
            }
            if (stdErrorHandler != null) {
                stdErrorHandler.interrupt();
                try {
                    stdErrorHandler.join();
                } catch (InterruptedException e) {
                    // this should never happen, so ignore this one
                }
            }
        }

        if (process.exitValue() != 0) {
            // logOutputFile(outFile, "error");
            throw new MojoFailureException(
                    "mqsicreate bar finished with exit code: "
                            + process.exitValue());
        }
    }

    private String getCommandLine(List<String> command) {
        String ret = "";
        for (String element : command) {
            ret = ret.concat(" ").concat(element);
        }
        return ret;
    }

    /**
     * @return a list of objects to be (explicitly) added to the bar file
     * @throws MojoFailureException
     */
    @SuppressWarnings("unchecked")
    private Collection<? extends String> getObjectNames()
            throws MojoFailureException {
        List<String> objectNames = new ArrayList<String>();

        // get the names of files under: the workspace directory, matching
        // includeFlowPatterns, not matching anything in a directory called
        // "tempfiles", excluding the base directory
        try {
            // since excludes is a regex and "\" is special for regexes, it must
            // be escaped. Not really sure if tempfiles pops up everywhere or
            // not

            String excludes = "tempfiles"
                    + (File.separator == "\\" ? "\\\\" : File.pathSeparator)
                    + "\\.*";
            if (excludeArtifactsPattern != null
                    && excludeArtifactsPattern.length() > 1) {
                excludes = excludes + "," + excludeArtifactsPattern;
            }
            objectNames = FileUtils.getFileNames(workspace,
                    includeArtifactsPattern, excludes, false);

        } catch (IOException e) {
            throw new MojoFailureException(
                    "Could not resolve includeArtifactsPattern: "
                            + includeArtifactsPattern, e);
        }

        // make sure that we found something to add to the bar file
        // if (objectNames.size() == 0) {
        // throw new MojoFailureException(
        // "Nothing matched includeFlowsPattern: "
        // + excludeArtifactsPattern
        // + " excludeArtifactsPattern: "
        // + excludeArtifactsPattern);
        // }

        return objectNames;
    }

}
