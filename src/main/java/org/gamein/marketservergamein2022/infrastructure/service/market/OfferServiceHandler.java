package org.gamein.marketservergamein2022.infrastructure.service.market;

import org.gamein.marketservergamein2022.core.dto.result.TimeResultDTO;
import org.gamein.marketservergamein2022.core.dto.result.market.OfferDTO;
import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.exception.NotFoundException;
import org.gamein.marketservergamein2022.core.service.market.OfferService;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.*;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.LogType;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.OrderType;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingMethod;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingStatus;
import org.gamein.marketservergamein2022.infrastructure.repository.*;
import org.gamein.marketservergamein2022.infrastructure.repository.market.OfferRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.market.OrderRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.market.RegionDistanceRepository;
import org.gamein.marketservergamein2022.infrastructure.util.CollectShipping;
import org.gamein.marketservergamein2022.infrastructure.util.RestUtil;
import org.gamein.marketservergamein2022.infrastructure.util.TeamUtil;
import org.gamein.marketservergamein2022.infrastructure.util.TimeUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.gamein.marketservergamein2022.infrastructure.util.TeamUtil.*;


@Service
public class OfferServiceHandler implements OfferService {
    private final TaskScheduler taskScheduler;
    private final OrderRepository orderRepository;
    private final ShippingRepository shippingRepository;
    private final OfferRepository offerRepository;
    private final StorageProductRepository storageProductRepository;
    private final LogRepository logRepository;
    private final TeamRepository teamRepository;
    private final RegionDistanceRepository regionDistanceRepository;

    private final TimeRepository timeRepository;

    @Value("${live.data.url}")
    private String liveUrl;

    public OfferServiceHandler(TaskScheduler taskScheduler, OrderRepository orderRepository,
                               ShippingRepository shippingRepository, OfferRepository offerRepository,
                               StorageProductRepository storageProductRepository, LogRepository logRepository, TeamRepository teamRepository, RegionDistanceRepository regionDistanceRepository, TimeRepository timeRepository) {
        this.taskScheduler = taskScheduler;
        this.orderRepository = orderRepository;
        this.shippingRepository = shippingRepository;
        this.offerRepository = offerRepository;
        this.storageProductRepository = storageProductRepository;
        this.logRepository = logRepository;
        this.teamRepository = teamRepository;
        this.regionDistanceRepository = regionDistanceRepository;
        this.timeRepository = timeRepository;
    }

    @Override
    public OfferDTO createOffer(Team team, Long orderId, ShippingMethod shippingMethod)
            throws BadRequestException, NotFoundException {

        if (shippingMethod != null)
            if (shippingMethod.equals(ShippingMethod.SAME_REGION))
                throw new BadRequestException("حمل و نقل معتبر نمی باشد.");

        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isEmpty()) {
            throw new NotFoundException("سفارش یافت نشد!");
        }
        Order order = orderOptional.get();

        if (order.getCancelled() || order.getAcceptDate() != null) {
            throw new BadRequestException("سفارش دیگر وجود ندارد!");
        }

        if (order.getSubmitter().getId().equals(team.getId())) {
            throw new BadRequestException("شما نمی‌توانید سفارش خود را قبول کنید!");
        }

        List<Offer> prevOffers = offerRepository.findAllByOfferer_IdAndOrder_IdAndCancelledIsFalse(team.getId(), order.getId());
        if (prevOffers.size() > 0) {
            throw new BadRequestException("شما نمی‌توانید به یک سفارش بیش از یک بار پیشنهاد بدهید!");
        }

        Offer offer = new Offer();
        offer.setOrder(order);
        offer.setCreationDate(LocalDateTime.now(ZoneOffset.UTC));
        offer.setOfferer(team);

        int distance = 0;
        if (order.getType() == OrderType.SELL) {
            distance = regionDistanceRepository.findById(
                    new RegionDistancePK(offer.getOfferer().getRegion(), order.getSubmitter().getRegion())
            ).get().getDistance();
            if (distance == 0)
                shippingMethod = ShippingMethod.SAME_REGION;
            offer.setShippingMethod(shippingMethod);
        }


