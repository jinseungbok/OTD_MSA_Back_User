package com.otd.otd_user.application.user;

import com.otd.configuration.security.SignInProviderType;
import com.otd.otd_user.entity.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUidAndProviderType(String uid, SignInProviderType signInProviderType);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    User findByUserId(Long userId);

    @Modifying
    @Query("UPDATE User u Set u.refreshToken = :refreshToken WHERE u.userId = :userId")
    void updateRefreshToken(Long userId, String refreshToken);

    // 유저 조회
    @Query("SELECT u.userId, p.pointId FROM User u JOIN u.points p WHERE u.userId = :userId")
    List<Object[]> findUserAndPointIds(@Param("userId") Long userId);

    // 포인트 조회
    @Query("SELECT u.point FROM User u WHERE u.userId = :userId")
    Integer findPointByUserId(@Param("userId") Long userId);

    // 포인트 등록
    @Query("update User u set u.point = :point where u.userId = :userId")
    @Modifying
    void addPointByUserId(int point, Long userId);

    // 포인트 수정, 차감
    @Modifying
    @Query("UPDATE User u SET u.point = u.point + :amount WHERE u.userId = :userId")
    void updatePointByUserId(int amount, Long userId);

    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :lastLogin WHERE u.userId = :userId")
    void updateLastLoginByUserId(Long userId, LocalDateTime lastLogin);

    @Modifying
    @Query("UPDATE User u set u.xp = :xp WHERE u.userId = :userId")
    void addXpByUserId(int xp, Long userId);
}