package org.gamein.marketservergamein2022.infrastructure.repository.factory;

import org.gamein.marketservergamein2022.core.sharedkernel.entity.TeamResearch;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.BuildingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface TeamResearchRepository extends JpaRepository<TeamResearch, Long> {
    TeamResearch findFirstBySubject_IdAndEndTimeIsBeforeOrderByEndTime(Long id, LocalDateTime now);
    TeamResearch findFirstBySubject_IdOrderByEndTime(Long id);
    List<TeamResearch> findAllByTeam_Id(Long teamId);
    TeamResearch findFirstByEndTimeAfter(LocalDateTime date);
    TeamResearch findFirstByEndTimeAfterAndTeamId(LocalDateTime date,Long teamId);
    Optional<TeamResearch> findByTeam_IdAndSubject_Name(Long teamId, String name);
    TeamResearch findByTeam_IdAndSubject_IdAndEndTimeBefore(Long teamId, Long subjectId, LocalDateTime now);

    @Query(value = "SELECT COUNT(*) FROM team_researches AS t WHERE " +
            "t.subject_id = :subjectId AND t.begin_time + (t.end_time - t.begin_time) / 2 < :now", nativeQuery = true)
    Long getResearchCount(Long subjectId, LocalDateTime now);

    @Query(value = "SELECT * FROM team_researches AS t WHERE " +
            "t.subject_id = :subjectId AND t.begin_time + (t.end_time - t.begin_time) / 2  < :now ORDER BY t.end_time",
            nativeQuery = true)
    List<TeamResearch> findFirstResearch(Long subjectId, LocalDateTime now);

    List<TeamResearch> findAllBySubject_IdAndEndTimeBefore(Long subjectId, LocalDateTime now);

    Boolean existsByTeam_IdAndSubject_Id(Long teamId, Long subjectId);

    List<TeamResearch> findAllByTeamIdAndAndEndTimeAfter(Long teamId, LocalDateTime endTime);

    @Query(value = "SELECT COALESCE( AVG (tr.team.balance), 200000000) FROM TeamResearch AS tr WHERE tr.subject.id = " +
            ":parentId AND tr" +
            ".team" +
            ".id NOT IN (SELECT tr2.team.id " +
            "FROM TeamResearch AS tr2 WHERE tr2.subject.id = :subjectId) AND tr.team.id IN (SELECT b.team.id FROM" +
            " Building AS b WHERE b.type = :buildingType)")
    Double avgTeamBalanceWithParent(Long parentId, Long subjectId, BuildingType buildingType);

    @Query(value = "SELECT COALESCE( AVG (t.balance), 200000000) FROM Team AS t WHERE t.id NOT IN (SELECT tr.team.id " +
            "FROM TeamResearch AS tr WHERE tr.subject.id = :subjectId) AND t.id IN (SELECT b.team.id FROM" +
            " Building AS b WHERE b.type = :buildingType)")
    Double avgTeamBalance(Long subjectId, BuildingType buildingType);
}
