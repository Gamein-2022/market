package org.gamein.marketservergamein2022.core.sharedkernel.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gamein.marketservergamein2022.core.dto.result.OfferDTO;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingMethod;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.util.Date;

import static java.lang.Math.abs;


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
    private Date creationDate;

    @Column(name = "accept_date")
    private Date acceptDate;

    @Column(name = "declined", nullable = false, columnDefinition = "boolean default false")
    private Boolean declined = false;

    @Column(name = "cancelled", nullable = false, columnDefinition = "boolean default false")
    private Boolean cancelled = false;

    @Column
    private ShippingMethod shippingMethod;

    @Column(name = "archived", nullable = false, columnDefinition = "boolean default false")
    private Boolean archived = false;

    public OfferDTO toDTO() {
        int distance = abs(offerer.getRegion() - order.getSubmitter().getRegion());
        return new OfferDTO(
                id,
                offerer.getId(),
                order.toDTO(),
                creationDate,
                declined,
                acceptDate,
                cancelled,
                offerer.getRegion(),
                distance * 5,
                distance * 25,
                distance * 50,
                distance * 10,
                null
        );
    }
}
