package ch.sbb.maven.plugins.iib.mojos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import ch.sbb.maven.plugins.iib.utils.ConfigurablePropertiesUtil;

public class ValidateConfigurablePropertiesMojoUnitTest {

    /**
     * tests that the method getIndentation() returns the correct values
     */
    @Test
    public void getIndentationTest() {
        ValidateConfigurablePropertiesMojo mojo = new ValidateConfigurablePropertiesMojo();
        assertTrue(mojo.getIndentation("Test") == 0);
        assertTrue(mojo.getIndentation("  Test") == 2);
        assertTrue(mojo.getIndentation("    Test") == 4);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getConfigurablePropertiesTest() {

        String outputFile = "mqsireadbar-output1.txt";
        InputStream outputStream = this.getClass().getResourceAsStream(outputFile);
        assertTrue("Error reading file: " + outputFile + ". Does it exist?", outputStream != null);

        String propertiesFile = "mqsireadbar-validProperties1.txt";
        InputStream propertiesStream = this.getClass().getResourceAsStream(propertiesFile);
        assertTrue("Error reading file: " + propertiesFile + ". Does it exist?", propertiesStream != null);

        ValidateConfigurablePropertiesMojo mojo = new ValidateConfigurablePropertiesMojo();
        List<String> outputLines = null;
        List<String> validProperties = null;
        try {
            outputLines = IOUtils.readLines(outputStream);
            validProperties = ConfigurablePropertiesUtil.getPropNames(IOUtils.readLines(propertiesStream));
        } catch (IOException e) {
            fail("Exception reading file: " + outputFile + " - " + e.getMessage());
        }
        List<String> discoveredProperties = ConfigurablePropertiesUtil.getPropNames(mojo.getConfigurableProperties(outputLines));

        // check that all the discoveredProperties are valid properties
        if (!validProperties.containsAll(discoveredProperties)) {
            discoveredProperties.removeAll(validProperties);
            fail("validProperties != discoveredProerties - following entries are missing: " + discoveredProperties.toString());
        }

        // check that all the validProperties are discovered (properties)
        if (!discoveredProperties.containsAll(validProperties)) {
            validProperties.removeAll(discoveredProperties);
            fail("validProperties != discoveredProerties - following entries are missing: " + validProperties.toString());
        }

    }

    @Test
    public void getTraceFileParameterTest() {
        ValidateConfigurablePropertiesMojo mojo = new ValidateConfigurablePropertiesMojo();
        mojo.applyBarOverrideTraceFile = new File("/tmp/dir/", "trace.txt");
        File propFile = new File("inte.properties");
        assertEquals(new File("/tmp/dir/", "trace-inte.txt").getAbsolutePath(), mojo.getTraceFileParameter(propFile));
    }
}
