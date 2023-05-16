package org.gamein.marketservergamein2022.core.dto.request.factory;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.gamein.marketservergamein2022.core.sharedkernel.enums.ProductGroup;

@Getter
@Setter
@AllArgsConstructor
public class InitFactoryLineProductRequestDTO {
    private Long lineId;
    private ProductGroup group;
}
