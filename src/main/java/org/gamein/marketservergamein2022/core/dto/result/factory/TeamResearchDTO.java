package org.gamein.marketservergamein2022.core.dto.result.factory;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;


@AllArgsConstructor
@Getter
public class TeamResearchDTO {
    private ResearchSubjectDTO subject;
    private int paidAmount;
    private ZonedDateTime beginTime;
    private ZonedDateTime currentTime;
    private ZonedDateTime endTime;
    private String status;
    private long balance;
    private long price;
    private int duration;
}
