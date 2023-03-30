package org.gamein.marketservergamein2022.core.sharedkernel.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gamein.marketservergamein2022.core.dto.result.OrderDTO;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.OrderType;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.util.Date;


@DynamicInsert
@Entity
@Table(name = "orders")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "type", updatable = false, nullable = false)
    private OrderType type;

    @Column(name = "unit_price", updatable = false, nullable = false)
    private Long unitPrice;

    @Column(name = "submit_date", nullable = false)
    private Date submitDate;

    @Column(name = "cancelled", nullable = false, columnDefinition = "boolean default false")
    private Boolean cancelled = false;

    @Column(name = "accept_date")
    private Date acceptDate;

    @ManyToOne(optional = false)
    private Product product;

    @Column(name = "product_amount", nullable = false)
    private Integer productAmount;

    @Column(name = "archived", nullable = false, columnDefinition = "boolean default false")
    private Boolean archived = false;

    @ManyToOne(optional = false)
    private Team submitter;

    @ManyToOne
    private Team accepter;

    public OrderDTO toDTO() {
        return new OrderDTO(
                id,
                type,
                submitter.getName(),
                product.getName(),
                productAmount,
                unitPrice,
                submitDate,
                cancelled,
                acceptDate,
                submitter.getRegion()
        );
    }
}