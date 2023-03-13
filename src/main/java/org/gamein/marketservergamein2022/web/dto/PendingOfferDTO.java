package org.gamein.marketservergamein2022.web.dto;

import lombok.Getter;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.PendingOffer;

import java.util.Date;


@Getter
public class PendingOfferDTO {
    private Long id;
    private Long teamId;
    private Long offerId;
    private Date creationDate;
    private Boolean declined;

    public PendingOfferDTO(PendingOffer pendingOffer) {
        this.id = pendingOffer.getId();
        this.teamId = pendingOffer.getAccepter().getId();
        this.offerId = pendingOffer.getOffer().getId();
        this.creationDate = pendingOffer.getCreationDate();
        this.declined = pendingOffer.getDeclined();
    }
}
