package org.gamein.marketservergamein2022.core.dto.result.market;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.gamein.marketservergamein2022.core.dto.result.ProductDTO;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.OrderType;

import java.time.LocalDateTime;


@AllArgsConstructor
@Getter
public class OrderDTO {
    private final Long id;
    private final OrderType orderType;
    private final String submitterTeamName;
    private final ProductDTO product;
    private final Integer quantity;
    private final Long unitPrice;
    private final LocalDateTime submitDate;
    private final Boolean cancelled;
    private final LocalDateTime acceptDate;
    private final int region;
    private final Integer offerCount;
    private final Integer distance;
}
