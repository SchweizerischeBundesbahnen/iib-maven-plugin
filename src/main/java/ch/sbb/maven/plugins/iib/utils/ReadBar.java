package ch.sbb.maven.plugins.iib.utils;

import java.io.IOException;
import java.util.Enumeration;

import com.ibm.broker.config.proxy.BarEntry;
import com.ibm.broker.config.proxy.BarFile;
import com.ibm.broker.config.proxy.DeploymentDescriptor;

/**
 * Highly borrowed from
 * 
 * @see com.ibm.broker.config.util.ReadBar
 * 
 * @author u209936 (Jamie Townsend)
 * @since 2.1, 2015
 */
@SuppressWarnings("javadoc")
public class ReadBar {

    public static ConfigurableProperties getOverridableProperties(BarFile barfile) throws IOException {
        ConfigurableProperties configurableProperties = new ConfigurableProperties();
        // int i = 0;
        if (barfile != null)
        {
            Enumeration<String> enumeration = barfile.getBarEntryNames();
            do
            {
                if (!enumeration.hasMoreElements()) {
                    break;
                }
                String s1 = enumeration.nextElement();
                BarEntry barentry = barfile.getBarEntryByName(s1);

                if (barentry.isApplication() || barentry.isLibrary()) {
                    configurableProperties.putAll(getOverridableProperties(BarFile.loadBarFile(barentry.getBytes(), barentry.getFullName())));
                }
            } while (true);
            DeploymentDescriptor deploymentdescriptor = barfile.getDeploymentDescriptor();
            if (deploymentdescriptor != null)
            {
                Enumeration<String> enumeration1 = deploymentdescriptor.getPropertyIdentifiers();
                if (enumeration1.hasMoreElements())
                {
                    while (enumeration1.hasMoreElements())
                    {
                        String overrideKey = enumeration1.nextElement();
                        String overrideValue = (deploymentdescriptor.getOverride(overrideKey) != null) ? deploymentdescriptor.getOverride(overrideKey) : "";
                        configurableProperties.put(overrideKey, overrideValue);
                    }
                }
            }
        }
        return configurableProperties;
    }

    public static ConfigurableProperties getOverridableProperties(String barFileName) throws IOException {

        BarFile barfile = BarFile.loadBarFile(barFileName);
        return ReadBar.getOverridableProperties(barfile);

    }

}
