package org.gamein.marketservergamein2022.core.dto.result.market;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.gamein.marketservergamein2022.core.dto.result.ProductDTO;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingMethod;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingStatus;

import java.sql.Timestamp;


@AllArgsConstructor
@Getter
public class ShippingDTO {
    private Long id;
    private Integer sourceRegion;
    private Long teamId;
    private ShippingMethod method;
    private ShippingStatus status;
    private Timestamp departureTime;
    private Timestamp arrivalTime;
    private Timestamp currentTime;
    private ProductDTO product;
    private int amount;
    private boolean collectable;
}
