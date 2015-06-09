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
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import ch.sbb.maven.plugins.iib.utils.ConfigurablePropertiesUtil;
import ch.sbb.maven.plugins.iib.utils.ProcessOutputCatcher;
import ch.sbb.maven.plugins.iib.utils.ProcessOutputLogger;

/**
 * Goal which reads the a bar file, including creating a list of configurable properties
 */
@Mojo(name = "validate-configurable-properties", defaultPhase = LifecyclePhase.PACKAGE)
public class ValidateConfigurablePropertiesMojo extends AbstractMojo {

    /**
     * Whether the applybaroverride command should be executed or not
     */
    @Parameter(property = "iib.applybaroverride", defaultValue = "true", required = true)
    protected Boolean applyBarOverride;

    /**
     * The name of the BAR (compressed file format) archive file where the result is stored.
     * 
     */
    @Parameter(property = "iib.barName", defaultValue = "${project.build.directory}/iib/${project.artifactId}-${project.version}.bar", required = true)
    protected File barName;

    /**
     * The name of the default properties file to be generated from the bar file.
     * 
     */
    @Parameter(property = "iib.configurablePropertiesFile", defaultValue = "${project.build.directory}/iib/default.properties", required = true)
    protected File defaultPropertiesFile;

    /**
     * Whether or not to fail the build if properties are found to be invalid.
     */
    @Parameter(property = "iib.failOnInvalidProperties", defaultValue = "true", required = true)
    protected Boolean failOnInvalidProperties;

    /**
     * Installation directory of the IIB Toolkit
     */
    @Parameter(property = "iib.toolkitInstallDir", required = true)
    protected File toolkitInstallDir;

    /**
     * The path of the workspace in which the projects are extracted to be built.
     */
    @Parameter(property = "iib.workspace", defaultValue = "${project.build.directory}/iib/workspace", required = true)
    protected File workspace;


    /**
     * The basename of the trace file to use when applybaroverriding bar files
     */
    @Parameter(property = "iib.applyBarOverrideTraceFile", defaultValue = "${project.build.directory}/applybaroverridetrace.txt", required = true)
    protected File applyBarOverrideTraceFile;

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
     * Projects containing files to include in the BAR file in the workspace. Required for a new workspace. A new workspace is a system folder which don't contain a .metadata folder.
     */
    @Parameter(property = "iib.applicationName")
    protected String applicationName;


    public void execute() throws MojoFailureException, MojoExecutionException {

        copyAndFilterResources();

        getLog().info("Reading bar file: " + barName);

        List<String> params = new ArrayList<String>();
        params.add("-b");
        params.add(barName.getAbsolutePath());

        // process the bar file recursively (applies to applications and libraries)
        params.add("-r");

        List<String> output = executeReadBar(params);

        List<String> configurableProperties = getConfigurableProperties(output);

        writeToFile(configurableProperties, defaultPropertiesFile);

        validatePropertiesFiles(ConfigurablePropertiesUtil.getPropNames(configurableProperties));

        if (applyBarOverride) {
            executeApplyBarOverrides();
        }
    }

    private void copyAndFilterResources() throws MojoFailureException, MojoExecutionException {

        getLog().debug("Project Build Resources: " + project.getBuild().getResources().toString());

        // copy the main resources
        executeMojo(plugin(groupId("org.apache.maven.plugins"), artifactId("maven-resources-plugin"), version("2.6")), goal("copy-resources"), configuration(element(name("outputDirectory"),
                "${project.build.directory}/iib"), element(name("resources"), element(name("resource"),
                // TODO hard-coding this isn't great form
                // see also ValidateConfigurablePropertiesMojo.java
                element(name("directory"), "src/main/resources"), element(name("filtering"), "true")))), executionEnvironment(project, session, buildPluginManager));

        // copy the test resources
        executeMojo(plugin(groupId("org.apache.maven.plugins"), artifactId("maven-resources-plugin"), version("2.6")), goal("copy-resources"), configuration(element(name("outputDirectory"),
                "${project.build.directory}/iib-test"), element(name("resources"), element(name("resource"),
                // TODO hard-coding this isn't great form
                // see also ValidateConfigurablePropertiesMojo.java
                element(name("directory"), "src/test/resources"), element(name("filtering"), "true")))), executionEnvironment(project, session, buildPluginManager));


    }

