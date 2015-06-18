package ch.sbb.maven.plugins.iib.utils;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ConfigurablePropertiesUtilTest {


    @Test
    public void getPropNameTest() {
        assertEquals("a", ConfigurablePropertiesUtil.getPropName("a"));
        assertEquals("a", ConfigurablePropertiesUtil.getPropName("a=1"));
        assertEquals("a", ConfigurablePropertiesUtil.getPropName("a = 1"));
        assertEquals("a", ConfigurablePropertiesUtil.getPropName(" a  "));
        assertEquals("a", ConfigurablePropertiesUtil.getPropName(" a=1  "));
        assertEquals("a", ConfigurablePropertiesUtil.getPropName(" a = 1  "));
        assertEquals("a#b", ConfigurablePropertiesUtil.getPropName("a#b"));
        assertEquals("a#b", ConfigurablePropertiesUtil.getPropName("a#b=1"));
        assertEquals("a#b", ConfigurablePropertiesUtil.getPropName("a#b = 1"));
        assertEquals("a#b", ConfigurablePropertiesUtil.getPropName("  a#b  "));
        assertEquals("a#b", ConfigurablePropertiesUtil.getPropName("  a#b=1  "));
        assertEquals("a#b", ConfigurablePropertiesUtil.getPropName("  a#b = 1  "));
    }

    @Test
    public void getPropValueTest() {
        assertEquals("", ConfigurablePropertiesUtil.getPropValue("a"));
        assertEquals("1", ConfigurablePropertiesUtil.getPropValue("a=1"));
        assertEquals("1", ConfigurablePropertiesUtil.getPropValue("a = 1"));
        assertEquals("", ConfigurablePropertiesUtil.getPropValue(" a  "));
        assertEquals("1", ConfigurablePropertiesUtil.getPropValue(" a=1  "));
        assertEquals("1", ConfigurablePropertiesUtil.getPropValue(" a = 1  "));
        assertEquals("", ConfigurablePropertiesUtil.getPropValue("a#b"));
        assertEquals("1", ConfigurablePropertiesUtil.getPropValue("a#b=1"));
        assertEquals("1", ConfigurablePropertiesUtil.getPropValue("a#b = 1"));
        assertEquals("", ConfigurablePropertiesUtil.getPropValue("  a#b  "));
        assertEquals("1", ConfigurablePropertiesUtil.getPropValue("  a#b=1  "));
        assertEquals("1", ConfigurablePropertiesUtil.getPropValue("  a#b = 1  "));
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


        List<String> resultProps = ConfigurablePropertiesUtil.getJavaClassLoaderProperties(sampleProps);

        assertEquals(expectedProps, resultProps);

    }

}
