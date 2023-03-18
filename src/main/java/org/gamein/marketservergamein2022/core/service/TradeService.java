package org.gamein.marketservergamein2022.core.service;

import org.gamein.marketservergamein2022.core.dto.result.*;
import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingMethod;


public interface TradeService {
    ShippingDTO buyFromGamein(Team team, Long productId, Long quantity, ShippingMethod method)
            throws BadRequestException;

    OrderDTO sellToGamein(Team team, Long productId, Long quantity)
            throws BadRequestException;
}
