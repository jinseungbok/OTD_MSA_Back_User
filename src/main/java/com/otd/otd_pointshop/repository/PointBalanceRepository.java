package com.otd.otd_pointshop.repository;

import com.otd.otd_pointshop.entity.PointBalance;
import com.otd.otd_user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointBalanceRepository extends JpaRepository<PointBalance, Long> {
    Optional<PointBalance> findByUser(User user);
}
