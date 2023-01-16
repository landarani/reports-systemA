package org.cotalent.reports.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "SystemA Reporting", description = "A rest service to for trade reports"))
public class ReportsApplication {
  public static void main(String[] args) {
    SpringApplication.run(ReportsApplication.class, args);
  }
}
