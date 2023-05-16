package org.gamein.marketservergamein2022.core.dto.request.market;

import lombok.Getter;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingMethod;


@Getter
public class CreateOfferRequestDTO {
    private Long orderId;
    private ShippingMethod shippingMethod;
}
