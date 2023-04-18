package org.gamein.marketservergamein2022.infrastructure.service;

import org.gamein.marketservergamein2022.core.dto.result.ProductDTO;
import org.gamein.marketservergamein2022.core.dto.result.ProductInStorageDTO;
import org.gamein.marketservergamein2022.core.dto.result.RawMaterialDTO;
import org.gamein.marketservergamein2022.core.dto.result.RegionRawMaterialDTO;
import org.gamein.marketservergamein2022.core.service.ProductService;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Product;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.StorageProduct;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;
import org.gamein.marketservergamein2022.infrastructure.repository.ProductRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.StorageProductRepository;
import org.gamein.marketservergamein2022.infrastructure.util.TeamUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.Math.abs;


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
        List<Product> myRegion = productRepository.findAllByLevelAndRegionsContaining(0, team.getRegion());
        List<Product> otherRegions = productRepository.findAllByLevelAndRegionsNotContaining(0, team.getRegion());
        return new RegionRawMaterialDTO(
                myRegion.stream().map(product -> {
                            return new RawMaterialDTO(product.getId(), product.getName(),
                                    product.getPrice(), 0, 0, 0, 0);
                        })
                        .collect(Collectors.toList()),
                otherRegions.stream().map(product -> {
                    int distance = abs(team.getRegion() - TeamUtil.findMinDistanceRegion(product.getRegions(), team.getRegion()));
                    return new RawMaterialDTO(product.getId(), product.getName(),
                            product.getPrice(), distance * 5, distance * 25, distance * 50, distance * 10);
                }).collect(Collectors.toList()),
                team.getBalance()
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
