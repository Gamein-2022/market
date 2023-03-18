package org.gamein.marketservergamein2022.infrastructure.repository;

import org.gamein.marketservergamein2022.core.sharedkernel.entity.Order;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByCancelled(Boolean cancelled);
    List<Order> findAllByAccepter_IdOrSubmitter_Id(Long accepterId, Long submitterId);
}