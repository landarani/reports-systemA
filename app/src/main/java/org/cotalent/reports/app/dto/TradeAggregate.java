package org.cotalent.reports.app.dto;

import java.util.Objects;

import org.springframework.util.ObjectUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public class TradeAggregate {
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
