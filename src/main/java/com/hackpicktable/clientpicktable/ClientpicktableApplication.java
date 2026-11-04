package com.hackpicktable.clientpicktable;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ClientpicktableApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClientpicktableApplication.class, args);
	}

}
