package org.gamein.marketservergamein2022.infrastructure.repository.schedule;

import org.gamein.marketservergamein2022.core.sharedkernel.entity.WealthLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WealthLogRepository extends JpaRepository<WealthLog, Long> {
}
