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
@Table(name = "research_subjects")
public class ResearchSubject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private long id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "available_year", nullable = false)
    private int availableYear;

    @Column(name = "base_price", nullable = false)
    private int basePrice;

    @Column(name = "base_duration", nullable = false)
    private int baseDuration;

    @ManyToOne
    private ResearchSubject parent;
}
