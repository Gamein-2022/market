package org.gamein.marketservergamein2022.infrastructure.service.market;

import org.gamein.marketservergamein2022.core.dto.result.market.FinalProductSellOrderDTO;
import org.gamein.marketservergamein2022.core.dto.result.market.NextTradeTaskDTO;
import org.gamein.marketservergamein2022.core.dto.result.market.ShippingDTO;
import org.gamein.marketservergamein2022.core.dto.result.TimeResultDTO;
import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.exception.NotFoundException;
import org.gamein.marketservergamein2022.core.service.market.TradeService;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.*;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.LogType;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingMethod;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingStatus;
import org.gamein.marketservergamein2022.infrastructure.repository.*;
import org.gamein.marketservergamein2022.infrastructure.repository.market.*;
import org.gamein.marketservergamein2022.infrastructure.util.CollectShipping;
import org.gamein.marketservergamein2022.infrastructure.util.TeamUtil;
import org.gamein.marketservergamein2022.infrastructure.util.TimeUtil;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.gamein.marketservergamein2022.infrastructure.util.TeamUtil.*;


@Service
public class TradeServiceHandler implements TradeService {
    private final TaskScheduler taskScheduler;
    private final ProductRepository productRepository;
    private final TeamRepository teamRepository;
    private final ShippingRepository shippingRepository;
    private final StorageProductRepository storageProductRepository;
    private final FinalProductSellOrderRepository finalProductSellOrderRepository;
    private final TimeRepository timeRepository;
    private final LogRepository logRepository;
    private final RegionDistanceRepository regionDistanceRepository;

    public TradeServiceHandler(TaskScheduler taskScheduler, ProductRepository productRepository,
                               TeamRepository teamRepository, ShippingRepository shippingRepository,
                               StorageProductRepository storageProductRepository,
                               FinalProductSellOrderRepository finalProductSellOrderRepository,
                               TimeRepository timeRepository, LogRepository logRepository, RegionDistanceRepository regionDistanceRepository) {
        this.taskScheduler = taskScheduler;
        this.productRepository = productRepository;
        this.teamRepository = teamRepository;
        this.shippingRepository = shippingRepository;
        this.storageProductRepository = storageProductRepository;
        this.finalProductSellOrderRepository = finalProductSellOrderRepository;
        this.timeRepository = timeRepository;
        this.logRepository = logRepository;
        this.regionDistanceRepository = regionDistanceRepository;
    }

    @Override
    public ShippingDTO buyFromGamein(Team team, Long productId, Integer quantity, ShippingMethod method)
            throws BadRequestException {
        if (method != null)
            if (method.equals(ShippingMethod.SAME_REGION))
                throw new BadRequestException("حمل و نقل معتبر نمی باشد.");
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            throw new BadRequestException("کالای مورد نظر یافت نشد!");
        }
        Product product = productOptional.get();
        if (quantity <= 0) {
            throw new BadRequestException("تعداد درخواستی نامعتبر است!");
        }

        if (product.getLevel() > 0) {
            throw new BadRequestException("شما تنها می‌توانید مواد اولیه از فروشگاه گیمین بخرید!");
        }

        Integer sourceRegion = regionDistanceRepository.minDistanceRegion(team.getRegion(), product.getRegions()).get(0);
        int distance = regionDistanceRepository.findById(
                new RegionDistancePK(sourceRegion, team.getRegion())
        ).get().getDistance();
        if (distance == 0)
            method = ShippingMethod.SAME_REGION;


        int shippingCost = calculateShippingPrice(
                method,
                distance,
                quantity * product.getUnitVolume()
        );


        Shipping shipping = new Shipping();
        shipping.setMethod(method);
        shipping.setTeam(team);
        shipping.setDepartureTime(LocalDateTime.now(ZoneOffset.UTC));

