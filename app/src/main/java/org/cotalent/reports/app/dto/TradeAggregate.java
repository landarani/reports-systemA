package org.cotalent.reports.app.dto;

import java.util.Objects;

import org.springframework.util.ObjectUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class TradeAggregate {

  public static TradeAggregate of(String record) {
    log.debug("Unmarshaling [{}] into TradeAggregate object", record);
    String[] values = record.split(",");
    return builder()
        .client(Trade.Client.of(values[0]))
        .product(Trade.Product.of(values[1]))
        .totalTransactionAmount(Long.parseLong(values[2]))
        .build();
  };

  private final Trade.Client client;
  private final Trade.Product product;

  private long totalTransactionAmount;

  public void moveTotalBy(long movement) {
    this.totalTransactionAmount += movement;
  }

  @Override
  public int hashCode() {
    return Objects.hash(client, product);
  }

  @Override
  public boolean equals(Object other) {
    return ObjectUtils.nullSafeHashCode(other) == hashCode();
  }

  public String toCsvLine() {
    return client + "," + product + "," + totalTransactionAmount;
  }
}
