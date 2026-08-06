package com.otd.otd_admin.application.admin;

import com.otd.configuration.model.ResultResponse;
import com.otd.configuration.model.UserPrincipal;
import com.otd.otd_admin.application.admin.model.*;
import com.otd.otd_admin.application.admin.model.dashboard.AdminDashBoardChallengeDto;
import com.otd.otd_admin.application.admin.model.dashboard.AdminDashBoardInquiryDto;
import com.otd.otd_admin.application.admin.model.dashboard.AdminDashBoardPointDto;
import com.otd.otd_admin.application.admin.model.dashboard.AdminDashBoardUserDto;
import com.otd.otd_admin.application.admin.model.statistics.*;
import com.otd.otd_challenge.entity.ChallengeDefinition;
import com.otd.otd_challenge.entity.ChallengePointHistory;
import com.otd.otd_pointshop.entity.PurchaseHistory;
import com.otd.otd_user.entity.Inquiry;
import com.otd.otd_user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/OTD/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    // 대시보드
    @GetMapping("dash/user")
    public AdminDashBoardUserDto getUserDashBoard(){
        return adminService.getUserDashBoard();
    }

    @GetMapping("/dash/challenge")
    public AdminDashBoardChallengeDto getChallengeDashBoard(){
        return adminService.getChallengeDashBoard();
    }

    @GetMapping("/dash/point")
    public AdminDashBoardPointDto getPointDashBoard(){
        return adminService.getPointDashBoard();
    }

    @GetMapping("/dash/inquiry")
    public AdminDashBoardInquiryDto getInquiryDashBoard(){
        return adminService.getInquiryDashBoard();
    }

    // 유저
    @GetMapping("/user")
    public List<User> getUsers() {
        return adminService.getUsers();
    }

    @GetMapping("/user/{userId}")
    public AdminUserDetailGetRes getUserDetail(@PathVariable Long userId) {
        return adminService.getUserDetail(userId);
    }

    @PutMapping("/user/modify")
    public ResultResponse<?> putUser(@RequestBody AdminUserPutReq req){
        return adminService.putUserDetail(req);
    }

    // 챌린지
    @GetMapping("/challenge")
    public List<ChallengeDefinition> getChallenges() {
        return adminService.getChallenges();
    }

    @GetMapping("/challenge/progress/{id}")
    public List<AdminChallengeProgress> getChallengeProgress(@PathVariable Long id){
        return adminService.getChallengeProgress(id);
    }

    @PostMapping("/challenge/add")
    public ResultResponse<?> addChallenge(
            @RequestPart("challenge") AdminChallengeDto challengeDto,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        if (file != null && !file.isEmpty()){
            String fileName = adminService.saveChallengeImage(file);
            challengeDto.setCdImage(fileName);
        }
        AdminChallengeDto save = adminService.addChallenge(challengeDto);
        return new ResultResponse<>("챌린지 등록 성공", save);
    }

    @PutMapping("/challenge/modify")
    public ResultResponse<?> putChallenge(
            @RequestPart("challenge") AdminChallengeDto challengeDto,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        AdminChallengeDto update = adminService.modifyChallenge(challengeDto, file);
        return new ResultResponse<>("챌린지 수정 성공", update);
    }

    @DeleteMapping("/user/{userId}")
    public ResultResponse<?> deleteUser(@PathVariable Long userId){
        return adminService.removeUser(userId);
    }

    @DeleteMapping("/challenge/{cdId}")
    public ResultResponse<?> deleteChallenge(@PathVariable Long cdId) {
        return adminService.removeChallenge(cdId);
    }

    // 포인트
    @GetMapping("/point")
    public List<ChallengePointHistory> getPointHistory() {
        return adminService.getPointHistory();
    }

    @GetMapping("/purchase")
    public List<AdminPurchaseDto> getPurchaseHistory() {
        return adminService.getPurchaseHistory();
    }
    // 통계
    @GetMapping("/statistics/user")
    public AdminStatisticsUserDto getUserStatistics() {
        return adminService.getUserStatistics();
    }

    @GetMapping("/statistics/challenge")
    public AdminStatisticsChallengeDto getChallengeStatistics() {
        return adminService.getChallengeStatistics();
    }

    @GetMapping("/statistics/inquiry")
    public AdminStatisticsInquiryDto getInquiryStatistics() {
        return adminService.getInquiryStatistics();
    }

    // 문의
    @GetMapping("/qna")
    public List<Inquiry> getInquiry() {
        return adminService.getInquiry();
    }

    @PutMapping("/qna/modify")
    public ResultResponse<?> modifyInquiry(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                           @RequestBody AdminInquiryReq req) {
        req.setAdminId(userPrincipal.getSignedUserId());
        return adminService.putInquiry(req);
    }
    @GetMapping("/qna/{inquiryId}")
    public Inquiry getInquiryDetail(@PathVariable Long inquiryId) {
        return adminService.getInquiryDetail(inquiryId);
    }
}
