package org.gamein.marketservergamein2022.infrastructure.util;

import lombok.AllArgsConstructor;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Shipping;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.StorageProduct;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingStatus;
import org.gamein.marketservergamein2022.infrastructure.repository.ShippingRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.StorageProductRepository;

import java.util.Optional;


@AllArgsConstructor
public class CollectShipping implements Runnable {
    private final Shipping shipping;
    private final ShippingRepository shippingRepo;
    private final StorageProductRepository spRepo;

//    @Override
//    public void run() {
//        if (TeamUtil.calculateAvailableSpace(shipping.getTeam()) >=
//                shipping.getProduct().getUnitVolume() * shipping.getAmount()) {
//            Optional<StorageProduct> storageProductOptional = shipping.getTeam().getStorageProducts().stream().filter(
//                    storageProduct -> storageProduct.getProduct().getId() == shipping.getProduct().getId()
//            ).findFirst();
//            if (storageProductOptional.isPresent()) {
//                StorageProduct sp = storageProductOptional.get();
//                sp.setInRouteAmount(sp.getInRouteAmount() - shipping.getAmount());
//                sp.setInStorageAmount(sp.getInStorageAmount() + shipping.getAmount());
//                spRepo.save(sp);
//            }
////            else {
////                // TODO throw some error
////            }
//            shipping.setStatus(ShippingStatus.DONE);
//        } else {
//            shipping.setStatus(ShippingStatus.IN_QUEUE);
//            // TODO schedule expiration after 1 minute
//        }
//        shippingRepo.save(shipping);
//    }
    @Override
    public void run() {
        shipping.setStatus(ShippingStatus.IN_QUEUE);
        shippingRepo.save(shipping);
    }
}
