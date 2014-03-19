package ch.sbb.iib9.plugin.mojos;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import ch.sbb.iib9.plugin.utils.ProcessOutputLogger;
import ch.sbb.iib9.plugin.utils.ZipUtils;

/**
 * Creates a .bar file from a wmb-bar Project.
 * 
 * Implemented with help from: https://github.com/TimMoore/mojo-executor/blob/master/README.md
 * 
 * @goal create-bar
 * @requiresProject true
 * 
 */
public class CreateBarMojo extends AbstractMojo {

    /**
     * The name of the BAR (compressed file format) archive file where the result is stored.
     * 
     * @parameter expression="${wmb.barName}" default-value= "${project.build.directory}/wmb/${project.artifactId}-${project.version}.bar"
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
     * @parameter expression="${wmb.versionString}" default-value="${project.version}"
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
     * Pattern (or patterns, comma separated) of jars to be excluded from the generated bar file
     * 
     * @parameter expression="${wmb.discardJarsPattern}" default-value="**\/javacompute_**.jar,**\/jplugin2_**.jar"
     */
    protected String discardJarsPattern;

    /**
     * Whether classloaders are in use with this bar
     * 
     * @parameter expression="${wmb.useClassloaders}" default-value="false"
     * @since 1.5
     */
    protected Boolean useClassloaders;

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

        getLog().info("Creating bar file: " + barName);

        File barDir = barName.getParentFile();
        if (!barDir.exists()) {
            barDir.getParentFile().mkdirs();
        }

        List<String> params = constructParams();
        try {
            executeMqsiCreateBar(params);
        } catch (MojoFailureException e) {
            // A bug with the M2Eclipse Plugin causes it to fail to initialise. Restarting the same job
            // with an existing and now initialised Workspace sometimes helps, so we'll try it now.
            // With IIB9 Toolkit, a newer version of M2Eclipse can be used and the initialisation works
            // in headless mode. At that stage, this retry can be removed.
            executeMqsiCreateBar(params);
        }


