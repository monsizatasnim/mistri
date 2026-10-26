package com.mistry.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingApiTest {

    private static final String BASE = "http://localhost:8080";
    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final ObjectMapper JSON = new ObjectMapper();
    private static String token;

    private static HttpResponse<String> call(String method, String path, String body, boolean withToken) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(BASE + path))
                .header("Content-Type", "application/json")
                .method(method, body == null
                        ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(body));
        if (withToken) b.header("Authorization", "Bearer " + token);
        return HTTP.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    @BeforeAll
    static void login() throws Exception {
        HttpResponse<String> r = call("POST", "/api/auth/login",
                "{\"identifier\":\"cust@example.com\",\"password\":\"secret123\"}", false);
        assertEquals(200, r.statusCode());
        token = JSON.readTree(r.body()).get("token").asText();
    }

    private static long createBooking() throws Exception {
        HttpResponse<String> r = call("POST", "/api/bookings", "{\"customerId\":1}", true);
        assertEquals(201, r.statusCode());
        JsonNode node = JSON.readTree(r.body());
        assertEquals("PENDING", node.get("status").asText());
        return node.get("id").asLong();
    }

    @Test
    void bookingNeedsLogin() throws Exception {
        HttpResponse<String> r = call("POST", "/api/bookings", "{\"customerId\":1}", false);
        assertTrue(r.statusCode() == 401 || r.statusCode() == 403, "status was " + r.statusCode());
    }

    @Test
    void customerCanCreateBookingAndSeeItInTheList() throws Exception {
        long id = createBooking();
        HttpResponse<String> r = call("GET", "/api/bookings/customer/1", null, true);
        assertEquals(200, r.statusCode());
        assertTrue(r.body().contains("\"id\":" + id));
    }

    @Test
    void cancellingBeforeProviderResponseGivesFullRefund() throws Exception {
        long id = createBooking();
        HttpResponse<String> r = call("PUT", "/api/bookings/" + id + "/cancel", null, true);
        assertEquals(200, r.statusCode());
        assertEquals("CANCELLED_FULL_REFUND", JSON.readTree(r.body()).get("status").asText());
    }

    @Test
    void rescheduleSetsRequestedStatus() throws Exception {
        long id = createBooking();
        HttpResponse<String> r = call("PUT", "/api/bookings/" + id + "/reschedule", null, true);
        assertEquals(200, r.statusCode());
        assertEquals("RESCHEDULE_REQUESTED", JSON.readTree(r.body()).get("status").asText());
    }

    @Test
    void pendingBookingCannotBeStarted() throws Exception {
        long id = createBooking();
        HttpResponse<String> r = call("PUT", "/api/bookings/" + id + "/start", null, true);
        assertTrue(r.statusCode() >= 400, "status was " + r.statusCode());
    }
}