package org.gamein.marketservergamein2022.infrastructure.service.schedule;

import org.gamein.marketservergamein2022.core.dto.result.TimeResultDTO;
import org.gamein.marketservergamein2022.core.dto.result.schedule.RegionDTO;
import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.*;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.BuildingType;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.LogType;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.OrderType;
import org.gamein.marketservergamein2022.infrastructure.repository.*;
import org.gamein.marketservergamein2022.infrastructure.repository.factory.BuildingInfoRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.factory.BuildingRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.factory.ResearchSubjectRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.factory.TeamResearchRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.market.*;
import org.gamein.marketservergamein2022.infrastructure.repository.schedule.DemandLogRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.schedule.RegionRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.schedule.WealthLogRepository;
import org.gamein.marketservergamein2022.infrastructure.util.GameinTradeTasks;
import org.gamein.marketservergamein2022.infrastructure.util.RestUtil;
import org.gamein.marketservergamein2022.infrastructure.util.TeamUtil;
import org.gamein.marketservergamein2022.infrastructure.util.TimeUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.gamein.marketservergamein2022.infrastructure.util.TeamUtil.getSPFromProduct;

@Service
@EnableScheduling
@Configuration
public class ScheduleService {
    private final TimeRepository timeRepository;
    private final TeamRepository teamRepository;
    private final TeamResearchRepository teamResearchRepository;
    private final FinalProductSellOrderRepository finalProductSellOrderRepository;
    private final ProductRepository productRepository;
    private final DemandRepository demandRepository;
    private final RegionRepository regionRepository;
    private final LogRepository logRepository;
    private final StorageProductRepository storageProductRepository;
    private final OrderRepository orderRepository;
    private final OfferRepository offerRepository;
    private final DemandLogRepository demandLogRepository;
    private final BuildingRepository buildingRepository;
    private final BuildingInfoRepository buildingInfoRepository;
    private final WealthLogRepository wealthLogRepository;
    private final ResearchSubjectRepository researchSubjectRepository;

    private final TeamDateRepository teamDateRepository;

    @Value("${live.data.url}")
    private String liveUrl;

    public ScheduleService(TimeRepository timeRepository, TeamRepository teamRepository, TeamResearchRepository teamResearchRepository, FinalProductSellOrderRepository finalProductSellOrderRepository, ProductRepository productRepository, DemandRepository demandRepository, RegionRepository regionRepository, LogRepository logRepository, StorageProductRepository storageProductRepository, OrderRepository orderRepository, OfferRepository offerRepository, DemandLogRepository demandLogRepository, BuildingRepository buildingRepository, BuildingInfoRepository buildingInfoRepository, WealthLogRepository wealthLogRepository, ResearchSubjectRepository researchSubjectRepository, TeamDateRepository teamDateRepository) {
        this.timeRepository = timeRepository;
        this.teamRepository = teamRepository;
        this.teamResearchRepository = teamResearchRepository;
        this.finalProductSellOrderRepository = finalProductSellOrderRepository;
        this.productRepository = productRepository;
        this.demandRepository = demandRepository;
        this.regionRepository = regionRepository;
        this.logRepository = logRepository;
        this.storageProductRepository = storageProductRepository;
        this.orderRepository = orderRepository;
        this.offerRepository = offerRepository;
        this.demandLogRepository = demandLogRepository;
        this.buildingRepository = buildingRepository;
        this.buildingInfoRepository = buildingInfoRepository;
        this.wealthLogRepository = wealthLogRepository;
        this.researchSubjectRepository = researchSubjectRepository;
        this.teamDateRepository = teamDateRepository;
    }

