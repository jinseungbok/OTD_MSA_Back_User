package com.otd.otd_pointshop.application.stats;

import com.otd.configuration.model.UserPrincipal;
import com.otd.otd_pointshop.application.point.model.PointApiResponse;
import com.otd.otd_pointshop.application.stats.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/OTD/pointshop/admin/stats")
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/summary")
    public ResponseEntity<?> getMonthlySummary(@AuthenticationPrincipal UserPrincipal admin) {
        if (!isAdmin(admin)) return forbidden("관리자 권한이 필요합니다.");
        List<MonthlySummaryStatsRes> summary = statsService.getMonthlySummary();
        return ResponseEntity.ok(new PointApiResponse<>(true, "월별 요약 통계 조회 성공", summary, null));
    }

    @GetMapping("/detail/{month}")
    public ResponseEntity<?> getMonthlyDetail(
            @AuthenticationPrincipal UserPrincipal admin,
            @PathVariable String month,
            @RequestParam(required = false) Long userId
    ) {
        if (!isAdmin(admin)) return forbidden("관리자 권한이 필요합니다.");
        List<MonthlyDetailRes> details = statsService.getMonthlyDetail(month, userId);
        return ResponseEntity.ok(new PointApiResponse<>(true, "월별 상세 내역 조회 성공", details, null));
    }

    @GetMapping("/top/recharge")
    public ResponseEntity<?> getTopRechargeUsers(@AuthenticationPrincipal UserPrincipal admin) {
        if (!isAdmin(admin)) return forbidden("관리자 권한이 필요합니다.");
        List<UserSpendTopRes> topRecharge = statsService.getTopRechargeUsers();
        return ResponseEntity.ok(new PointApiResponse<>(true, "충전 TOP 10 조회 성공", topRecharge, null));
    }

    @GetMapping("/top/purchase")
    public ResponseEntity<?> getTopPurchaseUsers(@AuthenticationPrincipal UserPrincipal admin) {
        if (!isAdmin(admin)) return forbidden("관리자 권한이 필요합니다.");
        List<UserSpendTopRes> topPurchase = statsService.getTopPurchaseUsers();
        return ResponseEntity.ok(new PointApiResponse<>(true, "구매 TOP 10 조회 성공", topPurchase, null));
    }

    @GetMapping("/popular")
    public ResponseEntity<?> getPopularItems(@AuthenticationPrincipal UserPrincipal admin) {
        if (!isAdmin(admin)) return forbidden("관리자 권한이 필요합니다.");
        List<PopularItemRes> popular = statsService.getPopularItems();
        return ResponseEntity.ok(new PointApiResponse<>(true, "인기 아이템 조회 성공", popular, null));
    }

    private boolean isAdmin(UserPrincipal admin) {
        return admin != null && admin.hasRole("ADMIN");
    }

    private ResponseEntity<PointApiResponse<?>> forbidden(String msg) {
        return ResponseEntity.status(403).body(new PointApiResponse<>(false, msg));
    }
}
