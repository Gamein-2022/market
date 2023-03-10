package org.gamein.marketservergamein2022.core.sharedkernel.entity;

import lombok.Getter;

import javax.persistence.*;


//@Entity
@Getter
//@Table(name = "storage_products")
public class StorageProduct  {
    @Column(name = "productId")
    String productId;

    @Column(name = "status")
    String status;

    @Column(name = "amount")
    long amount;

    @ManyToOne
    private Factory factory;
}