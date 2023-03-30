package org.gamein.marketservergamein2022.core.service;

import org.gamein.marketservergamein2022.core.dto.result.ProductDTO;
import org.gamein.marketservergamein2022.core.dto.result.ProductInStorageDTO;
import org.gamein.marketservergamein2022.core.dto.result.RegionRawMaterialDTO;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;

import java.util.List;


public interface ProductService {
    RegionRawMaterialDTO getRawMaterials(Team team);

    List<ProductDTO> getIntermediateProducts();

    List<ProductInStorageDTO> getFinalProducts(Long teamId);
}
