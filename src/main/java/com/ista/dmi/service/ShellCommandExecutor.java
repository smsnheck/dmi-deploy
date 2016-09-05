package com.ista.dmi.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class ShellCommandExecutor {

  private final Logger LOGGER = LoggerFactory.getLogger(ShellCommandExecutor.class);

  public void executeCommand(String command) {
    StringBuffer output = new StringBuffer();

    Process p;
    try {
      p = Runtime.getRuntime().exec("cmd /c " + command);
      p.waitFor();
      BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

      String line = "";
      while ((line = reader.readLine()) != null) {
        output.append(line + "\n");
      }

    } catch (Exception e) {
      LOGGER.error("Exception caught {}", e);
    }

    LOGGER.info(output.toString());
  }

}
