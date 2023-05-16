package org.gamein.marketservergamein2022.core.dto.result.factory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gamein.marketservergamein2022.core.dto.result.BaseProductDTO;
import org.gamein.marketservergamein2022.core.dto.result.ProductDTO;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RequirementDTO {
    private BaseProductDTO product;
    private long inStorage;
    private int numberPerOne;
}
