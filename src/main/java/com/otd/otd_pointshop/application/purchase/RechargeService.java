package com.otd.otd_pointshop.application.purchase;

import com.otd.otd_admin.application.admin.Repository.AdminUserRepository;
import com.otd.otd_pointshop.application.stats.model.MonthlyRechargeStatsRes;
import com.otd.otd_pointshop.application.purchase.model.RechargeGetRes;
import com.otd.otd_pointshop.application.purchase.model.RechargePostReq;
import com.otd.otd_pointshop.application.purchase.model.RechargePostRes;
import com.otd.otd_pointshop.entity.RechargeHistory;
import com.otd.otd_pointshop.repository.RechargeHistoryRepository;
import com.otd.otd_user.application.user.UserRepository;
import com.otd.otd_user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RechargeService {

    private final RechargeHistoryRepository rechargeHistoryRepository;
    private final UserRepository userRepository;
    private final AdminUserRepository adminUserRepository;

    // (관리자) 특정 유저 포인트 충전
    @Transactional
    public RechargePostRes rechargePoint(Long userId, RechargePostReq req) {
        // 관리자 유효성 검사
        if (req.getAdminId() == null || !adminUserRepository.existsById(req.getAdminId().intValue())) {
            throw new EntityNotFoundException("유효하지 않은 관리자 ID입니다.");
        }

        // 충전 금액 검증
        if (req.getAmount() == null || req.getAmount() <= 0) {
            throw new IllegalArgumentException("충전 포인트는 1 이상이어야 합니다.");
        }

        // 유저 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));

        // 포인트 충전 처리
        int newBalance = user.getPoint() + req.getAmount();
        user.setPoint(newBalance);
        userRepository.save(user);

        // 충전 이력 기록
        RechargeHistory history = RechargeHistory.builder()
                .adminId(req.getAdminId())
                .user(user)
                .amount(req.getAmount())
                .rechargeAt(LocalDateTime.now())
                .build();
        rechargeHistoryRepository.save(history);

        log.info("[관리자 포인트 충전 완료] adminId={}, userId={}, userName={}, amount={}, balance={}",
                req.getAdminId(), userId, user.getName(), req.getAmount(), newBalance);

        // 응답 DTO 생성
        return RechargePostRes.builder()
                .rechargeId(history.getRechargeId())
                .adminId(req.getAdminId())
                .userId(user.getUserId())
                .name(user.getName())
                .amount(req.getAmount())
                .balance(newBalance)
                .rechargeAt(history.getRechargeAt())
                .build();
    }

    // (관리자) 전체 충전 이력 조회
    public List<RechargeGetRes> getRechargeHistories() {
        return rechargeHistoryRepository.findAll().stream()
                .map(RechargeGetRes::fromEntity)
                .collect(Collectors.toList());
    }

    // (관리자/사용자 공용) 특정 유저 충전 이력 조회
    public List<RechargePostRes> getRechargeHistoryByUserId(Long userId) {
        return rechargeHistoryRepository.findByUser_UserId(userId).stream()
                .map(RechargePostRes::fromEntity)
                .collect(Collectors.toList());
    }

    // (관리자/사용자 공용) 포인트 잔액 조회
    public Integer getBalance(Long userId) {
        return userRepository.findById(userId)
                .map(User::getPoint)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
    }

    // 사용자 전용
    public Integer getBalanceByUserId(Long userId) {
        return getBalance(userId);
    }

    // 관리자 월별 충전 통계
    public List<MonthlyRechargeStatsRes> getMonthlyRechargeStatsByAdmin(Long adminId) {
        List<Object[]> raw = rechargeHistoryRepository.findMonthlyRechargeStatsByAdmin(adminId);
        return raw.stream()
                .map(obj -> new MonthlyRechargeStatsRes(
                        (String) obj[0],                   // month
                        ((Number) obj[1]).longValue()      // totalAmount
                ))
                .toList();
    }

    // (관리자) 총 충전 포인트 합계
    public Integer getTotalRechargeAmount() {
        Integer total = rechargeHistoryRepository.findTotalRechargeAmount();
        return total != null ? total : 0;
    }
}