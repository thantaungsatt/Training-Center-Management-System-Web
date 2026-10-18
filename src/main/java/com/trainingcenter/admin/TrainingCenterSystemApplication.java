package com.trainingcenter.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.trainingcenter.entities")
@EnableJpaRepositories("com.trainingcenter.repositories")
@ComponentScan("com.trainingcenter")
public class TrainingCenterSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrainingCenterSystemApplication.class, args);
	}

}
