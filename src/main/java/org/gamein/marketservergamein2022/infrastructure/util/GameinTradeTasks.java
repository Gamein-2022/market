package org.gamein.marketservergamein2022.infrastructure.util;

import org.gamein.marketservergamein2022.core.sharedkernel.entity.Brand;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.FinalProductSellOrder;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Product;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


public class GameinTradeTasks {
    private final HashMap<Long, Integer> demands = new HashMap<>();
    private final HashMap<Long, Integer> brands = new HashMap<>();
    private final HashMap<Long, Integer> prevBrandsMap = new HashMap<>();
    private final HashMap<Long, Integer> prevPrevBrandsMap = new HashMap<>();
    private final List<Brand> previousBrands;
    private final List<Brand> previousPreviousBrands;
    private final int totalDemand;
    private final Date firstTime;
    private final Date secondTime;
    private final Date thirdTime;
    private final Date fourthTime;
    private final List<Product> products;
    private final List<FinalProductSellOrder> orders;
    private final List<Team> teams;

    public GameinTradeTasks(List<Brand> previousBrands, List<Brand> previousPreviousBrands, int totalDemand, Date firstTime, Date secondTime, Date thirdTime, Date fourthTime, List<Product> products, List<FinalProductSellOrder> orders, List<Team> teams) {
        this.previousBrands = previousBrands;
        this.previousPreviousBrands = previousPreviousBrands;
        this.totalDemand = totalDemand;
        this.firstTime = firstTime;
        this.secondTime = secondTime;
        this.thirdTime = thirdTime;
        this.fourthTime = fourthTime;
        this.products = products;
        this.orders = orders;
        this.teams = teams;
    }

    public void run() {
        calculateDemands();
        for (Product product : products) {
            divideDemandByProduct(
                    orders.stream()
                            .filter(order -> order.getProduct().getId() == product.getId())
                            .collect(Collectors.toList()), product
            );
        }
    }

    public void divideDemandByProduct(List<FinalProductSellOrder> orders,
                                      Product product) {
        int demand = demands.get(product.getId());
        int totalSold = 0;
        double totalBrandOnPrice;
        List<Double> brandOnPrices = new ArrayList<>();
        for (FinalProductSellOrder order : orders) {
            int brand = brands.get(order.getSubmitter().getId());
            double brandOnPrice = (double) brand / order.getQuantity();
            brandOnPrices.add(brandOnPrice);
        }
        while (true) {
            totalBrandOnPrice = 0;
            for (int i = 0; i < orders.size(); i++) {
                // skip completed orders
                if (orders.get(i).getSoldQuantity().equals(orders.get(i).getQuantity())) continue;

                totalBrandOnPrice += brandOnPrices.get(i);
            }
            for (int i = 0; i < orders.size(); i++) {
                // skip completed orders
                if (orders.get(i).getSoldQuantity().equals(orders.get(i).getQuantity())) continue;

                int sellAmount = (int) Math.ceil((brandOnPrices.get(i) / totalBrandOnPrice) * demand);
                FinalProductSellOrder order = orders.get(i);
                if (sellAmount > order.getQuantity() - order.getSoldQuantity()) {
                    sellAmount = order.getQuantity() - order.getSoldQuantity();
                }
                order.setAcceptDate(new Date());
                order.setSoldQuantity(order.getSoldQuantity() + sellAmount);

                Team team = order.getSubmitter();
                team.setBalance(team.getBalance() + (sellAmount * order.getUnitPrice()));

                totalSold += sellAmount;
            }
            if (totalSold >= demand ||
                    orders.stream().allMatch(order -> order.getSoldQuantity().equals(order.getQuantity()))) {
                break;
            }
        }

        orders.forEach(order -> order.setClosed(true));
    }

    private void calculateDemands() {
        int firstEraDemand = totalDemand;
        int secondEraDemand = 0;
        int thirdEraDemand = 0;
        int fourthEraDemand = 0;
        int fifthEraDemand = 0;

        if (firstTime != null) {
            int timePassed = (int) ((new Date().getTime() - firstTime.getTime()) / 5 * 60 * 1000);
            while (timePassed != 0) {
                firstEraDemand *= 0.99; // TODO change this to a better function
                timePassed -= 1;
            }
            secondEraDemand = totalDemand - firstEraDemand;
        }
        if (secondTime != null) {
            int timePassed = (int) ((new Date().getTime() - secondTime.getTime()) / 5 * 60 * 1000);
            while (timePassed != 0) {
                secondEraDemand *= 0.99; // TODO change this to a better function
                timePassed -= 1;
            }
            thirdEraDemand = totalDemand - secondEraDemand - firstEraDemand;
        }
        if (thirdTime != null) {
            int timePassed = (int) ((new Date().getTime() - thirdTime.getTime()) / 5 * 60 * 1000);
            while (timePassed != 0) {
                thirdEraDemand *= 0.99; // TODO change this to a better function
                timePassed -= 1;
            }
            fourthEraDemand = totalDemand - thirdEraDemand - secondEraDemand - firstEraDemand;
        }
        if (fourthTime != null) {
            int timePassed = (int) ((new Date().getTime() - fourthTime.getTime()) / 5 * 60 * 1000);
            while (timePassed != 0) {
                fourthEraDemand *= 0.99; // TODO change this to a better function
                timePassed -= 1;
            }
            fifthEraDemand = totalDemand - fourthEraDemand - thirdEraDemand - secondEraDemand - firstEraDemand;
        }
        HashMap<Integer, Integer> yearDemand = new HashMap<>();
        yearDemand.put(2000, firstEraDemand);
        yearDemand.put(2007, secondEraDemand);
        yearDemand.put(2011, thirdEraDemand);
        yearDemand.put(2016, fourthEraDemand);
        yearDemand.put(2023, fifthEraDemand);

        for (Product p : products) {
            demands.put(p.getId(), (int) p.getDemandCoefficient() * yearDemand.get(p.getAvailableYear()));
        }
    }

    private void calculateBrands() {
//        for (Brand brand : previousBrands) {
//            prevBrandsMap.put(brand.getTeam().getId(), brand.getBrand());
//        }
//        for (Brand brand : previousPreviousBrands) {
//            prevPrevBrandsMap.put(brand.getTeam().getId(), brand.getBrand());
//        }
//        double alpha = 1.0;
//        double betta = 1.0;
//        double theta = 1.0;
//        double lambda = 1.0;
//
//        ArrayList<Integer> formulae;
//        int sumFormulae;
//        for (Team team : teams) {
//            int formula =
//                    (int) (alpha * Math.pow((double) getCarbonFootprint(), lambda)) +
//                    betta * calculateSellShareForProduct;
//        }
        for (Team team : teams) {
            brands.put(team.getId(), 50);
        }
    }

    private int getCarbonFootprint() {
        return 50;
    }
}
