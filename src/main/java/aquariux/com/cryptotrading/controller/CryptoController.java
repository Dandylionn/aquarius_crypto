package aquariux.com.cryptotrading.controller;

import aquariux.com.cryptotrading.entity.Price;
import aquariux.com.cryptotrading.entity.Trade;
import aquariux.com.cryptotrading.entity.Wallet;
import aquariux.com.cryptotrading.service.PriceService;
import aquariux.com.cryptotrading.service.TradingService;
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

    public CryptoController(PriceService priceService, TradingService tradingService) {
        this.priceService = priceService;
        this.tradingService = tradingService;
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
    public Trade executeTrade(
            @RequestParam Long userId,
            @RequestParam String tradePair,
            @RequestParam String tradeType,
            @RequestParam BigDecimal amount,
            @RequestParam BigDecimal price) {
        return tradingService.executeTrade(userId, tradePair, tradeType, amount, price);
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
