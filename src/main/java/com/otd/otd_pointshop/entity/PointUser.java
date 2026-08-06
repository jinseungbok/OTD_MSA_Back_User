package com.otd.otd_pointshop.entity;

import com.otd.otd_user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "point_user")
public class PointUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 유저 PK
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 현재 잔여 포인트
    @Column(name = "point_balance", nullable = false)
    private Integer pointBalance;

    // 변동 포인트 (예: +1000, -500)
    @Column(name = "point_delta")
    private Integer pointDelta;

    // 변동 사유 (ex: 구매, 충전, 관리자지급)
    @Column(name = "reason", length = 255)
    private String reason;

    //  등록 시각
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
