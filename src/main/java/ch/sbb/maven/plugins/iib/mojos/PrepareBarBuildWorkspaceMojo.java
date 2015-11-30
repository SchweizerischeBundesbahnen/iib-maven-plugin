package ch.sbb.maven.plugins.iib.mojos;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

import ch.sbb.maven.plugins.iib.generated.maven_pom.Dependency;
import ch.sbb.maven.plugins.iib.generated.maven_pom.Model;
import ch.sbb.maven.plugins.iib.utils.DependencyPredicate;
import ch.sbb.maven.plugins.iib.utils.PomXmlUtils;

/**
 * Unpacks the dependent WebSphere Message Broker Projects.
 * 
 * Implemented with help from: https://github.com/TimMoore/mojo-executor/blob/master/README.md
 * 
 * requiresDependencyResolution below is required for the unpack-dependencies goal to work correctly. See https://github.com/TimMoore/mojo-executor/issues/3
 */
@Mojo(name = "prepare-bar-build-workspace", requiresDependencyResolution = ResolutionScope.TEST)
public class PrepareBarBuildWorkspaceMojo extends AbstractMojo {

    /**
     * a comma separated list of dependency types to be unpacked
     */
    private static final String UNPACK_IIB_DEPENDENCY_TYPES = "zip";

    private static final String UNPACK_IIB_DEPENDENCY_SCOPE = "compile";

    /**
     * Whether classloaders are in use with this bar
     */
    @Parameter(property = "iib.useClassloaders", defaultValue = "false", required = true)
    protected Boolean useClassloaders;

    /**
     * The path of the workspace in which the projects are extracted to be built.
     */
    @Parameter(property = "iib.workspace", defaultValue = "${project.build.directory}/iib/workspace", required = true)
    protected File workspace;

