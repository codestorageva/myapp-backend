package com.vaistra.service.impl;

import com.vaistra.config.spring_batch.city_batch.CityProcessor;
import com.vaistra.config.spring_batch.city_batch.CityWriter;
import com.vaistra.dto.CityDto;
import com.vaistra.dto.CityExportDTO;
import com.vaistra.dto.response.*;
import com.vaistra.dto.update.CityUpdateDto;
import com.vaistra.entity.City;
import com.vaistra.entity.State;
import com.vaistra.exception.DuplicateEntryException;
import com.vaistra.exception.InactiveStatusException;
import com.vaistra.exception.ResourceNotFoundException;
import com.vaistra.repository.CityRepository;
import com.vaistra.repository.StateRepository;
import com.vaistra.service.CityService;
import com.vaistra.util.AppUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CityServiceImpl implements CityService {

    @PersistenceContext
    private final EntityManager entityManager;

    //---------------------------------------------------CONSTRUCTOR INJECTION------------------------------------------

    private final CityRepository cityRepository;
    private final StateRepository stateRepository;

    private final JobLauncher jobLauncher;
    private final Job job;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");
    private final AppUtils appUtils;

    @Autowired
    private RestTemplate restTemplate;

    public CityServiceImpl(EntityManager entityManager, CityRepository cityRepository, StateRepository stateRepository, JobLauncher jobLauncher, @Qualifier("cityReaderJob") Job job, AppUtils appUtils) {
        this.entityManager = entityManager;
        this.cityRepository = cityRepository;
        this.stateRepository = stateRepository;
        this.jobLauncher = jobLauncher;
        this.job = job;
        this.appUtils = appUtils;
    }


    @Override
    public MessageResponse addCity(CityDto cityDto, Map<String, String> headers) {

        cityDto.setCityName(cityDto.getCityName().trim());

        // HANDLE IF STATE EXIST BY ID
        State state = stateRepository.findById(cityDto.getStateId())
                .orElseThrow(() -> new ResourceNotFoundException("State with Id '" + cityDto.getStateId() + " not found!"));

        String stateName = state.getStateName();

        // HANDLE IF DUPLICATE DISTRICT NAME
        if (cityRepository.existsByCityNameIgnoreCaseAndState_StateNameIgnoreCase(cityDto.getCityName(), stateName))
            throw new DuplicateEntryException("City with name '" + cityDto.getCityName() + "' already exist!");

        //  IS STATE STATUS ACTIVE ?
        if (!state.getStatus())
            throw new InactiveStatusException("State with id '" + cityDto.getStateId() + "' is not active!");

        City city = new City();
        city.setCityName(cityDto.getCityName());
        city.setState(state);

        if (cityDto.getStatus() != null)
            city.setStatus(cityDto.getStatus());
        else
            city.setStatus(true);

        city.setIsDeleted(false);
        city.setCreatedAt(LocalDateTime.now());
        city.setUpdatedAt(LocalDateTime.now());
        city.setDeletedAt(null);


        cityRepository.save(city);

        return new MessageResponse(true, HttpStatus.OK, "City Saved");
    }

    @Override
    public DataResponse getCityById(int cityId, Map<String, String> headers) {

        if (cityRepository.findById(cityId).isPresent())
            return new DataResponse(true, HttpStatus.OK, appUtils.cityToDto(cityRepository.findById(cityId).get()));
        else
            throw new ResourceNotFoundException("City with id '" + cityId + "' not found!");
    }

    @Override
    public HttpResponse getAllCities(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortDirection, String isDeleted, String status, Map<String, String> headers) {

        Page<City> pageCity = null;
        List<CityDto> cities = null;

        Integer intKeyword = null;
        Boolean softDeleted = null;
        Boolean isStatus = null;

        if (isDeleted.equalsIgnoreCase("true")) {
            softDeleted = Boolean.TRUE;
        } else if (isDeleted.equalsIgnoreCase("false")) {
            softDeleted = Boolean.FALSE;
        }

        if (status.equalsIgnoreCase("true")) {
            isStatus = Boolean.TRUE;
        } else if (status.equalsIgnoreCase("false")) {
            isStatus = Boolean.FALSE;
        }

        try {
            intKeyword = Integer.parseInt(keyword);
        } catch (Exception ignored) {
        }

        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<City> criteriaQuery = criteriaBuilder.createQuery(City.class);
            Root<City> root = criteriaQuery.from(City.class);

            Join<City, State> stateJoin = root.join("state");

            Order order = sortDirection.equalsIgnoreCase("asc") ?
                    criteriaBuilder.asc(root.get(sortBy)) :
                    criteriaBuilder.desc(root.get(sortBy));
            criteriaQuery.orderBy(order);

            // Create the query to retrieve a page of results
            Predicate statusPredicate = criteriaBuilder.equal(root.get("status"), isStatus);
            Predicate deletedPredicate = criteriaBuilder.equal(root.get("isDeleted"), softDeleted);
            Predicate stateNotDeletedPredicate = criteriaBuilder.equal(stateJoin.get("isDeleted"), Boolean.FALSE);
            Predicate stateActivePredicate = criteriaBuilder.equal(stateJoin.get("status"), Boolean.TRUE);
            Predicate districtIdPredicate = criteriaBuilder.equal(root.get("cityId"), intKeyword);
            Predicate districtAndStatePredicate = null;
            if (keyword != null) {
                districtAndStatePredicate =
                        criteriaBuilder.or(
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("cityName").as(String.class)), "%" + keyword.toLowerCase() + "%"),
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("state").get("stateName").as(String.class)), "%" + keyword.toLowerCase() + "%"));
            }

            Predicate combinedPredicate = null;

            if (isStatus != null) {
                combinedPredicate = statusPredicate;

                if (softDeleted != null) {

                    if (intKeyword != null) {
                        combinedPredicate = criteriaBuilder.and(combinedPredicate, districtIdPredicate, deletedPredicate, stateActivePredicate, stateNotDeletedPredicate);  // Add state condition
                    } else if (keyword != null) {
                        combinedPredicate = criteriaBuilder.and(combinedPredicate, districtAndStatePredicate, deletedPredicate, stateActivePredicate, stateNotDeletedPredicate);  // Add state condition
                    } else {
                        combinedPredicate = criteriaBuilder.and(combinedPredicate, deletedPredicate, stateActivePredicate, stateNotDeletedPredicate);  // Add state condition
                    }
                }
            }

            // Create the query to retrieve a page of results
            criteriaQuery.select(root)
                    .where(combinedPredicate);

            Pageable pageable = PageRequest.of(pageNumber, pageSize);
            List<City> resultList = entityManager.createQuery(criteriaQuery)
                    .setFirstResult((int) pageable.getOffset())
                    .setMaxResults(pageable.getPageSize())
                    .getResultList();

            long totalCount = entityManager.createQuery(criteriaQuery).getResultList().size();

            pageCity = new PageImpl<>(resultList, pageable, totalCount);

            cities = appUtils.citiesToDtos(resultList);

        } catch (NoResultException ignored) {
        }

        return HttpResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .pageNumber(pageCity.getNumber())
                .pageSize(pageCity.getSize())
                .totalElements(pageCity.getTotalElements())
                .totalPages(pageCity.getTotalPages())
                .isLastPage(pageCity.isLast())
                .data(cities)
                .build();
    }



    @Override
    public MessageResponse updateCity(CityUpdateDto cityDto, int cityId, Map<String, String> headers) {

        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new ResourceNotFoundException("City with id '" + cityId + "' not found!"));

        String stateName = city.getState().getStateName();



        // HANDLE IF DUPLICATE DISTRICT NAME
        if (cityDto.getCityName() != null) {

            if (cityDto.getStateId() != null) {
                State state = stateRepository.findById(cityDto.getStateId())
                        .orElseThrow(() -> new ResourceNotFoundException("State with ID '" + cityDto.getStateId() + "' not found!"));

                stateName = state.getStateName();

                city.setState(state);
            }

            City cityWithSameName = cityRepository.findByCityNameIgnoreCaseAndState_StateNameIgnoreCase(cityDto.getCityName().trim(), stateName.trim());

            if (cityWithSameName != null && !cityWithSameName.getCityId().equals(city.getCityId()))
                throw new DuplicateEntryException("City '" + cityDto.getCityName() + "' already exist!");

            city.setCityName(cityDto.getCityName().trim());
        }

        if (cityDto.getStatus() != null)
            city.setStatus(cityDto.getStatus());

        city.setUpdatedAt(LocalDateTime.now());

        cityRepository.save(city);

        return new MessageResponse(true, HttpStatus.OK, "City updated.");
    }

    @Override
    public MessageResponse softDeleteCityById(int cityId, Map<String, String> headers) {

        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new ResourceNotFoundException("City with id '" + cityId + "' not found!"));

        city.setIsDeleted(true);
        city.setDeletedAt(LocalDateTime.now());
        cityRepository.save(city);

        return new MessageResponse(true, HttpStatus.OK, "City with id '" + cityId + "' soft deleted!");
    }

    @Override
    public MessageResponse restoreCityById(int cityId, Map<String, String> headers) {

        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new ResourceNotFoundException("City with id '" + cityId + "' not found!"));

        city.setIsDeleted(false);
        city.setDeletedAt(null);
        cityRepository.save(city);

        return new MessageResponse(true, HttpStatus.OK, "City with id '" + cityId + "' restored!");
    }

    @Override
    public MessageListResponse uploadCityCSV(MultipartFile file, Map<String, String> headers) {

        if (file == null)
            throw new ResourceNotFoundException("CSV File is not Uploaded");
        if (file.isEmpty())
            throw new ResourceNotFoundException("City CSV File not found...!");
        if (!Objects.equals(file.getContentType(), "text/csv"))
            throw new IllegalArgumentException("Invalid file type. Please upload a CSV file.");
        if (!appUtils.isSupportedExtensionBatch(file.getOriginalFilename()))
            throw new ResourceNotFoundException("Only CSV and Excel File is Accepted");
        try {
            File tempFile = File.createTempFile(LocalDate.now().format(dateFormatter) + "_" + LocalTime.now().format(timeFormatter) + "_City_" + "temp", ".csv");

            String orignalFileName = file.getOriginalFilename();
            assert orignalFileName != null;
            file.transferTo(tempFile);


            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("inputFileCity", tempFile.getAbsolutePath())
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(job, jobParameters);

            long records = 0;
            long failedCity = 0;

            if (execution.getExitStatus().equals(ExitStatus.COMPLETED)) {
                records = CityWriter.getInsertedCounter();
                failedCity = CityWriter.getFailedCounter();
                if (tempFile.exists()) {
                    if (tempFile.delete()) ;
                }
                CityWriter.setInsertedCounter(0);
                CityWriter.setFailedCounter(0);
            }

            if (CityProcessor.getCities() == null) {
                return MessageListResponse.builder()
                        .success(true)
                        .successCode(HttpStatus.OK)
                        .message("You Already Have All Cities in Our records")
                        .build();
            } else if (records == 0) {
                return MessageListResponse.builder()
                        .success(true)
                        .successCode(HttpStatus.OK)
                        .message("Can't Upload Any Data because of There are No relevant States  exist in our Record" +
                                " Following " + failedCity + " listed Cities Failed to Upload")
                        .data(CityProcessor.getCities())
                        .build();
            } else if (failedCity == 0) {
                return MessageListResponse.builder()
                        .success(true)
                        .successCode(HttpStatus.OK)
                        .message("All (" + records + ") City Data Uploaded Successfully")
                        .build();
            } else {
                return MessageListResponse.builder()
                        .success(true)
                        .successCode(HttpStatus.OK)
                        .message(records + " City Data Partially Uploaded Successfully" +
                                " Following " + failedCity + " listed Cities are Failed to Upload Because of There are No relevant States exist in our Record")
                        .data(CityProcessor.getCities())
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return MessageListResponse.builder()
                    .success(false)
                    .successCode(HttpStatus.BAD_REQUEST)
                    .message(e.toString())
                    .build();
        }
    }


    @Override
    public ListResponse getAllCitiesByStateIdAndStatusAndIsDeleted(Integer stateId, Map<String, String> headers) {
        List<CityDto> cities = null;
        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<City> cq = cb.createQuery(City.class);
            Root<City> root = cq.from(City.class);

            Predicate byState = cb.equal(root.get("state").get("stateId"), stateId);
            Predicate byStatus = cb.equal(root.get("status"), true);
            Predicate byNotDeleted = cb.equal(root.get("isDeleted"), false);

            cq.select(root)
                    .where(cb.and(byState, byStatus, byNotDeleted))
                    .orderBy(cb.asc(root.get("cityName"))); // Ensure this is a valid field

            List<City> resultList = entityManager.createQuery(cq).getResultList();
            System.out.println("Cities found: " + resultList.size()); // Debug log

            cities = appUtils.citiesToDtos(resultList);
        } catch (NoResultException ignored) {
            System.out.println("No cities found.");
        }
        return new ListResponse(true, HttpStatus.OK, cities);
    }


    @Override
    public ListResponse exportedCityData(Map<String, String> headers) {

        List<CityExportDTO> cities = new ArrayList<>();
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<City> criteriaQuery = criteriaBuilder.createQuery(City.class);
            Root<City> root = criteriaQuery.from(City.class);

            criteriaQuery.select(root)
                    .where(criteriaBuilder.equal(root.get("isDeleted"), false))
                    .orderBy(criteriaBuilder.asc(root.get("districtName")));

            List<City> resultList = entityManager.createQuery(criteriaQuery).getResultList();

            cities = appUtils.cityExportDTOStoCities(resultList);

        } catch (NoResultException ignored) {
        }

        return new ListResponse(true, HttpStatus.OK, cities);
    }

    @Override
    public LongResponse getTotalCity() {

        Long count = cityRepository.count();
        return new LongResponse(true,HttpStatus.OK,count);
    }
}
