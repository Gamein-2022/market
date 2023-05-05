package org.gamein.marketservergamein2022.core.sharedkernel.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "time")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Time {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "begin_time")
    private LocalDateTime beginTime;

    @Column(name = "stopped_time")
    private Long stoppedTimeSeconds;

    @Column(name = "choose_region_duration")
    private Long chooseRegionDuration;

    @Column(name = "last_stop")
    private LocalDateTime lastStopTime;

    @Column(name = "is_game_paused", columnDefinition = "boolean default false")
    private Boolean isGamePaused;

    @Column(name = "is_region_payed", columnDefinition = "boolean default false")
    private Boolean isRegionPayed;

    @Column(name = "r_and_d_price_multiplier", nullable = false, columnDefinition = "double precision default 0.25")
    private Double rAndDPriceMultiplier = 0.25;

    @Column(name = "scale", nullable = false, columnDefinition = "bigint default 0.25")
    private Long scale = 1000000L;

    @Column(name = "next_final_order_time")
    private Date nextFinalOrderTime;

    @Column(name = "plane_base_price", nullable = false, columnDefinition = "integer default 30000")
    private Integer planeBasePrice = 30000;

    @Column(name = "ship_base_price", nullable = false, columnDefinition = "integer default 10000")
    private Integer shipBasePrice = 10000;

    @Column(name = "plane_var_price", nullable = false, columnDefinition = "integer default 300")
    private Integer planeVarPrice = 300;

    @Column(name = "ship_var_price", nullable = false, columnDefinition = "integer default 100")
    private Integer shipVarPrice = 100;

    @Column(name = "storage_base_capacity", nullable = false, columnDefinition = "integer default 50000000")
    private Integer storageBaseCapacity = 50000000;

    @Column(name = "storage_upgraded_capacity", nullable = false, columnDefinition = "integer default 75000000")
    private Integer storageUpgradedCapacity = 75000000;
}
