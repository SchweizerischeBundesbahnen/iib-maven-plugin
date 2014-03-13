package ch.sbb.wmb7.plugin.utils;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoFailureException;
import org.junit.Ignore;
import org.junit.Test;

public class ZipUtilsTest {

    @Ignore
    @Test
    public void removeFilesTest() throws IOException, MojoFailureException {
        ZipUtils.removeFiles(new File("/Users/u209936/Workspaces/DMS/maven-wmb7-plugin/src/test/resources/ch/sbb/wmb7/plugin/utils/ziputilstest/DMS_DAZU_BAR-0.0.1-SNAPSHOT.bar"), "*.jar");
    }

}
