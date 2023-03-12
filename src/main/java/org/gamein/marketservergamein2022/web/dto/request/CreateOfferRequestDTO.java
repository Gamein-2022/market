package org.gamein.marketservergamein2022.web.dto.request;

import lombok.Getter;


@Getter
public class CreateOfferRequestDTO {
    private String offerType;
    private Long productId;
    private Long quantity;
    private Long price;
}
