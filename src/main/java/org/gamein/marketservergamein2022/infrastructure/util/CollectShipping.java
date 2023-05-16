package org.gamein.marketservergamein2022.infrastructure.util;

import lombok.AllArgsConstructor;
import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Shipping;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.StorageProduct;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingStatus;
import org.gamein.marketservergamein2022.infrastructure.repository.ShippingRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.StorageProductRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.TeamRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.TimeRepository;


@AllArgsConstructor
public class CollectShipping implements Runnable {
    private final Shipping shipping;
    private final ShippingRepository shippingRepo;
    private final StorageProductRepository spRepo;
    private final TeamRepository teamRepo;
    private final TimeRepository timeRepo;

    @Override
    public void run() {
        Long a = (long) shipping.getProduct().getUnitVolume() * shipping.getAmount();
        if (TeamUtil.calculateAvailableSpace(shipping.getTeam(), timeRepo.findById(1L).get()) >= a) {


            StorageProduct sp = TeamUtil.getOrCreateSPFromProduct(shipping.getTeam(), shipping.getProduct(), spRepo,
                    teamRepo);

            try {
                TeamUtil.removeProductFromRoute(sp, shipping.getAmount());
                TeamUtil.addProductToStorage(
                        sp,
                        shipping.getAmount()
                );
                spRepo.save(sp);
            } catch (BadRequestException e) {
                throw new RuntimeException(e);
            }

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
