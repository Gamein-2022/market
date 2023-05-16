package org.gamein.marketservergamein2022.core.dto.result.factory;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Timestamp;
import java.time.ZonedDateTime;


@AllArgsConstructor
@Getter
public class TeamResearchDTO {
    private ResearchSubjectDTO subject;
    private int paidAmount;
    private Timestamp beginTime;
    private Timestamp currentTime;
    private Timestamp endTime;
    private String status;
    private long balance;
    private long price;
    private int duration;
}
