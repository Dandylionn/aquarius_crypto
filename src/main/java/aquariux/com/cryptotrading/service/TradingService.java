package aquariux.com.cryptotrading.service;

import aquariux.com.cryptotrading.entity.Trade;
import aquariux.com.cryptotrading.entity.Wallet;

import java.math.BigDecimal;
import java.util.List;

public interface TradingService {
    
    Trade executeTrade(Long userId, String tradePair, String tradeType, BigDecimal amount, BigDecimal price);

    List<Wallet> getWalletsByUserId(Long userId);

    void saveWallet(Wallet wallet);

    List<Trade> getTradeHistory(Long userId);
}
