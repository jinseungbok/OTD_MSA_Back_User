package com.otd.otd_user.application.user;

import com.otd.configuration.feignclient.LifeFeignClient;
import com.otd.otd_challenge.application.challenge.ChallengeService;
import com.otd.otd_challenge.application.challenge.model.detail.ChallengeProgressGetReq;
import com.otd.otd_challenge.application.challenge.model.home.ChallengeHomeGetRes;
import com.otd.otd_user.application.email.model.PasswordChangeReq;
import com.otd.otd_user.application.email.model.PasswordResetReq;
import com.otd.otd_user.application.user.model.*;
import com.otd.configuration.jwt.JwtTokenManager;
import com.otd.configuration.model.ResultResponse;
import com.otd.configuration.model.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/OTD/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtTokenManager jwtTokenManager;
    private final PointService pointService;
    private final ChallengeService challengeService;
    private final UserRepository userRepository;
    private final LifeFeignClient lifeFeignClient;

    @Value("${constants.profile.base-url}")
    private String profileBaseUrl;

    @PostMapping(
            value = "/join",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResultResponse<?> join(
            @Valid @RequestPart("req") UserJoinReq req,
            @RequestPart(value = "pic", required = false) MultipartFile pic,
            HttpServletRequest request) {

        // IP 주소 추출
        String ipAddress = getClientIp(request);
        req.setIpAddress(ipAddress);

        // User-Agent 추출
        String userAgent = request.getHeader("User-Agent");
        req.setUserAgent(userAgent);

        log.info("회원가입 요청 - uid: {}, IP: {}, pic: {}",
                req.getUid(), ipAddress, pic != null ? pic.getOriginalFilename() : "없음");

        userService.join(req, pic);
        return new ResultResponse<>("회원가입 성공", 1);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @PostMapping("/login")
    public ResultResponse<?> login(@Valid @RequestBody UserLoginReq req
            , HttpServletRequest request
            , HttpServletResponse response) {
        log.info("로그인 요청: {}", req.getUid());
        UserLoginDto userloginDto = userService.login(req);
        String refreshToken = jwtTokenManager.issue(response, userloginDto.getJwtUser());
        userService.updateRefreshToken(userloginDto.getUserLoginRes().getUserId(), refreshToken);
        // ip & userAgent 추출
        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        log.info("로그인 성공 : userId {}, ip {}, userAgent {}",
                userloginDto.getUserLoginRes().getUserId(), ipAddress, userAgent);
        // 로그인 로그 저장
        userService.saveLoginLog(userloginDto.getUserLoginRes().getUserId(), ipAddress, userAgent);

        return new ResultResponse<>("로그인 성공", userloginDto.getUserLoginRes());
    }

    @PostMapping("/reset-password")
    public ResultResponse<?> resetPassword(@Valid @RequestBody PasswordResetReq req) {
        log.info("비밀번호 재설정 요청: {}", req.getEmail());
        userService.resetPassword(req);
        return new ResultResponse<>("비밀번호가 성공적으로 변경되었습니다.", null);
    }

    @PatchMapping("/password")
    public ResultResponse<?> changePassword(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody PasswordChangeReq req) {
        userService.changePassword(userPrincipal.getSignedUserId(), req);
        return new ResultResponse<>("비밀번호가 성공적으로 변경되었습니다.", null);
    }

    @GetMapping("/check-uid/{uid}")
    public ResultResponse<?> checkUidDuplicate(@PathVariable String uid) {
        boolean isAvailable = userService.isUidAvailable(uid);
        return new ResultResponse<>("아이디 중복 확인", Map.of("isAvailable", isAvailable));
    }

    @GetMapping("/check-nickname/{nickname}")
    public ResultResponse<?> checkNicknameDuplicate(@PathVariable String nickname) {
        boolean isAvailable = userService.isNicknameAvailable(nickname);
        return new ResultResponse<>("닉네임 중복 확인", Map.of("isAvailable", isAvailable));
    }

    @PatchMapping("/nickname")
    public ResponseEntity<?> updateNickname(
            @Valid @RequestBody NicknameUpdateDto request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        if (!userService.isNicknameAvailable(request.getNickname())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "이미 사용중인 닉네임입니다."));
        }
        userService.updateNickname(userPrincipal.getSignedUserId(), request.getNickname());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "닉네임이 변경되었습니다.",
                "data", Map.of("nickname", request.getNickname())
        ));
    }

    @PostMapping("/logout")
    public ResultResponse<?> logout(HttpServletResponse response) {
        log.info("로그아웃 요청");
        jwtTokenManager.logout(response);
        return new ResultResponse<>("로그아웃 성공", null);
    }

    @PostMapping("/reissue")
    public ResultResponse<?> reissue(HttpServletResponse response, HttpServletRequest request) {
        log.info("reissue request: {}  respone: {}", request,  response);
        jwtTokenManager.reissue(request, response);
        return new ResultResponse<>("AccessToken 재발행 성공", null);
    }

    @GetMapping("/profile")
    public ResultResponse<?> getProfileUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        UserProfileGetRes userProfileGetRes = userService.getProfileUser(userPrincipal.getSignedUserId());
        return new ResultResponse<>("프로파일 유저 정보", userProfileGetRes);
    }

    @PatchMapping("/profile/pic")
    public ResultResponse<?> patchProfilePic(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestPart MultipartFile pic) {
        String savedFileName = userService.patchProfilePic(userPrincipal.getSignedUserId(), pic);
        return new ResultResponse<>("프로파일 사진 수정 완료", savedFileName);
    }

    @DeleteMapping("/profile/pic")
    public ResultResponse<?> deleteProfilePic(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        userService.deleteProfilePic(userPrincipal.getSignedUserId());
        return new ResultResponse<>("프로파일 사진 삭제 완료", null);
    }

    @GetMapping("/pointhistory/{userId}")
    public ResultResponse<?> getPointHistory(@PathVariable Long userId) {
        PointHistoryResponseDTO response = pointService.getPointHistory(userId);
        return new ResultResponse<>("포인트 내역 조회 성공", response);
    }

    @DeleteMapping("/account")
    public ResultResponse<?> deleteUser(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletResponse response) {
        log.info("회원 탈퇴 요청 - userId: {}", userPrincipal.getSignedUserId());
        int result = userService.deleteById(userPrincipal.getSignedUserId());
        jwtTokenManager.logout(response);
        return new ResultResponse<>("회원 탈퇴 완료", result);
    }
    //전체 미션 완료 내역 조회
    @GetMapping("/missions/complete")
    public ResultResponse<?> getAllMissionComplete(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        ChallengeProgressGetReq req = new ChallengeProgressGetReq();
        // 필요한 경우 날짜 설정
        ChallengeHomeGetRes response = pointService.getSelectedListAll(userPrincipal.getSignedUserId(), req);
        return new ResultResponse<>("미션 완료 내역 조회 성공", response);
    }

    // 포인트 조회
    @GetMapping("/{userId}/points")
    public void userPoints(@PathVariable Long userId) {
        userService.printUserPointMapping(userId);
    }
}