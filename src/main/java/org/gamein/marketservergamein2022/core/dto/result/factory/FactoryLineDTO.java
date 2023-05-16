package org.gamein.marketservergamein2022.core.dto.result.factory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.gamein.marketservergamein2022.core.sharedkernel.entity.Product;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.LineStatus;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.LineType;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ProductGroup;

import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
public class FactoryLineDTO {
    private long id;
    private LineType type;
    private Product product;
    private Integer count;
    private LineStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime currentTime;
    private ProductGroup group;
}
