package org.gamein.marketservergamein2022.core.sharedkernel.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.gamein.marketservergamein2022.core.sharedkernel.enums.BuildingType;

import javax.persistence.*;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "buildings_info")
public class BuildingInfo {
    @Id
    @Column(name = "type", unique = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private BuildingType type;

    @Column(name = "build_price")
    private int buildPrice;

    @Column(name = "upgrade_price")
    private int upgradePrice;

    @Column(name = "base_line_count")
    private int baseLineCount;

    @Column(name = "upgraded_line_count")
    private int upgradeLineCount;
}
