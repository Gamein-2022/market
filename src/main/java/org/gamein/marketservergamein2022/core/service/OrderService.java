package org.gamein.marketservergamein2022.core.service;

import org.gamein.marketservergamein2022.core.dto.result.OrderDTO;
import org.gamein.marketservergamein2022.core.dto.result.ShippingInfoDTO;
import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.exception.NotFoundException;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.OrderType;

import java.util.List;


public interface OrderService {
    OrderDTO createOrder(Team team, OrderType orderType, Long productId, Integer quantity, Long price)
            throws BadRequestException;

    List<OrderDTO> getAllOrders();

    List<OrderDTO> getTeamTrades(Long teamId);

    OrderDTO cancelOrder(Team team, Long orderId)
            throws NotFoundException, BadRequestException;

    OrderDTO archiveOrder(Team team, Long orderId)
            throws NotFoundException, BadRequestException;

    ShippingInfoDTO getOrderShippingPrices(Team team, Long orderId) throws
            NotFoundException;
}
