package com.ista.dmi;

import java.util.Collections;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.ista.dmi.service.BuildService;

@SpringBootApplication
public class DmiDeployApplication {

  @Autowired
  private BuildService buildService;
  
	public static void main(String[] args) throws Exception {
		SpringApplication.run(DmiDeployApplication.class, args);
	}
	
	@PostConstruct
	private void init() {
	  buildService.build(Collections.EMPTY_LIST);
	}
	
}