        long balance = team.getBalance();
        if (balance >= product.getPrice() * quantity + shippingCost) {
            balance -= product.getPrice() * quantity + shippingCost;
            team.setBalance(balance);
            StorageProduct sp = TeamUtil.addProductToRoute(
                    getOrCreateSPFromProduct(team, product),
                    quantity
            );
            storageProductRepository.save(sp);

            saveLog(LogType.BUY, product, shippingCost, quantity, team);

            shipping.setArrivalTime(
                    shipping.getDepartureTime().plusSeconds(calculateShippingDuration(shipping.getMethod(), distance))
            );
            shipping.setSourceRegion(sourceRegion);
            shipping.setStatus(ShippingStatus.IN_ROUTE);
            shipping.setProduct(product);
            shipping.setAmount(quantity);
            shipping = shippingRepository.save(shipping);
            team.getShippings().add(shipping);
            teamRepository.save(team);

            taskScheduler.schedule(new CollectShipping(shipping, shippingRepository, storageProductRepository,
                            teamRepository, timeRepository),
                    shipping.getArrivalTime().toInstant(ZoneOffset.UTC));

            return shipping.toDTO();
        } else {
            throw new BadRequestException("اعتبار شما کافی نیست!");
        }
    }

    private void saveLog(LogType logType, Product product, int shippingCost, int quantity, Team team) {
        Time time = timeRepository.findById(1L).get();
        TimeResultDTO timeResultDTO = TimeUtil.getTime(time);
        Log log = new Log();
        log.setType(logType);
        log.setTotalCost(((long) product.getPrice() * quantity + shippingCost));
        log.setTeam(team);
        log.setProductCount(Long.valueOf(quantity));
        log.setProduct(product);
        log.setTimestamp(LocalDateTime.of(Math.toIntExact(timeResultDTO.getYear()),
                Math.toIntExact(timeResultDTO.getMonth()),
                Math.toIntExact(timeResultDTO.getDay()),
                12,
                23));
        logRepository.save(log);
    }

    @Override
    public FinalProductSellOrderDTO sellToGamein(Team team, Long productId, Integer quantity, Long price)
            throws NotFoundException, BadRequestException {
        if (quantity <= 0)
            throw new BadRequestException("تعداد واردشده معتبر نمی باشد.");
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            throw new NotFoundException("محصول مورد نظر یافت نشد!");
        }
        Product product = productOptional.get();

        if (product.getLevel() < 2) {
            throw new BadRequestException("شما فقط محصولات نهایی را می‌توانید به گیمین بفروشید!");
        }
        if (finalProductSellOrderRepository.existsByProduct_IdAndCancelledIsFalseAndClosedIsFalse(productId)) {
            throw new BadRequestException("شما یک سفارش باز برای این محصول دارید!");
        }
        if (price < product.getMinPrice() || price > product.getMaxPrice()) {
            throw new BadRequestException("قیمت نامعتبر برای این محصول!");
        }

        Optional<StorageProduct> spOptional = getSPFromProduct(team, product);
        if (spOptional.isEmpty() ||
                spOptional.get().getInStorageAmount() - spOptional.get().getBlockedAmount() < quantity) {
            throw new BadRequestException("شما به مقدار کافی " + product.getName() + " ندارید!");
        }
        StorageProduct sp = spOptional.get();

        TeamUtil.addProductToBlock(
                sp,
                quantity
        );
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

    @Override
    public FinalProductSellOrderDTO cancelSellOrder(Team team, Long orderId) throws NotFoundException, BadRequestException {
        FinalProductSellOrder order = validateAndReturnOrder(team.getId(), orderId);

        if (order.getClosed()) {
            throw new BadRequestException("سفارش شما تکمیل شده است!");
        }

        order.setCancelled(true);
        TeamUtil.removeProductFromBlock(
                TeamUtil.getSPFromProduct(team, order.getProduct()).get(),
                order.getQuantity()
        );
        finalProductSellOrderRepository.save(order);
        return order.toDTO();
    }

    @Override
    public FinalProductSellOrderDTO archiveSellOrder(Team team, Long orderId) throws NotFoundException, BadRequestException {
        FinalProductSellOrder order = validateAndReturnOrder(team.getId(), orderId);
        if (!order.getClosed()) {
            throw new BadRequestException("سفارش باز را نمی‌توان بایگانی کرد!");
        }
        order.setArchived(true);
        finalProductSellOrderRepository.save(order);
        return order.toDTO();
    }

    @Override
    public NextTradeTaskDTO nextTime() {
        return new NextTradeTaskDTO(
                Timestamp.valueOf(timeRepository.findById(1L).get().getNextFinalOrderTime()),
                Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC))
        );
    }

    private FinalProductSellOrder validateAndReturnOrder(Long teamId, Long orderId) throws NotFoundException,
            BadRequestException {
        Optional<FinalProductSellOrder> orderOptional = finalProductSellOrderRepository.findById(orderId);
        if (orderOptional.isEmpty()) {
            throw new NotFoundException("سفارش فروش شما یافت نشد!");
        }
        FinalProductSellOrder order = orderOptional.get();

        if (!teamId.equals(order.getSubmitter().getId())) {
            throw new NotFoundException("سفارش فروش شما یافت نشد!");
        }

        if (order.getCancelled()) {
            throw new BadRequestException("این سفارش قبلا حذف شده است!");
        }
        if (order.getArchived()) {
            throw new BadRequestException("این سفارش قبلا بایگانی شده است!");
        }
        return order;
    }

