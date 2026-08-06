package com.otd.otd_challenge.application.challenge.Repository;

import com.otd.otd_challenge.entity.ChallengeDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChallengeDefinitionRepository extends JpaRepository<ChallengeDefinition, Long> {

    List<ChallengeDefinition> findByCdType(String cdPeriod);

    ChallengeDefinition findByCdId(Long cdId);

    List<ChallengeDefinition> findByCdName(String cdName);

    int deleteByCdId(Long cdId);

    @Query("SELECT COUNT(cd) FROM ChallengeDefinition cd")
    int countAllChallenge();
}
