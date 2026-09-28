package com.mistry.platform.service;

import com.mistry.platform.dto.LoginRequest;
import com.mistry.platform.dto.LoginResponse;
import com.mistry.platform.dto.RegisterRequest;
import com.mistry.platform.dto.RegisterResponse;
import com.mistry.platform.entity.Customer;
import com.mistry.platform.exception.DuplicateAccountException;
import com.mistry.platform.repository.CustomerRepository;
import com.mistry.platform.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

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

    public LoginResponse login(LoginRequest request) {

        String identifier = request.getIdentifier();

        Optional<Customer> customerOpt = customerRepository.findByEmail(identifier);
        if (customerOpt.isEmpty()) {
            customerOpt = customerRepository.findByPhone(identifier);
        }

        Customer customer = customerOpt.orElseThrow(
                () -> new BadCredentialsException("Invalid email/phone or password"));

        if (!passwordEncoder.matches(request.getPassword(), customer.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email/phone or password");
        }

        String token = jwtUtil.generateToken(customer.getId(), identifier);

        return new LoginResponse(token, customer.getId(), customer.getFullName());
    }
}
