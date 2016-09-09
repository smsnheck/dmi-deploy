package com.ista.dmi.controller;

import java.util.List;

public class DeploymentModel {

  private List<String> deployment;

  private boolean gitUpdate;

  private boolean dbUpdate;

  private List<String> copyModules;

  public List<String> getDeployment() {
    return deployment;
  }

  public void setDeployment(List<String> deployment) {
    this.deployment = deployment;
  }

  public boolean isGitUpdate() {
    return gitUpdate;
  }

  public void setGitUpdate(boolean gitUpdate) {
    this.gitUpdate = gitUpdate;
  }

  public boolean isDbUpdate() {
    return dbUpdate;
  }

  public void setDbUpdate(boolean dbUpdate) {
    this.dbUpdate = dbUpdate;
  }

  public List<String> getCopyModules() {
    return copyModules;
  }

  public void setCopyModules(List<String> copyModules) {
    this.copyModules = copyModules;
  }
}
