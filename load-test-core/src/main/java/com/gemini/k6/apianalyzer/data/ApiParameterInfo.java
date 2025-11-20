package com.gemini.k6.apianalyzer.data;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiParameterInfo {
    private String name;
    private String type; // e.g., "java.lang.String", "long", "com.example.UserDto"
    private String paramType; // e.g., "PATH_VARIABLE", "QUERY_PARAM", "REQUEST_BODY", "HEADER"
    private boolean required;
    private String defaultValue; // Optional
    // Potentially add more fields like annotations on the parameter
}
