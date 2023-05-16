package org.gamein.marketservergamein2022.core.dto.result.factory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.gamein.marketservergamein2022.core.dto.result.ProductDTO;


@Getter
@AllArgsConstructor
public class ProductInStorageDTO {
    private ProductDTO product;
    private int inStorage;
}
