package aquariux.com.cryptotrading.service;

import aquariux.com.cryptotrading.entity.Trade;
import aquariux.com.cryptotrading.entity.Wallet;

import java.math.BigDecimal;
import java.util.List;

public interface TradingService {

    // Execute a trade (buy/sell)
    Trade executeTrade(Long userId, String tradePair, String tradeType, BigDecimal amount, BigDecimal price);

    // Fetch wallet balances for a user
    List<Wallet> getWalletsByUserId(Long userId);

    void saveWallet(Wallet wallet);

    // Retrieve trade history for a user
    List<Trade> getTradeHistory(Long userId);
}
