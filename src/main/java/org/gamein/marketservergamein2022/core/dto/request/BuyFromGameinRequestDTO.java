package org.gamein.marketservergamein2022.core.dto.request;

import lombok.Getter;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingMethod;


@Getter
public class BuyFromGameinRequestDTO {
    private Long productId;
    private Integer quantity;
    private ShippingMethod shippingMethod;
}
