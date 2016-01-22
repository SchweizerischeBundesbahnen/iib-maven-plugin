package ch.sbb.maven.plugins.iib.mojos;

import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Goal which reads the default.properties file to figure out if the classloader approach for this bar project is consistent. Either all jar nodes in all flows must use a classloader or none of them
 * should.
 */
@Mojo(name = "validate-top-level-name")
public class ValidateTopLevelNameMojo extends AbstractMojo {

    /**
     * The name of the default properties file to be generated from the bar file.
     */
    @Parameter(property = "iib.configurablePropertiesPath", defaultValue = "${project.build.resources[0].directory}", required = true)
    protected String propertyPath;

    /**
     * The type of the dependency which is added in the pom.xml of the bar project. app and lib are allowed.
     */
    @Parameter(property = "iib.projectType", defaultValue = "app", required = true)
    protected String projectType;

    /**
     * The Maven Project Object
     */
    @Parameter(property = "project", required = true, readonly = true)
    protected MavenProject project;

    public void execute() throws MojoFailureException {
        List<Dependency> dependencyList = project.getDependencies();
        if (dependencyList.size() != 1) {
            throw new RuntimeException("Invalid dependencies in bar project. Only one dependency is allowed.");
        }

        if (!dependencyList.get(0).getArtifactId().endsWith("-" + projectType)) {
            throw new RuntimeException("Invalid dependencies in bar project or wrong iib.projectType is defined. Only " + projectType + " is allowed");
        }
    }


}
