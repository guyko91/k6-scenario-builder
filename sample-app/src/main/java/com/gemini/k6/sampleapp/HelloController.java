package com.gemini.k6.sampleapp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Sample App!";
    }

    @GetMapping("/greeting")
    public String greeting() {
        return "Greetings from Sample App!";
    }
}
