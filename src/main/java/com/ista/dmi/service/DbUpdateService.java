package com.ista.dmi.service;

import com.ista.dmi.enumeration.ShellCommands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DbUpdateService {
  @Value("${dmiSource}")
  private String dmiSource;

  @Autowired
  private ShellCommandExecutor shellCommandExecutor;

  public int update() {
    return shellCommandExecutor.executeCommand(String.format(ShellCommands.UPDATE_DATABASE.getCommand(), dmiSource));
  }
}
