package org.gamein.marketservergamein2022.web.controller;

import org.gamein.marketservergamein2022.core.dto.request.*;
import org.gamein.marketservergamein2022.core.dto.result.*;
import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.exception.NotFoundException;
import org.gamein.marketservergamein2022.core.iao.AuthInfo;
import org.gamein.marketservergamein2022.core.service.OfferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("offer")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class OfferController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @PostMapping(value = "",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> createOffer(@ModelAttribute("authInfo") AuthInfo authInfo,
                                                  @RequestBody CreateOfferRequestDTO request) {
        try {
            return new ResponseEntity<>(
                    ServiceResult.createResult(offerService.createOffer(
                            authInfo.getTeam(),
                            request.getOrderId(),
                            request.getShippingMethod()
                    )),
                    HttpStatus.OK
            );
        } catch (BadRequestException e) {
            logger.error(e.toString());
            ErrorResult error = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        } catch (NotFoundException e) {
            logger.error(e.toString());
            ErrorResult error = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "received", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> getIncomingOffers(@ModelAttribute("authInfo") AuthInfo authInfo) {
        return new ResponseEntity<>(
                ServiceResult.createResult(
                        new ReceivedOffersDTO(offerService.getReceivedOffers(authInfo.getTeam().getId()),
                                authInfo.getTeam().getBalance())
                ),
                HttpStatus.OK
        );
    }

    @GetMapping(value = "received/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> getRecievedOfferByOrder(@ModelAttribute("authInfo") AuthInfo authInfo,
                                                              @PathVariable(value = "id") Long orderId) {
        return new ResponseEntity<>(
                ServiceResult.createResult(
                        new ReceivedOffersDTO(offerService.getOrderOffers(authInfo.getTeam().getId(), orderId),
                                authInfo.getTeam().getBalance())
                ),
                HttpStatus.OK
        );
    }

    @GetMapping(value = "sent", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> getSentOffers(@ModelAttribute("authInfo") AuthInfo authInfo) {
        return new ResponseEntity<>(
                ServiceResult.createResult(offerService.getSentOffers(authInfo.getTeam().getId())),
                HttpStatus.OK
        );
    }

    @DeleteMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> cancelOffer(@ModelAttribute("authInfo") AuthInfo authInfo,
                                                  @PathVariable(value = "id") Long offerId) {
        try {
            return new ResponseEntity<>(
                    ServiceResult.createResult(offerService.cancelOffer(authInfo.getTeam(), offerId)),
                    HttpStatus.OK
            );
        } catch (BadRequestException e) {
            logger.error(e.toString());
            ErrorResult error = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        } catch (NotFoundException e) {
            logger.error(e.toString());
            ErrorResult error = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping(value = "{id}/accept",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> acceptOffer(@ModelAttribute("authInfo") AuthInfo authInfo,
                                                  @RequestBody AcceptOfferRequestDTO request,
                                                  @PathVariable(value = "id") Long offerId) {
        try {
            return new ResponseEntity<>(
                    ServiceResult.createResult(offerService.acceptOffer(
                            authInfo.getTeam(),
                            offerId,
                            request.getShippingMethod()
                    )),
                    HttpStatus.OK
            );
        } catch (BadRequestException e) {
            logger.error(e.toString());
            ErrorResult error = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        } catch (NotFoundException e) {
            logger.error(e.toString());
            ErrorResult error = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping(value = "{id}/decline", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> declineOffer(@ModelAttribute("authInfo") AuthInfo authInfo,
                                                  @PathVariable(value = "id") Long offerId) {
        try {
            return new ResponseEntity<>(
                    ServiceResult.createResult(offerService.declineOffer(authInfo.getTeam(), offerId)),
                    HttpStatus.OK
            );
        } catch (BadRequestException e) {
            logger.error(e.toString());
            ErrorResult error = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        } catch (NotFoundException e) {
            logger.error(e.toString());
            ErrorResult error = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
    }
}
