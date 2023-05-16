package org.gamein.marketservergamein2022.web.controller.factory;



import org.gamein.marketservergamein2022.core.dto.request.factory.CreateBuildingRequestDTO;
import org.gamein.marketservergamein2022.core.dto.result.BaseResult;
import org.gamein.marketservergamein2022.core.dto.result.ErrorResult;
import org.gamein.marketservergamein2022.core.dto.result.ServiceResult;
import org.gamein.marketservergamein2022.core.dto.result.factory.BuildingDTO;
import org.gamein.marketservergamein2022.core.dto.result.factory.TeamBuildingsResult;
import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.exception.NotEnoughMoneyException;
import org.gamein.marketservergamein2022.core.exception.NotFoundException;
import org.gamein.marketservergamein2022.core.iao.AuthInfo;
import org.gamein.marketservergamein2022.core.service.factory.BuildingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/building")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class BuildingController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final BuildingService buildingService;

    public BuildingController(BuildingService buildingService) {
        this.buildingService = buildingService;
    }

    @PostMapping(value = "",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> createBuilding(
            @ModelAttribute("authInfo") AuthInfo authInfo,
            @RequestBody CreateBuildingRequestDTO request
    ) {
        try {
            BuildingDTO buildingDTO = buildingService.createBuilding(
                    authInfo.getTeam(),
                    request.getType(),
                    request.getGround()
            );
            ServiceResult<BuildingDTO> result = ServiceResult.createResult(buildingDTO);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (BadRequestException | NotEnoughMoneyException e) {
            logger.error(e.getMessage());
            ErrorResult result = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping(value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> upgradeBuilding(
            @ModelAttribute("authInfo") AuthInfo authInfo,
            @PathVariable(value = "id") long buildingId
    ) {
        try {
            BuildingDTO buildingDTO = buildingService.upgradeBuilding(
                    authInfo.getTeam(),
                    buildingId
            );
            ServiceResult<BuildingDTO> result = ServiceResult.createResult(buildingDTO);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (BadRequestException e) {
            logger.error(e.getMessage());
            ErrorResult result = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        } catch (NotFoundException e) {
            logger.error(e.getMessage());
            ErrorResult result = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping(value = "/{ground}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> destroyBuilding(
            @ModelAttribute("authInfo") AuthInfo authInfo,
            @PathVariable(value = "ground") byte ground
    ) {
        try {
            return new ResponseEntity<>(ServiceResult.createResult(buildingService.destroyBuilding(
                    authInfo.getTeam(),
                    ground
            )), HttpStatus.OK);
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

    @GetMapping(value = "",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ServiceResult<TeamBuildingsResult>> getTeamBuildings(@ModelAttribute("authInfo") AuthInfo authInfo) {
        ServiceResult<TeamBuildingsResult> result =
                ServiceResult.createResult(buildingService.getTeamBuildings(authInfo.getTeam()));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping(value = "upgrade-region",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> upgradeRegion(
            @ModelAttribute("authInfo") AuthInfo authInfo
    ) {
        try {
            buildingService.upgradeRegion(authInfo.getTeam());
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NotEnoughMoneyException | BadRequestException e) {
            logger.error(e.getMessage());
            ErrorResult result = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "{ground}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> getGroundInfo(
            @ModelAttribute("authInfo") AuthInfo authInfo,
            @PathVariable(value = "ground") byte ground
    ) {
        return new ResponseEntity<>(
                ServiceResult.createResult(buildingService.getGroundDetails(authInfo.getTeam(), ground)),
                HttpStatus.OK
        );
    }
}