package com.otd.otd_pointshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "items")
@Entity
@Table(name = "point_category")
public class PointCategory {
    // 카테고리 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_category_id")
    private Long pointCategoryId;

    // 카테고리 이름
    @Column(name = "category_name", nullable = false, unique = true, length = 50)
    private String categoryName;

    // 해당 카테고리에 속한 아이템들
    @OneToMany(mappedBy = "pointCategory", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Point> items = new ArrayList<>();

    // 편의 메서드
    public void addItem(Point point) {
        if (items == null) items = new ArrayList<>();
        items.add(point);
        point.setPointCategory(this);
    }
}