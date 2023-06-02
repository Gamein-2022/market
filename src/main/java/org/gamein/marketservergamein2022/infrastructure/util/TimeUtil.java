package org.gamein.marketservergamein2022.infrastructure.util;

import org.gamein.marketservergamein2022.core.dto.result.TimeResultDTO;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Time;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class TimeUtil {
    public static TimeResultDTO getTime(Time time) {
        LocalDateTime beginDate = time.getBeginTime();
        Long stoppedSeconds = time.getStoppedTimeSeconds();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        long durationSeconds = Duration.between(beginDate, now).toSeconds() - stoppedSeconds - time.getChooseRegionDuration();
        long durationMilliSeconds = Duration.between(beginDate, now).toMillis() - stoppedSeconds * 1000
                - time.getChooseRegionDuration() * 1000;
        long daySeconds = 8L;
        long daysFromBeginning = durationSeconds / daySeconds;

        long days = 254 + daysFromBeginning;
        byte era = 0;
        if (daysFromBeginning >= 7680)
            era = 4;
        else if (daysFromBeginning >= 6105)
            era = 3;
        else if (daysFromBeginning >= 2738)
            era = 2;
        else if (daysFromBeginning >= 1163) {
            era = 1;
        }

        long year = 2003 + days / 360;

        days -= (year - 2003) * 360;

        long month = 1 + days / 30;

        days -= (month - 1) * 30;

        long day = days + 1;


        TimeResultDTO timeResultDTO = new TimeResultDTO();
        timeResultDTO.setDurationMillis(durationMilliSeconds);
        timeResultDTO.setDay(day);
        timeResultDTO.setMonth(month);
        timeResultDTO.setYear(year);
        timeResultDTO.setEra(era);
        timeResultDTO.setIsGamePaused(time.getIsGamePaused());
        return timeResultDTO;
    }
}
