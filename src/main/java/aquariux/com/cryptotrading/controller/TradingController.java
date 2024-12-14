package aquariux.com.cryptotrading.controller;

import aquariux.com.cryptotrading.entity.Price;
import aquariux.com.cryptotrading.entity.Trade;
import aquariux.com.cryptotrading.service.PriceService;
import aquariux.com.cryptotrading.service.TradingService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api")
public class TradingController {

    private final PriceService priceService;
    private final TradingService tradingService;

    public TradingController(PriceService priceService, TradingService tradingService) {
        this.priceService = priceService;
        this.tradingService = tradingService;
    }

    @GetMapping("/prices")
    public Price getLatestAggregatedPrice() {
        // Fetch the latest aggregated price from the database
        return priceService.getLatestPrices().stream().findFirst().orElse(null);
    }

    @PostMapping("/trade")
    public Trade executeTrade(
            @RequestParam Long userId,
            @RequestParam String tradePair,
            @RequestParam String tradeType,
            @RequestParam BigDecimal amount,
            @RequestParam BigDecimal price) {
        return tradingService.executeTrade(userId, tradePair, tradeType, amount, price);
    }

    // Additional endpoints for wallet and trade history can go here
}
