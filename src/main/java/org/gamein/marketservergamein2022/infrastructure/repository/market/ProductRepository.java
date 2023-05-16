package org.gamein.marketservergamein2022.infrastructure.repository.market;

import org.gamein.marketservergamein2022.core.sharedkernel.entity.Product;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ProductGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByLevelAndRegionsContaining(Integer level, Integer region);
    List<Product> findAllByLevelAndRegionsNotContaining(Integer level, Integer region);
    List<Product> findAllByLevelBetween(Integer lower, Integer upper);

    List<Product> findAllByLevelBetweenAndEraBefore(Integer lower,Integer upper,Byte era);

    Product findProductById(Long id);
    List<Product> findAllByGroup(ProductGroup group);

    List<Product> findAllByGroupAndEraBefore(ProductGroup group,Byte era);
    Boolean existsByGroupAndAvailableDayLessThanEqual(ProductGroup group, Long availableDay);
}
