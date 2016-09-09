package com.ista.dmi.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class ShellCommandExecutor {

  private final Logger LOGGER = LoggerFactory.getLogger(ShellCommandExecutor.class);

  public int executeCommand(String command) {

    Process p = null;
    try {
      ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", command);
      pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
      pb.redirectError(ProcessBuilder.Redirect.INHERIT);
      p = pb.start();

      p.waitFor();
    } catch (Exception e) {
      LOGGER.error("Exception caught", e);
    }
    return p.exitValue();
  }

}
