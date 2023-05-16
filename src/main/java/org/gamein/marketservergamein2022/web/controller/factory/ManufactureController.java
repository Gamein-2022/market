package org.gamein.marketservergamein2022.web.controller.factory;


import org.gamein.marketservergamein2022.core.dto.request.factory.InitFactoryLineProductRequestDTO;
import org.gamein.marketservergamein2022.core.dto.request.factory.StartNewProcessRequestDTO;
import org.gamein.marketservergamein2022.core.dto.result.BaseResult;
import org.gamein.marketservergamein2022.core.dto.result.ErrorResult;
import org.gamein.marketservergamein2022.core.dto.result.ServiceResult;
import org.gamein.marketservergamein2022.core.dto.result.factory.FactoryLineDTO;
import org.gamein.marketservergamein2022.core.exception.*;
import org.gamein.marketservergamein2022.core.iao.AuthInfo;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.LineType;
import org.gamein.marketservergamein2022.infrastructure.service.factory.ManufactureServiceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("line")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ManufactureController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ManufactureServiceHandler serviceHandler;

    public ManufactureController(ManufactureServiceHandler manufactureServiceHandler) {
        this.serviceHandler = manufactureServiceHandler;
    }

    @GetMapping("groups")
    public ResponseEntity<BaseResult> getTeamAvailableProducts(
            @ModelAttribute("authInfo") AuthInfo authInfo,
            @RequestParam(value = "t") int t
            ){
        return new ResponseEntity<>(ServiceResult.createResult(serviceHandler.getGroups(t == 0 ? LineType.PRODUCTION
                : LineType.ASSEMBLY)),
                HttpStatus.OK);
    }

    @GetMapping("{id}/available")
    public ResponseEntity<BaseResult> getLineAvailableProducts(
            @ModelAttribute("authInfo") AuthInfo authInfo,
            @PathVariable(value = "id") Long lineId
    ){
        try {
            return new ResponseEntity<>(
                    ServiceResult.createResult(serviceHandler.getAvailableProducts(authInfo.getTeam(), lineId)),
                    HttpStatus.OK);
        } catch (UnauthorizedException e) {
            logger.error(e.getMessage());
            ErrorResult result = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(result, HttpStatus.UNAUTHORIZED);
        } catch (NotFoundException e) {
            logger.error(e.getMessage());
            ErrorResult result = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
        } catch (BadRequestException e) {
            logger.error(e.getMessage());
            ErrorResult result = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("init")
    ResponseEntity<BaseResult> init(
            @ModelAttribute("authInfo") AuthInfo authInfo,
            @RequestBody InitFactoryLineProductRequestDTO request
    ) {
        try {
            FactoryLineDTO factoryLineDTO = serviceHandler
                    .initFactoryLine(authInfo.getTeam(), request.getGroup(), request.getLineId());
            ServiceResult<FactoryLineDTO> result = ServiceResult.createResult(factoryLineDTO);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NotFoundException e) {
            logger.error(e.getMessage());
            ErrorResult result = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
        } catch (UnauthorizedException e) {
            logger.error(e.getMessage());
            ErrorResult result = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(result, HttpStatus.UNAUTHORIZED);
        } catch (BadRequestException e) {
            logger.error(e.getMessage());
            ErrorResult result = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("start")
    ResponseEntity<BaseResult> startNewProcess(
            @ModelAttribute("authInfo") AuthInfo authInfo,
            @RequestBody StartNewProcessRequestDTO request
    ) {
        try {
            FactoryLineDTO factoryLineDTO = serviceHandler.startNewProcess(
                    authInfo.getTeam(), request.getLineId(), request.getProductId(), request.getCount());
            ServiceResult<FactoryLineDTO> result = ServiceResult.createResult(factoryLineDTO);
            return new ResponseEntity<>(result, HttpStatus.OK);

        } catch (UnauthorizedException e) {
            logger.error(e.getMessage(), e);
            ErrorResult errorResult = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(errorResult, HttpStatus.UNAUTHORIZED);
        } catch (NotFoundException | NotEnoughMaterial | NotEnoughMoneyException e) {
            logger.error(e.getMessage(), e);
            ErrorResult errorResult = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(errorResult, HttpStatus.NOT_FOUND);
        } catch (LineInProgressException | BadRequestException e) {
            logger.error(e.getMessage(), e);
            ErrorResult errorResult = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(errorResult, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity<BaseResult> cancelProcess(
            @ModelAttribute("authInfo") AuthInfo authInfo,
            @PathVariable(value = "id") Long lineId
    ) {
        try {
            return new ResponseEntity<>(ServiceResult.createResult(
                    serviceHandler.cancelProcess(authInfo.getTeam(), lineId)
            ), HttpStatus.OK);
        } catch (UnauthorizedException e) {
            logger.error(e.getMessage(), e);
            ErrorResult errorResult = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(errorResult, HttpStatus.UNAUTHORIZED);
        } catch (NotFoundException e) {
            logger.error(e.getMessage(), e);
            ErrorResult errorResult = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(errorResult, HttpStatus.NOT_FOUND);
        } catch (BadRequestException e) {
            logger.error(e.getMessage(), e);
            ErrorResult errorResult = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(errorResult, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping()
    ResponseEntity<BaseResult> getTeamLines(
            @ModelAttribute("authInfo") AuthInfo authInfo
    ) {
        List<FactoryLineDTO> factoryLineDTOS = serviceHandler.getTeamLines(authInfo.getTeam());
        ServiceResult<List<FactoryLineDTO>> result = ServiceResult.createResult(factoryLineDTOS);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("collect")
    ResponseEntity<BaseResult> collectLine(
            @ModelAttribute("authInfo") AuthInfo authInfo,
            @RequestParam("id") Long lineId
    ) {
        try {
            FactoryLineDTO factoryLineDTO = serviceHandler.collectLine(authInfo.getTeam(), lineId);
            ServiceResult<FactoryLineDTO> result = ServiceResult.createResult(factoryLineDTO);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (UnauthorizedException e) {
            logger.error(e.getMessage(), e);
            ErrorResult errorResult = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(errorResult, HttpStatus.UNAUTHORIZED);
        } catch (NotFoundException e) {
            logger.error(e.getMessage(), e);
            ErrorResult errorResult = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(errorResult, HttpStatus.NOT_FOUND);
        } catch (RemainignTimeException | BadRequestException e) {
            logger.error(e.getMessage(), e);
            ErrorResult errorResult = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(errorResult, HttpStatus.BAD_REQUEST);
        }
    }

//    @GetMapping("req")
//    public ResponseEntity<BaseResult> creatingProductRequirements(
//            @ModelAttribute("authInfo") AuthInfo authInfo,
//            @RequestParam("productId") Long productId,
//            @RequestParam("lineId") Long lineId
//    ) {
//        try {
//            CreatingRequirementsDTO requirementDTOS = serviceHandler
//                    .getCreatingProductRequirements(lineId, productId, authInfo.getTeam());
//            ServiceResult<CreatingRequirementsDTO> result = ServiceResult.createResult(requirementDTOS);
//            return new ResponseEntity<>(result, HttpStatus.OK);
//
//        } catch (UnauthorizedException e) {
//            logger.error(e.getMessage(), e);
//            ErrorResult errorResult = new ErrorResult(e.getMessage());
//            return new ResponseEntity<>(errorResult, HttpStatus.UNAUTHORIZED);
//        } catch (NotFoundException e) {
//            logger.error(e.getMessage(), e);
//            ErrorResult errorResult = new ErrorResult(e.getMessage());
//            return new ResponseEntity<>(errorResult, HttpStatus.NOT_FOUND);
//        } catch (BadRequestException e) {
//            logger.error(e.getMessage(), e);
//            ErrorResult errorResult = new ErrorResult(e.getMessage());
//            return new ResponseEntity<>(errorResult, HttpStatus.BAD_REQUEST);
//        }
//    }

}
