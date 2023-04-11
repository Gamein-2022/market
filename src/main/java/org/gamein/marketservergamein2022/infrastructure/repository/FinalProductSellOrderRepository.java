package org.gamein.marketservergamein2022.infrastructure.repository;

import org.gamein.marketservergamein2022.core.sharedkernel.entity.FinalProductSellOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface FinalProductSellOrderRepository extends JpaRepository<FinalProductSellOrder, Long> {
    List<FinalProductSellOrder> findAllByClosedIsFalse();

    @Query(value = "SELECT submitter_id FROM final_product_sell_order WHERE product_id = :productId",
            nativeQuery = true)
    List<Long> findTeam_IdsByProduct_Id(Long productId);

    @Query(value = "SELECT product_id FROM final_product_sell_order WHERE submitter_id = :teamId",
            nativeQuery = true)
    List<Long> findProduct_IdsByTeam_Id(Long teamId);

    @Query(value = "SELECT SUM(sold_quantity) FROM final_product_sell_order WHERE submitter_id = :teamId",
            nativeQuery = true)
    Long totalSoldAmount(Long teamId);

    @Query(value = "SELECT SUM(sold_quantity) FROM final_product_sell_order WHERE submitter_id = :teamId AND product_id = :productId",
            nativeQuery = true)
    Long totalProductSoldAmount(Long teamId, Long productId);
}
