package com.otd.otd_pointshop.application.point;

import com.otd.otd_pointshop.entity.PointUser;
import com.otd.otd_pointshop.repository.PointUserRepository;
import com.otd.otd_user.application.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointUserService {

    private final PointUserRepository pointUserRepository;
    private final UserRepository userRepository;

    // 유저 포인트 내역 조회
    @Transactional
    public List<PointUser> getUserPointHistory(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        return pointUserRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);
    }

    public int getPointBalance(Long userId) {
        Integer balance = userRepository.findPointByUserId(userId);
        if (balance == null) {
            throw new RuntimeException("사용자 정보를 찾을 수 없습니다.");
        }
        return balance;
    }

    // 포인트 충전
    @Transactional
    public void chargePoint(Long userId, int amount) {
        userRepository.updatePointByUserId(amount, userId);
    }

    // 포인트 차감
    @Transactional
    public void deductPoint(Long userId, int amount) {

        // 유저의 포인트 조회
        int current = getPointBalance(userId);
        if (current < amount) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }
        userRepository.updatePointByUserId(-amount, userId);
    }
}