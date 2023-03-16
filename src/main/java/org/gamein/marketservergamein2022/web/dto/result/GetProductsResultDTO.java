package org.gamein.marketservergamein2022.web.dto.result;

import lombok.Getter;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Product;
import org.gamein.marketservergamein2022.web.dto.ProductDTO;

import java.util.ArrayList;
import java.util.List;


@Getter
public class GetProductsResultDTO implements BaseResultDTO {
    private List<ProductDTO> myRegion;
    private List<ProductDTO> otherRegions;

    public GetProductsResultDTO(List<Product> myRegion, List<Product> otherRegions) {
        List<ProductDTO> myRegionDTOS = new ArrayList<>();
        myRegion.forEach(product -> myRegionDTOS.add(new ProductDTO(product)));
        this.myRegion = myRegionDTOS;

        List<ProductDTO> otherRegionsDTOS = new ArrayList<>();
        otherRegions.forEach(product -> otherRegionsDTOS.add(new ProductDTO(product)));
        this.otherRegions = otherRegionsDTOS;
    }
}
