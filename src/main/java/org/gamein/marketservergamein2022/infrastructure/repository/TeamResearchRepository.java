package org.gamein.marketservergamein2022.infrastructure.repository;

import org.gamein.marketservergamein2022.core.sharedkernel.entity.TeamResearch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamResearchRepository extends JpaRepository<TeamResearch, Long> {
    TeamResearch findFirstBySubject_IdOrderByEndTime(Long id);
}
