package org.gamein.marketservergamein2022.core.sharedkernel.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "wealth_logs")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class WealthLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @ManyToOne(optional = false)
    private Team team;

    @Column(name = "wealth", nullable = false)
    private Long wealth;

    @Column(name = "time", nullable = false)
    private LocalDateTime time;

    @Column(name = "ten_minute_round", nullable = false)
    private Long tenMinuteRound;
}
