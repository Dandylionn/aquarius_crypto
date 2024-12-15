package aquariux.com.cryptotrading.repository;

import aquariux.com.cryptotrading.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByUserIdAndCryptoSymbol(Long userId, String cryptoSymbol);
    List<Wallet> findByUserId(Long userId);  // fetch all wallets for a user
}
