package ch.sbb.maven.plugins.iib.mojos;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import ch.sbb.maven.plugins.iib.utils.ProjectType;

/**
 * Goal which reads the propertiey files to figure out if the naming convention for this bar project is observed.
 * It must have the following 3 files:
 * test.parentArtefactId-app or lib
 * inte.parentArtefactId-app or lib
 * prod.parentArtefactId-app or lib
 */
@Mojo(name = "validate-property-name")
public class ValidatePropertiesMojo extends AbstractMojo {

    /**
     * The path of the properties files to be used from the overrideProperties command.
     * The default folder is src/main/resources
     */
    @Parameter(property = "iib.configurablePropertiesPath", defaultValue = "${project.build.resources[0].directory}", required = true)
    protected String propertyPath;

    /**
     * The type of the dependency which is addes in the pom.xml of the bar project.
     */
    @Parameter(property = "iib.projectType", defaultValue = "APPLICATION", required = true)
    protected ProjectType projectType;

    /**
     * The Maven Project Object
     */
    @Parameter(property = "project", required = true, readonly = true)
    protected MavenProject project;

    public void execute() throws MojoFailureException {
        File testProperty = new File(propertyPath + "/test." + project.getParent().getArtifactId() + "-" + projectType.getType() + ".properties");
        File inteProperty = new File(propertyPath + "/inte." + project.getParent().getArtifactId() + "-" + projectType.getType() + ".properties");
        File prodProperty = new File(propertyPath + "/prod." + project.getParent().getArtifactId() + "-" + projectType.getType() + ".properties");

        if (!testProperty.isFile() || !inteProperty.isFile() || !prodProperty.isFile()) {
            throw new MojoFailureException("Validation error: The Property files have a wrong name.");
        }

    }


}
