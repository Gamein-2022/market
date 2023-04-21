package org.gamein.marketservergamein2022.core.sharedkernel.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gamein.marketservergamein2022.core.dto.result.FinalProductSellOrderDTO;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.util.Date;


@DynamicInsert
@Entity
@Table(name = "final_product_sell_order")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FinalProductSellOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private long id;

    @Column(name = "unit_price", updatable = false, nullable = false)
    private Long unitPrice;

    @Column(name = "submit_date", nullable = false)
    private Date submitDate;

    @Column(name = "cancelled", nullable = false, columnDefinition = "boolean default false")
    private Boolean cancelled = false;

    @Column(name = "closed", nullable = false, columnDefinition = "boolean default false")
    private Boolean closed = false;

    @Column(name = "accept_date")
    private Date acceptDate;

    @ManyToOne(optional = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "sold_quantity", nullable = false, columnDefinition = "integer default 0")
    private Integer soldQuantity = 0;

    @Column(name = "archived", nullable = false, columnDefinition = "boolean default false")
    private Boolean archived = false;

    @ManyToOne(optional = false)
    private Team submitter;

    public FinalProductSellOrderDTO toDTO() {
        return new FinalProductSellOrderDTO(
          id,
          unitPrice,
          submitDate,
          cancelled,
          acceptDate,
          product.toDTO(),
          quantity,
          soldQuantity,
          submitter.getId()
        );
    }
}
