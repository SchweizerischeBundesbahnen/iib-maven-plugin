package ch.sbb.maven.plugins.iib.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

@SuppressWarnings("serial")
public final class ConfigurableProperties extends TreeMap<String, String> {


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


    public void load(File file) throws IOException {

        // empty the existing data
        this.clear();

        // read the file line by line, adding it to the existing data
        LineIterator iterator = FileUtils.lineIterator(file, "UTF-8");
        try {
            while (iterator.hasNext()) {
                String line = iterator.nextLine();

                String key;
                String value;

                Integer equalsPos = line.indexOf("=");

                // if there is no "=", only the key is present
                if (equalsPos == -1) {
                    key = line.trim();
                    value = "";
                } else {
                    // there is an "=" present, so split out the key & value
                    key = line.substring(0, equalsPos).trim();
                    value = line.substring(equalsPos + 1).trim();
                }
                this.put(key, value);
            }
        } finally {
            LineIterator.closeQuietly(iterator);
        }
    }

    public void save(File file) throws IOException {

        List<String> lines = new ArrayList<String>();

        for (Map.Entry<String, String> entry : this.entrySet()) {
            String line = entry.getKey();
            String value = entry.getValue();
            if (value != null && value.length() > 0) {
                line = line + " = " + value;
            }
            lines.add(line);
        }

        FileUtils.writeLines(file, lines);
    }

}
