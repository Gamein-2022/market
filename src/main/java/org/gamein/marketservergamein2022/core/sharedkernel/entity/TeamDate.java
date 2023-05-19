package org.gamein.marketservergamein2022.core.sharedkernel.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class TeamDate {

    @Id
    private Long team_id;

    @Column(name = "date")
    LocalDateTime dateTime;


}
