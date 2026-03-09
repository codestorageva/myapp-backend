package com.vaistra.dto.response;

import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MapResponse {
    Map<String, Object> response = new HashMap<>();
}
