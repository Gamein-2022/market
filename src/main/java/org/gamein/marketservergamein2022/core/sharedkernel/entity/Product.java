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
    private Long id;

    @Column(name = "level", nullable = false)
    private Integer level;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "pretty_name", nullable = false, columnDefinition = "character varying(255) default ''")
    private String prettyName;

    @ElementCollection()
    @CollectionTable(name = "product_regions",joinColumns = @JoinColumn(name = "product_id"))
    private List<Integer> regions;

    @Column(name = "price")
    private Integer price;

    @Column(name = "available_day")
    private Integer availableDay;

    @ManyToOne
    private ResearchSubject RAndD;

    @Column(name = "production_rate")
    private Long productionRate;

    @Column(name = "unit_volume", nullable = false)
    private Integer unitVolume;

    @Column(name = "demand_coefficient")
    private Double demandCoefficient;

    @Column(name = "fixed_cost")
    private Integer fixedCost;

    @Column(name = "variable_cost")
    private Integer variableCost;

    @Column(name = "min_price")
    private Integer minPrice;

    @Column(name = "max_price")
    private Integer maxPrice;

    @Column(name = "product_group")
    @Enumerated(EnumType.STRING)
    private ProductGroup group;

    public ProductDTO toDTO() {
        return new ProductDTO(id, name, price, level, unitVolume, productionRate, prettyName);
    }
}