    /*@Transactional
    @Scheduled(initialDelay = 0,fixedDelay = 4, timeUnit = TimeUnit.MINUTES)
    public void storageCost() {
        Time time = timeRepository.findById(1L).get();


        if (time.getIsRegionPayed()) {
            System.out.println("--> Start calculating storage cost : " + LocalDateTime.now(ZoneOffset.UTC));
            String text = "کارشناسان گیمین در حال محاسبه هزینه انبارداری شما می باشند. شکیبا باشید.\uD83C\uDF3C";
            RestUtil.sendNotificationToAll(text,"WARNING",liveUrl);
            teamDateRepository.updateAllTeamDateAll(LocalDateTime.now(ZoneOffset.UTC));
            teamRepository.updateStorageCost(time.getScale());
            *//*List<Team> allTeams = teamRepository.findAll();
            System.out.println("calculating storage cost : " + LocalDateTime.now(ZoneOffset.UTC));
            TimeResultDTO timeResultDTO = TimeUtil.getTime(time);
            System.out.println("calculating storage cost : " + LocalDateTime.now(ZoneOffset.UTC));
            for (Team team : allTeams) {
                if (team.getId().equals(0L)) continue;
                long cost = 0L;
                List<StorageProduct> teamProducts = team.getStorageProducts();
                for (StorageProduct storageProduct : teamProducts) {
                    long totalVolume = storageProduct.getInStorageAmount();
                    cost += totalVolume * storageProduct.getProduct().getMinPrice();
                }
                if (team.getBalance() >= cost / time.getStorageCostScale()) {
                    team.setBalance(team.getBalance() - cost / time.getStorageCostScale());
                    Log log = new Log();
                    log.setType(LogType.STORAGE_COST);
                    log.setTeam(team);
                    log.setTotalCost(cost / time.getStorageCostScale());
                    log.setProductCount(0L);
                    log.setTimestamp(LocalDateTime.of(Math.toIntExact(timeResultDTO.getYear()),
                            Math.toIntExact(timeResultDTO.getMonth()),
                            Math.toIntExact(timeResultDTO.getDay()),
                            12,
                            23));
                    logRepository.save(log);

                } else
                    team.setBalance(0);
            }
            System.out.println("calculating storage cost : " + LocalDateTime.now(ZoneOffset.UTC));*//*

            text = "هزینه انبارداری این ماه از حساب شما برداشت شد. موفق باشید.";
            RestUtil.sendNotificationToAll(text, "UPDATE_BALANCE", liveUrl);

            System.out.println("--> End calculating storage cost :" + LocalDateTime.now(ZoneOffset.UTC));
        }
    }*/

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void buyFinalProducts() {
        Time time = timeRepository.findById(1L).get();
        if (time.getIsGamePaused()) return;

        try {
            teamDateRepository.updateAllTeamDate(LocalDateTime.now(ZoneOffset.UTC));
            String text = "گیمین در حال خرید محصول نهایی می باشد.";
            RestUtil.sendNotificationToAll(text, "WARNING", liveUrl);
            System.out.println("final product orders task now commencing: " + LocalDateTime.now(ZoneOffset.UTC) + "\n");
            LocalDateTime nextTime = LocalDateTime.now(ZoneOffset.UTC).plusMinutes(5);
            time.setNextFinalOrderTime(nextTime);
            timeRepository.save(time);

            long fiveMinutesFromBeginning =
                    (TimeUtil.getTime(time).getDurationMillis() / (5 * 60 * 1000)) * 5;
            List<FinalProductSellOrder> orders =
                    finalProductSellOrderRepository.findAllByClosedIsFalseAndCancelledIsFalse();
            List<Product> products = productRepository.findAllByLevelBetween(3, 3);
            TeamResearch first = teamResearchRepository.findFirstBySubject_IdAndEndTimeIsBeforeOrderByEndTime(11L, LocalDateTime.now(ZoneOffset.UTC));
            TeamResearch second = teamResearchRepository.findFirstBySubject_IdAndEndTimeIsBeforeOrderByEndTime(12L, LocalDateTime.now(ZoneOffset.UTC));
            TeamResearch third = teamResearchRepository.findFirstBySubject_IdAndEndTimeIsBeforeOrderByEndTime(13L, LocalDateTime.now(ZoneOffset.UTC));
            TeamResearch fourth = teamResearchRepository.findFirstBySubject_IdAndEndTimeIsBeforeOrderByEndTime(14L, LocalDateTime.now(ZoneOffset.UTC));
            Optional<Demand> demandOptional = demandRepository.findById(fiveMinutesFromBeginning);
            if (demandOptional.isEmpty()) {
                System.err.printf("Demand %d not found!\n", fiveMinutesFromBeginning);
                return;
            }
            Demand demand = demandOptional.get();

            new GameinTradeTasks(
                    logRepository, (int) (time.getDemandMultiplier() * demand.getDemand()),
                    first != null ? first.getEndTime() : null,
                    second != null ? second.getEndTime() : null,
                    third != null ? third.getEndTime() : null,
                    fourth != null ? fourth.getEndTime() : null,
                    products,
                    orders,
                    finalProductSellOrderRepository, storageProductRepository, timeRepository, demandLogRepository,
                    fiveMinutesFromBeginning).run();
            finalProductSellOrderRepository.saveAll(orders);
            teamRepository.saveAll(orders.stream().map(FinalProductSellOrder::getSubmitter).collect(Collectors.toList()));
            System.out.println("final product orders end :" + LocalDateTime.now(ZoneOffset.UTC) + "\n");
        } catch (Exception e) {
            System.err.println("Error in scheduled task: trade service handler:");
            System.err.println(e.getMessage());
        }
    }

