package org.gamein.marketservergamein2022.core.sharedkernel.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.OfferType;

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

    @Column(name = "type", updatable = false, nullable = false)
    private OfferType type;

    @Column(name = "submit_date", nullable = false)
    private Date submitDate;

    @Column(name = "accept_date")
    private Date acceptDate;

    @OneToOne(optional = false)
    private Product product;

    @Column(name = "product_amount", nullable = false)
    private long productAmount;

    @ManyToOne(optional = false)
    private Team submitter;

    @ManyToOne
    private Team accepter;
}