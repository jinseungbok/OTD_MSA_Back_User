package com.otd.configuration.enumcode.model;

import com.otd.configuration.enumcode.AbstractEnumCodeConverter;
import com.otd.configuration.enumcode.EnumMapperType;
import jakarta.persistence.Converter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum EnumChallengeRole implements EnumMapperType {
    TBD("00", "없음"), // To Be Determined
    BRONZE("01", "브론즈"),
    SILVER("02", "실버"),
    GOLD("03", "골드"),
    DIAMOND("04", "다이아");

    private final String code;
    private final String value;

    @Converter(autoApply = false)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumChallengeRole> {
        public CodeConverter() { super(EnumChallengeRole.class, false); }
    }
    // 설문조사
    public static EnumChallengeRole fromCode(int surveyAnswers) {
        return switch (surveyAnswers) {
            case 0, 1, 2, 3 -> EnumChallengeRole.BRONZE;
            case 4, 5, 6 -> EnumChallengeRole.SILVER;
            case 7, 8, 9 -> EnumChallengeRole.GOLD;
            case 10, 11, 12 -> EnumChallengeRole.DIAMOND;
            default -> EnumChallengeRole.TBD;
        };
    }
    // DB티어 -> enum코드로 변경
    public static EnumChallengeRole fromCode(String code) {
        return Arrays.stream(values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(TBD);
    }
    public boolean isHigherOrEqual(EnumChallengeRole other) {
        // code를 int로 변환해서 비교
        return Integer.parseInt(this.code) >= Integer.parseInt(other.code);
    }
}
