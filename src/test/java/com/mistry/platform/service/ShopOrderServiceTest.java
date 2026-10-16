package com.mistry.platform.service;

import com.mistry.platform.dto.PlaceShopOrderRequest;
import com.mistry.platform.dto.ShopOrderItemRequest;
import com.mistry.platform.dto.ShopOrderResponse;
import com.mistry.platform.entity.AccountStatus;
import com.mistry.platform.entity.OrderTerm;
import com.mistry.platform.entity.Product;
import com.mistry.platform.entity.ProductAvailabilityType;
import com.mistry.platform.entity.ServiceProvider;
import com.mistry.platform.entity.ShopOrder;
import com.mistry.platform.entity.ShopOrderStatus;
import com.mistry.platform.entity.ShopOwner;
import com.mistry.platform.entity.StockStatus;
import com.mistry.platform.entity.VerificationStatus;
import com.mistry.platform.exception.ShopOrderException;
import com.mistry.platform.repository.ProductRepository;
import com.mistry.platform.repository.ServiceProviderRepository;
import com.mistry.platform.repository.ShopOrderRepository;
import com.mistry.platform.repository.ShopOwnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShopOrderServiceTest {

    private static final String PROVIDER_EMAIL = "provider@test.com";

    @Mock
    private ShopOrderRepository orderRepository;
    @Mock
    private ShopOwnerRepository shopOwnerRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ServiceProviderRepository serviceProviderRepository;

    @InjectMocks
    private ShopOrderService service;

    private ServiceProvider provider;

    @BeforeEach
    void setUp() {
        provider = new ServiceProvider();
        provider.setId(7L);
        provider.setAccountStatus(AccountStatus.ACTIVE);
        when(serviceProviderRepository.findByEmail(PROVIDER_EMAIL)).thenReturn(Optional.of(provider));

        ShopOwner shop = new ShopOwner();
        shop.setId(3L);
        shop.setShopName("Karim Hardware");
        shop.setVerificationStatus(VerificationStatus.VERIFIED);
        shop.setAccountStatus(AccountStatus.ACTIVE);
        when(shopOwnerRepository.findById(3L)).thenReturn(Optional.of(shop));

        when(orderRepository.save(any(ShopOrder.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private void product(long id, ProductAvailabilityType type, StockStatus stock) {
        Product p = new Product();
        p.setId(id);
        p.setShopOwnerId(3L);
        p.setName("Power Drill");
        p.setAvailabilityType(type);
        p.setSalePrice(new BigDecimal("3500"));
        p.setRentPrice(new BigDecimal("300"));
        p.setStockStatus(stock);
        when(productRepository.findByIdAndShopOwnerId(id, 3L)).thenReturn(Optional.of(p));
    }

    private PlaceShopOrderRequest request(Boolean urgent, OrderTerm term) {
        return new PlaceShopOrderRequest(3L, urgent, " Mirpur 10 ", null,
                List.of(new ShopOrderItemRequest(11L, 2, term)));
    }

    private ShopOrder order(ShopOrderStatus status) {
        ShopOrder o = new ShopOrder();
        o.setId(5L);
        o.setProviderId(7L);
        o.setShopOwnerId(3L);
        o.setStatus(status);
        o.setTotalAmount(BigDecimal.TEN);
        when(orderRepository.findByIdAndProviderId(5L, 7L)).thenReturn(Optional.of(o));
        return o;
    }

    @Test
    void placeOrderCalculatesTotalAndKeepsUrgentFlag() {
        product(11L, ProductAvailabilityType.BOTH, StockStatus.AVAILABLE);

        ShopOrderResponse response = service.placeOrder(PROVIDER_EMAIL, request(true, OrderTerm.RENT));

        assertTrue(response.urgent());
        assertEquals(ShopOrderStatus.PLACED, response.status());
        assertEquals(0, new BigDecimal("600").compareTo(response.totalAmount()));
        assertEquals(1, response.items().size());
        assertEquals("Mirpur 10", response.deliveryLocation());
        assertEquals("Karim Hardware", response.shopName());
    }

    @Test
    void orderIsNotUrgentUnlessTheProviderSaysSo() {
        product(11L, ProductAvailabilityType.BOTH, StockStatus.AVAILABLE);

        ShopOrderResponse response = service.placeOrder(PROVIDER_EMAIL, request(null, OrderTerm.SALE));

        assertFalse(response.urgent());
        assertEquals(0, new BigDecimal("7000").compareTo(response.totalAmount()));
    }

    @Test
    void rentingASaleOnlyProductIsRejected() {
        product(11L, ProductAvailabilityType.SALE, StockStatus.AVAILABLE);

        ShopOrderException e = assertThrows(ShopOrderException.class,
                () -> service.placeOrder(PROVIDER_EMAIL, request(false, OrderTerm.RENT)));

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        verify(orderRepository, never()).save(any(ShopOrder.class));
    }

    @Test
    void outOfStockProductIsRejected() {
        product(11L, ProductAvailabilityType.BOTH, StockStatus.OUT_OF_STOCK);

        ShopOrderException e = assertThrows(ShopOrderException.class,
                () -> service.placeOrder(PROVIDER_EMAIL, request(false, OrderTerm.SALE)));

        assertEquals(HttpStatus.CONFLICT, e.getStatus());
    }

    @Test
    void orderWithoutItemsIsRejected() {
        PlaceShopOrderRequest empty = new PlaceShopOrderRequest(3L, false, null, null, List.of());

        ShopOrderException e = assertThrows(ShopOrderException.class,
                () -> service.placeOrder(PROVIDER_EMAIL, empty));

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
    }

    @Test
    void suspendedProviderCannotOrder() {
        provider.setAccountStatus(AccountStatus.SUSPENDED);

        ShopOrderException e = assertThrows(ShopOrderException.class,
                () -> service.placeOrder(PROVIDER_EMAIL, request(false, OrderTerm.SALE)));

        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
    }

    @Test
    void markUrgentWorksWhileTheOrderIsWaitingForTheShop() {
        order(ShopOrderStatus.PLACED);

        assertTrue(service.markUrgent(PROVIDER_EMAIL, 5L).urgent());
    }

    @Test
    void markUrgentIsRejectedOnceTheShopHasAccepted() {
        order(ShopOrderStatus.ACCEPTED);

        ShopOrderException e = assertThrows(ShopOrderException.class,
                () -> service.markUrgent(PROVIDER_EMAIL, 5L));

        assertEquals(HttpStatus.CONFLICT, e.getStatus());
    }
}