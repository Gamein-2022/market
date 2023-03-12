package org.gamein.marketservergamein2022.web.dto;

import lombok.Getter;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Product;


@Getter
public class ProductDTO {
    private String name;

    public ProductDTO(Product product) {
        this.name = product.getName();
    }
}
