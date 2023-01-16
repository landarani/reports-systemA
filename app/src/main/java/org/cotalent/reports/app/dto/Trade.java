package org.cotalent.reports.app.dto;

import java.time.LocalDate;

import org.cotalent.reports.model.Fields;
import org.cotalent.reports.model.SystemAField;
import org.cotalent.reports.model.SystemARecord;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Trade {
  private static final char SEPERATOR = '_';

  @SystemARecord
  private Client client;

  @SystemARecord
  private Product product;

  @SystemAField(field = Fields.QUANTITY_LONG)
  private Long longQuantity;

  @SystemAField(field = Fields.QUANTITY_SHORT)
  private Long shortQuantity;

  public Long quantity() {
    return longQuantity - shortQuantity;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Client {
    @SystemAField(field = Fields.CLIENT_TYPE)
    private String clientType;

    @SystemAField(field = Fields.CLIENT_NUMBER)
    private Number clientNumber;

    @SystemAField(field = Fields.ACCOUNT_NUMBER)
    private Number accountNumber;

    @SystemAField(field = Fields.SUBACCOUNT_NUMBER)
    private Number subAccountNumber;

    @Override
    public String toString() {
      return clientType + SEPERATOR + clientNumber + SEPERATOR + accountNumber + SEPERATOR + subAccountNumber;
    }
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Product {
    @SystemAField(field = Fields.EXCHANGE_CODE)
    private String exchangeCode;

    @SystemAField(field = Fields.PRODUCT_GROUP_CODE)
    private String productGroupCode;

    @SystemAField(field = Fields.SYMBOL)
    private String symbol;

    @SystemAField(field = Fields.EXPIRATION_DATE)
    private LocalDate expirationDate;

    @Override
    public String toString() {
      return exchangeCode + SEPERATOR + productGroupCode + SEPERATOR + symbol + SEPERATOR + expirationDate;
    }
  }
}
