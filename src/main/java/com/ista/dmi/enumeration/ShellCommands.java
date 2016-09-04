package com.ista.dmi.enumeration;

public enum ShellCommands {

  MAVEN_BUILD("mvn clean install -DskipTests"),
  UPDATE_DATABASE("mvn liquibase:update -PupdateSQL"); //TODO 
  private final String command;

  private ShellCommands(String command) {
    this.command = command;
  }

  public String getCommand() {
    return command;
  }
  
}

