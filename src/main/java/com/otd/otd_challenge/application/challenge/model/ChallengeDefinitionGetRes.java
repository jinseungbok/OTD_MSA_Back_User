package com.otd.otd_challenge.application.challenge.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.otd.configuration.enumcode.model.EnumChallengeRole;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ChallengeDefinitionGetRes {
    private int id;
    private int goal;
    private String image;
    private String name;
    private String type;
    private int reward;
    private String unit;
    private int xp;
    private String note;
    @JsonIgnore
    private String tierCode;

    private EnumChallengeRole tier;
    private boolean available;
//    @Value("${constants.file.challenge-pic}")
//    private String imgPath;
//
//    public void setImage(String image) {
//        this.image = imgPath + image;
//    }
}
