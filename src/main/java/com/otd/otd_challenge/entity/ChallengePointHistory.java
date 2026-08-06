package com.otd.otd_challenge.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.otd.otd_user.entity.CreatedAt;
import com.otd.otd_user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ChallengePointHistory extends CreatedAt {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long chId;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne
  @JoinColumn(name = "cd_id", nullable = false)
  private ChallengeDefinition challengeDefinition;

  @Column(nullable = false)
  private int point;

  @Column(nullable = false, length = 30)
  private String reason;
}
