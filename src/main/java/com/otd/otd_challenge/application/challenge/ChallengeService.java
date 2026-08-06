package com.otd.otd_challenge.application.challenge;

import com.otd.configuration.enumcode.model.EnumChallengeRole;
import com.otd.configuration.model.ResultResponse;
import com.otd.configuration.util.FormattedTime;
import com.otd.otd_challenge.application.challenge.Repository.*;
import com.otd.otd_challenge.application.challenge.model.*;
import com.otd.otd_challenge.application.challenge.model.detail.*;
import com.otd.otd_challenge.application.challenge.model.feignClient.ChallengeRecordDeleteReq;
import com.otd.otd_challenge.application.challenge.model.feignClient.ExerciseDataReq;
import com.otd.otd_challenge.application.challenge.model.feignClient.MealDataReq;
import com.otd.otd_challenge.application.challenge.model.home.*;
import com.otd.otd_challenge.application.challenge.model.settlement.ChallengeSettlementDto;
import com.otd.otd_challenge.application.challenge.model.settlement.ChallengeSuccessDto;
import com.otd.otd_challenge.entity.*;
import com.otd.otd_user.application.user.UserRepository;
import com.otd.otd_user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeService {
    private final ChallengeMapper challengeMapper;
    private final ChallengeDefinitionRepository challengeDefinitionRepository;
    private final ChallengeProgressRepository challengeProgressRepository;
    private final UserRepository userRepository;
    private final ChallengeSettlementRepository challengeSettlementRepository;
    private final ChallengeRecordRepository challengeRecordRepository;
    @Value("${constants.file.challenge}")
    private String imgPath;
    private void addImgPath(List<?> list) {
        for (Object o : list) {
            if (o instanceof ChallengeDefinitionGetRes cd) {
                cd.setImage(imgPath + "/" + cd.getImage());
            } else if (o instanceof ChallengeProgressGetRes cp) {
                cp.setImage(imgPath + "/" + cp.getImage());
            } else if (o instanceof ChallengeDefinition ecd) {
                ecd.setCdImage(imgPath + "/" + ecd.getCdImage());
            }
        }
    }
    public Map<String, Object> getChallengeList() {
        List<ChallengeDefinitionGetRes> res = challengeMapper.findAll();

        List<ChallengeDefinitionGetRes> personal = new ArrayList<>();
        List<ChallengeDefinitionGetRes> weekly = new ArrayList<>();
        List<ChallengeDefinitionGetRes> competition = new ArrayList<>();

        addImgPath(res);

        for (ChallengeDefinitionGetRes challengeDefinitionGetRes : res) {

            switch (challengeDefinitionGetRes.getType()) {
                case "personal" -> personal.add(challengeDefinitionGetRes);
                case "weekly" -> weekly.add(challengeDefinitionGetRes);
                case "competition" -> competition.add(challengeDefinitionGetRes);
            }
        }
        Map<String, List<ChallengeDefinitionGetRes>> grouping = competition.stream()
                .collect(Collectors.groupingBy(ChallengeDefinitionGetRes::getName));

        Map<String, Object> dto = new HashMap<>();

        dto.put("personalChallenge", personal);
        dto.put("weeklyChallenge", weekly);
        dto.put("competitionChallenge", grouping);

        log.info("ChallengeService getChallengeList dto: {}", dto);
        return dto;
    }
    // HomeInfo
    public ChallengeHomeGetRes getSelectedList(Long userId, ChallengeProgressGetReq req) {
        req.setUserId(userId);
        List<ChallengeProgressGetRes> monthly = challengeMapper.findAllMonthlyFromUserId(req);
        List<ChallengeProgressGetRes> weekly = challengeMapper.findAllWeeklyFromUserId(req);
        List<ChallengeDefinition> daily = challengeDefinitionRepository.findByCdType("daily");
        User userInfo = userRepository.findByUserId(userId);
        Integer success = challengeMapper.findSuccessChallenge(userId);
        List<ChallengeMissionCompleteGetRes> missionComplete =
                challengeMapper.findByUserIdAndMissionComplete(userId);
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

        addImgPath(weekly);
        addImgPath(daily);
        addImgPath(monthly);
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

    // CD쪽 DB tier와 챌린지 등급 매핑
    private void mapChallengeTier(List<ChallengeDefinitionGetRes> challenges, EnumChallengeRole userRole) {
        challenges.forEach(cd -> {
            EnumChallengeRole challengeRole = EnumChallengeRole.fromCode(cd.getTierCode()); // 문자열 그대로 변환
            cd.setTier(challengeRole);
            cd.setAvailable(userRole.isHigherOrEqual(challengeRole));
        });
    }
    public List<ChallengeDefinitionGetRes> getChallengeList(Long userId, ChallengeProgressGetReq req) {
        req.setUserId(userId);
        List<ChallengeDefinitionGetRes> res = challengeMapper.findByType(req);
        addImgPath(res);
        User user = userRepository.findByUserId(userId);
        EnumChallengeRole userRole = user.getChallengeRole();
        mapChallengeTier(res, userRole);

        return res;
    }

    public Map<String, List<ChallengeDefinitionGetRes>> getCompetitionList(Long userId, ChallengeProgressGetReq req) {
        req.setUserId(userId);
        List<ChallengeDefinitionGetRes> res = challengeMapper.findByTypeForCompetition(req);
        addImgPath(res);
        User user = userRepository.findByUserId(userId);
        EnumChallengeRole userRole = user.getChallengeRole();
        mapChallengeTier(res, userRole);
        return res.stream()
                    .collect(Collectors.groupingBy(ChallengeDefinitionGetRes::getName));
    }

    private void formatRankingRecords(List<ChallengeRankGetRes> rankingList, String unit) {
        for (ChallengeRankGetRes rank : rankingList) {
            if ("분".equals(unit)) {
                rank.setFormattedTotalRecord(FormattedTime.formatMinutes(rank.getTotalRecord()));
            } else {
                DecimalFormat df = (rank.getTotalRecord() % 1 == 0)
                        ? new DecimalFormat("0")
                        : new DecimalFormat("0.0");
                rank.setFormattedTotalRecord(df.format(rank.getTotalRecord()) + unit);
            }
        }
    }

    private void fetchName(List<ChallengeRankGetRes> list) {
        for (ChallengeRankGetRes rank : list) {
            if (rank.getNickName() == null) {
                rank.setNickName(rank.getName());
            }
        }
    }

    public ChallengeDetailPerGetRes getDetailPer(Long cdId, Long userId, ChallengeProgressGetReq req) {
        req.setCdId(cdId);
        req.setUserId(userId);
        ChallengeDefinition cd = challengeDefinitionRepository.findByCdId(cdId);
        req.setType(cd.getCdType());
        // 상세정보
        ChallengeDetailPerGetRes res = challengeMapper.findProgressByUserIdAndCdId(req);
        // top5
        List<ChallengeRankGetRes> top5Ranking = challengeMapper.findTop5Ranking(req);
        // 내 주위 랭킹
        List<ChallengeRankGetRes> aroundRanking = challengeMapper.findAroundMyRank(req);



        if (res.getGoal() > res.getTotalRecord()) {
            double percentage = ((double) res.getTotalRecord() / res.getGoal()) * 100;
            res.setPercent(Math.round(percentage * 10 ) / 10.0);
        } else {
            res.setPercent(100.0);
        }

        res.setFormattedFields();
        formatRankingRecords(top5Ranking, res.getUnit());
        formatRankingRecords(aroundRanking, res.getUnit());
        fetchName(top5Ranking);
        fetchName(aroundRanking);

        res.setTopRanking(top5Ranking);
        res.setAroundRanking(aroundRanking);
        return res;
    }

    @Transactional
    public ResultResponse<?> updateIsSuccess(Long cpId){
        int result = challengeProgressRepository.updateIsSuccess(cpId);
        return new ResultResponse<>("success", result);
    }

    public ChallengeDetailDayGetRes getDetailDay(Long cdId, Long userId, ChallengeProgressGetReq req) {
        req.setCdId(cdId);
        req.setUserId(userId);
        List<ChallengeDetailDayGetRes> res = challengeMapper.findDayByUserIdAndCdId(req);

        ChallengeDetailDayGetRes map = res.get(0);
        List<Integer> record = res.stream().map(ChallengeDetailDayGetRes::getDate).collect(Collectors.toList());
        if (map.getDate() == null) {
            map.setRecDate(new ArrayList<>());
        } else {
            map.setRecDate(record);
        }
        return map;
    }

    @Transactional
    public ResultResponse<?> saveMissionRecord(Long userId, ChallengeRecordMissionPostReq req){
        int result = challengeMapper.saveMissionRecordByUserIdAndCpId(userId, req.getCdId());
        ChallengeDefinition point = challengeDefinitionRepository.findByCdId(req.getCdId());
        User user = userRepository.findByUserId(userId);
        int newPoint = user.getPoint() + point.getCdReward();
        int newXp = user.getXp() + point.getXp();

        userRepository.addXpByUserId(newXp, userId);
        userRepository.addPointByUserId(newPoint, userId);
        EnumChallengeRole myRole = user.getChallengeRole();

        EnumChallengeRole newRole = tierService.checkTierUp(myRole, newXp);
        if (newRole != myRole) {
            challengeRoleRepository.updateChallengeRole(user.getUserId(), newRole);
        }
        return new ResultResponse<>("success", result);
    }

    public ResultResponse<?> saveChallenge(Long userId, ChallengePostReq req){
        User user = userRepository.findByUserId(userId);
        ChallengeDefinition cd = challengeDefinitionRepository.findByCdId(req.getCdId());

        LocalDate startDate = LocalDate.now();
        YearMonth ym = YearMonth.from(startDate);
        LocalDate endDate = ym.atEndOfMonth();

        if ("weekly".equals(req.getType())){
            endDate = startDate.with(DayOfWeek.SUNDAY);
            if (startDate.getDayOfWeek() == DayOfWeek.SUNDAY){
                endDate = startDate;
            }
        } else if ("competition".equals(req.getType())|| "personal".equals(req.getType())){
            endDate = ym.atEndOfMonth();
        }
        ChallengeProgress challengeProgress = ChallengeProgress.builder()
                .user(user)
                .challengeDefinition(cd)
                .startDate(startDate)
                .endDate(endDate)
                .build();


        challengeProgressRepository.save(challengeProgress);

        return new ResultResponse<>("저장 되었습니다.",challengeProgress);
    }

    // 정산 테스트
    private final ChallengeSettlementMapper challengeSettlementMapper;
    private final ChallengePointRepository challengePointRepository;
    private final TierService tierService;
    private final ChallengeRoleRepository challengeRoleRepository;
    @Transactional
    public ResultResponse<?> setSettlement(ChallengeSettlementDto dto){
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
        return new ResultResponse<>("정산완료", 1);
    }

    public List<MainHomeGetRes> getMainHomeChallenge(Long userId, MainHomGetReq req) {
        req.setUserId(userId);
        List<MainHomeGetRes> res = challengeMapper.findAllMyChallenge(req);

        for (MainHomeGetRes pr : res) {
            if (pr.getType().equals("personal")){
                // 이번달 총 일수 구하기
                LocalDate now  = LocalDate.now();
                int month = now.lengthOfMonth();
                int targetDays = 15;
                if (pr.getTotalRecord() >= targetDays) {
                    pr.setPercent(100.0);
                } else {
                    double percentage = ((double) pr.getTotalRecord() / targetDays) * 100;
                    pr.setPercent(Math.round(percentage * 10) / 10.0);
                }
                pr.setFormatedName(pr.getName() + "(" + pr.getGoal() + pr.getUnit() + ")");
            } else {
                if (pr.getGoal() > pr.getTotalRecord()) {
                    double percentage = ((double) pr.getTotalRecord() / pr.getGoal()) * 100;
                    pr.setPercent(Math.round(percentage * 10) / 10.0);
                } else {
                    pr.setPercent(100.0);
                }

                if (pr.getUnit().equals("분")){
                    pr.setFormatedName(pr.getName() + "(" + FormattedTime.formatMinutes(pr.getGoal()) + ")");
                } else {
                    pr.setFormatedName(pr.getName() + "(" + pr.getGoal() + pr.getUnit() + ")");
                }
            }
        }
        return res;
    }

    private static final int DEFAULT_PERSONAL_GOAL = 15;
    public int updateProgressEx(ExerciseDataReq req) {
        // 월간 개인챌린지 조회
        List<ChallengeProgress> personalProgresses =
                challengeProgressRepository.findActiveProgressByType(
                        req.getUserId(),
                        req.getToday()
                );
        // 운동 이름과 같은 챌린지 조회
        ChallengeProgress mapProgresses =
                challengeProgressRepository.findActiveProgress(
                        req.getUserId(),
                        req.getName(),
                        req.getToday()
                ).orElse(null);

        List<ChallengeProgress> progresses = new ArrayList<>();
        if (personalProgresses != null)progresses.addAll(personalProgresses);
        if (mapProgresses != null)progresses.add(mapProgresses);

        for (ChallengeProgress cp : progresses) {

            String cdName = cp.getChallengeDefinition().getCdName();
            String cdType = cp.getChallengeDefinition().getCdType();

            boolean exist = challengeRecordRepository
                    .existsByChallengeProgressAndRecDate(cp, req.getRecordDate());

            if (cdType.equals("personal")) {
                // 운동하기 챌린지인 경우: 어떤 운동이든 하루 한 번만 +1
                if (cdName.equals("운동하기")) {
                    if (!exist) {
                        ChallengeRecord cr = ChallengeRecord.builder()
                                .challengeProgress(cp)
                                .recDate(req.getRecordDate())
                                .recordId(req.getRecordId())
                                .recValue(1.0)
                                .build();
                        challengeRecordRepository.save(cr);

                        cp.setTotalRecord(cp.getTotalRecord() + 1);
                        if (cp.getTotalRecord() >= DEFAULT_PERSONAL_GOAL) {
                            cp.setSuccess(true);
                        }
                        challengeProgressRepository.save(cp);
                    }
                }
                // 일반 개인 챌린지 (계단오르기, 요가 등)
                else if (cdName.equals(req.getName())) {
                    if (!exist) {
                        ChallengeRecord cr = ChallengeRecord.builder()
                                .challengeProgress(cp)
                                .recDate(req.getRecordDate())
                                .recordId(req.getRecordId())
                                .recValue(1.0)
                                .build();
                        challengeRecordRepository.save(cr);

                        cp.setTotalRecord(cp.getTotalRecord() + 1);
                        if (cp.getTotalRecord() >= cp.getChallengeDefinition().getCdGoal()) {
                            cp.setSuccess(true);
                        }
                        challengeProgressRepository.save(cp);
                    }
                }
                else if (cdName.equals("칼로리 소비")){
                    if (!exist && req.getTotalKcal() >= cp.getChallengeDefinition().getCdGoal()){
                        ChallengeRecord cr = ChallengeRecord.builder()
                                .challengeProgress(cp)
                                .recDate(req.getRecordDate())
                                .recordId(req.getRecordId())
                                .recValue(300)
                                .build();
                        challengeRecordRepository.save(cr);

                        cp.setTotalRecord(cp.getTotalRecord() + 1);
                        if (cp.getTotalRecord() >= cp.getChallengeDefinition().getCdGoal()) {
                            cp.setSuccess(true);
                        }
                        challengeProgressRepository.save(cp);
                    }
                }
            } else {
                double newTotal = cp.getTotalRecord() + req.getRecord();
                cp.setTotalRecord(newTotal);

                double goal = cp.getChallengeDefinition().getCdGoal();
                if (newTotal >= goal) {
                    cp.setSuccess(true);
                }
                ChallengeRecord cr = ChallengeRecord.builder()
                        .challengeProgress(cp)
                        .recDate(req.getRecordDate())
                        .recordId(req.getRecordId())
                        .recValue(req.getRecord())
                        .build();

                challengeRecordRepository.save(cr);
                challengeProgressRepository.save(cp);
            }
        }
        return 1;
    }
    public int deleteRecord(ChallengeRecordDeleteReq req) {

        // 운동 이름과 같은 챌린지 조회
        ChallengeProgress mapProgresses =
                challengeProgressRepository.findActiveProgress(
                        req.getUserId(),
                        req.getName(),
                        req.getToday()
                ).orElse(null);

        if (mapProgresses != null &&
                mapProgresses.getChallengeDefinition() != null &&
                !"personal".equals(mapProgresses.getChallengeDefinition().getCdType())) {

            ChallengeRecord cr = challengeRecordRepository
                    .findByChallengeProgressAndRecordId(mapProgresses, req.getRecordId());

            if (cr != null) {
                mapProgresses.setTotalRecord(mapProgresses.getTotalRecord() - cr.getRecValue());
                challengeRecordRepository.delete(cr);
                if (mapProgresses.getTotalRecord() < mapProgresses.getChallengeDefinition().getCdGoal()) {
                    mapProgresses.setSuccess(false);
                }
                challengeProgressRepository.save(mapProgresses);
            }
        }

        //  개인 챌린지 조회
        List<ChallengeProgress> exerciseProgress =
                challengeProgressRepository.findActiveProgressByType(
                        req.getUserId(),
                        req.getToday()
                );

        // Personal 챌린지 삭제 처리
        for (ChallengeProgress cp : exerciseProgress) {
            if (cp == null) continue; // 안전장치
            String cdName = cp.getChallengeDefinition().getCdName();
            ChallengeRecord cr =
                    challengeRecordRepository.findByChallengeProgressAndRecDate(
                            cp, req.getRecordDate());
            if (req.getCount() == 0 && !cdName.equals("칼로리 소비")) {

                if (cr != null) { // null check 추가
                    cp.setTotalRecord(cp.getTotalRecord() - 1);
                    challengeRecordRepository.delete(cr);

                    if (cp.getTotalRecord() < DEFAULT_PERSONAL_GOAL) {
                        cp.setSuccess(false);
                    }
                    challengeProgressRepository.save(cp);
                }
            } else if ("칼로리 소비".equals(cdName)
                    && cp.getChallengeDefinition().getCdGoal() > req.getTotalKcal()) {

                if (cr != null) {
                    cp.setTotalRecord(cp.getTotalRecord() - 1);
                    challengeRecordRepository.delete(cr);

                    if (cp.getTotalRecord() < DEFAULT_PERSONAL_GOAL) {
                        cp.setSuccess(false);
                    }
                }
                challengeProgressRepository.save(cp);
            }
        }
        return 1;
    }

    public int updateProgressMeal(MealDataReq req) {
        List<ChallengeProgress> personalProgresses =
                challengeProgressRepository.findActiveProgressByType(
                        req.getUserId(),
                        req.getToday()
                );

        for (ChallengeProgress cp : personalProgresses) {
            String cdName = cp.getChallengeDefinition().getCdName();

            // 챌린지 이름 매칭 (정확히 일치해야 함)
            if (cdName.equals(req.getName())) {

                // 단백질 or 물 이 목표 이상인 경우 한 번 더 체크
                if (cp.getChallengeDefinition().getCdGoal() <= req.getValue()) {

                    // 오늘 날짜가 챌린지 기간 안이고, 기록이 아직 없는 경우
                    boolean exist = challengeRecordRepository
                            .existsByChallengeProgressAndRecDate(cp, req.getRecDate());

                    if (!exist && !cp.getStartDate().isAfter(req.getRecDate()) &&
                            !cp.getEndDate().isBefore(req.getRecDate())) {

                        ChallengeRecord cr = ChallengeRecord.builder()
                                .challengeProgress(cp)
                                .recValue(req.getValue())
                                .recDate(req.getRecDate())
                                .build();

                        challengeRecordRepository.save(cr);
                        cp.setTotalRecord(cp.getTotalRecord() + 1);

                        if (cp.getTotalRecord() >= DEFAULT_PERSONAL_GOAL) {
                            cp.setSuccess(true);
                        }
                        challengeProgressRepository.save(cp);
                    }
                }
            }
        }
        return 1;
    }
}
