package aquariux.com.cryptotrading.service.impl;

import aquariux.com.cryptotrading.entity.Trade;
import aquariux.com.cryptotrading.entity.User;
import aquariux.com.cryptotrading.entity.Wallet;
import aquariux.com.cryptotrading.repository.TradeRepository;
import aquariux.com.cryptotrading.repository.UserRepository;
import aquariux.com.cryptotrading.repository.WalletRepository;
import aquariux.com.cryptotrading.service.TradingService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TradingServiceImpl implements TradingService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TradeRepository tradeRepository;

    public TradingServiceImpl(UserRepository userRepository, WalletRepository walletRepository, TradeRepository tradeRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.tradeRepository = tradeRepository;
    }

    @Override
    public Trade executeTrade(Long userId, String tradePair, String tradeType, BigDecimal amount, BigDecimal price) {
        // Fetch the user and validate
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        // Fetch the wallet for the crypto symbol (e.g., BTC, ETH)
        String cryptoSymbol = tradePair.substring(0, tradePair.length() - 4); // Extract crypto symbol (e.g., "BTC" from "BTCUSDT")
        Wallet wallet = walletRepository.findByUserIdAndCryptoSymbol(userId, cryptoSymbol)
                .orElseThrow(() -> new RuntimeException("Wallet not found for " + cryptoSymbol));

        // Logic for "BUY" trade type
        if ("BUY".equalsIgnoreCase(tradeType)) {
            BigDecimal totalCost = amount.multiply(price);
            if (user.getUsdtBalance().compareTo(totalCost) < 0) {
                throw new RuntimeException("Insufficient USDT balance");
            }
            // Deduct USDT balance from the user
            user.setUsdtBalance(user.getUsdtBalance().subtract(totalCost));

            // Update the user's wallet with the new crypto balance (add to the balance)
            wallet.setBalance(wallet.getBalance().add(amount));

        } else if ("SELL".equalsIgnoreCase(tradeType)) {
            BigDecimal totalValue = amount.multiply(price);
            if (wallet.getBalance().compareTo(amount) < 0) {
                throw new RuntimeException("Insufficient " + cryptoSymbol + " balance");
            }
            // Deduct the crypto balance from the wallet
            wallet.setBalance(wallet.getBalance().subtract(amount));

            // Add the equivalent USDT to the user's balance
            user.setUsdtBalance(user.getUsdtBalance().add(totalValue));
        } else {
            throw new RuntimeException("Invalid trade type");
        }

        // Save the updated user and wallet details
        userRepository.save(user);
        walletRepository.save(wallet);

        // Create and save the trade record
        Trade trade = new Trade();
        trade.setUserId(userId);
        trade.setTradePair(tradePair);
        trade.setTradeType(tradeType);
        trade.setTradeAmount(amount);
        trade.setTradePrice(price);
        trade.setTradeTimestamp(LocalDateTime.now());

        return tradeRepository.save(trade);
    }
}
