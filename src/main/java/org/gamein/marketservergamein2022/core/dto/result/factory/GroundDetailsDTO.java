package org.gamein.marketservergamein2022.core.dto.result.factory;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public class GroundDetailsDTO {
    private Integer productionBuildCost;
    private Integer assemblyBuildCost;
    private Integer recycleBuildCost;
    private Integer upgradeRegionCost;
    private Boolean isGroundAvailable;
    private BuildingDetailsDTO building;
}
