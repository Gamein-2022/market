package org.gamein.marketservergamein2022.web.dto.request;

import lombok.Getter;

@Getter
public class AcceptPendingOfferRequestDTO {
    private Long pendingOfferId;
    private String shippingMethod;
}
