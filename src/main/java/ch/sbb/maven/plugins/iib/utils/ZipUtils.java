package ch.sbb.maven.plugins.iib.utils;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.ZipFileSet;

public final class ZipUtils {

    /**
     * hide the default constructor
     */
    private ZipUtils() {
        super();
    }

    /**
     * Removes files from a given zip file.
     * 
     * @param zipFile name of the zip file to be modified
     * @param removePattern pattern of the files to be removed
     * 
     * @throws IOException
     */
    public static void removeFiles(File zipFile, String removePattern) throws IOException {
        String zipFileName = zipFile.getName();
        File tmpFile = new File(zipFile.getCanonicalPath() + ".tmp");
        Project antProject = new Project();
        Target antTarget = new Target();
        antProject.addTarget("zip", antTarget);
        Zip zipTask = new Zip();
        zipTask.setProject(antProject);
        zipTask.setDestFile(tmpFile);
        ZipFileSet set = new ZipFileSet();
        set.setSrc(zipFile);
        set.setExcludes(removePattern);
        zipTask.addZipfileset(set);
        antTarget.addTask(zipTask);
        antTarget.execute();
        zipFile.delete();
        tmpFile.renameTo(new File(tmpFile.getParentFile(), zipFileName));
    }

}
