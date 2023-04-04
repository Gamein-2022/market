package org.gamein.marketservergamein2022.infrastructure.service;

import org.gamein.marketservergamein2022.core.dto.result.FinalProductSellOrderDTO;
import org.gamein.marketservergamein2022.core.dto.result.ShippingDTO;
import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.exception.NotFoundException;
import org.gamein.marketservergamein2022.core.service.TradeService;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.*;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingMethod;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingStatus;
import org.gamein.marketservergamein2022.infrastructure.repository.*;
import org.gamein.marketservergamein2022.infrastructure.util.CollectShipping;
import org.gamein.marketservergamein2022.infrastructure.util.GameinTradeTasks;
import org.gamein.marketservergamein2022.infrastructure.util.TeamUtil;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.lang.Math.abs;


@Service
public class TradeServiceHandler implements TradeService {
    private final TaskScheduler taskScheduler;
    private final ProductRepository productRepository;
    private final TeamRepository teamRepository;
    private final ShippingRepository shippingRepository;
    private final StorageProductRepository storageProductRepository;
    private final FinalProductSellOrderRepository finalProductSellOrderRepository;

    public TradeServiceHandler(TaskScheduler taskScheduler, ProductRepository productRepository,
                               TeamRepository teamRepository, ShippingRepository shippingRepository,
                               StorageProductRepository storageProductRepository,
                               FinalProductSellOrderRepository finalProductSellOrderRepository) {
        this.taskScheduler = taskScheduler;
        this.productRepository = productRepository;
        this.teamRepository = teamRepository;
        this.shippingRepository = shippingRepository;
        this.storageProductRepository = storageProductRepository;
        this.finalProductSellOrderRepository = finalProductSellOrderRepository;
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
            StorageProduct sp = TeamUtil.addProductToRoute(team, product, quantity);
            storageProductRepository.save(sp);



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
            shipping = shippingRepository.save(shipping);
            team.getShippings().add(shipping);
            teamRepository.save(team);

            taskScheduler.schedule(new CollectShipping(shipping, shippingRepository, storageProductRepository),
                    shipping.getArrivalTime());

            return shipping.toDTO();
        } else {
            throw new BadRequestException("Not enough balance!");
        }
    }

    @Override
    public FinalProductSellOrderDTO sellToGamein(Team team, Long productId, Integer quantity, Long price)
            throws NotFoundException, BadRequestException {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            throw new NotFoundException("محصول مورد نظر یافت نشد!");
        }
        Product product = productOptional.get();

        if (product.getLevel() < 2) {
            throw new BadRequestException("شما فقط محصولات نهایی را می‌توانید به گیمین بفروشید!");
        }

        StorageProduct sp = TeamUtil.blockProductInStorage(team, product, quantity);
        storageProductRepository.save(sp);

        FinalProductSellOrder order = new FinalProductSellOrder();
        order.setSubmitDate(new Date());
        order.setSubmitter(team);
        order.setProduct(product);
        order.setUnitPrice(price);
        order.setQuantity(quantity);
        finalProductSellOrderRepository.save(order);

        return order.toDTO();
    }

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    private void buy() {
        System.out.println("scheduled task");
        List<FinalProductSellOrder> orders = finalProductSellOrderRepository.findAllByClosedIsFalse();
        new GameinTradeTasks(orders).run();
        finalProductSellOrderRepository.saveAll(orders);
        teamRepository.saveAll(orders.stream().map(FinalProductSellOrder::getSubmitter).collect(Collectors.toList()));
    }
}
