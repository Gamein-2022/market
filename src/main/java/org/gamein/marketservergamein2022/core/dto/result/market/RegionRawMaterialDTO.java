package org.gamein.marketservergamein2022.core.dto.result.market;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.gamein.marketservergamein2022.core.dto.result.RawMaterialDTO;

import java.util.List;


@AllArgsConstructor
@Getter
public class RegionRawMaterialDTO {
    private List<RawMaterialDTO> myRegion;
    private List<RawMaterialDTO> otherRegions;
    private long balance;
}
