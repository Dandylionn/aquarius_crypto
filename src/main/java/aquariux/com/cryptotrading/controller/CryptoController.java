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
        return userService.save(user);
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
    ResponseEntity<Trade> executeTrade(@RequestBody Trade trade){
        if (trade == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        Long userId = trade.getUserId();
        String tradePair = trade.getTradePair();
        String tradeType = trade.getTradeType();
        BigDecimal amount = trade.getTradeAmount();

        // Fetch the user (will throw RuntimeException if user is not found)
        User user = userService.findById(userId);

        // Fetch the latest price for the given trade pair
        Price latestPrice = priceService.getLatestPriceForPair(tradePair);

        BigDecimal price = BigDecimal.ZERO;

        // Choose the best price for the trade
        if ("BUY".equalsIgnoreCase(tradeType)) {
            price = latestPrice.getAskPrice(); // For buying, we use the ask price
        } else if ("SELL".equalsIgnoreCase(tradeType)) {
            price = latestPrice.getBidPrice(); // For selling, we use the bid price
        } else {
            throw new RuntimeException("Invalid trade type");
        }

        // Check if user has enough balance for the trade (e.g., if they are buying)
        if ("BUY".equalsIgnoreCase(tradeType) && user.getUsdtBalance().compareTo(price.multiply(amount)) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // Deduct balance if the trade is a buy
        if ("BUY".equalsIgnoreCase(tradeType)) {
            user.setUsdtBalance(user.getUsdtBalance().subtract(price.multiply(amount)));
            userService.updateUserBalance(user.getId(), user.getUsdtBalance()); // Update the user's balance after the trade
        }

        // Execute the trade and return the trade object
//        return tradingService.executeTrade(userId, tradePair, tradeType, amount, price);
        System.out.println("Trade details: " + trade);
        System.out.println("Latest price for " + tradePair + ": " + latestPrice);
        return ResponseEntity.ok(trade);
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
