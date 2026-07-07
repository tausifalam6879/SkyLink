package com.skylink.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/")
    public String home() {
        return "SkyLink API is Running Successfully 🚀";
    }

    @GetMapping("/test")
    public String test() {
        return "Test API Working";
    }
}