package com.otd.otd_admin.application.admin.Repository;

import com.otd.otd_admin.application.admin.model.statistics.GenderCountRes;
import com.otd.otd_user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AdminUserRepository extends JpaRepository<User, Integer> {
    @Query("SELECT new com.otd.otd_admin.application.admin.model.statistics.GenderCountRes(u.gender, COUNT(u)) " +
            "FROM User u GROUP BY u.gender")
    List<GenderCountRes> countUserByGender();

    @Query(value = """
        SELECT COUNT(DISTINCT u.user_id)
        FROM user u
        JOIN user_role ur ON u.user_id = ur.user_id
        WHERE ur.role_code NOT IN ('03', '04')
    """, nativeQuery = true)
    int countUser();

    @Query("SELECT SUM(point) FROM User")
    int sumPoint();

    List<User> findTop5ByOrderByCreatedAtDesc();
}
