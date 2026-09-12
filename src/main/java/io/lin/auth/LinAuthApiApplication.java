package io.lin.auth;

import io.lin.auth.feature.auth.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class LinAuthApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(LinAuthApiApplication.class, args);
	}

}
