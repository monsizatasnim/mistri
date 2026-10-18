package com.mistry.platform.service;

import com.mistry.platform.entity.Customer;
import com.mistry.platform.entity.RecipientType;
import com.mistry.platform.entity.ServiceProvider;
import com.mistry.platform.entity.ShopOwner;
import com.mistry.platform.exception.NotificationException;
import com.mistry.platform.repository.CustomerRepository;
import com.mistry.platform.repository.ServiceProviderRepository;
import com.mistry.platform.repository.ShopOwnerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipientDirectoryTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private ServiceProviderRepository serviceProviderRepository;
    @Mock
    private ShopOwnerRepository shopOwnerRepository;

    @InjectMocks
    private RecipientDirectory directory;

    private Authentication login(String name, String role) {
        return new UsernamePasswordAuthenticationToken(name, null, List.of(new SimpleGrantedAuthority(role)));
    }

    @Test
    void aCustomerIsFoundByPhoneWhenTheEmailDoesNotMatch() {
        Customer customer = mock(Customer.class);
        when(customer.getId()).thenReturn(2L);
        when(customerRepository.findByEmail("01711111111")).thenReturn(Optional.empty());
        when(customerRepository.findByPhone("01711111111")).thenReturn(Optional.of(customer));

        assertEquals(Optional.of(2L), directory.idOf(RecipientType.CUSTOMER, "01711111111"));
    }

    @Test
    void aBlankLoginFindsNobody() {
        assertTrue(directory.idOf(RecipientType.CUSTOMER, " ").isEmpty());
        assertTrue(directory.idOf(RecipientType.PROVIDER, null).isEmpty());
    }

    @Test
    void theProviderRoleResolvesToTheProviderAccount() {
        ServiceProvider provider = new ServiceProvider();
        provider.setId(7L);
        when(serviceProviderRepository.findByEmail("provider1@test.com")).thenReturn(Optional.of(provider));

        NotificationRecipient me = directory.currentRecipient(login("provider1@test.com", "ROLE_PROVIDER"));

        assertEquals(new NotificationRecipient(RecipientType.PROVIDER, 7L), me);
    }

    @Test
    void theShopOwnerRoleResolvesToTheShopOwnerAccount() {
        ShopOwner shopOwner = new ShopOwner();
        shopOwner.setId(3L);
        when(shopOwnerRepository.findByEmail("shop1@test.com")).thenReturn(Optional.of(shopOwner));

        NotificationRecipient me = directory.currentRecipient(login("shop1@test.com", "ROLE_SHOP_OWNER"));

        assertEquals(new NotificationRecipient(RecipientType.SHOP_OWNER, 3L), me);
    }

    @Test
    void anAdminHasNoNotifications() {
        NotificationException e = assertThrows(NotificationException.class,
                () -> directory.currentRecipient(login("admin@test.com", "ROLE_ADMIN")));

        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
    }

    @Test
    void aLoginWithoutAnAccountIsRefused() {
        NotificationException e = assertThrows(NotificationException.class,
                () -> directory.currentRecipient(login("ghost@test.com", "ROLE_CUSTOMER")));

        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
    }

    @Test
    void noLoginAtAllIsRefused() {
        NotificationException e = assertThrows(NotificationException.class,
                () -> directory.currentRecipient(null));

        assertEquals(HttpStatus.UNAUTHORIZED, e.getStatus());
    }

    @Test
    void theContactOfAProviderHasNamePhoneAndEmail() {
        ServiceProvider provider = new ServiceProvider();
        provider.setId(7L);
        provider.setFullName("Rahim");
        provider.setPhone("01611111111");
        provider.setEmail("rahim@test.com");
        when(serviceProviderRepository.findById(7L)).thenReturn(Optional.of(provider));

        RecipientDirectory.Contact contact = directory.contactOf(RecipientType.PROVIDER, 7L).orElseThrow();

        assertEquals(new RecipientDirectory.Contact("Rahim", "01611111111", "rahim@test.com"), contact);
    }

    @Test
    void theContactOfAnUnknownAccountIsEmpty() {
        assertTrue(directory.contactOf(RecipientType.SHOP_OWNER, 99L).isEmpty());
        assertTrue(directory.contactOf(RecipientType.CUSTOMER, null).isEmpty());
    }
}
