package com.ista.dmi.enumeration;

public enum ShellCommands {

  MAVEN_BUILD("mvn clean install -U -T 2 -Dmaven.test.skip=true"),
  UPDATE_DATABASE("mvn liquibase:update -PupdateSQL -Dliquibase.contexts=update,review,POST-PROPERTY-INSERT");

  private final String command;

  private ShellCommands(String command) {
    this.command = command;
  }

  public String getCommand() {
    return command;
  }
  
}

