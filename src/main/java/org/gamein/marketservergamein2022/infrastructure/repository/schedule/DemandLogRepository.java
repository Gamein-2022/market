package org.gamein.marketservergamein2022.infrastructure.repository.schedule;

import org.gamein.marketservergamein2022.core.sharedkernel.entity.DemandLog;
import org.springframework.data.jpa.repository.JpaRepository;


public interface DemandLogRepository extends JpaRepository<DemandLog, Long> {
}
