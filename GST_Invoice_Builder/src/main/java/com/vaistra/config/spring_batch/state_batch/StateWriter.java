package com.vaistra.config.spring_batch.state_batch;



import com.vaistra.entity.State;
import com.vaistra.repository.StateRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class StateWriter implements ItemWriter<State> {
    @Getter
    @Setter
    private static long insertedCounter = 0;
    @Getter
    @Setter
    private static long FailedCounter = 0;
    private final StateRepository stateRepository;

    @Autowired
    public StateWriter(StateRepository stateRepository) {
        this.stateRepository = stateRepository;
    }

    @Override
    public void write(Chunk<? extends State> chunk) throws Exception {
        for (State state : chunk) {
            try {
                stateRepository.save(state);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}