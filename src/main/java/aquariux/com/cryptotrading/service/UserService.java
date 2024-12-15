package aquariux.com.cryptotrading.service;

import aquariux.com.cryptotrading.entity.User;

import java.math.BigDecimal;

public interface UserService {
    User findById(Long userId);
    User updateUserBalance(Long userId, BigDecimal newBalance);
    User save(User user);
}