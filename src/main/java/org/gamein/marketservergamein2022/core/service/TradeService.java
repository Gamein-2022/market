package org.gamein.marketservergamein2022.core.service;

import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.web.dto.result.CreateOfferResultDTO;
import org.gamein.marketservergamein2022.web.dto.result.GetAllProductsResultDTO;
import org.gamein.marketservergamein2022.web.dto.result.TradeWithGameinResultDTO;

public interface TradeService {
    TradeWithGameinResultDTO tradeWithGamein(Long teamId, String side, Long productId, Long quantity)
            throws BadRequestException;

    GetAllProductsResultDTO getAllProducts();

    CreateOfferResultDTO createOffer(Long teamId, String offerType, Long productId, Long quantity, Long price)
            throws BadRequestException;
}
