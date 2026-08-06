package com.otd.otd_challenge.application.challenge.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChallengePostReq {
    private Long userId;
    private Long cdId;
    private String type;
}
