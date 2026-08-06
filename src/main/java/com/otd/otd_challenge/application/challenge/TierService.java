package com.otd.otd_challenge.application.challenge;

import com.otd.configuration.enumcode.model.EnumChallengeRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TierService {
    public EnumChallengeRole checkTierUp(EnumChallengeRole myRole, int xp) {
        int level = xp / 100; // 100xp = 1레벨

        switch (myRole) {
            case BRONZE:
                if (level >= 5) return EnumChallengeRole.SILVER;
                break;
            case SILVER:
                if (level >= 10) return EnumChallengeRole.GOLD;
                break;
            case GOLD:
                if (level >= 15) return EnumChallengeRole.DIAMOND;
                break;
            case DIAMOND:
                return EnumChallengeRole.DIAMOND;
        }
        return myRole;
    }
}
