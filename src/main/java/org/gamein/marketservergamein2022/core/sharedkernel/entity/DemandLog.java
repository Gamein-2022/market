package org.gamein.marketservergamein2022.core.sharedkernel.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "demand_logs")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DemandLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "total_demand", nullable = false)
    private Integer totalDemand;

    @Column(name = "first_era_demand", nullable = false)
    private Integer firstEraDemand;

    @Column(name = "second_era_demand", nullable = false)
    private Integer secondEraDemand;

    @Column(name = "third_era_demand", nullable = false)
    private Integer thirdEraDemand;

    @Column(name = "fourth_era_demand", nullable = false)
    private Integer fourthEraDemand;

    @Column(name = "fifth_era_demand", nullable = false)
    private Integer fifthEraDemand;

    @Column(name = "product_demands", nullable = false, columnDefinition = "varchar(4096) default ''")
    private String productDemands;

    @Column(name = "time", nullable = false)
    private LocalDateTime time;

    @Column(name = "demand_id", nullable = false)
    private Long demandId;
}
