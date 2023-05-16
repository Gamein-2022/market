package org.gamein.marketservergamein2022.core.dto.result.factory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.gamein.marketservergamein2022.core.dto.result.BaseResult;
import org.gamein.marketservergamein2022.core.dto.result.ProductDTO;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class CreatingRequirementsDTO extends BaseResult {
    private List<RequirementDTO> requirements;
    private int basePrice;
    private long balance;
    private ProductDTO product;
    private boolean hasRAndDRequirement;
}
