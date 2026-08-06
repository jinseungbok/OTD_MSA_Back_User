package com.otd.otd_pointshop.application.purchase;

import com.otd.otd_pointshop.application.purchase.model.PurchaseHistoryRes;
import com.otd.otd_pointshop.application.purchase.model.PurchasePostRes;
import com.otd.otd_pointshop.entity.Point;
import com.otd.otd_pointshop.entity.PointImage;
import com.otd.otd_pointshop.entity.PurchaseHistory;
import com.otd.otd_pointshop.entity.PointHistory;
import com.otd.otd_pointshop.repository.PointRepository;
import com.otd.otd_pointshop.repository.PurchaseHistoryRepository;
import com.otd.otd_pointshop.repository.PointHistoryRepository;
import com.otd.otd_user.application.user.UserRepository;
import com.otd.otd_user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseHistoryService {

    private final PurchaseHistoryRepository purchaseHistoryRepository;
    private final PointRepository pointRepository;
    private final UserRepository userRepository;
    private final PointHistoryRepository pointHistoryRepository;

    // [POST] 포인트 상품 구매
    @Transactional
    public PurchasePostRes purchaseItem(Long userId, Long pointId) {
        // 사용자 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 포인트 아이템 검증
        Point point = pointRepository.findById(pointId)
                .orElseThrow(() -> new EntityNotFoundException("요청한 포인트 아이템이 존재하지 않습니다."));

        int balance = user.getPoint();
        int price = point.getPointScore();

        // 포인트 잔액 확인
        if (balance < price) {
            throw new IllegalArgumentException(
                    String.format("포인트가 부족합니다. (잔액: %dP, 필요: %dP)", balance, price)
            );
        }

        // 포인트 차감
        user.setPoint(balance - price);
        userRepository.save(user);

        // 구매 이력 저장
        PurchaseHistory history = new PurchaseHistory();
        history.setUser(user);
        history.setPoint(point);
        history.setPurchaseAt(LocalDateTime.now());
        purchaseHistoryRepository.save(history);

        // 포인트 내역 기록
        PointHistory pointHistory = new PointHistory();
        pointHistory.setUser(user);
        pointHistory.setChangePoint(-price); // 음수 기록
        pointHistory.setDescription(point.getPointItemName());
        pointHistory.setCreatedAt(LocalDateTime.now());
        pointHistoryRepository.save(pointHistory);

        log.info("[구매 완료] userId={}, pointId={}, itemName={}, 차감={}, 잔액={}",
                userId, pointId, point.getPointItemName(), price, user.getPoint());

        // 이미지 URL 처리
        String imageUrl = null;
        List<PointImage> images = point.getPointItemImages();
        if (images != null && !images.isEmpty()) {
            imageUrl = images.get(0).getImageUrl(); // 첫 번째 이미지만 사용
        }

        // 정확히 방금 구매한 상품 정보 반환
        return PurchasePostRes.builder()
                .purchaseId(history.getPurchaseId())
                .pointId(point.getPointId())
                .pointItemName(point.getPointItemName())
                .pointScore(point.getPointScore())
                .pointItemImage(imageUrl)
                .purchaseAt(history.getPurchaseAt())
                .build();
    }

    // [POST] 포인트 사용
    @Transactional
    public PurchasePostRes usePoint(Long userId, int usedAmount, String usageType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        int balance = user.getPoint();
        if (balance < usedAmount) {
            throw new IllegalArgumentException(
                    String.format("포인트가 부족합니다. (잔액: %dP, 필요: %dP)", balance, usedAmount)
            );
        }

        user.setPoint(balance - usedAmount);
        userRepository.save(user);

        PurchaseHistory history = new PurchaseHistory();
        history.setUser(user);
        history.setUsageType(usageType != null ? usageType : "GENERAL_USE");
        history.setPurchaseAt(LocalDateTime.now());
        purchaseHistoryRepository.save(history);

        PointHistory pointHistory = new PointHistory();
        pointHistory.setUser(user);
        pointHistory.setChangePoint(-usedAmount);
        pointHistory.setDescription("포인트 사용 (" + (usageType != null ? usageType : "기타") + ")");
        pointHistory.setCreatedAt(LocalDateTime.now());
        pointHistoryRepository.save(pointHistory);

        log.info("[포인트 사용 완료] userId={}, used={}, usageType={}, remain={}",
                userId, usedAmount, usageType, user.getPoint());

        return PurchasePostRes.builder()
                .purchaseId(history.getPurchaseId())
                .pointItemName("포인트 사용 (" + (usageType != null ? usageType : "기타") + ")")
                .pointScore(usedAmount)
                .purchaseAt(history.getPurchaseAt())
                .build();
    }

    // [GET] (관리자) 전체 구매 이력 조회
    public List<PurchaseHistoryRes> getAllHistories() {
        return purchaseHistoryRepository.findAll().stream()
                .map(PurchaseHistoryRes::fromEntity)
                .toList();
    }

    // [GET] (사용자) 개인 구매 이력 조회
    public List<PurchaseHistoryRes> getUserPurchaseHistory(Long userId) {
        return purchaseHistoryRepository.findPurchaseHistoryWithImageByUserId(userId);
    }

    // [GET] memberId 기반 중복 제거
    public List<PurchaseHistoryRes> getHistory(Long userId) {
        return getUserPurchaseHistory(userId);
    }

    // [GET] controller method
    public List<PurchaseHistoryRes> getPurchaseHistoryByUser(Long userId) {
        return getUserPurchaseHistory(userId);
    }
}