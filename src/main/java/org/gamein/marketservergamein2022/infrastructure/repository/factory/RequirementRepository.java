package org.gamein.marketservergamein2022.infrastructure.repository.factory;


import org.gamein.marketservergamein2022.core.sharedkernel.entity.Requirement;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RequirementRepository extends CrudRepository<Requirement,Long> {
    List<Requirement> findRequirementsByProductId(Long productId);
}
