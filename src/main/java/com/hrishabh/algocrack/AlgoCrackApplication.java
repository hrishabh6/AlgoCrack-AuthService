package com.hrishabh.algocrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("com.hrishabh.algocrack.repository")

@EntityScan("com.hrishabh.algocrackentityservice.models")
@EnableJpaAuditing
public class AlgoCrackApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlgoCrackApplication.class, args);
    }

}
