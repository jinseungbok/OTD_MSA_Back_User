package com.otd.otd_admin.application.admin.model.statistics;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GenderCountRes {
    private String gender;
    private Long count;
}
