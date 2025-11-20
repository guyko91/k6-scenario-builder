package com.gemini.k6.apianalyzer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.gemini.k6.apianalyzer.data.ApiControllerInfo;
import com.gemini.k6.apianalyzer.data.ApiMethodInfo;
import com.gemini.k6.apianalyzer.data.ApiParameterInfo;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ApiAnalyzerProcessorTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Test
    void testSimpleRestController() {
        JavaFileObject controllerFile = JavaFileObjects.forSourceLines(
                "com.example.SimpleController",
                "package com.example;",
                "",
                "import org.springframework.web.bind.annotation.GetMapping;",
                "import org.springframework.web.bind.annotation.RestController;",
                "",
                "@RestController",
                "public class SimpleController {",
                "",
                "    @GetMapping(\"/simple/hello\")",
                "    public String sayHello() {",
                "        return \"Hello\";",
                "    }",
                "}"
        );

        Compilation compilation = javac()
                .withProcessors(new ApiAnalyzerProcessor())
                .compile(controllerFile);

        assertThat(compilation).succeededWithoutWarnings();
        assertThat(compilation).generatedFile(javax.tools.StandardLocation.CLASS_OUTPUT, "api-meta.json");

        try {
            String jsonOutput = compilation.generatedFile(javax.tools.StandardLocation.CLASS_OUTPUT, "api-meta.json")
                    .orElseThrow(() -> new AssertionError("api-meta.json not generated"))
                    .getCharContent(true)
                    .toString();

            List<ApiControllerInfo> apiControllers = objectMapper.readValue(jsonOutput,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ApiControllerInfo.class));

            assertNotNull(apiControllers);
            assertEquals(1, apiControllers.size());

            ApiControllerInfo controllerInfo = apiControllers.get(0);
            assertEquals("com.example.SimpleController", controllerInfo.getClassName());
            assertEquals("", controllerInfo.getBaseMapping());
            assertEquals(1, controllerInfo.getMethods().size());

            ApiMethodInfo methodInfo = controllerInfo.getMethods().get(0);
            assertEquals("sayHello", methodInfo.getMethodName());
            assertEquals("GET", methodInfo.getHttpMethod());
            assertEquals("/simple/hello", methodInfo.getPath());
            assertTrue(methodInfo.getParameters().isEmpty());

        } catch (IOException e) {
            throw new RuntimeException("Failed to read generated api-meta.json", e);
            }
        }
        
            @Test
            void testSimplePathVariable() {
                JavaFileObject controllerFile = JavaFileObjects.forSourceLines(
                        "com.example.PathVariableController",
                        "package com.example;",
                        "",
                        "import org.springframework.web.bind.annotation.GetMapping;",
                        "import org.springframework.web.bind.annotation.PathVariable;",
                        "import org.springframework.web.bind.annotation.RestController;",
                        "",
                        "@RestController",
                        "public class PathVariableController {",
                        "",
                        "    @GetMapping(\"/items/{itemId}\")",
                        "    public String getItem(@PathVariable String itemId) {",
                        "        return \"Item: \" + itemId;",
                        "    }",
                        "}"
                );
        
                Compilation compilation = javac()
                        .withProcessors(new ApiAnalyzerProcessor())
                        .compile(controllerFile);
        
                assertThat(compilation).succeededWithoutWarnings();
                assertThat(compilation).generatedFile(javax.tools.StandardLocation.CLASS_OUTPUT, "api-meta.json");
        
                try {
                    String jsonOutput = compilation.generatedFile(javax.tools.StandardLocation.CLASS_OUTPUT, "api-meta.json")
                            .orElseThrow(() -> new AssertionError("api-meta.json not generated"))
                            .getCharContent(true)
                            .toString();
        
                    List<ApiControllerInfo> apiControllers = objectMapper.readValue(jsonOutput,
                            objectMapper.getTypeFactory().constructCollectionType(List.class, ApiControllerInfo.class));
        
                    assertNotNull(apiControllers);
                    assertEquals(1, apiControllers.size());
        
                    ApiControllerInfo controllerInfo = apiControllers.get(0);
                    assertEquals("com.example.PathVariableController", controllerInfo.getClassName());
                    assertEquals(1, controllerInfo.getMethods().size());
        
                    ApiMethodInfo methodInfo = controllerInfo.getMethods().get(0);
                    assertEquals("getItem", methodInfo.getMethodName());
                    assertEquals("GET", methodInfo.getHttpMethod());
                    assertEquals("/items/{itemId}", methodInfo.getPath());
                    assertEquals(1, methodInfo.getParameters().size());
        
                    ApiParameterInfo paramInfo = methodInfo.getParameters().get(0);
                    assertEquals("itemId", paramInfo.getName());
                    assertEquals("java.lang.String", paramInfo.getType());
                    assertEquals("PATH_VARIABLE", paramInfo.getParamType());
                    assertTrue(paramInfo.isRequired());
        
                } catch (IOException e) {
                    throw new RuntimeException("Failed to read generated api-meta.json", e);
                }
            }
        
            @Test
            void testSimplePostRequest() {
                JavaFileObject controllerFile = JavaFileObjects.forSourceLines(
                        "com.example.PostController",
                        "package com.example;",
                        "",
                        "import org.springframework.web.bind.annotation.PostMapping;",
                        "import org.springframework.web.bind.annotation.RequestBody;",
                        "import org.springframework.web.bind.annotation.RestController;",
                        "",
                        "class ItemDto {",
                        "    private String name;",
                        "    public String getName() { return name; }",
                        "    public void setName(String name) { this.name = name; }",
                        "}",
                        "",
                        "@RestController",
                        "public class PostController {",
                        "",
                        "    @PostMapping(\"/items\")",
                        "    public String createItem(@RequestBody ItemDto item) {",
                        "        return \"Created: \" + item.getName();",
                        "    }",
                        "}"
                );
        
                Compilation compilation = javac()
                        .withProcessors(new ApiAnalyzerProcessor())
                        .compile(controllerFile);
        
                assertThat(compilation).succeededWithoutWarnings();
                assertThat(compilation).generatedFile(javax.tools.StandardLocation.CLASS_OUTPUT, "api-meta.json");
        
                try {
                    String jsonOutput = compilation.generatedFile(javax.tools.StandardLocation.CLASS_OUTPUT, "api-meta.json")
                            .orElseThrow(() -> new AssertionError("api-meta.json not generated"))
                            .getCharContent(true)
                            .toString();
        
                    List<ApiControllerInfo> apiControllers = objectMapper.readValue(jsonOutput,
                            objectMapper.getTypeFactory().constructCollectionType(List.class, ApiControllerInfo.class));
        
                    assertNotNull(apiControllers);
                    assertEquals(1, apiControllers.size());
        
                    ApiControllerInfo controllerInfo = apiControllers.get(0);
                    assertEquals("com.example.PostController", controllerInfo.getClassName());
                    assertEquals(1, controllerInfo.getMethods().size());
        
                    ApiMethodInfo methodInfo = controllerInfo.getMethods().get(0);
                    assertEquals("createItem", methodInfo.getMethodName());
                    assertEquals("POST", methodInfo.getHttpMethod());
                    assertEquals("/items", methodInfo.getPath());
                    assertEquals(1, methodInfo.getParameters().size());
        
                    ApiParameterInfo paramInfo = methodInfo.getParameters().get(0);
                    assertEquals("item", paramInfo.getName());
                    assertEquals("com.example.ItemDto", paramInfo.getType());
                    assertEquals("REQUEST_BODY", paramInfo.getParamType());
                    assertTrue(paramInfo.isRequired());
        
                } catch (IOException e) {
                    throw new RuntimeException("Failed to read generated api-meta.json", e);
                }
            }
        
            @Test
            void testDeleteWithPathVariableAndBody() {
                JavaFileObject controllerFile = JavaFileObjects.forSourceLines(
                        "com.example.DeleteController",
                        "package com.example;",
                        "",
                        "import org.springframework.web.bind.annotation.DeleteMapping;",
                        "import org.springframework.web.bind.annotation.PathVariable;",
                        "import org.springframework.web.bind.annotation.RequestBody;",
                        "import org.springframework.web.bind.annotation.RestController;",
                        "",
                        "class ReasonDto {",
                        "    private String reason;",
                        "    public String getReason() { return reason; }",
                        "    public void setReason(String reason) { this.reason = reason; }",
                        "}",
                        "",
                        "@RestController",
                        "public class DeleteController {",
                        "",
                        "    @DeleteMapping(\"/items/{id}\")",
                        "    public void deleteItem(@PathVariable Long id, @RequestBody ReasonDto reason) {",
                        "        // Deletes item with a reason",
                        "    }",
                        "}"
                );
        
                Compilation compilation = javac()
                        .withProcessors(new ApiAnalyzerProcessor())
                        .compile(controllerFile);
        
                assertThat(compilation).succeededWithoutWarnings();
                assertThat(compilation).generatedFile(javax.tools.StandardLocation.CLASS_OUTPUT, "api-meta.json");
        
                try {
                    String jsonOutput = compilation.generatedFile(javax.tools.StandardLocation.CLASS_OUTPUT, "api-meta.json")
                            .orElseThrow(() -> new AssertionError("api-meta.json not generated"))
                            .getCharContent(true)
                            .toString();
        
                    List<ApiControllerInfo> apiControllers = objectMapper.readValue(jsonOutput,
                            objectMapper.getTypeFactory().constructCollectionType(List.class, ApiControllerInfo.class));
        
                    assertNotNull(apiControllers);
                    assertEquals(1, apiControllers.size());
        
                    ApiControllerInfo controllerInfo = apiControllers.get(0);
                    assertEquals("com.example.DeleteController", controllerInfo.getClassName());
                    assertEquals(1, controllerInfo.getMethods().size());
        
                    ApiMethodInfo methodInfo = controllerInfo.getMethods().get(0);
                    assertEquals("deleteItem", methodInfo.getMethodName());
                    assertEquals("DELETE", methodInfo.getHttpMethod());
                    assertEquals("/items/{id}", methodInfo.getPath());
                    assertEquals(2, methodInfo.getParameters().size());
        
                    ApiParameterInfo idParam = methodInfo.getParameters().get(0);
                    assertEquals("id", idParam.getName());
                    assertEquals("java.lang.Long", idParam.getType());
                    assertEquals("PATH_VARIABLE", idParam.getParamType());
                    assertTrue(idParam.isRequired());
                    
                    ApiParameterInfo reasonParam = methodInfo.getParameters().get(1);
                    assertEquals("reason", reasonParam.getName());
                    assertEquals("com.example.ReasonDto", reasonParam.getType());
                    assertEquals("REQUEST_BODY", reasonParam.getParamType());
                    assertTrue(reasonParam.isRequired());
        
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to read generated api-meta.json", e);
                        }
                    }
                
                    @Test
                    void testControllerWithRequestMappingAndParameters() {
                        JavaFileObject controllerFile = JavaFileObjects.forSourceLines(
                                "com.example.ComplexController",
                                "package com.example;",                "",
                "import org.springframework.web.bind.annotation.DeleteMapping;",
                "import org.springframework.web.bind.annotation.GetMapping;",
                "import org.springframework.web.bind.annotation.PathVariable;",
                "import org.springframework.web.bind.annotation.PostMapping;",
                "import org.springframework.web.bind.annotation.RequestBody;",
                "import org.springframework.web.bind.annotation.RequestMapping;",
                "import org.springframework.web.bind.annotation.RequestParam;",
                "import org.springframework.web.bind.annotation.RestController;",
                "",
                "class UserDto {",
                "    private String name;",
                "    private int age;",
                "    public String getName() { return name; }",
                "    public void setName(String name) { this.name = name; }",
                "    public int getAge() { return age; }",
                "    public void setAge(int age) { this.age = age; }",
                "}",
                "",
                "@RestController",
                "@RequestMapping(\"/api/v1/users\")",
                "public class ComplexController {",
                "",
                "    @GetMapping(\"/{id}\")",
                "    public String getUserById(@PathVariable(\"id\") Long userId) {",
                "        return \"User: \" + userId;",
                "    }",
                "",
                "    @PostMapping",
                "    public String createUser(@RequestBody UserDto user, @RequestParam(name = \"dryRun\", required = false) boolean dryRun) {",
                "        return \"Created: \" + user.getName() + (dryRun ? \" (dry run)\" : \"\");",
                "    }",
                "",
                "    @DeleteMapping(\"/{id}\")",
                "    public void deleteUser(@PathVariable Long id) {",
                "        // Deletes user",
                "    }",
                "}"
        );

        Compilation compilation = javac()
                .withProcessors(new ApiAnalyzerProcessor())
                .compile(controllerFile);

        assertThat(compilation).succeededWithoutWarnings();
        assertThat(compilation).generatedFile(javax.tools.StandardLocation.CLASS_OUTPUT, "api-meta.json");

        try {
            String jsonOutput = compilation.generatedFile(javax.tools.StandardLocation.CLASS_OUTPUT, "api-meta.json")
                    .orElseThrow(() -> new AssertionError("api-meta.json not generated"))
                    .getCharContent(true)
                    .toString();

            List<ApiControllerInfo> apiControllers = objectMapper.readValue(jsonOutput,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ApiControllerInfo.class));

            assertNotNull(apiControllers);
            assertEquals(1, apiControllers.size());

            ApiControllerInfo controllerInfo = apiControllers.get(0);
            assertEquals("com.example.ComplexController", controllerInfo.getClassName());
            assertEquals("/api/v1/users", controllerInfo.getBaseMapping());
            assertEquals(3, controllerInfo.getMethods().size());

            // Test getUserById
            ApiMethodInfo getUserByIdMethod = controllerInfo.getMethods().stream()
                    .filter(m -> m.getMethodName().equals("getUserById"))
                    .findFirst().orElseThrow();
            assertEquals("GET", getUserByIdMethod.getHttpMethod());
            assertEquals("/api/v1/users/{id}", getUserByIdMethod.getPath());
            assertEquals(1, getUserByIdMethod.getParameters().size());
            ApiParameterInfo userIdParam = getUserByIdMethod.getParameters().get(0);
            assertEquals("id", userIdParam.getName());
            assertEquals("java.lang.Long", userIdParam.getType());
            assertEquals("PATH_VARIABLE", userIdParam.getParamType());
            assertTrue(userIdParam.isRequired());

            // Test createUser
            ApiMethodInfo createUserMethod = controllerInfo.getMethods().stream()
                    .filter(m -> m.getMethodName().equals("createUser"))
                    .findFirst().orElseThrow();
            assertEquals("POST", createUserMethod.getHttpMethod());
            assertEquals("/api/v1/users", createUserMethod.getPath());
            assertEquals(2, createUserMethod.getParameters().size());

            ApiParameterInfo userParam = createUserMethod.getParameters().get(0);
            assertEquals("user", userParam.getName());
            assertEquals("com.example.UserDto", userParam.getType());
            assertEquals("REQUEST_BODY", userParam.getParamType());
            assertTrue(userParam.isRequired());

            ApiParameterInfo dryRunParam = createUserMethod.getParameters().get(1);
            assertEquals("dryRun", dryRunParam.getName());
            assertEquals("boolean", dryRunParam.getType());
            assertEquals("QUERY_PARAM", dryRunParam.getParamType());
            assertFalse(dryRunParam.isRequired()); // `required = false` in @RequestParam

            // Test deleteUser
            ApiMethodInfo deleteUserMethod = controllerInfo.getMethods().stream()
                    .filter(m -> m.getMethodName().equals("deleteUser"))
                    .findFirst().orElseThrow();
            assertEquals("DELETE", deleteUserMethod.getHttpMethod());
            assertEquals("/api/v1/users/{id}", deleteUserMethod.getPath());
            assertEquals(1, deleteUserMethod.getParameters().size());
            ApiParameterInfo idParam = deleteUserMethod.getParameters().get(0);
            assertEquals("id", idParam.getName());
            assertEquals("java.lang.Long", idParam.getType()); // Note: in source it's 'Long id', so it's a wrapper type
            assertEquals("PATH_VARIABLE", idParam.getParamType());
            assertTrue(idParam.isRequired());


        } catch (IOException e) {
            throw new RuntimeException("Failed to read generated api-meta.json", e);
        }
    }
}