package com.otd.otd_pointshop.application.stats;

import com.otd.otd_pointshop.application.stats.model.*;
import com.otd.otd_pointshop.repository.StatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final StatsRepository statsRepository;

    // [GET] 전체 월별 요약
    public List<MonthlySummaryStatsRes> getMonthlySummary() {
        return mergeStats(
                statsRepository.findMonthlyRechargeTotals(),
                statsRepository.findMonthlyPurchaseTotals()
        );
    }

    // [GET] 유저별 월별 요약
    public List<MonthlySummaryStatsRes> getMonthlySummaryByUser(Long userId) {
        return mergeStats(
                statsRepository.findMonthlyRechargeTotalsByUser(userId),
                statsRepository.findMonthlyPurchaseTotalsByUser(userId)
        );
    }

    // [GET] 월별 상세 내역 (충전 + 구매)
    public List<MonthlyDetailRes> getMonthlyDetail(String month, Long userId) {
        // 충전 내역 조회
        List<Object[]> rechargeList = (userId != null)
                ? statsRepository.findRechargeDetailsByMonthAndUser(month, userId)
                : statsRepository.findRechargeDetailsByMonth(month);

        // 구매 내역 조회
        List<Object[]> purchaseList = (userId != null)
                ? statsRepository.findPurchaseDetailsByMonthAndUser(month, userId)
                : statsRepository.findPurchaseDetailsByMonth(month);

        List<MonthlyDetailRes> result = new ArrayList<>();

        // 충전 내역 추가
        rechargeList.forEach(r -> result.add(MonthlyDetailRes.builder()
                .type("RECHARGE")
                .date(toStringSafe(r[0]))
                .nickname(toStringSafe(r[1]))
                .itemName(null)
                .amount(toLongSafe(r[2]))
                .createdAt(toStringSafe(r[0]))
                .build()));

        // 구매 내역 추가
        purchaseList.forEach(p -> result.add(MonthlyDetailRes.builder()
                .type("PURCHASE")
                .date(toStringSafe(p[0]))
                .nickname(toStringSafe(p[1]))
                .itemName(toStringSafe(p[2]))
                .amount(toLongSafe(p[3]))
                .createdAt(toStringSafe(p[0]))
                .build()));

        // 최신순 정렬
        result.sort(Comparator.comparing(MonthlyDetailRes::getDate).reversed());
        return result;
    }

    // [GET] 충전 TOP10 유저
    public List<UserSpendTopRes> getTopRechargeUsers() {
        return statsRepository.findTopRechargeUsers().stream()
                .map(rank -> UserSpendTopRes.builder()
                        .userId(toLongSafe(rank[0]))
                        .nickname(toStringSafe(rank[1]))
                        .totalAmount(toLongSafe(rank[2]))
                        .type("RECHARGE")
                        .build())
                .toList();
    }

    // [GET] 구매 TOP10 유저
    public List<UserSpendTopRes> getTopPurchaseUsers() {
        return statsRepository.findTopPurchaseUsers().stream()
                .map(rank -> UserSpendTopRes.builder()
                        .userId(toLongSafe(rank[0]))
                        .nickname(toStringSafe(rank[1]))
                        .totalAmount(toLongSafe(rank[2]))
                        .type("PURCHASE")
                        .build())
                .toList();
    }

    // [GET] 인기 아이템 조회
    public List<PopularItemRes> getPopularItems() {
        return statsRepository.findPopularItems().stream()
                .map(result -> PopularItemRes.builder()
                        .pointItemName(toStringSafe(result[0]))
                        .purchaseCount(toIntSafe(result[1]))
                        .build())
                .toList();
    }

    // [GET] 총 사용 포인트 상위 유저 조회
    public List<UserSpendTopRes> getTopUsers() {
        return statsRepository.findTopUsersByTotalSpentPoints().stream()
                .map(result -> UserSpendTopRes.builder()
                        .userId(toLongSafe(result[0]))
                        .totalAmount(toLongSafe(result[1]))
                        .type("PURCHASE")
                        .build())
                .toList();
    }

    // [공통] 월별 통계 병합 로직
    private List<MonthlySummaryStatsRes> mergeStats(List<Object[]> rechargeData, List<Object[]> purchaseData) {
        Map<String, Long> rechargeMap = toMonthAmountMap(rechargeData);
        Map<String, Long> purchaseMap = toMonthAmountMap(purchaseData);

        // 두 Map의 모든 월(month) 합집합 생성
        Set<String> allMonths = new HashSet<>();
        allMonths.addAll(rechargeMap.keySet());
        allMonths.addAll(purchaseMap.keySet());

        return allMonths.stream()
                .sorted()
                .map(month -> {
                    long recharge = rechargeMap.getOrDefault(month, 0L);
                    long purchase = purchaseMap.getOrDefault(month, 0L);
                    return MonthlySummaryStatsRes.builder()
                            .month(month)
                            .totalRecharge(recharge)
                            .totalPurchase(purchase)
                            .netChange(recharge - purchase)
                            .build();
                })
                .toList();
    }

    // [유틸] Object[] → 월-금액 Map 변환
    private Map<String, Long> toMonthAmountMap(List<Object[]> data) {
        if (data == null) return Collections.emptyMap();
        return data.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        row -> toStringSafe(row[0]),
                        row -> toLongSafe(row[1]),
                        Long::sum
                ));
    }

    // [유틸] null-safe 변환 메서드
    private String toStringSafe(Object o) {
        return o != null ? o.toString() : "";
    }

    private long toLongSafe(Object o) {
        return (o instanceof Number n) ? n.longValue() : 0L;
    }

    private int toIntSafe(Object o) {
        return (o instanceof Number n) ? n.intValue() : 0;
    }
}
