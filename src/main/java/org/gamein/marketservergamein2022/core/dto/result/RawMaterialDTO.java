package org.gamein.marketservergamein2022.core.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Product;


@Getter
public class RawMaterialDTO extends ProductDTO {
    private final int shipBasePrice = 100;
    private final int shipDistanceFactor = 5;
    private final int planeBasePrice = 200;
    private final int planeDistanceFactor = 10;

    public RawMaterialDTO(Long id, String name, Long price) {
        super(id, name, price);
    }
}
