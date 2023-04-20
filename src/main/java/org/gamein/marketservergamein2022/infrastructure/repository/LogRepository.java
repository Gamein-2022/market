package org.gamein.marketservergamein2022.infrastructure.repository;

import org.gamein.marketservergamein2022.core.sharedkernel.entity.Log;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.LogType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface LogRepository extends CrudRepository<Log,Long> {
    List<Log> findAllByTypeAndTeamId(LogType logType,Long teamId);

}
