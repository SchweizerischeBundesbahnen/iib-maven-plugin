package ch.sbb.maven.plugins.iib.mojos;

import static ch.sbb.maven.plugins.iib.utils.PomXmlUtils.getModel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import com.ibm.broker.config.appdev.CommandProcessorPublicWrapper;

/**
 * Creates a .bar file from a previously prepared worksapce
 */

@Mojo(name = "package-bar", defaultPhase = LifecyclePhase.COMPILE)
public class PackageBarMojo extends AbstractMojo {

    /**
     * The name of the BAR (compressed file format) archive file where the
     * result is stored.
     */
    @Parameter(property = "iib.barName", defaultValue = "${project.build.directory}/iib/${project.artifactId}-${project.version}.bar", required = true)
    protected File barName;

    /**
     * The name of the trace file to use when packaging bar files
     */
    @Parameter(property = "iib.packageBarTraceFile", defaultValue = "${project.build.directory}/packagebartrace.txt", required = true)
    protected File packageBarTraceFile;

    /**
     * Exclude artifacts pattern (or patterns, comma separated). By default, exclude pom.xml's as each project will have one and this causes a packaging error.
     */
    @Parameter(property = "iib.excludeArtifactsPattern", defaultValue = "**/pom.xml")
    protected String excludeArtifactsPattern;

    /**
     * Include artifacts pattern (or patterns, comma separated). By default, the default value used for mqsipackagebar, except .esql & .subflow, which as not compilable
     * 
     * @see <a href="http://www-01.ibm.com/support/knowledgecenter/SSMKHH_9.0.0/com.ibm.etools.mft.doc/bc31720_.htm">IIB9 Documentation</a>
     */
    @Parameter(property = "iib.includeArtifactsPattern", defaultValue =
            "**/*.xsdzip,**/*.tblxmi,**/*.xsd,**/*.wsdl,**/*.dictionary,**/*.xsl,**/*.xslt,**/*.xml,**/*.jar,**/*.inadapter,**/*.outadapter,**/*.insca,**/*.outsca,**/*.descriptor,**/*.php,**/*.idl,**/*.map,**/*.msgflow",
            required = true)
    protected String includeArtifactsPattern;

    /**
     * Projects containing files to include in the BAR file in the workspace. Required for a new workspace.
     */
    @Parameter(property = "iib.projectName", defaultValue = "")
    protected String projectName;

    /**
     * The path of the workspace in which the projects are extracted to be built.
     */
    @Parameter(property = "iib.workspace", defaultValue = "${project.build.directory}/iib/workspace", required = true)
    protected File workspace;
    //
    /**
     * Pattern (or patterns, comma separated) of jars to be excluded from the generated bar file
     */
    @Parameter(property = "iib.discardJarsPattern", defaultValue = "**/javacompute_**.jar,**/jplugin2_**.jar")
    protected String discardJarsPattern;

    /**
     * Whether classloaders are in use with this bar
     */
    @Parameter(property = "iib.useClassloaders", defaultValue = "false", required = true)
    protected Boolean useClassloaders;

    /**
     * The Maven Project Object
     */
    @Parameter(property = "project", required = true, readonly = true)
    protected MavenProject project;


