package org.gamein.marketservergamein2022.infrastructure.service;

import org.gamein.marketservergamein2022.core.dto.result.OfferDTO;
import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.exception.NotFoundException;
import org.gamein.marketservergamein2022.core.service.OfferService;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.*;
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


@Service
public class OfferServiceHandler implements OfferService {
    private final TaskScheduler taskScheduler;
    private final OrderRepository orderRepository;
    private final ShippingRepository shippingRepository;
    private final OfferRepository offerRepository;
    private final StorageProductRepository storageProductRepository;
    private final TeamRepository teamRepository;

    public OfferServiceHandler(TaskScheduler taskScheduler, OrderRepository orderRepository,
                               ShippingRepository shippingRepository, OfferRepository offerRepository,
                               StorageProductRepository storageProductRepository, TeamRepository teamRepository) {
        this.taskScheduler = taskScheduler;
        this.orderRepository = orderRepository;
        this.shippingRepository = shippingRepository;
        this.offerRepository = offerRepository;
        this.storageProductRepository = storageProductRepository;
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
            StorageProduct sp = TeamUtil.blockProductInStorage(team, order.getProduct(), order.getProductAmount());
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
        long shippingCost = 0;
        if (offer.getOrder().getType() == OrderType.BUY) {
            if (shippingMethod == ShippingMethod.PLANE) {
                shippingCost = distance * 50L;
            } else if (shippingMethod == ShippingMethod.SHIP) {
                shippingCost = distance * 10L;
            } else if (offer.getOrder().getSubmitter().getRegion() != offer.getOfferer().getRegion()) {
                throw new BadRequestException("روش ارسال نامعتبر است!");
            }

            if (shippingCost > team.getBalance()) {
                throw new BadRequestException("شما پول کافی برای پرداخت هزینه‌ی حمل را ندارید!");
            }
        }


        offer.setAcceptDate(new Date());
        offerRepository.save(offer);

        Order order = offer.getOrder();
        order.setAcceptDate(new Date());
        order.setAccepter(offer.getOfferer());

        offerRepository.findAllByOrder_Id(order.getId()).forEach(
                o -> {
                    if (!o.getId().equals(offer.getId())) {
                        o.setDeclined(true);
                        offerRepository.save(o);
                    }
                }
        );

        StorageProduct sp = TeamUtil.addProductToRoute(team,order.getProduct(),order.getProductAmount());
        storageProductRepository.save(sp);
        team.setBalance(team.getBalance() - shippingCost);
        teamRepository.save(team);
        Shipping shipping = new Shipping();
        shipping.setDepartureTime(new Date());
        shipping.setStatus(ShippingStatus.IN_ROUTE);
        shipping.setProduct(order.getProduct());
        shipping.setAmount(order.getProductAmount());
        if (order.getType() == OrderType.BUY) {
            shipping.setTeam(order.getSubmitter());
            shipping.setMethod(shippingMethod);
            shipping.setSourceRegion(offer.getOfferer().getRegion());
            shipping.setArrivalTime(new Date(new Date().getTime() +
                    abs(offer.getOfferer().getRegion() - team.getRegion()) * 10000L));
            sp = TeamUtil.addProductToRoute(order.getSubmitter(), shipping.getProduct(), shipping.getAmount());
            storageProductRepository.save(sp);
            sp = TeamUtil.removeProductFromBlocked(order.getAccepter(), shipping.getProduct(),
                    shipping.getAmount());
            storageProductRepository.save(sp);
        } else {
            shipping.setTeam(offer.getOfferer());
            shipping.setMethod(offer.getShippingMethod());
            shipping.setSourceRegion(order.getSubmitter().getRegion());
            shipping.setArrivalTime(new Date(new Date().getTime() +
                    abs(order.getSubmitter().getRegion() - team.getRegion()) * 10000L));
            sp = TeamUtil.addProductToRoute(order.getAccepter(), shipping.getProduct(), shipping.getAmount());
            storageProductRepository.save(sp);
            sp = TeamUtil.removeProductFromBlocked(order.getSubmitter(), shipping.getProduct(),
                    shipping.getAmount());
            storageProductRepository.save(sp);
        }
        order.setShipping(shipping);
        orderRepository.save(order);
        shippingRepository.save(shipping);
        taskScheduler.schedule(new CollectShipping(shipping, shippingRepository, storageProductRepository),
                shipping.getArrivalTime());
        // TODO notify players of new shipping
        return offer.toDTO();
    }

    @Override
    public OfferDTO declineOffer(Team team, Long offerId)
            throws BadRequestException, NotFoundException {
        Offer offer = checkOfferAccess(team.getId(), offerId);
        offer.setDeclined(true);
        offerRepository.save(offer);

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
            throws BadRequestException{
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
}
