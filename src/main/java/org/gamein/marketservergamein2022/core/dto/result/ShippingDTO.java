package org.gamein.marketservergamein2022.core.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Shipping;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingMethod;

import java.time.LocalDateTime;
import java.util.Date;


@AllArgsConstructor
@Getter
public class ShippingDTO {
    private Integer sourceRegion;
    private Long teamId;
    private ShippingMethod method;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private ProductDTO product;
    private int amount;
}
