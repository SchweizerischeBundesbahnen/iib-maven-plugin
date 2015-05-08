package ch.sbb.maven.plugins.iib.utils;

import java.util.ArrayList;
import java.util.List;

public final class ConfigurablePropertiesUtil {

    /**
     * hide the default constructor
     */
    private ConfigurablePropertiesUtil() {
        super();
    }

    public static List<String> getJavaClassLoaderProperties(List<String> configurableProperties) {
        List<String> clProps = new ArrayList<String>();
        for (String propEntry : configurableProperties) {
            if (getPropName(propEntry).endsWith(".javaClassLoader")) {
                clProps.add(propEntry);
            }
        }

        return clProps;
    }

    public static List<String> getPropNames(List<String> configurableProperties) {
        List<String> propNames = new ArrayList<String>();
        for (String confProp : configurableProperties) {
            // use the value up to an equals sign if present
            propNames.add(getPropName(confProp));
        }
        return propNames;
    }

    public static String getPropName(String configurablePropertyEntry) {
        // use the value up to an equals sign if present
        return (configurablePropertyEntry.split("=")[0]).trim();
    }

    public static String getPropValue(String configurablePropertyEntry) {
        // use the value up to an equals sign if present
        if (configurablePropertyEntry.split("=").length == 2) {
            return (configurablePropertyEntry.split("=")[1]).trim();
        }
        return "";
    }

}
