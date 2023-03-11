package org.gamein.marketservergamein2022.web.dto.request;

import lombok.Getter;


@Getter
public class TradeWithGameinRequestDTO {
    private String side;
    private Long productId;
    private Long quantity;
    private Long price;
}
