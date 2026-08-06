package com.otd.otd_challenge.entity;

import com.otd.otd_user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ChallengeMission {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long cm_id;

  @ManyToOne
  @JoinColumn(name = "cd_id", nullable = false)
  private ChallengeDefinition challengeDefinition;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private LocalDate successDate;
}
