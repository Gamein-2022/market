package org.gamein.marketservergamein2022.infrastructure.repository;

import org.gamein.marketservergamein2022.core.sharedkernel.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ProductRepository extends JpaRepository<Product, Long> {
}
