package org.gamein.marketservergamein2022.web.controller;

import org.gamein.marketservergamein2022.core.exception.InvalidTokenException;
import org.gamein.marketservergamein2022.infrastructure.util.RestUtil;
import org.gamein.marketservergamein2022.web.dto.AuthInfo;
import org.gamein.marketservergamein2022.web.dto.result.ErrorResultDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.client.RestClientException;

import javax.servlet.http.HttpServletRequest;


@ControllerAdvice
public class InitController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @ModelAttribute(name = "authInfo")
    public AuthInfo getLoginInformation(HttpServletRequest request) throws InvalidTokenException {
        String token = request.getHeader("Authorization");
        if (token == null || token.length() < 8) {
            throw new InvalidTokenException("Invalid token!");
        }
        logger.info(token);

        try {
            AuthInfo result = RestUtil.getAuthInfo(token);
            if (result.getTeamId() == null) {
                throw new InvalidTokenException("User does not have a team!");
            }
            logger.info("userId: " + result.getUserId().toString() +
                    " --- teamId: " + result.getTeamId());
            return result;
        } catch (RestClientException e) {
            throw new InvalidTokenException("Invalid token!");
        }
    }

    @ExceptionHandler(value = InvalidTokenException.class)
    public ResponseEntity<ErrorResultDTO> exception(InvalidTokenException exception) {
        logger.error("Error: " + exception.getMessage());

        ErrorResultDTO result = new ErrorResultDTO(exception.getMessage(), HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(result, result.getStatus());
    }
}
