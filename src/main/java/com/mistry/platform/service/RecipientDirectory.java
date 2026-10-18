package com.mistry.platform.service;

import com.mistry.platform.entity.Customer;
import com.mistry.platform.entity.RecipientType;
import com.mistry.platform.entity.ServiceProvider;
import com.mistry.platform.entity.ShopOwner;
import com.mistry.platform.exception.NotificationException;
import com.mistry.platform.repository.CustomerRepository;
import com.mistry.platform.repository.ServiceProviderRepository;
import com.mistry.platform.repository.ShopOwnerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Optional;

/** Finds customers, providers and shop owners, by login, by id, and for the logged-in user. */
@Service
public class RecipientDirectory {

    /** How to reach someone. Either phone or email can be missing. */
    public record Contact(String name, String phone, String email) {
    }

    private final CustomerRepository customerRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final ShopOwnerRepository shopOwnerRepository;

    public RecipientDirectory(CustomerRepository customerRepository,
                              ServiceProviderRepository serviceProviderRepository,
                              ShopOwnerRepository shopOwnerRepository) {
        this.customerRepository = customerRepository;
        this.serviceProviderRepository = serviceProviderRepository;
        this.shopOwnerRepository = shopOwnerRepository;
    }

    /** The logged-in customer, provider or shop owner. Admins have no notifications. */
    public NotificationRecipient currentRecipient(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new NotificationException(HttpStatus.UNAUTHORIZED, "Please log in first.");
        }

        RecipientType type = null;
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            type = typeOfRole(authority.getAuthority());
            if (type != null) {
                break;
            }
        }
        if (type == null) {
            throw new NotificationException(HttpStatus.FORBIDDEN,
                    "Notifications are available to customers, service providers and shop owners.");
        }

        Long id = idOf(type, authentication.getName())
                .orElseThrow(() -> new NotificationException(HttpStatus.FORBIDDEN, "Account not found."));
        return new NotificationRecipient(type, id);
    }

    /** The id of the account that logs in with this email or phone number. */
    public Optional<Long> idOf(RecipientType type, String login) {
        if (login == null || login.isBlank()) {
            return Optional.empty();
        }
        return switch (type) {
            case CUSTOMER -> customerRepository.findByEmail(login)
                    .or(() -> customerRepository.findByPhone(login))
                    .map(Customer::getId);
            case PROVIDER -> serviceProviderRepository.findByEmail(login)
                    .or(() -> serviceProviderRepository.findByPhone(login))
                    .map(ServiceProvider::getId);
            case SHOP_OWNER -> shopOwnerRepository.findByEmail(login)
                    .or(() -> shopOwnerRepository.findByPhone(login))
                    .map(ShopOwner::getId);
        };
    }

    public Optional<Contact> contactOf(RecipientType type, Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return switch (type) {
            case CUSTOMER -> customerRepository.findById(id)
                    .map(c -> new Contact(c.getFullName(), c.getPhone(), c.getEmail()));
            case PROVIDER -> serviceProviderRepository.findById(id)
                    .map(p -> new Contact(p.getFullName(), p.getPhone(), p.getEmail()));
            case SHOP_OWNER -> shopOwnerRepository.findById(id)
                    .map(s -> new Contact(s.getOwnerName(), s.getPhone(), s.getEmail()));
        };
    }

    private RecipientType typeOfRole(String role) {
        if ("ROLE_CUSTOMER".equals(role)) {
            return RecipientType.CUSTOMER;
        }
        if ("ROLE_PROVIDER".equals(role)) {
            return RecipientType.PROVIDER;
        }
        if ("ROLE_SHOP_OWNER".equals(role)) {
            return RecipientType.SHOP_OWNER;
        }
        return null;
    }
}
