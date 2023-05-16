package org.gamein.marketservergamein2022.infrastructure.repository.factory;


import org.gamein.marketservergamein2022.core.sharedkernel.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BuildingRepository extends JpaRepository<Building, Long> {
    List<Building> findAllByTeamId(Long teamId);
    Optional<Building> findByGroundAndTeam_Id(Byte ground, Long teamId);
}
