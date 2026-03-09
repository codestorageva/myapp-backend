package com.vaistra.service.impl;



import org.springframework.batch.core.*;
import com.vaistra.config.spring_batch.state_batch.StateWriter;
import com.vaistra.dto.StateDto;
import com.vaistra.dto.StateExportDTO;
import com.vaistra.dto.response.*;
import com.vaistra.dto.update.StateUpdateDto;
import com.vaistra.entity.State;
import com.vaistra.exception.DuplicateEntryException;
import com.vaistra.exception.ResourceNotFoundException;
import com.vaistra.repository.CityRepository;
import com.vaistra.repository.StateRepository;
import com.vaistra.service.StateService;
import com.vaistra.util.AppUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import lombok.Getter;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.*;
import org.springframework.http.*;
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
public class StateServiceImpl implements StateService {

    @Getter
    private static String userName;
    @PersistenceContext
    private final EntityManager entityManager;

    //---------------------------------------------------CONSTRUCTOR INJECTION------------------------------------------
    private final StateRepository stateRepository;
    private final AppUtils appUtils;
    private final JobLauncher jobLauncher;
    private final Job job;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");
    private final CityRepository cityRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    public StateServiceImpl(EntityManager entityManager, StateRepository stateRepository, AppUtils appUtils, JobLauncher jobLauncher, @Qualifier("stateReaderJob") Job job, CityRepository cityRepository) {
        this.entityManager = entityManager;
        this.stateRepository = stateRepository;
        this.appUtils = appUtils;
        this.jobLauncher = jobLauncher;
        this.job = job;
        this.cityRepository = cityRepository;
    }


    //----------------------------------------------------SERVICE METHODS-----------------------------------------------
    @Override
    public MessageResponse addState(StateDto stateDto, Map<String, String> headers) {

            stateDto.setStateName(stateDto.getStateName().trim());


            //  HANDLE DUPLICATE ENTRY STATE NAME
            if (stateRepository.existsByStateNameIgnoreCase(stateDto.getStateName()))
                throw new DuplicateEntryException(("State with name '" + stateDto.getStateName() + "' already exist!"));


            State state = new State();
            state.setStateName(stateDto.getStateName());

            if (stateDto.getStatus() != null)
                state.setStatus(stateDto.getStatus());
            else
                state.setStatus(true);
            state.setCreatedAt(LocalDateTime.now());
            state.setUpdatedAt(LocalDateTime.now());
            state.setDeletedAt(null);
            state.setIsDeleted(false);

            stateRepository.save(state);
            return new MessageResponse(true, HttpStatus.OK, "State saved.");

    }

    @Override
    public DataResponse getStateById(int stateId, Map<String, String> headers) {
        if (stateRepository.findById(stateId).isPresent())
            return new DataResponse(true, HttpStatus.OK, appUtils.stateToDto(stateRepository.findById(stateId).get()));
        else
            throw new ResourceNotFoundException("State with id '" + stateId + "' not found!");
    }

