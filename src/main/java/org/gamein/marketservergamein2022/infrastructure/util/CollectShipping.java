package org.gamein.marketservergamein2022.infrastructure.util;

import lombok.AllArgsConstructor;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Shipping;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.StorageProduct;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingStatus;
import org.gamein.marketservergamein2022.infrastructure.repository.ShippingRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.StorageProductRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.TeamRepository;

import java.util.Optional;


@AllArgsConstructor
public class CollectShipping implements Runnable {
    private final Shipping shipping;
    private final ShippingRepository shippingRepo;
    private final StorageProductRepository spRepo;
    private final TeamRepository teamRepo;

    @Override
    public void run() {
        if (TeamUtil.calculateAvailableSpace(shipping.getTeam()) >=
                shipping.getProduct().getUnitVolume() * shipping.getAmount()) {

            TeamUtil.addProductToStorage(
                    TeamUtil.getOrCreateSPFromProduct(shipping.getTeam(), shipping.getProduct(), spRepo, teamRepo),
                    shipping.getAmount()
            );

            shipping.setStatus(ShippingStatus.DONE);
        } else {
            shipping.setStatus(ShippingStatus.IN_QUEUE);
        }
        shippingRepo.save(shipping);
    }
//    @Override
//    public void run() {
//        shipping.setStatus(ShippingStatus.IN_QUEUE);
//        shippingRepo.save(shipping);
//    }
}
