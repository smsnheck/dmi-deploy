package com.ista.dmi;

import javax.annotation.PostConstruct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class DmiDeployApplication {

//  @Autowired
//  private BuildService buildService;
  
	public static void main(String[] args) throws Exception {
		SpringApplication.run(DmiDeployApplication.class, args);
	}
	
	@PostConstruct
	private void init() {
//	  buildService.build(Collections.EMPTY_LIST);
	}
	
}
