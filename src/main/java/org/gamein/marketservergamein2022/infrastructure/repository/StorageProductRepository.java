package org.gamein.marketservergamein2022.infrastructure.repository;

import org.gamein.marketservergamein2022.core.sharedkernel.entity.StorageProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StorageProductRepository extends JpaRepository<StorageProduct, Long> {
    Optional<StorageProduct> findFirstByProduct_IdAndTeam_Id(Long productId, Long teamId);
}
