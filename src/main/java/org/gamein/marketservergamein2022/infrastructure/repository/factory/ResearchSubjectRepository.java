package org.gamein.marketservergamein2022.infrastructure.repository.factory;

import org.gamein.marketservergamein2022.core.sharedkernel.entity.ResearchSubject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResearchSubjectRepository extends JpaRepository<ResearchSubject, Long> {
    ResearchSubject findByName(String name);
}
