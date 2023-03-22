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
    public static long calculateAvailableSpace(Team team) {
        long result = team.getBuildings().stream().filter(b -> b.getType() == BuildingType.STORAGE).count() * 10000;
        for (StorageProduct storageProduct : team.getStorageProducts()) {
            result -= storageProduct.getInStorageAmount() * storageProduct.getProduct().getUnitVolume();
        }
        result -= team.getReservedSpace();
        return result;
    }

    public static void addProductToStorage(Team team, Product product, Long amount,
                                           TeamRepository teamRepo, StorageProductRepository spRepo,
                                           String where, boolean queueable) throws BadRequestException {
        Optional<StorageProduct> storageProductOptional = team.getStorageProducts().stream().filter(
                storageProduct -> storageProduct.getProduct().getId() == product.getId()
        ).findFirst();

        StorageProduct sp;

        long availableSpace = calculateAvailableSpace(team);
        long queueableAmount = 0;

        if (queueable) {
            queueableAmount = amount - (availableSpace / product.getUnitVolume());
            if (queueableAmount < 0) {
                queueableAmount = 0;
            }
            amount -= queueableAmount;
        } else if (availableSpace > amount * product.getUnitVolume()) {
            throw new BadRequestException("You don't have enough space in storage!");
        }

        if (storageProductOptional.isPresent()) {
            sp = storageProductOptional.get();
        } else {
            sp = new StorageProduct();
            sp.setProduct(product);
            sp.setTeam(team);
            sp.setInStorageAmount(0);
            sp.setInQueueAmount(0);
            sp.setManufacturingAmount(0);
            sp.setInRouteAmount(0);

            team.getStorageProducts().add(sp);
        }

        switch (where) {
            case "storage" -> {
                sp.setInStorageAmount(sp.getInStorageAmount() + amount);
                sp.setInQueueAmount(sp.getInQueueAmount() + queueableAmount);
            }
            case "queue" -> sp.setInQueueAmount(sp.getInQueueAmount() + amount);
            case "manufacture" -> sp.setManufacturingAmount(sp.getManufacturingAmount() + amount);
            case "shipping" -> sp.setInRouteAmount(sp.getInRouteAmount() + amount);
        }

        spRepo.save(sp);
        teamRepo.save(team);
    }

    public static void removeProductFromStorage(Team team, Product product, Long amount, StorageProductRepository repo)
            throws BadRequestException {
        Optional<StorageProduct> storageProductOptional = team.getStorageProducts().stream().filter(
                storageProduct ->
                        product.getId() == storageProduct.getProduct().getId()
        ).findFirst();
        if (storageProductOptional.isEmpty()) {
            throw new BadRequestException("You don't have this amount of " + product.getName() + "!");
        }

        StorageProduct sp = storageProductOptional.get();
        if (sp.getInStorageAmount() + sp.getInQueueAmount() < amount) {
            throw new BadRequestException("You don't have this amount of " + product.getName() + "!");
        }

        if (amount > sp.getInQueueAmount()) {
            sp.setInStorageAmount(sp.getInStorageAmount() + sp.getInQueueAmount() - amount);
        } else {
            sp.setInQueueAmount(sp.getInQueueAmount() - amount);
        }
        repo.save(sp);
    }
}
