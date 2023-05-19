package org.gamein.marketservergamein2022.web.controller.factory;


import org.gamein.marketservergamein2022.core.dto.request.factory.StartResearchRequestDTO;
import org.gamein.marketservergamein2022.core.dto.result.BaseResult;
import org.gamein.marketservergamein2022.core.dto.result.ErrorResult;
import org.gamein.marketservergamein2022.core.dto.result.ServiceResult;
import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.exception.NotFoundException;
import org.gamein.marketservergamein2022.core.iao.AuthInfo;
import org.gamein.marketservergamein2022.core.service.factory.ResearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/research")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ResearchController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ResearchService researchService;



    public ResearchController(ResearchService researchService) {
        this.researchService = researchService;

    }

    @GetMapping(value = "",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> getTeamResearches(
            @ModelAttribute("authInfo") AuthInfo authInfo
    ) {
        return new ResponseEntity<>(
                ServiceResult.createResult(researchService.getTeamResearches(authInfo.getTeam())),
                HttpStatus.OK
        );
    }

    @PostMapping(value = "",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> startResearch(
            @ModelAttribute("teamInfo") Long teamId,
            @RequestBody StartResearchRequestDTO request
    ) {
        try {
            return new ResponseEntity<>(
                    ServiceResult.createResult(researchService.startResearchProcess(
                            teamId,
                            request.getName()
                    )),
                    HttpStatus.OK
            );
        } catch (BadRequestException e) {
            logger.error(e.getMessage(),e);
            ErrorResult result = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        } catch (NotFoundException e) {
            logger.error(e.getMessage(),e);
            ErrorResult result = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "current",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> getCurrentResearch(@ModelAttribute("authInfo") AuthInfo authInfo) {
            return new ResponseEntity<>(
                    ServiceResult.createResult(researchService.getCurrentResearch(
                            authInfo.getTeam()
                    )),
                    HttpStatus.OK
            );
    }

    @GetMapping(value = "subjects/{name}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> getSubjectInfo(@ModelAttribute AuthInfo authInfo,
                                                     @PathVariable(value = "name") String name) {
        try {
            return new ResponseEntity<>(
                    ServiceResult.createResult(researchService.getSubjectInfo(authInfo.getTeam(), name)),
                    HttpStatus.OK
            );
        } catch (NotFoundException e) {
            logger.error(e.getMessage(),e);
            ErrorResult result = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping(value = "subjects/{name}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> putOffResearch(@ModelAttribute("teamInfo") Long teamId,
                                                     @PathVariable(value = "name") String name) {
        try {
            return new ResponseEntity<>(
                    ServiceResult.createResult(researchService.stopResearch(teamId, name)),
                    HttpStatus.OK
            );
        } catch (BadRequestException e) {
            logger.error(e.getMessage(),e);
            ErrorResult result = new ErrorResult(e.getMessage());
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
    }
}
