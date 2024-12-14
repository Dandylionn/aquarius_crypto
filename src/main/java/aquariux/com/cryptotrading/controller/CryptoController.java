package aquariux.com.cryptotrading.controller;

import aquariux.com.cryptotrading.entity.Price;
import aquariux.com.cryptotrading.entity.Trade;
import aquariux.com.cryptotrading.entity.Wallet;
import aquariux.com.cryptotrading.service.PriceService;
import aquariux.com.cryptotrading.service.TradingService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

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
    public Price getLatestAggregatedPrice() {
        return priceService.getLatestPrices().stream().findFirst().orElse(null);
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
