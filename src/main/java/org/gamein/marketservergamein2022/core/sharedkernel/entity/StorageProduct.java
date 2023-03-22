package org.gamein.marketservergamein2022.core.sharedkernel.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "storage_products")
public class StorageProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private long id;

    @ManyToOne(optional = false)
    private Product product;

    @Column(name = "in_storage_amount", nullable = false)
    private long inStorageAmount;

    @Column(name = "in_queue_amount", nullable = false)
    private long inQueueAmount;

    @Column(name = "manufacturing_amount", nullable = false)
    private long manufacturingAmount;

    @Column(name = "in_route_amount", nullable = false)
    private long inRouteAmount;

    @ManyToOne(optional = false)
    private Team team;
}