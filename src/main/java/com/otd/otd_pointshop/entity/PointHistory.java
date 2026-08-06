package com.otd.otd_pointshop.entity;

import com.otd.otd_user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // UserId PK 값 저장
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "charge_point", nullable = false)
    private int chargePoint;      // + 적립, - 차감

    @Column(name = "description") // ex) "스타벅스 아메리카노"
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}