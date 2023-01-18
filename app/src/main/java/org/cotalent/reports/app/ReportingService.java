package org.cotalent.reports.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.cotalent.reports.app.dto.TradeAggregate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ReportingService {
  private static final String REPORT_FILE_NAME = "Output.csv";

  private final DateTimeFormatter folderFormat = DateTimeFormatter.ofPattern("yyyyMMdd");

  private final File reportBaseFile;
  private final Clock clock;

  public ReportingService(Clock clock, @Value("${reports.base-folder.output}") String reportBaseFolder) {
    this.reportBaseFile = new File(reportBaseFolder);
    this.clock = clock;
  }

  public Flux<Map<LocalDate, List<TradeAggregate>>> getTradeAggregates(byte noOfDays) {
    return getTradeAggregates(LocalDate.now(clock).minusDays(1), noOfDays);
  }

  public Flux<Map<LocalDate, List<TradeAggregate>>> getTradeAggregates(LocalDate lastDate, byte noOfDays) {
    // TO-DO: limit the number of days to avoid DOS attack.
    LocalDate startDate = lastDate.minusDays(noOfDays);
    return Flux.generate(() -> lastDate,
        (state, sink) -> {
          while (state.isAfter(startDate)) {
            log.debug("Checking report for day [{}]...", state);
            File reportFile = new File(reportBaseFile, state.format(folderFormat) + File.separator + REPORT_FILE_NAME);
            if (reportFile.exists()) {
              log.debug("Reading report content for [{}]", state);
              try (BufferedReader reader = new BufferedReader(new FileReader(reportFile))) {
                sink.next(Collections.singletonMap(state, unmarshal(reader)));
                return state.minusDays(1);
              } catch (Exception x) {
                log.error("Error preparing report for [{}] ", state, x);
              }
            }
            state = state.minusDays(1);
          }
          log.debug("[{}] is before earliest date [{}]. Completing <===", state, startDate);
          sink.complete();
          return state;
        });
  }

  public Mono<Resource> downloadCsv(LocalDate date) throws Exception {
    File reportFile = new File(reportBaseFile, date.format(folderFormat) + File.separator + REPORT_FILE_NAME);
    if (!reportFile.exists()) {
      throw new FileNotFoundException(reportFile.getPath());
    }
    return Mono.just(new FileSystemResource(reportFile));
  }

  private List<TradeAggregate> unmarshal(BufferedReader reader) throws Exception {
    String header = reader.readLine();
    // skipping header
    log.debug("Reading the content of a CSV report and skipping header: [{}]", header);
    return reader.lines().map(TradeAggregate::of).collect(Collectors.toList());
  }
}
