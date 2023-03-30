package org.gamein.marketservergamein2022.infrastructure.repository;

import org.gamein.marketservergamein2022.core.sharedkernel.entity.FinalProductSellOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface FinalProductSellOrderRepository extends JpaRepository<FinalProductSellOrder, Long> {
    List<FinalProductSellOrder> findAllByClosedIsFalse();
}
