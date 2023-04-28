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



        long daySeconds = 8L;
        long monthSeconds = 30L * daySeconds;
        long yearSeconds = 12 * monthSeconds;
        long daysFromBeginning = durationSeconds / daySeconds;
        long monthFromBeginning = durationSeconds / monthSeconds;
        long yearFromBeginning = durationSeconds / yearSeconds;

        byte era = 0;
        if (daysFromBeginning >= 7425)
            era = 4;
        else if (daysFromBeginning >= 4688)
            era = 3;
        else if (daysFromBeginning >= 2738)
            era = 2;
        else if (daysFromBeginning >= 1163) {
            era = 1;
        }

        long year = 2002 + (yearFromBeginning) + 1;

        long month = ((8 + monthFromBeginning) % 12) + 1;

        long day = ((14 + daysFromBeginning) % 30) + 1;


        TimeResultDTO timeResultDTO = new TimeResultDTO();
        timeResultDTO.setSecondOfDate(durationSeconds);
        timeResultDTO.setDay(day);
        timeResultDTO.setMonth(month);
        timeResultDTO.setYear(year);
        timeResultDTO.setEra(era);
        return timeResultDTO;
    }
}
