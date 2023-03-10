package org.gamein.marketservergamein2022.core.sharedkernel.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

//@Entity
//@Table(name = "products")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @Column(name = "level")
    private int level;

    @Column(name = "name")
    private String name;

    @OneToMany
    private List<Product> components;

    @Column(name = "volume")
    long volume;

    @Column(name = "region")
    int region;

    @Column(name = "price")
    long price;
}
