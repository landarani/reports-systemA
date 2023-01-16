package org.cotalent.reports.app.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {
  private final String message;
}
