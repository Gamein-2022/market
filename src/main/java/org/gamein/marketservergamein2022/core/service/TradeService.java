package org.gamein.marketservergamein2022.core.service;

import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.exception.NotFoundException;
import org.gamein.marketservergamein2022.core.exception.UnauthorizedException;
import org.gamein.marketservergamein2022.web.dto.OfferDTO;
import org.gamein.marketservergamein2022.web.dto.result.*;

public interface TradeService {
    TradeWithGameinResultDTO tradeWithGamein(Long teamId, String side, Long productId, Long quantity)
            throws BadRequestException;

    GetAllProductsResultDTO getAllProducts();

    CreateOfferResultDTO createOffer(Long teamId, String offerType, Long productId, Long quantity, Long price)
            throws BadRequestException;

    GetAllOffersResultDTO getAllOffers();

    GetAllOffersResultDTO getTeamTrades(Long teamId) throws BadRequestException;

    AcceptOfferResultDTO acceptOffer(Long offerId, Long accepterId, String shippingMethod) throws BadRequestException;

    CreateOfferResultDTO cancelOffer(Long teamId, Long offerId) throws UnauthorizedException, BadRequestException;

    GetPendingOfferResultDTO getPendingOffers(Long teamId);

    AcceptSellOfferResultDTO acceptSellOffer(Long pendingOfferId, String shippingMethod, Long teamId) throws BadRequestException, NotFoundException;
}
