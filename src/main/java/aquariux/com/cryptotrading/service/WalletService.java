package aquariux.com.cryptotrading.service;

import aquariux.com.cryptotrading.entity.Wallet;

import java.math.BigDecimal;

public interface WalletService {

    Wallet getWalletByUserId(Long userId, String cryptoSymbol);

    void updateWalletBalance(Long userId, String cryptoSymbol, BigDecimal newBalance);

    Wallet createWallet(Long userId, String cryptoSymbol, BigDecimal initialBalance);
}
