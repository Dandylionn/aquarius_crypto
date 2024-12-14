package aquariux.com.cryptotrading.service.impl;

import aquariux.com.cryptotrading.entity.Price;
import aquariux.com.cryptotrading.repository.PriceRepository;
import aquariux.com.cryptotrading.service.PriceService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
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
        try {
            // Trading pairs to support
            List<String> tradingPairs = List.of("BTCUSDT", "ETHUSDT");

            // Fetch prices from Binance
            String binanceUrl = "https://api.binance.com/api/v3/ticker/bookTicker";
            List<BinancePrice> binancePrices = restTemplate.exchange(
                    binanceUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<BinancePrice>>() {}
            ).getBody();

            // Fetch prices from Huobi
            String huobiUrl = "https://api.huobi.pro/market/tickers";
            HuobiPriceResponse huobiResponse = restTemplate.getForObject(huobiUrl, HuobiPriceResponse.class);

            for (String pair : tradingPairs) {
                // Find the relevant ticker for the trading pair in both APIs
                BinancePrice binancePrice = binancePrices.stream()
                        .filter(price -> price.getSymbol().equalsIgnoreCase(pair))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException(pair + " ticker not found in Binance API"));

                HuobiPrice huobiPrice = huobiResponse.getData().stream()
                        .filter(ticker -> ticker.getSymbol().equalsIgnoreCase(pair.toLowerCase())) // Adjust for case sensitivity
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException(pair + " ticker not found in Huobi API"));

                // Calculate the best aggregated price
                Price aggregatedPrice = calculateBestPrice(binancePrice, huobiPrice, pair);

                // Save to the database
                priceRepository.save(aggregatedPrice);
                // Log selected prices
//                System.out.println("Binance BTCUSDT Price: " + binancePrice);
//                System.out.println("Huobi BTCUSDT Price: " + huobiPrice);
                // Log for debugging
                System.out.println("Aggregated price for " + pair + ": " + aggregatedPrice);
            }
        } catch (Exception e) {
            // Log the error for debugging purposes
            System.err.println("Error fetching and saving prices: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Price calculateBestPrice(BinancePrice binancePrice, HuobiPrice huobiPrice, String tradingPair) {
        BigDecimal bestBidPrice = (binancePrice.getBidPrice() != null && huobiPrice.getBid() != null)
                ? binancePrice.getBidPrice().max(huobiPrice.getBid())
                : (binancePrice.getBidPrice() != null ? binancePrice.getBidPrice() : huobiPrice.getBid());

        BigDecimal bestAskPrice = (binancePrice.getAskPrice() != null && huobiPrice.getAsk() != null)
                ? binancePrice.getAskPrice().min(huobiPrice.getAsk())
                : (binancePrice.getAskPrice() != null ? binancePrice.getAskPrice() : huobiPrice.getAsk());

        if (bestBidPrice == null || bestAskPrice == null) {
            throw new RuntimeException("Bid or Ask price is null, cannot calculate aggregated price for " + tradingPair);
        }

        // Create and return the aggregated price
        Price price = new Price();
        price.setTradePair(tradingPair); // Set trading pair (BTCUSDT or ETHUSDT)
        price.setBidPrice(bestBidPrice);
        price.setAskPrice(bestAskPrice);
        price.setTimestamp(LocalDateTime.now());
        return price;
    }


    @Override
    public List<Price> getLatestPrices() {
        return priceRepository.findAll();
    }

    private BigDecimal getMaxValue(BigDecimal value1, BigDecimal value2) {
        if (value1 == null) return value2;
        if (value2 == null) return value1;
        return value1.max(value2);
    }

    private BigDecimal getMinValue(BigDecimal value1, BigDecimal value2) {
        if (value1 == null) return value2;
        if (value2 == null) return value1;
        return value1.min(value2);
    }

    // Classes for Binance API
    public static class BinancePrice {
        private String symbol;
        private BigDecimal bidPrice;
        private BigDecimal askPrice;

        // Getters and setters
        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public BigDecimal getBidPrice() {
            return bidPrice;
        }

        public void setBidPrice(String bidPrice) {
            this.bidPrice = new BigDecimal(bidPrice);
        }

        public BigDecimal getAskPrice() {
            return askPrice;
        }

        public void setAskPrice(String askPrice) {
            this.askPrice = new BigDecimal(askPrice);
        }

        @Override
        public String toString() {
            return "BinancePrice{" +
                    "symbol='" + symbol + '\'' +
                    ", bidPrice=" + bidPrice +
                    ", askPrice=" + askPrice +
                    '}';
        }
    }

    // Classes for Huobi API
    public static class HuobiPrice {
        private String symbol;
        private BigDecimal bid;
        private BigDecimal ask;

        // Getters and setters
        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public BigDecimal getBid() {
            return bid;
        }

        public void setBid(BigDecimal bid) {
            this.bid = bid;
        }

        public BigDecimal getAsk() {
            return ask;
        }

        public void setAsk(BigDecimal ask) {
            this.ask = ask;
        }

        @Override
        public String toString() {
            return "HuobiPrice{" +
                    "symbol='" + symbol + '\'' +
                    ", bid=" + bid +
                    ", ask=" + ask +
                    '}';
        }
    }

    public static class HuobiPriceResponse {
        private List<HuobiPrice> data;

        // Getters and setters
        public List<HuobiPrice> getData() {
            return data;
        }

        public void setData(List<HuobiPrice> data) {
            this.data = data;
        }
    }
}



