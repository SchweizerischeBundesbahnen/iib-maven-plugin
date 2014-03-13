package ch.sbb.wmb7.plugin.mojos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;

import ch.sbb.wmb7.plugin.utils.ConfigurablePropertiesUtil;

/**
 * Goal which reads the default.properties file to figure out if the classloader approach for this bar project is consistent. Either all jar nodes in all flows must use a classloader or none of them
 * should.
 * 
 * @goal validate-classloader-approach
 * @requiresProject true
 */
public class ValidateClassloaderApproachMojo extends AbstractMojo {

    /**
     * The name of the default properties file to be generated from the bar file.
     * 
     * @parameter expression="${wmb.configurablePropertiesFile}" default-value="${project.build.directory}/wmb/default.properties"
     * @required
     */
    protected File defaultPropertiesFile;

    /**
     * Whether or not to fail the build if the classloader approach is invalid.
     * 
     * @parameter expression="${wmb.failOnInvalidClassloader}" default-value="true"
     * @required
     */
    protected Boolean failOnInvalidClassloader;

    /**
     * Whether classloaders are in use with this bar
     * 
     * @parameter expression="${wmb.useClassloaders}" default-value="false"
     * @since 1.5
     */
    protected Boolean useClassloaders;

    public void execute() throws MojoFailureException {

        // the defaultPropertiesFile will be created in an earlier Maven build
        // step
        List<String> configurableProperties;
        try {
            configurableProperties = readFromFile(defaultPropertiesFile);
        } catch (IOException e) {
            throw new MojoFailureException("Error reading " + defaultPropertiesFile, e);
        }

        if (getLog().isDebugEnabled()) {
            getLog().debug("Configurable Properties:");
            for (String property : configurableProperties) {
                getLog().debug("  " + property);

            }
        }

        // loop through the javaClassLoader properties to see if they're
        // consistent
        List<String> clProps = ConfigurablePropertiesUtil.getJavaClassLoaderProperties(configurableProperties);

        for (String clProp : clProps) {
            // if clDefined is null, this is the first entry
            boolean clValueDefined = !"".equals(ConfigurablePropertiesUtil.getPropValue(clProp));
            if (clValueDefined != useClassloaders) {
                logInconsistency(clProps);
                if (failOnInvalidClassloader) {
                    throw new MojoFailureException("Inconsistent classloader configuration. (wmb.useClassloaders = " + useClassloaders + ", classloader values defined = " + clValueDefined + ")");
                }
            }
        }
    }

    private void logInconsistency(List<String> clProps) {
        String logMsg = "Inconsistent classloader configuration. ${wmb.useClassloaders} == " + useClassloaders + ". If classloaders are in use, all Java Nodes should define a classloader:";
        if (failOnInvalidClassloader) {
            getLog().error(logMsg);
            for (String string : clProps) {
                getLog().error("  " + string);
            }
        } else {
            getLog().warn(logMsg);
            for (String string : clProps) {
                getLog().warn("  " + string);
            }
        }
    }

    private ArrayList<String> readFromFile(File file) throws IOException {

        ArrayList<String> configurableProperties = new ArrayList<String>();

        getLog().info("Reading configurable properties from: " + defaultPropertiesFile.getAbsolutePath());

        FileReader fr = null;
        try {
            fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);

            String line;
            while ((line = br.readLine()) != null) {
                configurableProperties.add(line);
            }
        } finally {
            try {
                if (fr != null) {
                    fr.close();
                }
            } catch (IOException e) {
                // ignore any error here
            }
        }

        return configurableProperties;
    }

}
