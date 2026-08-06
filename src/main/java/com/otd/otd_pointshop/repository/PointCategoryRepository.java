package com.otd.otd_pointshop.repository;

import com.otd.otd_pointshop.entity.PointCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PointCategoryRepository extends JpaRepository<PointCategory, Long> {
    Optional<PointCategory> findByCategoryName(String categoryName);
    boolean existsByCategoryName(String categoryName);
}
