package ch.sbb.iib.plugin.mojos;

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
import java.util.UUID;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import ch.sbb.iib.plugin.utils.ConfigurablePropertiesUtil;
import ch.sbb.iib.plugin.utils.ProcessOutputCatcher;

/**
 * Goal which reads the a bar file, including creating a list of configurable properties
 * 
 * @goal validate-configurable-properties
 * @phase package
 * @requiresProject true
 */
public class ValidateConfigurablePropertiesMojo extends AbstractMojo {

    /**
     * Whether the applybaroverride command should be executed or not
     * 
     * @parameter expression="${iib.applybaroverride}" default-value="true"
     * @required
     */
    protected Boolean applyBarOverride;

    /**
     * The name of the BAR (compressed file format) archive file where the result is stored.
     * 
     * @parameter expression="${iib.barName}" default-value="${project.build.directory}/iib/${project.artifactId}-${project.version}.bar"
     * @required
     */
    protected File barName;

    /**
     * The name of the default properties file to be generated from the bar file.
     * 
     * @parameter expression="${iib.configurablePropertiesFile}" default-value="${project.build.directory}/iib/default.properties"
     * @required
     */
    protected File defaultPropertiesFile;

    /**
     * Whether or not to fail the build if properties are found to be invalid.
     * 
     * @parameter expression="${iib.failOnInvalidProperties}" default-value="true"
     * @required
     */
    protected Boolean failOnInvalidProperties;

    /**
     * Installation directory of the IIB Toolkit
     * 
     * @parameter expression="${iib.toolkitInstallDir}"
     * @required
     */
    protected File toolkitInstallDir;


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

        copyAndFilterResources();

        getLog().info("Reading bar file: " + barName);

        List<String> params = new ArrayList<String>();
        params.add("-b");
        params.add(barName.getAbsolutePath());

        List<String> output = executeReadBar(params);

        List<String> configurableProperties = getConfigurableProperties(output);

        writeToFile(configurableProperties, defaultPropertiesFile);

        validatePropertiesFiles(ConfigurablePropertiesUtil.getPropNames(configurableProperties));

        if (applyBarOverride) {
            executeApplyBarOverrides();
        }
    }

    private void copyAndFilterResources() throws MojoFailureException, MojoExecutionException {

        getLog().error(project.getBuild().getResources().toString());

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

    private void executeApplyBarOverrides() throws MojoFailureException {

        try {
            getLog().info("Applying properties files as bar file overrides");
            for (File propFile : getTargetPropertiesFiles()) {

                getLog().info("  " + propFile.getAbsolutePath());

                List<String> params = new ArrayList<String>();
                params.add("-b");
                params.add(barName.getAbsolutePath());

                params.add("-o");
                String outputBarFile = new File(propFile.getParent(), propFile.getName().replaceAll("properties$", "bar")).getAbsolutePath();
                params.add(outputBarFile);

                params.add("-p");
                params.add(propFile.getAbsolutePath());

                executeApplyBarOverride(params);

            }
        } catch (IOException e) {
            throw new MojoFailureException("Error applying bar overrides", e);
        }
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
     * @return
     * @throws MojoFailureException
     */
    private ArrayList<String> executeApplyBarOverride(List<String> params) throws MojoFailureException {
        ArrayList<String> output = new ArrayList<String>();

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
        ProcessOutputCatcher stdOutHandler = null;
        try {
            pb.redirectErrorStream(true);
            process = pb.start();
            stdOutHandler = new ProcessOutputCatcher(process.getInputStream(), output);
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
        if (getLog().isDebugEnabled()) {
            Log log = getLog();
            for (String outputLine : output) {
                log.debug(outputLine);
            }
        }
        return output;
    }

    /**
     * @param params
     * @return
     * @throws MojoFailureException
     */
    private ArrayList<String> executeReadBar(List<String> params) throws MojoFailureException {
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
            throw new MojoFailureException("Could not create command file: " + cmdFile.getAbsolutePath(), e1);
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
            throw new MojoFailureException("Error creating configurable properties file: " + defaultPropertiesFile);
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
     * @param output
     * @return
     */
    private List<String> getConfigurableProperties(List<String> output) {
        // extract the configurable properties
        // 1. search the output for "  Deployment descriptor:"
        // 2. everything after that is a configurable property up until
        // 3. a blank line followed by "  BIP8071I: Successful command completion."
        boolean ddFound = false;

        // this could probably be done more efficiently with a subList
        List<String> configurableProperties = new ArrayList<String>();
        for (String outputLine : output) {
            if (!ddFound) {
                if ("  Deployment descriptor:".equals(outputLine)) {
                    ddFound = true;
                }
                continue;
            } else {
                // "  Deployment descriptor:" has been found
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
