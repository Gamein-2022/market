package org.gamein.marketservergamein2022.infrastructure.service;

import org.gamein.marketservergamein2022.core.dto.result.OrderDTO;
import org.gamein.marketservergamein2022.core.dto.result.ShippingDTO;
import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.service.TradeService;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Order;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Product;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Shipping;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingMethod;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingStatus;
import org.gamein.marketservergamein2022.infrastructure.repository.*;
import org.gamein.marketservergamein2022.infrastructure.util.CollectShipping;
import org.gamein.marketservergamein2022.infrastructure.util.TeamUtil;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

import static java.lang.Math.abs;


@Service
public class TradeServerHandler implements TradeService {
    private final TaskScheduler taskScheduler;
    private final ProductRepository productRepository;
    private final TeamRepository teamRepository;
    private final OrderRepository orderRepository;
    private final ShippingRepository shippingRepository;
    private final StorageProductRepository storageProductRepository;

    public TradeServerHandler(TaskScheduler taskScheduler, ProductRepository productRepository,
                              TeamRepository teamRepository, OrderRepository orderRepository,
                              ShippingRepository shippingRepository,
                              StorageProductRepository storageProductRepository) {
        this.taskScheduler = taskScheduler;
        this.productRepository = productRepository;
        this.teamRepository = teamRepository;
        this.orderRepository = orderRepository;
        this.shippingRepository = shippingRepository;
        this.storageProductRepository = storageProductRepository;
    }

    @Override
    public ShippingDTO buyFromGamein(Team team, Long productId, Integer quantity, ShippingMethod method)
            throws BadRequestException {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            throw new BadRequestException("Invalid product!");
        }
        Product product = productOptional.get();
        if (quantity <= 0) {
            throw new BadRequestException("Invalid quantity!");
        }
        if (method == ShippingMethod.SAME_REGION) {
            throw new BadRequestException("Invalid shipping method!");
        }

        long balance = team.getBalance();

        if (product.getLevel() > 0) {
            throw new BadRequestException("Gamein only sells raw material or second-hand products!");
        }
        if (balance >= product.getPrice() * quantity) {
            balance -= product.getPrice() * quantity;
            // TODO reduce shipping amount from balance
            team.setBalance(balance);
            teamRepository.save(team);

            TeamUtil.addProductToStorage(team, product, quantity, teamRepository, storageProductRepository,
                    "shipping");

            Shipping shipping = new Shipping();
            shipping.setMethod(method);
            shipping.setTeam(team);
            shipping.setDepartureTime(new Date());
            // TODO make product region an array & find the nearest region for this
            shipping.setArrivalTime(new Date((new Date()).getTime() +
                    abs(product.getRegion() - team.getRegion()) * 10000L));
            shipping.setSourceRegion(product.getRegion());
            shipping.setStatus(ShippingStatus.IN_ROUTE);
            shipping.setProduct(product);
            shipping.setAmount(quantity);
            shippingRepository.save(shipping);

            taskScheduler.schedule(new CollectShipping(shipping, shippingRepository, storageProductRepository),
                    shipping.getArrivalTime());

            return shipping.toDTO();
        } else {
            throw new BadRequestException("Not enough balance!");
        }
    }

    @Override
    public OrderDTO sellToGamein(Team team, Long productId, Integer quantity)
            throws BadRequestException {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            throw new BadRequestException("Invalid product!");
        }
        Product product = productOptional.get();

        if (product.getLevel() < 2) {
            throw new BadRequestException("Gamein only buys final products!");
        }
        // TODO validate the amount they can sell to gamein

        long balance = team.getBalance();

        TeamUtil.removeProductFromStorage(team, product, quantity, storageProductRepository); // throws error if
        // there is not enough of the product (hopefully :))

        balance += product.getPrice() * quantity;
        team.setBalance(balance);
        teamRepository.save(team);

        Order order = new Order();
        order.setSubmitDate(new Date());
        order.setSubmitter(team);
        order.setAcceptDate(new Date());
        order.setCancelled(false);
        order.setProduct(product);
        order.setUnitPrice(product.getPrice());
        order.setProductAmount(quantity);
        // TODO set accepter to gamein team
        orderRepository.save(order);

        return order.toDTO();
    }
}
