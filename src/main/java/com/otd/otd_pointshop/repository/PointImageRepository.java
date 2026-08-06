package com.otd.otd_pointshop.repository;

import com.otd.otd_pointshop.entity.Point;
import com.otd.otd_pointshop.entity.PointImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointImageRepository extends JpaRepository<PointImage, Long> {
    Page<PointImage> findByPoint_PointId(Long pointId, Pageable pageable); // 특정 포인트 이미지 목록 조회
    List<PointImage> findByPoint(Point point); // 특정 포인트 아이템 이미지 목록 조회
    List<PointImage> findByPoint_PointIdAndImageUrl(Long pointId, String imageUrl); // 특정 포인트id, 이미지 url 필터링
  //void updateAllByPoint(Point point); // save에 포함되어 있으므로 별도 update 필요 없음
    void deleteAllByPoint(Point point); // 특정 포인트 이미지 전체 삭제
}
