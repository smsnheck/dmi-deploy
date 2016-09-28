package com.ista.dmi.service;

import com.ista.dmi.enumeration.ShellCommands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class WildflyService {

  @Autowired
  private SshCommandExecutor sshCommandExecutor;

  public int execute(ShellCommands shellCommand) throws IOException {
    return sshCommandExecutor.executeCommand(shellCommand.getCommand());
  }
}
