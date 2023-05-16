package org.gamein.marketservergamein2022.infrastructure.repository.market;

import org.gamein.marketservergamein2022.core.sharedkernel.entity.Demand;
import org.springframework.data.jpa.repository.JpaRepository;


public interface DemandRepository extends JpaRepository<Demand, Long> {
}
