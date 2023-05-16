package org.gamein.marketservergamein2022.core.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.gamein.marketservergamein2022.core.dto.result.market.ShippingDTO;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.OrderType;


@Getter
@AllArgsConstructor
public class TradeLogsDTO {
    OrderType type;
    ProductDTO product;
    int count;
    int sourceRegion;
    int targetRegion;
    ShippingDTO shipping;
    int cost;
}
