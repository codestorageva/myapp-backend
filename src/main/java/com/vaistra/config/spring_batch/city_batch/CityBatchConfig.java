package com.vaistra.config.spring_batch.city_batch;

import com.vaistra.dto.CityDto;
import com.vaistra.entity.City;
import com.vaistra.repository.CityRepository;
import com.vaistra.repository.StateRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;
import java.util.Arrays;

@Configuration
public class CityBatchConfig {

    @Bean
    @Qualifier("cityReaderJob")
    public Job cityReaderJob(JobRepository jobRepository,
                             PlatformTransactionManager transactionManager,
                             @Qualifier("chunkStepCity") Step chunkStepCity) {
        return new JobBuilder("cityReaderJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(chunkStepCity)
                .build();
    }


    @Bean
    public Step chunkStepCity(JobRepository jobRepository,
                              PlatformTransactionManager transactionManager,
                              FlatFileItemReader<CityDto> reader,
                              ItemProcessor<CityDto, City> processor,
                              ItemWriter<City> writer) {
        return new StepBuilder("cityReaderStep", jobRepository)
                .<CityDto, City>chunk(1000, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<CityDto> cityReader(@Value("#{jobParameters[inputFileCity]}") String pathToFile) {
        return new FlatFileItemReaderBuilder<CityDto>()
                .name("cityReader")
                .resource(new FileSystemResource(new File(pathToFile)))
                .delimited()
                .names("stateName", "cityName", "status")
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(CityDto.class);
                }})
                .linesToSkip(1)
                .strict(false)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<CityDto, City> cityProcessor(CityRepository cityRepository,
                                                      StateRepository stateRepository) {
        CompositeItemProcessor<CityDto, City> processor = new CompositeItemProcessor<>();
        processor.setDelegates(Arrays.asList(new CityProcessor(cityRepository, stateRepository)));
        return processor;
    }

    @Bean
    @StepScope
    public ItemWriter<City> cityWriter(CityRepository cityRepository) {
        return new CityWriter(cityRepository);
    }
}
