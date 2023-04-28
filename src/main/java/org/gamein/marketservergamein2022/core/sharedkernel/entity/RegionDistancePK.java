package org.gamein.marketservergamein2022.core.sharedkernel.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;


@Embeddable
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RegionDistancePK implements Serializable {
    @Column(name = "source_region", nullable = false)
    private int sourceRegion;

    @Column(name = "dest_region", nullable = false)
    private int destRegion;
}
