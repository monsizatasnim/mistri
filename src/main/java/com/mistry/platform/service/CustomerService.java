package com.mistry.platform.service;

import com.mistry.platform.dto.RegisterRequest;
import com.mistry.platform.dto.RegisterResponse;
import com.mistry.platform.entity.Customer;
import com.mistry.platform.exception.DuplicateAccountException;
import com.mistry.platform.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterResponse register(RegisterRequest request) {

        if (customerRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateAccountException("This phone number is already registered");
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()
                && customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateAccountException("This email is already registered");
        }

        Customer customer = new Customer();
        customer.setFullName(request.getFullName());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());
        customer.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        customer.setDefaultLocation(request.getDefaultLocation());

        Customer saved = customerRepository.save(customer);

        return new RegisterResponse(saved.getId(), "Registration successful. You can now log in.");
    }
}