    private List<String> addObjectsAppsLibs() throws MojoFailureException, MojoExecutionException {
        List<String> params = new ArrayList<String>();
        List<String> apps = new ArrayList<String>();
        List<String> libs = new ArrayList<String>();

        // loop through the projects, adding them as "-a" Applications, "-l"
        // libraries or the deployable artefacts as "-o" objects

        // only direct dependencies of the current bar project will be added as Applications or Libraries
        // loop through them
        for (Dependency dependency : project.getDependencies()) {
            // only check for dependencies with scope "compile"
            if (!dependency.getScope().equals("compile")) {
                continue;
            }

            // load pom.xml from workspace to check if this dependency is an app oder a lib project
            File pomfile = new File(project.getBuild().getDirectory() + "/iib/workspace/" + dependency.getArtifactId() + "/pom.xml");
            Model model = getModel(pomfile);
            MavenProject dependencyProject = new MavenProject(model);
            // iib-app or iib-scr (used with diffrent params by the mqsipackagebar) everything else will be ignored
            String packing = dependencyProject.getPackaging();

            // Load and rename the directory name --> pack the dependency with that name in the .bar
            Path projectDirectory = pomfile.getParentFile().toPath();
            try {
                FileUtils.forceDelete(pomfile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (packing.equals("iib-src")) {
                libs.add(model.getArtifactId());
            } else if (packing.equals("iib-app")) {
                apps.add(model.getArtifactId());
            }
        }

        // if there are applications, add them
        if (!apps.isEmpty()) {
            params.add("-k");
            params.addAll(apps);
        }

        // if there are libraries, add them
        if (!libs.isEmpty()) {
            params.add("-y");
            params.addAll(libs);
        }

        // if there are no applications and no libraries, add "unmanaged" objects
        // this should never apply for SBB
        if (apps.isEmpty() && libs.isEmpty()) {
            params.add("-o");
            params.addAll(getObjectNames());
        }

        return params;
    }

    protected List<String> constructParams() throws MojoFailureException, MojoExecutionException {
        List<String> params = new ArrayList<String>();

        // bar file name - required
        params.add("-a");
        params.add(barName.getAbsolutePath());

        // workspace parameter - required
        createWorkspaceDirectory();
        params.add("-w");
        params.add(workspace.toString());

        // object names - required
        params.addAll(addObjectsAppsLibs());

        // always trace the packaging process
        params.add("-v");
        params.add(packageBarTraceFile.getAbsolutePath());

        return params;
    }

    /**
     * @throws MojoFailureException
     */
    protected void createWorkspaceDirectory() throws MojoFailureException {
        if (!workspace.exists()) {
            workspace.mkdirs();
        }
        if (!workspace.isDirectory()) {
            throw new MojoFailureException(
                    "Workspace parameter is not a directory: "
                            + workspace.toString());
        }
    }

    public void execute() throws MojoFailureException, MojoExecutionException {

        getLog().info("Creating bar file: " + barName);

        File barDir = barName.getParentFile();
        if (!barDir.exists()) {
            barDir.getParentFile().mkdirs();
        }

        List<String> params = constructParams();

        executeMqsiPackageBar(params);
    }

    private void executeMqsiPackageBar(List<String> params) {
        getLog().info("Packaging Bar File with the parameters: " + params);
        String[] paramsArray = params.toArray(new String[0]);
        new CommandProcessorPublicWrapper(paramsArray).process();
    }

    /**
     * @return a list of objects to be (explicitly) added to the bar file
     * @throws MojoFailureException
     */
    @SuppressWarnings("unchecked")
    private Collection<? extends String> getObjectNames()
            throws MojoFailureException {
        List<String> objectNames = new ArrayList<String>();

        // get the names of files under: the workspace directory, matching includeFlowPatterns, not matching anything in a directory called "tempfiles", excluding the base directory
        try {
            // since excludes is a regex and "\" is special for regexes, it must be escaped. Not really sure if tempfiles pops up everywhere or not
            String excludes = "tempfiles"
                    + (File.separator == "\\" ? "\\\\" : File.pathSeparator)
                    + "\\.*";
            if (excludeArtifactsPattern != null
                    && excludeArtifactsPattern.length() > 1) {
                excludes = excludes + "," + excludeArtifactsPattern;
            }
            objectNames = FileUtils.getFileNames(workspace,
                    includeArtifactsPattern, excludes, false);

        } catch (IOException e) {
            throw new MojoFailureException(
                    "Could not resolve includeArtifactsPattern: "
                            + includeArtifactsPattern, e);
        }

        return objectNames;
    }

}
