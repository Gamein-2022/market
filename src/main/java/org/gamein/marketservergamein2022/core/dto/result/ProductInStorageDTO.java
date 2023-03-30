package org.gamein.marketservergamein2022.core.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class ProductInStorageDTO {
    private ProductDTO product;
    private int inStorage;
}
