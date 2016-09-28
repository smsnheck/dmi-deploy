package com.ista.dmi.service;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class SshCommandExecutor {

  private final Logger LOGGER = LoggerFactory.getLogger(SshCommandExecutor.class);

  public int executeCommand(String command) throws IOException {
    int result = 0;
    try (SSHClient sshClient = new SSHClient()) {

      sshClient.addHostKeyVerifier("06:22:ef:ba:d3:6c:ea:46:61:7b:07:bc:e9:e3:8c:8e");
      sshClient.connect("192.168.50.10");
      sshClient.authPassword("vagrant", "vagrant".toCharArray());
      final Session session = sshClient.startSession();
      final Session.Command executionCommand = session.exec(command + " &");


      LOGGER.info(executionCommand.getInputStream().toString());
//      executionCommand.join(5, TimeUnit.SECONDS);
      executionCommand.join();
      result = executionCommand.getExitStatus();
      sshClient.close();
      sshClient.disconnect();
    }

    return result;
  }

}
