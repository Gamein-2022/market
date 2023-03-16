package org.gamein.marketservergamein2022.core.service;

import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.exception.NotFoundException;
import org.gamein.marketservergamein2022.core.exception.UnauthorizedException;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;
import org.gamein.marketservergamein2022.web.dto.result.*;

public interface TradeService {
    TradeWithGameinResultDTO tradeWithGamein(Team team, String side, Long productId, Long quantity)
            throws BadRequestException;

    GetRawMaterialsResultDTO getRawMaterials(Team team);

    GetProductsResultDTO getIntermediateProducts();

    CreateOfferResultDTO createOffer(Team team, String offerType, Long productId, Long quantity, Long price)
            throws BadRequestException;

    GetAllOffersResultDTO getAllOffers();

    GetAllOffersResultDTO getTeamTrades(Team team) throws BadRequestException;

    AcceptOfferResultDTO acceptOffer(Long offerId, Team accepter, String shippingMethod) throws BadRequestException;

    CreateOfferResultDTO cancelOffer(Team team, Long offerId) throws UnauthorizedException, BadRequestException;

    GetPendingOfferResultDTO getPendingOffers(Team team);

    AcceptSellOfferResultDTO acceptSellOffer(Long pendingOfferId, String shippingMethod, Team team) throws BadRequestException, NotFoundException;

    DeclinePendingOfferResultDTO declineSellOffer(Long pendingOfferId, Team team) throws BadRequestException, NotFoundException;
}
