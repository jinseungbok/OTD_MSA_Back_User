package com.otd.otd_admin.application.admin;

import com.otd.configuration.constants.ConstFile;
import com.otd.configuration.enumcode.EnumConvertUtils;
import com.otd.configuration.enumcode.model.EnumChallengeRole;
import com.otd.configuration.enumcode.model.EnumInquiryStatus;
import com.otd.configuration.enumcode.model.EnumUserRole;
import com.otd.configuration.feignclient.LifeFeignClient;
import com.otd.configuration.model.ResultResponse;
import com.otd.configuration.util.MyFileManager;
import com.otd.otd_admin.application.admin.Repository.AdminInquiryRepository;
import com.otd.otd_admin.application.admin.Repository.AdminPointRepository;
import com.otd.otd_admin.application.admin.Repository.AdminUserLoginLogRepository;
import com.otd.otd_admin.application.admin.Repository.AdminUserRepository;
import com.otd.otd_admin.application.admin.model.*;
import com.otd.otd_admin.application.admin.model.dashboard.AdminDashBoardChallengeDto;
import com.otd.otd_admin.application.admin.model.dashboard.AdminDashBoardInquiryDto;
import com.otd.otd_admin.application.admin.model.dashboard.AdminDashBoardPointDto;
import com.otd.otd_admin.application.admin.model.dashboard.AdminDashBoardUserDto;
import com.otd.otd_admin.application.admin.model.statistics.*;
import com.otd.otd_challenge.application.challenge.Repository.*;
import com.otd.otd_challenge.entity.ChallengeDefinition;
import com.otd.otd_challenge.entity.ChallengePointHistory;
import com.otd.otd_challenge.entity.ChallengeProgress;
import com.otd.otd_pointshop.entity.PurchaseHistory;
import com.otd.otd_pointshop.repository.PurchaseHistoryRepository;
import com.otd.otd_user.application.email.InquiryRepository;
import com.otd.otd_user.application.user.UserRepository;
import com.otd.otd_user.application.user.model.UserRoleRepository;
import com.otd.otd_user.entity.Inquiry;
import com.otd.otd_user.entity.User;
import com.otd.otd_user.entity.UserRole;
import com.otd.otd_user.entity.UserRoleIds;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    private final ChallengeProgressRepository challengeProgressRepository;
    private final ChallengeDefinitionRepository challengeDefinitionRepository;
    private final ChallengePointRepository challengePointRepository;
    private final UserRoleRepository userRoleRepository;
    private final AdminMapper adminMapper;
    private final AdminUserRepository adminUserRepository;
    private final AdminPointRepository adminPointRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminInquiryRepository adminInquiryRepository;
    private final MyFileManager myFileManager;
    private final LifeFeignClient lifeFeignClient;
    private final ConstFile constFile;
    private final AdminUserLoginLogRepository adminUserLoginLogRepository;
    private final InquiryRepository inquiryRepository;
    private final PurchaseHistoryRepository purchaseHistoryRepository;

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public List<ChallengeDefinition> getChallenges() {
        return challengeDefinitionRepository.findAll();
    }

    public List<ChallengePointHistory> getPointHistory() {
        return adminPointRepository.findAll();
    }

    public List<AdminPurchaseDto> getPurchaseHistory() {
        return adminMapper.getAdminPurchaseHistory();
    }

    public List<AdminChallengeProgress> getChallengeProgress(Long id) {
        LocalDate date = LocalDate.now();
        List<AdminChallengeProgress> result = adminMapper.findByCdId(id, date);
        for (AdminChallengeProgress acp : result) {
            String format =  acp.getCdGoal() + acp.getCdUnit();
            acp.setGoal(format);

            String format2 = acp.getTotalRecord().intValue() + acp.getCdUnit();
            acp.setRecord(format2);
        }
        return result;
    }

    public List<Inquiry> getInquiry() {
        return adminInquiryRepository.findAll();
    }

    public Inquiry getInquiryDetail(Long inquiryId) {
        return adminInquiryRepository.findById(inquiryId).orElseThrow(() -> new IllegalArgumentException("해당 문의글이 존재하지 않습니다. id=" + inquiryId));
    }

    public ResultResponse<?> putInquiry(AdminInquiryReq req) {
        Inquiry inquiry = inquiryRepository.findById(req.getId());
        User user = userRepository.findByUserId(req.getAdminId());

            inquiry.setReply(req.getReply());
            inquiry.setReplyAt(LocalDateTime.now());
            inquiry.setAdminId(user);
            inquiry.setStatus(EnumInquiryStatus.RESOLVED);
            inquiryRepository.save(inquiry);
        return new ResultResponse<>("문의 답변이 완료되었습니다", inquiry);
    }

    // 대시보드 유저
    public AdminDashBoardUserDto getUserDashBoard(){
        AdminDashBoardUserDto dto = new AdminDashBoardUserDto();

        // 전체 사용자 수
        int userCount = adminUserRepository.countUser();
        // 최근 회원가입자 top5
        List<User> recentJoinTop5 = adminUserRepository.findTop5ByOrderByCreatedAtDesc();
        // 오늘 로그인한 회원 수
        int todayLogin = adminUserLoginLogRepository.countTodayLogin();
        dto.setTotalUserCount(userCount);
        dto.setRecentJoinUser(recentJoinTop5);
        dto.setTodayLoginUserCount(todayLogin);
        return dto;
    }
    // 대시보드 챌린지
    public AdminDashBoardChallengeDto getChallengeDashBoard(){
        AdminDashBoardChallengeDto dto = new AdminDashBoardChallengeDto();

        // 전체 챌린지 개수
        int totalCount = challengeDefinitionRepository.countAllChallenge();
        // 참여자 수 top5 챌린지
        List<ChallengeDefinition> top5 = adminMapper.findTop5ByParticipationRate();
        // 실패율 top3 챌린지
        List<ChallengeDefinition> failTop3 = adminMapper.findTop3ByFailRate();
        // 전체 평균 성공률
        Double avgSuccessRate = adminMapper.findAverageSuccessRate();

        dto.setTotalChallengeCount(totalCount);
        dto.setParticipantTop5Challenge(top5);
        dto.setFailTop3Challenge(failTop3);
        dto.setSuccessRate(avgSuccessRate);

        return dto;
    }

    // 대시보드 포인트
    public AdminDashBoardPointDto getPointDashBoard(){
        AdminDashBoardPointDto dto = new AdminDashBoardPointDto();

        // 전체 포인트
        int totalPoint = adminUserRepository.sumPoint();
        // 포인트 보유 top5 유저
        List<User> top5User = adminMapper.findTop5ByPoint();

        dto.setTotalPoint(totalPoint);
        dto.setPointTop5User(top5User);

        return dto;
    }

    // 대시보드 문의
    public AdminDashBoardInquiryDto getInquiryDashBoard(){
        AdminDashBoardInquiryDto dto = new AdminDashBoardInquiryDto();

        // 총 문의 건수
        int totalInquiry = adminInquiryRepository.countAllInquiry();
        // 미답변 건수
        int unansweredCount = adminInquiryRepository.countByStatus(EnumInquiryStatus.PENDING);
        // 최근 글 5개
        List<Inquiry> recent5Inquiry = adminMapper.findRecent5Inquiry();
        for (Inquiry inquiry : recent5Inquiry) {
            String statusCode = inquiry.getStatus().getCode();
            inquiry.setStatusCode(statusCode);
        }
        // 평균 문의 처리 시간
        Double avgResponseTime = adminMapper.getAvgInquiryRepliedTime();
        // 문의 답변율
        Double responseRate = adminMapper.getInquiryRepliedRate();

        dto.setTotalInquiryCount(totalInquiry);
        dto.setUnansweredInquiryCount(unansweredCount);
        dto.setRecentInquiryList(recent5Inquiry);
        dto.setAvgRepliedTime(avgResponseTime);
        dto.setResponseRate(responseRate);

        return dto;
    }

    // 유저 챌린지 진행 기록, 포인트 지급 내역
    public AdminUserDetailGetRes getUserDetail(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        List<ChallengeProgress> cp = challengeProgressRepository.findByUserId(userId);
        List<ChallengePointHistory> ch = challengePointRepository.findByUserId(userId);
        List<AdminPurchaseDto> pd = adminMapper.getUserPurchaseList(userId);
        return AdminUserDetailGetRes.builder().
            challengeProgress(cp).challengePointHistory(ch).pointPurchases(pd).build();
    }

    // 통계 유저
    public AdminStatisticsUserDto getUserStatistics(){
        AdminStatisticsUserDto dto = new AdminStatisticsUserDto();

        // 성별 분포
        List<GenderCountRes> genderCount = adminUserRepository.countUserByGender();
        // 연령대 비율
        List<AgeCountRes> ageCount = adminMapper.groupByAge();
        // 6개월간 회원가입수
        List<SignInCountRes> signInCount = adminMapper.countBySignIn();

        dto.setGenderCount(genderCount);
        dto.setAgeCount(ageCount);
        dto.setSignInCount(signInCount);

        return dto;
    }

    // 통계 챌린지
    public AdminStatisticsChallengeDto getChallengeStatistics(){
        AdminStatisticsChallengeDto dto = new AdminStatisticsChallengeDto();

        // 챌린지 티어 비율
        List<TierCountRes> tierCount = adminMapper.countByTier();
        // 챌린지 타입별 성공률 , 타입별 갯수
        List<ChallengeSuccessRateCountRes> challengeSuccessRateCount = adminMapper.countByChallengeType();
        // 6개월간 챌린지 참여자 수
        List<ChallengeParticipationCountRes> challengeParticipationCount = adminMapper.countByChallengeParticipation();
        // 챌린지 타입별 비율
        List<ChallengeTypeCountRes> challengeTypeCount = adminMapper.countByChallengeTypeRatio();

        dto.setTierCount(tierCount);
        dto.setChallengeSuccessRateCount(challengeSuccessRateCount);
        dto.setChallengeParticipationCount(challengeParticipationCount);
        dto.setChallengeTypeCount(challengeTypeCount);

        return dto;
    }

    // 통계 문의
    public AdminStatisticsInquiryDto getInquiryStatistics() {
        AdminStatisticsInquiryDto dto = new AdminStatisticsInquiryDto();

        // 문의 답변율
        Double responseRate = adminMapper.getInquiryRepliedRate();
        // 6개월간 문의건수
        List<InquiryCountRes> inquiryCount = adminMapper.countByInquiry();

        dto.setResponseRate(responseRate);
        dto.setInquiryCount(inquiryCount);

        return dto;
    }

    public ResultResponse<?> putUserDetail(AdminUserPutReq req){
        User user = userRepository.findByUserId(req.getUserId());
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다.");
        }
        user.setName(req.getName());
        user.setNickName(req.getNickName());
        user.setPoint(req.getPoint());
        user.setXp(req.getXp());
        user.setUid(req.getUid());
        user.setPic(req.getPic());
        if (req.getUpw() != null && !req.getUpw().isBlank()) {
            user.setUpw(passwordEncoder.encode(req.getUpw()));
        }
        // 기존 역할 조회
        UserRole exist = userRoleRepository.findByUserId(user.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user_role 정보가 없습니다."));

        EnumUserRole newRole = req.getUserRoles();
        EnumChallengeRole newChallenge = req.getChallengeRole();

        EnumUserRole currentRole = exist.getUserRoleIds().getRoleCode();
        EnumChallengeRole currentChallenge = exist.getUserRoleIds().getChallengeCode();

        // 값이 다르면 삭제 후 새로 추가
        if (!currentRole.equals(newRole) || !currentChallenge.equals(newChallenge)) {
            user.getUserRoles().remove(exist);   // 관계 제거
            userRoleRepository.delete(exist);

            UserRoleIds ids = new UserRoleIds(user.getUserId(), newRole, newChallenge);
            UserRole newUserRole = new UserRole(ids, user);
            user.getUserRoles().add(newUserRole);
            userRoleRepository.save(newUserRole);
        }

        userRepository.save(user);
        return new ResultResponse<>("유저 정보가 수정되었습니다.", user.getUserId());
    }

    public AdminChallengeDto addChallenge(AdminChallengeDto dto) {
        ChallengeDefinition cd = ChallengeDefinition.builder()
                .cdName(dto.getCdName())
                .cdType(dto.getCdType())
                .cdGoal(dto.getCdGoal())
                .cdUnit(dto.getCdUnit())
                .cdReward(dto.getCdReward())
                .xp(dto.getXp())
                .tier(dto.getTier())
                .cdImage(dto.getCdImage())
                .build();

        challengeDefinitionRepository.save(cd);
        dto.setCdId(cd.getCdId());
        return dto;
    }

    public String saveChallengeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "파일이 없습니다");
        }
        return myFileManager.saveChallengeImage(file);
    }

    public AdminChallengeDto modifyChallenge(AdminChallengeDto dto
            , MultipartFile file) {
        ChallengeDefinition cd = challengeDefinitionRepository.findByCdId(dto.getCdId());
        if (cd == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 챌린지입니다");
        }
        // 새 이미지가 있으면 교체
        if (file != null && !file.isEmpty()) {
            // 새 파일 저장
            String newFileName = saveChallengeImage(file);

            // 기존 파일 삭제
            if (cd.getCdImage() != null) {
                File oldFile = new File(constFile.uploadDirectory + "/challenge/" + cd.getCdImage());
                if (oldFile.exists() && !oldFile.delete()) {
                    log.warn("기존 이미지 삭제 실패: {}", oldFile.getAbsolutePath());
                }
            }

            dto.setCdImage(newFileName);
        } else {
            // 파일 안 넣으면 기존 이미지 유지
            dto.setCdImage(cd.getCdImage());
        }
        cd.update(dto);
        return dto;
    }

    public ResultResponse<?> removeUser(Long userId) {
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "유저 없음");
        }

        // life 서버에 삭제 요청
        ResultResponse<?> lifeResponse = lifeFeignClient.deleteUserData(userId);

        // cascade로 묶여있는 관련 데이터까지 삭제
        userRepository.delete(user);

        return new ResultResponse<>(
                "유저 삭제 완료 (Life 서버 응답: " + lifeResponse.getMessage() + ")", userId
        );
    }

    public ResultResponse<?> removeChallenge(Long cdId) {
        ChallengeDefinition cd = challengeDefinitionRepository.findByCdId(cdId);
        if (cd == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 챌린지입니다");
        }

        if (cd.getCdImage() != null) {
            String filePath = constFile.uploadDirectory + "/challenge/" + cd.getCdImage();
            File file = new File(filePath);

            if (file.exists()) {
                boolean deleted = file.delete();
                if (!deleted) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "챌린지 이미지 삭제에 실패했습니다");
                }
            }
        }
        int result = challengeDefinitionRepository.deleteByCdId(cdId);
        if (result != 1) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "챌린지 삭제에 실패했습니다.");
        } 
        return new ResultResponse<>("챌린지 및 이미지가 삭제가 되었습니다.", result);
    }
}
