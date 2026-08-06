package com.otd.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration //빈등록
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final String uploadDirectory;

    public WebMvcConfiguration(@Value("${constants.file.upload-directory}") String uploadDirectory) {
        log.info("Upload Path: {}", uploadDirectory);
        this.uploadDirectory = uploadDirectory;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + uploadDirectory + "/challenge/");
        registry.addResourceHandler("/profile/**")
                .addResourceLocations("file:" + uploadDirectory + "/profile/");
        registry.addResourceHandler("/pointshop/**")
                .addResourceLocations("file:" + uploadDirectory + "/pointshop/");
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        //RestController 애노테이션이 있는 컨트롤러의 주소앞에 /api를 모두 붙여준다.
        configurer.addPathPrefix("/api",
                HandlerTypePredicate.forAnnotation(RestController.class)
                        .and(HandlerTypePredicate.forBasePackage("com.otd")) // 전체 OTD 내 컨트롤러
        );
    }
}
