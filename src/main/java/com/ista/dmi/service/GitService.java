package com.ista.dmi.service;

import com.ista.dmi.enumeration.Modules;
import com.ista.dmi.enumeration.ShellCommands;
import jdk.nashorn.tools.Shell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GitService {
  @Value("${dmiSource}")
  private String dmiSource;

  @Autowired
  private ShellCommandExecutor shellCommandExecutor;

  public int update() {
    return shellCommandExecutor.executeCommand(String.format(ShellCommands.GIT_UPDATE.getCommand(), dmiSource, dmiSource));
  }
}
