package aquariux.com.cryptotrading.controller;

import aquariux.com.cryptotrading.entity.Price;
import aquariux.com.cryptotrading.entity.Trade;
import aquariux.com.cryptotrading.service.PriceService;
import aquariux.com.cryptotrading.service.TradingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
public class CryptoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PriceService priceService;

    @Mock
    private TradingService tradingService;

    @InjectMocks
    private CryptoController cryptoController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cryptoController).build();
    }

    @Test
    void testGetLatestAggregatedPrice() throws Exception {
        // Mock the service call
        Price price = new Price();
        price.setTradePair("BTCUSDT");
        price.setBidPrice(BigDecimal.valueOf(20000));
        price.setAskPrice(BigDecimal.valueOf(20500));
        price.setTimestamp(java.time.LocalDateTime.now());
        when(priceService.getLatestPrices()).thenReturn(List.of(price));

        // Perform GET request
        mockMvc.perform(get("/api/prices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tradePair").value("BTCUSDT"))
                .andExpect(jsonPath("$[0].bidPrice").value(20000))
                .andExpect(jsonPath("$[0].askPrice").value(20500));
    }

    @Test
    void testExecuteTrade() throws Exception {
        // Mock the service call
        Trade trade = new Trade();
        trade.setId(1L);
        trade.setUserId(1L);
        trade.setTradePair("BTCUSDT");
        trade.setTradeType("BUY");
        trade.setTradeAmount(BigDecimal.valueOf(0.5));
        trade.setTradePrice(BigDecimal.valueOf(20500));
        when(priceService.getLatestPriceForPair("BTCUSDT")).thenReturn(new Price());
        when(tradingService.executeTrade(1L, "BTCUSDT", "BUY", BigDecimal.valueOf(0.5), BigDecimal.valueOf(20500))).thenReturn(trade);

        // Perform POST request
        mockMvc.perform(post("/api/trade")
                        .param("userId", "1")
                        .param("tradePair", "BTCUSDT")
                        .param("tradeType", "BUY")
                        .param("amount", "0.5")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tradePair").value("BTCUSDT"))
                .andExpect(jsonPath("$.tradeType").value("BUY"))
                .andExpect(jsonPath("$.amount").value(0.5));
    }
}
