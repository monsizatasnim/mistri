package com.mistry.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the customer and provider booking endpoints (LSBP-56).
 * The Spring app must be running on localhost:8080.
 * Test accounts: customer 01333333333 / secret123, provider provider@test.com / provider123 (id 3).
 */
class CustomerBookingApiTest {

    private static final String BASE = "http://localhost:8080";
    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Random RANDOM = new Random();
    private static String customerToken;
    private static String providerToken;
    private static final List<Long> CREATED = new ArrayList<>();

    @AfterAll
    static void cancelCreatedBookings() throws Exception {
        for (Long id : CREATED) {
            call("POST", "/api/customer/bookings/" + id + "/cancel", null, customerToken);
        }
    }

    private static HttpResponse<String> call(String method, String path, String body, String token) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(BASE + path))
                .header("Content-Type", "application/json")
                .method(method, body == null
                        ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(body));
        if (token != null) b.header("Authorization", "Bearer " + token);
        return HTTP.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String login(String identifier, String password) throws Exception {
        HttpResponse<String> r = call("POST", "/api/auth/login",
                "{\"identifier\":\"" + identifier + "\",\"password\":\"" + password + "\"}", null);
        assertEquals(200, r.statusCode());
        return JSON.readTree(r.body()).get("token").asText();
    }

    @BeforeAll
    static void loginBoth() throws Exception {
        customerToken = login("01333333333", "secret123");
        providerToken = login("provider@test.com", "provider123");
    }

    // A different future time every call, so two tests never clash on the same slot.
    private static String futureTime() {
        return LocalDateTime.now().plusDays(60).plusMinutes(RANDOM.nextInt(100000)).withNano(0).toString();
    }

    private static long createBooking() throws Exception {
        String body = "{\"providerId\":3,\"serviceName\":\"Auto test service\",\"appointmentTime\":\"" + futureTime() + "\"}";
        HttpResponse<String> r = call("POST", "/api/customer/bookings", body, customerToken);
        assertEquals(201, r.statusCode(), r.body());
        JsonNode node = JSON.readTree(r.body());
        assertEquals("PENDING", node.get("status").asText());
        long newId = node.get("id").asLong();
        CREATED.add(newId);
        return newId;
    }

    @Test
    void customerBookingsNeedLogin() throws Exception {
        HttpResponse<String> r = call("GET", "/api/customer/bookings", null, null);
        assertTrue(r.statusCode() == 401 || r.statusCode() == 403, "status was " + r.statusCode());
    }

    @Test
    void providerCannotUseCustomerBookings() throws Exception {
        HttpResponse<String> r = call("GET", "/api/customer/bookings", null, providerToken);
        assertTrue(r.statusCode() == 401 || r.statusCode() == 403, "status was " + r.statusCode());
    }

    @Test
    void customerCanCreateBookingAndSeeItInTheList() throws Exception {
        long id = createBooking();
        HttpResponse<String> r = call("GET", "/api/customer/bookings", null, customerToken);
        assertEquals(200, r.statusCode());
        assertTrue(r.body().contains("\"id\":" + id), "booking " + id + " not in the list");
    }

    @Test
    void bookingWithoutProviderIsRejected() throws Exception {
        String body = "{\"serviceName\":\"No provider\",\"appointmentTime\":\"" + futureTime() + "\"}";
        HttpResponse<String> r = call("POST", "/api/customer/bookings", body, customerToken);
        assertEquals(400, r.statusCode());
    }

    @Test
    void customerCanCancelPendingBookingButNotTwice() throws Exception {
        long id = createBooking();
        HttpResponse<String> first = call("POST", "/api/customer/bookings/" + id + "/cancel", null, customerToken);
        assertEquals(200, first.statusCode(), first.body());
        assertEquals("CANCELLED", JSON.readTree(first.body()).get("status").asText());
        HttpResponse<String> second = call("POST", "/api/customer/bookings/" + id + "/cancel", null, customerToken);
        assertTrue(second.statusCode() >= 400, "second cancel status was " + second.statusCode());
    }

    @Test
    void providerCanAcceptStartAndCompleteBooking() throws Exception {
        long id = createBooking();
        String p = "/api/provider/bookings/" + id;
        HttpResponse<String> a = call("POST", p + "/accept", null, providerToken);
        assertEquals(200, a.statusCode(), a.body());
        assertEquals("CONFIRMED", JSON.readTree(a.body()).get("status").asText());
        HttpResponse<String> s = call("POST", p + "/start", null, providerToken);
        assertEquals(200, s.statusCode(), s.body());
        assertEquals("IN_PROGRESS", JSON.readTree(s.body()).get("status").asText());
        HttpResponse<String> c = call("POST", p + "/complete", null, providerToken);
        assertEquals(200, c.statusCode(), c.body());
        assertEquals("COMPLETED", JSON.readTree(c.body()).get("status").asText());
    }

    @Test
    void providerCanDeclineButNotAcceptAfterwards() throws Exception {
        long id = createBooking();
        String p = "/api/provider/bookings/" + id;
        HttpResponse<String> d = call("POST", p + "/decline", null, providerToken);
        assertEquals(200, d.statusCode(), d.body());
        assertEquals("DECLINED", JSON.readTree(d.body()).get("status").asText());
        HttpResponse<String> a = call("POST", p + "/accept", null, providerToken);
        assertEquals(409, a.statusCode());
    }

    @Test
    void providerListShowsCustomerName() throws Exception {
        long id = createBooking();
        HttpResponse<String> r = call("GET", "/api/provider/bookings", null, providerToken);
        assertEquals(200, r.statusCode());
        boolean found = false;
        for (JsonNode n : JSON.readTree(r.body())) {
            if (n.get("id").asLong() == id) {
                found = true;
                assertEquals("Other Person", n.get("customerName").asText());
            }
        }
        assertTrue(found, "booking " + id + " not in provider list");
    }
}
