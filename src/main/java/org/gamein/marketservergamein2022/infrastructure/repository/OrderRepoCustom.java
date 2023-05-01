package org.gamein.marketservergamein2022.infrastructure.repository;

import org.gamein.marketservergamein2022.core.sharedkernel.entity.Order;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.OrderType;

import java.util.List;

public interface OrderRepoCustom {
    List<Order> allOrders(OrderType type, Long productId);
}
