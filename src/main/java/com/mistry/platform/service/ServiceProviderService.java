package com.mistry.platform.service;

import com.mistry.platform.dto.ServiceProviderRegisterResponse;
import com.mistry.platform.dto.ServiceProviderLoginResponse;
import com.mistry.platform.dto.ServiceProviderVerificationResponse;
import com.mistry.platform.entity.AccountStatus;
import com.mistry.platform.entity.ServiceProvider;
import com.mistry.platform.entity.VerificationStatus;
import com.mistry.platform.exception.AccountSuspendedException;
import com.mistry.platform.exception.DuplicateAccountException;
import com.mistry.platform.repository.ServiceProviderRepository;
import com.mistry.platform.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ServiceProviderService {

    @Autowired
    private ServiceProviderRepository providerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private JwtUtil jwtUtil;

    public ServiceProviderRegisterResponse register(
            String fullName,
            String phone,
            String email,
            String password,
            String nidNumber,
            String tradeLicenseNumber,
            MultipartFile nidDocument,
            MultipartFile tradeLicenseDocument,
            MultipartFile experienceCertificate
    ) {
        if (providerRepository.existsByEmail(email)) {
            throw new DuplicateAccountException("An account with this email already exists.");
        }

        String nidPath = fileStorageService.store(nidDocument, "nid");
        String tradeLicensePath = fileStorageService.store(tradeLicenseDocument, "trade_license");
        String experiencePath = fileStorageService.store(experienceCertificate, "experience_cert");

        ServiceProvider provider = new ServiceProvider();
        provider.setFullName(fullName);
        provider.setPhone(phone);
        provider.setEmail(email);
        provider.setPassword(passwordEncoder.encode(password));
        provider.setNidNumber(nidNumber);
        provider.setNidDocumentPath(nidPath);
        provider.setTradeLicenseNumber(tradeLicenseNumber);
        provider.setTradeLicenseDocumentPath(tradeLicensePath);
        provider.setExperienceCertificatePath(experiencePath);
        provider.setVerificationStatus(VerificationStatus.PENDING);

        ServiceProvider saved = providerRepository.save(provider);

        return new ServiceProviderRegisterResponse(
                saved.getId(),
                "Registration successful. Your documents are pending admin verification."
        );
    }

    public ServiceProviderLoginResponse login(String email, String password) {
        ServiceProvider provider = providerRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(password, provider.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (provider.getAccountStatus() == AccountStatus.SUSPENDED) {
            throw new AccountSuspendedException("This account has been suspended. Please contact support.");
        }

        String token = jwtUtil.generateToken(provider.getId(), provider.getEmail(), "ROLE_PROVIDER");

        return new ServiceProviderLoginResponse(token, provider.getId(), provider.getFullName());
    }

    public ServiceProviderVerificationResponse getVerificationStatus(String email) {
        ServiceProvider provider = providerRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Provider not found"));

        return new ServiceProviderVerificationResponse(provider);
    }
}