
package com.otd.otd_user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.otd.configuration.enumcode.model.EnumChallengeRole;
import com.otd.configuration.enumcode.model.EnumUserRole;
import com.otd.configuration.security.SignInProviderType;
import com.otd.otd_challenge.entity.*;
import com.otd.otd_pointshop.entity.Point;
import com.otd.otd_pointshop.entity.PointBalance;
import com.otd.otd_pointshop.entity.PointUser;
import com.otd.otd_pointshop.entity.PurchaseHistory;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Entity //테이블을 만들고 DML때 사용
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class User extends UpdatedAt{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 50)
    private String uid;

    @Column(nullable = false, length = 100)
    @JsonIgnore
    private String upw;

    @Column(length = 30, name = "nick_name")
    private String nickName;

    @Column(length = 100)
    private String pic;

    @Column(length = 30)
    private String name;

    @Column(name = "birth_date")
    private String birthDate;

    @Column(length = 1)
    private String gender;

    @Column(length = 100)
    private String email;

    @Column(length = 30)
    private String phone;

    @Convert(converter = SignInProviderType.CodeConverter.class)
    @Column(length = 2)
    private SignInProviderType providerType;

    @Column(name = "point", columnDefinition = "int DEFAULT 0", nullable = false)
    private int point;

    @Column(columnDefinition = "int DEFAULT 0")
    private int xp;

    @Column(length = 300)
    @JsonIgnore
    private String refreshToken;

    @Column
    private LocalDateTime lastLogin;

    // 유저가 보유한 포인트 아이템 (1:N)
    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Point> points = new ArrayList<>();

    public void addPoint(Point pointEntity) {
        if (points == null) points = new ArrayList<>();
        points.add(pointEntity);
        pointEntity.setUser(this);
    }

    // 포인트 사용 이력
    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PointUser> pointUsers = new ArrayList<>();

    public void addPointUser(PointUser pointUser) {
        if (this.pointUsers == null) this.pointUsers = new ArrayList<>();
        this.pointUsers.add(pointUser);
        pointUser.setUser(this);
    }

    // 포인트 잔액, 구매 이력
    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PointBalance> pointBalances = new ArrayList<>();

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseHistory> purchaseHistories = new ArrayList<>();


    @Column(name = "onboarding_completed", columnDefinition = "INT DEFAULT 0")
    private Integer onboardingCompleted = 0;

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAgreement> agreements = new ArrayList<>();

    //cascade는 자식과 나랑 모든 연결 (내가 영속성되면 자식도 영속성되고, 내가 삭제되면 자식도 삭제 된다. 등등)
    //ohphanRemoval은 userRoles에서 자식을 하나 제거함. 그러면 DB에도 뺀 자식은 삭제처리가 된다.
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch =  FetchType.EAGER)
    private List<UserRole> userRoles = new ArrayList<>(1);

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChallengeMission> challengeMissions = new ArrayList<>();

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChallengePointHistory> challengePointHistories = new ArrayList<>();

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChallengeProgress> challengeProgresses = new ArrayList<>();

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChallengeSettlementLog> challengeSettlementLogs = new ArrayList<>();

    public void addUserRoles(List<EnumUserRole> enumUserRole, EnumChallengeRole enumChallengeRole) {
        for(EnumUserRole e : enumUserRole) {
            UserRoleIds ids = new UserRoleIds(this.userId, e, enumChallengeRole);
            UserRole userRole = new UserRole(ids, this);

            this.userRoles.add(userRole);
        }
    }
    public EnumChallengeRole getChallengeRole() {
        return this.userRoles.stream()
                .map(userRole -> userRole.getUserRoleIds().getChallengeCode())
                .findFirst() // 유저가 가진 챌린지 Role 하나만 꺼냄
                .orElse(EnumChallengeRole.TBD);
    }

    public Integer getAge() {
        if (this.birthDate == null || this.birthDate.length() < 10) {
            return null;
        }
        LocalDate birthDate = LocalDate.parse(this.birthDate, DateTimeFormatter.ISO_LOCAL_DATE);
        return Period.between(birthDate, LocalDate.now()).getYears();
    }


}
