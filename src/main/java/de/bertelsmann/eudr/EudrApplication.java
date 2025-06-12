package de.bertelsmann.eudr;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import eu.europa.ec.tracesnt.eudr.echo.EudrEchoResponseType;

@SpringBootApplication
public class EudrApplication {

	public static void main(String[] args) {
		SpringApplication.run(EudrApplication.class, args);
	}

	@Bean
	CommandLineRunner lookup(EchoServiceClient client) {
		return args -> {
			String query = "Hello";

			EudrEchoResponseType response = client.getEchoServiceResponse(query);
			System.err.println(response.getStatus());
		};
	}
}
