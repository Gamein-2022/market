package org.gamein.marketservergamein2022.infrastructure.repository.schedule;

import org.gamein.marketservergamein2022.core.sharedkernel.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, Long> {
    Region findFirstByRegionId(Integer regionId);
}
