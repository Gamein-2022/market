package org.gamein.marketservergamein2022.web.dto.result;

import lombok.Getter;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Shipping;
import org.gamein.marketservergamein2022.web.dto.ShippingDTO;


@Getter
public class AcceptSellOfferResultDTO implements AcceptOfferResultDTO {
    private ShippingDTO shipping;

    public AcceptSellOfferResultDTO(Shipping shipping) {
        this.shipping = new ShippingDTO(shipping);
    }
}
