package org.gamein.marketservergamein2022.core.dto.result.market;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Timestamp;


@Getter
@AllArgsConstructor
public class NextTradeTaskDTO {
    private Timestamp nextTime;
    private Timestamp currentTime;
}
