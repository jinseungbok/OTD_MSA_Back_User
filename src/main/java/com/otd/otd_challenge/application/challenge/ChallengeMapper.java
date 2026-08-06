package com.otd.otd_challenge.application.challenge;

import com.otd.otd_challenge.application.challenge.model.*;
import com.otd.otd_challenge.application.challenge.model.detail.*;
import com.otd.otd_challenge.application.challenge.model.home.ChallengeMissionCompleteGetRes;
import com.otd.otd_challenge.application.challenge.model.home.ChallengeRecordMissionPostReq;
import com.otd.otd_challenge.application.challenge.model.home.MainHomGetReq;
import com.otd.otd_challenge.application.challenge.model.home.MainHomeGetRes;
import com.otd.otd_challenge.application.challenge.model.settlement.ChallengeSettlementDto;
import com.otd.otd_challenge.entity.ChallengeProgress;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ChallengeMapper {
    List<ChallengeDefinitionGetRes> findAll();
    List<ChallengeProgressGetRes> findAllMonthlyFromUserId(ChallengeProgressGetReq req);
    List<ChallengeProgressGetRes> findAllWeeklyFromUserId(ChallengeProgressGetReq req);
    List<ChallengeDefinitionGetRes> findByType(ChallengeProgressGetReq req);
    List<ChallengeDefinitionGetRes> findByTypeForCompetition(ChallengeProgressGetReq req);
    ChallengeDetailPerGetRes findProgressByUserIdAndCdId(ChallengeProgressGetReq req);
    List<ChallengeRankGetRes> findTop5Ranking(ChallengeProgressGetReq req);
    List<ChallengeRankGetRes> findAroundMyRank(ChallengeProgressGetReq req);
    List<ChallengeDetailDayGetRes> findDayByUserIdAndCdId(ChallengeProgressGetReq req);
    Integer findSuccessChallenge(Long userId);
    int saveMissionRecordByUserIdAndCpId(Long userId, Long cdId);
    List<ChallengeMissionCompleteGetRes> findByUserIdAndMissionComplete(Long userId);
    List<MainHomeGetRes> findAllMyChallenge(MainHomGetReq req);
    Optional<ChallengeProgress> findByUserIdAndCdId(Long userId, Long cdId);
}
