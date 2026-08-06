package com.otd.otd_pointshop.application.point;

import com.otd.otd_pointshop.entity.PointBalance;
import com.otd.otd_pointshop.repository.PointBalanceRepository;
import com.otd.otd_user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointBalanceService {
    private final PointBalanceRepository pointBalanceRepository;

    @Transactional
    public void createBalance(Long userId, int amount) {
        // UserRepository 없이 FK만 세팅
        User userRef = new User();
        userRef.setUserId(userId);

        PointBalance balance = new PointBalance();
        balance.setUser(userRef);
        balance.setPointBalance(amount);

        pointBalanceRepository.save(balance);
    }
}