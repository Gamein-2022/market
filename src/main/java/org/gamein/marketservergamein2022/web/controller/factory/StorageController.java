package org.gamein.marketservergamein2022.web.controller.factory;


import org.gamein.marketservergamein2022.core.dto.request.factory.RemoveFromStorageRequestDTO;
import org.gamein.marketservergamein2022.core.dto.result.BaseResult;
import org.gamein.marketservergamein2022.core.dto.result.ErrorResult;
import org.gamein.marketservergamein2022.core.dto.result.ServiceResult;
import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.exception.NotFoundException;
import org.gamein.marketservergamein2022.core.iao.AuthInfo;
import org.gamein.marketservergamein2022.core.service.factory.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/storage")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class StorageController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final StorageService storageService;

    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping(value = "",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> getStorageInfo(@ModelAttribute AuthInfo authInfo) {
        return new ResponseEntity<>(
                ServiceResult.createResult(storageService.getStorageInfo(authInfo.getTeam())),
                HttpStatus.OK
        );
    }

    @GetMapping(value = "queue",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> getStorageQueue(@ModelAttribute AuthInfo authInfo) {
        return new ResponseEntity<>(
                ServiceResult.createResult(storageService.getStorageQueue(authInfo.getTeam())),
                HttpStatus.OK
        );
    }

    @PutMapping(value = "{id}/collect",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> collectFromQueue(@ModelAttribute AuthInfo authInfo,
                                                   @PathVariable(value = "id") Long shippingId) {
        try {
            return new ResponseEntity<>(
                    ServiceResult.createResult(storageService.collectFromQueue(authInfo.getTeam(), shippingId)),
                    HttpStatus.OK
            );
        } catch (NotFoundException e) {
            logger.error(e.toString());
            ErrorResult error = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        } catch (BadRequestException e) {
            logger.error(e.toString());
            ErrorResult error = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping(value = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> removeFromQueue(@ModelAttribute AuthInfo authInfo,
                                                      @PathVariable(value = "id") Long shippingId) {
        try {
            return new ResponseEntity<>(
                    ServiceResult.createResult(storageService.removeFromQueue(authInfo.getTeam(), shippingId)),
                    HttpStatus.OK
            );
        } catch (NotFoundException e) {
            logger.error(e.toString());
            ErrorResult error = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        } catch (BadRequestException e) {
            logger.error(e.toString());
            ErrorResult error = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "in-route",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> getInRouteProducts(@ModelAttribute AuthInfo authInfo) {
        return new ResponseEntity<>(
                ServiceResult.createResult(storageService.getInRouteShippings(authInfo.getTeam())),
                HttpStatus.OK
        );
    }

    @PutMapping(value = "remove",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> removeFromStorage(@ModelAttribute AuthInfo authInfo,
                                                        @RequestBody RemoveFromStorageRequestDTO request) {
        try {
            return new ResponseEntity<>(
                    ServiceResult.createResult(storageService.removeFromStorage(authInfo.getTeam(),
                            request.getProductId(), request.getQuantity())),
                    HttpStatus.OK
            );
        } catch (BadRequestException e) {
            logger.error(e.toString());
            ErrorResult error = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "upgrade-storage")
    public ResponseEntity<BaseResult> upgradeStorage (
            @ModelAttribute AuthInfo authInfo
    ){
        try {
            storageService.upgradeStorage(authInfo.getTeam());
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (BadRequestException e) {
            logger.error(e.toString());
            ErrorResult error = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }catch (Exception e){
            logger.error(e.toString());
            ErrorResult error = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
