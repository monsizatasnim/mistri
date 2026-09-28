package com.mistry.platform.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @GetMapping("/portal")
    public ResponseEntity<Map<String, String>> portal(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
                "message", "Welcome to the customer portal",
                "loggedInAs", authentication.getName()
        ));
    }
}
