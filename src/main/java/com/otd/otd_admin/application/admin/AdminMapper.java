package com.otd.otd_admin.application.admin;

import com.otd.otd_admin.application.admin.model.AdminChallengeProgress;
import com.otd.otd_admin.application.admin.model.AdminPurchaseDto;
import com.otd.otd_admin.application.admin.model.statistics.*;
import com.otd.otd_challenge.entity.ChallengeDefinition;
import com.otd.otd_user.entity.Inquiry;
import com.otd.otd_user.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface AdminMapper {
    List<AgeCountRes> groupByAge();
    List<SignInCountRes> countBySignIn();

    List<TierCountRes> countByTier();
    List<ChallengeSuccessRateCountRes> countByChallengeType();
    List<ChallengeTypeCountRes> countByChallengeTypeRatio();
    List<ChallengeParticipationCountRes> countByChallengeParticipation();
    List<AdminPurchaseDto> getUserPurchaseList(Long userId);
    List<InquiryCountRes> countByInquiry();
    List<AdminPurchaseDto> getAdminPurchaseHistory();
    List<ChallengeDefinition> findTop3ByFailRate();
    List<ChallengeDefinition> findTop5ByParticipationRate();
    Double findAverageSuccessRate();
    List<User> findTop5ByPoint();
    List<Inquiry> findRecent5Inquiry();
    Double getAvgInquiryRepliedTime();
    Double getInquiryRepliedRate();
    List<AdminChallengeProgress> findByCdId(Long id, LocalDate date);
}
