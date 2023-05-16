package org.gamein.marketservergamein2022.core.sharedkernel.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.List;


@DynamicInsert
@Entity
@Table(name = "teams")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @OneToMany
    private List<User> users;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Shipping> shippings;

    @OneToOne(optional = false)
    private User owner;

    @Column(name = "balance", nullable = false)
    private long balance;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "region")
    private int region;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany
    private List<StorageProduct> storageProducts;

    @Column(name = "reserved_space", nullable = false, columnDefinition = "int default 0")
    private int reservedSpace = 0;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany
    private List<Building> buildings;

    @Column(name = "is_storage_upgraded",columnDefinition = "boolean default false")
    private Boolean isStorageUpgraded;

    @Column(name = "is_region_upgraded", columnDefinition = "boolean default false")
    private Boolean isRegionUpgraded = false;
}