package org.gamein.marketservergamein2022.core.service.market;

import org.gamein.marketservergamein2022.core.dto.result.market.OfferDTO;
import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.exception.NotFoundException;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingMethod;

import java.util.List;


public interface OfferService {
    OfferDTO createOffer(Long teamId, Long orderId, ShippingMethod shippingMethod)
            throws BadRequestException, NotFoundException;

    List<OfferDTO> getReceivedOffers(Long teamId);

    List<OfferDTO> getOrderOffers(Long teamId, Long orderId);

    List<OfferDTO> getSentOffers(Long teamId);

    OfferDTO acceptOffer(Long teamId, Long offerId, ShippingMethod shippingMethod)
            throws BadRequestException, NotFoundException;


    OfferDTO declineOffer(Long teamId, Long offerId)
            throws BadRequestException, NotFoundException;

    OfferDTO cancelOffer(Long teamId, Long offerId)
            throws BadRequestException, NotFoundException;

    OfferDTO archiveOffer(Team team, Long offerId)
        throws BadRequestException, NotFoundException;
}
