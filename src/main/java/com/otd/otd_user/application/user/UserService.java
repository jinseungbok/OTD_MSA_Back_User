package com.otd.otd_user.application.user;

import com.otd.configuration.util.MyFileManager;
import com.otd.configuration.enumcode.model.EnumChallengeRole;
import com.otd.otd_pointshop.entity.Point;
import com.otd.otd_pointshop.entity.PointUser;
import com.otd.otd_pointshop.repository.PointRepository;
import com.otd.otd_user.application.email.EmailService;
import com.otd.otd_user.application.email.model.PasswordChangeReq;
import com.otd.otd_user.application.email.model.PasswordResetReq;
import com.otd.otd_user.application.term.TermsRepository;
import com.otd.otd_user.application.user.model.*;
import com.otd.configuration.enumcode.model.EnumUserRole;
import com.otd.configuration.model.JwtUser;
import com.otd.configuration.security.SignInProviderType;
import com.otd.configuration.util.ImgUploadManager;
import com.otd.otd_user.entity.User;
import com.otd.otd_user.entity.Terms;
import com.otd.otd_user.entity.UserAgreement;
import com.otd.otd_user.application.term.UserAgreementRepository;
import com.otd.otd_user.entity.UserLoginLog;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImgUploadManager imgUploadManager;
    private final EmailService emailService;
    private final MyFileManager myFileManager;
    private final TermsRepository termsRepository;
    private final UserAgreementRepository userAgreementRepository;
    private final UserLoginLogRepository userLoginLogRepository;
    private final PointRepository pointRepository;

    public boolean isUidAvailable(String uid) {
        return userMapper.countByUid(uid) == 0;
    }

    @Transactional
    public void join(UserJoinReq req, MultipartFile pic) {
        String hashedPassword = passwordEncoder.encode(req.getUpw());

        User user = User.builder()
                .providerType(SignInProviderType.LOCAL)
                .uid(req.getUid())
                .upw(hashedPassword)
                .nickName(req.getNickname())
                .email(req.getEmail())
                .name(req.getName())
                .phone(req.getPhone())
                .birthDate(req.getBirthDate())
                .gender(req.getGender())
                .onboardingCompleted(1)
                .build();

        EnumChallengeRole challengeRole = EnumChallengeRole.fromCode(req.getSurveyAnswers());
        if (req.getRoles() == null || req.getRoles().isEmpty()) {
            user.addUserRoles(List.of(EnumUserRole.USER_2), challengeRole);
        } else {
            user.addUserRoles(req.getRoles(), challengeRole);
        }

        User savedUser = userRepository.save(user);

        if(pic != null) {
            String savedFileName = myFileManager.saveProfilePic(savedUser.getUserId(), pic);
            savedUser.setPic(savedFileName);
        }

        // 약관 동의 처리 추가
        if (req.getAgreedTermsIds() != null && !req.getAgreedTermsIds().isEmpty()) {
            saveUserAgreements(savedUser, req.getAgreedTermsIds(), req.getIpAddress(), req.getUserAgent());
        }

        log.info("회원가입 완료 - userId: {}, email: {}", savedUser.getUserId(), savedUser.getEmail());
    }

    // 약관 동의 저장 메서드
    private void saveUserAgreements(User user, List<Long> termsIds, String ipAddress, String userAgent) {
        for (Long termsId : termsIds) {
            Terms terms = termsRepository.findById(termsId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "약관을 찾을 수 없습니다. ID: " + termsId));

            UserAgreement agreement = UserAgreement.builder()
                    .user(user)
                    .terms(terms)
                    .agreed(true)
                    .agreedAt(LocalDateTime.now())
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();

            userAgreementRepository.save(agreement);
        }
        log.info("약관 동의 저장 완료 - userId: {}, 동의 약관 수: {}", user.getUserId(), termsIds.size());
    }

    @Transactional
    public void updateRefreshToken(Long userId, String refreshToken) {
        userRepository.updateRefreshToken(userId, refreshToken);
    }

    @Transactional
    public UserLoginDto login(UserLoginReq req) {
        User user = userRepository.findByUidAndProviderType(req.getUid(), SignInProviderType.LOCAL);
        if(user == null || !passwordEncoder.matches(req.getUpw(), user.getUpw())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "아이디/비밀번호를 확인해 주세요.");
        }
        userRepository.updateLastLoginByUserId(user.getUserId(), LocalDateTime.now());

        List<EnumUserRole> roles = user.getUserRoles().stream()
                .map(item -> item.getUserRoleIds().getRoleCode()).toList();
        log.info("roles: {}", roles);
        JwtUser jwtUser = new JwtUser(user.getUserId(), roles);
        EnumUserRole userRole = user.getUserRoles().stream()
                .map(item -> item.getUserRoleIds().getRoleCode()).findFirst().orElse(null);
        EnumChallengeRole challengeRoles = user.getUserRoles().stream()
                .map(item -> item.getUserRoleIds().getChallengeCode())
                .findFirst().orElse(null);

        UserLoginRes userLoginRes = UserLoginRes.builder()
                .userId(user.getUserId())
                .nickName(user.getNickName())
                .pic(user.getPic())
                .point(user.getPoint())
                .xp(user.getXp())
                .gender(user.getGender())
                .age(user.getAge())
                .email(user.getEmail())
                .challengeRole(challengeRoles)
                .userRole(userRole)
                .onboardingCompleted(user.getOnboardingCompleted())
                .build();

        return UserLoginDto.builder()
                .jwtUser(jwtUser)
                .userLoginRes(userLoginRes)
                .build();
    }

    //로그 저장
    @Transactional
    public void saveLoginLog(Long userId, String ip, String userAgent){
        User user = userRepository.findByUserId(userId);
        UserLoginLog log = UserLoginLog.builder()
                .user(user)
                .loginDate(LocalDateTime.now())
                .ipAddress(ip)
                .userAgent(userAgent)
                .build();
        userLoginLogRepository.save(log);
    }

    //비밀번호 재설정(비밀번호 찾기)
    @Transactional
    public void resetPassword(PasswordResetReq req) {
        // 1. 비밀번호 재설정 권한 확인
        if (!emailService.canResetPassword(req.getEmail())) {
            throw new IllegalArgumentException("비밀번호 재설정 권한이 없습니다. 이메일 인증을 먼저 완료해주세요.");
        }

        // 2. 사용자 조회
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 이메일입니다."));

        // 3. 새 비밀번호 해싱 및 저장
        String hashedPassword = passwordEncoder.encode(req.getNewPassword());
        user.setUpw(hashedPassword);

        // 4. 비밀번호 재설정 권한 삭제
        emailService.removePasswordResetPermission(req.getEmail());

        log.info("비밀번호 재설정 완료: {}", req.getEmail());
    }
    @Transactional
    public void changePassword(long userId, PasswordChangeReq req) {
        // 새 비밀번호와 확인 비밀번호 일치 확인
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new IllegalArgumentException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 사용자입니다."));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getUpw())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }

        // 새 비밀번호 해싱 및 저장
        String hashedPassword = passwordEncoder.encode(req.getNewPassword());
        user.setUpw(hashedPassword);

        log.info("비밀번호 변경 완료: 사용자 ID {}", userId);
    }


    public boolean isNicknameAvailable(String nickname) {
        return userMapper.countByNickname(nickname) == 0;
    }


    public UserProfileGetRes getProfileUser(long signedUserId) {
        return userMapper.findProfileByUserId(signedUserId);
    }

    @Transactional
    public String patchProfilePic(long signedUserId, MultipartFile pic) {
        User user = userRepository.findById(signedUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 사용자입니다."));

        myFileManager.removeProfileDirectory(signedUserId);
        String savedFileName = myFileManager.saveProfilePic(signedUserId, pic);

        user.setPic(savedFileName);
        userRepository.save(user);

        log.info("프로필 사진 업데이트 완료 - userId: {}, pic: {}", signedUserId, savedFileName);

        return savedFileName;
    }

    @Transactional
    public void deleteProfilePic(long signedUserId) {
        User user = userRepository.findById(signedUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 사용자입니다."));
        imgUploadManager.removeProfileDirectory(signedUserId);
        user.setPic(null);
    }

    @Transactional
    public void updateNickname(Long userId, String nickname) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "사용자를 찾을 수 없습니다."));

        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임을 입력해주세요.");
        }
        if (nickname.length() < 2 || nickname.length() > 10) {
            throw new IllegalArgumentException("닉네임은 2~10자여야 합니다.");
        }
        if (user.getNickName().equals(nickname)) {
            throw new IllegalArgumentException("현재 닉네임과 동일합니다.");
        }

        if (!isNicknameAvailable(nickname)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용중인 닉네임입니다.");
        }

        user.setNickName(nickname);

        log.info("닉네임 변경 완료 - userId: {}, 새 닉네임: {}", userId, nickname);
    }
    @Transactional
    public void updateEmail(Long userId, String email) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "사용자를 찾을 수 없습니다."));

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일을 입력해주세요.");
        }

        // 이메일 형식 검증
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!email.matches(emailRegex)) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다.");
        }

        // 현재 이메일과 동일한지 확인
        if (user.getEmail().equals(email)) {
            throw new IllegalArgumentException("현재 이메일과 동일합니다.");
        }

        // 이메일 중복 확인
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용중인 이메일입니다.");
        }

        // 이메일 인증 확인
        if (!emailService.canResetPassword(email)) {
            throw new IllegalArgumentException("이메일 인증을 완료해주세요.");
        }

        user.setEmail(email);
        userRepository.save(user);

        emailService.removePasswordResetPermission(email);

        log.info("이메일 변경 완료 - userId: {}, 새 이메일: {}", userId, email);
    }
    @Transactional
    public int deleteById(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "존재하지 않는 사용자입니다."));

        userRepository.delete(user);
        log.info("회원 삭제 완료 - userId: {}", userId);
        return 1;
    }

    @Transactional
    public void updateUserPoint(Long userId, int delta, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "존재하지 않는 사용자입니다."));

        // 현재 잔액 업데이트
        int newBalance = user.getPoint() + delta;
        if (newBalance < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "포인트가 부족합니다.");
        }
        user.setPoint(newBalance);

        // PointUser 로그 추가
        PointUser pointUser = PointUser.builder()
                .user(user)
                .pointBalance(newBalance)
                .pointDelta(delta)
                .reason(reason)
                .build();

        user.addPointUser(pointUser);
        userRepository.save(user);

        log.info("userId={} / Δ{} / 잔액={} / reason={}", userId, delta, newBalance, reason);
    }


    // 포인트 조회
    @Transactional
    public void printUserPointMapping(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "존재하지 않는 사용자입니다."));
        if (user.getPoints() == null) {
            System.out.println("해당 유저는 보유한 포인트 상품이 없습니다.");
            return;
        }
        for (Point p : user.getPoints()) {
            System.out.println("user_id = " + user.getUserId() +
                    ", point_id = " + p.getPointId() +
                    ", item_name = " + p.getPointItemName());
        }
    }

    // 포인트 등록
    @Transactional
    public void addPointToUser(Long userId, Point point) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "존재하지 않는 사용자입니다."));

        user.addPoint(point); // 양방향 연결
        userRepository.save(user);

        log.info("유저 {}에게 포인트 상품 '{}'이 추가되었습니다.", user.getUserId(), point.getPointItemName());
    }
}