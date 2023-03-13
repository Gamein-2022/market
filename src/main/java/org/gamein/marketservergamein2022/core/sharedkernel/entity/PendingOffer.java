package org.gamein.marketservergamein2022.core.sharedkernel.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;


@Entity
@Table(name = "pending_offers")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PendingOffer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @ManyToOne(optional = false)
    private Offer offer;

    @ManyToOne(optional = false)
    private Team accepter;

    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Column(name = "accept_date")
    private Date acceptDate;

    @Column(name = "declined")
    private Boolean declined;
}