    @Override
    public HttpResponse getAllStates(String keyword, int pageNumber, int pageSize, String sortBy, String sortDirection, String isDeleted, String status, Map<String, String> headers) {
        Page<State> pageState = null;
        List<StateDto> states = null;

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
                CriteriaQuery<State> criteriaQuery = criteriaBuilder.createQuery(State.class);
                Root<State> root = criteriaQuery.from(State.class);

                Order order = sortDirection.equalsIgnoreCase("asc") ?
                        criteriaBuilder.asc(root.get(sortBy)) :
                        criteriaBuilder.desc(root.get(sortBy));
                criteriaQuery.orderBy(order);

                // Create the query to retrieve a page of results
                Predicate statusPredicate = criteriaBuilder.equal(root.get("status"), isStatus);
                Predicate deletedPredicate = criteriaBuilder.equal(root.get("isDeleted"), softDeleted);
                Predicate stateIdPredicate = criteriaBuilder.equal(root.get("stateId"), intKeyword);
                Predicate stateAndCountryNamePredicate = null;
                if (keyword != null) {
                    stateAndCountryNamePredicate =
                            criteriaBuilder.or(
                                    criteriaBuilder.like(criteriaBuilder.lower(root.get("stateName").as(String.class)), "%" + keyword.toLowerCase() + "%"));
                }

                Predicate combinedPredicate = null;

                if (isStatus != null) {
                    combinedPredicate = statusPredicate;

                    if (softDeleted != null) {

                        if (intKeyword != null) {
                            combinedPredicate = criteriaBuilder.and(combinedPredicate, stateIdPredicate, deletedPredicate);  // Add state condition
                        } else if (keyword != null) {
                            combinedPredicate = criteriaBuilder.and(combinedPredicate, stateAndCountryNamePredicate, deletedPredicate);  // Add state condition
                        } else {
                            combinedPredicate = criteriaBuilder.and(combinedPredicate, deletedPredicate);  // Add state condition
                        }
                    }
                }

                // Create the query to retrieve a page of results
                criteriaQuery.select(root)
                        .where(combinedPredicate);

                Pageable pageable = PageRequest.of(pageNumber, pageSize);
                List<State> resultList = entityManager.createQuery(criteriaQuery)
                        .setFirstResult((int) pageable.getOffset())
                        .setMaxResults(pageable.getPageSize())
                        .getResultList();

                long totalCount = entityManager.createQuery(criteriaQuery).getResultList().size();

                pageState = new PageImpl<>(resultList, pageable, totalCount);

                states = appUtils.statesToDtos(resultList);

            } catch (NoResultException ignored) {
            }

