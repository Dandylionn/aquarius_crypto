package aquariux.com.cryptotrading.service;

import aquariux.com.cryptotrading.entity.Price;
import java.util.List;

public interface PriceService {
    void fetchAndSaveLatestPrices();
    List<Price> getLatestPrices();
}
