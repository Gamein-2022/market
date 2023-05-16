package org.gamein.marketservergamein2022.infrastructure.util;

import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.*;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingMethod;
import org.gamein.marketservergamein2022.infrastructure.repository.StorageProductRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.TeamRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.factory.BuildingInfoRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.factory.BuildingRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.factory.TeamResearchRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static java.lang.Math.abs;
import static java.lang.Math.pow;


public class TeamUtil {
    public static int calculateStorageSpace(Team team) {
        return team.getIsStorageUpgraded() ? 80_000_000 : 20_000_000;
    }


    public static int calculateUsedSpace(Team team) {
        int result = 0;
        for (StorageProduct storageProduct : team.getStorageProducts()) {
            result += storageProduct.getInStorageAmount() * storageProduct.getProduct().getUnitVolume();
        }
        return result;
    }

    public static int calculateAvailableSpace(Team team) {
        int result = calculateStorageSpace(team);
        for (StorageProduct storageProduct : team.getStorageProducts()) {
            long unitVolume = storageProduct.getProduct().getUnitVolume();
            result -= storageProduct.getInStorageAmount() * unitVolume;
            result -= storageProduct.getManufacturingAmount() * unitVolume;
        }
        return result;
    }
    public static int calculateBlockedAmount(Team team) {
        int result = 0;
        for (StorageProduct storageProduct : team.getStorageProducts()) {
            result += storageProduct.getBlockedAmount() * storageProduct.getProduct().getUnitVolume();
        }
        return result;
    }

    public static int calculateManufacturing(Team team) {
        int result = 0;
        for (StorageProduct sp : team.getStorageProducts()) {
            result += sp.getManufacturingAmount() * sp.getProduct().getUnitVolume();
        }
        return result;
    }

    public static StorageProduct addProductToRoute(StorageProduct sp, Integer amount) {
        sp.setInRouteAmount(sp.getInRouteAmount() + amount);

        return sp;
    }

    public static StorageProduct removeProductFromRoute(StorageProduct sp, Integer amount) {
        sp.setInRouteAmount(sp.getInRouteAmount() - amount);
        return sp;
    }

    public static StorageProduct addProductToManufacturing(StorageProduct sp, int amount) {
        sp.setManufacturingAmount(sp.getManufacturingAmount() + amount);
        return sp;
    }

    public static StorageProduct removeProductFromManufacturing(StorageProduct sp, int amount) {
        sp.setManufacturingAmount(sp.getManufacturingAmount() - amount);
        return sp;
    }

    public static StorageProduct addProductToStorage(StorageProduct sp, Integer amount) {
        sp.setInStorageAmount(sp.getInStorageAmount() + amount);

        return sp;
    }

    public static StorageProduct removeProductFromStorage(StorageProduct sp, Integer amount) {
        sp.setInStorageAmount(sp.getInStorageAmount() - amount);
        return sp;
    }


    public static StorageProduct addProductToBlock(StorageProduct sp, Integer amount) {
        sp.setBlockedAmount(sp.getBlockedAmount() + amount);
        return sp;
    }

    public static StorageProduct removeProductFromBlock(StorageProduct sp, Integer amount) {
        sp.setBlockedAmount(sp.getBlockedAmount() - amount);
        return sp;
    }


    public static StorageProduct removeProductFromSellable(StorageProduct sp, Integer amount) {
        sp.setSellableAmount(sp.getSellableAmount() - amount);
        return sp;
    }

    public static StorageProduct addProductToSellable(StorageProduct sp, Integer amount) {
        sp.setSellableAmount(sp.getSellableAmount() + amount);
        return sp;
    }

    public static Optional<StorageProduct> getSPFromProduct(Team team, Product product) {
        return team.getStorageProducts().stream().filter(sp -> sp.getProduct().getId().equals(product.getId())).findFirst();
    }

    public static StorageProduct getOrCreateSPFromProduct(Team team, Product product) {
        Optional<StorageProduct> storageProductOptional = getSPFromProduct(team, product);
        if (storageProductOptional.isPresent()) {
            return storageProductOptional.get();
        } else {
            StorageProduct sp = new StorageProduct();
            sp.setProduct(product);
            sp.setTeam(team);
            team.getStorageProducts().add(sp);
            return sp;
        }
    }

    public static int calculateShippingPrice(ShippingMethod method, int distance, int volume) {
        int basePrice = method == ShippingMethod.SHIP ? ShippingInfo.shipBasePrice : ShippingInfo.planeBasePrice;
        int varPrice = method == ShippingMethod.SHIP ? ShippingInfo.shipVarPrice : ShippingInfo.planeVarPrice;
        int price = basePrice + varPrice * (int) pow(distance * volume, 0.5);
        return method == ShippingMethod.SAME_REGION ? ShippingInfo.shipBasePrice : price;
    }

    public static int calculateShippingDuration(ShippingMethod method, int distance) {
        return method == ShippingMethod.SAME_REGION ? 0 : method == ShippingMethod.SHIP ?
                distance * 3 * 60 : distance * 60;
    }


    public Long getTeamWealth(Team team,
                              BuildingInfoRepository buildingInfoRepository,
                              TeamResearchRepository teamResearchRepository) {
        long wealth = 0L;
        Iterable<BuildingInfo> buildingInfos = buildingInfoRepository.findAll();
        List<StorageProduct> teamsProduct = team.getStorageProducts();
        for (StorageProduct storageProduct : teamsProduct) {
            wealth += (long) storageProduct.getProduct().getMinPrice() * storageProduct.getInStorageAmount();
        }
        List<Building> teamBuildings = team.getBuildings();
        for (Building building : teamBuildings) {
            for (BuildingInfo buildingInfo : buildingInfos) {
                if (buildingInfo.getType().equals(building.getType())) {
                    wealth += buildingInfo.getBuildPrice();
                    wealth += building.isUpgraded() ? buildingInfo.getUpgradePrice() : 0;
                }
            }
        }
        List<TeamResearch> teamResearches = teamResearchRepository.findAllByTeamIdAndAndEndTimeAfter(team.getId(),
                LocalDateTime.now(ZoneOffset.UTC));
        for (TeamResearch teamResearch : teamResearches) {
            wealth += teamResearch.getPaidAmount();
        }
        wealth += team.getBalance();
        return wealth;
    }


}
