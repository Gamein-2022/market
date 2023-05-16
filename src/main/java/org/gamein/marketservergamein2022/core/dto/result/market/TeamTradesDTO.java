package org.gamein.marketservergamein2022.core.dto.result.market;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.gamein.marketservergamein2022.core.dto.result.market.FinalProductSellOrderDTO;
import org.gamein.marketservergamein2022.core.dto.result.market.OrderDTO;

import java.util.List;


@AllArgsConstructor
@Getter
public class TeamTradesDTO {
    List<OrderDTO> orders;
    List<FinalProductSellOrderDTO> finalOrders;
}
