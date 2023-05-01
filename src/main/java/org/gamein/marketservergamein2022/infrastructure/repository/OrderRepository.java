package org.gamein.marketservergamein2022.infrastructure.repository;

import org.gamein.marketservergamein2022.core.sharedkernel.entity.Order;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.OrderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;


public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByCancelledIsFalseAndAcceptDateIsNullAndArchivedIsFalse();
    List<Order> findAllByProduct_IdAndCancelledIsFalseAndAcceptDateIsNullAndArchivedIsFalse(Long productId);
    List<Order> findAllByProduct_IdAndTypeAndCancelledIsFalseAndAcceptDateIsNullAndArchivedIsFalseOrderByUnitPrice(Long productId, OrderType orderType);
    List<Order> findAllByProduct_IdAndTypeAndCancelledIsFalseAndAcceptDateIsNullAndArchivedIsFalseOrderByUnitPriceDesc(Long productId, OrderType orderType);
    List<Order> findAllBySubmitter_IdAndArchivedIsFalse(Long submitterId);
    List<Order> findAllBySubmitter_IdOrAccepter_IdAndAcceptDateIsNotNull(Long submitterId, Long accepterId);
}