        try {
            // if classloaders are in use, all jars are to be removed
            if (useClassloaders) {
                getLog().info("Classloaders in use. All jars will be removed from the bar file.");
                ZipUtils.removeFiles(barName, "**/*.jar");
            } else {
                // remove the jars specified with discardJarsPattern
                if (discardJarsPattern != null && !"".equals(discardJarsPattern)) {
                    getLog().info("Classloaders are not in use. The following jars will be removed from the bar file: " + discardJarsPattern);
                    ZipUtils.removeFiles(barName, discardJarsPattern);
                }
            }
        } catch (IOException e) {
            throw new MojoFailureException("Error removing jar files from bar file", e);
        }

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
            String workspaceProjects = getWorkspaceProjects();
            if (workspaceProjects == null || "".equals(workspaceProjects)) {
                throw (new MojoFailureException("No projects were found in the workspace: " + workspace.getAbsolutePath()));
            }
            params.add(workspaceProjects);
        }

        // object names - required
        params.add("-o");
        params.addAll(getObjectNames());

        return params;
    }

    /**
     * @return the names of the projects (actually, just all directories) in the workspace
     * @throws MojoFailureException
     */
    private String getWorkspaceProjects() throws MojoFailureException {

        String dirNames = null;

        for (File file : workspace.listFiles()) {
            if (!file.isDirectory() || file.getName().equals(".metadata")) {
                continue;
            }
            if (dirNames == null) {
                dirNames = file.getName();
            } else {
                dirNames = dirNames + " " + file.getName();
            }
        }

        return dirNames;
    }

    /**
     * @return a list of objects to be (explicitly) added to the bar file
     * @throws MojoFailureException
     */
    @SuppressWarnings("unchecked")
    private Collection<? extends String> getObjectNames() throws MojoFailureException {
        List<String> objectNames = new ArrayList<String>();

        // get the names of files under: the workspace directory, matching
        // includeFlowPatterns, not matching anything in a directory called
        // "tempfiles", excluding the base directory
        try {
            // since excludes is a regex and "\" is special for regexes, it must
            // be escaped. Not really sure if tempfiles pops up everywhere or
            // not

            String excludes = "tempfiles" + (File.separator == "\\" ? "\\\\" : File.pathSeparator) + "\\.*";
            if (excludeArtifactsPattern != null && excludeArtifactsPattern.length() > 1) {
                excludes = excludes + "," + excludeArtifactsPattern;
            }
            objectNames = FileUtils.getFileNames(workspace, includeArtifactsPattern, excludes, false);

        } catch (IOException e) {
            throw new MojoFailureException("Could not resolve includeArtifactsPattern: " + includeArtifactsPattern);
        }

        // make sure that we found something to add to the bar file
        if (objectNames.size() == 0) {
            throw new MojoFailureException("Nothing matched includeFlowsPattern: " + excludeArtifactsPattern + " excludeArtifactsPattern: " + excludeArtifactsPattern);
        }

        return objectNames;
    }

    /**
     * @throws MojoFailureException
     */
    protected void createWorkspaceDirectory() throws MojoFailureException {
        if (!workspace.exists()) {
            workspace.mkdirs();
        }
        if (!workspace.isDirectory()) {
            throw new MojoFailureException("Workspace parameter is not a directory: " + workspace.toString());
        }
    }

    /**
     * executes mqsicreatebar. Since mqsicreatebar does something strange with stdOut & stdErr, command must be written to a temporary file and executed from there.
     * 
     * @param params
     * @throws MojoFailureException
     */
    private void executeMqsiCreateBar(List<String> params) throws MojoFailureException {

        File cmdFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "createbarCommand-" + UUID.randomUUID() + ".cmd");

        // make sure that it will be cleaned up on exit
        cmdFile.deleteOnExit();

        // construct the command - very windows-centric for now
        List<String> command = new ArrayList<String>();
        String executable = "\"" + toolkitInstallDir + File.separator + "mqsicreatebar\"";
        command.add(executable);
        command.addAll(params);
        // command.add("> " + outFile.getAbsolutePath() + " 2>&1");

        if (getLog().isDebugEnabled()) {
            getLog().debug("executing command file: " + cmdFile.getAbsolutePath());
            getLog().debug("executeMqsiCreateBar command: " + getCommandLine(command));
        }

        try {
            FileUtils.fileWrite(cmdFile, getCommandLine(command));

            // make sure it can be executed on Unix
            cmdFile.setExecutable(true);
        } catch (IOException e1) {
            throw new MojoFailureException("Could not create command file: " + cmdFile.getAbsolutePath());
        }

        // ProcessBuilder pb = new ProcessBuilder(command);
        ProcessBuilder pb = new ProcessBuilder(cmdFile.getAbsolutePath());

        pb.directory(workspace);
        // redirect subprocess stderr to stdout
        pb.redirectErrorStream(true);
        Process process;
        ProcessOutputLogger stdOutHandler = null;
        try {
            process = pb.start();
            stdOutHandler = new ProcessOutputLogger(process.getInputStream(), getLog());
            stdOutHandler.start();
            process.waitFor();
        } catch (IOException e) {
            throw new MojoFailureException("Error executing: " + getCommandLine(command), e.getCause());
        } catch (InterruptedException e) {
            throw new MojoFailureException("Error executing: " + getCommandLine(command), e.getCause());
        } finally {
            if (stdOutHandler != null) {
                stdOutHandler.interrupt();
                try {
                    stdOutHandler.join();
                } catch (InterruptedException e) {
                    // this should never happen, so ignore this one
                }
            }
        }

        if (process.exitValue() != 0) {
            // logOutputFile(outFile, "error");
            throw new MojoFailureException("mqsicreate bar finished with exit code: " + process.exitValue());
        }
    }

    private String getCommandLine(List<String> command) {
        String ret = new String();
        for (String element : command) {
            ret = ret.concat(" ").concat(element);
        }
        return ret;
    }

}