            return HttpResponse.builder()
                    .success(true)
                    .successCode(HttpStatus.OK)
                    .pageNumber(pageState.getNumber())
                    .pageSize(pageState.getSize())
                    .totalElements(pageState.getTotalElements())
                    .totalPages(pageState.getTotalPages())
                    .isLastPage(pageState.isLast())
                    .data(states)
                    .build();
        }


    @Override
    public MessageResponse updateState(StateUpdateDto stateDto, int stateId, Map<String, String> headers) {

            //  HANDLE IF STATE EXIST BY ID
            State state = stateRepository.findById(stateId)
                    .orElseThrow(() -> new ResourceNotFoundException("State with id '" + stateId + "' not found!"));

            if (stateDto.getStateName() != null) {
                State stateWithSameName = stateRepository.findByStateNameIgnoreCase(stateDto.getStateName().trim());

                if (stateWithSameName != null && !stateWithSameName.getStateId().equals(state.getStateId()))
                    throw new DuplicateEntryException("State '" + stateDto.getStateName() + "' already exist!");


                state.setStateName(stateDto.getStateName().trim());
            }


            if (stateDto.getStatus() != null)
                state.setStatus(stateDto.getStatus());

            state.setUpdatedAt(LocalDateTime.now());
            stateRepository.save(state);
            return new MessageResponse(true, HttpStatus.OK, "State updated.");

    }

    @Override
    public MessageResponse softDeleteStateById(int stateId, Map<String, String> headers) {

            State state = stateRepository.findById(stateId).orElseThrow(() -> new ResourceNotFoundException("State with id '" + stateId + "' not found!"));
            state.setIsDeleted(true);
            state.setDeletedAt(LocalDateTime.now());
            stateRepository.save(state);

            return new MessageResponse(true, HttpStatus.OK, "State with id " + stateId + "' Soft deleted!");


    }

    @Override
    public MessageResponse restoreStateById(int stateId, Map<String, String> headers) {

            State state = stateRepository.findById(stateId).orElseThrow(() -> new ResourceNotFoundException("State with id '" + stateId + "' not found!"));
            state.setIsDeleted(false);
            state.setDeletedAt(null);
            stateRepository.save(state);
            return new MessageResponse(true, HttpStatus.OK, "State with id " + stateId + "' Restored!");

    }

    @Override
    public MessageListResponse uploadStateCSV(MultipartFile file, Map<String, String> headers) {

            if (file == null)
                throw new ResourceNotFoundException("CSV File is not Uploaded");

            if (file.isEmpty())
                throw new ResourceNotFoundException("Country CSV File not found...!");

            if (!Objects.equals(file.getContentType(), "text/csv"))
                throw new IllegalArgumentException("Invalid file type. Please upload a CSV file.");

            if (!appUtils.isSupportedExtensionBatch(file.getOriginalFilename()))
                throw new ResourceNotFoundException("Only CSV and Excel File is Accepted");


            try {
                File tempFile = File.createTempFile(LocalDate.now().format(dateFormatter) + "_" + LocalTime.now().format(timeFormatter) + "_State_" + "temp", ".csv");
                String orignalFileName = file.getOriginalFilename();
                assert orignalFileName != null;
                file.transferTo(tempFile);

                JobParameters jobParameters = new JobParametersBuilder()
                        .addString("inputFileState", tempFile.getAbsolutePath())
                        .toJobParameters();
                JobExecution execution = null;
                try {
                    execution = jobLauncher.run(job, jobParameters);
                    System.out.println("test");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                long records = 0;
                long failedState = 0;

                if (execution.getExitStatus().equals(ExitStatus.COMPLETED)) {
                    System.out.println("Job is Completed....");
                    records = StateWriter.getInsertedCounter();
                    failedState = StateWriter.getFailedCounter();
                    if (tempFile.exists()) {
                        if (tempFile.delete())
                            System.out.println("File Deleted");
                        else
                            System.out.println("Can't Delete File");
                    }
                    StateWriter.setInsertedCounter(0);
                    StateWriter.setFailedCounter(0);
                }

                if (failedState == 0) {
                    return MessageListResponse.builder()
                            .success(true)
                            .successCode(HttpStatus.OK)
                            .message("All (" + records + ") State Data Uploaded Successfully")
                            .build();
                } else {
                    return MessageListResponse.builder()
                            .success(true)
                            .successCode(HttpStatus.OK)
                            .message(records + " State Data Partially Uploaded Successfully" +
                                    " Following " + failedState + " listed States are Failed to Upload Because of There are No relavant Countries exist in our Record")
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
    public ListResponse getAllStatesByStatusAndIsDeleted(Map<String, String> headers) {
        List<StateDto> states = null;

        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<State> criteriaQuery = criteriaBuilder.createQuery(State.class);
            Root<State> root = criteriaQuery.from(State.class);

            Predicate countryIdPredicate = criteriaBuilder.equal(root.get("country").get("countryId"), 145);
            Predicate statusPredicate = criteriaBuilder.equal(root.get("status"), true);
            Predicate deletedPredicate = criteriaBuilder.equal(root.get("isDeleted"), false);

            criteriaQuery.select(root)
                    .where(countryIdPredicate, statusPredicate, deletedPredicate)
                    .orderBy(criteriaBuilder.asc(root.get("stateName")));

            // Fetch results for the current page
            List<State> resultList = entityManager.createQuery(criteriaQuery).getResultList();

            // Convert the resultList to DTOs
            states = appUtils.statesToDtos(resultList);

        } catch (NoResultException ignored) {

        }

        return new ListResponse(true, HttpStatus.OK, states);

    }

    @Override
    public ListResponse exportedStateData(Map<String, String> headers) {

        List<StateExportDTO> states = new ArrayList<>();

            try {
                CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
                CriteriaQuery<State> criteriaQuery = criteriaBuilder.createQuery(State.class);
                Root<State> root = criteriaQuery.from(State.class);

                criteriaQuery.select(root)
                        .where(criteriaBuilder.equal(root.get("isDeleted"), false))
                        .orderBy(criteriaBuilder.asc(root.get("stateName")));

                List<State> resultList = entityManager.createQuery(criteriaQuery).getResultList();

                states = appUtils.stateExportDTOStoStates(resultList);

            } catch (NoResultException ignored) {
            }

            return new ListResponse(true, HttpStatus.OK, states);
    }

    @Override
    public LongResponse getTotalState() {
        Long count = stateRepository.count();
        return new LongResponse(true, HttpStatus.OK, count);
    }
}
