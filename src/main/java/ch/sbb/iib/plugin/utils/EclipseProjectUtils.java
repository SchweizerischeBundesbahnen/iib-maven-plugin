package ch.sbb.iib.plugin.utils;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import ch.sbb.maven.plugins.iib_maven_plugin.eclipse_project.ProjectDescription;

public class EclipseProjectUtils {

    private static ProjectDescription getProjectDescription(File workspace, String projectName) throws MojoFailureException {
        ProjectDescription projectDescription = new ProjectDescription();
        try {
            // unmarshall the .project file, which is in the temp workspace
            // under a directory of the same name as the projectName
            projectDescription = unmarshallEclipseProjectFile(new File(
                    new File(workspace, projectName), ".project"));
        } catch (JAXBException e) {
            throw (new MojoFailureException(
                    "Error parsing .project file for: " + projectName, e));
        }
        return projectDescription;
    }

    /**
     * @param workspace 
     * @return the names of the projects (actually, just all directories) in the
     *         workspace
     * @throws MojoFailureException
     */
    public static List<String> getWorkspaceProjects(File workspace) throws MojoFailureException {

        List<String> workspaceProjects = new ArrayList<String>();

        for (File file : workspace.listFiles()) {
            if (file.isDirectory() && !file.getName().equals(".metadata")) {
                workspaceProjects.add(file.getName());
            }
        }

        if (workspaceProjects.isEmpty()) {
            throw (new MojoFailureException(
                    "No projects were found in the workspace: "
                            + workspace.getAbsolutePath()));
        }

        return workspaceProjects;
    }


    public static boolean isApplication(File workspace, String projectName, Log log) throws MojoFailureException {

        List<String> natureList = getProjectDescription(workspace, projectName).getNatures().getNature();
        if (natureList
                .contains("com.ibm.etools.msgbroker.tooling.applicationNature")) {
            log.debug(
                    projectName + " is an IIB Application");
            return true;
        } else {
            return false;
        }
    }

    public static boolean isLibrary(File workspace, String projectName, Log log) throws MojoFailureException {

        List<String> natureList = getProjectDescription(workspace, projectName).getNatures().getNature();
        if (natureList
                .contains("com.ibm.etools.msgbroker.tooling.libraryNature")) {
            log.debug(projectName + " is an IIB Library");
            return true;
        } else {
            return false;
        }
    }

    protected static ProjectDescription unmarshallEclipseProjectFile(File projectFile)
            throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(ProjectDescription.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (ProjectDescription) unmarshaller.unmarshal(projectFile);

    }


}