    /*@Scheduled(fixedDelay = 3, timeUnit = TimeUnit.MINUTES)
    public void tradeOffers() {
        Time time = timeRepository.findById(1L).get();
        if (time.getIsGamePaused()) return;

        Optional<Team> teamOptional = teamRepository.findById(0L);
        if (teamOptional.isEmpty()) {
            System.err.println("gamein team not found!");
            return;
        }
        Team gamein = teamOptional.get();
        for (Order order : orderRepository.allConvincingOrders(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(6))) {
            System.out.println(order);
            Offer offer = new Offer();
            offer.setOfferer(gamein);
            offer.setCreationDate(LocalDateTime.now(ZoneOffset.UTC));
            offer.setOrder(order);
            offer.setShippingMethod(ShippingMethod.SAME_REGION);
            offerRepository.save(offer);
        }
        gamein.setRegion(new Random().nextInt(8) + 1);
        teamRepository.save(gamein);
    }*/

    @Scheduled(initialDelay = 2,fixedDelay = 10, timeUnit = TimeUnit.MINUTES)
    public void saveTeamsWealth() {
        Time time = timeRepository.findById(1L).get();
        if (time.getIsGamePaused()) return;

        for (Team team : teamRepository.findAll()) {
            WealthLog log = new WealthLog();
            log.setTeam(team);
            log.setWealth(getTeamWealth(team, storageProductRepository, buildingRepository, buildingInfoRepository));
            log.setTime(LocalDateTime.now(ZoneOffset.UTC));
            log.setTenMinuteRound(TimeUtil.getTime(time).getDurationMillis() / (10 * 60 * 1000));
            wealthLogRepository.save(log);
        }
    }

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void payRegionPrice() {
        Time time = timeRepository.findById(1L).get();
        if (time.getIsRegionPayed()) return;
        if (time.getIsGamePaused()) return;

        Long duration = Duration.between(time.getBeginTime(), LocalDateTime.now(ZoneOffset.UTC)).toSeconds();
        boolean isChooseRegionFinished = duration - time.getStoppedTimeSeconds() > time.getChooseRegionDuration();
        if (!time.getIsRegionPayed() && isChooseRegionFinished) {
            /*List<Region> regions = regionRepository.findAll();*/
            Map<Integer, Long> regionsPopulation = RegionDTO.getRegionsPopulation(teamRepository.getRegionsPopulation());
            for (int i = 1; i < 9; i++) {
                if (!regionsPopulation.containsKey(i))
                    regionsPopulation.put(i, 0L);
            }

            List<Team> teams = teamRepository.findAll();
            for (Team team : teams) {
                if (team.getRegion() == 0) {
                    Random random = new Random();
                    team.setRegion(random.nextInt(8) + 1);
                    regionsPopulation.put(team.getRegion(), regionsPopulation.get(team.getRegion()) + 1);
                }
            }

            Map<Long, Long> regionsPrice = new HashMap<>();
            List<Region> regions = new ArrayList<>();
            for (int i = 1; i < 9; i++) {
                Region region = regionRepository.findFirstByRegionId(i);
                Long price = calculateRegionPrice(regionsPopulation.get(i));
                regionsPrice.put((long) i, price);
                region.setRegionPayed(price);
                regions.add(region);
            }
            regionRepository.saveAll(regions);
            for (Team team : teams) {
                team.setBalance(team.getBalance() - regionsPrice.get((long) team.getRegion()));
            }
            teamRepository.saveAll(teams);
            time.setIsRegionPayed(true);
            timeRepository.save(time);
            String text = "هزینه زمین از حساب شما برداشت شد.";
            RestUtil.sendNotificationToAll(text, "UPDATE_BALANCE", liveUrl);
        }
    }

