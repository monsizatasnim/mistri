package com.mistry.platform.service;

import com.mistry.platform.dto.ServiceProviderRegisterResponse;
import com.mistry.platform.entity.ServiceProvider;
import com.mistry.platform.entity.VerificationStatus;
import com.mistry.platform.exception.DuplicateAccountException;
import com.mistry.platform.repository.ServiceProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
}