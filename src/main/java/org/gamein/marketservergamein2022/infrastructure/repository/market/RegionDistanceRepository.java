package org.gamein.marketservergamein2022.infrastructure.repository.market;

import org.gamein.marketservergamein2022.core.sharedkernel.entity.RegionDistance;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.RegionDistancePK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RegionDistanceRepository extends JpaRepository<RegionDistance, RegionDistancePK> {
    @Query(value = "SELECT MIN (rd.distance) FROM RegionDistance as rd WHERE rd.id.sourceRegion IN :sources AND " +
            "rd.id.destRegion = :dest")
    Integer minDistance(List<Integer> sources, Integer dest);

    @Query(value = "SELECT rd.id.sourceRegion FROM RegionDistance AS rd WHERE rd.id.destRegion = :dest AND rd.id" +
            ".sourceRegion IN :sources ORDER BY rd.distance")
    List<Integer> minDistanceRegion(Integer dest, List<Integer> sources);
}
