package org.gamein.marketservergamein2022.infrastructure.service;

import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.service.TradeService;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Offer;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Product;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.OfferType;
import org.gamein.marketservergamein2022.infrastructure.repository.OfferRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.ProductRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.TeamRepository;
import org.gamein.marketservergamein2022.web.dto.result.CreateOfferResultDTO;
import org.gamein.marketservergamein2022.web.dto.result.GetAllProductsResultDTO;
import org.gamein.marketservergamein2022.web.dto.result.TradeWithGameinResultDTO;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;


@Service
public class TradeServiceHandler implements TradeService {
    private final ProductRepository productRepository;
    private final TeamRepository teamRepository;
    private final OfferRepository offerRepository;

    public TradeServiceHandler(ProductRepository productRepository, TeamRepository teamRepository,
                               OfferRepository offerRepository) {
        this.productRepository = productRepository;
        this.teamRepository = teamRepository;
        this.offerRepository = offerRepository;
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

        // TODO check if product is available at current in game year
        // TODO check product tier & supply & brand value &...
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
        // TODO talk about balance requirements for putting offer with game design team

        Offer offer = new Offer();
        offer.setSubmitter(team);
        offer.setProduct(product);
        offer.setType(type);
        offer.setProductAmount(quantity);
        offer.setPrice(price);
        offer.setSubmitDate(new Date());
        offer.setExpirationDate(new Date((new Date()).getTime() + 60000));

        offerRepository.save(offer);

        return new CreateOfferResultDTO(offer);
    }
}
