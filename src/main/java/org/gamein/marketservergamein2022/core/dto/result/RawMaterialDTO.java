package org.gamein.marketservergamein2022.core.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class RawMaterialDTO {
    private final long id;
    private final String name;
    private final double price;
    private int planeDuration;
    private int shipDuration;
    private int distance;
    private int unitVolume;
    private final int planePrice;
    private final int shipPrice;
    private final int planeVariablePrice;
    private final int shipVariablePrice;
}
