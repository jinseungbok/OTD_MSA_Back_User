package com.otd.otd_user.application.term;

import com.otd.otd_user.application.term.model.AgreementRequestDto;
import com.otd.otd_user.application.term.model.AgreementResponseDto;
import com.otd.otd_user.application.term.model.ApiResponse;
import com.otd.otd_user.application.term.model.TermsResponseDto;
import com.otd.configuration.enumcode.model.EnumTermsType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/OTD/terms")
@RequiredArgsConstructor
public class TermsController {

    private final TermsService termsService;

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<TermsResponseDto>>> getActiveTerms() {
        List<TermsResponseDto> terms = termsService.getActiveTerms().stream()
                .map(term -> TermsResponseDto.builder()
                        .termsId(term.getTermsId())
                        .type(term.getType())
                        .typeDescription(term.getType().getDescription())
                        .title(term.getTitle())
                        .content(term.getContent())
                        .version(term.getVersion())
                        .isRequired(term.getIsRequired())
                        .effectiveDate(term.getEffectiveDate())
                        .createdAt(term.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(terms));
    }

    @GetMapping("/{type}")
    public ResponseEntity<ApiResponse<TermsResponseDto>> getTermsByType(@PathVariable EnumTermsType type) {
        var term = termsService.getActiveTermsByType(type);

        TermsResponseDto dto = TermsResponseDto.builder()
                .termsId(term.getTermsId())
                .type(term.getType())
                .typeDescription(term.getType().getDescription())
                .title(term.getTitle())
                .content(term.getContent())
                .version(term.getVersion())
                .isRequired(term.getIsRequired())
                .effectiveDate(term.getEffectiveDate())
                .createdAt(term.getCreatedAt())
                .build();

        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PostMapping("/agree")
    public ResponseEntity<ApiResponse<String>> agreeToTerms(
            @RequestBody AgreementRequestDto request,
            @RequestParam Long userId,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        termsService.agreeToTerms(userId, request.getTermsIds(), ipAddress, userAgent);

        return ResponseEntity.ok(ApiResponse.success("약관 동의가 완료되었습니다."));
    }

    @GetMapping("/agreements/{userId}")
    public ResponseEntity<ApiResponse<List<AgreementResponseDto>>> getUserAgreements(
            @PathVariable Long userId) {

        List<AgreementResponseDto> agreements = termsService.getUserAgreements(userId).stream()
                .map(agreement -> AgreementResponseDto.builder()
                        .agreementId(agreement.getAgreementId())
                        .termsId(agreement.getTerms().getTermsId())
                        .enumTermsType(agreement.getTerms().getType())
                        .termsTitle(agreement.getTerms().getTitle())
                        .agreed(agreement.getAgreed())
                        .agreedAt(agreement.getAgreedAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(agreements));
    }

    @GetMapping("/check-required/{userId}")
    public ResponseEntity<ApiResponse<Boolean>> checkRequiredAgreements(@PathVariable Long userId) {
        boolean hasAgreed = termsService.hasAgreedToAllRequiredTerms(userId);
        return ResponseEntity.ok(ApiResponse.success(hasAgreed));
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
}