package org.gamein.marketservergamein2022.core.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;


@AllArgsConstructor
@Getter
public class ReceivedOffersDTO {
    private List<OfferDTO> offers;
    private long balance;
}
