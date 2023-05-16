package org.gamein.marketservergamein2022.infrastructure.repository.factory;


import org.gamein.marketservergamein2022.core.sharedkernel.entity.BuildingInfo;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.BuildingType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuildingInfoRepository extends JpaRepository<BuildingInfo, BuildingType> {
}
