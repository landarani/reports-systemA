package org.cotalent.reports.app;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.time.LocalDate;

import org.cotalent.reports.app.dto.Trade;
import org.junit.Before;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;
import reactor.test.StepVerifier;

@Slf4j
public class TradeConsumerTest {
  private final TradeConsumer sut = new TradeConsumer();

  @Before
  public void reset() {
    sut.reset();
  }

  @Test
  public void allTheSameTest() {
    Trade t = Trade.builder()
        .longQuantity(5L)
        .shortQuantity(1L)
        .client(Trade.Client.builder()
            .accountNumber(1234)
            .clientNumber(111)
            .clientType("CL")
            .subAccountNumber(2).build())
        .product(Trade.Product
            .builder()
            .exchangeCode("ABC")
            .productGroupCode("PG")
            .symbol("S")
            .expirationDate(LocalDate.parse("2023-01-01"))
            .build())
        .build();
    sut.accept(t);
    sut.accept(t);
    sut.accept(t);

    StepVerifier.create(sut.report())
        .assertNext(r -> {
          log.info("Checking report [{}]", r.toCsvLine());
          assertThat(r.getTotalTransactionAmount(), is(12L));
          assertThat(r.getClient().getAccountNumber(), is(1234));
          assertThat(r.getProduct().getProductGroupCode(), is("PG"));
        })
        .expectComplete()
        .verify();
  }

  @Test
  public void withTwoTradesTest() {
    Trade t = Trade.builder()
        .longQuantity(5L)
        .shortQuantity(1L)
        .client(Trade.Client.builder()
            .accountNumber(1234)
            .clientNumber(111)
            .clientType("CL")
            .subAccountNumber(2).build())
        .product(Trade.Product
            .builder()
            .exchangeCode("ABC")
            .productGroupCode("PG")
            .symbol("S")
            .expirationDate(LocalDate.parse("2023-01-01"))
            .build())
        .build();
    sut.accept(t);
    sut.accept(t);
    t.setClient(Trade.Client.builder()
        .accountNumber(4321)
        .clientNumber(111)
        .clientType("CL")
        .subAccountNumber(2).build());
    sut.accept(t);

    StepVerifier.create(sut.report())
        .assertNext(r -> {
          log.info("Checking report [{}]", r.toCsvLine());
          assertThat(r.getTotalTransactionAmount(), is(8L));
          assertThat(r.getClient().getAccountNumber(), is(1234));
          assertThat(r.getProduct().getProductGroupCode(), is("PG"));
        })
        .assertNext(r -> {
          log.info("Checking report [{}]", r.toCsvLine());
          assertThat(r.getTotalTransactionAmount(), is(4L));
          assertThat(r.getClient().getAccountNumber(), is(4321));
          assertThat(r.getProduct().getProductGroupCode(), is("PG"));
        })
        .expectComplete()
        .verify();
  }
}
