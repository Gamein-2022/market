package org.gamein.marketservergamein2022.infrastructure.service;

import org.gamein.marketservergamein2022.core.dto.result.OrderDTO;
import org.gamein.marketservergamein2022.core.dto.result.ShippingInfoDTO;
import org.gamein.marketservergamein2022.core.dto.result.TradeLogsDTO;
import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.exception.NotFoundException;
import org.gamein.marketservergamein2022.core.service.OrderService;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Order;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Product;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.StorageProduct;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.OrderType;
import org.gamein.marketservergamein2022.infrastructure.repository.*;
import org.gamein.marketservergamein2022.infrastructure.util.TeamUtil;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.Math.abs;


@Service
public class OrderServiceHandler implements OrderService {
    private final ProductRepository productRepository;
    private final TeamRepository teamRepository;
    private final OrderRepository orderRepository;

    private final StorageProductRepository storageProductRepository;

    public OrderServiceHandler(ProductRepository productRepository, TeamRepository teamRepository,
                               OrderRepository orderRepository,
                               StorageProductRepository storageProductRepository) {
        this.productRepository = productRepository;
        this.teamRepository = teamRepository;
        this.orderRepository = orderRepository;
        this.storageProductRepository = storageProductRepository;
    }

    @Override
    public OrderDTO createOrder(Team team, OrderType orderType, Long productId, Integer quantity, Long price)
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
            StorageProduct sp = TeamUtil.blockProductInStorage(team, product, quantity);
            storageProductRepository.save(sp);
        }

        Order order = new Order();
        order.setSubmitter(team);
        order.setProduct(product);
        order.setType(orderType);
        order.setProductAmount(quantity);
        order.setUnitPrice(price);
        order.setSubmitDate(new Date());
        orderRepository.save(order);

        return order.toDTO();
    }

    @Override
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAllByCancelledIsFalseAndAcceptDateIsNullAndArchivedIsFalse().stream()
                .map(Order::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getTeamTrades(Long teamId) {
        return orderRepository.findAllBySubmitter_IdAndArchivedIsFalse(teamId).stream()
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

        if (order.getType() == OrderType.BUY) {
            team.setBalance(team.getBalance() + (order.getUnitPrice() * order.getProductAmount()));
            teamRepository.save(team);
        } else {
            TeamUtil.addProductToRoute(team, order.getProduct(), order.getProductAmount());
        }

        order.setCancelled(true);
        orderRepository.save(order);

        return order.toDTO();
    }

    @Override
    public OrderDTO archiveOrder(Team team, Long orderId) throws NotFoundException, BadRequestException {
        Optional<Order> orderOptional = orderRepository.findById(orderId); // TODO refactor this
        if (orderOptional.isEmpty()) {
            throw new NotFoundException("Order not found!");
        }
        Order order = orderOptional.get();

        if (!team.getId().equals(order.getSubmitter().getId())) {
            throw new NotFoundException("Order not found!");
        }

        if (order.getCancelled()) {
            throw new BadRequestException("Order can't be archived, it is cancelled!");
        }
        if (order.getArchived()) {
            throw new BadRequestException("Order already archived!");
        }
        if (order.getAcceptDate() == null) {
            throw new BadRequestException("You can't archive an open order!");
        }

        order.setArchived(true);
        orderRepository.save(order);
        return order.toDTO();
    }

    @Override
    public ShippingInfoDTO getOrderShippingPrices(Team team, Long orderId) throws NotFoundException {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isEmpty()) {
            throw new NotFoundException("Order not found!");
        }
        Order order = orderOptional.get();

        int distance = abs(team.getRegion() - order.getSubmitter().getRegion());

        return new ShippingInfoDTO(
                distance * 5,
                distance * 25,
                distance * 50,
                distance * 10,
                team.getBalance()
        );
    }

    @Override
    public List<TradeLogsDTO> getTeamLogs(Long teamId) {
        return orderRepository.findAllBySubmitter_IdOrAccepter_IdAndAcceptDateIsNotNull(teamId, teamId)
                        .stream().map(order -> ((order.getSubmitter().getId().equals(teamId) && order.getType() == OrderType.BUY) ||
                                    (order.getAccepter().getId().equals(teamId) && order.getType() == OrderType.SELL))
                                    ? order.toBuyLogDTO() : order.toSellLogDTO()).collect(Collectors.toList());
    }
}
