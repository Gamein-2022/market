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

    public static StorageProduct addProductToRoute(StorageProduct sp, Integer amount) {
        sp.setInRouteAmount(sp.getInRouteAmount() + amount);

        return sp;
    }

    public static StorageProduct addProductToStorage(StorageProduct sp, Integer amount) {
        sp.setInStorageAmount(sp.getInStorageAmount() + amount);

        return sp;
    }

    public static StorageProduct blockProductInStorage(StorageProduct sp, Integer amount)
            throws BadRequestException {
        if (sp.getInStorageAmount() - sp.getBlockedAmount() < amount) {
            throw new BadRequestException("شما مقدار کافی " + sp.getProduct().getName() + " ندارید!");
        }

        sp.setBlockedAmount(sp.getBlockedAmount() + amount);

        return sp;
    }

    public static StorageProduct unblockProduct(StorageProduct sp, Integer amount)
            throws BadRequestException {
        if (sp.getBlockedAmount() < amount) {
            throw new BadRequestException("شما مقدار کافی " + sp.getProduct().getName() + " ندارید!");
        }

        sp.setBlockedAmount(sp.getBlockedAmount() - amount);
        sp.setInStorageAmount(sp.getInStorageAmount() + amount);

        return sp;
    }

    public static StorageProduct removeProductFromBlocked(StorageProduct sp, Integer amount)
            throws BadRequestException {
        if (sp.getBlockedAmount() < amount) {
            throw new BadRequestException("شما مقدار کافی " + sp.getProduct().getName() + " ندارید!");
        }

        sp.setBlockedAmount(sp.getBlockedAmount() - amount);

        return sp;
    }

    public static StorageProduct removeProductFromStorage(StorageProduct sp, Integer amount)
            throws BadRequestException {
        if (sp.getInStorageAmount() < amount) {
            throw new BadRequestException("شما مقدار کافی " + sp.getProduct().getName() + " ندارید!");
        }

        sp.setInStorageAmount(sp.getInStorageAmount() - amount);

        return sp;
    }

    public static StorageProduct removeProductFromInRoute(StorageProduct sp, Integer amount)
            throws BadRequestException {
        if (sp.getInRouteAmount() < amount) {
            throw new BadRequestException("این مقدار " + sp.getProduct().getName() + " در مسیر نیست!");
        }

        sp.setInRouteAmount(sp.getInRouteAmount() - amount);

        return sp;
    }

    public static StorageProduct getSPFromProduct(Team team, Product product,
                                           StorageProductRepository storageProductRepository)
            throws BadRequestException {
        Optional<StorageProduct> storageProductOptional = storageProductRepository.findFirstByProduct_IdAndTeam_Id(product.getId(),
                team.getId());
        if (storageProductOptional.isEmpty()) {
            throw new BadRequestException("شما مقدار کافی " + product.getName() + " ندارید!");
        }
        return storageProductOptional.get();
    }

    public static StorageProduct getOrCreateSPFromProduct(Team team, Product product,
                                                   StorageProductRepository storageProductRepository,
                                                   TeamRepository teamRepository) {
        Optional<StorageProduct> storageProductOptional = storageProductRepository.findFirstByProduct_IdAndTeam_Id(product.getId(),
                team.getId());
        if (storageProductOptional.isPresent()) {
            return storageProductOptional.get();
        } else {
            StorageProduct sp = new StorageProduct();
            sp.setProduct(product);
            sp.setTeam(team);

            team.getStorageProducts().add(sp);
            teamRepository.save(team);
            storageProductRepository.save(sp);

            return sp;
        }
    }
}
