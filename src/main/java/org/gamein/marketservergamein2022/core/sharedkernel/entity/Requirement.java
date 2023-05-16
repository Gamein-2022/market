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
@Table(name = "requirements")
public class Requirement {
    @Id
    @Column(name = "id")
    private long id;

    @Column(name = "product_id")
    private long productId;

    @OneToOne
    private Product requirement;

    @Column(name = "count")
    private int count;


}
