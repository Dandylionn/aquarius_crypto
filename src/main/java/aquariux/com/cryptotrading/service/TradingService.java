package aquariux.com.cryptotrading.service;

import aquariux.com.cryptotrading.entity.Trade;
import java.math.BigDecimal;

public interface TradingService {

    Trade executeTrade(Long userId, String tradePair, String tradeType, BigDecimal amount, BigDecimal price);
}
