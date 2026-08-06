package com.otd.otd_user.application.email;

import com.otd.configuration.enumcode.model.EnumInquiryStatus;
import com.otd.otd_user.application.email.model.InquiryDetailRes;
import com.otd.otd_user.application.email.model.InquiryEmailReq;
import com.otd.otd_user.application.email.model.InquiryListRes;
import com.otd.otd_user.application.user.UserRepository;
import com.otd.otd_user.entity.EmailVerification;
import com.otd.otd_user.entity.Inquiry;
import com.otd.otd_user.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    @Lazy
    private final JavaMailSender mailSender;
    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;
    private final InquiryRepository inquiryRepository;
    private final PasswordEncoder passwordEncoder;

    private static final int CODE_EXPIRY_MINUTES = 5;
    private static final int VERIFICATION_STATUS_EXPIRY_MINUTES = 30;
    private static final int PASSWORD_RESET_EXPIRY_MINUTES = 10;

    // ==================== 회원가입 관련 ====================

    /**
     * 회원가입용 이메일 인증코드 발송
     */
    @Transactional
    public void sendEmailVerificationCode(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        emailVerificationRepository.deleteByEmailAndType(email, EmailVerification.VerificationType.SIGNUP);

        String verificationCode = generateRandomCode();
        EmailVerification verification = new EmailVerification();
        verification.setEmail(email);
        verification.setCode(verificationCode);
        verification.setType(EmailVerification.VerificationType.SIGNUP);
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES));

        emailVerificationRepository.save(verification);
        sendVerificationEmail(email, verificationCode);

        log.info("회원가입용 이메일 인증코드 발송 완료: {}", email);
    }

    /**
     * 이메일 인증코드 검증 (회원가입용)
     */
    @Transactional
    public boolean verifyEmailCode(String email, String code) {
        Optional<EmailVerification> verificationOpt = emailVerificationRepository
                .findTopByEmailAndTypeOrderByCreatedAtDesc(email, EmailVerification.VerificationType.SIGNUP);

        if (verificationOpt.isEmpty() || !verificationOpt.get().canVerify() ||
                !verificationOpt.get().getCode().equals(code)) {
            return false;
        }

        EmailVerification verification = verificationOpt.get();
        verification.setVerified(true);
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(VERIFICATION_STATUS_EXPIRY_MINUTES));
        emailVerificationRepository.save(verification);

        log.info("이메일 인증 성공: {}", email);
        return true;
    }

    /**
     * 이메일 인증 상태 확인
     */
    public boolean isEmailVerified(String email) {
        Optional<EmailVerification> verificationOpt = emailVerificationRepository
                .findVerifiedByEmailAndType(email, EmailVerification.VerificationType.SIGNUP, LocalDateTime.now());
        return verificationOpt.isPresent();
    }

    /**
     * 이메일 인증 상태 삭제 (회원가입 완료 후)
     */
    @Transactional
    public void removeEmailVerificationStatus(String email) {
        emailVerificationRepository.deleteByEmailAndType(email, EmailVerification.VerificationType.SIGNUP);
    }

    // ==================== 비밀번호 재설정 관련 ====================

    /**
     * 비밀번호 재설정용 이메일 인증코드 발송
     */
    @Transactional
    public void sendPasswordResetCode(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("등록되지 않은 이메일입니다.");
        }

        emailVerificationRepository.deleteByEmailAndType(email, EmailVerification.VerificationType.PASSWORD_RESET);

        String resetCode = generateRandomCode();
        EmailVerification verification = new EmailVerification();
        verification.setEmail(email);
        verification.setCode(resetCode);
        verification.setType(EmailVerification.VerificationType.PASSWORD_RESET);
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES));

        emailVerificationRepository.save(verification);
        sendPasswordResetEmail(email, resetCode);

        log.info("비밀번호 재설정용 이메일 인증코드 발송 완료: {}", email);
    }

    /**
     * 비밀번호 재설정 코드 검증
     */
    @Transactional
    public boolean verifyPasswordResetCode(String email, String code) {
        Optional<EmailVerification> verificationOpt = emailVerificationRepository
                .findTopByEmailAndTypeOrderByCreatedAtDesc(email, EmailVerification.VerificationType.PASSWORD_RESET);

        if (verificationOpt.isEmpty() || !verificationOpt.get().canVerify() ||
                !verificationOpt.get().getCode().equals(code)) {
            return false;
        }

        EmailVerification verification = verificationOpt.get();
        verification.setVerified(true);
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(PASSWORD_RESET_EXPIRY_MINUTES));
        emailVerificationRepository.save(verification);

        log.info("비밀번호 재설정 코드 인증 성공: {}", email);
        return true;
    }

    /**
     * 비밀번호 재설정 권한 확인
     */
    public boolean canResetPassword(String email) {
        Optional<EmailVerification> verificationOpt = emailVerificationRepository
                .findVerifiedByEmailAndType(email, EmailVerification.VerificationType.PASSWORD_RESET, LocalDateTime.now());
        return verificationOpt.isPresent();
    }

    /**
     * 비밀번호 재설정
     */
    @Transactional
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        user.setUpw(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        removePasswordResetPermission(email);
        log.info("비밀번호 재설정 완료: {}", email);
    }

    /**
     * 비밀번호 재설정 권한 삭제
     */
    @Transactional
    public void removePasswordResetPermission(String email) {
        emailVerificationRepository.deleteByEmailAndType(email, EmailVerification.VerificationType.PASSWORD_RESET);
    }



    /**
     * 이메일 변경용 인증코드 발송
     */
    @Transactional
    public void sendEmailUpdateCode(String newEmail, Long userId) {
        if (userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        emailVerificationRepository.deleteByEmailAndType(newEmail, EmailVerification.VerificationType.EMAIL_UPDATE);

        String verificationCode = generateRandomCode();
        EmailVerification verification = new EmailVerification();
        verification.setEmail(newEmail);
        verification.setCode(verificationCode);
        verification.setType(EmailVerification.VerificationType.EMAIL_UPDATE);
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES));

        emailVerificationRepository.save(verification);
        sendEmailUpdateVerification(newEmail, verificationCode);

        log.info("이메일 변경 인증코드 발송 완료: {} (사용자ID: {})", newEmail, userId);
    }

    /**
     * 이메일 변경 코드 검증
     */
    @Transactional
    public boolean verifyEmailUpdateCode(String email, String code) {
        Optional<EmailVerification> verificationOpt = emailVerificationRepository
                .findTopByEmailAndTypeOrderByCreatedAtDesc(email, EmailVerification.VerificationType.EMAIL_UPDATE);

        if (verificationOpt.isEmpty() || !verificationOpt.get().canVerify() ||
                !verificationOpt.get().getCode().equals(code)) {
            return false;
        }

        EmailVerification verification = verificationOpt.get();
        verification.setVerified(true);
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        emailVerificationRepository.save(verification);

        log.info("이메일 변경 인증 성공: {}", email);
        return true;
    }

    /**
     * 이메일 변경 권한 확인
     */
    public boolean canUpdateEmail(String email) {
        Optional<EmailVerification> verificationOpt = emailVerificationRepository
                .findVerifiedByEmailAndType(email, EmailVerification.VerificationType.EMAIL_UPDATE, LocalDateTime.now());
        return verificationOpt.isPresent();
    }

    /**
     * 이메일 중복 확인
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    /**
     * 이메일 변경
     */
    @Transactional
    public void updateEmail(Long userId, String newEmail) {
        if (!isEmailAvailable(newEmail)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        user.setEmail(newEmail);
        userRepository.save(user);
    }



    /**
     * 아이디 찾기용 이메일 인증코드 발송
     */
    @Transactional
    public void sendFindIdCode(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("등록되지 않은 이메일입니다.");
        }

        emailVerificationRepository.deleteByEmailAndType(email, EmailVerification.VerificationType.FIND_ID);

        String verificationCode = generateRandomCode();
        EmailVerification verification = new EmailVerification();
        verification.setEmail(email);
        verification.setCode(verificationCode);
        verification.setType(EmailVerification.VerificationType.FIND_ID);
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES));

        emailVerificationRepository.save(verification);
        sendFindIdEmail(email, verificationCode);

        log.info("아이디 찾기용 이메일 인증코드 발송 완료: {}", email);
    }

    /**
     * 아이디 찾기 인증코드 검증
     */
    @Transactional
    public boolean verifyFindIdCode(String email, String code) {
        Optional<EmailVerification> verificationOpt = emailVerificationRepository
                .findTopByEmailAndTypeOrderByCreatedAtDesc(email, EmailVerification.VerificationType.FIND_ID);

        if (verificationOpt.isEmpty() || !verificationOpt.get().canVerify() ||
                !verificationOpt.get().getCode().equals(code)) {
            return false;
        }

        EmailVerification verification = verificationOpt.get();
        verification.setVerified(true);
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        emailVerificationRepository.save(verification);

        log.info("아이디 찾기 인증 성공: {}", email);
        return true;
    }

    /**
     * 이메일로 사용자 아이디 조회
     */
    public String getUserIdByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return user.getUid();
    }

    /**
     * 문의하기 저장 및 이메일 전송
     */
    @Transactional
    public void sendInquiryEmail(InquiryEmailReq req, Long userId) {
        log.info("문의 저장 시작: userId={}, subject={}", userId, req.getSubject());

        try {
            // 1. 사용자 정보 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

            // 2. Inquiry 엔티티 생성 및 저장
            Inquiry inquiry = new Inquiry();
            inquiry.setUser(user);
            inquiry.setSubject(req.getSubject());
            inquiry.setContent(req.getMessage());
            inquiry.setSenderName(req.getSenderName());
            inquiry.setSenderEmail(user.getEmail());
            inquiry.setStatus(EnumInquiryStatus.PENDING);

            Inquiry savedInquiry = inquiryRepository.save(inquiry);
            log.info("문의 저장 완료: inquiryId={}", savedInquiry.getId());

            // 3. 관리자에게 이메일 알림 전송 (선택사항)
            try {
                sendInquiryNotificationToAdmin(savedInquiry);
            } catch (Exception e) {
                log.error("관리자 이메일 알림 전송 실패 (문의는 저장됨)", e);
            }

        } catch (Exception e) {
            log.error("문의 저장 실패", e);
            throw new RuntimeException("문의 저장에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 관리자에게 문의 알림 이메일 전송
     */
    private void sendInquiryNotificationToAdmin(Inquiry inquiry) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo("hwangsubin93@gmail.com");
            helper.setSubject("[새 문의] " + inquiry.getSubject());
            helper.setText(createInquiryEmailContent(inquiry), true);
            helper.setFrom("hwangsubin93@gmail.com", "OTD 웹사이트");

            mailSender.send(message);
            log.info("관리자 이메일 알림 전송 완료");
        } catch (Exception e) {
            log.error("관리자 이메일 전송 실패", e);
            throw new RuntimeException("이메일 전송 실패", e);
        }
    }

    /**
     * 사용자의 문의 내역 조회
     */
    public List<InquiryListRes> getMyInquiries(Long userId) {
        List<Inquiry> inquiryList = inquiryRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return inquiryList.stream()
                .map(inquiry -> InquiryListRes.builder()
                        .id(inquiry.getId())
                        .subject(inquiry.getSubject())
                        .status(inquiry.getStatus())
                        .statusTitle(inquiry.getStatus().getTitle())
                        .createdAt(inquiry.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 문의 상세 조회
     */
    public InquiryDetailRes getInquiryDetail(Long inquiryId, Long userId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("문의를 찾을 수 없습니다."));

        if (!inquiry.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("권한이 없습니다.");
        }

        return InquiryDetailRes.builder()
                .id(inquiry.getId())
                .subject(inquiry.getSubject())
                .content(inquiry.getContent())
                .senderName(inquiry.getSenderName())
                .senderEmail(inquiry.getSenderEmail())
                .status(inquiry.getStatus())
                .statusTitle(inquiry.getStatus().getTitle())
                .reply(inquiry.getReply())
                .replyAt(inquiry.getReplyAt())
                .createdAt(inquiry.getCreatedAt())
                .build();
    }



    /**
     * 만료된 인증 정보 정리 (스케줄러)
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredVerifications() {
        int deletedCount = emailVerificationRepository.deleteExpiredVerifications(LocalDateTime.now());
        if (deletedCount > 0) {
            log.info("만료된 이메일 인증 정보 {} 건 삭제 완료", deletedCount);
        }
    }

    /**
     * 6자리 랜덤 코드 생성
     */
    private String generateRandomCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }



    private void sendVerificationEmail(String email, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("[OneToDay] 이메일 인증코드");
            helper.setText(createVerificationEmailContent(code), true);
            helper.setFrom("hwangsubin93@gmail.com");
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("인증 이메일 발송 실패: {}", email, e);
            throw new RuntimeException("이메일 발송에 실패했습니다.");
        }
    }

    private void sendEmailUpdateVerification(String email, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("[OneToDay] 이메일 변경 인증코드");
            helper.setText(createEmailUpdateVerificationContent(code), true);
            helper.setFrom("hwangsubin93@gmail.com");
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("이메일 변경 인증 이메일 발송 실패: {}", email, e);
            throw new RuntimeException("이메일 발송에 실패했습니다.");
        }
    }

    private void sendPasswordResetEmail(String email, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("[OneToDay] 비밀번호 재설정 인증코드");
            helper.setText(createPasswordResetEmailContent(code), true);
            helper.setFrom("hwangsubin93@gmail.com");
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("비밀번호 재설정 이메일 발송 실패: {}", email, e);
            throw new RuntimeException("이메일 발송에 실패했습니다.");
        }
    }

    private void sendFindIdEmail(String email, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("[OneToDay] 아이디 찾기 인증코드");
            helper.setText(createFindIdEmailContent(code), true);
            helper.setFrom("hwangsubin93@gmail.com");
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("아이디 찾기 이메일 발송 실패: {}", email, e);
            throw new RuntimeException("이메일 발송에 실패했습니다.");
        }
    }

    // ==================== HTML 템플릿 ====================

    private String createVerificationEmailContent(String code) {
        return """
                <div style="max-width: 600px; margin: 0 auto; padding: 20px; font-family: Arial, sans-serif;">
                    <div style="text-align: center; margin-bottom: 30px;">
                        <h1 style="color: #10b981;">OneToDay</h1>
                        <h2 style="color: #374151;">이메일 인증</h2>
                    </div>
                    <div style="background-color: #f9fafb; padding: 30px; border-radius: 8px; text-align: center;">
                        <p style="font-size: 16px; color: #6b7280; margin-bottom: 20px;">
                            회원가입을 완료하기 위해 아래 인증코드를 입력해주세요.
                        </p>
                        <div style="background-color: #10b981; color: white; font-size: 32px; font-weight: bold; 
                                    padding: 20px; border-radius: 8px; letter-spacing: 8px; margin: 20px 0;">
                            %s
                        </div>
                        <p style="font-size: 14px; color: #9ca3af;">
                            인증코드는 5분간 유효합니다.
                        </p>
                    </div>
                </div>
                """.formatted(code);
    }

    private String createEmailUpdateVerificationContent(String code) {
        return """
            <div style="max-width: 600px; margin: 0 auto; padding: 20px; font-family: Arial, sans-serif;">
                <div style="text-align: center; margin-bottom: 30px;">
                    <h1 style="color: #3b82f6;">OneToDay</h1>
                    <h2 style="color: #374151;">이메일 변경 인증</h2>
                </div>
                <div style="background-color: #eff6ff; padding: 30px; border-radius: 8px; text-align: center; border: 1px solid #bfdbfe;">
                    <p style="font-size: 16px; color: #6b7280; margin-bottom: 20px;">
                        이메일 변경을 완료하기 위해 아래 인증코드를 입력해주세요.
                    </p>
                    <div style="background-color: #3b82f6; color: white; font-size: 32px; font-weight: bold; 
                                padding: 20px; border-radius: 8px; letter-spacing: 8px; margin: 20px 0;">
                        %s
                    </div>
                    <p style="font-size: 14px; color: #9ca3af;">
                        인증코드는 5분간 유효합니다.
                    </p>
                </div>
            </div>
            """.formatted(code);
    }

    private String createPasswordResetEmailContent(String code) {
        return """
                <div style="max-width: 600px; margin: 0 auto; padding: 20px; font-family: Arial, sans-serif;">
                    <div style="text-align: center; margin-bottom: 30px;">
                        <h1 style="color: #ef4444;">OneToDay</h1>
                        <h2 style="color: #374151;">비밀번호 재설정</h2>
                    </div>
                    <div style="background-color: #fef2f2; padding: 30px; border-radius: 8px; text-align: center; border: 1px solid #fecaca;">
                        <p style="font-size: 16px; color: #6b7280; margin-bottom: 20px;">
                            비밀번호 재설정을 위해 아래 인증코드를 입력해주세요.
                        </p>
                        <div style="background-color: #ef4444; color: white; font-size: 32px; font-weight: bold; 
                                    padding: 20px; border-radius: 8px; letter-spacing: 8px; margin: 20px 0;">
                            %s
                        </div>
                        <p style="font-size: 14px; color: #9ca3af;">
                            인증코드는 5분간 유효하며, 인증 후 10분간 비밀번호를 변경할 수 있습니다.
                        </p>
                    </div>
                </div>
                """.formatted(code);
    }

    private String createFindIdEmailContent(String code) {
        return """
            <div style="max-width: 600px; margin: 0 auto; padding: 20px; font-family: Arial, sans-serif;">
                <div style="text-align: center; margin-bottom: 30px;">
                    <h1 style="color: #3b82f6;">OneToDay</h1>
                    <h2 style="color: #374151;">아이디 찾기 인증</h2>
                </div>
                <div style="background-color: #eff6ff; padding: 30px; border-radius: 8px; text-align: center; border: 1px solid #bfdbfe;">
                    <p style="font-size: 16px; color: #6b7280; margin-bottom: 20px;">
                        아이디 찾기를 위해 아래 인증코드를 입력해주세요.
                    </p>
                    <div style="background-color: #3b82f6; color: white; font-size: 32px; font-weight: bold; 
                                padding: 20px; border-radius: 8px; letter-spacing: 8px; margin: 20px 0;">
                        %s
                    </div>
                    <p style="font-size: 14px; color: #9ca3af;">
                        인증코드는 5분간 유효합니다.
                    </p>
                </div>
            </div>
            """.formatted(code);
    }

    private String createInquiryEmailContent(Inquiry inquiry) {
        return """
            <div style="max-width: 600px; margin: 0 auto; padding: 20px; font-family: Arial, sans-serif;">
                <h2 style="color: #333; border-bottom: 2px solid #007bff; padding-bottom: 10px;">
                    새로운 문의가 접수되었습니다
                </h2>
                <div style="background-color: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0;">
                    <p><strong>문의 번호:</strong> %d</p>
                    <p><strong>제목:</strong> %s</p>
                    <p><strong>보낸이:</strong> %s</p>
                    <p><strong>이메일:</strong> %s</p>
                    <p><strong>상태:</strong> %s</p>
                    <p><strong>접수시간:</strong> %s</p>
                </div>
                <div style="background-color: #ffffff; padding: 20px; border: 1px solid #dee2e6; border-radius: 5px;">
                    <h4 style="color: #495057; margin-top: 0;">문의 내용:</h4>
                    <p style="line-height: 1.6; white-space: pre-wrap;">%s</p>
                </div>
            </div>
            """.formatted(
                inquiry.getId(),
                inquiry.getSubject(),
                inquiry.getSenderName(),
                inquiry.getSenderEmail(),
                inquiry.getStatus().getTitle(),
                inquiry.getCreatedAt(),
                inquiry.getContent()
        );
    }
}