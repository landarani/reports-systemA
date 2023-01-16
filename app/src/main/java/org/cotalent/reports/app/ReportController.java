package org.cotalent.reports.app;

import java.time.LocalDate;

import org.cotalent.reports.app.dto.TradeAggregate;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/reports")
@Tag(name = "Trade Report APIs", description = "APIs to receive report for daily trades.")
public class ReportController {

  @GetMapping("/")
  @ApiResponse(description = "Returns <noOfDays> days of the reports on and before the latest day of available report.")
  public Flux<TradeAggregate> getLatestReport(
      @RequestParam(name = "noOfDays", required = false, defaultValue = "1") int noOfDays) {
    return null;
  }

  @GetMapping("/{date}")
  @ApiResponse(description = "Returns <noOfDays> days of the reports on and before the given date.")
  public Flux<TradeAggregate> getReport(@PathVariable LocalDate date,
      @RequestParam(name = "noOfDays", required = false, defaultValue = "1") int noOfDays) {
    return null;
  }

  @GetMapping(path = "/{date}/csv", produces = "text/csv")
  @ApiResponse(description = "Downloads the report in CSV format for the requested date.")
  public Resource downloadCsv(@PathVariable LocalDate date) {
    return null;
  }

  @GetMapping(path = "/{date}", headers = "Accept=text/csv", produces = "text/csv")
  @ApiResponse(description = "Downloads the report in CSV format for the requested date.")
  public Resource download(@PathVariable LocalDate date) {
    return null;
  }
}
