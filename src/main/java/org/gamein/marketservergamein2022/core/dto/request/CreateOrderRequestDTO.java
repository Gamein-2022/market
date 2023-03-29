package org.gamein.marketservergamein2022.core.dto.request;

import lombok.Getter;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.OrderType;


@Getter
public class CreateOrderRequestDTO {
    private OrderType orderType;
    private Long productId;
    private Integer quantity;
    private Long price;
}
