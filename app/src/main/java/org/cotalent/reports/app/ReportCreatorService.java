package org.cotalent.reports.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.cotalent.reports.app.dto.Trade;
import org.cotalent.reports.app.dto.TradeAggregate;
import org.cotalent.reports.model.SystemAMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class ReportCreatorService {
  private static final String INPUT_FILE_NAME = "Input.txt";
  private static final String OUTPUT_FILE_NAME = "Output.csv";
  private static final String CSV_HEADER = "Client_Information,Product_Information,Total_Transaction_Amount";

  @Value("${reports.base-folder.output}")
  private String outputFolder;

  @Value("${reports.base-folder.input}")
  private String inputFolder;

  private final DateTimeFormatter folderFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
  private final SystemAMapper mapper = new SystemAMapper();

  private final TradeConsumer tradeConsumer;

  @Scheduled(fixedRate = 10, timeUnit = TimeUnit.SECONDS)
  public void scan() {
    File input = new File(inputFolder);
    log.debug("Scanning: [{}] and checking output from [{}]", inputFolder, outputFolder);
    if (!input.exists() && input.isFile()) {
      log.warn("[{}] is not a directory or doesn't exist.", inputFolder);
    }
    File[] subfolders = input.listFiles(File::isDirectory);
    if (subfolders.length == 0) {
      log.warn("There is no input to process. Checking in some seconds...");
      return;
    }
    Arrays.stream(subfolders)
        .map(this::isAcceptableFolder)
        .filter(Objects::nonNull)
        .forEach(this::process);
  }

  public void process(LocalDate date) {
    log.debug("Processing the input file for the date [{}]", date);
    File outputFolderDate;
    try {
      outputFolderDate = new File(outputFolder, date.format(folderFormat));
      if (!outputFolderDate.exists()) {
        log.info("Creating the output folder for the date [{}]", date);
        if (!outputFolderDate.mkdir()) {
          throw new IOException(outputFolderDate.getPath() + " doesn't exist and cannot be created.");
        }
      }
    } catch (Exception x) {
      log.error("Unable to create output folder for the date [{}]", date, x);
      return;
    }

    try (
        FileReader fileReader = new FileReader(
            inputFolder + File.separator + date.format(folderFormat) + File.separator + INPUT_FILE_NAME);
        BufferedReader reader = new BufferedReader(fileReader);
        PrintWriter csvWriter = new PrintWriter(new FileWriter(new File(outputFolderDate, OUTPUT_FILE_NAME), false))) {
      mapper.read(Trade.class, reader).subscribe(tradeConsumer);
      csvWriter.println(CSV_HEADER);
      tradeConsumer.report().map(TradeAggregate::toCsvLine).subscribe(csvWriter::println);
    } catch (Exception x) {
      log.error("Error processing input for [{}]", date, x);
    } finally {
      tradeConsumer.reset();
    }
  }

  public LocalDate isAcceptableFolder(File file) {
    try {
      LocalDate date = LocalDate.from(folderFormat.parse(file.getName()));
      File input = new File(file, INPUT_FILE_NAME);
      File output = new File(outputFolder + File.separator + file.getName() + File.separator + OUTPUT_FILE_NAME);
      if (input.exists() && input.isFile() && !(output.exists() && output.isFile())) {
        return date;
      }
    } catch (Exception x) {
      log.debug("rejecting non standard folder [{}]", file.getPath());
    }
    return null;
  }
}
