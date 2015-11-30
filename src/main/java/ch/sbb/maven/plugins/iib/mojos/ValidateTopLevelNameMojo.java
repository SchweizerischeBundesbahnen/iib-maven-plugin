package ch.sbb.maven.plugins.iib.mojos;

import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import ch.sbb.maven.plugins.iib.utils.ProjectType;

/**
 * Goal which reads the dependencies from the bar projec to figure out if the naming convention for this bar project is observed.
 * Only one dependeny with the suffix -app or -lib is allowed (depends on the iib.projectType property)
 */
@Mojo(name = "validate-top-level-name")
public class ValidateTopLevelNameMojo extends AbstractMojo {

    /**
     * The type of the dependency which is added in the pom.xml of the bar project. app and lib are allowed.
     */
    @Parameter(property = "iib.projectType", defaultValue = "APPLICATION", required = true)
    protected ProjectType projectType;

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

        if (!dependencyList.get(0).getArtifactId().endsWith("-" + projectType.getType())) {
            throw new RuntimeException("Invalid dependencies in bar project or wrong iib.projectType is defined. Only " + projectType + " is allowed");
        }
    }


}
