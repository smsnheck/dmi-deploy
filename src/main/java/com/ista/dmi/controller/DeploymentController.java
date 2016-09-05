package com.ista.dmi.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.Arrays;
import java.util.List;

import com.ista.dmi.service.BuildService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DeploymentController {
  private List<String> modules = Arrays.asList("common", "cds", "cus", "pds", "ibs", "pcs", "mdr");
  private List<String> dbModules = Arrays.asList("cds-dbmigrations", "cus-dbmigrations", "pds-dbmigrations", "ibs-dbmigrations", "pcs-dbmigrations", "mdr-dbmigrations", "pcscamunda-dbmigrations");

  @Autowired
  private BuildService buildService;

  @RequestMapping(value = "/deployment", method = GET)
  public String getDeployment(Model model) {
    model.addAttribute("buildModules", modules);
    model.addAttribute("dbModules", dbModules);
    model.addAttribute("deployment", new DeploymentModel());
    
    return "deployment";
  }
  

  @RequestMapping(value = "/deployment", method = POST)
  public Model postDeployment(@ModelAttribute DeploymentModel deploymentModel, Model model) {
    model.addAttribute("buildModules", modules);
    model.addAttribute("dbModules", dbModules);
    model.addAttribute("deployment", new DeploymentModel());

    buildService.build(deploymentModel.getDeployment());
    
    return model;
  }

}
