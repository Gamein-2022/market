package org.gamein.marketservergamein2022.core.sharedkernel.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gamein.marketservergamein2022.core.dto.result.OfferDTO;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingMethod;

import javax.persistence.*;
import java.util.Date;


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

    @Column(name = "declined")
    private Boolean declined;

    @Column
    private ShippingMethod shippingMethod;

    public OfferDTO toDTO() {
        return new OfferDTO(
                id,
                offerer.getId(),
                order.getId(),
                creationDate,
                declined,
                acceptDate
        );
    }
}
