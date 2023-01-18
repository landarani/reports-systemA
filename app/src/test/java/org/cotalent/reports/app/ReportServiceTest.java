package org.cotalent.reports.app;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.cotalent.reports.app.dto.TradeAggregate;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.springframework.util.FileCopyUtils;

import lombok.extern.slf4j.Slf4j;
import reactor.test.StepVerifier;

@Slf4j
public class ReportServiceTest {

  private static final String REPORT_CONTENT_LINE_1 = "Client_Information,Product_Information,Total_Transaction_Amount";
  private static final String REPORT_CONTENT_LINE_2 = "CL_4321_3_1,CME_FU_N1_2010-09-10,-79";

  private ReportingService sut;

  @Rule
  public TemporaryFolder baseFolder = new TemporaryFolder();

  @Before
  public void init() throws Exception {
    // Test for normal report
    try (PrintWriter out = new PrintWriter(new File(baseFolder.newFolder("20220116"), "Output.csv"))) {
      out.println(REPORT_CONTENT_LINE_1);
      out.println(REPORT_CONTENT_LINE_2);
    }

    // Included in more than one day report
    try (OutputStream target = new FileOutputStream(new File(baseFolder.newFolder("20220114"), "Output.csv"))) {
      FileCopyUtils.copy(getClass().getResourceAsStream("/Output.csv"), target);
    }

    // Malformed report
    try (PrintWriter out = new PrintWriter(new File(baseFolder.newFolder("20220115"), "Output.csv"))) {
      out.println(REPORT_CONTENT_LINE_2);
      out.println("blah");
      out.println("blah");
    }

    LocalDate testDate = LocalDate.parse("2022-01-15");
    Clock clock = Mockito.mock(Clock.class);
    Clock fixed = Clock.fixed(testDate.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
    when(clock.instant()).thenReturn(fixed.instant());
    when(clock.getZone()).thenReturn(fixed.getZone());
    sut = new ReportingService(clock, baseFolder.getRoot().getPath());
  }

  @After
  public void clean() throws Exception {
    baseFolder.delete();
  }

  @Test
  public void shouldThrowNotFoundExceptionWithWrongDate() {
    assertThrows(FileNotFoundException.class, () -> {
      sut.downloadCsv(LocalDate.parse("2022-01-13"));
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

  @Test
  public void shouldReadOneDayReport() throws Exception {
    LocalDate lastDay = LocalDate.parse("2022-01-16");
    StepVerifier.create(sut.getTradeAggregates(lastDay, (byte) 1))
        .assertNext(map -> {
          assertThat(map, hasKey(lastDay));
          assertThat(map, aMapWithSize(1));
          List<TradeAggregate> tal = map.get(lastDay);
          assertThat(tal, hasSize(1));
          TradeAggregate ta = tal.get(0);
          log.info("[shouldReadOneDayReport] Verifying the report: [{}]", ta);
          // Verifying Client Info
          assertThat(ta.getClient().getClientType(), is("CL"));
          assertThat(ta.getClient().getClientNumber(), is(4321));
          assertThat(ta.getClient().getAccountNumber(), is(3));
          assertThat(ta.getClient().getSubAccountNumber(), is(1));

          // Verifying Product Info
          assertThat(ta.getProduct().getExchangeCode(), is("CME"));
          assertThat(ta.getProduct().getProductGroupCode(), is("FU"));
          assertThat(ta.getProduct().getSymbol(), is("N1"));
          assertThat(ta.getProduct().getExpirationDate(), is(LocalDate.parse("2010-09-10")));

          // Verifying Total Transaction Amount
          assertThat(ta.getTotalTransactionAmount(), is(-79L));
        })
        .verifyComplete();
  }

  @Test
  public void shouldReadYesterdayReportByDefault() throws Exception {
    LocalDate reportDay = LocalDate.parse("2022-01-14");
    StepVerifier.create(sut.getTradeAggregates((byte) 1))
        .assertNext(map -> {
          assertThat(map, hasKey(reportDay));
          assertThat(map, aMapWithSize(1));
          List<TradeAggregate> tal = map.get(reportDay);
          assertThat(tal, hasSize(5));
          TradeAggregate ta = tal.get(2);
          log.info("[shouldReadYesterdayReportByDefault] Verifying the report: [{}]", ta);
          // Verifying Client Info
          assertThat(ta.getClient().getClientType(), is("CL"));
          assertThat(ta.getClient().getClientNumber(), is(4321));
          assertThat(ta.getClient().getAccountNumber(), is(2));
          assertThat(ta.getClient().getSubAccountNumber(), is(1));

          // Verifying Product Info
          assertThat(ta.getProduct().getExchangeCode(), is("SGX"));
          assertThat(ta.getProduct().getProductGroupCode(), is("FU"));
          assertThat(ta.getProduct().getSymbol(), is("NK"));
          assertThat(ta.getProduct().getExpirationDate(), is(LocalDate.parse("2010-09-10")));

          // Verifying Total Transaction Amount
          assertThat(ta.getTotalTransactionAmount(), is(46L));
        })
        .verifyComplete();
  }

  @Test
  public void shouldReadFromPeriod() throws Exception {
    LocalDate lastDay = LocalDate.parse("2022-01-16");
    LocalDate firstDay = LocalDate.parse("2022-01-14");
    LocalDate malFormedDay = LocalDate.parse("2022-01-15");
    StepVerifier.create(sut.getTradeAggregates(lastDay, (byte) 10))
        .assertNext(map -> {
          assertThat(map, hasKey(lastDay));
          assertThat(map, aMapWithSize(1));
          List<TradeAggregate> tal = map.get(lastDay);
          assertThat(tal, hasSize(1));
          TradeAggregate ta = tal.get(0);
          log.info("[shouldReadFromPeriod] Verifying the report: [{}]", ta);
          // Verifying Client Info
          assertThat(ta.getClient().getClientType(), is("CL"));
          assertThat(ta.getClient().getClientNumber(), is(4321));
          assertThat(ta.getClient().getAccountNumber(), is(3));
          assertThat(ta.getClient().getSubAccountNumber(), is(1));

          // Verifying Product Info
          assertThat(ta.getProduct().getExchangeCode(), is("CME"));
          assertThat(ta.getProduct().getProductGroupCode(), is("FU"));
          assertThat(ta.getProduct().getSymbol(), is("N1"));
          assertThat(ta.getProduct().getExpirationDate(), is(LocalDate.parse("2010-09-10")));

          // Verifying Total Transaction Amount
          assertThat(ta.getTotalTransactionAmount(), is(-79L));
        })
        /*
         * .assertNext(entry->
         * 
         * {
         * assertThat(entry, hasKey(malFormedDay));
         * StepVerifier.create(entry.get(malFormedDay))
         * .expectError();
         * })
         */
        .assertNext(map -> {
          assertThat(map, hasKey(firstDay));
          assertThat(map.get(firstDay), hasSize(5));
        }).verifyComplete();
  }

  @Test
  public void shouldReturnEmptyWithNoReport() {
    LocalDate noReportDay = LocalDate.parse("2022-01-13");
    StepVerifier.create(sut.getTradeAggregates(noReportDay, (byte) 1))
        .verifyComplete();

  }

  @Test
  public void shouldReturnEmptyIfMalformed() {
    LocalDate malFormedDay = LocalDate.parse("2022-01-15");
    StepVerifier.create(sut.getTradeAggregates(malFormedDay, (byte) 1))
        .verifyComplete();
  }
}
