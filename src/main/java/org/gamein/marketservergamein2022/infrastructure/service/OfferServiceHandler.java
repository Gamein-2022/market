package org.gamein.marketservergamein2022.infrastructure.service;

import org.gamein.marketservergamein2022.core.dto.result.OfferDTO;
import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.exception.NotFoundException;
import org.gamein.marketservergamein2022.core.service.OfferService;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.*;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.LogType;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.OrderType;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingMethod;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingStatus;
import org.gamein.marketservergamein2022.infrastructure.repository.*;
import org.gamein.marketservergamein2022.infrastructure.util.CollectShipping;
import org.gamein.marketservergamein2022.infrastructure.util.TeamUtil;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.Math.abs;
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

    public OfferServiceHandler(TaskScheduler taskScheduler, OrderRepository orderRepository,
                               ShippingRepository shippingRepository, OfferRepository offerRepository,
                               StorageProductRepository storageProductRepository, LogRepository logRepository, TeamRepository teamRepository) {
        this.taskScheduler = taskScheduler;
        this.orderRepository = orderRepository;
        this.shippingRepository = shippingRepository;
        this.offerRepository = offerRepository;
        this.storageProductRepository = storageProductRepository;
        this.logRepository = logRepository;
        this.teamRepository = teamRepository;
    }

    @Override
    public OfferDTO createOffer(Team team, Long orderId, ShippingMethod shippingMethod)
            throws BadRequestException, NotFoundException {
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

        if (order.getType() == OrderType.BUY) {
            StorageProduct sp = TeamUtil.blockProductInStorage(
                    getSPFromProduct(team, order.getProduct(), storageProductRepository),
                    order.getProductAmount()
            );
            storageProductRepository.save(sp);
        } else {
            long balance = team.getBalance();
            balance -= order.getUnitPrice() * order.getProductAmount();
            team.setBalance(balance);
            teamRepository.save(team);
        }


        Offer offer = new Offer();
        offer.setOrder(order);
        offer.setCreationDate(new Date());
        offer.setOfferer(team);

        if (order.getType() == OrderType.SELL) {
            // TODO validate shipping method (should be optional because of buy offers)
            // TODO validate shipping method even further (should be optional in same region buy offers too)
            // TODO & even further! (should be either PLANE or SHIP, players are not allowed to select SAME_REGION as
            //  a shipping method)
            offer.setShippingMethod(shippingMethod);
        }

        offerRepository.save(offer);
        // TODO notify players of new offers

        return offer.toDTO();
    }

    @Override
    public List<OfferDTO> getReceivedOffers(Long teamId) {
        return offerRepository.findAllByOrder_Submitter_IdAndCancelledIsFalseAndDeclinedIsFalseAndArchivedIsFalse(teamId).stream()
                .map(Offer::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<OfferDTO> getOrderOffers(Long teamId, Long orderId) {
        return offerRepository.findAllByOrder_Submitter_IdAndOrder_IdAndCancelledIsFalseAndDeclinedIsFalseAndArchivedIsFalse(teamId, orderId).stream()
                .map(Offer::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<OfferDTO> getSentOffers(Long teamId) {
        return offerRepository.findAllByOfferer_IdAndArchivedIsFalse(teamId).stream()
                .map(Offer::toDTO).collect(Collectors.toList());
    }

    @Override
    public OfferDTO acceptOffer(Team team, Long offerId, ShippingMethod shippingMethod)
            throws BadRequestException, NotFoundException {
        Offer offer = checkOfferAccess(team.getId(), offerId);

        int distance = abs(offer.getOfferer().getRegion() - offer.getOrder().getSubmitter().getRegion());

        Order order = offer.getOrder();

        int shippingCost = calculateShippingPrice(
                offer.getOrder().getType() == OrderType.BUY ? shippingMethod : offer.getShippingMethod(),
                distance
        );

        if (offer.getOrder().getType() == OrderType.BUY) {
            if (shippingCost > team.getBalance()) {
                throw new BadRequestException("شما پول کافی برای پرداخت هزینه‌ی حمل را ندارید!");
            }
        }

        offer.setAcceptDate(new Date());
        offerRepository.save(offer);

        order.setAcceptDate(new Date());
        order.setAccepter(offer.getOfferer());

        offerRepository.findAllByOrder_IdAndCancelledIsFalseAndDeclinedIsFalse(order.getId()).forEach(
                o -> {
                    if (!o.getId().equals(offer.getId())) {
                        try {
                            undoOffer(o);
                        } catch (BadRequestException e) {
                            // TODO so something about this exception
                            throw new RuntimeException(e);
                        }
                        o.setDeclined(true);
                        offerRepository.save(o);
                    }
                }
        );

        Shipping shipping = new Shipping();
        shipping.setDepartureTime(new Date());
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
        shipping.setArrivalTime(new Date(new Date().getTime() +
                calculateShippingDuration(shipping.getMethod(), distance)));
        StorageProduct sp = TeamUtil.addProductToRoute(
                getOrCreateSPFromProduct(
                        buyer,
                        shipping.getProduct(),
                        storageProductRepository,
                        teamRepository
                ),
                shipping.getAmount()
        );
        buyer.setBalance(buyer.getBalance() - shippingCost);
        storageProductRepository.save(sp);
        teamRepository.save(buyer);
        sp = TeamUtil.removeProductFromBlockedAndStorage(
                getSPFromProduct(seller, shipping.getProduct(), storageProductRepository),
                shipping.getAmount()
        );
        seller.setBalance(seller.getBalance() + order.getProductAmount() * order.getUnitPrice());
        storageProductRepository.save(sp);
        teamRepository.save(seller);
        shippingRepository.save(shipping);
        order.setShipping(shipping);
        orderRepository.save(order);
        taskScheduler.schedule(new CollectShipping(shipping, shippingRepository, storageProductRepository, teamRepository),
                shipping.getArrivalTime());
        // TODO notify players of new shipping

        Log buyerLog = new Log();
        buyerLog.setType(LogType.BUY);
        buyerLog.setTeam(buyer);
        buyerLog.setProduct(offer.getOrder().getProduct());
        buyerLog.setTotalCost(order.getProductAmount() * order.getUnitPrice() + shippingCost);
        buyerLog.setProductCount(Long.valueOf(offer.getOrder().getProductAmount()));
        logRepository.save(buyerLog);

        Log sellerLog = new Log();
        sellerLog.setType(LogType.SELL);
        sellerLog.setTeam(seller);
        sellerLog.setProduct(offer.getOrder().getProduct());
        sellerLog.setProductCount(Long.valueOf(offer.getOrder().getProductAmount()));
        sellerLog.setTotalCost(order.getProductAmount() * order.getUnitPrice());
        logRepository.save(sellerLog);

        return offer.toDTO();
    }

    @Override
    public OfferDTO declineOffer(Team team, Long offerId)
            throws BadRequestException, NotFoundException {
        Offer offer = checkOfferAccess(team.getId(), offerId);
        offer.setDeclined(true);
        offerRepository.save(offer);

        undoOffer(offer);

        return offer.toDTO();
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

        return offer.toDTO();
    }

    @Override
    public OfferDTO archiveOffer(Team team, Long offerId) throws BadRequestException, NotFoundException {
        Optional<Offer> offerOptional = offerRepository.findById(offerId); // TODO refactor this
        if (offerOptional.isEmpty()) {
            throw new NotFoundException("Offer not found!");
        }
        Offer offer = offerOptional.get();

        if (!team.getId().equals(offer.getOfferer().getId())) {
            throw new NotFoundException("Offer not found!");
        }

        if (offer.getCancelled()) {
            throw new BadRequestException("Offer can't be archived, it is cancelled!");
        }
        if (offer.getDeclined()) {
            throw new BadRequestException("Offer can't be archived, it is declined!");
        }
        if (offer.getArchived()) {
            throw new BadRequestException("Offer already archived!");
        }
        if (offer.getAcceptDate() == null) {
            throw new BadRequestException("You can't archive an open offer!");
        }

        offer.setArchived(true);
        offerRepository.save(offer);
        return offer.toDTO();
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
            StorageProduct sp = TeamUtil.unblockProduct(
                    getSPFromProduct(offer.getOfferer(), offer.getOrder().getProduct(), storageProductRepository),
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
