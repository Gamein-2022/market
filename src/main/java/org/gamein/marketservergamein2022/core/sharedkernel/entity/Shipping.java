package org.gamein.marketservergamein2022.core.sharedkernel.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gamein.marketservergamein2022.core.dto.result.ShippingDTO;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingMethod;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingStatus;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "shipping")
public class Shipping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private long id;

    @Column(name = "method", nullable = false)
    private ShippingMethod method;

    @Column(name = "source_region", nullable = false)
    private Integer sourceRegion;

    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;

    @Column(name = "status")
    private ShippingStatus status;

    @ManyToOne(optional = false)
    private Team team;

    @ManyToOne(optional = false,cascade = CascadeType.ALL)
    private Product product;

    @Column(name = "amount", nullable = false)
    private int amount;

    public ShippingDTO toDTO() {
        return new ShippingDTO(
                sourceRegion, team.getId(), method, departureTime, arrivalTime, product.toDTO(), amount
        );
    }
}
