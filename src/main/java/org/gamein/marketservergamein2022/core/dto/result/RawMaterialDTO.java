package org.gamein.marketservergamein2022.core.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
public class RawMaterialDTO {
    private final int shipBasePrice = 100;
    private final int shipDistanceFactor = 5;
    private final int planeBasePrice = 200;
    private final int planeDistanceFactor = 10;
    private final long id;
    private final String name;
    private final long price;

    public RawMaterialDTO(ProductDTO productDTO) {
        this.id = productDTO.getId();
        this.name = productDTO.getName();
        this.price = productDTO.getPrice();
    }
}
