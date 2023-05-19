package org.gamein.marketservergamein2022.infrastructure.repository;

import org.gamein.marketservergamein2022.core.dto.result.schedule.RegionDTO;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.BuildingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {
    @Query(value = "SELECT COUNT(*) FROM Team")
    Long getTeamsCount();

    @Query("select new org.gamein.marketservergamein2022.core.dto.result.schedule.RegionDTO(t.region,count(t))  from Team as t group by t.region")
    List<RegionDTO> getRegionsPopulation();

    @Query("SELECT COUNT (*) > 0 FROM Team AS t WHERE t.id = :teamId AND t.id IN (SELECT b.team.id FROM Building AS b" +
            " WHERE b.type = :buildingType) AND t.id IN (SELECT tr.team.id FROM TeamResearch AS tr WHERE tr.id = " +
            ":parentId)")
    Boolean isTeamEligible(Long teamId, BuildingType buildingType, Long parentId);

    @Query("SELECT COUNT (*) > 0 FROM Team AS t WHERE t.id = :teamId AND t.id IN (SELECT b.team.id FROM Building AS b" +
            " WHERE b.type = :buildingType)")
    Boolean isTeamEligible(Long teamId, BuildingType buildingType);

    @Modifying
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Query(value = "UPDATE teams AS t set balance = GREATEST (balance - (sc.sum / :scale),0) FROM " +
            "(SELECT sp.team_id, SUM (sp.in_storage_amount * p.min_price) FROM products AS p JOIN storage_products AS" +
            " sp ON sp.product_id = p.id GROUP BY sp.team_id) AS sc WHERE sc.team_id = t.id;", nativeQuery = true)
    void updateStorageCost(Long scale);
}
