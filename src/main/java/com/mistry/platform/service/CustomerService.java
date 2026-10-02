package com.mistry.platform.service;

import com.mistry.platform.dto.ForgotPasswordRequest;
import com.mistry.platform.dto.ForgotPasswordResponse;
import com.mistry.platform.dto.LoginRequest;
import com.mistry.platform.dto.LoginResponse;
import com.mistry.platform.dto.ProfileResponse;
import com.mistry.platform.dto.RegisterRequest;
import com.mistry.platform.dto.RegisterResponse;
import com.mistry.platform.dto.ResetPasswordRequest;
import com.mistry.platform.dto.UpdateProfileRequest;
import com.mistry.platform.entity.AccountStatus;
import com.mistry.platform.entity.Customer;
import com.mistry.platform.exception.AccountSuspendedException;
import com.mistry.platform.exception.DuplicateAccountException;
import com.mistry.platform.repository.CustomerRepository;
import com.mistry.platform.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private static final SecureRandom RANDOM = new SecureRandom();

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

        if (customer.getAccountStatus() == AccountStatus.SUSPENDED) {
            throw new AccountSuspendedException("This account has been suspended. Please contact support.");
        }

        String token = jwtUtil.generateToken(customer.getId(), identifier);

        return new LoginResponse(token, customer.getId(), customer.getFullName());
    }

    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {

        String identifier = request.getIdentifier();

        Optional<Customer> customerOpt = customerRepository.findByEmail(identifier);
        if (customerOpt.isEmpty()) {
            customerOpt = customerRepository.findByPhone(identifier);
        }

        Customer customer = customerOpt.orElseThrow(
                () -> new BadCredentialsException("No account found with this email or phone"));

        String code = String.format("%06d", RANDOM.nextInt(1000000));

        customer.setResetCode(code);
        customer.setResetCodeExpiry(LocalDateTime.now().plusMinutes(15));
        customerRepository.save(customer);

        return new ForgotPasswordResponse(
                "A verification code has been generated. In production this would be emailed or texted to the customer.",
                code);
    }

    public void resetPassword(ResetPasswordRequest request) {

        Customer customer = customerRepository.findByResetCode(request.getResetCode())
                .orElseThrow(() -> new BadCredentialsException("Invalid or expired reset code"));

        if (customer.getResetCodeExpiry() == null
                || customer.getResetCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("Invalid or expired reset code");
        }

        customer.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        customer.setResetCode(null);
        customer.setResetCodeExpiry(null);
        customerRepository.save(customer);
    }

    private Customer findByIdentifierOrThrow(String identifier) {
        Optional<Customer> customerOpt = customerRepository.findByEmail(identifier);
        if (customerOpt.isEmpty()) {
            customerOpt = customerRepository.findByPhone(identifier);
        }
        return customerOpt.orElseThrow(
                () -> new BadCredentialsException("Customer account not found"));
    }

    private ProfileResponse toProfileResponse(Customer customer) {
        return new ProfileResponse(
                customer.getId(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getDefaultLocation(),
                customer.getProfilePictureUrl());
    }

    public ProfileResponse getProfile(String loggedInIdentifier) {
        Customer customer = findByIdentifierOrThrow(loggedInIdentifier);
        return toProfileResponse(customer);
    }

    public ProfileResponse updateProfile(String loggedInIdentifier, UpdateProfileRequest request) {

        Customer customer = findByIdentifierOrThrow(loggedInIdentifier);

        if (!request.getPhone().equals(customer.getPhone())
                && customerRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateAccountException("This phone number is already registered to another account");
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !request.getEmail().equals(customer.getEmail())
                && customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateAccountException("This email is already registered to another account");
        }

        customer.setFullName(request.getFullName());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());
        customer.setDefaultLocation(request.getDefaultLocation());
        customer.setProfilePictureUrl(request.getProfilePictureUrl());

        Customer saved = customerRepository.save(customer);

        return toProfileResponse(saved);
    }
}