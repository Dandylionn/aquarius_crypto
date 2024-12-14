package aquariux.com.cryptotrading.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Price {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tradePair; // BTCUSDT, ETHUSDT
    private BigDecimal bidPrice;
    private BigDecimal askPrice;
    private LocalDateTime timestamp;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTradePair() {
        return tradePair;
    }

    public void setTradePair(String tradePair) {
        this.tradePair = tradePair;
    }

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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    @Override
    public String toString() {
        return "Price{" +
                "tradePair='" + tradePair + '\'' +
                ", bidPrice=" + bidPrice +
                ", askPrice=" + askPrice +
                ", timestamp=" + timestamp +
                '}';
    }
}
