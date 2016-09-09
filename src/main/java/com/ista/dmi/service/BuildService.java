package com.ista.dmi.service;

import java.util.ArrayList;
import java.util.List;

import com.ista.dmi.enumeration.Modules;
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

  public int build(List<String> modules) {
    List<Integer> result = new ArrayList<>();
    modules.forEach(module -> {
      result.add(shellCommandExecutor.executeCommand(ShellCommands.MAVEN_BUILD.getCommand() + " -f " + dmiSource + Modules.valueOf(module.toUpperCase()).getModulePath()));
    });
    return result.stream().mapToInt(Integer::intValue).sum();
  }

}