    private void executeApplyBarOverrides() throws MojoFailureException, MojoExecutionException {

        try {
            getLog().info("Applying properties files as bar file overrides");
            for (File propFile : getTargetPropertiesFiles()) {

                getLog().info("  " + propFile.getAbsolutePath());

                List<String> params = new ArrayList<String>();

                // (Required) The path to the BAR file.
                params.add("-b");
                params.add(barName.getAbsolutePath());

                // (Optional) The name of the output BAR file to which the BAR file changes are to be made.
                params.add("-o");
                String outputBarFile = new File(propFile.getParent(), propFile.getName().replaceAll("properties$", "bar")).getAbsolutePath();
                params.add(outputBarFile);

                // (Optional) The path to one of the following resources:
                // - A BAR file that contains the deployment descriptor.
                // - A properties file in which each line contains a property-name=override.
                // - A deployment descriptor that is used to apply overrides to the BAR file.
                params.add("-p");
                params.add(propFile.getAbsolutePath());

                // (Optional) The name of an application in the BAR file
                params.add("-k");
                params.add(getApplicationName());

                // (Optional) A list of the property-name=override pairs, current-property-value=override pairs.
                // -m

                // (Optional) Specifies that all deployment descriptor files are updated recursively.
                // -r

                // (Optional) Specifies that the internal trace is to be sent to the named file.
                params.add("-v");
                params.add(getTraceFileParameter(propFile));

                // (Optional) The name of a library in the BAR file to which to apply overrides.
                // -y

                executeApplyBarOverride(params);

            }
        } catch (IOException e) {
            throw new MojoFailureException("Error applying bar overrides", e);
        }
    }

    /**
     * @param propFile the name of the apply bar override property file
     * @return the value to be passed to the (-v) Trace parameter on the command line
     */
    protected String getTraceFileParameter(File propFile) {
        String filename = FilenameUtils.getBaseName(applyBarOverrideTraceFile.getAbsolutePath()) + "-" + FilenameUtils.getBaseName(propFile.getName()) + ".txt";
        String directory = applyBarOverrideTraceFile.getParent();
        return new File(directory, filename).getAbsolutePath();
    }

    private String getApplicationName() throws MojoExecutionException {
        // if the application name is specified, use it
        if (applicationName != null && !applicationName.isEmpty()) {
            return applicationName;
        }

        // application name not specified, look for a single dependency
        Set<Artifact> artifacts = project.getDependencyArtifacts();
        if (artifacts.size() == 1) {
            // there is only one dependency - it should be an application
            // there is only one, but we have to use the iterator...
            for (Artifact artifact : artifacts) {
                // return the artifactId of the first (only) artifact
                return artifact.getArtifactId();
            }
        }

        // TODO return project.getArtifactId()artifacts - "bar" + "app"

        throw new MojoExecutionException("Unable to determine application to be overriden");
    }

    @SuppressWarnings("unchecked")
    private void validatePropertiesFiles(List<String> validProps) throws MojoFailureException {

        boolean invalidPropertiesFound = false;

        List<File> propFiles = null;
        try {
            propFiles = getTargetPropertiesFiles();
        } catch (IOException e) {
            throw new MojoFailureException("Error searching for properties files", e);
        }
        getLog().info("Validating properties files");
        for (File file : propFiles) {
            getLog().info("  " + file.getAbsolutePath());
            try {
                List<String> definedProps = FileUtils.loadFile(file);

                // check if all the defined properties are valid
                if (!validProps.containsAll(ConfigurablePropertiesUtil.getPropNames(definedProps))) {

                    getLog().error("Invalid properties found in " + file.getAbsolutePath());
                    invalidPropertiesFound = true;

                    // list the invalid properties in this file
                    for (String definedProp : definedProps) {
                        if (!validProps.contains(ConfigurablePropertiesUtil.getPropName(definedProp))) {
                            getLog().error("  " + definedProp);
                        }
                    }
                }

            } catch (IOException e) {
                throw new MojoFailureException("Error loading properties file: " + file.getAbsolutePath(), e);
            }
        }

        if (failOnInvalidProperties && invalidPropertiesFound) {
            throw new MojoFailureException("Invalid properties were found");
        }
    }

    /**
     * @param params
     * @throws MojoFailureException
     */
    private void executeApplyBarOverride(List<String> params) throws MojoFailureException {

        File cmdFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "applybaroverrideCommand-" + UUID.randomUUID() + ".cmd");

        // make sure that it will be cleaned up on exit
        cmdFile.deleteOnExit();

        // construct the command - very windows-centric for now
        List<String> command = new ArrayList<String>();
        String executable = "\"" + toolkitInstallDir + File.separator + "mqsiapplybaroverride\"";
        command.add(executable);
        command.addAll(params);
        // command.add("> " + outFile.getAbsolutePath() + " 2>&1");

        if (getLog().isDebugEnabled()) {
            getLog().debug("executing command file: " + cmdFile.getAbsolutePath());
            getLog().debug("executeMqsiApplyBarOverride command: " + getCommandLine(command));
        }

        try {
            FileUtils.fileWrite(cmdFile, getCommandLine(command));

            // make sure it can be executed on Unix
            cmdFile.setExecutable(true);
        } catch (IOException e1) {
            throw new MojoFailureException("Could not create command file: " + cmdFile.getAbsolutePath(), e1);
        }

        // ProcessBuilder pb = new ProcessBuilder(command);
        ProcessBuilder pb = new ProcessBuilder(cmdFile.getAbsolutePath());

