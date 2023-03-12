package org.gamein.marketservergamein2022.web.dto;

import lombok.Getter;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Offer;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.OfferType;

import java.util.Date;


@Getter
public class OfferDTO {
    private final Long id;
    private final OfferType offerType;
    private final String submitterTeamName;
    private final String productName;
    private final Long quantity;
    private final Long price;
    private final Date submitDate;
    private final Boolean cancelled;
    private final Date acceptDate;

    public OfferDTO(Offer offer) {
        this.id = offer.getId();
        this.offerType = offer.getType();
        this.submitterTeamName = offer.getSubmitter().getName();
        this.productName = offer.getProduct().getName();
        this.quantity = offer.getProductAmount();
        this.price = offer.getPrice();
        this.submitDate = offer.getSubmitDate();
        this.cancelled = offer.getCancelled();
        this.acceptDate = offer.getAcceptDate();
    }
}
