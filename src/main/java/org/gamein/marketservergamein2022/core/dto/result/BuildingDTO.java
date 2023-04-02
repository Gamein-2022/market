package org.gamein.marketservergamein2022.core.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.BuildingType;


@Getter
@AllArgsConstructor
public class BuildingDTO {
    private long id;
    private BuildingType type;
    private boolean upgraded;
}
