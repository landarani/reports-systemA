package org.cotalent.reports.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.cotalent.reports.app.dto.TradeAggregate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ReportService {
  private static final String REPORT_FILE_NAME = "Output.csv";

  private final DateTimeFormatter folderFormat = DateTimeFormatter.ofPattern("yyyyMMdd");

  private final File reportBaseFile;

  public ReportService(@Value("${reports.base-folder.output}") String reportBaseFolder) {
    this.reportBaseFile = new File(reportBaseFolder);
  }

  public Flux<Map<LocalDate, TradeAggregate>> getTradeAggregates(LocalDate lastDate, int noOfDays) {
    return Flux.empty();
  }

  public Mono<Resource> downloadCsv(LocalDate date) throws Exception {
    File reportFile = new File(reportBaseFile, date.format(folderFormat) + File.separator + REPORT_FILE_NAME);
    if (!reportFile.exists()) {
      throw new FileNotFoundException(reportFile.getPath());
    }
    return Mono.just(new FileSystemResource(reportFile));
  }
}
