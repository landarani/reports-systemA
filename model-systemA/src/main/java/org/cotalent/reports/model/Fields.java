package org.cotalent.reports.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
public enum Fields {

  RECORD_CODE(1, 3),
  CLIENT_TYPE(4, 4),
  CLIENT_NUMBER(8, 4),
  ACCOUNT_NUMBER(12, 4),
  SUBACCOUNT_NUMBER(16, 4),
  OPPOSITE_PARTY_CODE(20, 6),
  PRODUCT_GROUP_CODE(26, 2),
  EXCHANGE_CODE(28, 4),
  SYMBOL(32, 6),
  EXPIRATION_DATE(38, 8),
  CURRENCY_CODE(46, 3),
  MOVEMENT_CODE(49, 2),
  BUY_SELL_CODE(51, 1),
  QUANTITY_LONG_SIGN(52, 1),
  QUANTITY_LONG(53, 10),
  QUANTITY_SHORT_SIGN(63, 1),
  QUANTITY_SHORT(64, 10),
  EXCHANGE_BROKER_FEE(74, 12),
  EXCHANGE_BROKER_FEE_D_C(86, 1),
  EXCHANGE_BROKER_FEE_CURRENCY_CODE(87, 3),
  CLEARING_FEE(90, 12),
  CLEARING_FEE_D_C(102, 1),
  CLEARING_FEE_CURRENCY_CODE(103, 3),
  COMMISSION(106, 12),
  COMMISSION_D_C(118, 1),
  COMMISSION_CURRENCY_CODE(119, 3),
  TRANSACTION_DATE(122, 8),
  FUTURE_REFERENCE(130, 6),
  TICKET_NUMBER(136, 6),
  EXTERNAL_NUMBER(142, 6),
  TRANSACTION_PRICE(148, 15),
  TRADER_INITIALS(163, 6),
  OPPOSITE_TRADER_ID(169, 7),
  OPEN_CLOSE_CODE(176, 1);

  private final int start;

  @Getter
  private final int len;

  public int getStartIndex() {
    return start - 1;
  }
}