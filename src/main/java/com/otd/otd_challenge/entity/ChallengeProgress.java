package com.otd.otd_challenge.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.otd.otd_user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@Setter
@DynamicInsert
public class ChallengeProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cpId;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "cd_id", nullable = false)
    private ChallengeDefinition challengeDefinition;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(columnDefinition = "double DEFAULT 0", nullable = false)
    private Double totalRecord;

    @Column(columnDefinition = "TINYINT(1) DEFAULT 0", nullable = false)
    private boolean isSuccess;

    @Builder.Default
    @OneToMany(mappedBy = "challengeProgress", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChallengeRecord> challengeRecords = new ArrayList<>();
}
