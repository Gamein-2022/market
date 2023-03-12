package org.gamein.marketservergamein2022.web.dto.result;

import lombok.Getter;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.PendingOffer;
import org.gamein.marketservergamein2022.web.dto.PendingOfferDTO;


@Getter
public class AcceptBuyOfferResultDTO implements AcceptOfferResultDTO {
    private final PendingOfferDTO pendingOfferDTO;

    public AcceptBuyOfferResultDTO(PendingOffer pendingOffer) {
        this.pendingOfferDTO = new PendingOfferDTO(pendingOffer);
    }
}
