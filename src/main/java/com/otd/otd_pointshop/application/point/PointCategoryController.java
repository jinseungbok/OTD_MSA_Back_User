package com.otd.otd_pointshop.application.point;

import com.otd.otd_pointshop.application.point.model.PointApiResponse;
import com.otd.otd_pointshop.entity.PointCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/OTD/pointshop/category")
public class PointCategoryController {

    private final PointCategoryService pointCategoryService;

    /** [POST] 카테고리 등록 */
    @PostMapping("/add")
    public PointApiResponse<PointCategory> addCategory(@RequestParam String categoryName) {
        PointCategory category = pointCategoryService.createCategory(categoryName);
        return PointApiResponse.success("카테고리 등록 성공", category);
    }

    /** [GET] 전체 카테고리 목록 조회 */
    @GetMapping("/list")
    public PointApiResponse<List<PointCategory>> getAllCategories() {
        List<PointCategory> list = pointCategoryService.getAllCategories();
        return PointApiResponse.success("카테고리 목록 조회 성공", list);
    }

    /** [GET] 특정 카테고리 단건 조회 */
    @GetMapping("/{id}")
    public PointApiResponse<PointCategory> getCategory(@PathVariable Long id) {
        PointCategory category = pointCategoryService.getCategoryById(id);
        return PointApiResponse.success("카테고리 조회 성공", category);
    }

    /** [PUT] 카테고리 이름 수정 */
    @PutMapping("/{id}")
    public PointApiResponse<PointCategory> updateCategory(
            @PathVariable Long id,
            @RequestParam String newName
    ) {
        PointCategory updated = pointCategoryService.updateCategory(id, newName);
        return PointApiResponse.success("카테고리 수정 성공", updated);
    }

    /** [DELETE] 카테고리 삭제 */
    @DeleteMapping("/{id}")
    public PointApiResponse<Void> deleteCategory(@PathVariable Long id) {
        pointCategoryService.deleteCategory(id);
        return PointApiResponse.success("카테고리 삭제 성공", null);
    }
}
