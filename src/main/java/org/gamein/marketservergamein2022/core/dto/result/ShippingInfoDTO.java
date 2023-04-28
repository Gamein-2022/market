package org.gamein.marketservergamein2022.core.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public class ShippingInfoDTO {
    private int planeDuration;
    private int shipDuration;
    private int planePrice;
    private int shipPrice;
    private long balance;
    private int distance;
}
