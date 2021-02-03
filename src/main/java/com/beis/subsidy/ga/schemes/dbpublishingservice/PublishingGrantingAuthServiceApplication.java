package com.beis.subsidy.ga.schemes.dbpublishingservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.Bean;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@SpringBootApplication
//@EnableFeignClients("com.beis.subsidy.award.transperancy.dbpublishingservice.service")
public class PublishingGrantingAuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PublishingGrantingAuthServiceApplication.class, args);
	}
	
	@Bean
    public OpenAPI customOpenAPI(@Value("${application-description}") String appDesciption, @Value("${application-version}") String appVersion) {
     return new OpenAPI()
          .info(new Info()
          .title("BEIS Subsidy Control - Publishing Granting Authority Schemes ")
          .version("1.0")
          .description("BEIS Subsidy Control - Publishing Granting Authority Schemes APIs for transparency database")
          .termsOfService("http://swagger.io/terms/")
          .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }
	
	
}
