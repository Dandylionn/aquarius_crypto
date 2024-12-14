package aquariux.com.cryptotrading.service.impl;

import aquariux.com.cryptotrading.entity.User;
import aquariux.com.cryptotrading.repository.UserRepository;
import aquariux.com.cryptotrading.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    // Constructor-based injection for UserRepository
    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Find user by ID
    @Override
    public User findById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Update user's USDT balance
    @Override
    public User updateUserBalance(Long userId, BigDecimal newBalance) {
        User user = findById(userId);
        user.setUsdtBalance(newBalance);  // Set the new balance
        return userRepository.save(user);  // Save the updated user back to the database
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }
}
