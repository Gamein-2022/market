package org.gamein.marketservergamein2022.core.dto.result.market;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.gamein.marketservergamein2022.core.dto.result.market.OfferDTO;

import java.util.List;


@AllArgsConstructor
@Getter
public class ReceivedOffersDTO {
    private List<OfferDTO> offers;
    private long balance;
}
