package org.gamein.marketservergamein2022.infrastructure.util;

import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Product;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.StorageProduct;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.BuildingType;

import java.util.Optional;


public class TeamUtil {
    public static int calculateStorageSpace(Team team) {
        return (int) ((team.getBuildings().stream().filter(b -> b.getType() == BuildingType.STORAGE).count() + 1) * 3000);
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

    public static int calculateUsedSpace(Team team) {
        int result = 0;
        for (StorageProduct storageProduct : team.getStorageProducts()) {
            result += storageProduct.getInStorageAmount() * storageProduct.getProduct().getUnitVolume();
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
            sp.setInRouteAmount(amount);

            team.getStorageProducts().add(sp);
        }

        return sp;
    }

    public static StorageProduct addProductToStorage(Team team, Product product, Integer amount) {
        Optional<StorageProduct> storageProductOptional = team.getStorageProducts().stream().filter(
                storageProduct -> storageProduct.getProduct().getId() == product.getId()
        ).findFirst();

        StorageProduct sp;
        if (storageProductOptional.isPresent()) {
            sp = storageProductOptional.get();
            sp.setInStorageAmount(sp.getInStorageAmount() + amount);
        } else {
            sp = new StorageProduct();
            sp.setProduct(product);
            sp.setTeam(team);
            sp.setInStorageAmount(amount);

            team.getStorageProducts().add(sp);
        }

        return sp;
    }

    public static StorageProduct blockProductInStorage(Team team, Product product, Integer amount)
            throws BadRequestException {
        StorageProduct sp = getSPFromProduct(team, product);
        if (sp.getInStorageAmount() - sp.getBlockedAmount() < amount) {
            throw new BadRequestException("شما مقدار کافی " + product.getName() + " ندارید!");
        }

        sp.setBlockedAmount(sp.getBlockedAmount() + amount);

        return sp;
    }

    public static StorageProduct removeProductFromBlocked(Team team, Product product, Integer amount)
            throws BadRequestException {
        StorageProduct sp = getSPFromProduct(team, product);
        if (sp.getBlockedAmount() < amount) {
            throw new BadRequestException("شما مقدار کافی " + product.getName() + " ندارید!");
        }

        sp.setBlockedAmount(sp.getBlockedAmount() - amount);

        return sp;
    }

    public static StorageProduct removeProductFromStorage(Team team, Product product, Integer amount)
            throws BadRequestException {
        StorageProduct sp = getSPFromProduct(team, product);
        if (sp.getInStorageAmount() < amount) {
            throw new BadRequestException("شما مقدار کافی " + product.getName() + " ندارید!");
        }

        sp.setInStorageAmount(sp.getInStorageAmount() - amount);

        return sp;
    }

    public static StorageProduct removeProductFromInRoute(Team team, Product product, Integer amount)
            throws BadRequestException {
        StorageProduct sp = getSPFromProduct(team, product);
        if (sp.getInRouteAmount() < amount) {
            throw new BadRequestException("این مقدار " + product.getName() + " در مسیر نیست!");
        }

        sp.setInRouteAmount(sp.getInRouteAmount() - amount);

        return sp;
    }

    private static StorageProduct getSPFromProduct(Team team, Product product) throws BadRequestException {
        Optional<StorageProduct> storageProductOptional = team.getStorageProducts().stream().filter(
                storageProduct ->
                        product.getId() == storageProduct.getProduct().getId()
        ).findFirst();
        if (storageProductOptional.isEmpty()) {
            throw new BadRequestException("شما مقدار کافی " + product.getName() + " ندارید!");
        }
        return storageProductOptional.get();
    }
}
