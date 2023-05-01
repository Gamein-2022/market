package org.gamein.marketservergamein2022.core.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.OrderType;

import java.time.LocalDateTime;
import java.util.Date;


@AllArgsConstructor
@Getter
public class OrderDTO {
    private final Long id;
    private final OrderType orderType;
    private final String submitterTeamName;
    private final String productName;
    private final Integer quantity;
    private final Long unitPrice;
    private final LocalDateTime submitDate;
    private final Boolean cancelled;
    private final LocalDateTime acceptDate;
    private final int region;
    private final Integer offerCount;
}
