package com.otd.configuration.security;

import com.otd.configuration.enumcode.AbstractEnumCodeConverter;
import com.otd.configuration.enumcode.EnumMapperType;
import jakarta.persistence.Converter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SignInProviderType implements EnumMapperType {
      LOCAL("1", "LOCAL")
    , KAKAO("2", "KAKAO")
    , NAVER("3", "NAVER")
    , GOOGLE("4", "GOOGLE");

    private final String code;
    private final String value;

    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<SignInProviderType> {
        public CodeConverter() {
            super(SignInProviderType.class, false);
        }
    }
}
