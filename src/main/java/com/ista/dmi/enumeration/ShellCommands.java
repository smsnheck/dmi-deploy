package com.ista.dmi.enumeration;

public enum ShellCommands {

  MAVEN_BUILD("mvn clean install -U -T 2 -Dmaven.test.skip=true"),
  UPDATE_DATABASE("mvn liquibase:update -PupdateSQL -Dliquibase.contexts=update,review,POST-PROPERTY-INSERT -f %spom.xml"),
  GIT_UPDATE("git --git-dir=%s.git --work-tree=%s pull"),
  COPY("xcopy %s\\*.war %s%s.war /Y");

  private final String command;

  ShellCommands(String command) {
    this.command = command;
  }

  public String getCommand() {
    return command;
  }
  
}

