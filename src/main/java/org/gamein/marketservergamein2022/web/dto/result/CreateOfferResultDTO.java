package org.gamein.marketservergamein2022.web.dto.result;

import lombok.Getter;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Offer;
import org.gamein.marketservergamein2022.web.dto.OfferDTO;


@Getter
public class CreateOfferResultDTO implements BaseResultDTO {
    private OfferDTO offer;

    public CreateOfferResultDTO(Offer offer) {
        this.offer = new OfferDTO(offer);
    }
}
