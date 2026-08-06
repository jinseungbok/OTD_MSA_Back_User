package com.otd.otd_pointshop.application.purchase;

import com.otd.configuration.model.UserPrincipal;
import com.otd.otd_pointshop.application.point.model.PointApiResponse;
import com.otd.otd_pointshop.application.purchase.model.*;
import com.otd.otd_user.application.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/OTD/pointshop/recharge")
public class RechargeController {

    private final RechargeService rechargeService;
    private final UserRepository userRepository;

    // [POST] 관리자 포인트 충전
    @PostMapping("/{userId}")
    public ResponseEntity<?> rechargePoint(
            @AuthenticationPrincipal UserPrincipal admin,
            @PathVariable Long userId,
            @RequestBody RechargePostReq req
    ) {
        if (admin == null || !admin.hasRole("ADMIN"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new PointApiResponse<>(false, "관리자 권한이 필요합니다."));

        RechargePostRes res = rechargeService.rechargePoint(userId, req);
        log.info("[포인트 충전] admin={}, userId={}, amount={}",
                admin.getSignedUserId(), userId, req.getAmount());
        return ResponseEntity.ok(new PointApiResponse<>(true, "포인트 충전 성공", res, null));
    }

    // [GET] 전체 충전 내역 (관리자)
    @GetMapping("/admin/history")
    public ResponseEntity<?> getAllRechargeHistory(@AuthenticationPrincipal UserPrincipal admin) {
        if (admin == null || !admin.hasRole("ADMIN"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new PointApiResponse<>(false, "관리자 권한이 필요합니다."));

        List<RechargeGetRes> list = rechargeService.getRechargeHistories();
        return ResponseEntity.ok(new PointApiResponse<>(true, "전체 충전 내역 조회 성공", list, null));
    }

    // [GET] 내 포인트 잔액 (사용자)
    @GetMapping("/balance")
    public ResponseEntity<?> getMyBalance(@AuthenticationPrincipal UserPrincipal user) {
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new PointApiResponse<>(false, "로그인이 필요합니다."));

        try {
            Long userId = user.getSignedUserId();
            Integer balance = userRepository.findPointByUserId(userId);
            if (balance == null) balance = 0;

            log.info("[getMyBalance] userId={}, balance={}", userId, balance);
            return ResponseEntity.ok(new PointApiResponse<>(true, "포인트 잔액 조회 성공", balance));

        } catch (Exception e) {
            log.error("[getMyBalance] 예외 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PointApiResponse<>(false, "포인트 잔액 조회 실패", 0));
        }
    }
}
