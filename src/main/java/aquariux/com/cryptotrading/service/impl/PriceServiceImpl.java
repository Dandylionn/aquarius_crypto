package aquariux.com.cryptotrading.service.impl;

import aquariux.com.cryptotrading.entity.Price;
import aquariux.com.cryptotrading.repository.PriceRepository;
import aquariux.com.cryptotrading.service.PriceService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PriceServiceImpl implements PriceService {

    private final PriceRepository priceRepository;
    private final RestTemplate restTemplate;

    public PriceServiceImpl(PriceRepository priceRepository, RestTemplate restTemplate) {
        this.priceRepository = priceRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    @Scheduled(fixedRate = 10000) // 10 seconds
    public void fetchAndSaveLatestPrices() {
        // Fetch prices from Binance
        String binanceUrl = "https://api.binance.com/api/v3/ticker/bookTicker";
        BinancePrice binancePrice = restTemplate.getForObject(binanceUrl, BinancePrice.class);

        // Fetch prices from Huobi
        String huobiUrl = "https://api.huobi.pro/market/tickers";
        HuobiPrice huobiPrice = restTemplate.getForObject(huobiUrl, HuobiPrice.class);

        // Determine the best bid (SELL) and ask (BUY) prices
        Price aggregatedPrice = calculateBestPrice(binancePrice, huobiPrice);

        // Save to the database
        priceRepository.save(aggregatedPrice);
    }

    @Override
    public List<Price> getLatestPrices() {
        return priceRepository.findAll();
    }

    private Price calculateBestPrice(BinancePrice binancePrice, HuobiPrice huobiPrice) {
        BigDecimal bestBidPrice = binancePrice.getBidPrice().max(huobiPrice.getBidPrice());
        BigDecimal bestAskPrice = binancePrice.getAskPrice().min(huobiPrice.getAskPrice());

        Price price = new Price();
        price.setTradePair("BTCUSDT"); // or "ETHUSDT" based on implementation
        price.setBidPrice(bestBidPrice);
        price.setAskPrice(bestAskPrice);
        price.setTimestamp(LocalDateTime.now());
        return price;
    }

    // Classes to map API responses
    public static class BinancePrice {
        private BigDecimal bidPrice;
        private BigDecimal askPrice;

        // Getters and setters
        public BigDecimal getBidPrice() {
            return bidPrice;
        }

        public void setBidPrice(BigDecimal bidPrice) {
            this.bidPrice = bidPrice;
        }

        public BigDecimal getAskPrice() {
            return askPrice;
        }

        public void setAskPrice(BigDecimal askPrice) {
            this.askPrice = askPrice;
        }
    }

    public static class HuobiPrice {
        private BigDecimal bidPrice;
        private BigDecimal askPrice;

        // Getters and setters
        public BigDecimal getBidPrice() {
            return bidPrice;
        }

        public void setBidPrice(BigDecimal bidPrice) {
            this.bidPrice = bidPrice;
        }

        public BigDecimal getAskPrice() {
            return askPrice;
        }

        public void setAskPrice(BigDecimal askPrice) {
            this.askPrice = askPrice;
        }
    }
}
