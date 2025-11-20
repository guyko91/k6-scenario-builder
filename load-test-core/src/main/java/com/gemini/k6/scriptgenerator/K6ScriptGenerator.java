package com.gemini.k6.scriptgenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gemini.k6.apianalyzer.data.ApiControllerInfo;
import com.gemini.k6.apianalyzer.data.ApiMethodInfo;
import com.gemini.k6.apianalyzer.data.ApiParameterInfo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

public class K6ScriptGenerator {

    public void generate(File apiMetaFile, File outputDir) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<ApiControllerInfo> apiControllers = objectMapper.readValue(apiMetaFile,
                objectMapper.getTypeFactory().constructCollectionType(List.class, ApiControllerInfo.class));

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        for (ApiControllerInfo controllerInfo : apiControllers) {
            String scriptContent = generateControllerScript(controllerInfo);
            String fileName = toKebabCase(controllerInfo.getClassName().substring(controllerInfo.getClassName().lastIndexOf('.') + 1).replace("Controller", "")) + ".js";
            File outputFile = new File(outputDir, fileName);
            Files.writeString(outputFile.toPath(), scriptContent, StandardCharsets.UTF_8);
        }
    }

    private String generateControllerScript(ApiControllerInfo controllerInfo) {
        StringBuilder scriptBuilder = new StringBuilder();
        scriptBuilder.append("import http from 'k6/http';\n");
        scriptBuilder.append("import { check } from 'k6';\n\n");

        for (ApiMethodInfo methodInfo : controllerInfo.getMethods()) {
            scriptBuilder.append(generateMethodFunction(methodInfo));
            scriptBuilder.append("\n"); // Add a newline between functions
        }

        return scriptBuilder.toString();
    }

    private String generateMethodFunction(ApiMethodInfo methodInfo) {
        StringBuilder functionBuilder = new StringBuilder();
        String functionName = methodInfo.getMethodName();
        String httpMethod = methodInfo.getHttpMethod().toLowerCase();
        String path = methodInfo.getPath();

        List<String> params = methodInfo.getParameters().stream()
                .map(ApiParameterInfo::getName)
                .collect(Collectors.toList());

        functionBuilder.append("export function ").append(functionName).append("(");
        functionBuilder.append(String.join(", ", params));
        functionBuilder.append(") {\n");

                String url = "`" + path + "`";

                // Handle Path Variables

                for (ApiParameterInfo param : methodInfo.getParameters()) {

                    if ("PATH_VARIABLE".equals(param.getParamType())) {

                        url = url.replace("{" + param.getName() + "}", "${" + param.getName() + "}");

                    }

                }

        // Handle Query Parameters
        List<String> queryParams = methodInfo.getParameters().stream()
                .filter(p -> "QUERY_PARAM".equals(p.getParamType()))
                .map(ApiParameterInfo::getName)
                .collect(Collectors.toList());

        if (!queryParams.isEmpty()) {
            url = url.substring(0, url.length() - 1); // remove trailing `
            url += "?" + queryParams.stream().map(p -> p + "=${" + p + "}").collect(Collectors.joining("&")) + "`";
        }


        functionBuilder.append("  const url = ").append(url).append(";\n");

        String payload = "null";
        for (ApiParameterInfo param : methodInfo.getParameters()) {
            if ("REQUEST_BODY".equals(param.getParamType())) {
                payload = "JSON.stringify(" + param.getName() + ")";
                break;
            }
        }

        functionBuilder.append("  const payload = ").append(payload).append(";\n");

        functionBuilder.append("  const params = {\n");
        functionBuilder.append("    headers: {\n");
        functionBuilder.append("      'Content-Type': 'application/json',\n");
        functionBuilder.append("    },\n");
        functionBuilder.append("  };\n\n");

        functionBuilder.append("  const res = http.").append(httpMethod).append("(url, payload, params);\n\n");

        functionBuilder.append("  check(res, {\n");
        functionBuilder.append("    'status is 200': (r) => r.status === 200,\n");
        functionBuilder.append("  });\n");

        functionBuilder.append("}\n"); // Ensure a single newline at the end of the function

        return functionBuilder.toString();
    }

    private String toKebabCase(String input) {
        return input.replaceAll("([a-z0-9])([A-Z])", "$1-$2").toLowerCase();
    }
}
