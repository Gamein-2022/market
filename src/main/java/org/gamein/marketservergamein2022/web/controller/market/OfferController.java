package org.gamein.marketservergamein2022.web.controller.market;

import org.gamein.marketservergamein2022.core.dto.request.market.AcceptOfferRequestDTO;
import org.gamein.marketservergamein2022.core.dto.request.market.CreateOfferRequestDTO;
import org.gamein.marketservergamein2022.core.dto.result.*;
import org.gamein.marketservergamein2022.core.dto.result.market.ReceivedOffersDTO;
import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.exception.NotFoundException;
import org.gamein.marketservergamein2022.core.iao.AuthInfo;
import org.gamein.marketservergamein2022.core.service.market.OfferService;
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
    public ResponseEntity<BaseResult> createOffer(@ModelAttribute("teamInfo") Long teamId,
                                                  @RequestBody CreateOfferRequestDTO request) {
        try {
            return new ResponseEntity<>(
                    ServiceResult.createResult(offerService.createOffer(
                            teamId,
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
    public ResponseEntity<BaseResult> cancelOffer(@ModelAttribute("teamInfo") Long teamId,
                                                  @PathVariable(value = "id") Long offerId) {
        try {
            return new ResponseEntity<>(
                    ServiceResult.createResult(offerService.cancelOffer(teamId, offerId)),
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
    public ResponseEntity<BaseResult> acceptOffer(@ModelAttribute("teamInfo") Long teamId,
                                                  @RequestBody AcceptOfferRequestDTO request,
                                                  @PathVariable(value = "id") Long offerId) {
        try {
            return new ResponseEntity<>(
                    ServiceResult.createResult(offerService.acceptOffer(
                            teamId,
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
    public ResponseEntity<BaseResult> declineOffer(@ModelAttribute("teamInfo") Long teamId,
                                                  @PathVariable(value = "id") Long offerId) {
        try {
            return new ResponseEntity<>(
                    ServiceResult.createResult(offerService.declineOffer(teamId, offerId)),
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

    @PutMapping(value = "{id}/archive", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> archiveOffer(@ModelAttribute("authInfo") AuthInfo authInfo,
                                                   @PathVariable(value = "id") Long offerId) {
        try {
            return new ResponseEntity<>(
                    ServiceResult.createResult(offerService.archiveOffer(authInfo.getTeam(), offerId)),
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
