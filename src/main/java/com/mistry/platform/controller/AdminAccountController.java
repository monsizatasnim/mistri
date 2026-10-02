package com.mistry.platform.controller;

import com.mistry.platform.dto.CustomerAccountResponse;
import com.mistry.platform.dto.ProviderAccountResponse;
import com.mistry.platform.entity.AccountStatus;
import com.mistry.platform.service.AdminAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/accounts")
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    public AdminAccountController(AdminAccountService adminAccountService) {
        this.adminAccountService = adminAccountService;
    }

    @GetMapping("/customers")
    public ResponseEntity<List<CustomerAccountResponse>> searchCustomers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) AccountStatus status) {
        return ResponseEntity.ok(adminAccountService.searchCustomers(search, status));
    }

    @GetMapping("/customers/{id}")
    public ResponseEntity<CustomerAccountResponse> getCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(adminAccountService.getCustomer(id));
    }

    @PostMapping("/customers/{id}/suspend")
    public ResponseEntity<CustomerAccountResponse> suspendCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(adminAccountService.suspendCustomer(id));
    }

    @PostMapping("/customers/{id}/reactivate")
    public ResponseEntity<CustomerAccountResponse> reactivateCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(adminAccountService.reactivateCustomer(id));
    }

    @GetMapping("/providers")
    public ResponseEntity<List<ProviderAccountResponse>> searchProviders(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) AccountStatus status) {
        return ResponseEntity.ok(adminAccountService.searchProviders(search, status));
    }

    @GetMapping("/providers/{id}")
    public ResponseEntity<ProviderAccountResponse> getProvider(@PathVariable Long id) {
        return ResponseEntity.ok(adminAccountService.getProvider(id));
    }

    @PostMapping("/providers/{id}/suspend")
    public ResponseEntity<ProviderAccountResponse> suspendProvider(@PathVariable Long id) {
        return ResponseEntity.ok(adminAccountService.suspendProvider(id));
    }

    @PostMapping("/providers/{id}/reactivate")
    public ResponseEntity<ProviderAccountResponse> reactivateProvider(@PathVariable Long id) {
        return ResponseEntity.ok(adminAccountService.reactivateProvider(id));
    }
}