package com.otd.otd_challenge.entity;

import com.otd.otd_user.entity.CreatedAt;
import com.otd.otd_user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ChallengeSettlementLog extends CreatedAt {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long csId;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne
  @JoinColumn(name = "cd_id", nullable = false)
  private ChallengeDefinition challengeDefinition;

  @Column(nullable = false)
  private int totalPoint;

  @Column(nullable = false)
  private int totalXp;

  @Column(nullable = false, length = 30)
  private String type;
}
