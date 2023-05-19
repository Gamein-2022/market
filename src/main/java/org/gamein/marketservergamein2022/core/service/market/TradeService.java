package org.gamein.marketservergamein2022.core.service.market;

import org.gamein.marketservergamein2022.core.dto.result.market.FinalProductSellOrderDTO;
import org.gamein.marketservergamein2022.core.dto.result.market.NextTradeTaskDTO;
import org.gamein.marketservergamein2022.core.dto.result.market.ShippingDTO;
import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.exception.NotFoundException;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingMethod;


public interface TradeService {
    ShippingDTO buyFromGamein(Long teamId, Long productId, Integer quantity, ShippingMethod method)
            throws BadRequestException;

    FinalProductSellOrderDTO sellToGamein(Long teamId, Long productId, Integer quantity, Long price)
            throws NotFoundException, BadRequestException;

    FinalProductSellOrderDTO cancelSellOrder(Long teamId, Long orderId)
            throws NotFoundException, BadRequestException;

    FinalProductSellOrderDTO archiveSellOrder(Team team, Long orderId)
            throws NotFoundException, BadRequestException;

    NextTradeTaskDTO nextTime();
}
