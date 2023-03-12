package org.gamein.marketservergamein2022.web.dto.result;

import lombok.Getter;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Product;
import org.gamein.marketservergamein2022.web.dto.ProductDTO;

import java.util.ArrayList;
import java.util.List;


@Getter
public class GetAllProductsResultDTO implements BaseResultDTO {
    private List<ProductDTO> products;

    public GetAllProductsResultDTO(List<Product> products) {
        List<ProductDTO> productDTOS = new ArrayList<>();
        products.forEach(product -> productDTOS.add(new ProductDTO(product)));
        this.products = productDTOS;
    }
}
