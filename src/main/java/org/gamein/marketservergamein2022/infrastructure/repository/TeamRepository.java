package org.gamein.marketservergamein2022.infrastructure.repository;

import org.gamein.marketservergamein2022.core.dto.result.schedule.RegionDTO;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {
    @Query(value = "SELECT COUNT(*) FROM Team")
    Long getTeamsCount();

    @Query("select new org.gamein.marketservergamein2022.core.dto.result.schedule.RegionDTO(t.region,count(t))  from Team as t group by t.region")
    List<RegionDTO> getRegionsPopulation();
}
