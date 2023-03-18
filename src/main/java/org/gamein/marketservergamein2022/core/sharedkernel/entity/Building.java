package org.gamein.marketservergamein2022.core.sharedkernel.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gamein.marketservergamein2022.core.dto.result.BuildingDTO;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.BuildingType;

import javax.persistence.*;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "buildings")
public class Building {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private long id;

    @ManyToOne(optional = false)
    private Team team;

    @Column(name = "type")
    private BuildingType type;

    @Column(name = "upgraded")
    private boolean upgraded;

    @Column(name = "position")
    private byte position;

    public BuildingDTO toDTO() {
        return new BuildingDTO(
                id,
                type,
                position,
                upgraded
        );
    }
}
