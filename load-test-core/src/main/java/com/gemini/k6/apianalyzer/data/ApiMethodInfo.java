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
public class ApiMethodInfo {
    private String methodName;
    private String httpMethod; // e.g., "GET", "POST"
    private String path;       // e.g., "/hello", "/users/{id}"
    private List<ApiParameterInfo> parameters; // Information about method parameters
    // Potentially add more fields like consumes, produces, headers, etc.
}
