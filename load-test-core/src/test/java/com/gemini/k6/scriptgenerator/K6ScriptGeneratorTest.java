package com.gemini.k6.scriptgenerator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class K6ScriptGeneratorTest {

    private K6ScriptGenerator scriptGenerator;

    @BeforeEach
    void setUp() throws IOException {
        scriptGenerator = new K6ScriptGenerator();
        Files.createDirectories(Path.of("build")); // Ensure 'build' directory exists
    }

    @Test
    void testGenerate() throws IOException {
        // Create a dummy api-meta.json file
        String jsonContent = "[{\n" +
                "  \"className\": \"com.example.SimpleController\",\n" +
                "  \"baseMapping\": \"/api\",\n" +
                "  \"methods\": [\n" +
                "    {\n" +
                "      \"methodName\": \"sayHello\",\n" +
                "      \"httpMethod\": \"GET\",\n" +
                "      \"path\": \"/api/hello\",\n" +
                "      \"parameters\": []\n" +
                "    },\n" +
                "    {\n" +
                "      \"methodName\": \"getUserById\",\n" +
                "      \"httpMethod\": \"GET\",\n" +
                "      \"path\": \"/api/users/{id}\",\n" +
                "      \"parameters\": [\n" +
                "        {\n" +
                "          \"name\": \"id\",\n" +
                "          \"type\": \"java.lang.String\",\n" +
                "          \"paramType\": \"PATH_VARIABLE\",\n" +
                "          \"required\": true\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}]\n";
        Path apiMetaFile = Path.of("build/api-meta.json");
        Files.createDirectories(apiMetaFile.getParent());
        Files.write(apiMetaFile, jsonContent.getBytes());

        // Run the generator
        File outputDir = new File("build/k6-scripts");
        scriptGenerator.generate(apiMetaFile.toFile(), outputDir);

        // Verify the output
        File expectedFile = new File(outputDir, "simple-controller.js");
        assertTrue(expectedFile.exists());

        String scriptContent = new String(Files.readAllBytes(expectedFile.toPath()));
        String expectedScriptContent = "import http from 'k6/http';\n" +
                "import { check } from 'k6';\n\n" +
                "export function sayHello() {\n" +
                "  const url = `/api/hello`;\n" +
                "  const payload = null;\n" +
                "  const params = {\n" +
                "    headers: {\n" +
                "      'Content-Type': 'application/json',\n" +
                "    },\n" +
                "  };\n\n" +
                "  const res = http.get(url, payload, params);\n\n" +
                "  check(res, {\n" +
                "    'status is 200': (r) => r.status === 200,\n" +
                "  });\n" +
                "}\n" +
                "\n" +
                "export function getUserById(id) {\n" +
                "  const url = `/api/users/${id}` ;\n" +
                "  const payload = null;\n" +
                "  const params = {\n" +
                "    headers: {\n" +
                "      'Content-Type': 'application/json',\n" +
                "    },\n" +
                "  };\n\n" +
                "  const res = http.get(url, payload, params);\n\n" +
                "  check(res, {\n" +
                "    'status is 200': (r) => r.status === 200,\n" +
                "  });\n" +
                "}\n";
        assertEquals(expectedScriptContent, scriptContent);
    }
}