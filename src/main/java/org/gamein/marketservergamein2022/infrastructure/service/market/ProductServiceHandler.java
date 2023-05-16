package org.gamein.marketservergamein2022.infrastructure.service.market;

import org.gamein.marketservergamein2022.core.dto.result.*;
import org.gamein.marketservergamein2022.core.dto.result.factory.ProductInStorageDTO;
import org.gamein.marketservergamein2022.core.dto.result.ProductDTO;
import org.gamein.marketservergamein2022.core.dto.result.RawMaterialDTO;
import org.gamein.marketservergamein2022.core.dto.result.market.RegionRawMaterialDTO;
import org.gamein.marketservergamein2022.core.service.market.ProductService;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Product;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.StorageProduct;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Time;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ShippingMethod;
import org.gamein.marketservergamein2022.infrastructure.repository.market.ProductRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.market.RegionDistanceRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.StorageProductRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.TimeRepository;
import org.gamein.marketservergamein2022.infrastructure.util.ShippingInfo;
import org.gamein.marketservergamein2022.infrastructure.util.TimeUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.gamein.marketservergamein2022.infrastructure.util.TeamUtil.calculateShippingDuration;


@Service
public class ProductServiceHandler implements ProductService {
    private final ProductRepository productRepository;
    private final StorageProductRepository storageProductRepository;
    private final TimeRepository timeRepository;
    private final RegionDistanceRepository regionDistanceRepository;

    public ProductServiceHandler(ProductRepository productRepository, StorageProductRepository storageProductRepository, TimeRepository timeRepository, RegionDistanceRepository regionDistanceRepository) {
        this.productRepository = productRepository;
        this.storageProductRepository = storageProductRepository;
        this.timeRepository = timeRepository;
        this.regionDistanceRepository = regionDistanceRepository;
    }

    @Override
    public RegionRawMaterialDTO getRawMaterials(Team team) {
        List<Product> myRegion = productRepository.findAllByLevelAndRegionsContaining(0, team.getRegion());
        List<Product> otherRegions = productRepository.findAllByLevelAndRegionsNotContaining(0, team.getRegion());
        Time time = timeRepository.findById(1L).get();
        return new RegionRawMaterialDTO(
                myRegion.stream().map(product -> new RawMaterialDTO(
                                product.getId(),
                                product.getName(),
                                product.getPrettyName(),
                                product.getPrice(),
                                0,
                                0,
                                0, product.getUnitVolume(),
                                ShippingInfo.planeBasePrice,
                                ShippingInfo.shipBasePrice,
                                ShippingInfo.planeVarPrice,
                                ShippingInfo.shipVarPrice))
                        .collect(Collectors.toList()),
                otherRegions.stream().map(product -> {
                    int distance = regionDistanceRepository.minDistance(product.getRegions(), team.getRegion());

                    return new RawMaterialDTO(product.getId(), product.getName(),
                            product.getPrettyName(),
                            product.getPrice(),
                            calculateShippingDuration(ShippingMethod.PLANE, distance),
                            calculateShippingDuration(ShippingMethod.SHIP, distance),
                            distance, product.getUnitVolume(),
                            ShippingInfo.planeBasePrice,
                            ShippingInfo.shipBasePrice,
                            ShippingInfo.planeVarPrice,
                            ShippingInfo.shipVarPrice);
                }).collect(Collectors.toList()),
                team.getBalance()
        );
    }

    @Override
    public List<ProductDTO> getIntermediateProducts() {
        Time time = timeRepository.findById(1L).get();
        TimeResultDTO resultDTO = TimeUtil.getTime(time);
        return productRepository.findAllByLevelBetweenAndEraBefore(1, 2, (byte) (resultDTO.getEra() + 1)).stream()
                .map(Product::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<ProductInStorageDTO> getFinalProducts(Long teamId) {

        return productRepository.findAllByLevelBetween(3, 3).stream()
                .filter(product -> {
                    Optional<StorageProduct> st = storageProductRepository
                            .findFirstByProduct_IdAndTeam_Id(product.getId(), teamId);
                    return st.isPresent();
                })
                .map(product -> {
                    Optional<StorageProduct> spOptional = storageProductRepository
                            .findFirstByProduct_IdAndTeam_Id(product.getId(), teamId);
                    return spOptional.map(storageProduct -> new ProductInStorageDTO(product.toDTO(),
                            storageProduct.getInStorageAmount())).orElseGet(() -> new ProductInStorageDTO(product.toDTO(), 0));
                }).collect(Collectors.toList());
    }
}
