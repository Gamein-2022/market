package org.gamein.marketservergamein2022.infrastructure.service;

import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.service.TradeService;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Product;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;
import org.gamein.marketservergamein2022.infrastructure.repository.ProductRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.TeamRepository;
import org.gamein.marketservergamein2022.web.dto.result.GetAllProductsResultDTO;
import org.gamein.marketservergamein2022.web.dto.result.TradeWithGameinResultDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class TradeServiceHandler implements TradeService {
    private final ProductRepository productRepository;
    private final TeamRepository teamRepository;

    public TradeServiceHandler(ProductRepository productRepository, TeamRepository teamRepository) {
        this.productRepository = productRepository;
        this.teamRepository = teamRepository;
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
}
