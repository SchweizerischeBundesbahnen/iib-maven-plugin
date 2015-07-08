package ch.sbb.maven.plugins.iib.utils;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

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

    public static Properties getOverridableProperties(BarFile barfile) throws IOException {
        Properties configurableProperties = new Properties();
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
                // String as[] = barentry.getKeywords();
                // String s3 = (new SimpleDateFormat()).format(barentry.getModifyTime());
                // System.out.println((new StringBuilder()).append(s).append(barentry.getFullName()).append(" (").append(s3).append("):").toString());
                // String as1[] = as;
                // int j = as1.length;
                // for (int k = 0; k < j; k++)
                // {
                // String s5 = as1[k];
                // // String s6 = barentry.getKeywordValue(s5);
                // // System.out.println((new StringBuilder()).append(s).append("  ").append(s5).append(" = ").append(s6).toString());
                // }

                // if (_isRecursive && (barentry.isApplication() || barentry.isLibrary())) {
                if (barentry.isApplication() || barentry.isLibrary()) {
                    // try
                    // {
                    // i = runCommand(BarFile.loadBarFile(barentry.getBytes(), barentry.getFullName()), (new StringBuilder()).append(s).append(s).toString());
                    configurableProperties.putAll(getOverridableProperties(BarFile.loadBarFile(barentry.getBytes(), barentry.getFullName())));
                    // } catch (IOException ioexception)
                    // {
                    // DisplayMessage.write(1050, ioexception.toString());
                    // i = CompletionCodeType.failure.intValue();
                    // }
                }
            } while (true);
            DeploymentDescriptor deploymentdescriptor = barfile.getDeploymentDescriptor();
            if (deploymentdescriptor != null)
            {
                Enumeration<String> enumeration1 = deploymentdescriptor.getPropertyIdentifiers();
                if (enumeration1.hasMoreElements())
                {
                    // System.out.println((new StringBuilder()).append(s).append("Deployment descriptor:").toString());
                    while (enumeration1.hasMoreElements())
                    {
                        String s2 = enumeration1.nextElement();
                        String override = (deploymentdescriptor.getOverride(s2) != null) ? deploymentdescriptor.getOverride(s2) : "";
                        configurableProperties.put(s2, override);
                        // String s4 = deploymentdescriptor.getOverride(s2);
                        // if (s4 == null) {
                        // System.out.println((new StringBuilder()).append(s).append("  ").append(s2).toString());
                        // } else {
                        // System.out.println((new StringBuilder()).append(s).append("  ").append(s2).append(" = ").append(s4).toString());
                        // }
                    }
                }
            }
        }
        return configurableProperties;
    }

    public static Properties getOverridableProperties(String barFileName) throws IOException {

        BarFile barfile = BarFile.loadBarFile(barFileName);
        return ReadBar.getOverridableProperties(barfile);

    }

}
