package ch.sbb.maven.plugins.iib.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ProcessOutputCatcher extends Thread {

    private InputStream is;
    private ArrayList<String> output;

    public ProcessOutputCatcher(InputStream is, ArrayList<String> output) {
        this.is = is;
        this.output = output;
    }

    @Override
    public void run() {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        String line = null;
        try {
            while (true) {
                line = bufferedReader.readLine();
                if (line != null) {
                    output.add(line);
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
        } catch (Throwable t) {
            // TODO handle this better
            t.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                // ignore this one
            }
        }
    }
}