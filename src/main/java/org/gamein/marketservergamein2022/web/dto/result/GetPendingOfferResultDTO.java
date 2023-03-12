package org.gamein.marketservergamein2022.web.dto.result;

import lombok.Getter;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.PendingOffer;
import org.gamein.marketservergamein2022.web.dto.PendingOfferDTO;

import java.util.ArrayList;
import java.util.List;


@Getter
public class GetPendingOfferResultDTO implements BaseResultDTO {
    List<PendingOfferDTO> pendingOffers;

    public GetPendingOfferResultDTO(List<PendingOffer> pendingOffers) {
        List<PendingOfferDTO> pendingOfferDTOS = new ArrayList<>();
        pendingOffers.forEach(pendingOffer -> pendingOfferDTOS.add(new PendingOfferDTO(pendingOffer)));
        this.pendingOffers = pendingOfferDTOS;
    }
}
