package com.ista.dmi.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.ista.dmi.enumeration.ShellCommands;
import com.ista.dmi.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DeploymentController {
  private List<String> modules = Arrays.asList("common", "cds", "cus", "pds", "ibs", "pcs", "mdr");
  private List<String> copyModules = Arrays.asList("cds", "cus", "pds", "ibs", "pcs", "mdr");
  private List<String> dbModules = Arrays.asList("cds-dbmigrations", "cus-dbmigrations", "pds-dbmigrations", "ibs-dbmigrations", "pcs-dbmigrations", "mdr-dbmigrations", "pcscamunda-dbmigrations");

  @Autowired
  private BuildService buildService;

  @Autowired
  private GitService gitService;

  @Autowired
  private DbUpdateService dbUpdateService;

  @Autowired
  private CopyService copyService;

  @Autowired
  private WildflyService wildflyService;

  @RequestMapping(value = "/deployment", method = GET)
  public String getDeployment(Model model) {
    model.addAttribute("buildModules", modules);
    model.addAttribute("copyModules", copyModules);
    model.addAttribute("dbModules", dbModules);
    model.addAttribute("deployment", new DeploymentModel());
    
    return "deployment";
  }
  

  @RequestMapping(value = "/deployment", method = POST)
  public Model postDeployment(@ModelAttribute DeploymentModel deploymentModel, Model model) throws IOException {
    model.addAttribute("buildModules", modules);
    model.addAttribute("dbModules", dbModules);
    model.addAttribute("copyModules", copyModules);
    model.addAttribute("deployment", deploymentModel);

    int gitExitCode = -1;
    if (deploymentModel.isGitUpdate()) {
      gitExitCode = gitService.update();
    }

    int mavenExitCode = -1;
    if (!CollectionUtils.isEmpty(deploymentModel.getDeployment())) {
      mavenExitCode = buildService.build(deploymentModel.getDeployment());
    }

    int wildflyRestartExitCode = -1;
    if (deploymentModel.isDbUpdate() || !CollectionUtils.isEmpty(deploymentModel.getCopyModules())) {
      wildflyRestartExitCode = wildflyService.execute(ShellCommands.WIDLFLY_STOP);
    }

    int liquibaseExitCode = -1;
    if (deploymentModel.isDbUpdate()) {
      liquibaseExitCode = dbUpdateService.update();
    }

    int copyExitCode = -1;
    if (!CollectionUtils.isEmpty(deploymentModel.getCopyModules())) {
      copyExitCode = copyService.copy(deploymentModel.getCopyModules());
    }

    if (deploymentModel.isDbUpdate() || !CollectionUtils.isEmpty(deploymentModel.getCopyModules())) {
      wildflyRestartExitCode += wildflyService.execute(ShellCommands.WIDLFLY_START);
    }

    model.addAttribute("gitExitCode", gitExitCode);
    model.addAttribute("mavenExitCode", mavenExitCode);
    model.addAttribute("liquibaseExitCode", liquibaseExitCode);
    model.addAttribute("copyExitCode", copyExitCode);
    model.addAttribute("wildflyRestartExitCode", wildflyRestartExitCode);

    return model;
  }

}
