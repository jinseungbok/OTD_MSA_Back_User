package com.otd.otd_challenge.application.challenge;

import com.otd.otd_challenge.application.challenge.model.settlement.ChallengeSettlementDto;
import com.otd.otd_challenge.application.challenge.model.settlement.ChallengeSettlementGetReq;
import com.otd.otd_challenge.application.challenge.model.settlement.ChallengeSettlementGetRes;
import com.otd.otd_challenge.application.challenge.model.settlement.ChallengeSuccessDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ChallengeSettlementMapper {
  List<Long> findByUserId(ChallengeSettlementDto dto);
  List<ChallengeSuccessDto> findProgressChallengeByUserId(ChallengeSettlementDto dto);
  List<ChallengeSettlementGetRes> findSettlementLogByUserIdAndSettlementDate(ChallengeSettlementGetReq req);
}
