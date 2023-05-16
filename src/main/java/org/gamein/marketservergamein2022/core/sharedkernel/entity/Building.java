package org.gamein.marketservergamein2022.core.sharedkernel.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gamein.marketservergamein2022.core.dto.result.factory.BuildingDTO;
import org.gamein.marketservergamein2022.core.dto.result.factory.BuildingDetailsDTO;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.BuildingType;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.LineStatus;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


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

    @Column(name = "upgraded", columnDefinition = "boolean default false")
    private boolean upgraded = false;

    @Column(name = "ground", nullable = false)
    private Byte ground;

    @OneToMany(cascade = CascadeType.REMOVE)
    private List<FactoryLine> lines;

    public void addLine(FactoryLine line) {
        if (lines == null) {
            lines = new ArrayList<>();
        }
        lines.add(line);
        line.setBuilding(this);
    }

    public BuildingDTO toDTO() {
        return new BuildingDTO(
                type,
                upgraded,
                lines.stream().map(line -> line.getStatus() == LineStatus.IN_PROGRESS).collect(Collectors.toList())
        );
    }

    public BuildingDetailsDTO toDetailsDTO(int upgradePrice) {
        return new BuildingDetailsDTO(
                id,
                lines.stream().map(FactoryLine::toDTO).collect(Collectors.toList()),
                upgraded,
                upgradePrice
        );
    }
}
