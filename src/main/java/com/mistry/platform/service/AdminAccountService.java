package com.mistry.platform.service;

import com.mistry.platform.dto.CustomerAccountResponse;
import com.mistry.platform.dto.ProviderAccountResponse;
import com.mistry.platform.entity.AccountStatus;
import com.mistry.platform.entity.Customer;
import com.mistry.platform.entity.ServiceProvider;
import com.mistry.platform.exception.AccountNotFoundException;
import com.mistry.platform.repository.CustomerRepository;
import com.mistry.platform.repository.ServiceProviderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminAccountService {

    private final CustomerRepository customerRepository;
    private final ServiceProviderRepository serviceProviderRepository;

    public AdminAccountService(CustomerRepository customerRepository,
                               ServiceProviderRepository serviceProviderRepository) {
        this.customerRepository = customerRepository;
        this.serviceProviderRepository = serviceProviderRepository;
    }

    public List<CustomerAccountResponse> searchCustomers(String search, AccountStatus status) {
        return customerRepository.findAll().stream()
                .filter(c -> matchesSearch(search, c.getFullName(), c.getEmail(), c.getPhone()))
                .filter(c -> status == null || c.getAccountStatus() == status)
                .map(this::toCustomerResponse)
                .toList();
    }

    public CustomerAccountResponse getCustomer(Long id) {
        return toCustomerResponse(findCustomerOrThrow(id));
    }

    public CustomerAccountResponse suspendCustomer(Long id) {
        Customer customer = findCustomerOrThrow(id);
        customer.setAccountStatus(AccountStatus.SUSPENDED);
        return toCustomerResponse(customerRepository.save(customer));
    }

    public CustomerAccountResponse reactivateCustomer(Long id) {
        Customer customer = findCustomerOrThrow(id);
        customer.setAccountStatus(AccountStatus.ACTIVE);
        return toCustomerResponse(customerRepository.save(customer));
    }

    public List<ProviderAccountResponse> searchProviders(String search, AccountStatus status) {
        return serviceProviderRepository.findAll().stream()
                .filter(p -> matchesSearch(search, p.getFullName(), p.getEmail(), p.getPhone()))
                .filter(p -> status == null || p.getAccountStatus() == status)
                .map(this::toProviderResponse)
                .toList();
    }

    public ProviderAccountResponse getProvider(Long id) {
        return toProviderResponse(findProviderOrThrow(id));
    }

    public ProviderAccountResponse suspendProvider(Long id) {
        ServiceProvider provider = findProviderOrThrow(id);
        provider.setAccountStatus(AccountStatus.SUSPENDED);
        return toProviderResponse(serviceProviderRepository.save(provider));
    }

    public ProviderAccountResponse reactivateProvider(Long id) {
        ServiceProvider provider = findProviderOrThrow(id);
        provider.setAccountStatus(AccountStatus.ACTIVE);
        return toProviderResponse(serviceProviderRepository.save(provider));
    }

    private Customer findCustomerOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("No customer found with id: " + id));
    }

    private ServiceProvider findProviderOrThrow(Long id) {
        return serviceProviderRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("No service provider found with id: " + id));
    }

    private boolean matchesSearch(String search, String... fields) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String lower = search.toLowerCase();
        for (String field : fields) {
            if (field != null && field.toLowerCase().contains(lower)) {
                return true;
            }
        }
        return false;
    }

    private CustomerAccountResponse toCustomerResponse(Customer c) {
        return new CustomerAccountResponse(c.getId(), c.getFullName(), c.getEmail(), c.getPhone(),
                c.getDefaultLocation(), c.getAccountStatus(), c.getCreatedAt());
    }

    private ProviderAccountResponse toProviderResponse(ServiceProvider p) {
        return new ProviderAccountResponse(p.getId(), p.getFullName(), p.getEmail(), p.getPhone(),
                p.getNidNumber(), p.getTradeLicenseNumber(), p.getVerificationStatus(),
                p.getAccountStatus(), p.getCreatedAt());
    }
}