package org.gamein.marketservergamein2022.web.controller;

import org.gamein.marketservergamein2022.core.dto.request.*;
import org.gamein.marketservergamein2022.core.dto.result.*;
import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.exception.NotFoundException;
import org.gamein.marketservergamein2022.core.iao.AuthInfo;
import org.gamein.marketservergamein2022.core.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping("order")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class OrderController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping(value = "",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> createOrder(@ModelAttribute("authInfo") AuthInfo authInfo,
                                                  @RequestBody CreateOrderRequestDTO request) {
        try {
            return new ResponseEntity<>(
                    ServiceResult.createResult(orderService.createOrder(
                            authInfo.getTeam(),
                            request.getOrderType(),
                            request.getProductId(),
                            request.getQuantity(),
                            request.getPrice()
                    )),
                    HttpStatus.OK
            );
        } catch (BadRequestException e) {
            logger.error(e.getMessage());
            ErrorResult error = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> getOrders(
            @RequestParam("product") Optional<Long> productId
    ) {
        return new ResponseEntity<>(
                ServiceResult.createResult(orderService.getAllOrders(productId)),
                HttpStatus.OK
        );
    }

    @GetMapping(value = "history",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> getTradeHistory(@ModelAttribute("authInfo") AuthInfo authInfo) {
        return new ResponseEntity<>(
                ServiceResult.createResult(orderService.getTeamTrades(authInfo.getTeam().getId())),
                HttpStatus.OK
        );
    }

    @DeleteMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> cancelOrder(@ModelAttribute("authInfo") AuthInfo authInfo,
                                                  @PathVariable(value = "id") Long orderId) {
        try {
            return new ResponseEntity<>(
                    ServiceResult.createResult(orderService.cancelOrder(authInfo.getTeam(), orderId)),
                    HttpStatus.OK
            );
        } catch (BadRequestException e) {
            logger.error(e.toString());
            ErrorResult error = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        } catch (NotFoundException e) {
            logger.error(e.toString());
            ErrorResult error = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }
    }

    @PutMapping(value = "{id}/archive", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> archiveOrder(@ModelAttribute("authInfo") AuthInfo authInfo,
                                                   @PathVariable(value = "id") Long orderId) {
        try {
            return new ResponseEntity<>(
                    ServiceResult.createResult(orderService.archiveOrder(authInfo.getTeam(), orderId)),
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

    @GetMapping(value = "{id}/shipping-info", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> getShippingInfo(@ModelAttribute("authInfo") AuthInfo authInfo,
                                                   @PathVariable(value = "id") Long orderId) {
        try {
            return new ResponseEntity<>(
                    ServiceResult.createResult(orderService.getOrderShippingPrices(authInfo.getTeam(), orderId)),
                    HttpStatus.OK
            );
        } catch (NotFoundException e) {
            logger.error(e.toString());
            ErrorResult error = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "logs", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> getOrderLogs(@ModelAttribute("authInfo") AuthInfo authInfo) {
        return new ResponseEntity<>(
                ServiceResult.createResult(orderService.getTeamLogs(authInfo.getTeam().getId())),
                HttpStatus.OK
        );
    }
}
