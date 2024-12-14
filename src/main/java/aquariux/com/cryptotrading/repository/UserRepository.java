package aquariux.com.cryptotrading.repository;

import aquariux.com.cryptotrading.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByUsdtBalanceGreaterThan(BigDecimal balance);

}
