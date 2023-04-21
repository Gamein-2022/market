package org.gamein.marketservergamein2022.core.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public class ProductDTO {
    private final Long id;
    private final String name;
    private final Integer price;
    private final Integer level;
    private final Integer unitVolume;
    private final Long productionRate;
    private final String prettyName;
    private final String prettyGroup;
    private final Integer minPrice;
    private final Integer maxPrice;
}
