package org.gamein.marketservergamein2022.web.dto.result;

import lombok.Getter;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Offer;
import org.gamein.marketservergamein2022.web.dto.OfferDTO;

import java.util.ArrayList;
import java.util.List;


@Getter
public class GetAllOffersResultDTO implements BaseResultDTO {
    List<OfferDTO> offers;

    public GetAllOffersResultDTO(List<Offer> offers) {
        List<OfferDTO> offerDTOS = new ArrayList<>();
        offers.forEach(offer -> offerDTOS.add(new OfferDTO(offer)));
        this.offers = offerDTOS;
    }
}
