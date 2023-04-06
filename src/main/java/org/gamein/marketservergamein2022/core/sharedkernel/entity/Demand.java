package org.gamein.marketservergamein2022.core.sharedkernel.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;


@Entity
@Table(name = "demands")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Demand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "minute", unique = true, nullable = false)
    private Long minute;

    @Column(name = "demand")
    private Integer demand;
}
