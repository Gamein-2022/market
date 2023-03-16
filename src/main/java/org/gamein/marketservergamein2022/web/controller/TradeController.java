package org.gamein.marketservergamein2022.web.controller;

import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.exception.NotFoundException;
import org.gamein.marketservergamein2022.core.exception.UnauthorizedException;
import org.gamein.marketservergamein2022.core.service.TradeService;
import org.gamein.marketservergamein2022.web.dto.AuthInfo;
import org.gamein.marketservergamein2022.web.dto.request.AcceptOfferRequestDTO;
import org.gamein.marketservergamein2022.web.dto.request.AcceptPendingOfferRequestDTO;
import org.gamein.marketservergamein2022.web.dto.request.CreateOfferRequestDTO;
import org.gamein.marketservergamein2022.web.dto.request.TradeWithGameinRequestDTO;
import org.gamein.marketservergamein2022.web.dto.result.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/trade")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TradeController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }


    @GetMapping(value = "/raw-materials",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResultDTO> getRawMaterials(@ModelAttribute("authInfo") AuthInfo authInfo) {
        return new ResponseEntity<>(tradeService.getRawMaterials(authInfo.getTeam()), HttpStatus.OK);
    }

    @PostMapping(value = "/trade-with-gamein",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResultDTO> tradeWithGamein(@ModelAttribute("authInfo") AuthInfo authInfo,
                                                         @RequestBody TradeWithGameinRequestDTO request) {
        try {
            TradeWithGameinResultDTO result = tradeService.tradeWithGamein(authInfo.getTeam(), request.getSide(),
                    request.getProductId(),
                    request.getQuantity());
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (BadRequestException e) {
            ErrorResultDTO error = new ErrorResultDTO(e.getMessage(), HttpStatus.BAD_REQUEST);
            return new ResponseEntity<>(error, error.getStatus());
        }
    }

    @PostMapping(value = "/offer",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResultDTO> createOffer(@ModelAttribute("authInfo") AuthInfo authInfo,
                                                     @RequestBody CreateOfferRequestDTO request) {
        try {
            CreateOfferResultDTO result = tradeService.createOffer(authInfo.getTeam(), request.getOfferType(),
                    request.getProductId(), request.getQuantity(), request.getPrice());
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (BadRequestException e) {
            ErrorResultDTO error = new ErrorResultDTO(e.getMessage(), HttpStatus.BAD_REQUEST);
            return new ResponseEntity<>(error, error.getStatus());
        }
    }

    @GetMapping(value = "/offer",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResultDTO> getOffers() {
        return new ResponseEntity<>(tradeService.getAllOffers(), HttpStatus.OK);
    }

    @GetMapping(value = "/trade-history",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResultDTO> getMyOffers(@ModelAttribute("authInfo") AuthInfo authInfo) {
        try {
            return new ResponseEntity<>(tradeService.getTeamTrades(authInfo.getTeam()), HttpStatus.OK);
        } catch (BadRequestException e) {
            ErrorResultDTO error = new ErrorResultDTO(e.getMessage(), HttpStatus.BAD_REQUEST);
            return new ResponseEntity<>(error, error.getStatus());
        }
    }

    @PostMapping(value = "/accept-offer",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResultDTO> acceptOffer(@ModelAttribute("authInfo") AuthInfo authInfo,
                                                     @RequestBody AcceptOfferRequestDTO request) {
        try {
            AcceptOfferResultDTO result = tradeService.acceptOffer(request.getOfferId(), authInfo.getTeam(),
                    request.getShippingMethod());
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (BadRequestException e) {
            ErrorResultDTO error = new ErrorResultDTO(e.getMessage(), HttpStatus.BAD_REQUEST);
            return new ResponseEntity<>(error, error.getStatus());
        }
    }

    @DeleteMapping(value = "/offer/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResultDTO> cancelOffer(@ModelAttribute("authInfo") AuthInfo authInfo,
                                                     @PathVariable(value = "id") Long offerId) {
        try {
            CreateOfferResultDTO result = tradeService.cancelOffer(authInfo.getTeam(), offerId);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (BadRequestException e) {
            ErrorResultDTO error = new ErrorResultDTO(e.getMessage(), HttpStatus.BAD_REQUEST);
            return new ResponseEntity<>(error, error.getStatus());
        } catch (UnauthorizedException e) {
            ErrorResultDTO error = new ErrorResultDTO(e.getMessage(), HttpStatus.UNAUTHORIZED);
            return new ResponseEntity<>(error, error.getStatus());
        }
    }

    @GetMapping(value = "/pending-offers", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResultDTO> getPendingOffers(@ModelAttribute("authInfo") AuthInfo authInfo) {
        return new ResponseEntity<>(tradeService.getPendingOffers(authInfo.getTeam()), HttpStatus.OK);
    }

    @PostMapping(value = "/acceptPendingOffer",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResultDTO> acceptPendingOffer(@ModelAttribute("authInfo") AuthInfo authInfo,
                                                            @RequestBody AcceptPendingOfferRequestDTO request) {
        try {
            AcceptSellOfferResultDTO result = tradeService.acceptSellOffer(request.getPendingOfferId(),
                    request.getShippingMethod(), authInfo.getTeam());
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (BadRequestException e) {
            ErrorResultDTO error = new ErrorResultDTO(e.getMessage(), HttpStatus.BAD_REQUEST);
            return new ResponseEntity<>(error, error.getStatus());
        } catch (NotFoundException e) {
            ErrorResultDTO error = new ErrorResultDTO(e.getMessage(), HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(error, error.getStatus());
        }
    }

    @PostMapping(value = "/declinePendingOffer",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResultDTO> declinePendingOffer(@ModelAttribute("authInfo") AuthInfo authInfo,
                                                             @RequestBody AcceptPendingOfferRequestDTO request) {
        try {
            DeclinePendingOfferResultDTO result = tradeService.declineSellOffer(request.getPendingOfferId(),
                    authInfo.getTeam());
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (BadRequestException e) {
            ErrorResultDTO error = new ErrorResultDTO(e.getMessage(), HttpStatus.BAD_REQUEST);
            return new ResponseEntity<>(error, error.getStatus());
        } catch (NotFoundException e) {
            ErrorResultDTO error = new ErrorResultDTO(e.getMessage(), HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(error, error.getStatus());
        }
    }
}
