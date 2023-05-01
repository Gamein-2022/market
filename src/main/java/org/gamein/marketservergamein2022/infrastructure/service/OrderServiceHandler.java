package org.gamein.marketservergamein2022.infrastructure.service;

import org.gamein.marketservergamein2022.core.dto.result.*;
import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.exception.NotFoundException;
import org.gamein.marketservergamein2022.core.service.OrderService;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.*;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.LogType;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.OrderType;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingMethod;
import org.gamein.marketservergamein2022.infrastructure.repository.*;
import org.gamein.marketservergamein2022.infrastructure.util.TeamUtil;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.gamein.marketservergamein2022.infrastructure.util.TeamUtil.*;


@Service
public class OrderServiceHandler implements OrderService {
    private final ProductRepository productRepository;
    private final TeamRepository teamRepository;
    private final OrderRepository orderRepository;
    private final LogRepository logRepository;
    private final StorageProductRepository storageProductRepository;
    private final FinalProductSellOrderRepository finalProductSellOrderRepository;
    private final RegionDistanceRepository regionDistanceRepository;
    private final OfferRepository offerRepository;

    public OrderServiceHandler(ProductRepository productRepository, TeamRepository teamRepository,
                               OrderRepository orderRepository,
                               LogRepository logRepository, StorageProductRepository storageProductRepository,
                               FinalProductSellOrderRepository finalProductSellOrderRepository, RegionDistanceRepository regionDistanceRepository, OfferRepository offerRepository) {
        this.productRepository = productRepository;
        this.teamRepository = teamRepository;
        this.orderRepository = orderRepository;
        this.logRepository = logRepository;
        this.storageProductRepository = storageProductRepository;
        this.finalProductSellOrderRepository = finalProductSellOrderRepository;
        this.regionDistanceRepository = regionDistanceRepository;
        this.offerRepository = offerRepository;
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
            throw new BadRequestException("محصول مورد نظر وجود ندارد!");
        }
        Product product = productOptional.get();
        if (product.getLevel() >= 3 || product.getLevel() <= 0) {
            throw new BadRequestException("شما نمی‌توانید این محصول را معامله کنید!");
        }
        if (price < product.getMinPrice() || price > product.getMaxPrice()) {
            throw new BadRequestException("قیمت نامعتبر برای این محصول!");
        }

        if (orderType == OrderType.BUY) {
            long balance = team.getBalance();
            if (balance < price * quantity) {
                throw new BadRequestException("اعتبار شما کافی نیست!");
            }
            balance -= quantity * price;
            team.setBalance(balance);
            teamRepository.save(team);
        } else {
            StorageProduct sp = TeamUtil.blockProductInStorage(
                    getSPFromProduct(team, product, storageProductRepository),
                    quantity
            );
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

        return order.toDTO(0, 0);
    }

    @Override
    public List<OrderDTO> getAllOrders(Team team, OrderType type, Long productId) {
        return orderRepository.allOrders(type, productId)
                .stream().map(order -> order.toDTO(
                        offerRepository.countAllByOrder_IdAndCancelledIsFalseAndDeclinedIsFalseAndArchivedIsFalse(order.getId()),
                        regionDistanceRepository.findById(
                                new RegionDistancePK(team.getRegion(), order.getSubmitter().getRegion())
                        ).get().getDistance()
                )).collect(Collectors.toList());
    }

    @Override
    public TeamTradesDTO getTeamTrades(Team team) {
        return new TeamTradesDTO(
                orderRepository.findAllBySubmitter_IdAndArchivedIsFalse(team.getId()).stream()
                        .map(order -> order.toDTO(
                                offerRepository.countAllByOrder_IdAndCancelledIsFalseAndDeclinedIsFalseAndArchivedIsFalse(order.getId()),
                                regionDistanceRepository.findById(
                                        new RegionDistancePK(team.getRegion(), order.getSubmitter().getRegion())
                                ).get().getDistance()
                        )).collect(Collectors.toList()),
                finalProductSellOrderRepository.findAllBySubmitter_IdAndArchivedIsFalse(team.getId()).stream()
                        .map(FinalProductSellOrder::toDTO).collect(Collectors.toList()));
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
            TeamUtil.unblockProduct(
                    getSPFromProduct(team, order.getProduct(), storageProductRepository),
                    order.getProductAmount()
            );
        }

        offerRepository.findAllByOrder_IdAndCancelledIsFalseAndDeclinedIsFalse(order.getId()).forEach(
                o -> {
                    try {
                        undoOffer(o);
                    } catch (BadRequestException e) {
                        // TODO so something about this exception
                        throw new RuntimeException(e);
                    }
                    o.setDeclined(true);
                    offerRepository.save(o);
                }
        );

        order.setCancelled(true);
        orderRepository.save(order);

        return order.toDTO(
                offerRepository.countAllByOrder_IdAndCancelledIsFalseAndDeclinedIsFalseAndArchivedIsFalse(order.getId()),
                regionDistanceRepository.findById(
                        new RegionDistancePK(team.getRegion(), order.getSubmitter().getRegion())
                ).get().getDistance()
        );
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
        return order.toDTO(
                offerRepository.countAllByOrder_IdAndCancelledIsFalseAndDeclinedIsFalseAndArchivedIsFalse(order.getId()),
                regionDistanceRepository.findById(
                        new RegionDistancePK(team.getRegion(), order.getSubmitter().getRegion())
                ).get().getDistance()
        );
    }

    @Override
    public ShippingInfoDTO getOrderShippingPrices(Team team, Long orderId) throws NotFoundException {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isEmpty()) {
            throw new NotFoundException("Order not found!");
        }
        Order order = orderOptional.get();

        int distance = regionDistanceRepository.findById(
                new RegionDistancePK(order.getSubmitter().getRegion(), team.getRegion())
        ).get().getDistance();

        return new ShippingInfoDTO(
                calculateShippingDuration(ShippingMethod.PLANE, distance) / 8000,
                calculateShippingDuration(ShippingMethod.SHIP, distance) / 8000,
                calculateShippingPrice(ShippingMethod.PLANE, distance, 0),
                calculateShippingPrice(ShippingMethod.SHIP, distance, 0),
                team.getBalance(),
                distance
        );
    }

    @Override
    public GetTeamLogsResultDTO getTeamLogs(Long teamId) {
        List<LogDTO> firstList = logRepository.findAllByTypeAndTeamId(LogType.BUY, teamId)
                .stream().map(Log::toDto).collect(Collectors.toList());
        List<LogDTO> secondList = logRepository.findAllByTypeAndTeamId(LogType.SELL, teamId)
                .stream().map(Log::toDto).toList();
        firstList.addAll(secondList);
        return new GetTeamLogsResultDTO(firstList);
    }

    private void undoOffer(Offer offer)
            throws BadRequestException {
        if (offer.getOrder().getType() == OrderType.SELL) {
            Team team = offer.getOfferer();
            team.setBalance(team.getBalance() + offer.getOrder().getProductAmount() * offer.getOrder().getUnitPrice());
            teamRepository.save(team);
        } else {
            StorageProduct sp = TeamUtil.unblockProduct(
                    getSPFromProduct(offer.getOfferer(), offer.getOrder().getProduct(), storageProductRepository),
                    offer.getOrder().getProductAmount()
            );
            storageProductRepository.save(sp);
        }
    }
}
