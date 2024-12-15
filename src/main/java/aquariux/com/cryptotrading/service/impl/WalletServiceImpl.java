package aquariux.com.cryptotrading.service.impl;

import aquariux.com.cryptotrading.entity.Wallet;
import aquariux.com.cryptotrading.repository.WalletRepository;
import aquariux.com.cryptotrading.service.WalletService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;

    public WalletServiceImpl(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Override
    public Wallet getWalletByUserId(Long userId, String cryptoSymbol) {
        return walletRepository.findByUserIdAndCryptoSymbol(userId, cryptoSymbol)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
    }

    @Override
    public void updateWalletBalance(Long userId, String cryptoSymbol, BigDecimal newBalance) {
        Wallet wallet = getWalletByUserId(userId, cryptoSymbol);
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);
    }

    @Override
    public Wallet createWallet(Long userId, String cryptoSymbol, BigDecimal initialBalance) {
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setCryptoSymbol(cryptoSymbol);
        wallet.setBalance(initialBalance);
        return walletRepository.save(wallet);
    }
}