    @Scheduled(initialDelay = 13, fixedRate = 10, timeUnit = TimeUnit.MINUTES)
    public void cancelPendingOrders() {
        teamDateRepository.updateAllTeamDateAll(LocalDateTime.now(ZoneOffset.UTC));
        String text = "کارگران انبار در حال انبارگردانی می باشند. شکیبا باشید.";
        RestUtil.sendNotificationToAll(text,"WARNING",liveUrl);
        List<Order> orders =
                orderRepository.findAllBySubmitDateBeforeAndCancelledIsFalseAndAcceptDateIsNull(
                        LocalDateTime.now(ZoneOffset.UTC).minusMinutes(10)
                );
        for (Order o : orders) {
            cancelOrder(o);
        }
    }

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void calculateResearchCosts() {
        System.out.println(LocalDateTime.now(ZoneOffset.UTC) + " => started calculating R&D");
        Time time = timeRepository.findById(1L).get();

        List<ResearchSubject> subjects = researchSubjectRepository.findAll();
        for (ResearchSubject subject : subjects) {
            double N_tOnN =
                    (double) teamResearchRepository.getResearchCount(subject.getId(), LocalDateTime.now(ZoneOffset.UTC)) / teamRepository.getTeamsCount();

            int price = calculatePrice(subject, time);
            int duration = calculateDuration(subject, N_tOnN, time);
            subject.setPrice(price);
            subject.setDuration(duration);
        }
        researchSubjectRepository.saveAll(subjects);
        System.out.println(LocalDateTime.now(ZoneOffset.UTC) + " => done calculating R&D");
    }

//    public static Stream<Team> getEligibleTeams(ResearchSubject subject, Stream<Team> teams) {
//        if (subject.getProductGroup() != null) {
//            return teams.filter(team -> {
//                for (Building building : team.getBuildings()) {
//                    for (FactoryLine line : building.getLines()) {
//                        if (line.getGroup() == subject.getProductGroup()) {
//                            return true;
//                        }
//                    }
//                }
//                return false;
//            });
//        } else {
//            return teams.filter(team -> {
//                for (Building building : team.getBuildings()) {
//                    if (building.getType() == subject.getBuildingType()) {
//                        return true;
//                    }
//                }
//                return false;
//            });
//        }
//    }

    private int calculatePrice(ResearchSubject subject, Time time) {
//        double medianTeamBalance;
//        Stream<Team> teamsStream;
//        if (subject.getParent() == null) {
//            List<Team> teams = teamRepository.findAll();
//            teamsStream = getEligibleTeams(subject, teams.stream()
//                    .filter(t -> !teamResearchRepository.existsByTeam_IdAndSubject_Id(t.getId(), subject.getId())));
//        } else {
//            teamsStream =
//                    getEligibleTeams(subject, teamResearchRepository.findAllBySubject_IdAndEndTimeBefore(subject.getParent().getId(),
//                                    LocalDateTime.now(ZoneOffset.UTC)).stream().map(TeamResearch::getTeam)
//                            .filter(t -> !teamResearchRepository.existsByTeam_IdAndSubject_Id(t.getId(), subject.getId())));
//        }
//        List<Double> teamsBalances =
//                teamsStream.map(
//                        team -> (double) getTeamWealth(team, storageProductRepository, buildingRepository,
//                                buildingInfoRepository)
//                                - calculateBuildingsCost(team.getBuildings())
//                ).sorted().toList();
//        if (teamsBalances.size() == 0) {
//            return -1;
//        }
//        medianTeamBalance = teamsBalances.size() % 2 == 0 ?
//                (teamsBalances.get(teamsBalances.size() / 2 - 1) + teamsBalances.get(teamsBalances.size() / 2)) / 2 :
//                teamsBalances.get(teamsBalances.size() / 2);

        // new code
        double avgTeamBalance = subject.getParent() != null ?
                teamResearchRepository.avgTeamBalanceWithParent(subject.getParent().getId(), subject.getId(), subject.getBuildingType()) :
                teamResearchRepository.avgTeamBalance(subject.getId(), subject.getBuildingType());

        double alpha = subject.getBuildingType() == BuildingType.PRODUCTION_FACTORY ?
                time.getRAndDPriceMultiplierProduction() : time.getRAndDPriceMultiplierAssembly();

        return (int) (alpha * avgTeamBalance);
    }

