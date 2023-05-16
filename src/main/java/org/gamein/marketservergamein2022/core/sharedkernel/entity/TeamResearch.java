package org.gamein.marketservergamein2022.core.sharedkernel.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gamein.marketservergamein2022.core.dto.result.factory.TeamResearchDTO;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;



@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "team_researches")
public class TeamResearch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private long id;

    @ManyToOne(optional = false)
    private Team team;

    @ManyToOne(optional = false)
    private ResearchSubject subject;

    @Column(name = "begin_time")
    private LocalDateTime beginTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "paid_amount")
    private int paidAmount;





    public TeamResearchDTO toDTO(long balance, int price, int duration) {
        return new TeamResearchDTO(subject.toDTO(), paidAmount, beginTime == null ? null : beginTime.atZone(ZoneId.of(
                "UTC")), LocalDateTime.now(ZoneOffset.UTC).atZone(ZoneId.of("UTC")),
                endTime == null ? null : endTime.atZone(ZoneId.of("UTC")),
                endTime == null ?
                        "not-started" :
                        endTime.isBefore(LocalDateTime.now(ZoneOffset.UTC)) ?
                                "done" :
                                "doing", balance, price, duration);
    }








}
