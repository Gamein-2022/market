package org.gamein.marketservergamein2022.infrastructure.service;

import org.gamein.marketservergamein2022.core.dto.result.ProductDTO;
import org.gamein.marketservergamein2022.core.dto.result.ProductInStorageDTO;
import org.gamein.marketservergamein2022.core.dto.result.RawMaterialDTO;
import org.gamein.marketservergamein2022.core.dto.result.RegionRawMaterialDTO;
import org.gamein.marketservergamein2022.core.service.ProductService;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Product;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.StorageProduct;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;
import org.gamein.marketservergamein2022.infrastructure.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class ProductServiceHandler implements ProductService {
    private final ProductRepository productRepository;
    private final StorageProductRepository storageProductRepository;

    public ProductServiceHandler(ProductRepository productRepository, StorageProductRepository storageProductRepository) {
        this.productRepository = productRepository;
        this.storageProductRepository = storageProductRepository;
    }

    @Override
    public RegionRawMaterialDTO getRawMaterials(Team team) {
        List<Product> myRegion = productRepository.findAllByLevelAndRegion(0, team.getRegion());
        List<Product> otherRegions = productRepository.findAllByLevelAndRegionIsNot(0, team.getRegion());
        return new RegionRawMaterialDTO(
                myRegion.stream().map(Product::toDTO).map(RawMaterialDTO::new).collect(Collectors.toList()),
                otherRegions.stream().map(Product::toDTO).map(RawMaterialDTO::new).collect(Collectors.toList())
        );
    }

    @Override
    public List<ProductDTO> getIntermediateProducts() {
        return productRepository.findAllByLevelBetween(1, 2).stream()
                .map(Product::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<ProductInStorageDTO> getFinalProducts(Long teamId) {
        return productRepository.findAllByLevelBetween(3, 3).stream()
                .map(product -> {
                    Optional<StorageProduct> spOptional = storageProductRepository
                            .findFirstByProduct_IdAndTeam_Id(product.getId(), teamId);
                    return spOptional.map(storageProduct -> new ProductInStorageDTO(product.toDTO(),
                            storageProduct.getInStorageAmount())).orElseGet(() -> new ProductInStorageDTO(product.toDTO(), 0));
                }).collect(Collectors.toList());
    }
}
