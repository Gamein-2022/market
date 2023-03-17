package org.gamein.marketservergamein2022.web.dto.result;

import lombok.Getter;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Product;
import org.gamein.marketservergamein2022.web.dto.ProductDTO;

import java.util.ArrayList;
import java.util.List;


@Getter
public class GetRawMaterialsResultDTO implements BaseResultDTO {
    private final List<ProductDTO> myRegion;
    private final List<ProductDTO> otherRegions;
    private final int shipBasePrice = 100;
    private final int shipDistanceFactor = 5;
    private final int planeBasePrice = 200;
    private final int planeDistanceFactor = 10;

    public GetRawMaterialsResultDTO(List<Product> myRegion, List<Product> otherRegions) {
        List<ProductDTO> myRegionDTOS = new ArrayList<>();
        myRegion.forEach(product -> myRegionDTOS.add(new ProductDTO(product)));
        this.myRegion = myRegionDTOS;

        List<ProductDTO> otherRegionsDTOS = new ArrayList<>();
        otherRegions.forEach(product -> otherRegionsDTOS.add(new ProductDTO(product)));
        this.otherRegions = otherRegionsDTOS;
    }
}
