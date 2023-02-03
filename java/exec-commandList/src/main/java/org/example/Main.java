package org.example;

import java.io.File;
import java.io.IOException;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        List<String> command = List.of("/usr/bin/bash", "-c", "pdftk *.pdf burst output page_%03d.pdf");

        log.info("Starting: {}", command);

        Process exec = Runtime.getRuntime().exec(command.toArray(new String[0]), null, new File("."));

        new LogThread(exec.getInputStream(), false).start();
        new LogThread(exec.getErrorStream(), true).start();

        int exitCode = exec.waitFor();
        log.info("Exit: " + exitCode);
    }
}
