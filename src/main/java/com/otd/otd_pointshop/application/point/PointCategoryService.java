package com.otd.otd_pointshop.application.point;

import com.otd.otd_pointshop.entity.PointCategory;
import com.otd.otd_pointshop.repository.PointCategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PointCategoryService {

    private final PointCategoryRepository pointCategoryRepository;

    // [CREATE] 카테고리 등록
    public PointCategory createCategory(String categoryName) {
        if (pointCategoryRepository.existsByCategoryName(categoryName)) {
            throw new IllegalArgumentException("이미 존재하는 카테고리입니다: " + categoryName);
        }

        PointCategory category = PointCategory.builder()
                .categoryName(categoryName)
                .build();

        return pointCategoryRepository.save(category);
    }

    // [READ] 전체 카테고리 목록 조회
    @Transactional
    public List<PointCategory> getAllCategories() {
        return pointCategoryRepository.findAll();
    }

    // [READ] 카테고리 단건 조회
    @Transactional
    public PointCategory getCategoryById(Long id) {
        return pointCategoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다: " + id));
    }

    // [UPDATE] 카테고리 이름 수정
    public PointCategory updateCategory(Long id, String newName) {
        PointCategory category = getCategoryById(id);

        if (pointCategoryRepository.existsByCategoryName(newName)) {
            throw new IllegalArgumentException("이미 존재하는 카테고리명입니다: " + newName);
        }

        category.setCategoryName(newName);
        return pointCategoryRepository.save(category);
    }

    // [DELETE] 카테고리 삭제
    public void deleteCategory(Long id) {
        if (!pointCategoryRepository.existsById(id)) {
            throw new IllegalArgumentException("존재하지 않는 카테고리입니다: " + id);
        }
        pointCategoryRepository.deleteById(id);
    }
}
