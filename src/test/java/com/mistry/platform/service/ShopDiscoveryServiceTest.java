package com.mistry.platform.service;

import com.mistry.platform.dto.NearbyShopResponse;
import com.mistry.platform.entity.AccountStatus;
import com.mistry.platform.entity.ShopOwner;
import com.mistry.platform.entity.VerificationStatus;
import com.mistry.platform.exception.ShopOrderException;
import com.mistry.platform.repository.ShopOwnerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShopDiscoveryServiceTest {

    @Mock
    private ShopOwnerRepository shopOwnerRepository;

    @InjectMocks
    private ShopDiscoveryService service;

    private ShopOwner shop(long id, String name, String location) {
        ShopOwner s = new ShopOwner();
        s.setId(id);
        s.setShopName(name);
        s.setOwnerName("Owner " + id);
        s.setLocation(location);
        s.setContactInfo("0190000000" + id);
        s.setVerificationStatus(VerificationStatus.VERIFIED);
        s.setAccountStatus(AccountStatus.ACTIVE);
        return s;
    }

    @Test
    void shopsAreSortedByHowManyLocationWordsMatch() {
        ShopOwner uttara = shop(2L, "Rahim Electricals", "Uttara, Dhaka");
        ShopOwner mirpur = shop(1L, "Karim Hardware", "Mirpur 10, Dhaka");
        when(shopOwnerRepository.findByVerificationStatus(VerificationStatus.VERIFIED))
                .thenReturn(List.of(uttara, mirpur));

        List<NearbyShopResponse> result = service.findNearby("Mirpur, Dhaka");

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).shopOwnerId());
        assertEquals(List.of("dhaka", "mirpur"), result.get(0).matchedTerms());
        assertEquals(2L, result.get(1).shopOwnerId());
        assertEquals(List.of("dhaka"), result.get(1).matchedTerms());
    }

    @Test
    void shopsWithNoMatchingWordAreLeftOut() {
        when(shopOwnerRepository.findByVerificationStatus(VerificationStatus.VERIFIED))
                .thenReturn(List.of(shop(2L, "Rahim Electricals", "Uttara, Dhaka")));

        assertTrue(service.findNearby("Chittagong").isEmpty());
    }

    @Test
    void suspendedShopsAreNotShown() {
        ShopOwner suspended = shop(3L, "Closed Shop", "Mirpur, Dhaka");
        suspended.setAccountStatus(AccountStatus.SUSPENDED);
        when(shopOwnerRepository.findByVerificationStatus(VerificationStatus.VERIFIED))
                .thenReturn(List.of(suspended));

        assertTrue(service.findNearby("Mirpur").isEmpty());
    }

    @Test
    void blankLocationIsRejected() {
        ShopOrderException e = assertThrows(ShopOrderException.class, () -> service.findNearby("  "));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
    }

    @Test
    void shopThatIsNotVerifiedCannotBeViewed() {
        ShopOwner pending = shop(5L, "New Shop", "Mirpur, Dhaka");
        pending.setVerificationStatus(VerificationStatus.PENDING);
        when(shopOwnerRepository.findById(5L)).thenReturn(Optional.of(pending));

        ShopOrderException e = assertThrows(ShopOrderException.class, () -> service.getShop(5L));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
    }
}