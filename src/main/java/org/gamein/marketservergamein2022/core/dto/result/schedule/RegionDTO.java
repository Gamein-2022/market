package org.gamein.marketservergamein2022.core.dto.result.schedule;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class RegionDTO {
    Integer region;
    Long population;

    public static Map<Integer,Long> getRegionsPopulation(List<RegionDTO> regionDTOS){
        Map<Integer,Long> regionsPopulation = new HashMap<>();
        for (RegionDTO regionDTO : regionDTOS){
            regionsPopulation.put(regionDTO.getRegion(),regionDTO.getPopulation());
        }
        return regionsPopulation;
    }
    public RegionDTO(Integer region, Long population){
        this.region = region;
        this.population = population;
    }
}