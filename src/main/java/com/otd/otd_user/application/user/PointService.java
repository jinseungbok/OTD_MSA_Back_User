package com.otd.otd_user.application.user;

import com.otd.configuration.enumcode.model.EnumChallengeRole;
import com.otd.otd_challenge.application.challenge.ChallengeMapper;
import com.otd.otd_challenge.application.challenge.Repository.ChallengeDefinitionRepository;
import com.otd.otd_challenge.application.challenge.model.detail.ChallengeProgressGetReq;
import com.otd.otd_challenge.application.challenge.model.detail.ChallengeProgressGetRes;
import com.otd.otd_challenge.application.challenge.model.home.ChallengeHomeGetRes;
import com.otd.otd_challenge.application.challenge.model.home.ChallengeMissionCompleteGetRes;
import com.otd.otd_challenge.application.challenge.model.home.UserInfoGetRes;
import com.otd.otd_challenge.entity.ChallengeDefinition;
import com.otd.otd_challenge.entity.ChallengePointHistory;
import com.otd.otd_user.application.user.model.PointHistoryDTO;
import com.otd.otd_user.application.user.model.PointHistoryResponseDTO;
import com.otd.otd_user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointService {

    private final UserChallengePointRepository userChallengePointRepository;
    private final UserRepository userRepository;
    private final ChallengeMapper challengeMapper;
    private final ChallengeDefinitionRepository challengeDefinitionRepository;
    private final UserMapper userMapper;

    public PointHistoryResponseDTO getPointHistory(Long userId) {
        // 사용자 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));


        // 포인트 내역 조회
        List<ChallengePointHistory> histories =
                userChallengePointRepository.findByUserUserIdOrderByCreatedAtDesc(userId);


        // 데이터가 있으면 첫 번째 항목 로그 출력
        if (!histories.isEmpty()) {
            ChallengePointHistory first = histories.get(0);
            log.info("첫 번째 내역 - chId: {}, reason: {}, point: {}, createdAt: {}",
                    first.getChId(), first.getReason(), first.getPoint(), first.getCreatedAt());
        } else {
            log.warn("포인트 내역이 비어있습니다!");
        }

        // DTO 변환
        List<PointHistoryDTO> historyDTOs = histories.stream()
                .map(history -> PointHistoryDTO.builder()
                        .chId(history.getChId())
                        .reason(history.getReason())
                        .point(history.getPoint())
                        .createdAt(history.getCreatedAt())
                        .build())
                .collect(Collectors.toList());


        // 응답 생성
        return PointHistoryResponseDTO.builder()
                .user(PointHistoryResponseDTO.UserPointDTO.builder()
                        .nickName(user.getNickName())
                        .totalPoint(user.getPoint())
                        .build())
                .pointHistory(historyDTOs)
                .build();
    }
    //전체 조회
    public ChallengeHomeGetRes getSelectedListAll(Long userId, ChallengeProgressGetReq req) {
        req.setUserId(userId);
        List<ChallengeProgressGetRes> monthly = challengeMapper.findAllMonthlyFromUserId(req);
        List<ChallengeProgressGetRes> weekly = challengeMapper.findAllWeeklyFromUserId(req);
        List<ChallengeDefinition> daily = challengeDefinitionRepository.findByCdType("daily");
        User userInfo = userRepository.findByUserId(userId);
        Integer success = challengeMapper.findSuccessChallenge(userId);

        //전체 미션 완료 내역 조회
        List<ChallengeMissionCompleteGetRes> missionComplete =
                userMapper.findAllMissionCompleteByUserId(userId);

        List<ChallengeProgressGetRes> personal = new ArrayList<>();
        List<ChallengeProgressGetRes> competition = new ArrayList<>();
        int successCount = (success != null) ? success : 0;

        EnumChallengeRole cr = userInfo.getChallengeRole();

        UserInfoGetRes uInfo = UserInfoGetRes.builder()
                .userId(userId)
                .name(userInfo.getName())
                .nickName(userInfo.getNickName())
                .pic(userInfo.getPic())
                .xp(userInfo.getXp())
                .challengeRole(cr)
                .point(userInfo.getPoint()).build();



        for (ChallengeProgressGetRes challengeProgressGetRes : monthly) {
            switch (challengeProgressGetRes.getType()) {
                case "personal" -> personal.add(challengeProgressGetRes);
                case "competition" -> competition.add(challengeProgressGetRes);
            }
        }

        return ChallengeHomeGetRes.builder()
                .user(uInfo)
                .success(successCount)
                .dailyMission(daily)
                .weeklyChallenge(weekly)
                .competitionChallenge(competition)
                .personalChallenge(personal)
                .missionComplete(missionComplete)
                .build();
    }
}


