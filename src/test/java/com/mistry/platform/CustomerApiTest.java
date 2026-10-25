package com.mistry.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Backend (localhost:8080) cholte hobe
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CustomerApiTest {

    private static final String BASE = "http://localhost:8080";
    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final ObjectMapper JSON = new ObjectMapper();

    private static String email;
    private static String phone;
    private static String token;
    private static long paymentId;

    @BeforeAll
    static void init() {
        long now = System.currentTimeMillis();
        email = "apitest" + now + "@example.com";
        phone = "017" + String.valueOf(now).substring(5);
    }

    private static HttpResponse<String> postJson(String path, String body) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        return HTTP.send(req, HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> authed(String method, String path) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE + path))
                .header("Authorization", "Bearer " + token)
                .method(method, HttpRequest.BodyPublishers.noBody()).build();
        return HTTP.send(req, HttpResponse.BodyHandlers.ofString());
    }

    private static String registerBody() {
        return "{\"fullName\":\"Api Test\",\"phone\":\"" + phone + "\",\"email\":\"" + email
                + "\",\"password\":\"secret123\",\"defaultLocation\":\"Dhaka\"}";
    }

    @Test
    @Order(1)
    void customerCanRegister() throws Exception {
        HttpResponse<String> r = postJson("/api/auth/register", registerBody());
        assertEquals(200, r.statusCode());
        assertTrue(r.body().contains("Registration successful"));
    }

    @Test
    @Order(2)
    void duplicateRegistrationIsRejected() throws Exception {
        HttpResponse<String> r = postJson("/api/auth/register", registerBody());
        assertTrue(r.statusCode() >= 400 && r.statusCode() < 500, "status was " + r.statusCode());
    }

    @Test
    @Order(3)
    void registrationWithoutPasswordIsRejected() throws Exception {
        HttpResponse<String> r = postJson("/api/auth/register",
                "{\"fullName\":\"X\",\"phone\":\"01900000000\",\"email\":\"x@example.com\"}");
        assertTrue(r.statusCode() >= 400 && r.statusCode() < 500, "status was " + r.statusCode());
    }

    @Test
    @Order(4)
    void customerCanLogIn() throws Exception {
        HttpResponse<String> r = postJson("/api/auth/login",
                "{\"identifier\":\"" + email + "\",\"password\":\"secret123\"}");
        assertEquals(200, r.statusCode());
        JsonNode node = JSON.readTree(r.body());
        token = node.get("token").asText();
        assertNotNull(token);
        assertEquals("ROLE_CUSTOMER", node.get("role").asText());
    }

    @Test
    @Order(5)
    void wrongPasswordIsRejected() throws Exception {
        HttpResponse<String> r = postJson("/api/auth/login",
                "{\"identifier\":\"" + email + "\",\"password\":\"wrongpass\"}");
        assertTrue(r.statusCode() >= 400 && r.statusCode() < 500, "status was " + r.statusCode());
    }

    @Test
    @Order(6)
    void protectedEndpointNeedsToken() throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE + "/api/customer/payments")).GET().build();
        HttpResponse<String> r = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        assertTrue(r.statusCode() == 401 || r.statusCode() == 403, "status was " + r.statusCode());
    }

    @Test
    @Order(7)
    void customerCanMakeAPayment() throws Exception {
        HttpResponse<String> r = authed("POST", "/api/customer/payments?bookingId=1&amount=1500&paymentMethod=bKash");
        assertEquals(200, r.statusCode());
        JsonNode node = JSON.readTree(r.body());
        assertEquals("SUCCESS", node.get("status").asText());
        assertEquals(1500.0, node.get("amount").asDouble());
        paymentId = node.get("id").asLong();
    }

    @Test
    @Order(8)
    void paymentAppearsInHistory() throws Exception {
        HttpResponse<String> r = authed("GET", "/api/customer/payments");
        assertEquals(200, r.statusCode());
        assertTrue(r.body().contains("\"id\":" + paymentId));
    }

    @Test
    @Order(9)
    void customerCanOpenReceipt() throws Exception {
        HttpResponse<String> r = authed("GET", "/api/customer/payments/" + paymentId + "/receipt");
        assertEquals(200, r.statusCode());
        assertTrue(r.body().contains("receiptNo"));
    }

    @Test
    @Order(10)
    void zeroAmountPaymentIsRejected() throws Exception {
        HttpResponse<String> r = authed("POST", "/api/customer/payments?bookingId=1&amount=0&paymentMethod=bKash");
        assertEquals(400, r.statusCode());
    }
}