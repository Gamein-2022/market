package org.gamein.marketservergamein2022.infrastructure.util;

import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Product;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.StorageProduct;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.BuildingType;
import org.gamein.marketservergamein2022.infrastructure.repository.StorageProductRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.TeamRepository;

import java.util.List;
import java.util.Optional;

public class TeamUtil {
    public static long calculateAvailableSpace(Team team) {
        long result = team.getBuildings().stream().filter(b -> b.getType() == BuildingType.STORAGE).count() * 10000;
        for(StorageProduct storageProduct: team.getStorageProducts()) {
            result -= storageProduct.getAmount() * storageProduct.getProduct().getUnitVolume();
        }
        result -= team.getReservedSpace();
        return result;
    }

    public static void addProductToStorage(Team team, Product product, Long amount,
                                           TeamRepository teamRepo, StorageProductRepository spRepo) {
        Optional<StorageProduct> storageProductOptional = team.getStorageProducts().stream().filter(
                storageProduct -> storageProduct.getProduct().getId() == product.getId()
        ).findFirst();
        if (storageProductOptional.isPresent()) {
            StorageProduct sp = storageProductOptional.get();
            sp.setAmount(sp.getAmount() + amount);
            spRepo.save(sp);
        } else {
            StorageProduct sp = new StorageProduct();
            sp.setProduct(product);
            sp.setTeam(team);
            sp.setAmount(amount);
            sp.setStatus("what?");
            spRepo.save(sp);

            team.getStorageProducts().add(sp);
            teamRepo.save(team);
        }
    }

    public static void removeProductFromStorage(Team team, Product product, Long amount, StorageProductRepository repo)
        throws BadRequestException {
        Optional<StorageProduct> storageProductOptional = team.getStorageProducts().stream().filter(
                storageProduct ->
                        product.getId() == storageProduct.getProduct().getId()
        ).findFirst();
        if (storageProductOptional.isEmpty()) {
            throw new BadRequestException("You don't have this amount of " + product.getName() + " to sell!");
        }

        StorageProduct sp = storageProductOptional.get();
        if (sp.getAmount() < amount) {
            throw new BadRequestException("You don't have this amount of " + product.getName() + " to sell!");
        }

        sp.setAmount(sp.getAmount() - amount);
        repo.save(sp);
    }
}
