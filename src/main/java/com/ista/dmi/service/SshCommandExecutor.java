package com.ista.dmi.service;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SshCommandExecutor {

  private final Logger LOGGER = LoggerFactory.getLogger(SshCommandExecutor.class);

  @Value("${publicKeyFingerprint}")
  private String publicKeyFingerprint;

  public int executeCommand(String command) throws IOException {
    int result;
    try (SSHClient sshClient = new SSHClient()) {

      sshClient.addHostKeyVerifier(publicKeyFingerprint);
      sshClient.connect("192.168.50.10");
      sshClient.authPassword("vagrant", "vagrant".toCharArray());
      final Session session = sshClient.startSession();
      final Session.Command executionCommand = session.exec(command + " &");

      LOGGER.info(executionCommand.getInputStream().toString());
      executionCommand.join();
      result = executionCommand.getExitStatus();
      sshClient.close();
      sshClient.disconnect();
    }

    return result;
  }

}
