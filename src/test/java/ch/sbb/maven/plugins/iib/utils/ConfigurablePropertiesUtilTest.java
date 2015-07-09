package ch.sbb.maven.plugins.iib.utils;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ConfigurablePropertiesUtilTest {


    @Test
    public void getPropNameTest() {
        assertEquals("a", ConfigurableProperties.getPropName("a"));
        assertEquals("a", ConfigurableProperties.getPropName("a=1"));
        assertEquals("a", ConfigurableProperties.getPropName("a = 1"));
        assertEquals("a", ConfigurableProperties.getPropName(" a  "));
        assertEquals("a", ConfigurableProperties.getPropName(" a=1  "));
        assertEquals("a", ConfigurableProperties.getPropName(" a = 1  "));
        assertEquals("a#b", ConfigurableProperties.getPropName("a#b"));
        assertEquals("a#b", ConfigurableProperties.getPropName("a#b=1"));
        assertEquals("a#b", ConfigurableProperties.getPropName("a#b = 1"));
        assertEquals("a#b", ConfigurableProperties.getPropName("  a#b  "));
        assertEquals("a#b", ConfigurableProperties.getPropName("  a#b=1  "));
        assertEquals("a#b", ConfigurableProperties.getPropName("  a#b = 1  "));
    }

    @Test
    public void getPropValueTest() {
        assertEquals("", ConfigurableProperties.getPropValue("a"));
        assertEquals("1", ConfigurableProperties.getPropValue("a=1"));
        assertEquals("1", ConfigurableProperties.getPropValue("a = 1"));
        assertEquals("", ConfigurableProperties.getPropValue(" a  "));
        assertEquals("1", ConfigurableProperties.getPropValue(" a=1  "));
        assertEquals("1", ConfigurableProperties.getPropValue(" a = 1  "));
        assertEquals("", ConfigurableProperties.getPropValue("a#b"));
        assertEquals("1", ConfigurableProperties.getPropValue("a#b=1"));
        assertEquals("1", ConfigurableProperties.getPropValue("a#b = 1"));
        assertEquals("", ConfigurableProperties.getPropValue("  a#b  "));
        assertEquals("1", ConfigurableProperties.getPropValue("  a#b=1  "));
        assertEquals("1", ConfigurableProperties.getPropValue("  a#b = 1  "));
    }

    @Test
    public void getJavaClassLoaderPropertiesTest() {
        List<String> sampleProps = new ArrayList<String>();
        sampleProps.add("Flow1#Java Node");
        sampleProps.add("Flow1#javaClassLoader");
        sampleProps.add("Flow1#Java Node.javaClassLoader");
        sampleProps.add("Flow2#Java Node=x");
        sampleProps.add("Flow2#javaClassLoader=x");
        sampleProps.add("Flow2#Java Node.javaClassLoader=x");

        List<String> expectedProps = new ArrayList<String>();
        expectedProps.add("Flow1#Java Node.javaClassLoader");
        expectedProps.add("Flow2#Java Node.javaClassLoader=x");


        List<String> resultProps = ConfigurableProperties.getJavaClassLoaderProperties(sampleProps);

        assertEquals(expectedProps, resultProps);

    }

}
