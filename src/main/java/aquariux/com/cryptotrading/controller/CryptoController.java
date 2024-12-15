package aquariux.com.cryptotrading.controller;

import aquariux.com.cryptotrading.entity.Price;
import aquariux.com.cryptotrading.entity.Trade;
import aquariux.com.cryptotrading.entity.User;
import aquariux.com.cryptotrading.entity.Wallet;
import aquariux.com.cryptotrading.service.PriceService;
import aquariux.com.cryptotrading.service.TradingService;
import aquariux.com.cryptotrading.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class CryptoController {

    private final PriceService priceService;
    private final TradingService tradingService;
    private final UserService userService;

    public CryptoController(PriceService priceService, TradingService tradingService, UserService userService) {
        this.priceService = priceService;
        this.tradingService = tradingService;
        this.userService = userService;
    }
//    @PostMapping("/user")
//    public User createUser(@RequestParam String username) {
//        User newUser = new User();
//        newUser.setUsername(username);
//        // Set initial balance for the user
//        newUser.setUsdtBalance(BigDecimal.valueOf(50000.00)); // Initial balance
//        return userService.save(newUser);
//    }
    @PostMapping("/user")
    public User createUser(@RequestBody User user) {
        user.setUsdtBalance(BigDecimal.valueOf(50000.00)); // Set initial balance

        User savedUser = userService.save(user);

        List<String> supportedCryptos = List.of("BTC", "ETH");
        for (String crypto : supportedCryptos) {
            Wallet wallet = new Wallet();
            wallet.setUserId(savedUser.getId());
            wallet.setCryptoSymbol(crypto);
            wallet.setBalance(BigDecimal.ZERO); // Start with zero balance
            tradingService.saveWallet(wallet);
        }

        return savedUser;
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<User> getUserInfo(@PathVariable Long userId) {
        User user = userService.findById(userId);
        if (user != null) {
            return ResponseEntity.ok(user); // Return 200
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)  // Return 404
                    .body(null);
        }
    }

    // Endpoint to get the latest aggregated price for a trading pair
    @GetMapping("/prices")
    public List<Price> getLatestAggregatedPrice() {
        List<Price> allPrices = priceService.getLatestPrices();

        // Group by trade pair and find the latest price for each pair
        return allPrices.stream()
                .collect(Collectors.groupingBy(Price::getTradePair,
                        Collectors.maxBy(Comparator.comparing(Price::getTimestamp))))
                .values().stream()
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    // Endpoint to execute a trade (buy/sell)
    @PostMapping("/trade")
    public ResponseEntity<?> executeTrade(@RequestBody Trade trade) {
        if (trade == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid trade request.");
        }

        Long userId = trade.getUserId();
        String tradePair = trade.getTradePair();
        String tradeType = trade.getTradeType();
        BigDecimal amount = trade.getTradeAmount();

        try {
            // Fetch the user (return 404 if user is not found)
            User user = userService.findById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
            }

            // Fetch the latest price for the given trade pair
            Price latestPrice = priceService.getLatestPriceForPair(tradePair);
            if (latestPrice == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Trade pair not found.");
            }

            BigDecimal price;

            // Determine trade price based on type
            if ("BUY".equalsIgnoreCase(tradeType)) {
                price = latestPrice.getAskPrice(); // Buy at ask price
            } else if ("SELL".equalsIgnoreCase(tradeType)) {
                price = latestPrice.getBidPrice(); // Sell at bid price
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid trade type. Allowed values are 'BUY' or 'SELL'.");
            }

            // Check if user has sufficient balance
            BigDecimal totalCost = price.multiply(amount);
            if ("BUY".equalsIgnoreCase(tradeType) && user.getUsdtBalance().compareTo(totalCost) < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Insufficient balance. Required: " + totalCost + ", Available: " + user.getUsdtBalance());
            }

            // Deduct balance for buy trades
            if ("BUY".equalsIgnoreCase(tradeType)) {
                user.setUsdtBalance(user.getUsdtBalance().subtract(totalCost));
                userService.updateUserBalance(user.getId(), user.getUsdtBalance());
            }

            // Execute the trade
            Trade executedTrade = tradingService.executeTrade(userId, tradePair, tradeType, amount, price);

            // Return successful trade response
            return ResponseEntity.ok(executedTrade);

        } catch (Exception ex) {
            // Log unexpected errors for debugging
            ex.printStackTrace();

            // Return generic error message for server-side issues
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred. Please try again later.");
        }
    }

    // Endpoint to fetch wallet balances for a user
    @GetMapping("/wallet/{userId}")
    public List<Wallet> getWallet(@PathVariable Long userId) {
        return tradingService.getWalletsByUserId(userId);
    }

    // Endpoint to retrieve trade history for a user
    @GetMapping("/trades/{userId}")
    public List<Trade> getTradeHistory(@PathVariable Long userId) {
        return tradingService.getTradeHistory(userId);
    }
}
