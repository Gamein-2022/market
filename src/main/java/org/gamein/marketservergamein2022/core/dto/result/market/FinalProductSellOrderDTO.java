package org.gamein.marketservergamein2022.core.dto.result.market;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.gamein.marketservergamein2022.core.dto.result.ProductDTO;

import java.util.Date;


@AllArgsConstructor
@Getter
public class FinalProductSellOrderDTO {
    private Long id;
    private Long unitPrice;
    private Date submitDate;
    private Boolean cancelled;
    private Boolean closed;
    private Date acceptDate;
    private ProductDTO product;
    private Integer quantity;
    private Integer soldQuantity;
    private long submitterId;
}
