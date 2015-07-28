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
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.execution.MavenSession;
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

import ch.sbb.maven.plugins.iib.utils.ApplyBarOverride;
import ch.sbb.maven.plugins.iib.utils.ConfigurableProperties;
import ch.sbb.maven.plugins.iib.utils.ReadBar;

/**
 * Validates override .properties files and (optionally) applies them to the default .bar file.
 */
@Mojo(name = "apply-bar-overrides", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class ApplyBarOverridesMojo extends AbstractMojo {

    /**
     * Projects containing files to include in the BAR file in the workspace. Required for a new workspace.
     */
    @Parameter(property = "iib.applicationName")
    protected String applicationName;

    /**
     * Whether the applybaroverride command should be executed or not
     */
    @Parameter(property = "iib.applybaroverrides", defaultValue = "true", required = true)
    protected Boolean applyBarOverrides;

    /**
     * Whether the applybaroverride command should be executed or not
     */
    @Parameter(property = "iib.applyBarOverrideRecursively", defaultValue = "true", required = true)
    protected Boolean applyBarOverrideRecursively;

    /**
     * The basename of the trace file to use when applybaroverriding bar files
     */
    @Parameter(property = "iib.applyBarOverrideTraceFile", defaultValue = "${project.build.directory}/applybaroverridetrace.txt", required = true)
    protected File applyBarOverrideTraceFile;

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
    @Parameter(property = "iib.defaultPropertiesFile", defaultValue = "${project.build.directory}/iib/default.properties", required = true)
    protected File defaultPropertiesFile;

    /**
     * Whether or not to fail the build if properties are found to be invalid.
     */
    @Parameter(property = "iib.failOnInvalidProperties", defaultValue = "true", required = true)
    protected Boolean failOnInvalidProperties;

    /**
     * Appends the _ (underscore) character and the value of VersionString to the names of the compiled versions of the message flows (.cmf) files added to the BAR file, before the file extension.
     */
    @Parameter(property = "iib.versionString", defaultValue = "${project.version}")
    protected String versionString;

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


    public void execute() throws MojoFailureException {

        copyAndFilterResources();

        getLog().info("Reading bar file: " + barName);

        ConfigurableProperties overridableProperties;
        try {
            overridableProperties = getOverridableProperties();
        } catch (IOException e) {
            throw new MojoFailureException("Error extracting configurable properties from bar file: " + barName.getAbsolutePath(), e);
        }

        writeToFile(overridableProperties, defaultPropertiesFile);

        validatePropertiesFiles(overridableProperties);

        if (applyBarOverrides) {
            executeApplyBarOverrides();
        }
    }


    private void copyAndFilterResources() throws MojoFailureException {

        getLog().debug("Project Build Resources: " + project.getBuild().getResources().toString());

        try {
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


        } catch (MojoExecutionException e) {
            // TODO handle exception
            throw new MojoFailureException("Error while copying and filtering resources", e);
        }
    }

    private void executeApplyBarOverrides() throws MojoFailureException {
        for (File propFile : getTargetPropertiesFiles()) {
            String targetBarFilename = (new File(propFile.getParent(), FilenameUtils.getBaseName(propFile.getName()).concat(".bar"))).getAbsolutePath();
            try {
                ApplyBarOverride.applyBarOverride(barName.getAbsolutePath(), propFile.getAbsolutePath(), targetBarFilename);
            } catch (IOException e) {
                // TODO handle exception
                throw new MojoFailureException("Error applying properties file " + propFile.getAbsolutePath(), e);
            }
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

    private void validatePropertiesFiles(ConfigurableProperties overrideableProperties) throws MojoFailureException {

        boolean invalidPropertiesFound = false;

        List<File> propFiles = null;
        propFiles = getTargetPropertiesFiles();
        getLog().info("Validating properties files");
        for (File file : propFiles) {
            getLog().info("  " + file.getAbsolutePath());
            try {
                ConfigurableProperties definedProps = new ConfigurableProperties();
                definedProps.load(defaultPropertiesFile);

                // check if all the defined properties are valid
                if (!overrideableProperties.keySet().containsAll(definedProps.keySet())) {

                    getLog().error("Invalid properties found in " + file.getAbsolutePath());
                    invalidPropertiesFound = true;

                    // list the invalid properties in this file
                    for (Object definedProp : definedProps.keySet()) {
                        if (!overrideableProperties.containsKey(ConfigurableProperties.getPropName((String) definedProp))) {
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

    private void writeToFile(ConfigurableProperties configurableProperties, File file) throws MojoFailureException {

        getLog().info("Writing overridable properties to: " + defaultPropertiesFile.getAbsolutePath());

        try {
            configurableProperties.save(defaultPropertiesFile);
        } catch (IOException e) {
            throw new MojoFailureException("Error writing properties file: " + file.getAbsolutePath(), e);
        }


    }

    /**
     * @return a sorted list of properties that can be overriden for a given bar file
     * @throws IOException
     */
    protected ConfigurableProperties getOverridableProperties() throws IOException {

        return ReadBar.getOverridableProperties(barName.getAbsolutePath());
    }

    @SuppressWarnings("unchecked")
    private List<File> getTargetPropertiesFiles() throws MojoFailureException {
        List<File> propFiles = null;

        File targetIibDirectory = new File(project.getBuild().getDirectory(), "iib");

        try {
            // TODO hard-coding this isn't great form
            // see also PrepareIibBarPackagingMojo.java
            propFiles = FileUtils.getFiles(targetIibDirectory, "*.properties", "default.properties");
            File targetIibTestDir = new File(project.getBuild().getDirectory(), "iib-test");
            if (targetIibTestDir.canRead()) {
                propFiles.addAll(FileUtils.getFiles(targetIibTestDir, "*.properties", ""));
            }
        } catch (IOException e) {
            // TODO handle exception
            throw new MojoFailureException("Error searching for properties files under " + targetIibDirectory, e);
        }

        return propFiles;
    }
}
