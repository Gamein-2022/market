package org.gamein.marketservergamein2022.core.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;


@AllArgsConstructor
@Getter
public class OfferDTO {
    private Long id;
    private Long offererId;
    private OrderDTO order;
    private Date creationDate;
    private Boolean declined;
    private Date acceptDate;
    private Boolean cancelled;
    private int region;
    private int distance;
    private Long balance;

    public void setBalance(Long balance) {
        this.balance = balance;
    }
}
