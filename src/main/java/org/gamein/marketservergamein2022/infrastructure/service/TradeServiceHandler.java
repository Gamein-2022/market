package org.gamein.marketservergamein2022.infrastructure.service;

import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.exception.NotFoundException;
import org.gamein.marketservergamein2022.core.exception.UnauthorizedException;
import org.gamein.marketservergamein2022.core.service.TradeService;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.*;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.OfferType;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingMethod;
import org.gamein.marketservergamein2022.infrastructure.repository.*;
import org.gamein.marketservergamein2022.web.dto.result.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;


@Service
public class TradeServiceHandler implements TradeService {
    private final ProductRepository productRepository;
    private final TeamRepository teamRepository;
    private final OfferRepository offerRepository;
    private final ShippingRepository shippingRepository;
    private final PendingOfferRepository pendingOfferRepository;

    public TradeServiceHandler(ProductRepository productRepository, TeamRepository teamRepository,
                               OfferRepository offerRepository, ShippingRepository shippingRepository,
                               PendingOfferRepository pendingOfferRepository) {
        this.productRepository = productRepository;
        this.teamRepository = teamRepository;
        this.offerRepository = offerRepository;
        this.shippingRepository = shippingRepository;
        this.pendingOfferRepository = pendingOfferRepository;
    }

    @Override
    public TradeWithGameinResultDTO tradeWithGamein(Long teamId, String side, Long productId, Long quantity)
            throws BadRequestException {
        Optional<Team> teamOptional = teamRepository.findById(teamId);
        if (teamOptional.isEmpty()) {
            throw new BadRequestException("Team not found!");
        }
        Team team = teamOptional.get();

        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            throw new BadRequestException("Invalid product!");
        }
        Product product = productOptional.get();

        long balance = team.getBalance();

