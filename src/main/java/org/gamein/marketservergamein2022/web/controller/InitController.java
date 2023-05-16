package org.gamein.marketservergamein2022.web.controller;

import org.gamein.marketservergamein2022.core.dto.result.ErrorResult;
import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.exception.InvalidTokenException;
import org.gamein.marketservergamein2022.core.service.dashboard.AuthService;
import org.gamein.marketservergamein2022.infrastructure.util.RestUtil;
import org.gamein.marketservergamein2022.core.iao.AuthInfo;
import org.gamein.marketservergamein2022.core.iao.AuthInfoResponse;
import org.gamein.marketservergamein2022.web.controller.market.OfferController;
import org.gamein.marketservergamein2022.web.controller.market.OrderController;
import org.gamein.marketservergamein2022.web.controller.market.ProductController;
import org.gamein.marketservergamein2022.web.controller.market.TradeController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice(assignableTypes = {OfferController.class, OrderController.class,
        ProductController.class, TradeController.class})
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class InitController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AuthService authService;

    @Value("${auth.url}")
    private String authUrl;

    public InitController(AuthService authService) {
        this.authService = authService;
    }

    @ModelAttribute(name = "authInfo")
    public AuthInfo getLoginInformation(HttpServletRequest request) throws InvalidTokenException, BadRequestException {
        String token = request.getHeader("Authorization");
        if (token == null || token.length() < 8) {
            throw new InvalidTokenException("Invalid token!");
        }

        try {
            AuthInfoResponse result = RestUtil.getAuthInfo(token, authUrl);
            if (result.getTeamId() == null) {
                throw new InvalidTokenException("User does not have a team!");
            }
            logger.info("userId: " + result.getUserId().toString() +
                    " --- teamId: " + result.getTeamId());

            return new AuthInfo(
                    authService.getUserById(result.getUserId()),
                    authService.getTeamById(result.getTeamId())
            );
        } catch (RestClientException e) {
            throw new InvalidTokenException("Invalid token!");
        }
    }

    @ExceptionHandler(value = InvalidTokenException.class)
    public ResponseEntity<ErrorResult> exception(InvalidTokenException exception) {
        logger.error("Error: " + exception.getMessage());

        ErrorResult result = new ErrorResult(exception.getMessage());
        return new ResponseEntity<>(result, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = BadRequestException.class)
    public ResponseEntity<ErrorResult> exception(BadRequestException exception) {
        logger.error("Error: " + exception.getMessage());

        ErrorResult result = new ErrorResult(exception.getMessage());
        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }
}
