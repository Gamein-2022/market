package org.gamein.marketservergamein2022.core.dto.result.factory;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.gamein.marketservergamein2022.core.dto.result.ProductDTO;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ProductGroup;

import java.util.List;


@Getter
@AllArgsConstructor
public class ProductGroupDTO {
    List<ProductDTO> products;
    ProductGroup name;
}