//    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
//    private void buy() {
//        try {
//            System.out.println("scheduled task");
//            Time time = timeRepository.findById(1L).get();
//            long fiveMinutesFromBeginning =
//                    (Duration.ofSeconds(
//                            Duration.between(time.getBeginTime(), LocalDateTime.now(ZoneOffset.UTC)).toSeconds() - time.getStoppedTimeSeconds()
//                    ).toMinutes() / 5) * 5;
//            List<FinalProductSellOrder> orders =
//                    finalProductSellOrderRepository.findAllByClosedIsFalseAndCancelledIsFalse();
//            List<Product> products = productRepository.findAllByLevelBetween(3, 3);
//            TeamResearch first = teamResearchRepository.findFirstBySubject_IdOrderByEndTime(11L);
//            TeamResearch second = teamResearchRepository.findFirstBySubject_IdOrderByEndTime(12L);
//            TeamResearch third = teamResearchRepository.findFirstBySubject_IdOrderByEndTime(13L);
//            TeamResearch fourth = teamResearchRepository.findFirstBySubject_IdOrderByEndTime(14L);
//            Optional<Demand> demandOptional =  demandRepository.findById(fiveMinutesFromBeginning);
//            if (demandOptional.isEmpty()) {
//                System.err.printf("Demand %d not found!\n", fiveMinutesFromBeginning);
//                return;
//            }
//            Demand demand = demandOptional.get();
//
//            List<Team> teams = teamRepository.findAll();
//            List<Brand> previousBrands = brandRepository.findAllByPeriod(fiveMinutesFromBeginning - 1);
//            List<Brand> previousPreviousBrands = brandRepository.findAllByPeriod(fiveMinutesFromBeginning - 2);
//
//            HashMap<Long, Double> newBrandsMap = new GameinTradeTasks(
//                    previousBrands, previousPreviousBrands, demand.getDemand(),
//                    first != null ? first.getEndTime() : null,
//                    second != null ? second.getEndTime() : null,
//                    third != null ? third.getEndTime() : null,
//                    fourth != null ? fourth.getEndTime() : null,
//                    products,
//                    orders,
//                    teams,
//                    finalProductSellOrderRepository, storageProductRepository).run();
//            List<Brand> newBrands = new ArrayList<>();
//            for (Map.Entry<Long, Double> brand : newBrandsMap.entrySet()) {
//                Brand b = new Brand();
//                b.setTeam(teamRepository.findById(brand.getKey()).get());
//                b.setBrand(brand.getValue());
//                b.setPeriod(fiveMinutesFromBeginning);
//                newBrands.add(b);
//            }
//            brandRepository.saveAll(newBrands);
//            finalProductSellOrderRepository.saveAll(orders);
//            teamRepository.saveAll(orders.stream().map(FinalProductSellOrder::getSubmitter).collect(Collectors.toList()));
//            Date nextTime = new Date(
//                    (new Date().getTime()) + (5 * 60 * 1000)
//            );
//            time.setNextFinalOrderTime(nextTime);
//            timeRepository.save(time);
//        } catch (Exception e) {
//            System.err.println("Error in scheduled task: trade service handler:");
//            System.err.println(e.getMessage());
//        }
//    }
}
