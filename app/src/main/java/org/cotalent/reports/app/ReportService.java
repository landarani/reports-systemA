package org.cotalent.reports.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ReportService {
  private final String outputBaseFolder;

  public ReportService(@Value("${reports.base-folder.output}") String outputFolder) {
    this.outputBaseFolder = outputFolder;
  }
}
