package org.gamein.marketservergamein2022.core.dto.result.factory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.BuildingType;

import java.util.List;


@Getter
@AllArgsConstructor
public class BuildingDTO {
    private BuildingType type;
    private boolean upgraded;
    private List<Boolean> lines;
}
