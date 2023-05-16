package org.gamein.marketservergamein2022.core.dto.result.factory;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;


@AllArgsConstructor
@Getter
public class BuildingDetailsDTO {
    private Long id;
    private List<FactoryLineDTO> lines;
    private Boolean isUpgraded;
    private Integer upgradeCost;
}
