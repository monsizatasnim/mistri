package com.mistry.platform.service;

import com.mistry.platform.dto.LoginRequest;
import com.mistry.platform.dto.LoginResponse;
import com.mistry.platform.entity.AccountStatus;
import com.mistry.platform.entity.Admin;
import com.mistry.platform.entity.Customer;
import com.mistry.platform.entity.ServiceProvider;
import com.mistry.platform.entity.ShopOwner;
import com.mistry.platform.entity.VerificationStatus;
import com.mistry.platform.exception.AccountNotVerifiedException;
import com.mistry.platform.exception.AccountSuspendedException;
import com.mistry.platform.repository.AdminRepository;
import com.mistry.platform.repository.CustomerRepository;
import com.mistry.platform.repository.ServiceProviderRepository;
import com.mistry.platform.repository.ShopOwnerRepository;
import com.mistry.platform.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final CustomerRepository customerRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final ShopOwnerRepository shopOwnerRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {

        String identifier = request.getIdentifier();
        String password = request.getPassword();

        Optional<Customer> customerOpt = customerRepository.findByEmail(identifier);
        if (customerOpt.isEmpty()) {
            customerOpt = customerRepository.findByPhone(identifier);
        }
        if (customerOpt.isPresent()) {
            return loginCustomer(customerOpt.get(), password, identifier);
        }

        Optional<ServiceProvider> providerOpt = serviceProviderRepository.findByEmail(identifier);
        if (providerOpt.isPresent()) {
            return loginServiceProvider(providerOpt.get(), password);
        }

        Optional<ShopOwner> shopOwnerOpt = shopOwnerRepository.findByEmail(identifier);
        if (shopOwnerOpt.isEmpty()) {
            shopOwnerOpt = shopOwnerRepository.findByPhone(identifier);
        }
        if (shopOwnerOpt.isPresent()) {
            return loginShopOwner(shopOwnerOpt.get(), password, identifier);
        }

        Optional<Admin> adminOpt = adminRepository.findByEmail(identifier);
        if (adminOpt.isPresent()) {
            return loginAdmin(adminOpt.get(), password);
        }

        throw new BadCredentialsException("Invalid email/phone or password");
    }

    private LoginResponse loginCustomer(Customer customer, String password, String identifier) {
        if (!passwordEncoder.matches(password, customer.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email/phone or password");
        }
        String token = jwtUtil.generateToken(customer.getId(), identifier, "ROLE_CUSTOMER");
        return new LoginResponse(token, customer.getId(), customer.getFullName(), "ROLE_CUSTOMER");
    }

    private LoginResponse loginServiceProvider(ServiceProvider provider, String password) {
        if (!passwordEncoder.matches(password, provider.getPassword())) {
            throw new BadCredentialsException("Invalid email/phone or password");
        }
        if (provider.getAccountStatus() == AccountStatus.SUSPENDED) {
            throw new AccountSuspendedException("This account has been suspended. Please contact support.");
        }
        String token = jwtUtil.generateToken(provider.getId(), provider.getEmail(), "ROLE_PROVIDER");
        return new LoginResponse(token, provider.getId(), provider.getFullName(), "ROLE_PROVIDER");
    }

    private LoginResponse loginShopOwner(ShopOwner shopOwner, String password, String identifier) {
        if (!passwordEncoder.matches(password, shopOwner.getPassword())) {
            throw new BadCredentialsException("Invalid email/phone or password");
        }
        if (shopOwner.getAccountStatus() == AccountStatus.SUSPENDED) {
            throw new AccountSuspendedException("This account has been suspended. Please contact support.");
        }
        if (shopOwner.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new AccountNotVerifiedException(
                    "Your shop registration is still pending admin verification. You cannot log in yet.");
        }
        String token = jwtUtil.generateToken(shopOwner.getId(), identifier, "ROLE_SHOP_OWNER");
        return new LoginResponse(token, shopOwner.getId(), shopOwner.getOwnerName(), "ROLE_SHOP_OWNER");
    }

    private LoginResponse loginAdmin(Admin admin, String password) {
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new BadCredentialsException("Invalid email/phone or password");
        }
        String token = jwtUtil.generateToken(admin.getAdminId(), admin.getEmail(), admin.getRole());
        return new LoginResponse(token, admin.getAdminId(), admin.getFullName(), admin.getRole());
    }
}
