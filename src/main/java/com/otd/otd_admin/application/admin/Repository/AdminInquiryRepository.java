package com.otd.otd_admin.application.admin.Repository;

import com.otd.configuration.enumcode.model.EnumInquiryStatus;
import com.otd.otd_user.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AdminInquiryRepository extends JpaRepository<Inquiry, Long> {
    int countByStatus(EnumInquiryStatus status);

    @Query("SELECT COUNT(i) FROM Inquiry i")
    int countAllInquiry();
}
