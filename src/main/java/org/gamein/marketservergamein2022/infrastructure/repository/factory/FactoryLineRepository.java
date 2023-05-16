package org.gamein.marketservergamein2022.infrastructure.repository.factory;


import org.gamein.marketservergamein2022.core.sharedkernel.entity.FactoryLine;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface FactoryLineRepository extends CrudRepository<FactoryLine,Long> {
    List<FactoryLine> findAllByTeamId(Long teamId);
    List<FactoryLine> findALlByBuilding_IdAndEndTimeIsNull(Long buildingId);
}
