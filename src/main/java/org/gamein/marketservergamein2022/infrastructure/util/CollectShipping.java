package org.gamein.marketservergamein2022.infrastructure.util;

import lombok.AllArgsConstructor;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Shipping;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.StorageProduct;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingStatus;
import org.gamein.marketservergamein2022.infrastructure.repository.*;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;


@AllArgsConstructor
public class CollectShipping implements Runnable {
    private final Shipping shipping;
    private final ShippingRepository shippingRepo;
    private final StorageProductRepository spRepo;
    private final TeamRepository teamRepo;
    private final TimeRepository timeRepo;
    private final TeamDateRepository teamDateRepository;


//    @Override
//    public void run() {
//        Long a = (long) shipping.getProduct().getUnitVolume() * shipping.getAmount();
//        if (TeamUtil.calculateAvailableSpace(shipping.getTeam()) >= a) {
//
//            teamDateRepository.updateTeamDate(LocalDateTime.now(ZoneOffset.UTC),shipping.getTeam().getId());
//            StorageProduct sp = TeamUtil.getSPFromProduct(shipping.getTeam(), shipping.getProduct()).get();
//
//            TeamUtil.removeProductFromRoute(sp, shipping.getAmount());
//            TeamUtil.addProductToStorage(
//                    sp,
//                    shipping.getAmount()
//            );
//            spRepo.save(sp);
//
//
//            shipping.setStatus(ShippingStatus.DONE);
//        } else {
//            shipping.setStatus(ShippingStatus.IN_QUEUE);
//        }
//        shippingRepo.save(shipping);
//    }
    @Override
    public void run() {
        shipping.setStatus(ShippingStatus.IN_QUEUE);
        shippingRepo.save(shipping);
    }
}
