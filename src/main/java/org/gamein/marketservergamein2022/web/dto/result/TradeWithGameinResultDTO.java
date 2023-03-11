package org.gamein.marketservergamein2022.web.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public class TradeWithGameinResultDTO implements BaseResultDTO {
    private Long newBalance;
}
