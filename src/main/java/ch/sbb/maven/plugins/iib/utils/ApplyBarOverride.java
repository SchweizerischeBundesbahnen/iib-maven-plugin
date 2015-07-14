package ch.sbb.maven.plugins.iib.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;

import com.ibm.broker.config.proxy.BarFile;
import com.ibm.broker.config.proxy.CompletionCodeType;
import com.ibm.broker.config.proxy.LogEntry;

/**
 * Highly borrowed from
 * 
 * @see com.ibm.broker.config.util.ApplyBarOverride
 * 
 * @author u209936 (Jamie Townsend)
 * @since 2.1, 2015
 */
@SuppressWarnings("javadoc")
public class ApplyBarOverride
{

    public static Enumeration<LogEntry> applyBarOverride(String srcBarFile, String propertiesFile, String targetBarFile) throws IOException {
        CompletionCodeType completionCode = CompletionCodeType.success;
        BarFile barfile = null;
        Enumeration<LogEntry> logMessages = null;

        barfile = BarFile.loadBarFile(srcBarFile);

        if (propertiesFile != null)
        {
            logMessages = applyOverridesFromFile(barfile, propertiesFile);
            logMessages = null;
        }
        completionCode = getCompletionCode(logMessages);
        // enumeration = null;
        // }
        if (completionCode == CompletionCodeType.success)
        {
            barfile.saveAs(targetBarFile);
        }
        return logMessages;
    }


    private static Enumeration<LogEntry> applyOverridesFromFile(BarFile barfile, String propertiesFile)
            throws IOException {

        String appName = null;
        String libName = null;
        boolean recurse = true;

        Enumeration<LogEntry> enumeration = null;

        ConfigurableProperties configurableProperties = readPropertiesFile(propertiesFile);
        if (configurableProperties != null) {
            enumeration = barfile.applyOverrides(new HashMap<String, String>(configurableProperties), appName, libName, recurse);
        }
        return enumeration;
    }

    private static CompletionCodeType getCompletionCode(Enumeration<LogEntry> enumeration) {

        CompletionCodeType completionCode = CompletionCodeType.success;
        if (enumeration != null)
        {
            LogEntry logentry;
            while (enumeration.hasMoreElements()) {
                logentry = enumeration.nextElement();
                if (logentry.getMessageNumber() == 1145) {
                    return CompletionCodeType.failure;
                }
            }

        }
        return completionCode;
    }

    // TODO Refactor into separate class and re-use for .properties validation
    private static ConfigurableProperties readPropertiesFile(String propertiesFilename)
            throws IOException // , ConfigUtilityException
    {
        String commentString = System.getProperty("COMMENT", "#");
        String delimiterString = System.getProperty("KEY_VALUE_DELIMITER", "=");
        ConfigurableProperties linkedhashmap = new ConfigurableProperties();
        File file = new File(propertiesFilename);
        FileReader filereader = new FileReader(file);
        BufferedReader bufferedreader = new BufferedReader(filereader);
        for (String fileLine = bufferedreader.readLine(); fileLine != null; fileLine = bufferedreader.readLine())
        {
            int commentPosition = fileLine.indexOf(commentString);
            if (commentPosition == 0) {
                continue;
            }
            int delimiterPosition = fileLine.indexOf(delimiterString);
            String propertyName = null;
            String propertyValue = null;
            if (delimiterPosition != -1)
            {
                propertyName = fileLine.substring(0, delimiterPosition).trim();
                propertyValue = fileLine.substring(delimiterPosition + delimiterString.length()).trim();
            } else
            {
                propertyName = fileLine;
            }
            linkedhashmap.put(propertyName, propertyValue);
        }

        filereader.close();
        return linkedhashmap;
    }

}
