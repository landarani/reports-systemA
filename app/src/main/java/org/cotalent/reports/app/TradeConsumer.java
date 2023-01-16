package org.cotalent.reports.app;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.cotalent.reports.app.dto.Trade;
import org.cotalent.reports.app.dto.TradeAggregate;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;

@Component
public class TradeConsumer implements Consumer<Trade> {
  private final Map<TradeAggregate, TradeAggregate> report = new HashMap<>();

  @Override
  public void accept(Trade trade) {
    TradeAggregate next = new TradeAggregate(trade.getClient(), trade.getProduct());
    report.getOrDefault(next, next).moveTotalBy(trade.quantity());
    report.putIfAbsent(next, next);
  }

  public void reset() {
    report.clear();
  }

  public Flux<TradeAggregate> report() {
    return Flux.fromStream(report.keySet().stream());
  }
}
