package com.vaistra.service;


import com.vaistra.dto.StateDto;
import com.vaistra.dto.response.*;
import com.vaistra.dto.update.StateUpdateDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface StateService {

    MessageResponse addState(StateDto stateDto, Map<String, String> headers);

    DataResponse getStateById(int stateId, Map<String, String> headers);

    HttpResponse getAllStates(String keyword, int pageNumber, int pageSize, String sortBy, String sortDirection, String isDeleted, String status, Map<String, String> headers);

    MessageResponse updateState(StateUpdateDto stateDto, int stateId, Map<String, String> headers);

//    MessageResponse deleteStateById(int id, Map<String, String> headers);

    MessageResponse softDeleteStateById(int stateId, Map<String, String> headers);

    MessageResponse restoreStateById(int stateId, Map<String, String> headers);

    MessageListResponse uploadStateCSV(MultipartFile file, Map<String, String> headers);

    ListResponse getAllStatesByStatusAndIsDeleted(Map<String, String> headers);

    ListResponse exportedStateData(Map<String, String> headers);

    LongResponse getTotalState();
}
