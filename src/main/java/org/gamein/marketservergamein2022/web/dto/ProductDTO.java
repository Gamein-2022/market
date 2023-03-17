package org.gamein.marketservergamein2022.web.dto;

import lombok.Getter;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Product;


@Getter
public class ProductDTO {
    private final Long id;
    private final String name;
    private final Long price;
    private final int shipBasePrice = 100;
    private final int shipDistanceFactor = 5;
    private final int planeBasePrice = 200;
    private final int planeDistanceFactor = 10;

    public ProductDTO(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.price = product.getPrice();
    }
}
