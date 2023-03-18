package org.gamein.marketservergamein2022.core.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Product;


@AllArgsConstructor
@Getter
public class ProductDTO {
    private final Long id;
    private final String name;
    private final Long price;
}
