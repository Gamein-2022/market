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
@Table(name = "region_distances")
public class RegionDistance {
    @EmbeddedId
    private RegionDistancePK id;

    @Column(name = "distance", nullable = false)
    private int distance;
}
