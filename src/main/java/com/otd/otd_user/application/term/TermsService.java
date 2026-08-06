package com.otd.otd_user.application.term;

import com.otd.otd_user.application.user.UserRepository;
import com.otd.otd_user.entity.Terms;
import com.otd.configuration.enumcode.model.EnumTermsType;
import com.otd.otd_user.entity.User;
import com.otd.otd_user.entity.UserAgreement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermsService {

    private final TermsRepository termsRepository;
    private final UserAgreementRepository agreementRepository;
    private final UserRepository userRepository;

    public List<Terms> getActiveTerms() {
        return termsRepository.findByIsActiveTrueOrderByTypeAsc();
    }

    public Terms getActiveTermsByType(EnumTermsType type) {
        return termsRepository.findByTypeAndIsActiveTrue(type)
                .orElseThrow(() -> new RuntimeException("활성화된 약관을 찾을 수 없습니다."));
    }

    @Transactional
    public Terms createNewTerms(EnumTermsType type, String title, String content,
                                String version, Boolean isRequired) {
        termsRepository.findByTypeAndIsActiveTrue(type)
                .ifPresent(existingTerm -> {
                    existingTerm.setIsActive(false);
                    termsRepository.save(existingTerm);
                });

        Terms newTerm = Terms.builder()
                .type(type)
                .title(title)
                .content(content)
                .version(version)
                .isActive(true)
                .isRequired(isRequired)
                .effectiveDate(LocalDateTime.now())
                .build();

        return termsRepository.save(newTerm);
    }

    @Transactional
    public void agreeToTerms(Long userId, List<Long> termsIds,
                             String ipAddress, String userAgent) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        for (Long termsId : termsIds) {
            Terms terms = termsRepository.findById(termsId)
                    .orElseThrow(() -> new RuntimeException("약관을 찾을 수 없습니다."));

            UserAgreement agreement = UserAgreement.builder()
                    .user(user)
                    .terms(terms)
                    .agreed(true)
                    .agreedAt(LocalDateTime.now())
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();

            agreementRepository.save(agreement);
        }
    }

    public boolean hasAgreedToAllRequiredTerms(Long userId) {
        List<Terms> requiredTerms = termsRepository.findByIsActiveTrueAndIsRequiredTrue();
        List<UserAgreement> userAgreements = agreementRepository.findActiveAgreementsByUserId(userId);

        return requiredTerms.stream()
                .allMatch(requiredTerm ->
                        userAgreements.stream()
                                .anyMatch(agreement ->
                                        agreement.getTerms().getTermsId().equals(requiredTerm.getTermsId())
                                                && agreement.getAgreed()
                                )
                );
    }

    public List<UserAgreement> getUserAgreements(Long userId) {
        return agreementRepository.findByUserUserIdOrderByAgreedAtDesc(userId);
    }
}