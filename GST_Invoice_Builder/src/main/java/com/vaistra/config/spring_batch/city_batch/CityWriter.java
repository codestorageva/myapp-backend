package com.vaistra.config.spring_batch.city_batch;

import com.vaistra.entity.City;
import com.vaistra.repository.CityRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

@Slf4j
public class CityWriter implements ItemWriter<City> {

    private final CityRepository cityRepository;

    @Getter
    @Setter
    private static long insertedCounter = 0;
    @Getter
    @Setter
    private static long failedCounter = 0;

    public CityWriter(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    @Override
    public void write(Chunk<? extends City> chunk) {
        for (City city : chunk) {
            synchronized (this) {
                try {
                    if (city.getState() != null &&
                            !cityRepository.existsByCityNameIgnoreCaseAndState_StateNameIgnoreCase(city.getCityName(), city.getState().getStateName())) {
                        cityRepository.save(city);
                        insertedCounter++;
                    } else {
                        failedCounter++;
                    }
                } catch (Exception e) {
                    log.error("Failed to insert city: {}", city.getCityName(), e);
                    failedCounter++;
                }
            }
        }
    }
}