        if (order.getType() == OrderType.BUY) {
            Optional<StorageProduct> spOptional = getSPFromProduct(team, order.getProduct());
            if (spOptional.isEmpty() ||
                    spOptional.get().getSellableAmount() < order.getProductAmount()) {
                throw new BadRequestException("شما به مقدار کافی " + order.getProduct().getName() + " ندارید!");
            }
            StorageProduct sp = spOptional.get();
            TeamUtil.addProductToBlock(
                    sp,
                    order.getProductAmount()
            );
            TeamUtil.removeProductFromSellable(
                    sp,
                    order.getProductAmount()
            );
            storageProductRepository.save(sp);

        } else {
            int shippingCost = TeamUtil.calculateShippingPrice(shippingMethod,
                    distance, order.getProduct().getUnitVolume() * order.getProductAmount());

            Long cost = order.getUnitPrice() * order.getProductAmount() + shippingCost;
            if (team.getBalance() < cost) {
                throw new BadRequestException("شما پول کافی برای خرید این سفارش ندارید.");
            }
            long balance = team.getBalance() - order.getUnitPrice() * order.getProductAmount();
            team.setBalance(balance);
            teamRepository.save(team);
        }

        offerRepository.save(offer);



        String oType = "خرید ";
        if (order.getType().equals(OrderType.SELL)) {
            oType = "فروش ";
        }
        String text = "یک پیشنهاد جدید برای معامله ی " + oType + order.getProduct().getName() + " آمده است.";
        RestUtil.sendNotificationToATeam(text, "SUCCESS", String.valueOf(order.getSubmitter().getId()), liveUrl);


        if (order.getType().equals(OrderType.SELL)) {
            acceptOffer(order.getSubmitter(), offer.getId(), shippingMethod);
        }

