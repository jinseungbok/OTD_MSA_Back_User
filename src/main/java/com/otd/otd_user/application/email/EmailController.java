package com.otd.otd_user.application.email;

import com.otd.configuration.model.UserPrincipal;
import com.otd.otd_user.application.email.model.*;
import com.otd.configuration.model.ResultResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/OTD/email")
@RequiredArgsConstructor public class EmailController {
    private final EmailService emailService;

    /**
     * 회원가입용 이메일 인증코드 발송
     */
    @PostMapping("/send-verification")
    public ResultResponse<?> sendEmailVerification(@Valid @RequestBody EmailSendReq req) {
        log.info("회원가입용 이메일 인증코드 발송 요청: {}", req.getEmail());
        emailService.sendEmailVerificationCode(req.getEmail());
        return new ResultResponse<>("인증코드가 발송되었습니다.", null);
    }

    /**
     * 회원가입용 이메일 인증코드 검증
     */
    @PostMapping("/verify-code")
    public ResultResponse<?> verifyEmailCode(@Valid @RequestBody EmailVerifyReq req) {
        log.info("회원가입용 이메일 인증코드 검증 요청: {}", req.getEmail());
        boolean isValid = emailService.verifyEmailCode(req.getEmail(), req.getCode());
        if (isValid) {
            return new ResultResponse<>("이메일 인증이 완료되었습니다.", Map.of("verified", true));
        } else {
            return new ResultResponse<>("인증코드가 일치하지 않습니다.", Map.of("verified", false));
        }
    }
    /**
     * 이메일 변경용 인증코드 발송
     */
    @PostMapping("/send-email-update-code")
    public ResultResponse<?> sendEmailUpdateCode(
            @Valid @RequestBody EmailSendReq req,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("이메일 변경 인증코드 발송 요청: {} (사용자ID: {})",
                req.getEmail(), userPrincipal.getSignedUserId());
        emailService.sendEmailUpdateCode(req.getEmail(), userPrincipal.getSignedUserId());
        return new ResultResponse<>("이메일 변경 인증코드가 발송되었습니다.", null);
    }

    /**
     * 이메일 변경용 인증코드 검증
     */
    @PostMapping("/verify-email-update-code")
    public ResultResponse<?> verifyEmailUpdateCode(
            @Valid @RequestBody EmailVerifyReq req,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("이메일 변경 인증코드 검증 요청: {}", req.getEmail());
        boolean isValid = emailService.verifyEmailUpdateCode(req.getEmail(), req.getCode());
        if (isValid) {
            return new ResultResponse<>("인증이 완료되었습니다. 이메일을 변경할 수 있습니다.",
                    Map.of("verified", true));
        } else {
            return new ResultResponse<>("인증코드가 일치하지 않습니다.",
                    Map.of("verified", false));
        }
    }

    /**
     * 비밀번호 재설정용 이메일 인증코드 발송
     */
    @PostMapping("/send-password-reset")
    public ResultResponse<?> sendPasswordResetCode(@Valid @RequestBody EmailSendReq req) {
        log.info("비밀번호 재설정용 이메일 인증코드 발송 요청: {}", req.getEmail());
        emailService.sendPasswordResetCode(req.getEmail());
        return new ResultResponse<>("비밀번호 재설정 인증코드가 발송되었습니다.", null);
    }

    /**
     * 비밀번호 재설정용 인증코드 검증
     */
    @PostMapping("/verify-password-reset-code")
    public ResultResponse<?> verifyPasswordResetCode(@Valid @RequestBody EmailCodeVerifyReq req) {
        log.info("비밀번호 재설정용 인증코드 검증 요청: {}", req.getEmail());
        boolean isValid = emailService.verifyPasswordResetCode(req.getEmail(), req.getCode());
        if (isValid) {
            return new ResultResponse<>("인증이 완료되었습니다. 10분 내에 비밀번호를 변경해주세요.", Map.of("verified", true));
        } else {
            return new ResultResponse<>("인증코드가 일치하지 않습니다.", Map.of("verified", false));
        }
    }

    /**
     * 이메일 인증 상태 확인
     */
    @GetMapping("/verification-status/{email}")
    public ResultResponse<?> getEmailVerificationStatus(@PathVariable String email) {
        boolean isVerified = emailService.isEmailVerified(email);
        return new ResultResponse<>("이메일 인증 상태 조회", Map.of("email", email, "verified", isVerified));
    }

