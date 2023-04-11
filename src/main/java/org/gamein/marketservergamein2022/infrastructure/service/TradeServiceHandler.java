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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
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
    private final TeamResearchRepository teamResearchRepository;
    private final TimeRepository timeRepository;
    private final DemandRepository demandRepository;
    private final BrandRepository brandRepository;

    public TradeServiceHandler(TaskScheduler taskScheduler, ProductRepository productRepository,
                               TeamRepository teamRepository, ShippingRepository shippingRepository,
                               StorageProductRepository storageProductRepository,
                               FinalProductSellOrderRepository finalProductSellOrderRepository,
                               TeamResearchRepository teamResearchRepository,
                               TimeRepository timeRepository, DemandRepository demandRepository, BrandRepository brandRepository) {
        this.taskScheduler = taskScheduler;
        this.productRepository = productRepository;
        this.teamRepository = teamRepository;
        this.shippingRepository = shippingRepository;
        this.storageProductRepository = storageProductRepository;
        this.finalProductSellOrderRepository = finalProductSellOrderRepository;
        this.teamResearchRepository = teamResearchRepository;
        this.timeRepository = timeRepository;
        this.demandRepository = demandRepository;
        this.brandRepository = brandRepository;
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
        try {

            System.out.println("scheduled task");
            Time time = timeRepository.findById(1L).get();
            long fiveMinutesFromBeginning =
                    Duration.ofSeconds(
                            Duration.between(time.getBeginTime(), LocalDateTime.now()).toSeconds() - time.getStoppedTimeSeconds()
                    ).toMinutes() / 5;
            List<FinalProductSellOrder> orders = finalProductSellOrderRepository.findAllByClosedIsFalse();
            List<Product> products = productRepository.findAllByLevelBetween(3, 3);
            TeamResearch first = teamResearchRepository.findFirstBySubject_IdOrderByEndTime(11L);
            TeamResearch second = teamResearchRepository.findFirstBySubject_IdOrderByEndTime(12L);
            TeamResearch third = teamResearchRepository.findFirstBySubject_IdOrderByEndTime(13L);
            TeamResearch fourth = teamResearchRepository.findFirstBySubject_IdOrderByEndTime(14L);
            Demand demand = demandRepository.findById(fiveMinutesFromBeginning).get();
            List<Team> teams = teamRepository.findAll();
            List<Brand> previousBrands = brandRepository.findAllByPeriod(fiveMinutesFromBeginning - 1);
            List<Brand> previousPreviousBrands = brandRepository.findAllByPeriod(fiveMinutesFromBeginning - 2);

            HashMap<Long, Double> newBrandsMap = new GameinTradeTasks(
                    previousBrands, previousPreviousBrands, demand.getDemand(),
                    first != null ? first.getEndTime() : null,
                    second != null ? second.getEndTime() : null,
                    third != null ? third.getEndTime() : null,
                    fourth != null ? fourth.getEndTime() : null,
                    products,
                    orders,
                    teams,
                    finalProductSellOrderRepository).run();
            List<Brand> newBrands = new ArrayList<>();
            for (Map.Entry<Long, Double> brand : newBrandsMap.entrySet()) {
                Brand b = new Brand();
                b.setTeam(teamRepository.findById(brand.getKey()).get());
                b.setBrand(brand.getValue());
                b.setPeriod(fiveMinutesFromBeginning);
                newBrands.add(b);
            }
            brandRepository.saveAll(newBrands);
            finalProductSellOrderRepository.saveAll(orders);
            teamRepository.saveAll(orders.stream().map(FinalProductSellOrder::getSubmitter).collect(Collectors.toList()));
        } catch (Exception e) {
            System.err.println("Error in scheduled task: trade service handler:");
            System.err.println(e.getMessage());
        }
    }
}