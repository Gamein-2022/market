package org.gamein.marketservergamein2022.core.dto.result.factory;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.gamein.marketservergamein2022.core.sharedkernel.entity.FactoryLine;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Product;

@AllArgsConstructor
@Getter
public class ValidateNewProcessResult {
    private FactoryLine factoryLine;
    private Product product;
}
