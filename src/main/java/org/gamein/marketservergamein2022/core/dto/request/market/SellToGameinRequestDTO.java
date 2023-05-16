package org.gamein.marketservergamein2022.core.dto.request.market;

import lombok.Getter;


@Getter
public class SellToGameinRequestDTO {
    private Long productId;
    private Integer quantity;
    private Long price;
}
