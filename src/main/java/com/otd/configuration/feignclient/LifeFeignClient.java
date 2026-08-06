package com.otd.configuration.feignclient;

import com.otd.configuration.model.ResultResponse;
import com.otd.otd_user.application.user.model.NicknameUpdateDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "${constants.feign-client.life.name}", url = "${constants.feign-client.life.url}")
public interface LifeFeignClient {
    @PostMapping("/api/OTD/exercise/challenge/names")
    void sendActiveChallengeNames(@RequestBody List<String> challengeNames);

    @DeleteMapping("/api/OTD/admin2/{userId}")
    ResultResponse<?> deleteUserData(@PathVariable("userId") Long userId);

}