        return offer.toDTO(
                regionDistanceRepository.findById(
                        new RegionDistancePK(offer.getOfferer().getRegion(), offer.getOrder().getSubmitter().getRegion())
                ).get().getDistance(),
                timeRepository.findById(1L).get()
        );
    }

    @Override
    public List<OfferDTO> getReceivedOffers(Long teamId) {
        Time time = timeRepository.findById(1L).get();
        return offerRepository.findAllByOrder_Submitter_IdAndCancelledIsFalseAndDeclinedIsFalseAndArchivedIsFalse(teamId).stream()
                .map(offer -> offer.toDTO(
                        regionDistanceRepository.findById(
                                new RegionDistancePK(offer.getOfferer().getRegion(),
                                        offer.getOrder().getSubmitter().getRegion())
                        ).get().getDistance(),
                        time
                )).collect(Collectors.toList());
    }

    @Override
    public List<OfferDTO> getOrderOffers(Long teamId, Long orderId) {
        Time time = timeRepository.findById(1L).get();
        return offerRepository.findAllByOrder_Submitter_IdAndOrder_IdAndCancelledIsFalseAndDeclinedIsFalseAndArchivedIsFalse(teamId, orderId).stream()
                .map(offer -> offer.toDTO(
                        regionDistanceRepository.findById(
                                new RegionDistancePK(offer.getOfferer().getRegion(),
                                        offer.getOrder().getSubmitter().getRegion())
                        ).get().getDistance(),
                        time
                )).collect(Collectors.toList());
    }

    @Override
    public List<OfferDTO> getSentOffers(Long teamId) {
        Time time = timeRepository.findById(1L).get();
        return offerRepository.findAllByOffererIdAndArchivedIsFalseAndCancelledIsFalse(teamId).stream()
                .map(offer -> offer.toDTO(
                        regionDistanceRepository.findById(
                                new RegionDistancePK(offer.getOfferer().getRegion(),
                                        offer.getOrder().getSubmitter().getRegion())
                        ).get().getDistance(),
                        time
                )).collect(Collectors.toList());
    }

    @Override
    public OfferDTO acceptOffer(Team team, Long offerId, ShippingMethod shippingMethod)
            throws BadRequestException, NotFoundException {

        if (shippingMethod != null)
            if (shippingMethod.equals(ShippingMethod.SAME_REGION))
                throw new BadRequestException("حمل و نقل معتبر نمی باشد.");

        Offer offer = checkOfferAccess(team.getId(), offerId);
        Order order = offer.getOrder();


        int distance = regionDistanceRepository.findById(
                new RegionDistancePK(offer.getOfferer().getRegion(), order.getSubmitter().getRegion())
        ).get().getDistance();
        if (distance == 0)
            shippingMethod = ShippingMethod.SAME_REGION;

        int shippingCost = calculateShippingPrice(
                offer.getOrder().getType() == OrderType.BUY ? shippingMethod : offer.getShippingMethod(),
                distance,
                order.getProductAmount() * order.getProduct().getUnitVolume()
        );


        if (offer.getOrder().getType() == OrderType.BUY) {
            if (shippingCost > team.getBalance()) {
                throw new BadRequestException("شما پول کافی برای پرداخت هزینه‌ی حمل را ندارید!");
            }
        }

        offer.setAcceptDate(LocalDateTime.now(ZoneOffset.UTC));
        order.setAcceptDate(LocalDateTime.now(ZoneOffset.UTC));
        order.setAccepter(offer.getOfferer());


        removingOtherOffers(order, offer);

        Shipping shipping = createShipping(order, offer, shippingMethod, distance);

        order.setShipping(shipping);
        orderRepository.save(order);
        offerRepository.save(offer);

        Team buyer;
        Team seller;
        if (order.getType() == OrderType.BUY) {
            buyer = order.getSubmitter();
            seller = order.getAccepter();
        } else {
            buyer = order.getAccepter();
            seller = order.getSubmitter();
        }

        updateTeamsBalanceAndStorage(buyer, seller, order, shippingCost);

        taskScheduler.schedule(new CollectShipping(shipping, shippingRepository, storageProductRepository,
                        teamRepository, timeRepository),
                java.sql.Timestamp.valueOf(shipping.getArrivalTime()));

        if (!offer.getOfferer().getId().equals(0L)) sendNotificationToOfferer(order, offer);

        addLog(buyer, LogType.BUY, order.getProduct(), Long.valueOf(order.getProductAmount()),
                order.getProductAmount() * order.getUnitPrice() + shippingCost);

        addLog(seller, LogType.SELL, order.getProduct(), Long.valueOf(order.getProductAmount()),
                order.getProductAmount() * order.getUnitPrice());

        return offer.toDTO(distance, timeRepository.findById(1L).get());
    }

    private Shipping createShipping(Order order, Offer offer, ShippingMethod shippingMethod, int distance) {
        Shipping shipping = new Shipping();
        shipping.setDepartureTime(LocalDateTime.now(ZoneOffset.UTC));
        shipping.setStatus(ShippingStatus.IN_ROUTE);
        shipping.setProduct(order.getProduct());
        shipping.setAmount(order.getProductAmount());

        Team buyer;
        Team seller;
        ShippingMethod method;

        if (order.getType() == OrderType.BUY) {
            buyer = order.getSubmitter();
            seller = order.getAccepter();
            method = shippingMethod;
        } else {
            buyer = order.getAccepter();
            seller = order.getSubmitter();
            method = offer.getShippingMethod();
        }
        if (distance == 0) {
            method = ShippingMethod.SAME_REGION;
        }

        shipping.setMethod(method);

        shipping.setTeam(buyer);
        shipping.setSourceRegion(seller.getRegion());
        shipping.setArrivalTime(
                shipping.getDepartureTime().plusSeconds(calculateShippingDuration(shipping.getMethod(), distance))
        );
        return shippingRepository.save(shipping);
    }

    private void sendNotificationToOfferer(Order order, Offer offer) {
        String type;
        if (order.getType().equals(OrderType.SELL))
            type = "خرید";
        else
            type = "فروش";

        String text = "پیشنهاد شما برای " + type + " کالای " + order.getProduct().getName() + " تایید شد.";
        RestUtil.sendNotificationToATeam(text, "SUCCESS", String.valueOf(offer.getOfferer().getId()), liveUrl);
    }

    private void updateTeamsBalanceAndStorage(Team buyer, Team seller, Order order, int shippingCost) throws BadRequestException {
        StorageProduct sp = TeamUtil.addProductToRoute(
                getOrCreateSPFromProduct(
                        buyer,
                        order.getProduct()
                ),
                order.getProductAmount()
        );
        buyer.setBalance(buyer.getBalance() - shippingCost);
        storageProductRepository.save(sp);
        teamRepository.save(buyer);
        sp = getSPFromProduct(seller, order.getProduct()).get();
        TeamUtil.removeProductFromStorage(
                sp,
                order.getProductAmount()
        );
        TeamUtil.removeProductFromBlock(
                sp,
                order.getProductAmount()
        );
        seller.setBalance(seller.getBalance() + order.getProductAmount() * order.getUnitPrice());
        storageProductRepository.save(sp);
        teamRepository.save(seller);
    }

    private void removingOtherOffers(Order order, Offer offer) {
        offerRepository.findAllByOrder_IdAndCancelledIsFalseAndDeclinedIsFalse(order.getId()).forEach(
                o -> {
                    if (!o.getId().equals(offer.getId())) {
                        try {
                            undoOffer(o);
                        } catch (BadRequestException e) {
                            throw new RuntimeException(e);
                        }
                        o.setDeclined(true);
                        offerRepository.save(o);
                    }
                }
        );
    }

    private void addLog(Team team, LogType logType, Product product, Long count, Long cost) {
        Time time = timeRepository.findById(1L).get();
        TimeResultDTO timeResultDTO = TimeUtil.getTime(time);
        Log log = new Log();
        log.setType(logType);
        log.setTeam(team);
        log.setProduct(product);
        log.setProductCount(count);
        log.setTotalCost(cost);
        log.setTimestamp(LocalDateTime.of(Math.toIntExact(timeResultDTO.getYear()),
                Math.toIntExact(timeResultDTO.getMonth()),
                Math.toIntExact(timeResultDTO.getDay()),
                12,
                23));
        ;
        ;
        logRepository.save(log);
    }

    @Override
    public OfferDTO declineOffer(Team team, Long offerId)
            throws BadRequestException, NotFoundException {
        Offer offer = checkOfferAccess(team.getId(), offerId);
        offer.setDeclined(true);
        offerRepository.save(offer);

        undoOffer(offer);

        return offer.toDTO(
                regionDistanceRepository.findById(
                        new RegionDistancePK(offer.getOfferer().getRegion(), offer.getOrder().getSubmitter().getRegion())
                ).get().getDistance(),
                timeRepository.findById(1L).get()
        );
    }

    @Override
    public OfferDTO cancelOffer(Team team, Long offerId)
            throws BadRequestException, NotFoundException {
        Optional<Offer> offerOptional = offerRepository.findById(offerId);
        if (offerOptional.isEmpty()) {
            throw new NotFoundException("پیشنهاد یافت نشد!");
        }
        Offer offer = offerOptional.get();
        if (!offer.getOfferer().getId().equals(team.getId())) {
            throw new NotFoundException("پیشنهاد یافت نشد!");
        }

        checkOfferClosed(offer);

        offer.setCancelled(true);
        offerRepository.save(offer);

        undoOffer(offer);

        return offer.toDTO(
                regionDistanceRepository.findById(
                        new RegionDistancePK(offer.getOfferer().getRegion(), offer.getOrder().getSubmitter().getRegion())
                ).get().getDistance(),
                timeRepository.findById(1L).get()
        );
    }

    @Override
    public OfferDTO archiveOffer(Team team, Long offerId) throws BadRequestException, NotFoundException {
        Optional<Offer> offerOptional = offerRepository.findById(offerId); // TODO refactor this
        if (offerOptional.isEmpty()) {
            throw new NotFoundException("این پیشنهاد یافت نشد.");
        }
        Offer offer = offerOptional.get();

        if (!team.getId().equals(offer.getOfferer().getId())) {
            throw new NotFoundException("این پیشنهاد یافت نشد.");
        }

        if (offer.getCancelled()) {
            throw new BadRequestException("این پیشنهاد قبلا لغو شده است .");
        }
        /*if (offer.getDeclined()) {
            throw new BadRequestException("Offer can't be archived, it is declined!");
        }*/
        if (offer.getArchived()) {
            throw new BadRequestException("این پیشنهاد قبلا بایگانی شده است.");
        }
        if (offer.getAcceptDate() == null && !offer.getDeclined()) {
            throw new BadRequestException("این پیشنهاد همچنان باز است.");
        }

        offer.setArchived(true);
        offerRepository.save(offer);
        return offer.toDTO(
                regionDistanceRepository.findById(
                        new RegionDistancePK(offer.getOfferer().getRegion(), offer.getOrder().getSubmitter().getRegion())
                ).get().getDistance(),
                timeRepository.findById(1L).get()
        );
    }


    private Offer checkOfferAccess(Long teamId, Long offerId)
            throws BadRequestException, NotFoundException {
        Optional<Offer> offerOptional = offerRepository.findById(offerId);
        if (offerOptional.isEmpty()) {
            throw new NotFoundException("پیشنهاد یافت نشد!");
        }
        Offer offer = offerOptional.get();
        if (!offer.getOrder().getSubmitter().getId().equals(teamId)) {
            throw new NotFoundException("پیشنهاد یافت نشد!");
        }

        checkOfferClosed(offer);

        return offer;
    }

    private void checkOfferClosed(Offer offer)
            throws BadRequestException {
        if (offer.getAcceptDate() != null) {
            throw new BadRequestException("پیشنهاد قبلا قبول شده است!");
        }
        if (offer.getDeclined()) {
            throw new BadRequestException("پیشنهاد قبلا رد شده است!");
        }
        if (offer.getCancelled()) {
            throw new BadRequestException("پیشنهاد قبلا کنسل شده است!");
        }
    }

    private void undoOffer(Offer offer)
            throws BadRequestException {
        if (offer.getOrder().getType() == OrderType.BUY) {
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
        } else {
            Team team = offer.getOfferer();
            team.setBalance(team.getBalance() + offer.getOrder().getProductAmount() * offer.getOrder().getUnitPrice());
            teamRepository.save(team);
        }
    }
}
