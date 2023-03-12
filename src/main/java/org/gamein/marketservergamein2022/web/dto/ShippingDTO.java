package org.gamein.marketservergamein2022.web.dto;

import lombok.Getter;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Shipping;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingMethod;

import java.util.Date;


@Getter
public class ShippingDTO {
    private Integer sourceRegion;
    private Long teamId;
    private ShippingMethod method;
    private Date departureTime;
    private Date arrivalTime;

    public ShippingDTO(Shipping shipping) {
        this.sourceRegion = shipping.getSourceRegion();
        this.method = shipping.getMethod();
        this.teamId = shipping.getTeam().getId();
        this.departureTime = shipping.getDepartureTime();
        this.arrivalTime = shipping.getArrivalTime();
    }
}
