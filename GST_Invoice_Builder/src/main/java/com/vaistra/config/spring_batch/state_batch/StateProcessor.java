package com.vaistra.config.spring_batch.state_batch;


import com.vaistra.dto.StateDto;
import com.vaistra.entity.State;
import com.vaistra.repository.StateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class StateProcessor implements ItemProcessor<StateDto, State> {

    private  final StateRepository stateRepository;
    private final Set<String> processedRecords = new HashSet<>();



    int counter = 0;

    @Autowired
    public StateProcessor(StateRepository stateRepository) {
        this.stateRepository = stateRepository;
    }

    @Override
    public State process(StateDto stateDto) throws Exception {
        State state = new State();
        state.setStateName(stateDto.getStateName());
        state.setStatus(stateDto.getStatus());
        state.setIsDeleted(false);
        state.setCreatedAt(LocalDateTime.now());
        state.setUpdatedAt(LocalDateTime.now());
        state.setDeletedAt(null);

        return state;
    }
}