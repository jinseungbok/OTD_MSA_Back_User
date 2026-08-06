package com.otd.otd_user.entity;

import com.otd.configuration.enumcode.model.EnumChallengeRole;
import com.otd.configuration.enumcode.model.EnumUserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
@EqualsAndHashCode
@Setter
public class UserRoleIds implements Serializable {
    private Long userId;
    @Column(length = 2)
    @Convert(converter = EnumUserRole.CodeConverter.class)
    private EnumUserRole roleCode;

    @Column(length = 2)
    @Convert(converter = EnumChallengeRole.CodeConverter.class)
    private EnumChallengeRole challengeCode;
}
