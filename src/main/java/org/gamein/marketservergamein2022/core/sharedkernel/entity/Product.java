package org.gamein.marketservergamein2022.core.sharedkernel.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gamein.marketservergamein2022.core.dto.result.ProductDTO;

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

    @OneToMany
    private List<Product> components;

    @Column(name = "region", nullable = false)
    private int region;

    @Column(name = "price", nullable = false)
    private long price;

    @Column(name = "available_year", nullable = false)
    private long availableYear;

    @Column(name = "unit_volume", nullable = false)
    private long unitVolume;

    public ProductDTO toDTO() {
        return new ProductDTO(id, name, price, level);
    }
}
