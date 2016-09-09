package com.ista.dmi.service;

import com.ista.dmi.enumeration.Modules;
import com.ista.dmi.enumeration.ShellCommands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CopyService {
  
  @Value("${dmiSource}")
  private String dmiSource;

  @Value("${deployFolder}")
  private String deployFolder;

  @Autowired
  private ShellCommandExecutor shellCommandExecutor;

  public int copy(List<String> modules) {
    List<Integer> result = new ArrayList<>();
    modules.forEach(module -> {
      result.add(shellCommandExecutor.executeCommand(String.format(ShellCommands.COPY.getCommand(), dmiSource + Modules.valueOf(module.toUpperCase()).getTargetModulePath(), deployFolder, Modules.valueOf(module.toUpperCase()).name())));
      result.add(shellCommandExecutor.executeCommand(String.format(ShellCommands.COPY.getCommand(), dmiSource + Modules.valueOf(module.toUpperCase()).getTargetDbMigrationPath(), deployFolder, Modules.valueOf(module.toUpperCase()).name() + "-dbmigrations")));
    });
    return result.stream().mapToInt(Integer::intValue).sum();
  }

}
