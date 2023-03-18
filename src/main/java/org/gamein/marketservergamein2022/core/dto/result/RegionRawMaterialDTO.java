package org.gamein.marketservergamein2022.core.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;


@AllArgsConstructor
@Getter
public class RegionRawMaterialDTO {
    private List<ProductDTO> myRegion;
    private List<ProductDTO> otherRegions;
}
