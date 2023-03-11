package org.gamein.marketservergamein2022.infrastructure.repository;

import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
}
