package org.cotalent.reports.app;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.cotalent.reports.app.dto.TradeAggregate;
import org.springframework.core.io.Resource;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "Trade Report APIs", description = "APIs to receive report for daily trades.")
public class ReportController {

  private final DateTimeFormatter folderNameDate = DateTimeFormatter.ofPattern("yyyyMMdd");

  private final ReportingService service;

  @GetMapping(path = { "", "/" })
  @ApiResponse(description = "Returns <noOfDays> days of the reports on and before the latest day of available report.")
  public Flux<Map<LocalDate, List<TradeAggregate>>> getLatestReports(
      @RequestParam(name = "noOfDays", required = false, defaultValue = "1") byte noOfDays) {
    return service.getTradeAggregates(noOfDays);
  }

  @GetMapping(path = "/{date}")
  @ApiResponse(description = "Returns <noOfDays> days of the reports on and before the given date.")
  public Flux<Map<LocalDate, List<TradeAggregate>>> getReport(@PathVariable LocalDate date,
      @RequestParam(name = "noOfDays", required = false, defaultValue = "1") byte noOfDays) {
    return service.getTradeAggregates(date, noOfDays);
  }

  @GetMapping(path = "/{date}/csv", produces = "text/csv")
  @ApiResponse(description = "Downloads the report in CSV format for the requested date.")
  public Mono<Resource> downloadCsv(@PathVariable LocalDate date, ServerHttpResponse response) throws Exception {
    response.getHeaders().add("content-disposition",
        "attachment; filename=Output." + date.format(folderNameDate) + ".csv");
    return service.downloadCsv(date);
  }

  @GetMapping(path = "/{date}", headers = "Accept=text/csv", produces = "text/csv")
  @ApiResponse(description = "Downloads the report in CSV format for the requested date.")
  public Mono<Resource> download(@PathVariable LocalDate date, ServerHttpResponse response) throws Exception {
    return downloadCsv(date, response);
  }
}
