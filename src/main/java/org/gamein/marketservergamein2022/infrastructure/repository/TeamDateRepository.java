package org.gamein.marketservergamein2022.infrastructure.repository;

import org.gamein.marketservergamein2022.core.sharedkernel.entity.TeamDate;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Offer;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface TeamDateRepository extends CrudRepository<TeamDate,Integer> {
    @Modifying
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Query(value = "update TeamDate set dateTime =? 1 where team_id =? 2")
    void updateTeamDate(LocalDateTime localDateTime,Long teamId);

    @Modifying
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Query(value = "update TeamDate as t set t.dateTime =? 1 where t.team_id in" +
            " (select o.offerer.id from Offer as o where o.order.id =? 2)")
    void updateOfferer(LocalDateTime localDateTime,Long orderId);

    @Modifying
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Query(value = "update TeamDate as t set t.dateTime =? 1 where t.team_id in" +
            " (select s.submitter.id from FinalProductSellOrder as s where s.closed = false and s.cancelled = false) ")
    void updateAllTeamDate(LocalDateTime localDateTime);


    @Modifying
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Query(value = "update TeamDate as t set t.dateTime =? 1")
    void updateAllTeamDateAll(LocalDateTime localDateTime);

}
