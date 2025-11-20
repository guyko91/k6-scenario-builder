package com.gemini.k6.apianalyzer;

import com.gemini.k6.apianalyzer.data.ApiControllerInfo;
import com.gemini.k6.apianalyzer.data.ApiMethodInfo;
import com.gemini.k6.apianalyzer.data.ApiParameterInfo;
import com.google.auto.service.AutoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AutoService(Processor.class)
public class ApiAnalyzerProcessor extends AbstractProcessor {

    @Override
    public synchronized void init(javax.annotation.processing.ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        // Attempt to write to a resource file using Filer to confirm execution
        try {
            Filer filer = processingEnv.getFiler();
            FileObject fileObject = filer.createResource(StandardLocation.CLASS_OUTPUT, "", "annotation_processor_log.txt");
            try (Writer writer = fileObject.openWriter()) {
                writer.write("ApiAnalyzerProcessor initialized successfully using Filer.\n");
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to write log using Filer: " + e.getMessage());
        }
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "ApiAnalyzerProcessor initialized.");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "ApiAnalyzerProcessor processing round.");

        List<ApiControllerInfo> apiControllers = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // For pretty printing JSON

        // Find all classes annotated with @RestController
        Set<? extends Element> controllers = roundEnv.getElementsAnnotatedWith(RestController.class);

        for (Element controller : controllers) {
            if (controller.getKind() != ElementKind.CLASS) {
                continue;
            }

            TypeElement controllerElement = (TypeElement) controller;
            String className = controllerElement.getQualifiedName().toString();
            String baseMapping = "";

            // Get class-level @RequestMapping
            RequestMapping classRequestMapping = controllerElement.getAnnotation(RequestMapping.class);
            if (classRequestMapping != null) {
                baseMapping = Arrays.stream(classRequestMapping.value()).findFirst().orElse("");
            }

            List<ApiMethodInfo> methods = new ArrayList<>();

            // Find all methods in the controller
            for (Element enclosedElement : controllerElement.getEnclosedElements()) {
                if (enclosedElement.getKind() == ElementKind.METHOD) {
                    ExecutableElement methodElement = (ExecutableElement) enclosedElement;

                    String httpMethod = null;
                    String path = null;

                    // Check for method-level HTTP method annotations
                    if (methodElement.getAnnotation(GetMapping.class) != null) {
                        httpMethod = "GET";
                        path = Arrays.stream(methodElement.getAnnotation(GetMapping.class).value()).findFirst().orElse("");
                    } else if (methodElement.getAnnotation(PostMapping.class) != null) {
                        httpMethod = "POST";
                        path = Arrays.stream(methodElement.getAnnotation(PostMapping.class).value()).findFirst().orElse("");
                    } else if (methodElement.getAnnotation(PutMapping.class) != null) {
                        httpMethod = "PUT";
                        path = Arrays.stream(methodElement.getAnnotation(PutMapping.class).value()).findFirst().orElse("");
                    } else if (methodElement.getAnnotation(DeleteMapping.class) != null) {
                        httpMethod = "DELETE";
                        path = Arrays.stream(methodElement.getAnnotation(DeleteMapping.class).value()).findFirst().orElse("");
                    } else if (methodElement.getAnnotation(RequestMapping.class) != null) {
                        RequestMapping methodRequestMapping = methodElement.getAnnotation(RequestMapping.class);
                        httpMethod = Arrays.stream(methodRequestMapping.method())
                                .map(Enum::name)
                                .findFirst()
                                .orElse("GET"); // Default to GET if not specified
                        path = Arrays.stream(methodRequestMapping.value()).findFirst().orElse("");
                    }

                    if (httpMethod != null && path != null) {
                        String fullPath = (baseMapping + path).replaceAll("//", "/");

                        List<ApiParameterInfo> parameters = methodElement.getParameters().stream()
                                .map(parameter -> {
                                    String paramName = parameter.getSimpleName().toString();
                                    String paramType = parameter.asType().toString();
                                    String extractedParamType = "UNKNOWN";
                                    boolean required = true;
                                    String defaultValue = null;

                                    if (parameter.getAnnotation(PathVariable.class) != null) {
                                        PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
                                        extractedParamType = "PATH_VARIABLE";
                                        paramName = pathVariable.value().isEmpty() ? paramName : pathVariable.value();
                                        required = pathVariable.required();
                                    } else if (parameter.getAnnotation(RequestParam.class) != null) {
                                        RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
                                        extractedParamType = "QUERY_PARAM";
                                        paramName = requestParam.value().isEmpty() ? paramName : requestParam.value();
                                        required = requestParam.required();
                                        if (!requestParam.defaultValue().isEmpty()) {
                                            defaultValue = requestParam.defaultValue();
                                        }
                                    } else if (parameter.getAnnotation(RequestBody.class) != null) {
                                        RequestBody requestBody = parameter.getAnnotation(RequestBody.class);
                                        extractedParamType = "REQUEST_BODY";
                                        required = requestBody.required();
                                    } else {
                                        // Default to RequestBody if it's an object and no other annotation is present
                                        // This is a simplification; in a real scenario, more sophisticated logic is needed
                                        if (parameter.asType() instanceof DeclaredType &&
                                                !parameter.asType().getKind().isPrimitive() &&
                                                !parameter.asType().toString().startsWith("java.lang.")) {
                                            extractedParamType = "REQUEST_BODY";
                                        }
                                    }

                                    return ApiParameterInfo.builder()
                                            .name(paramName)
                                            .type(paramType)
                                            .paramType(extractedParamType)
                                            .required(required)
                                            .defaultValue(defaultValue)
                                            .build();
                                })
                                .collect(Collectors.toList());

                        methods.add(ApiMethodInfo.builder()
                                .methodName(methodElement.getSimpleName().toString())
                                .httpMethod(httpMethod)
                                .path(fullPath)
                                .parameters(parameters)
                                .build());
                    }
                }
            }
            apiControllers.add(ApiControllerInfo.builder()
                    .className(className)
                    .baseMapping(baseMapping)
                    .methods(methods)
                    .build());
        }

        // Write the collected API metadata to a JSON file
        try {
            FileObject fileObject = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "api-meta.json");
            try (Writer writer = fileObject.openWriter()) {
                objectMapper.writeValue(writer, apiControllers);
            }
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "API metadata written to api-meta.json");
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to write api-meta.json: " + e.getMessage());
        }

        return true; // Claim these annotations
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Stream.of(
                RestController.class.getName(),
                RequestMapping.class.getName(),
                GetMapping.class.getName(),
                PostMapping.class.getName(),
                PutMapping.class.getName(),
                DeleteMapping.class.getName()
        ).collect(Collectors.toSet());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
