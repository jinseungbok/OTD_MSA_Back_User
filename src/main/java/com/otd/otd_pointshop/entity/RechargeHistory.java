package com.otd.otd_pointshop.entity;

import com.otd.otd_user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_recharge_history",
        indexes = {
                @Index(name = "idx_recharge_admin", columnList = "admin_id"),
                @Index(name = "idx_recharge_user", columnList = "user_id"),
                @Index(name = "idx_recharge_date", columnList = "recharge_at")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RechargeHistory {

    // 충전 이력 ID (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recharge_id")
    private Long rechargeId;

    // 충전 수행 관리자 ID
    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    // 충전 대상 유저 (N:1)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 충전 금액
    @Column(name = "amount", nullable = false)
    private Integer amount;

    // 충전 일시
    @Column(name = "recharge_at", nullable = false, updatable = false)
    private LocalDateTime rechargeAt;

    // 생성 시각 자동 셋팅
    @PrePersist
    public void onCreate() {
        if (rechargeAt == null) {
            rechargeAt = LocalDateTime.now();
        }
    }
}