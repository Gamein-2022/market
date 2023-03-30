package org.gamein.marketservergamein2022.infrastructure.util;

import lombok.AllArgsConstructor;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.FinalProductSellOrder;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@AllArgsConstructor
public class GameinTradeTasks {
    private List<FinalProductSellOrder> orders;


    public void run() {
        int demand = calculateDemand();
        int totalSold = 0;
        double totalBrandOnPrice = 0;
        List<Double> brandOnPrices = new ArrayList<>();
        for (FinalProductSellOrder order : orders) {
            int brand = calculateBrand(order.getSubmitter());
            double brandOnPrice = (double) brand / order.getQuantity();
            totalBrandOnPrice += brandOnPrice;
            brandOnPrices.add(brandOnPrice);
        }
        while (true) {
            for (int i = 0; i < orders.size(); i++) {
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

    private int calculateDemand() {
        return 500; // TODO calculate this
    }

    private int calculateBrand(Team team) {
        return 50; // TODO calculate this
    }
}
