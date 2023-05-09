package org.gamein.marketservergamein2022.core.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;


@Getter
@AllArgsConstructor
public class NextTradeTaskDTO {
    private Date nextTime;
    private LocalDateTime currentTime;
}
