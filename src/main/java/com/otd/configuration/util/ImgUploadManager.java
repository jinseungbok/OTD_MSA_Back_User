package com.otd.configuration.util;

import com.otd.configuration.constants.ConstFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImgUploadManager {
    private final ConstFile constFile;
    private final MyFileUtils myFileUtils;

    public List<String> saveFeedPics(long feedId, List<MultipartFile> pics) {
        //폴더 생성
        String directory = String.format("%s/%s/%d", constFile.getUploadDirectory(), constFile.getChallenge(), feedId);
        myFileUtils.makeFolders(directory);

        List<String> randomFileNames = new ArrayList<>(pics.size());
        for(MultipartFile pic : pics) {
            String randomFileName = myFileUtils.makeRandomFileName(pic); //랜덤파일 이름 생성
            randomFileNames.add(randomFileName); //리턴할 randomFileNames에 이름 추가

            String savePath = directory + "/" + randomFileName;
            try {
                myFileUtils.transferTo(pic, savePath);
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "피드 이미지 저장에 실패하였습니다.");
            }
        }

        return randomFileNames;
    }

    private String makeProfileDirectoryPath(long userId) {
        return String.format("%s/%s/%d",  constFile.getUploadDirectory(), constFile.getProfilePic(), userId);
    }

    //프로파일 유저 폴더 삭제
    public void removeProfileDirectory(long userId) {
        String directory = makeProfileDirectoryPath(userId);
        myFileUtils.deleteFolder(directory, true);
    }

    private String makeFeedDirectoryPath(long feedId) {
        return String.format("%s/%s/%d",  constFile.getUploadDirectory(), constFile.getChallenge(), feedId);
    }

    //피드 폴더 삭제
    public void removeFeedDirectory(long feedId) {
        String directory = makeFeedDirectoryPath(feedId);
        myFileUtils.deleteFolder(directory, true);
    }

    //저장한 파일명 리턴
    public String saveProfilePic(long userId, MultipartFile profilePicFile) {
        //폴더 생성
        String directory = makeProfileDirectoryPath(userId);
        myFileUtils.makeFolders(directory);

        String randomFileName = myFileUtils.makeRandomFileName(profilePicFile);
        String savePath = directory + "/" + randomFileName;

        try {
            myFileUtils.transferTo(profilePicFile, savePath);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "프로파일 이미지 저장에 실패하였습니다.");
        }
        return randomFileName;
    }
    public String saveProfilePicFromBase64(Long userId, String base64Data) {
        try {
            // Base64 데이터에서 헤더 제거 (data:image/jpeg;base64, 부분)
            String base64Image = base64Data;
            if (base64Data.contains(",")) {
                base64Image = base64Data.split(",")[1];
            }

            // Base64 디코딩
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            // 파일명 생성 (기존 패턴과 유사하게)
            String fileName = "profile_" + System.currentTimeMillis() + ".jpg";

            // 기존 메서드와 동일한 방식으로 디렉토리 생성
            String directory = makeProfileDirectoryPath(userId);
            myFileUtils.makeFolders(directory);

            // 파일 저장 경로
            String savePath = directory + "/" + fileName;
            Path filePath = Paths.get(savePath);
            Files.write(filePath, imageBytes);

            return fileName;
        } catch (Exception e) {
            throw new RuntimeException("프로필 이미지 저장 실패", e);
        }
    }
}
