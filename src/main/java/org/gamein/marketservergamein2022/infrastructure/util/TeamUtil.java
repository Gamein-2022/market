package org.gamein.marketservergamein2022.infrastructure.util;

import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Product;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.StorageProduct;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.BuildingType;
import org.gamein.marketservergamein2022.infrastructure.repository.StorageProductRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.TeamRepository;

import java.util.Optional;

public class TeamUtil {
    public static long calculateStorageSpace(Team team) {
        return (team.getBuildings().stream().filter(b -> b.getType() == BuildingType.STORAGE).count() + 1) * 100000000;
    }
    public static long calculateAvailableSpace(Team team) {
        long result = calculateStorageSpace(team);
        for (StorageProduct storageProduct : team.getStorageProducts()) {
            long unitVolume = storageProduct.getProduct().getUnitVolume();
            result -= storageProduct.getInStorageAmount() * unitVolume;
            result -= storageProduct.getManufacturingAmount() * unitVolume;
        }
        return result;
    }

    public static long calculateUsedSpace(Team team) {
        long result = 0;
        for (StorageProduct storageProduct : team.getStorageProducts()) {
            result += storageProduct.getInStorageAmount() * storageProduct.getProduct().getUnitVolume();
        }
        return result;
    }

    public static StorageProduct addProductToRoute(Team team, Product product, Integer amount) {
        Optional<StorageProduct> storageProductOptional = team.getStorageProducts().stream().filter(
                storageProduct -> storageProduct.getProduct().getId() == product.getId()
        ).findFirst();

        StorageProduct sp;
        if (storageProductOptional.isPresent()) {
            sp = storageProductOptional.get();
            sp.setInRouteAmount(sp.getInRouteAmount() + amount);
        } else {
            sp = new StorageProduct();
            sp.setProduct(product);
            sp.setTeam(team);
            sp.setInStorageAmount(0);
            sp.setManufacturingAmount(0);
            sp.setInRouteAmount(amount);

            team.getStorageProducts().add(sp);
        }

        return sp;
    }

    public static void removeProductFromStorage(Team team, Product product, Integer amount,
                                                StorageProductRepository repo) // TODO refactor this & usages
            throws BadRequestException {
        Optional<StorageProduct> storageProductOptional = team.getStorageProducts().stream().filter(
                storageProduct ->
                        product.getId() == storageProduct.getProduct().getId()
        ).findFirst();
        if (storageProductOptional.isEmpty()) {
            throw new BadRequestException("شما مقدار کافی " + product.getName() + " برای فروش ندارید!");
        }

        StorageProduct sp = storageProductOptional.get();
        if (sp.getInStorageAmount() < amount) { // TODO find in queue amount from shipping and consider it too
            throw new BadRequestException("شما مقدار کافی " + product.getName() + " برای فروش ندارید!");
        }

        sp.setInStorageAmount(sp.getInStorageAmount() - amount);
        repo.save(sp);
    }
}
