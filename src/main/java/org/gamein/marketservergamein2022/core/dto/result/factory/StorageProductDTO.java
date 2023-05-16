package org.gamein.marketservergamein2022.core.dto.result.factory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.gamein.marketservergamein2022.core.dto.result.ProductDTO;


@AllArgsConstructor
@Getter
public class StorageProductDTO {
    private ProductDTO product;
    private long inStorageAmount;
    private long inRouteAmount;
    private long manufacturingAmount;
    private long blockedAmount;
    private long sellableAmount;
}
