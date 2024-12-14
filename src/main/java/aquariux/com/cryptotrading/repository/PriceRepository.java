package aquariux.com.cryptotrading.repository;

import aquariux.com.cryptotrading.entity.Price;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PriceRepository extends JpaRepository<Price, Long> {
    Optional<Price> findTopByTradePairOrderByTimestampDesc(String tradePair);
    List<Price> findAll();
}