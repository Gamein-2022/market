package org.gamein.marketservergamein2022.infrastructure.service;

import org.gamein.marketservergamein2022.core.dto.result.OrderDTO;
import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.exception.NotFoundException;
import org.gamein.marketservergamein2022.core.service.OrderService;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Order;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Product;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.OrderType;
import org.gamein.marketservergamein2022.infrastructure.repository.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class OrderServiceHandler implements OrderService {
    private final ProductRepository productRepository;
    private final TeamRepository teamRepository;
    private final OrderRepository orderRepository;

    public OrderServiceHandler(ProductRepository productRepository, TeamRepository teamRepository,
                              OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.teamRepository = teamRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public OrderDTO createOrder(Team team, OrderType orderType, Long productId, Long quantity, Long price)
            throws BadRequestException {
        if (productId == null) {
            throw new BadRequestException("\"productId\" is a required field!");
        }
        if (quantity == null) {
            throw new BadRequestException("\"quantity\" is a required field!");
        }
        if (price == null) {
            throw new BadRequestException("\"price\" is a required field!");
        }
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            throw new BadRequestException("Product does not exist!");
        }
        Product product = productOptional.get();
        if (product.getLevel() >= 3) {
            throw new BadRequestException("You can't trade this product!");
        }


        if (orderType == OrderType.BUY) {
            long balance = team.getBalance();
            if (balance < price * quantity) { // TODO consider shipping price too
                throw new BadRequestException("Not enough balance!");
            }
            balance -= quantity * price;
            team.setBalance(balance);
            teamRepository.save(team);
        } else {
            // TODO check if there is enough of this product for sell offers
        }

        Order order = new Order();
        order.setSubmitter(team);
        order.setProduct(product);
        order.setType(orderType);
        order.setProductAmount(quantity);
        order.setUnitPrice(price);
        order.setSubmitDate(new Date());
        order.setCancelled(false);
        orderRepository.save(order);

        return order.toDTO();
    }

    @Override
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAllByCancelled(false).stream()
                .map(Order::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getTeamTrades(Long teamId) {
        return orderRepository.findAllByAccepter_IdOrSubmitter_Id(teamId, teamId).stream()
                .map(Order::toDTO).collect(Collectors.toList());
    }

    @Override
    public OrderDTO cancelOrder(Team team, Long orderId)
            throws BadRequestException, NotFoundException {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isEmpty()) {
            throw new NotFoundException("Order not found!");
        }
        Order order = orderOptional.get();

        if (!team.getId().equals(order.getSubmitter().getId())) {
            throw new NotFoundException("Order not found!");
        }

        if (order.getCancelled()) {
            throw new BadRequestException("Order already canceled!");
        }

        order.setCancelled(true);
        orderRepository.save(order);

        return order.toDTO();
    }
}