    private int calculateDuration(ResearchSubject subject, double N_tOnN, Time time) {
        if (subject.getDurationBound() != null) {
            return (int) (time.getRAndDTimeCoeff() * (calculateDuration(subject.getDurationBound(), N_tOnN, time)));
        }
        double baseTime = subject.getBaseDuration();
        baseTime -= 60 * Math.sqrt(N_tOnN);
        TeamResearch firstFinishedResearch;
        try {
            firstFinishedResearch = teamResearchRepository.findFirstResearch(subject.getId(), LocalDateTime.now(ZoneOffset.UTC)).get(0);
        } catch (IndexOutOfBoundsException e) {
            firstFinishedResearch = null;
        }
        if (firstFinishedResearch != null) {
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            LocalDateTime half = firstFinishedResearch.getBeginTime().plus(
                    Duration.between(firstFinishedResearch.getBeginTime(),
                            firstFinishedResearch.getEndTime()).toMillis() / 2, ChronoUnit.MILLIS
            );
            double diff = Math.abs(Duration.between(now, half).toMinutes());
            baseTime -= 2 * Math.sqrt(
                    diff / time.getRAndDRush()
            );
        }
        baseTime = Math.max(baseTime, 10);
        return ((int) (baseTime * 60));
    }

    private int calculateBuildingsCost(List<Building> buildings) {
        int result = 0;
        for (BuildingType type : BuildingType.values()) {
            result += buildings.stream().filter(building -> building.getType() == type).count() *
                    buildingInfoRepository.findById(type).orElseGet(BuildingInfo::new).getBuildPrice();
        }
        return result;
    }

    private Long getTeamWealth(Team team, StorageProductRepository storageProductRepository,
                               BuildingRepository buildingRepository, BuildingInfoRepository buildingInfoRepository) {
        long wealth = 0L;
        List<StorageProduct> teamsProduct = storageProductRepository.findAllByTeamId(team.getId());
        for (StorageProduct storageProduct : teamsProduct) {
            wealth += (long) storageProduct.getProduct().getPrice() * storageProduct.getInStorageAmount();
        }
        List<Building> teamBuildings = buildingRepository.findAllByTeamId(team.getId());
        for (Building building : teamBuildings) {
            wealth += buildingInfoRepository.findById(building.getType()).orElseGet(BuildingInfo::new).getBuildPrice();
        }
        wealth += team.getBalance();
        return wealth;
    }

    private Long calculateRegionPrice(Long currentPopulation) {
        Time time = timeRepository.findById(1L).get();
        Long scale = time.getScale();
        Long teamsCount = teamRepository.getTeamsCount();
        return (long) ((1 + (2.25 / (0.8 + 9 * Math.exp(-0.8 * (16 * currentPopulation / (teamsCount - 0.26)))))) * scale);
    }

    private void cancelOrder(Order order) {
        Team team = order.getSubmitter();
        if (order.getType() == OrderType.BUY) {
            team.setBalance(team.getBalance() + (order.getUnitPrice() * order.getProductAmount()));
            teamRepository.save(team);
        } else {
            StorageProduct sp = getSPFromProduct(team, order.getProduct()).get();

            TeamUtil.removeProductFromBlock(
                    sp,
                    order.getProductAmount()
            );
            TeamUtil.addProductToSellable(
                    sp,
                    order.getProductAmount()
            );
            storageProductRepository.save(sp);
        }

        List<Offer> offers = new ArrayList<>();

        offerRepository.findAllByOrder_IdAndCancelledIsFalseAndDeclinedIsFalse(order.getId()).forEach(
                o -> {
                    try {
                        undoOffer(o);
                    } catch (BadRequestException e) {
                        throw new RuntimeException(e);
                    }
                    o.setDeclined(true);
                    offers.add(o);
                }
        );
        offerRepository.saveAll(offers);

        order.setCancelled(true);
        orderRepository.save(order);
    }

    private void undoOffer(Offer offer)
            throws BadRequestException {
        if (offer.getOrder().getType() == OrderType.SELL) {
            Team team = offer.getOfferer();
            team.setBalance(team.getBalance() + offer.getOrder().getProductAmount() * offer.getOrder().getUnitPrice());
            teamRepository.save(team);
        } else {
            StorageProduct sp = getSPFromProduct(offer.getOfferer(), offer.getOrder().getProduct()).get();
            TeamUtil.removeProductFromBlock(
                    sp,
                    offer.getOrder().getProductAmount()
            );
            TeamUtil.addProductToSellable(
                    sp,
                    offer.getOrder().getProductAmount()
            );
            storageProductRepository.save(sp);
        }
    }
}
