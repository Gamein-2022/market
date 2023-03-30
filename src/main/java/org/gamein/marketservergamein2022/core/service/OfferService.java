package org.gamein.marketservergamein2022.core.service;

import org.gamein.marketservergamein2022.core.dto.result.OfferDTO;
import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.exception.NotFoundException;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingMethod;

import java.util.List;


public interface OfferService {
    OfferDTO createOffer(Team team, Long orderId, ShippingMethod shippingMethod)
            throws BadRequestException, NotFoundException;

    List<OfferDTO> getReceivedOffers(Long teamId);

    List<OfferDTO> getOrderOffers(Long teamId, Long orderId);

    List<OfferDTO> getSentOffers(Long teamId);

    OfferDTO acceptOffer(Team team, Long offerId, ShippingMethod shippingMethod)
            throws BadRequestException, NotFoundException;

    OfferDTO declineOffer(Team team, Long offerId)
            throws BadRequestException, NotFoundException;

    OfferDTO cancelOffer(Team team, Long offerId)
            throws BadRequestException, NotFoundException;

    OfferDTO archiveOffer(Team team, Long offerId)
        throws BadRequestException, NotFoundException;
}
