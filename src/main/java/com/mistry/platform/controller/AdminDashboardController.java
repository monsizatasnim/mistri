package com.mistry.platform.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminDashboardController {

    @GetMapping("/dashboard")
    public Map<String, String> dashboard(Authentication authentication) {
        return Map.of(
                "message", "Welcome to the admin dashboard.",
                "loggedInAs", authentication.getName()
        );
    }
}