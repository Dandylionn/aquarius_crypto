package aquariux.com.cryptotrading.service.impl;

import aquariux.com.cryptotrading.entity.Trade;
import aquariux.com.cryptotrading.entity.User;
import aquariux.com.cryptotrading.entity.Wallet;
import aquariux.com.cryptotrading.repository.TradeRepository;
import aquariux.com.cryptotrading.repository.UserRepository;
import aquariux.com.cryptotrading.repository.WalletRepository;
import aquariux.com.cryptotrading.service.TradingService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
    @Transactional
    public void saveWallet(Wallet wallet) {
        walletRepository.save(wallet);
    }

    @Override
    public Trade executeTrade(Long userId, String tradePair, String tradeType, BigDecimal amount, BigDecimal price) {
        User user = validateUser(userId);
        Wallet wallet = validateWallet(userId, extractCryptoSymbol(tradePair));

        if ("BUY".equalsIgnoreCase(tradeType)) {
            processBuyTrade(user, wallet, amount, price);
        } else if ("SELL".equalsIgnoreCase(tradeType)) {
            processSellTrade(user, wallet, amount, price);
        } else {
            throw new RuntimeException("Invalid trade type. Allowed values are 'BUY' or 'SELL'.");
        }

        return saveTradeRecord(userId, tradePair, tradeType, amount, price);
    }

    private void processBuyTrade(User user, Wallet wallet, BigDecimal amount, BigDecimal price) {
        BigDecimal totalCost = amount.multiply(price);
        if (user.getUsdtBalance().compareTo(totalCost) < 0) {
            throw new RuntimeException("Insufficient USDT balance.");
        }
        user.setUsdtBalance(user.getUsdtBalance().subtract(totalCost));
        wallet.setBalance(wallet.getBalance().add(amount));
        saveUserAsync(user);
        saveWalletAsync(wallet);
    }

    private void processSellTrade(User user, Wallet wallet, BigDecimal amount, BigDecimal price) {
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient crypto balance.");
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
        user.setUsdtBalance(user.getUsdtBalance().add(amount.multiply(price)));
        saveWalletAsync(wallet);
        saveUserAsync(user);
    }

    private Trade saveTradeRecord(Long userId, String tradePair, String tradeType, BigDecimal amount, BigDecimal price) {
        Trade trade = new Trade();
        trade.setUserId(userId);
        trade.setTradePair(tradePair);
        trade.setTradeType(tradeType);
        trade.setTradeAmount(amount);
        trade.setTradePrice(price);
        trade.setTradeTimestamp(LocalDateTime.now());
        return tradeRepository.save(trade);
    }

    private User validateUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));
    }

    private Wallet validateWallet(Long userId, String cryptoSymbol) {
        return walletRepository.findByUserIdAndCryptoSymbol(userId, cryptoSymbol)
                .orElseThrow(() -> new RuntimeException("Wallet not found for crypto symbol: " + cryptoSymbol));
    }

    private String extractCryptoSymbol(String tradePair) {
        return tradePair.replace("USDT", "");
    }


    // async to save user
    @Async
    public CompletableFuture<Void> saveUserAsync(User user) {
        userRepository.save(user);
        return CompletableFuture.completedFuture(null);
    }

    // async to save wallet
    @Async
    public CompletableFuture<Void> saveWalletAsync(Wallet wallet) {
        walletRepository.save(wallet);
        return CompletableFuture.completedFuture(null);
    }

    // async to save trades
    @Async
    public CompletableFuture<Void> saveTradeAsync(Trade trade) {
        tradeRepository.save(trade);
        return CompletableFuture.completedFuture(null);
    }

    public List<Wallet> getWalletsByUserId(Long userId) {
        return walletRepository.findByUserId(userId);
    }

    public List<Trade> getTradeHistory(Long userId) {
        return tradeRepository.findByUserId(userId);
    }
}
