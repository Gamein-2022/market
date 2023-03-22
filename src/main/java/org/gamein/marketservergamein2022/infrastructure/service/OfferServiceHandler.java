package org.gamein.marketservergamein2022.infrastructure.service;

import org.gamein.marketservergamein2022.core.dto.result.OfferDTO;
import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.exception.NotFoundException;
import org.gamein.marketservergamein2022.core.service.OfferService;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Offer;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Order;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Shipping;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.OrderType;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingMethod;
import org.gamein.marketservergamein2022.infrastructure.repository.*;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class OfferServiceHandler implements OfferService {
    private final OrderRepository orderRepository;
    private final ShippingRepository shippingRepository;
    private final OfferRepository offerRepository;

    public OfferServiceHandler(OrderRepository orderRepository, ShippingRepository shippingRepository,
                              OfferRepository offerRepository) {
        this.orderRepository = orderRepository;
        this.shippingRepository = shippingRepository;
        this.offerRepository = offerRepository;
    }

    @Override
    public OfferDTO createOffer(Team team, Long orderId, ShippingMethod shippingMethod)
            throws BadRequestException, NotFoundException {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isEmpty()) {
            throw new NotFoundException("Order not found!");
        }
        Order order = orderOptional.get();

        if (order.getCancelled() || order.getAcceptDate() != null) {
            throw new BadRequestException("Order is no longer open!");
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
        return offerRepository.findAllByOrder_Submitter_Id(teamId).stream()
                .map(Offer::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<OfferDTO> getSentOffers(Long teamId) {
        return offerRepository.findAllByOfferer_Id(teamId).stream()
                .map(Offer::toDTO).collect(Collectors.toList());
    }

    @Override
    public OfferDTO acceptOffer(Team team, Long offerId, ShippingMethod shippingMethod)
            throws BadRequestException, NotFoundException {
        Offer offer = checkOfferAccess(team.getId(), offerId);
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

        Shipping shipping = new Shipping();
        shipping.setDepartureTime(new Date());
        shipping.setArrivalTime(new Date(new Date().getTime() + 60000));
        // TODO calculate arrival time (move in if else below, depends on the method)
        if (order.getType() == OrderType.BUY) {
            shipping.setTeam(order.getSubmitter());
            shipping.setMethod(shippingMethod);
            shipping.setSourceRegion(offer.getOfferer().getRegion());
        } else {
            shipping.setTeam(offer.getOfferer());
            shipping.setMethod(offer.getShippingMethod());
            shipping.setSourceRegion(order.getSubmitter().getRegion());
        }
        shippingRepository.save(shipping);
//        taskScheduler.schedule(collectShipping, shipping.getArrivalTime());
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


    private Offer checkOfferAccess(Long teamId, Long offerId)
            throws BadRequestException, NotFoundException {
        Optional<Offer> offerOptional = offerRepository.findById(offerId);
        if (offerOptional.isEmpty()) {
            throw new NotFoundException("Offer not found!");
        }
        Offer offer = offerOptional.get();
        if (!offer.getOrder().getSubmitter().getId().equals(teamId)) {
            throw new NotFoundException("Offer not found!");
        }

        if (offer.getAcceptDate() != null) {
            throw new BadRequestException("Offer already accepted!");
        }
        if (offer.getDeclined()) {
            throw new BadRequestException("Offer has been declined!");
        }
        return offer;
    }
}
