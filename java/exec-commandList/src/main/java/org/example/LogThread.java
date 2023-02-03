package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogThread extends Thread {

    private BufferedReader input;

    private boolean errorLog;

    public LogThread(InputStream input, boolean errorLog) {
        this.input = new BufferedReader(new InputStreamReader(input));
        this.errorLog = errorLog;
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = input.readLine()) != null) {
                if (this.errorLog) {
                    log.error("Process error output: {}", line);
                } else {
                    log.info("Process standard output: {}", line);
                }
            }
        } catch (IOException exc) {
            log.error("Error while reading command output", exc);
        }
    }

}
