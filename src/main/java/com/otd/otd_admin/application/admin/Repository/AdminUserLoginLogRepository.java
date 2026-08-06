package com.otd.otd_admin.application.admin.Repository;

import com.otd.otd_user.entity.UserLoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AdminUserLoginLogRepository extends JpaRepository<UserLoginLog, Long> {

    @Query("SELECT COUNT(DISTINCT l.user.userId) FROM UserLoginLog l WHERE DATE(l.loginDate) = CURRENT_DATE")
    int countTodayLogin();
}