    /**
     * 문의하기 이메일 전송
     */
    @PostMapping("/sendInquiry")
    public ResultResponse<?> sendInquiryEmail(@Valid @RequestBody InquiryEmailReq req, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = userPrincipal.getSignedUserId();
        log.info("문의하기 처리 시작: 제목={}, 보낸이={}, 사용자ID={}", req.getSubject(), req.getSenderName(), userId);
        try {
            emailService.sendInquiryEmail(req, userId);
            return new ResultResponse<>("문의가 성공적으로 전송되었습니다.", Map.of("success", true, "timestamp", System.currentTimeMillis()));
        } catch (Exception e) {
            log.error("문의하기 이메일 전송 실패", e);
            return new ResultResponse<>("문의 전송에 실패했습니다. 잠시 후 다시 시도해주세요.", Map.of("success", false));
        }
    }



    @PatchMapping("/email-update")
    public ResponseEntity<?> updateEmail(
            @Valid @RequestBody EmailUpdateDto request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        // 1. 이메일 중복 체크
        if (!emailService.isEmailAvailable(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "이미 사용중인 이메일입니다."));
        }

        // 2. 인증 완료 여부 확인 (새로 추가!)
        if (!emailService.canUpdateEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "이메일 인증이 필요합니다."));
        }

        // 3. 이메일 변경
        emailService.updateEmail(userPrincipal.getSignedUserId(), request.getEmail());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "이메일이 변경되었습니다.",
                "data", Map.of("email", request.getEmail())
        ));
    }
    /**
     * 비밀번호 재설정 (인증 완료 후)
     */
    @PatchMapping("/password-reset")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetDto request) {
        // 1. 인증 완료 여부 확인
        if (!emailService.canResetPassword(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "이메일 인증이 필요합니다."));
        }

        // 2. 비밀번호 변경
        emailService.resetPassword(request.getEmail(), request.getNewPassword());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "비밀번호가 변경되었습니다."
        ));
    }
    /**
     * 아이디 찾기용 이메일 인증코드 발송
     */
    @PostMapping("/send-find-id-code")
    public ResultResponse<?> sendFindIdCode(@Valid @RequestBody EmailSendReq req) {
        log.info("아이디 찾기용 이메일 인증코드 발송 요청: {}", req.getEmail());
        emailService.sendFindIdCode(req.getEmail());
        return new ResultResponse<>("아이디 찾기 인증코드가 발송되었습니다.", null);
    }

    /**
     * 아이디 찾기용 인증코드 검증 및 아이디 반환
     */
    @PostMapping("/verify-find-id-code")
    public ResponseEntity<?> verifyFindIdCode(@Valid @RequestBody EmailCodeVerifyReq req) {
        log.info("아이디 찾기용 인증코드 검증 요청: {}", req.getEmail());

        boolean isValid = emailService.verifyFindIdCode(req.getEmail(), req.getCode());

        if (!isValid) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "인증코드가 일치하지 않습니다."));
        }

        // 인증 성공 시 아이디 조회
        String userId = emailService.getUserIdByEmail(req.getEmail());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "userId", userId,
                "message", "인증이 완료되었습니다."
        ));
    }

    /**
     * 사용자 문의 내역 조회
     */
    @GetMapping("/inquiry/my-inquiries")
    public ResultResponse<?> getMyInquiries(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = userPrincipal.getSignedUserId();
        log.info("문의 내역 조회 요청: 사용자ID={}", userId);

        List<InquiryListRes> inquiries = emailService.getMyInquiries(userId);
        return new ResultResponse<>("문의 내역 조회 성공", inquiries);
    }

    /**
     * 특정 문의 상세 조회
     */
    @GetMapping("/inquiry/{inquiryId}")
    public ResultResponse<?> getInquiryDetail(
            @PathVariable Long inquiryId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = userPrincipal.getSignedUserId();
        log.info("문의 상세 조회 요청: ID={}, 사용자ID={}", inquiryId, userId);

        InquiryDetailRes inquiry = emailService.getInquiryDetail(inquiryId, userId);
        return new ResultResponse<>("문의 상세 조회 성공", inquiry);
    }
}

