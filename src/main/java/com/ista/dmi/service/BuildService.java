package com.ista.dmi.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ista.dmi.enumeration.ShellCommands;

@Service
public class BuildService {
  
  @Value("${dmiSource}")
  private String dmiSource;
  
  @Autowired
  private ShellCommandExecutor shellCommandExecutor;
  
  public void build(List<String> modules) {
    modules.stream().forEach(module -> {
      shellCommandExecutor.executeCommand("cd " + dmiSource + module + "\\-parent");
      shellCommandExecutor.executeCommand(ShellCommands.MAVEN_BUILD.getCommand());
    });
  }

}
