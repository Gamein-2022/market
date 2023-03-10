package org.gamein.marketservergamein2022.web.controller;

import org.gamein.marketservergamein2022.core.service.OfferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/offer")
public class OfferController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }
}