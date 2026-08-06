package com.otd.otd_challenge.entity;

import com.otd.configuration.enumcode.model.EnumChallengeRole;
import com.otd.otd_admin.application.admin.model.AdminChallengeDto;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class ChallengeDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cdId;

    @Column(nullable = false, length = 10)
    private String cdName;

    @Column(nullable = false)
    private int cdReward;

    @Column(nullable = false, length = 12)
    private String cdType;

    @Column(nullable = false)
    private String cdImage;

    @Column(nullable = false)
    private int cdGoal;

    @Column(nullable = false, length = 10)
    private String cdUnit;

    @Column(nullable = false, length = 4)
    private int xp;

    @Convert(converter = EnumChallengeRole.CodeConverter.class)
    @Column(nullable = false, length = 10)
    private EnumChallengeRole tier;

    @Column(length = 30, columnDefinition = "VARCHAR(30) DEFAULT '-'")
    private String note;

    public void update(AdminChallengeDto dto) {
        this.cdName = dto.getCdName();
        this.cdType = dto.getCdType();
        this.cdGoal = dto.getCdGoal();
        this.cdUnit = dto.getCdUnit();
        this.cdReward = dto.getCdReward();
        this.xp = dto.getXp();
        this.tier = dto.getTier();
        if (dto.getCdImage() != null) {
            this.cdImage = dto.getCdImage();
        }
    }
}
