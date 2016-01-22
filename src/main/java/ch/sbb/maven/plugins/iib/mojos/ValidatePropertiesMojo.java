package ch.sbb.maven.plugins.iib.mojos;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Goal which reads the default.properties file to figure out if the classloader approach for this bar project is consistent. Either all jar nodes in all flows must use a classloader or none of them
 * should.
 */
@Mojo(name = "validate-property-name")
public class ValidatePropertiesMojo extends AbstractMojo {

    /**
     * The name of the default properties file to be generated from the bar file.
     * The default folder is src/main/resources
     */
    @Parameter(property = "iib.configurablePropertiesPath", defaultValue = "${project.build.resources[0].directory}", required = true)
    protected String propertyPath;

    /**
     * The type of the dependency which is addes in the pom.xml of the bar project.
     */
    @Parameter(property = "iib.projectType", defaultValue = "app", required = true)
    protected String projectType;

    /**
     * The Maven Project Object
     */
    @Parameter(property = "project", required = true, readonly = true)
    protected MavenProject project;

    public void execute() throws MojoFailureException {
        File testProperty = new File(propertyPath + "/test." + project.getParent().getArtifactId() + "-" + projectType + ".properties");
        File inteProperty = new File(propertyPath + "/inte." + project.getParent().getArtifactId() + "-" + projectType + ".properties");
        File prodProperty = new File(propertyPath + "/prod." + project.getParent().getArtifactId() + "-" + projectType + ".properties");

        if (!testProperty.isFile() || !inteProperty.isFile() || !prodProperty.isFile()) {
            throw new MojoFailureException("Validation error: The Property files have a wrong name.");
        }

    }


}
