package org.cotalent.reports.app;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import lombok.extern.slf4j.Slf4j;
import reactor.test.StepVerifier;

@Slf4j
public class ReportServiceTest {

  private static final String REPORT_CONTENT_LINE_1 = "Client_Information,Product_Information,Total_Transaction_Amount";
  private static final String REPORT_CONTENT_LINE_2 = "CL_4321_3_1,CME_FU_N1_2010-09-10,-79";

  private ReportingService sut;

  @Rule
  public TemporaryFolder input = new TemporaryFolder();

  @Before
  public void init() throws Exception {
    File reportFolder = input.newFolder("20220116");
    sut = new ReportingService(reportFolder.getParent());
    File report = new File(reportFolder + File.separator + "Output.csv");
    try (PrintWriter out = new PrintWriter(new FileWriter(report))) {
      out.println(REPORT_CONTENT_LINE_1);
      out.println(REPORT_CONTENT_LINE_2);
    }
  }

  @Test
  public void shouldThrowNotFoundExceptionWithWrongDate() {
    assertThrows(FileNotFoundException.class, () -> {
      sut.downloadCsv(LocalDate.parse("2022-01-15"));
    });
  }

  @Test
  public void shouldDownloadReport() throws Exception {
    String expectedFileContent = REPORT_CONTENT_LINE_1 + "\n" + REPORT_CONTENT_LINE_2 + "\n";
    StepVerifier.create(sut.downloadCsv(LocalDate.parse("2022-01-16")))
        .assertNext(r -> {
          String actualFileContent = "";
          try {
            actualFileContent = Files.readString(Paths.get(r.getURI()), Charset.defaultCharset());
          } catch (Exception x) {
            log.error("Error in reading the test file content", x);
          }
          log.info("Actual output Content: \n[{}]", actualFileContent);
          assertThat(actualFileContent, is(expectedFileContent));
        })
        .verifyComplete();
  }
}
