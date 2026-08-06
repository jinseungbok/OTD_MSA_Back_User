package com.otd.otd_user.application.user;

import com.otd.otd_challenge.application.challenge.model.home.ChallengeMissionCompleteGetRes;
import com.otd.otd_user.application.user.model.UserLoginRes;
import com.otd.otd_user.application.user.model.UserProfileGetDto;
import com.otd.otd_user.application.user.model.UserProfileGetRes;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    UserProfileGetRes findProfileByUserId(long signedUserId);
    int countByUid(String uid);
    int countByNickname(String nickname);
    UserLoginRes findRoleByUserId(long signedUserId);
    List<ChallengeMissionCompleteGetRes> findAllMissionCompleteByUserId(Long userId);

}
