package org.cotalent.reports.app.it;

import java.time.Duration;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import lombok.extern.slf4j.Slf4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.Matchers.endsWith;

@ExtendWith(SpringExtension.class)
@Slf4j
public class ReportsApiIT {
  private final static int RESPONSE_TIMEOUT_SECONDS = 5;
  private final static String BASE_URL = "http://127.0.0.1:8080";

  @Test
  @DisplayName("Should download csv when Accept is text/csv.")
  public void shouldDownloadCsvWithHeader() {
    log.info(" Ensure that Spring boot is started manually at [{}]...", BASE_URL);
    log.info("----------> Starting download test with the header...");
    WebTestClient.bindToServer()
        .baseUrl(BASE_URL)
        .responseTimeout(Duration.ofSeconds(RESPONSE_TIMEOUT_SECONDS))
        .build()
        .get()
        .uri(builder -> builder.path("/reports/2023-01-18").build())
        .header("Accept", "text/csv")
        .exchange()
        .expectBody()
        .consumeWith(c -> {
          String body = new String(c.getResponseBodyContent());
          try {
            log.info("Response: \n{}", body);
          } catch (Exception x) {
            log.error("Error in printing response", x);
          }
          assertThat(body, startsWith("Client_Information,Product_Information,Total_Transaction_Amount"));
          assertThat(body.trim(), endsWith("CL_1234_3_1,CME_FU_NK._2010-09-10,-215"));
        });
  }

  @Test
  @DisplayName("Should download csv when url ends with /csv.")
  public void shouldDownloadCsvWhenRequestedInUrl() {
    log.info(" Ensure that Spring boot is started manually at [{}]...", BASE_URL);
    log.info("----------> Starting download test within the URL...");
    WebTestClient.bindToServer()
        .baseUrl(BASE_URL)
        .responseTimeout(Duration.ofSeconds(RESPONSE_TIMEOUT_SECONDS))
        .build()
        .get()
        .uri(builder -> builder.path("/reports/2023-01-18/csv").build())
        .exchange()
        .expectBody()
        .consumeWith(c -> {
          String body = new String(c.getResponseBodyContent());
          try {
            log.info("Response: \n{}", body);
          } catch (Exception x) {
            log.error("Error in printing response", x);
          }
          assertThat(body, startsWith("Client_Information,Product_Information,Total_Transaction_Amount"));
          assertThat(body.trim(), endsWith("CL_1234_3_1,CME_FU_NK._2010-09-10,-215"));
        });
  }

  @Test
  @DisplayName("Should get jason when Accept is not specified.")
  public void shouldGetJsonOtherwise() {
    log.info(" Ensure that Spring boot is started manually at [{}]...", BASE_URL);
    log.info("----------> Starting the test without Accept header...");
    WebTestClient.bindToServer()
        .baseUrl(BASE_URL)
        .responseTimeout(Duration.ofSeconds(RESPONSE_TIMEOUT_SECONDS))
        .build()
        .get()
        .uri(builder -> builder.path("/reports/2023-01-18").build())
        .exchange()
        .expectBody()
        .jsonPath("$[0].2023-01-18").isArray()
        .consumeWith(c -> {
          try {
            log.info("Response: \n{}", new String(c.getResponseBodyContent()));
          } catch (Exception x) {
            log.error("Error in printing response", x);
          }
        });
  }

  @Test
  @DisplayName("Should get jason in response with noOfDays")
  public void shouldGetJson() {
    log.info(" Ensure that Spring boot is started manually at [{}]...", BASE_URL);
    log.info("----------> Starting the test for a date and noOfDays...");
    WebTestClient.bindToServer()
        .baseUrl(BASE_URL)
        .responseTimeout(Duration.ofSeconds(RESPONSE_TIMEOUT_SECONDS))
        .build()
        .get()
        .uri(builder -> builder.path("/reports/2023-01-18").queryParam("noOfDays", "3")
            .build())
        .exchange()
        .expectBody()
        .jsonPath("$[0].2023-01-18").isArray()
        .jsonPath("$[1].2023-01-17").isArray()
        .consumeWith(c -> {
          try {
            log.info("Response: \n{}", new String(c.getResponseBodyContent()));
          } catch (Exception x) {
            log.error("Error in printing response", x);
          }
        });
  }

  @Test
  @DisplayName("Should get latest date is no date is given")
  public void shouldGetLatesDateIfNoDateIsGiven() {
    log.info(" Ensure that Spring boot is started manually at [{}]...", BASE_URL);
    log.info("----------> Starting the test with the root URL...");
    WebTestClient.bindToServer()
        .baseUrl(BASE_URL)
        .responseTimeout(Duration.ofSeconds(RESPONSE_TIMEOUT_SECONDS))
        .build()
        .get()
        .uri(builder -> builder.path("/reports").queryParam("noOfDays", "3")
            .build())
        .exchange()
        .expectBody()
        .jsonPath("$").isArray()
        .consumeWith(c -> {
          try {
            log.info("Response: \n{}", new String(c.getResponseBodyContent()));
          } catch (Exception x) {
            log.error("Error in printing response", x);
          }
        });
  }

  @Test
  @DisplayName("Should work with trailing slash in root url")
  public void shouldIgnoreTrailingSlashAtRoot() {
    log.info(" Ensure that Spring boot is started manually at [{}]...", BASE_URL);
    log.info("----------> Starting the test with trailing slash...");
    WebTestClient.bindToServer()
        .baseUrl(BASE_URL)
        .responseTimeout(Duration.ofSeconds(RESPONSE_TIMEOUT_SECONDS))
        .build()
        .get()
        .uri(builder -> builder.path("/reports/")
            .build())
        .exchange()
        .expectBody()
        .jsonPath("$").isArray()
        .consumeWith(c -> {
          try {
            log.info("Response: \n{}", new String(c.getResponseBodyContent()));
          } catch (Exception x) {
            log.error("Error in printing response", x);
          }
        });
  }
}
