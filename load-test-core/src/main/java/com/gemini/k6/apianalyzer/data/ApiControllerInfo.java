package com.gemini.k6.apianalyzer.data;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiControllerInfo {
    private String className;
    private String baseMapping; // Base path for the controller, e.g., "/api/v1/users"
    private List<ApiMethodInfo> methods;
    // Potentially add more fields like annotations on the class level
}
