package ch.sbb.maven.plugins.iib.utils;

import java.io.File;
import java.io.FileInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Unmarshaller;

import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.MojoExecutionException;

import ch.sbb.maven.plugins.iib.generated.maven_pom.Model;

/**
 * @author u209936
 * 
 */
public class PomXmlUtils {

    /**
     * @param pomFile
     * @return
     * @throws JAXBException
     */
    public static Model unmarshallPomFile(File pomFile)
            throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Model.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (Model) JAXBIntrospector.getValue(unmarshaller.unmarshal(pomFile));

    }

    public static org.apache.maven.model.Model getModel(File pomFile) throws MojoExecutionException {
        // first parse the original pom.xml
        MavenXpp3Reader pomReader = new MavenXpp3Reader();
        org.apache.maven.model.Model dependentModel;
        try {
            dependentModel = pomReader.read(new FileInputStream(pomFile));
        } catch (Throwable t) {
            // TODO handle exception
            throw new MojoExecutionException("An error occurred trying to parse: " + pomFile, t);
        }
        return dependentModel;
    }
}
