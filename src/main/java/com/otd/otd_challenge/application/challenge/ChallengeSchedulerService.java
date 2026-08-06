package com.otd.otd_challenge.application.challenge;

import com.otd.configuration.enumcode.model.EnumChallengeRole;
import com.otd.otd_challenge.application.challenge.Repository.ChallengeDefinitionRepository;
import com.otd.otd_challenge.application.challenge.Repository.ChallengePointRepository;
import com.otd.otd_challenge.application.challenge.Repository.ChallengeRoleRepository;
import com.otd.otd_challenge.application.challenge.Repository.ChallengeSettlementRepository;
import com.otd.otd_challenge.application.challenge.model.settlement.ChallengeSettlementDto;
import com.otd.otd_challenge.application.challenge.model.settlement.ChallengeSettlementGetReq;
import com.otd.otd_challenge.application.challenge.model.settlement.ChallengeSettlementGetRes;
import com.otd.otd_challenge.application.challenge.model.settlement.ChallengeSuccessDto;
import com.otd.otd_challenge.entity.ChallengeDefinition;
import com.otd.otd_challenge.entity.ChallengePointHistory;
import com.otd.otd_challenge.entity.ChallengeSettlementLog;
import com.otd.otd_user.application.user.UserRepository;
import com.otd.otd_user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeSchedulerService {

  private final ChallengeDefinitionRepository challengeDefinitionRepository;
  private final UserRepository userRepository;
  private final ChallengeSettlementMapper challengeSettlementMapper;
  private final ChallengePointRepository challengePointRepository;
  private final ChallengeSettlementRepository challengeSettlementRepository;
  private final TierService tierService;
  private final ChallengeRoleRepository challengeRoleRepository;

  @Transactional
  public void setSettlement(LocalDate startDate, LocalDate endDate, String type){
    ChallengeSettlementDto dto = ChallengeSettlementDto.builder().startDate(startDate).endDate(endDate).type(type).build();

    List<Long> userIds = challengeSettlementMapper.findByUserId(dto);

    for (Long userId : userIds) {
      User user = userRepository.findById(userId).orElseThrow();

      ChallengeSettlementDto userDto = ChallengeSettlementDto.builder()
              .userId(userId)
              .startDate(dto.getStartDate())
              .endDate(dto.getEndDate())
              .type(dto.getType())
              .build();

      List<ChallengeSuccessDto> challengeProgress = challengeSettlementMapper.findProgressChallengeByUserId(userDto);

      for (ChallengeSuccessDto progress : challengeProgress) {
        ChallengeDefinition cd = challengeDefinitionRepository.findById(progress.getCdId()).orElseThrow();

        int totalPoint = 0;

        totalPoint += progress.getReward();

        ChallengePointHistory ch = ChallengePointHistory.builder()
                .user(user).challengeDefinition(cd)
                .point(progress.getReward())
                .reason(progress.getType()+"_"+progress.getName())
                .build();

        challengePointRepository.save(ch);

        if(dto.getType().equals("weekly") || dto.getType().equals("competition")){
          if(progress.getRank() <= 3) {
            int rankPoint = switch (progress.getRank()) {
              case 1 -> 100;
              case 2 -> 70;
              case 3 -> 50;
              default -> 0;
            };
            String formatName = switch (progress.getRank()){
              case 1 -> "1위_reward_" + progress.getName();
              case 2 -> "2위_reward_" + progress.getName();
              case 3 -> "3위_reward_" + progress.getName();
              default -> progress.getName();
            };

            if (rankPoint > 0) {
              ChallengePointHistory chRank = ChallengePointHistory.builder()
                      .user(user)
                      .challengeDefinition(cd)
                      .point(rankPoint)
                      .reason(formatName)
                      .build();
              challengePointRepository.save(chRank);
              totalPoint += rankPoint;
            }
          }
        }

        if(dto.getType().equals("personal")){
          int endDateOfMonth = dto.getEndDate().getDayOfMonth();
          int attendancePoint = 0;
          String formatted = "";
          if(progress.getTotalRecord() == endDateOfMonth){
            attendancePoint = 100;
            formatted = "개근_reward_";
          }else if(progress.getTotalRecord() >= 25){
            attendancePoint = 70;
            formatted = "25일 이상_reward_";
          }else if(progress.getTotalRecord() >= 20){
            attendancePoint = 50;
            formatted = "20일 이상_reward_";
          }

          if(attendancePoint > 0){
            ChallengePointHistory chRank = ChallengePointHistory.builder()
                    .user(user)
                    .challengeDefinition(cd)
                    .point(attendancePoint)
                    .reason(formatted + progress.getName())
                    .build();
            challengePointRepository.save(chRank);
            totalPoint += attendancePoint;
          }
        }
        ChallengeSettlementLog log = ChallengeSettlementLog.builder().
                user(user).challengeDefinition(cd).type(progress.getType()+"_"+progress.getName()).totalXp(progress.getXp()).totalPoint(totalPoint).build();

        challengeSettlementRepository.save(log);

        int sumPoint = user.getPoint() + totalPoint;
        int sumXp = user.getXp() + progress.getXp();


        user.setPoint(sumPoint);
        user.setXp(sumXp);

        EnumChallengeRole myRole = user.getChallengeRole();

        EnumChallengeRole newRole = tierService.checkTierUp(myRole, sumXp);
        if (newRole != myRole) {
          challengeRoleRepository.updateChallengeRole(user.getUserId(), newRole);
        }
      }

    }
  }

  @Value("${constants.file.challenge}")
  private String imgPath;
  private void addImgPath(List<?> list) {
    for (Object o : list) {
     if(o instanceof ChallengeSettlementGetRes cs){
       cs.setImage(imgPath + "/" + cs.getImage());
     }
    }
  }
  public List<ChallengeSettlementGetRes> getSettlementLog(Long userId, ChallengeSettlementGetReq req){
    req.setUserId(userId);
    List<ChallengeSettlementGetRes> res = challengeSettlementMapper.findSettlementLogByUserIdAndSettlementDate(req);
    addImgPath(res);
    return res;
  }
}