        // redirect subprocess stderr to stdout
        pb.redirectErrorStream(true);
        Process process;
        ProcessOutputLogger stdOutHandler = null;
        try {
            pb.redirectErrorStream(true);
            process = pb.start();
            stdOutHandler = new ProcessOutputLogger(process.getInputStream(), getLog());
            stdOutHandler.start();
            process.waitFor();

        } catch (IOException e) {
            throw new MojoFailureException("Error executing: " + getCommandLine(command), e);
        } catch (InterruptedException e) {
            throw new MojoFailureException("Error executing: " + getCommandLine(command), e);
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
            throw new MojoFailureException("mqsiapplybaroverride finished with exit code: " + process.exitValue());
        }

        getLog().debug("mqsiapplybaroverride complete");

    }

    /**
     * @param params the parameters to be used with the mqsireadbar command
     * @return the screen output of the executed mqsireadbar command
     * @throws MojoFailureException
     */
    private List<String> executeReadBar(List<String> params) throws MojoFailureException {
        ArrayList<String> output = new ArrayList<String>();

        File cmdFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "readbarCommand-" + UUID.randomUUID() + ".cmd");

        // make sure that it will be cleaned up on exit
        cmdFile.deleteOnExit();

        // construct the command - very windows-centric for now
        List<String> command = new ArrayList<String>();
        String executable = "\"" + toolkitInstallDir + File.separator + "mqsireadbar\"";
        command.add(executable);
        command.addAll(params);
        // command.add("> " + outFile.getAbsolutePath() + " 2>&1");

        if (getLog().isDebugEnabled()) {
            getLog().debug("executing command file: " + cmdFile.getAbsolutePath());
            getLog().debug("executeMqsiReadBar command: " + getCommandLine(command));
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

        // redirect subprocess stderr to stdout
        pb.redirectErrorStream(true);
        Process process;
        ProcessOutputCatcher stdOutHandler = null;
        try {
            pb.redirectErrorStream(true);
            process = pb.start();
            stdOutHandler = new ProcessOutputCatcher(process.getInputStream(), output);
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
            throw new MojoFailureException("mqsireadbar finished with exit code: " + process.exitValue());
        }

        getLog().info("mqsireadbar complete");
        if (getLog().isDebugEnabled()) {
            Log log = getLog();
            for (String outputLine : output) {
                log.debug(outputLine);
            }
        }
        return output;
    }

    private void writeToFile(List<String> configurableProperties, File file) throws MojoFailureException {

        getLog().info("Writing configurable properties to: " + defaultPropertiesFile.getAbsolutePath());

        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            for (String prop : configurableProperties) {
                writer.write(prop + System.getProperty("line.separator"));
            }
        } catch (IOException e) {
            throw new MojoFailureException("Error creating configurable properties file: " + defaultPropertiesFile, e);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                // ignore any error here
            }

        }

    }

    /**
     * @param output the output of the mqsireadbar command for a given bar file
     * @return a list of properties that can be overriden for a given bar file
     */
    protected List<String> getConfigurableProperties(List<String> output) {
        // extract the configurable properties

        // output format changed for iib9...

        // 1. search the output for a line indented with spaces followed by "Deployment descriptor:"
        // 2. everything after that is a configurable property up until
        // 3.a. a line less indented than the output line from "1" above
        // OR
        // 3.b. a blank line followed by "  BIP8071I: Successful command completion."
        boolean inDeploymentDescriptor = false;
        int currentIndentation = 0;

        // this could probably be done more efficiently with a subList
        List<String> configurableProperties = new ArrayList<String>();
        for (String outputLine : output) {
            if (!inDeploymentDescriptor) {
                if (outputLine.matches(" *Deployment descriptor:")) {
                    inDeploymentDescriptor = true;

                    // calculate how far indented the outputLine is
                    currentIndentation = getIndentation(outputLine);
                }
                continue;
            } else {
                // inDeploymentDescriptor == true, check that it hasn't ended
                if (getIndentation(outputLine) < currentIndentation) {
                    // reset and continue
                    currentIndentation = 0;
                    inDeploymentDescriptor = false;
                    continue;
                }

                if (!outputLine.trim().equals("")) {
                    configurableProperties.add(outputLine.trim());
                } else {
                    // we found a blank line - assume it's the one before
                    // "  BIP8071I: Successful command completion." and stop
                    break;
                }
            }
        }
        return configurableProperties;
    }

    /**
     * @param outputLine
     * @return
     */
    protected int getIndentation(String outputLine) {
        return outputLine.length() - outputLine.replaceAll("^ *", "").length();
    }

    private String getCommandLine(List<String> command) {
        String ret = "";
        for (String element : command) {
            ret = ret.concat(" ").concat(element);
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    private List<File> getTargetPropertiesFiles() throws IOException {
        List<File> propFiles = null;

        // TODO hard-coding this isn't great form
        // see also PrepareIibBarPackagingMojo.java
        propFiles = FileUtils.getFiles(new File(project.getBuild().getDirectory(), "iib"), "*.properties", "default.properties");
        File targetIibTestDir = new File(project.getBuild().getDirectory(), "iib-test");
        if (targetIibTestDir.canRead()) {
            propFiles.addAll(FileUtils.getFiles(targetIibTestDir, "*.properties", ""));
        }

        return propFiles;
    }


}
