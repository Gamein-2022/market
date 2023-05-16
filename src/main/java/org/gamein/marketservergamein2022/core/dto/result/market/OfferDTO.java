package org.gamein.marketservergamein2022.core.dto.result.market;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;


@AllArgsConstructor
@Getter
public class OfferDTO {
    private Long id;
    private Long offererId;
    private OrderDTO order;
    private LocalDateTime creationDate;
    private Boolean declined;
    private LocalDateTime acceptDate;
    private Boolean cancelled;
    private int region;
    private int distance;
    private Long balance;
    private int planeDuration;
    private int shipDuration;
    private int shipBasePrice;
    private int planeBasePrice;
    private int shipVarPrice;
    private int planeVarPrice;
}
