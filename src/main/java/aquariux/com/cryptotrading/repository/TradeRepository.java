package aquariux.com.cryptotrading.repository;

import aquariux.com.cryptotrading.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findByUserId(Long userId);
    List<Trade> findByTradeType(String tradeType);

}
