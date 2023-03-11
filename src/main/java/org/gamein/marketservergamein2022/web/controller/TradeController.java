package org.gamein.marketservergamein2022.web.controller;

import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.infrastructure.service.trade.TradeService;
import org.gamein.marketservergamein2022.web.dto.AuthInfo;
import org.gamein.marketservergamein2022.web.dto.request.TradeWithGameinRequestDTO;
import org.gamein.marketservergamein2022.web.dto.result.BaseResultDTO;
import org.gamein.marketservergamein2022.web.dto.result.ErrorResultDTO;
import org.gamein.marketservergamein2022.web.dto.result.TradeWithGameinResultDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/trade")
public class TradeController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }


    @PostMapping(value = "/tradeWithGamein",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResultDTO> tradeWithGamein(@ModelAttribute("authInfo") AuthInfo authInfo,
                                                         @RequestBody TradeWithGameinRequestDTO request) {
        try {
            TradeWithGameinResultDTO result = tradeService.tradeWithGamein(authInfo.getTeamId(), request.getSide(),
                    request.getProductId(),
                    request.getQuantity());
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (BadRequestException e) {
            ErrorResultDTO error = new ErrorResultDTO(e.getMessage(), HttpStatus.BAD_REQUEST);
            return new ResponseEntity<>(error, error.getStatus());
        }
    }
}
