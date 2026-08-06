package com.otd.otd_pointshop.application.point;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/OTD/pointshop/balance")
public class PointBalanceController {

    private final PointBalanceService pointBalanceService;

    // [POST] 사용자 잔액 생성
    @PostMapping("/create")
    public String create(@RequestParam Long userId, @RequestParam int amount) {
        pointBalanceService.createBalance(userId, amount);
        return "Balance created for user " + userId;
    }
}
