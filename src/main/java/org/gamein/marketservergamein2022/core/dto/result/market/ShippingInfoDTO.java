package org.gamein.marketservergamein2022.core.dto.result.market;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public class ShippingInfoDTO {
    private int planeDuration;
    private int shipDuration;
    private int planeBasePrice;
    private int shipBasePrice;
    private int planeVariablePrice;
    private int shipVariablePrice;
    private long balance;
    private int distance;
}
