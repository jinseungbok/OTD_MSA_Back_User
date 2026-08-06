package com.otd.otd_pointshop.application.point;

import com.otd.otd_pointshop.application.point.model.PointGetRes;
import com.otd.otd_pointshop.entity.Point;
import com.otd.otd_pointshop.entity.PointImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PointshopMapper {

    // [GET] 유저별 포인트 목록 조회 (페이징)
    List<Point> findAllByUserId(
            @Param("userId") Long userId,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize
    );

    // [GET] 단일 포인트 아이템 조회
    Point findOneByUserIdAndPointId(
            @Param("userId") Long userId,
            @Param("pointId") Long pointId
    );

    // [GET] 포인트 이미지 조회
    List<PointImage> findImagesByPointId(@Param("pointId") Long pointId);

    // [INSERT / UPDATE / DELETE]
    int savePoint(Point point);
    int updatePoint(Point point);
    int deletePoint(@Param("userId") Long userId, @Param("pointId") Long pointId);

    // [GET] 유저별 포인트 목록 DTO 버전
    List<PointGetRes> findAllByUserId(
            @Param("userId") Long userId,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    int countPointByUserId(@Param("userId") Long userId);

    // [GET] 카테고리별 포인트 아이템 목록
    List<PointGetRes> findAllPoints(@Param("pointCategoryId") Long pointCategoryId);
}
