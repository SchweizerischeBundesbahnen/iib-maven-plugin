package ch.sbb.maven.plugins.iib.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.maven.plugin.logging.Log;

public class ProcessOutputLogger extends Thread {

    private InputStream is;
    private Log log;

    public ProcessOutputLogger(InputStream is, Log log) {
        this.is = is;
        this.log = log;
    }

    @Override
    public void run() {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        String line = null;
        try {
            while (true) {
                line = bufferedReader.readLine();
                if (line != null) {
                    log.info(line);
                    line = null;
                } else {
                    Thread.sleep(500);
                }
            }
        } catch (IOException ioe) {
            // TODO handle this better
            ioe.printStackTrace();
        } catch (InterruptedException ie) {
            // this is to be expected when the child process is finished
        } catch (Exception e) {
            // TODO handle this better
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                // ignore this one
            }
        }
    }
}