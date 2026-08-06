package com.otd.otd_pointshop.application.point;

import com.otd.otd_pointshop.application.point.model.*;
import com.otd.otd_pointshop.application.purchase.model.PurchaseHistoryRes;
import com.otd.otd_pointshop.entity.*;
import com.otd.otd_pointshop.repository.*;
import com.otd.otd_user.application.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class PointshopService {

    @Value("${constants.file.pointshop-pic}")
    private String uploadDir;

    private final UserRepository userRepository;
    private final PointRepository pointRepository;
    private final PointImageRepository pointImageRepository;
    private final PurchaseHistoryRepository purchaseHistoryRepository;
    private final PointCategoryRepository categoryRepository;
    private final SqlSessionTemplate sqlSessionTemplate;

    // [GET] 전체 포인트 아이템 목록 조회 (모든 사용자 공용)
    public Page<PointGetRes> getAllPointItems(Pageable pageable) {
        Page<Point> pointPage = pointRepository.findAll(pageable);

        List<PointGetRes> content = pointPage.getContent().stream()
                .peek(Point::syncUserPoint)
                .map(point -> {
                    List<PointImageRes> images = pointImageRepository
                            .findByPoint_PointId(point.getPointId(), PageRequest.of(0, 10))
                            .map(this::toImageDto)
                            .getContent();

                    return PointGetRes.builder()
                            .pointId(point.getPointId())
                            .pointItemName(point.getPointItemName())
                            .pointItemContent(point.getPointItemContent())
                            .pointScore(point.getPointScore())
                            .createdAt(point.getCreatedAt())
                            .images(images)
                            .userCurrentPoint(point.getUserCurrentPoint())
                            .build();
                })
                .toList();

        return new PageImpl<>(content, pageable, pointPage.getTotalElements());
    }

    // [GET] 카테고리 기반 아이템 조회 (MyBatis)
    public List<PointGetRes> getItemsByCategory(Long pointCategoryId) {
        var mapper = sqlSessionTemplate.getMapper(PointshopMapper.class);

        if (pointCategoryId != null && !categoryRepository.existsById(pointCategoryId)) {
            throw new IllegalArgumentException("존재하지 않는 카테고리 ID입니다: " + pointCategoryId);
        }

        List<PointGetRes> list = mapper.findAllPoints(pointCategoryId);
        log.info("[카테고리별 목록 조회] pointCategoryId={}, resultCount={}", pointCategoryId, list.size());
        return list;
    }

    // [POST] 포인트 아이템 등록 (관리자 전용)
    @Transactional
    public void createPointItem(PointPostReq dto, MultipartFile[] images) {
        Point point = new Point();
        point.setPointItemName(dto.getPointItemName());
        point.setPointItemContent(dto.getPointItemContent());
        point.setPointScore(dto.getPointScore() != null ? dto.getPointScore().intValue() : 0);

        // 포인트 카테고리 연결
        if (dto.getPointCategoryId() != null) {
            PointCategory category = categoryRepository.findById(dto.getPointCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));
            point.setPointCategory(category);
        }

        pointRepository.save(point);

        if (images != null && images.length > 0) {
            List<PointImage> savedImages = storeImages(images, point);
            point.setPointItemImages(savedImages);
        }

        log.info("[관리자 아이템 등록 완료] item={}, score={}", dto.getPointItemName(), dto.getPointScore());
    }

    // [PUT] 포인트 아이템 수정 (관리자 전용)
    @Transactional
    public void updatePointItem(PointPutReq dto, MultipartFile[] images) {
        Point point = pointRepository.findById(dto.getPointId())
                .orElseThrow(() -> new EntityNotFoundException("포인트 아이템을 찾을 수 없습니다."));

        point.setPointItemName(dto.getPointItemName());
        point.setPointItemContent(dto.getPointItemContent());
        point.setPointScore(dto.getPointScore());

        // 기존 이미지 삭제 후 재등록
        pointImageRepository.deleteAllByPoint(point);
        if (images != null && images.length > 0) {
            List<PointImage> savedImages = storeImages(images, point);
            point.setPointItemImages(savedImages);
        }

        pointRepository.save(point);
        log.info("[관리자 아이템 수정 완료] pointId={}", dto.getPointId());
    }

    // [DELETE] 포인트 아이템 삭제 (관리자 전용)
    @Transactional
    public void deletePointItem(Long pointId) {
        Point point = pointRepository.findById(pointId)
                .orElseThrow(() -> new EntityNotFoundException("포인트 아이템을 찾을 수 없습니다."));

        pointImageRepository.deleteAllByPoint(point);
        pointRepository.delete(point);
        log.info("[관리자 아이템 삭제 완료] pointId={}", pointId);
    }

    // [GET] 키워드 기반 검색 (전체 아이템 대상)
    public Set<String> searchPointKeyword(String keyword, Pageable pageable) {
        List<Point> points = pointRepository.findByPointItemContentContaining(keyword, pageable);

        return points.stream()
                .flatMap(p -> extractKeywords(p.getPointItemContent()).stream())
                .collect(Collectors.toSet());
    }

    // [GET] 유저 포인트 잔액 조회
    public int getUserPointBalance(Long userId) {
        Integer balance = userRepository.findPointByUserId(userId);
        if (balance == null) {
            throw new EntityNotFoundException("유저 포인트 정보를 찾을 수 없습니다.");
        }
        log.info("[포인트 잔액 조회] userId={}, balance={}", userId, balance);
        return balance;
    }

    // [GET] 유저 구매 이력 조회
    public List<PurchaseHistoryRes> getUserPurchaseHistory(Long userId) {
        return purchaseHistoryRepository.findByUser_UserId(userId).stream()
                .map(PurchaseHistoryRes::fromEntity)
                .toList();
    }

    // 이미지 저장 로직
    private List<PointImage> storeImages(MultipartFile[] images, Point point) {
        List<PointImage> savedImages = new ArrayList<>();

        Path directoryPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(directoryPath);
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 경로 생성 실패", e);
        }

        for (MultipartFile file : images) {
            validateImageExtension(file);

            String originalName = FilenameUtils.getBaseName(file.getOriginalFilename());
            String extension = FilenameUtils.getExtension(file.getOriginalFilename());
            String uuidName = UUID.randomUUID() + "_" + originalName + "." + extension;
            Path savePath = directoryPath.resolve(uuidName);

            try {
                file.transferTo(savePath.toFile());
            } catch (IOException e) {
                log.error("이미지 저장 실패: {}", originalName, e);
                throw new RuntimeException("이미지 저장 실패: " + originalName, e);
            }

            PointImage image = PointImage.builder()
                    .imageUrl(uuidName)
                    .imageType(file.getContentType())
                    .altText(originalName)
                    .point(point)
                    .build();

            savedImages.add(image);
        }

        return pointImageRepository.saveAll(savedImages);
    }

    // 이미지 확장자 검증
    private void validateImageExtension(MultipartFile file) {
        String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        if (!List.of("jpg", "jpeg", "png", "gif", "bmp").contains(ext.toLowerCase())) {
            throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다.");
        }
    }

    // 이미지 DTO 변환
    private PointImageRes toImageDto(PointImage img) {
        return PointImageRes.builder()
                .imageId(img.getImageId())
                .altText(img.getAltText())
                .imageType(img.getImageType())
                .imageUrl(img.getImageUrl())
                .build();
    }

    // 키워드 추출 유틸
    private Set<String> extractKeywords(String content) {
        if (content == null || content.isBlank()) return Set.of();
        return Arrays.stream(content.split("\\s+"))
                .map(word -> word.replaceAll("[^\\p{L}\\p{N}]", ""))                .filter(word -> word.length() > 1)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }
}
