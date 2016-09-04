package com.ista.dmi.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DeployService {
  
  @Value("${dmiSource}")
  private String dmiSource;
  
  @Value("${deployFolder}")
  private String deployFolder;
  
  @Autowired
  private ShellCommandExecutor shellCommandExecutor;
  
  public void build(List<String> modules) {
    modules.stream().forEach(module -> {
      shellCommandExecutor.executeCommand("cd " + dmiSource + module + "-parent\\" + module + "\\target\\");
      shellCommandExecutor.executeCommand("find . -name '*.war' | xargs cp " + deployFolder + module + ".war");
    });
  }

}
