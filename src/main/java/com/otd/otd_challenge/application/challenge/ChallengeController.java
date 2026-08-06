package com.otd.otd_challenge.application.challenge;

import com.otd.configuration.model.ResultResponse;
import com.otd.configuration.model.UserPrincipal;
import com.otd.otd_challenge.application.challenge.Repository.ChallengeProgressRepository;
import com.otd.otd_challenge.application.challenge.model.*;
import com.otd.otd_challenge.application.challenge.model.detail.ChallengeDetailDayGetRes;
import com.otd.otd_challenge.application.challenge.model.detail.ChallengeDetailPerGetRes;
import com.otd.otd_challenge.application.challenge.model.detail.ChallengeProgressGetReq;
import com.otd.otd_challenge.application.challenge.model.detail.ChallengeSuccessPutReq;
import com.otd.otd_challenge.application.challenge.model.feignClient.ChallengeRecordDeleteReq;
import com.otd.otd_challenge.application.challenge.model.feignClient.ExerciseDataReq;
import com.otd.otd_challenge.application.challenge.model.feignClient.MealDataReq;
import com.otd.otd_challenge.application.challenge.model.home.ChallengeHomeGetRes;
import com.otd.otd_challenge.application.challenge.model.home.ChallengeRecordMissionPostReq;
import com.otd.otd_challenge.application.challenge.model.home.MainHomGetReq;
import com.otd.otd_challenge.application.challenge.model.home.MainHomeGetRes;
import com.otd.otd_challenge.application.challenge.model.settlement.ChallengeSettlementDto;
import com.otd.otd_challenge.application.challenge.model.settlement.ChallengeSettlementGetReq;
import com.otd.otd_challenge.application.challenge.model.settlement.ChallengeSettlementGetRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/OTD/challenge")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;
    private final ChallengeSchedulerService challengeSchedulerService;
    private final ChallengeProgressRepository challengeProgressRepository;


    @GetMapping("/list")
    public Map<String, Object> getChallengeList(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("유저 아이디: {}", userPrincipal.getSignedUserId());
        return challengeService.getChallengeList();
    }

    @GetMapping("/selected")
    public ChallengeHomeGetRes getSelectedList(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                               @ModelAttribute ChallengeProgressGetReq req) {
        return challengeService.getSelectedList(userPrincipal.getSignedUserId(), req);
    }

    @GetMapping("/addlist")
    public List<ChallengeDefinitionGetRes> getChallengeList(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                            @ModelAttribute ChallengeProgressGetReq req) {
        return challengeService.getChallengeList(userPrincipal.getSignedUserId(), req);
    }

    @GetMapping("/addcompetitionlist")
    public Map<String, List<ChallengeDefinitionGetRes>> getCompetitionList(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                        @ModelAttribute ChallengeProgressGetReq req) {
        return challengeService.getCompetitionList(userPrincipal.getSignedUserId(), req);
    }

    @GetMapping("/detail/per/{cdId}")
    public ChallengeDetailPerGetRes getDetailPer(@PathVariable Long cdId, @AuthenticationPrincipal UserPrincipal userPrincipal,
                                                 @ModelAttribute ChallengeProgressGetReq req) {
        return challengeService.getDetailPer(cdId, userPrincipal.getSignedUserId(), req);
    }

    @GetMapping("/detail/day/{cdId}")
    public ChallengeDetailDayGetRes getDetailDay(@PathVariable Long cdId, @AuthenticationPrincipal UserPrincipal userPrincipal,
                                                 @ModelAttribute ChallengeProgressGetReq req) {
        return challengeService.getDetailDay(cdId, userPrincipal.getSignedUserId(), req);
    }

    @PutMapping("/success")
    public ResultResponse<?> putSuccess(@RequestBody ChallengeSuccessPutReq req) {
        return challengeService.updateIsSuccess(req.getCpId());
    }

    @PostMapping("/record/mission")
    public ResultResponse<?> postMissionRecord(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                               @RequestBody ChallengeRecordMissionPostReq req){
        return challengeService.saveMissionRecord(userPrincipal.getSignedUserId(), req);
    }
    @PostMapping("/add")
    public ResultResponse<?> postChallenge(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                           @RequestBody ChallengePostReq req){
        return challengeService.saveChallenge(userPrincipal.getSignedUserId(), req);
    }


    @PostMapping("/settlement")
    public ResultResponse<?> weeklySettlement(@RequestBody ChallengeSettlementDto dto) {
        return challengeService.setSettlement(dto);
    }

    @GetMapping("/settlement/log")
    public List<ChallengeSettlementGetRes> getSettlementLog(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                             @ModelAttribute ChallengeSettlementGetReq req){
        System.out.println("req" + req);
        return challengeSchedulerService.getSettlementLog(userPrincipal.getSignedUserId(), req);
    }

    @GetMapping("/home")
    public List<MainHomeGetRes> getMyChallenge(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                               @ModelAttribute MainHomGetReq req) {
        return challengeService.getMainHomeChallenge(userPrincipal.getSignedUserId(), req);
    }

    @PostMapping("/progress/exercise")
    public ResponseEntity<Integer> patchExerciseProgress(@RequestBody ExerciseDataReq req) {
        int result = challengeService.updateProgressEx(req);
        if (result > 0) {
            return ResponseEntity.ok(result); // 200 + 1
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result); // 400 + 0
        }
    }

    @DeleteMapping("/record/delete")
    public ResponseEntity<Integer> deleteRecord(@RequestBody ChallengeRecordDeleteReq req) {
        int result = challengeService.deleteRecord(req);
        if (result > 0) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
    }

    @PostMapping("/progress/meal")
    public ResponseEntity<Integer> patchMealProgress(@RequestBody MealDataReq req) {
        int result = challengeService.updateProgressMeal(req);
        if (result > 0) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
    }
    @GetMapping("/progress/challenges/{userId}")
    public ResponseEntity<List<String>> getActiveChallengeNames(@PathVariable Long userId
            , @RequestParam("recordDate") LocalDate strRecordDate) {
        log.info("userId: {}", userId);
        log.info("recordDate: {}", strRecordDate);

//        LocalDate recordDate = LocalDate.parse(strRecordDate);

        List<String> challengeNames = challengeProgressRepository.findActiveChallengeNames(userId, strRecordDate);
        return ResponseEntity.ok(challengeNames);
    }
}