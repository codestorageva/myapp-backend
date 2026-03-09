package com.vaistra.config.spring_batch.city_batch;

import com.vaistra.dto.CityDto;
import com.vaistra.entity.City;
import com.vaistra.entity.State;
import com.vaistra.repository.CityRepository;
import com.vaistra.repository.StateRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class CityProcessor implements ItemProcessor<CityDto, City> {

    private final CityRepository cityRepository;
    private final StateRepository stateRepository;

    @Getter
    private static final Map<Integer, String[]> cities = new HashMap<>();
    private final Set<String> processedRecords = new HashSet<>();
    private int counter = 0;

    public CityProcessor(CityRepository cityRepository, StateRepository stateRepository) {
        this.cityRepository = cityRepository;
        this.stateRepository = stateRepository;
    }

    @Override
    public City process(CityDto cityDto) {
        City city = new City();
        city.setCityName(cityDto.getCityName());
        city.setStatus(cityDto.getStatus());
        city.setIsDeleted(false);
        city.setCreatedAt(LocalDateTime.now());
        city.setUpdatedAt(LocalDateTime.now());
        city.setDeletedAt(null);

        synchronized (this) {
            State state = stateRepository.findByStateNameIgnoreCase(cityDto.getStateName());
            if (state != null) {
                city.setState(state);
            } else {
                cities.put(counter++, new String[]{cityDto.getStateName(), cityDto.getCityName()});
            }
        }

        return city;
    }
}
