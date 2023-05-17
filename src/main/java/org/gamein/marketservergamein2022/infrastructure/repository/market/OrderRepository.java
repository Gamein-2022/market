package org.gamein.marketservergamein2022.infrastructure.repository.market;

import org.gamein.marketservergamein2022.core.sharedkernel.entity.Order;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.OrderType;
import org.gamein.marketservergamein2022.infrastructure.repository.market.OrderRepoCustom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;


public interface OrderRepository extends JpaRepository<Order, Long>, OrderRepoCustom {
    List<Order> allOrders(OrderType type, Long productId);
    List<Order> findAllBySubmitter_IdAndArchivedIsFalse(Long submitterId);
    List<Order> findAllBySubmitter_IdOrAccepter_IdAndAcceptDateIsNotNull(Long submitterId, Long accepterId);
    List<Order> findAllBySubmitterIdAndArchivedIsFalseAndCancelledIsFalse(Long submitterId);
    List<Order> findAllBySubmitDateBeforeAndCancelledIsFalseAndAcceptDateIsNull(LocalDateTime tenMinutesAgo);
}