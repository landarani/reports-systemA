package org.cotalent.reports.model;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.Test;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@Slf4j
public class SystemAMapperTest {
  private final SystemAMapper sut = new SystemAMapper();

  private static final String HEALTHY_LINE_1 = "315CL  432100020001SGXDC FUSGX NK    20100910JPY01B 0000000001 0000000000000000000060DUSD000000000030DUSD000000000000DJPY201008200012380     688032000092500000000             O";
  private static final String HEALTHY_LINE_2 = "315CL  123400020001SGXDC FUSGX NK    20100910JPY01S 0000000000 0000000001000000000060DUSD000000000030DUSD000000000000DJPY201008200011590     687583000092700000000             O";
  private static final String INCOMPLETE_LINE = "315CL  432100030001FCC   FUCME N1    20100910JPY01S 0000000000";

  @Test
  public void shouldParseHealthyInput() throws Exception {
    Flux<TestRecord> actual = sut.read(TestRecord.class, prepareHealthyInput());
    StepVerifier
        .create(actual)
        .assertNext(r -> {
          log.info("Asserting the next transaction [{}]", r);
          log.info("----------------------------------------------");
          assertThat(r.getBuySellCode(), is("B"));
          assertThat(r.getShortQuantity(), is(0L));
          assertThat(r.getLongQuantity(), is(1L));
          assertThat(r.getNonTxnField(), nullValue());
          // Asserting Client
          assertThat(r.getClient().getClientType(), is("CL"));
          assertThat(r.getClient().getClientNumber(), is(4321));
          assertThat(r.getClient().getAccountNumber(), is(2));
          assertThat(r.getClient().getSubAccountNumber(), is(1));
          // Asserting Product
          assertThat(r.getProduct().getProductGroupCode(), is("FU"));
          assertThat(r.getProduct().getExchangeCode(), is("SGX"));
          assertThat(r.getProduct().getSymbol(), is("NK"));
          assertThat(r.getProduct().getExpirationDate(), is(LocalDate.parse("2010-09-10")));
        })
        .assertNext(r -> {
          log.info("Asserting the next transaction [{}]", r);
          log.info("----------------------------------------------");
          assertThat(r.getBuySellCode(), is("S"));
          assertThat(r.getShortQuantity(), is(1L));
          assertThat(r.getLongQuantity(), is(0L));
          assertThat(r.getNonTxnField(), nullValue());
          // Asserting Client
          assertThat(r.getClient().getClientType(), is("CL"));
          assertThat(r.getClient().getClientNumber(), is(1234));
          assertThat(r.getClient().getAccountNumber(), is(2));
          assertThat(r.getClient().getSubAccountNumber(), is(1));
          // Asserting Product
          assertThat(r.getProduct().getProductGroupCode(), is("FU"));
          assertThat(r.getProduct().getExchangeCode(), is("SGX"));
          assertThat(r.getProduct().getSymbol(), is("NK"));
          assertThat(r.getProduct().getExpirationDate(), is(LocalDate.parse("2010-09-10")));
        }).expectComplete()
        .verify();
  }

  @Test
  public void shouldThrowErrorWithWrongType() throws Exception {
    StepVerifier
        .create(sut.read(WrongFieldType.class, prepareHealthyInput()))
        .expectError()
        .verify();
  }

  @Test
  public void shouldThrowErrorWithWrongInput() throws Exception {
    StepVerifier
        .create(sut.read(TestRecord.class, prepareMalformedInput()))
        .assertNext(r -> {
          assertThat(r.getBuySellCode(), is("B"));
          assertThat(r.getShortQuantity(), is(0L));
          assertThat(r.getLongQuantity(), is(1L));
        })
        .expectError()
        .verify();
  }

  BufferedReader prepareMalformedInput() throws IOException {
    try (ByteArrayOutputStream data = new ByteArrayOutputStream(); PrintStream out = new PrintStream(data);) {
      out.println(HEALTHY_LINE_1);
      out.println(INCOMPLETE_LINE);
      return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data.toByteArray())));
    }
  }

  BufferedReader prepareHealthyInput() throws IOException {
    try (ByteArrayOutputStream data = new ByteArrayOutputStream(); PrintStream out = new PrintStream(data);) {
      out.println(HEALTHY_LINE_1);
      out.println(HEALTHY_LINE_2);
      return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data.toByteArray())));
    }
  }

  @NoArgsConstructor
  public static class WrongFieldType {
    @SystemAField(field = Fields.BUY_SELL_CODE)
    private Long longQuantity;
  }

  @Getter
  @NoArgsConstructor
  @ToString
  public static class TestRecord {
    @SystemAField(field = Fields.QUANTITY_LONG)
    private Long longQuantity;

    @SystemAField(field = Fields.QUANTITY_SHORT)
    private Long shortQuantity;

    @SystemAField(field = Fields.BUY_SELL_CODE)
    private String buySellCode;

    @SystemARecord
    private Client client;

    @SystemARecord
    private Product product;

    private Long nonTxnField;

    @Data
    @NoArgsConstructor
    @ToString
    public static class Client {
      @SystemAField(field = Fields.CLIENT_TYPE)
      private String clientType;

      @SystemAField(field = Fields.CLIENT_NUMBER)
      private Number clientNumber;

      @SystemAField(field = Fields.ACCOUNT_NUMBER)
      private Number accountNumber;

      @SystemAField(field = Fields.SUBACCOUNT_NUMBER)
      private Number subAccountNumber;
    }

    @Data
    @NoArgsConstructor
    @ToString
    public static class Product {
      @SystemAField(field = Fields.EXCHANGE_CODE)
      private String exchangeCode;

      @SystemAField(field = Fields.PRODUCT_GROUP_CODE)
      private String productGroupCode;

      @SystemAField(field = Fields.SYMBOL)
      private String symbol;

      @SystemAField(field = Fields.EXPIRATION_DATE)
      private LocalDate expirationDate;
    }
  }
}
