package org.gamein.marketservergamein2022.infrastructure.repository.market;

import org.gamein.marketservergamein2022.core.sharedkernel.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    List<Brand> findAllByPeriod(Long period);
}
