package org.gamein.marketservergamein2022.infrastructure.service.market;

import org.gamein.marketservergamein2022.core.dto.result.*;
import org.gamein.marketservergamein2022.core.dto.result.market.OrderDTO;
import org.gamein.marketservergamein2022.core.dto.result.market.ShippingInfoDTO;
import org.gamein.marketservergamein2022.core.dto.result.market.TeamTradesDTO;
import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.exception.NotFoundException;
import org.gamein.marketservergamein2022.core.service.market.OrderService;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.*;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.LogType;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.OrderType;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingMethod;
import org.gamein.marketservergamein2022.infrastructure.repository.*;
import org.gamein.marketservergamein2022.infrastructure.repository.market.*;
import org.gamein.marketservergamein2022.infrastructure.util.ShippingInfo;
import org.gamein.marketservergamein2022.infrastructure.util.TeamUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
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
    private final TimeRepository timeRepository;

    public OrderServiceHandler(ProductRepository productRepository, TeamRepository teamRepository,
                               OrderRepository orderRepository,
                               LogRepository logRepository, StorageProductRepository storageProductRepository,
                               FinalProductSellOrderRepository finalProductSellOrderRepository, RegionDistanceRepository regionDistanceRepository, OfferRepository offerRepository, TimeRepository timeRepository) {
        this.productRepository = productRepository;
        this.teamRepository = teamRepository;
        this.orderRepository = orderRepository;
        this.logRepository = logRepository;
        this.storageProductRepository = storageProductRepository;
        this.finalProductSellOrderRepository = finalProductSellOrderRepository;
        this.regionDistanceRepository = regionDistanceRepository;
        this.offerRepository = offerRepository;
        this.timeRepository = timeRepository;
    }

    @Override
    public OrderDTO createOrder(Team team, OrderType orderType, Long productId, Integer quantity, Long price)
            throws BadRequestException {

        Product product = validatingCreateOrder(productId,quantity,price);

        if (orderType == OrderType.BUY) {
            long balance = team.getBalance();
            if (balance < price * quantity) {
                throw new BadRequestException("اعتبار شما کافی نیست!");
            }
            balance -= quantity * price;
            team.setBalance(balance);
            teamRepository.save(team);
        } else {
            Optional<StorageProduct> spOptional = getSPFromProduct(team, product);
            if (spOptional.isEmpty() ||
                    spOptional.get().getInStorageAmount() - spOptional.get().getBlockedAmount() < quantity) {
                throw new BadRequestException("شما به مقدار کافی " + product.getName() + " ندارید!");
            }
            StorageProduct sp = spOptional.get();
            if (sp.getSellableAmount() < quantity) {
                throw new BadRequestException("شما به مقدار کافی " + product.getName() + " برای فروش ندارید!");
            }

            TeamUtil.addProductToBlock(
                    sp,
                    quantity
            );
            TeamUtil.removeProductFromSellable(
                    sp,
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
        order.setSubmitDate(LocalDateTime.now(ZoneOffset.UTC));
        orderRepository.save(order);

        return order.toDTO(0, 0);
    }

    private Product validatingCreateOrder(Long productId,Integer quantity,Long price) throws BadRequestException {
        if (productId == null) {
            throw new BadRequestException("محصول مورد نظر وجود ندارد!");
        }
        if (quantity == null) {
            throw new BadRequestException("تعداد وارد نشده است.");
        }
        if (quantity <= 0)
            throw new BadRequestException("تعداد باید بیشتر از صفر باشد.");
        if (price == null) {
            throw new BadRequestException("قیمت وارد نشده است");
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
        return product;
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
                orderRepository.findAllBySubmitterIdAndArchivedIsFalseAndCancelledIsFalse(team.getId()).stream()
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
            throw new NotFoundException("سفارش یافت نشد.");
        }
        Order order = orderOptional.get();

        if (!team.getId().equals(order.getSubmitter().getId())) {
            throw new NotFoundException("سفارش یافت نشد.");
        }

        if (order.getCancelled()) {
            throw new BadRequestException("این سفارش قبلا لغو شده است .");
        }

        if (order.getAcceptDate() != null)
            throw new BadRequestException("این سفارش قبلا پایان رسیده است.");

        if (order.getType() == OrderType.BUY) {
            team.setBalance(team.getBalance() + (order.getUnitPrice() * order.getProductAmount()));
            teamRepository.save(team);
        } else {
            StorageProduct sp = getSPFromProduct(team, order.getProduct()).get();

            TeamUtil.removeProductFromBlock(
                    sp,
                    order.getProductAmount()
            );
            TeamUtil.addProductToSellable(
                    sp,
                    order.getProductAmount()
            );
            storageProductRepository.save(sp);
        }

        List<Offer> offers = new ArrayList<>();
        offerRepository.findAllByOrder_IdAndCancelledIsFalseAndDeclinedIsFalse(order.getId()).forEach(
                o -> {
                    try {
                        undoOffer(o);
                    } catch (BadRequestException e) {
                        throw new RuntimeException(e);
                    }
                    o.setDeclined(true);
                    offers.add(o);
                }
        );
        offerRepository.saveAll(offers);

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

        Time time = timeRepository.findById(1L).get();

        return new ShippingInfoDTO(
                calculateShippingDuration(ShippingMethod.PLANE, distance),
                calculateShippingDuration(ShippingMethod.SHIP, distance),
                ShippingInfo.planeBasePrice,
                ShippingInfo.shipBasePrice,
                ShippingInfo.planeVarPrice,
                ShippingInfo.shipVarPrice,
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
            StorageProduct sp = getSPFromProduct(offer.getOfferer(), offer.getOrder().getProduct()).get();
            TeamUtil.removeProductFromBlock(
                    sp,
                    offer.getOrder().getProductAmount()
            );
            TeamUtil.addProductToSellable(
                    sp,
                    offer.getOrder().getProductAmount()
            );
            storageProductRepository.save(sp);
        }
    }
}
