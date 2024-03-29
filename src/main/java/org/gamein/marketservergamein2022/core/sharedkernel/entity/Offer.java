package org.gamein.marketservergamein2022.core.sharedkernel.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gamein.marketservergamein2022.core.dto.result.market.OfferDTO;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingMethod;
import org.gamein.marketservergamein2022.infrastructure.util.ShippingInfo;
import org.gamein.marketservergamein2022.infrastructure.util.TeamUtil;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.time.LocalDateTime;


@DynamicInsert
@Entity
@Table(name = "offers")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Offer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;
    @ManyToOne(optional = false)
    private Order order;
    @ManyToOne(optional = false)
    private Team offerer;
    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;
    @Column(name = "accept_date")
    private LocalDateTime acceptDate;
    @Column(name = "declined", nullable = false, columnDefinition = "boolean default false")
    private Boolean declined = false;
    @Column(name = "cancelled", nullable = false, columnDefinition = "boolean default false")
    private Boolean cancelled = false;
    @Column
    @Enumerated(EnumType.STRING)
    private ShippingMethod shippingMethod;
    @Column(name = "archived", nullable = false, columnDefinition = "boolean default false")
    private Boolean archived = false;

    public OfferDTO toDTO(int distance, Time time) {
        return new OfferDTO(
                id,
                offerer.getId(),
                order.toDTO(null, distance),
                creationDate,
                declined,
                acceptDate,
                cancelled,
                offerer.getRegion(),
                distance,
                null,
                TeamUtil.calculateShippingDuration(ShippingMethod.PLANE, distance),
                TeamUtil.calculateShippingDuration(ShippingMethod.SHIP, distance),
                ShippingInfo.shipBasePrice,
                ShippingInfo.planeBasePrice,
                ShippingInfo.shipVarPrice,
                ShippingInfo.planeVarPrice
        );
    }
}
