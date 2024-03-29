package org.gamein.marketservergamein2022.web.controller.market;

import org.gamein.marketservergamein2022.core.dto.request.market.BuyFromGameinRequestDTO;
import org.gamein.marketservergamein2022.core.dto.request.market.SellToGameinRequestDTO;
import org.gamein.marketservergamein2022.core.dto.result.*;
import org.gamein.marketservergamein2022.core.dto.result.market.ShippingDTO;
import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.exception.NotFoundException;
import org.gamein.marketservergamein2022.core.iao.AuthInfo;
import org.gamein.marketservergamein2022.core.service.market.TradeService;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("gamein")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TradeController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping(value = "buy",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> buyFromGamein(@ModelAttribute("teamInfo") Long teamId,
                                                    @RequestBody BuyFromGameinRequestDTO request) {
        try {
            ShippingDTO result = tradeService.buyFromGamein(
                    teamId,
                    request.getProductId(),
                    request.getQuantity(),
                    request.getShippingMethod()
            );
            return new ResponseEntity<>(ServiceResult.createResult(result), HttpStatus.OK);
        } catch (BadRequestException e) {
            logger.error(e.getMessage());
            ErrorResult error = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "sell",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> sellToGamein(@ModelAttribute("teamInfo") Long teamId,
                                                   @RequestBody SellToGameinRequestDTO request) {
        try {
            return new ResponseEntity<>(ServiceResult.createResult(tradeService.sellToGamein(
                    teamId,
                    request.getProductId(),
                    request.getQuantity(),
                    request.getPrice()
            )), HttpStatus.OK);
        } catch (BadRequestException e) {
            logger.error(e.getMessage());
            ErrorResult error = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        } catch (NotFoundException e) {
            logger.error(e.getMessage());
            ErrorResult error = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> cancelOrder(@ModelAttribute("teamInfo") Long teamId,
                                                  @PathVariable Long id) {
        try {
            return new ResponseEntity<>(ServiceResult.createResult(tradeService.cancelSellOrder(
                    teamId,
                    id
            )), HttpStatus.OK);
        } catch (BadRequestException e) {
            logger.error(e.getMessage());
            ErrorResult error = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        } catch (NotFoundException e) {
            logger.error(e.getMessage());
            ErrorResult error = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping(value = "{id}/archive", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> archiveOrder(@ModelAttribute("authInfo") AuthInfo authInfo,
                                                   @PathVariable Long id) {
        try {
            return new ResponseEntity<>(ServiceResult.createResult(tradeService.archiveSellOrder(
                    authInfo.getTeam(),
                    id
            )), HttpStatus.OK);
        } catch (BadRequestException e) {
            logger.error(e.getMessage());
            ErrorResult error = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        } catch (NotFoundException e) {
            logger.error(e.getMessage());
            ErrorResult error = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "next-time", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> getNextTime() {
        return new ResponseEntity<>(
                ServiceResult.createResult(tradeService.nextTime()), HttpStatus.OK
        );
    }
}
