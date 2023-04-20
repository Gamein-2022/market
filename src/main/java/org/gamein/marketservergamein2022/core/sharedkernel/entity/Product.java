package org.gamein.marketservergamein2022.core.sharedkernel.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gamein.marketservergamein2022.core.dto.result.ProductDTO;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ProductGroup;

import javax.persistence.*;
import java.util.List;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private long id;

    @Column(name = "level", nullable = false)
    private int level;

    @Column(name = "name", nullable = false)
    private String name;

    @ElementCollection
    private List<Integer> regions;

    @Column(name = "price")
    private double price;

    @Column(name = "available_day")
    private int availableDay;

    @ManyToOne
    private ResearchSubject RAndD;

    @Column(name = "production_rate")
    private Long productionRate;

    @Column(name = "unit_volume", nullable = false)
    private int unitVolume;

    @Column(name = "demand_coefficient")
    private Double demandCoefficient;

    @Column(name = "fixed_cost")
    private int fixedCost;

    @Column(name = "variable_cost")
    private int variableCost;

    @Column(name = "min_price")
    private int minPrice;

    @Column(name = "max_price")
    private int maxPrice;

    @Column(name = "product_group")
    @Enumerated(EnumType.STRING)
    private ProductGroup group;

    public ProductDTO toDTO() {
        return new ProductDTO(id, name, price, level, unitVolume, productionRate);
    }
}