    /**
     * List of remote repositories to be used by the plugin to resolve dependencies.
     */
    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true)
    protected List<RemoteRepository> remoteRepos;

    /**
     * The Maven Project Object
     */
    @Parameter(property = "project", required = true, readonly = true)
    protected MavenProject project;

    /**
     * The Maven Session Object
     */
    @Parameter(property = "session", required = true, readonly = true)
    protected MavenSession session;

    /**
     * The Maven PluginManager Object
     */
    @Component
    protected BuildPluginManager buildPluginManager;

    /**
     * The entry point to Aether, i.e. the component doing all the work.
     */
    @Component
    protected RepositorySystem repoSystem;

    /**
     * The current repository/network configuration of Maven.
     */
    @Parameter(property = "repositorySystemSession")
    protected RepositorySystemSession repoSession;

    public void execute() throws MojoExecutionException, MojoFailureException {

        // unpack the iib-src dependencies
        unpackIibDependencies();

        getLog().info("Copying jar dependencies into iib-src directories...");
        for (String dependencyDirectory : getDependencyDirectories()) {

            File pomfile = new File(dependencyDirectory + "/pom.xml");
            Model model = getModel(pomfile);

            // if classloaders are not in use, copy any transient dependencies of type jar into the dependency directory
            if (!useClassloaders) {
                // copyJarDependencies also deletes the "pom.xml"
                copyJarDependencies(dependencyDirectory, "pom.xml");
            }

            Collection<Dependency> filtered = CollectionUtils.select(project.getDependencies(), new DependencyPredicate("artifactId", model.getArtifactId()));

            if (filtered.size() == 0) {
                deleteFile(pomfile);
            }
        }
    }

    /**
     * @param dependencyDirectory
     * @param pomFilename
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    private void copyJarDependencies(String dependencyDirectory, String pomFilename) throws MojoExecutionException, MojoFailureException {

        File pomFile = new File(dependencyDirectory, pomFilename);

        // optimise performance by quickly checking if there's a dependency of type jar before kicking off the maven copy-dependencies (sub-)build

        for (Dependency dependency : getRuntimeJarDependencies(pomFile)) {

            // default value
            if (dependency.getType() == null || dependency.getType().isEmpty()) {
                dependency.setType("jar");
            }
            ArtifactResult jarArtifactResult = resolveArtifact(dependency.getGroupId(), dependency.getArtifactId(), dependency.getType(), dependency.getVersion());
            ArtifactResult pomArtifactResult = resolveArtifact(dependency.getGroupId(), dependency.getArtifactId(), "pom", dependency.getVersion());

            String tmpPomFilename = "." + pomArtifactResult.getArtifact().getArtifactId() + "-" + pomArtifactResult.getArtifact().getFile().getName();

            try {
                FileUtils.copyFile(jarArtifactResult.getArtifact().getFile(), new File(dependencyDirectory, jarArtifactResult.getArtifact().getFile().getName()));
                FileUtils.copyFile(pomArtifactResult.getArtifact().getFile(), new File(dependencyDirectory, tmpPomFilename));
            } catch (IOException e) {
                throw new MojoExecutionException("Error copying dependency from " + jarArtifactResult.getArtifact().getFile().getAbsolutePath() + " to " + dependencyDirectory, e);
            }

            // TODO this could potentially be optimised to copy-dependencies in one shot instead of each transient jar separately
            copyJarDependencies(dependencyDirectory, tmpPomFilename);
        }
    }

    /**
     * deletes a file than should be closed, but may not yet have been garbage collected
     * 
     * @param fileToDelete the file that should be deleted
     */
    private void deleteFile(File fileToDelete) {
        fileToDelete.delete();
        if (fileToDelete.exists()) {
            // couldn't delete it yet. Collect garbage, sleep a while & try again
            System.gc();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // fail silently
            }
            fileToDelete.delete();
            if (fileToDelete.exists()) {
                getLog().warn("Cannot delete file: " + fileToDelete);
            }
        }
    }

    private void flattenPom(File pomFile) {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setJavaHome(new File(System.getProperty("java.home")));
        request.setPomFile(pomFile);

        List<String> goals = new ArrayList<String>();
        goals.add("org.codehaus.mojo:flatten-maven-plugin:1.0.0-beta-5:flatten");

        goals.add("-f");
        goals.add(pomFile.getAbsolutePath());

        // if maven debugging is not enabled, run the flattening in quiet mode

        if (!getLog().isDebugEnabled()) {
            goals.add("--quiet");
        }

        request.setGoals(goals);

        Invoker invoker = new DefaultInvoker();

        try {
            invoker.execute(request);
            invoker = null;
        } catch (MavenInvocationException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> getDependencyDirectories() throws MojoExecutionException {
        List<String> dependencyDirectories;
        try {
            dependencyDirectories = FileUtils.getDirectoryNames(workspace, "*", ".*", true);
        } catch (IOException e1) {
            // TODO handle exception
            throw new MojoExecutionException("Error searching for dependent project directories under: " + workspace.getAbsolutePath());
        }
        return dependencyDirectories;
    }

    private Model getModel(File pomFile) throws MojoExecutionException {
        // first parse the original pom.xml

        Model model;
        try {
            model = PomXmlUtils.unmarshallPomFile(pomFile);
        } catch (Exception ex) {
            throw new MojoExecutionException("Unable to read pom File: " + pomFile.getAbsolutePath());
        }

        return model;
    }

    /**
     * returns the directly referenced jar dependencies required at runtime
     * 
     * @param pomFile
     * @return
     * @throws MojoExecutionException
     */
    private List<Dependency> getRuntimeJarDependencies(File pomFile) throws MojoExecutionException {

        List<Dependency> runtimeJars = new ArrayList<Dependency>();

        // do a quick analysis of the pom file to see if it has runtime jar dependencies
        boolean mustResolve = false;
        Model model = getModel(pomFile);
        if (model.getDependencies() != null) {
            for (Dependency dependency : model.getDependencies().getDependency()) {
                if (isRuntimeJarDependency(dependency)) {
                    mustResolve = true;
                }
            }
        }

        // runtime jar dependencies
        if (mustResolve) {

            // flatten the pom (expanding variables) into .flattened-pom.xml
            flattenPom(pomFile);

            // now get the model from the flattened pomFile
            File flattenedPomFile = new File(pomFile.getParentFile(), ".flattened-pom.xml");
            Model flattenedModel = getModel(flattenedPomFile);

            List<Dependency> dependencies = flattenedModel.getDependencies().getDependency();
            for (Dependency dependency : dependencies) {

                if (isRuntimeJarDependency(dependency)) {
                    runtimeJars.add(dependency);
                }
            }

            deleteFile(flattenedPomFile);
        }
        return runtimeJars;
    }

    /**
     * @param dependency
     * @return
     */
    private boolean isRuntimeJarDependency(Dependency dependency) {
        // if it's a jar dependency
        if (dependency.getType() == null || "jar".equals(dependency.getType())) {

            // if it's a compile or runtime dependency
            if (dependency.getScope() == null || "compile".equals(dependency.getScope()) || "runtime".equals(dependency.getScope())) {
                return true;
            }
        }
        return false;

    }

    private ArtifactResult resolveArtifact(String groupId, String artifactId, String extension, String version) throws MojoFailureException {
        Artifact artifact;
        try {
            artifact = new DefaultArtifact(groupId, artifactId, extension, version);
        } catch (IllegalArgumentException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }

        ArtifactRequest artifactRequest = new ArtifactRequest();
        artifactRequest.setArtifact(artifact);
        artifactRequest.setRepositories(remoteRepos);

        getLog().debug("Resolving artifact " + artifact + " from " + remoteRepos);

        ArtifactResult result;
        try {
            result = repoSystem.resolveArtifact(repoSession, artifactRequest);
        } catch (ArtifactResolutionException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }

        getLog().debug("Resolved artifact " + artifact + " to " + result.getArtifact().getFile() + " from "
                + result.getRepository());

        return result;
    }

    /**
     * unpacks dependencies of a given scope to the specified directory
     * 
     * @throws MojoExecutionException
     */
    private void unpackIibDependencies() throws MojoExecutionException {

        // define the directory to be unpacked into and create it
        workspace.mkdirs();

        // unpack all IIB dependencies that match the given scope (compile)
        executeMojo(plugin(groupId("org.apache.maven.plugins"), artifactId("maven-dependency-plugin"), version("2.8")), goal("unpack-dependencies"), configuration(element(name("outputDirectory"),
                workspace.getAbsolutePath()), element(name("includeTypes"), UNPACK_IIB_DEPENDENCY_TYPES), element(name("includeScope"), UNPACK_IIB_DEPENDENCY_SCOPE)),
                executionEnvironment(project, session, buildPluginManager));

        // delete the dependency-maven-plugin-markers directory
        try {
            FileUtils.deleteDirectory(new File(project.getBuild().getDirectory(), "dependency-maven-plugin-markers"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
