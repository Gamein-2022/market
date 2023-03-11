package org.gamein.marketservergamein2022.infrastructure.service.trade;

import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.web.dto.result.TradeWithGameinResultDTO;

public interface TradeService {
    TradeWithGameinResultDTO tradeWithGamein(Long teamId, String side, Long productId, Long quantity)
            throws BadRequestException;
}
