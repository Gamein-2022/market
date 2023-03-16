package org.gamein.marketservergamein2022.infrastructure.repository;

import org.gamein.marketservergamein2022.core.sharedkernel.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByLevelAndRegion(Integer level, Integer region);
    List<Product> findAllByLevelAndRegionIsNot(Integer level, Integer region);
    List<Product> findAllByLevel(Integer level);
}
