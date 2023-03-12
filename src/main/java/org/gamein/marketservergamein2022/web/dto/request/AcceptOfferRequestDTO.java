package org.gamein.marketservergamein2022.web.dto.request;

import lombok.Getter;


@Getter
public class AcceptOfferRequestDTO {
    private Long offerId;
    private String shippingMethod;
}
