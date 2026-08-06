package com.otd.otd_pointshop.application.purchase;

import com.otd.configuration.model.UserPrincipal;
import com.otd.otd_pointshop.application.point.model.PointApiResponse;
import com.otd.otd_pointshop.application.purchase.model.PurchaseHistoryRes;
import com.otd.otd_pointshop.application.purchase.model.PurchasePostRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/OTD/pointshop/purchase")
public class PurchaseHistoryController {

    private final PurchaseHistoryService purchaseHistoryService;

    // [POST] 아이템 구매
    @PostMapping("/{pointId}")
    public ResponseEntity<PointApiResponse<PurchasePostRes>> purchaseItem(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long pointId
    ) {

        if (user == null) {
            log.warn("[구매 요청 거부] 로그인 필요 - pointId={}", pointId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new PointApiResponse<>(false, "로그인이 필요합니다."));
        }

        Long userId = user.getSignedUserId();
        log.info("[구매 요청] userId={}, pointId={}", userId, pointId);

        try {
            PurchasePostRes result = purchaseHistoryService.purchaseItem(userId, pointId);
            return ResponseEntity.ok(new PointApiResponse<>(true, "구매 완료", result));

        } catch (EntityNotFoundException e) {
            log.warn("[구매 실패 - NotFound] userId={}, pointId={}, msg={}", userId, pointId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new PointApiResponse<>(false, e.getMessage()));

        } catch (IllegalArgumentException e) {
            log.warn("[구매 실패 - BadRequest] userId={}, pointId={}, msg={}", userId, pointId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new PointApiResponse<>(false, e.getMessage()));

        } catch (Exception e) {
            log.error("[구매 실패 - ServerError] userId={}, pointId={}, error={}", userId, pointId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new PointApiResponse<>(false, "서버 오류로 구매를 진행할 수 없습니다."));
        }
    }

    // [POST] 포인트 사용 (일반 차감용)
    @PostMapping("/use")
    public ResponseEntity<PointApiResponse<PurchasePostRes>> usePoint(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam int usedAmount,
            @RequestParam(required = false, defaultValue = "GENERAL_USE") String usageType
    ) {
        if (user == null) {
            log.warn("[포인트 사용 거부] 로그인 필요");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new PointApiResponse<>(false, "로그인이 필요합니다."));
        }

        Long userId = user.getSignedUserId();
        log.info("[포인트 사용 요청] userId={}, usedAmount={}, usageType={}", userId, usedAmount, usageType);

        try {
            PurchasePostRes result = purchaseHistoryService.usePoint(userId, usedAmount, usageType);
            return ResponseEntity.ok(new PointApiResponse<>(true, "포인트 사용 완료", result));

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new PointApiResponse<>(false, e.getMessage()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new PointApiResponse<>(false, e.getMessage()));

        } catch (Exception e) {
            log.error("[포인트 사용 실패] userId={}, usedAmount={}, error={}", userId, usedAmount, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new PointApiResponse<>(false, "서버 오류로 포인트 사용을 진행할 수 없습니다."));
        }
    }

    // [GET] 구매 이력 조회 (사용자)
    @GetMapping("/history/user")
    public ResponseEntity<PointApiResponse<List<PurchaseHistoryRes>>> getUserPurchaseHistory(
            @AuthenticationPrincipal UserPrincipal user
    ) {
        if (user == null) {
            log.warn("[이력 조회 거부] 로그인 필요");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new PointApiResponse<>(false, "로그인이 필요합니다."));
        }

        Long userId = user.getSignedUserId();
        log.info("[이력 조회 요청] userId={}", userId);

        List<PurchaseHistoryRes> history = purchaseHistoryService.getUserPurchaseHistory(userId);
        return ResponseEntity.ok(new PointApiResponse<>(true, "구매 이력 조회 성공", history));
    }
}