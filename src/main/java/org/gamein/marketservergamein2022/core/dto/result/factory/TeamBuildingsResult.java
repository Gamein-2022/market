package org.gamein.marketservergamein2022.core.dto.result.factory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.gamein.marketservergamein2022.core.dto.result.BaseResult;

import java.util.List;


@AllArgsConstructor
@Getter
@Setter
public class TeamBuildingsResult extends BaseResult {
    private List<BuildingDTO> buildings;
    private boolean isRegionUpgraded;
    private int upgradeRegionCost;
    private boolean isStorageUpgraded;
}
