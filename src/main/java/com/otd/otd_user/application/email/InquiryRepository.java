package com.otd.otd_user.application.email;

import com.otd.configuration.enumcode.model.EnumInquiryStatus;
import com.otd.otd_user.entity.Inquiry;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    // 특정 사용자의 문의 내역 조회 (최신순)
    @Query("SELECT i FROM Inquiry i WHERE i.user.userId = :userId ORDER BY i.createdAt DESC")
    List<Inquiry> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    Inquiry findById(long id);

//    // 특정 상태의 문의 조회
//    List<Inquiry> findByStatus(EnumInquiryStatus status);
//
//    // 특정 사용자의 특정 문의 조회
//    Optional<Inquiry> findByIdAndUserId(Long id, Long userId);
//
//    // 특정 사용자의 특정 문의 존재 여부
//    boolean existsByIdAndUserId(Long id, Long userId);
//
//    void deleteAllByUserId(Long userId);
}