        // TODO check product tier & supply & brand value &...
        // buy can only be tier 0 products (raw materials)
        // sell can only be tier 2 products (final products)
        if (side.equals("buy")) {
            // TODO check if there is enough storage before accepting trade
            if (balance >= product.getPrice() * quantity) {
                balance -= product.getPrice() * quantity;
                team.setBalance(balance);
                teamRepository.save(team);
                // TODO add purchase to storage

                return new TradeWithGameinResultDTO(balance);
            } else {
                throw new BadRequestException("Not enough balance!");
            }
        } else if (side.equals("sell")) {
            // TODO check if there is this amount of product present in storage
            if (balance < 1000000000) { // this should be the condition explained above
                balance += product.getPrice() * quantity;
                team.setBalance(balance);
                teamRepository.save(team);
                // TODO remove from storage

                return new TradeWithGameinResultDTO(balance);
            } else {
                throw new BadRequestException("Not enough " + product.getName() + " to sell!");
            }
        } else {
            throw new BadRequestException("Invalid side!");
        }
    }

    @Override
    public GetAllProductsResultDTO getAllProducts() {
        List<Product> products = productRepository.findAll();
        // TODO return only available products
        return new GetAllProductsResultDTO(products);
    }

    @Override
    public CreateOfferResultDTO createOffer(Long teamId, String offerType, Long productId, Long quantity, Long price)
            throws BadRequestException {

        if (offerType == null) {
            throw new BadRequestException("\"offerType\" is a required field!");
        }
        if (productId == null) {
            throw new BadRequestException("\"productId\" is a required field!");
        }
        if (quantity == null) {
            throw new BadRequestException("\"quantity\" is a required field!");
        }
        if (price == null) {
            throw new BadRequestException("\"price\" is a required field!");
        }
        // TODO filter out final product offers

        Optional<Team> teamOptional = teamRepository.findById(teamId);
        if (teamOptional.isEmpty()) {
            throw new BadRequestException("Team does not exist!");
        }
        Team team = teamOptional.get();

        OfferType type;
        if (offerType.equals("sell")) {
            type = OfferType.SELL;
        } else if (offerType.equals("buy")) {
            type = OfferType.BUY;
        } else {
            throw new BadRequestException("Invalid offer type!");
        }

        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            throw new BadRequestException("Product does not exist!");
        }
        Product product = productOptional.get();

        // TODO check if there is enough of this product for sell offers, & if there is enough storage for buy offers

        if (type == OfferType.BUY) {
            long balance = team.getBalance();
            balance -= quantity * price;
            team.setBalance(balance);
            teamRepository.save(team);
        }

        Offer offer = new Offer();
        offer.setSubmitter(team);
        offer.setProduct(product);
        offer.setType(type);
        offer.setProductAmount(quantity);
        offer.setPrice(price);
        offer.setSubmitDate(new Date());
        offer.setCancelled(false);
        offerRepository.save(offer);

        return new CreateOfferResultDTO(offer);
    }

    @Override
    public GetAllOffersResultDTO getAllOffers() {
        List<Offer> offers = offerRepository.findAllByCancelled(false);
        return new GetAllOffersResultDTO(offers);
    }

    @Override
    public GetAllOffersResultDTO getTeamTrades(Long teamId) throws BadRequestException {
        Optional<Team> teamOptional = teamRepository.findById(teamId);
        if (teamOptional.isEmpty()) {
            throw new BadRequestException("User is from invalid team!");
        }
        Team team = teamOptional.get();

        List<Offer> offers = offerRepository.findAllByAccepterOrSubmitter(team, team);
        return new GetAllOffersResultDTO(offers);
    }

    @Override
    public AcceptOfferResultDTO acceptOffer(Long offerId, Long accepterId, String shippingMethod) throws BadRequestException {
        if (offerId == null) {
            throw new BadRequestException("\"offerId\" is a required field!");
        }

        Optional<Team> teamOptional = teamRepository.findById(accepterId);
        if (teamOptional.isEmpty()) {
            throw new BadRequestException("User is from invalid team!");
        }
        Team team = teamOptional.get();

        Optional<Offer> offerOptional = offerRepository.findById(offerId);
        if (offerOptional.isEmpty()) {
            throw new BadRequestException("Invalid offer!");
        }
        Offer offer = offerOptional.get();

        if (team.getId().equals(offer.getSubmitter().getId())) {
            throw new BadRequestException("You can't accept your own offers!");
        }

        if (offer.getType() == OfferType.SELL) {
            ShippingMethod method;
            if (team.getRegion() == offer.getSubmitter().getRegion()) {
                method = ShippingMethod.SAME_REGION;
            } else {
                if (shippingMethod == null || shippingMethod.isEmpty()) {
                    throw new BadRequestException("\"shippingMethod\" is a required field when accepting sell offer " +
                            "from other regions!");
                }
                if (shippingMethod.equals("plane")) {
                    method = ShippingMethod.PLANE;
                } else if (shippingMethod.equals("ship")) {
                    method = ShippingMethod.SHIP;
                } else {
                    throw new BadRequestException("Invalid shippingMethod!");
                }
            }

            Shipping shipping = new Shipping();
            shipping.setMethod(method);
            shipping.setSourceRegion(offer.getSubmitter().getRegion());
            shipping.setTeam(team);
            shipping.setDepartureTime(new Date());
            shipping.setArrivalTime(new Date((new Date()).getTime() + 60000));
            shippingRepository.save(shipping);

            long balance = team.getBalance();
            balance -= offer.getProductAmount() * offer.getPrice();
            // TODO calculate & reduce shipping price from balance
            team.setBalance(balance);
            teamRepository.save(team);

            offer.setAccepter(team);
            offer.setAcceptDate(new Date());
            offerRepository.save(offer);

            return new AcceptSellOfferResultDTO(shipping);
        } else {
            PendingOffer pendingOffer = new PendingOffer();
            pendingOffer.setOffer(offer);
            pendingOffer.setAccepter(team);
            pendingOffer.setCreationDate(new Date());
            pendingOffer.setDeclined(false);
            pendingOfferRepository.save(pendingOffer);

            // TODO notify offer users they have a new pending acceptance

            return new AcceptBuyOfferResultDTO(pendingOffer);
        }
    }

    @Override
    public CreateOfferResultDTO cancelOffer(Long teamId, Long offerId) throws UnauthorizedException, BadRequestException {
        Optional<Team> teamOptional = teamRepository.findById(teamId);
        if (teamOptional.isEmpty()) {
            throw new BadRequestException("User is from invalid team!");
        }
        Team team = teamOptional.get();

        Optional<Offer> offerOptional = offerRepository.findById(offerId);
        if (offerOptional.isEmpty()) {
            throw new BadRequestException("offer does not exist!");
        }
        Offer offer = offerOptional.get();

        if (!team.getId().equals(offer.getSubmitter().getId())) {
            throw new UnauthorizedException("You can only cancel your own offers!");
        }

        if (offer.getCancelled()) {
            throw new BadRequestException("Offer already canceled!");
        }

        offer.setCancelled(true);
        offerRepository.save(offer);

        return new CreateOfferResultDTO(offer);
    }

    @Override
    public GetPendingOfferResultDTO getPendingOffers(Long teamId) {
        return new GetPendingOfferResultDTO(pendingOfferRepository.findAllByOffer_Submitter_Id(teamId));
    }

    @Override
    public AcceptSellOfferResultDTO acceptSellOffer(Long pendingOfferId, String shippingMethod, Long teamId) throws BadRequestException, NotFoundException {
        if (pendingOfferId == null) {
            throw new BadRequestException("\"pendingOfferId\" is a required field!");
        }

        Optional<Team> teamOptional = teamRepository.findById(teamId);
        if (teamOptional.isEmpty()) {
            throw new BadRequestException("User is from invalid team!");
        }
        Team team = teamOptional.get();

        Optional<PendingOffer> pendingOfferOptional = pendingOfferRepository.findById(pendingOfferId);
        if (pendingOfferOptional.isEmpty()) {
            throw new NotFoundException("PendingOffer not found!");
        }
        PendingOffer pendingOffer = pendingOfferOptional.get();

        if (!team.getId().equals(pendingOffer.getOffer().getSubmitter().getId())) {
            throw new NotFoundException("PendingOffer not found!");
        }

        if (pendingOffer.getAcceptDate() != null) {
            throw new BadRequestException("PendingOffer already accepted!");
        }
        if (pendingOffer.getDeclined()) {
            throw new BadRequestException("PendingOffer already declined!");
        }

        if (pendingOffer.getOffer().getAcceptDate() != null) {
            throw new BadRequestException("You have already accepted a seller for your buy offer!");
        }
        if (pendingOffer.getOffer().getCancelled()) {
            throw new BadRequestException("You have cancelled your buy offer!");
        }

        pendingOffer.setAcceptDate(new Date());
        pendingOfferRepository.save(pendingOffer);

        Offer offer = pendingOffer.getOffer();
        offer.setAcceptDate(new Date());
        offer.setAccepter(pendingOffer.getAccepter());
        offerRepository.save(offer);

        ShippingMethod method;
        if (team.getRegion() == offer.getAccepter().getRegion()) {
            method = ShippingMethod.SAME_REGION;
        } else {
            if (shippingMethod == null || shippingMethod.isEmpty()) {
                throw new BadRequestException("\"shippingMethod\" is a required field when accepting sell offer " +
                        "from other regions!");
            }
            if (shippingMethod.equals("plane")) {
                method = ShippingMethod.PLANE;
            } else if (shippingMethod.equals("ship")) {
                method = ShippingMethod.SHIP;
            } else {
                throw new BadRequestException("Invalid shippingMethod!");
            }
        }

        Shipping shipping = new Shipping();
        shipping.setMethod(method);
        shipping.setSourceRegion(offer.getAccepter().getRegion());
        shipping.setTeam(team);
        shipping.setDepartureTime(new Date());
        shipping.setArrivalTime(new Date((new Date()).getTime() + 60000));
        shippingRepository.save(shipping);

        return new AcceptSellOfferResultDTO(shipping);
    }

    @Override
    public DeclinePendingOfferResultDTO declineSellOffer(Long pendingOfferId, Long teamId) throws BadRequestException, NotFoundException {
        if (pendingOfferId == null) {
            throw new BadRequestException("\"pendingOfferId\" is a required field!");
        }

        Optional<Team> teamOptional = teamRepository.findById(teamId);
        if (teamOptional.isEmpty()) {
            throw new BadRequestException("User is from invalid team!");
        }
        Team team = teamOptional.get();

        Optional<PendingOffer> pendingOfferOptional = pendingOfferRepository.findById(pendingOfferId);
        if (pendingOfferOptional.isEmpty()) {
            throw new NotFoundException("PendingOffer not found!");
        }
        PendingOffer pendingOffer = pendingOfferOptional.get();

        if (!team.getId().equals(pendingOffer.getOffer().getSubmitter().getId())) {
            throw new NotFoundException("PendingOffer not found!");
        }

        if (pendingOffer.getAcceptDate() != null) {
            throw new BadRequestException("PendingOffer already accepted!");
        }
        if (pendingOffer.getDeclined()) {
            throw new BadRequestException("PendingOffer already declined!");
        }

        if (pendingOffer.getOffer().getAcceptDate() != null) {
            throw new BadRequestException("You have already accepted a seller for your buy offer!");
        }
        if (pendingOffer.getOffer().getCancelled()) {
            throw new BadRequestException("You have cancelled your buy offer!");
        }

        pendingOffer.setDeclined(true);
        pendingOfferRepository.save(pendingOffer);

        return new DeclinePendingOfferResultDTO();
    }
}
